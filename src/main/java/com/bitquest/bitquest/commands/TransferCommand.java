package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.User;
import com.bitquest.bitquest.Wallet;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.text.ParseException;


public class TransferCommand extends CommandAction {
    private BitQuest bitQuest;

    public TransferCommand(BitQuest plugin) {
        bitQuest = plugin;
    }

    public boolean run(CommandSender sender, Command cmd, String label, final String[] args, final Player player) {
        if (args.length == 2) {
            for (char c : args[0].toCharArray()) {
                if (!Character.isDigit(c))
                    return false;
            }
            int sendAmount = 0;
            try {
                sendAmount = Integer.valueOf(args[0]) * 100;
            } catch (NumberFormatException e) {
                return false;
            }
            System.out.println(sendAmount);
            Wallet fromWallet = null;
            try {
                fromWallet = new User(bitQuest, player).wallet;
            } catch (ParseException e1) {
                e1.printStackTrace();
            } catch (org.json.simple.parser.ParseException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (sendAmount < BitQuest.MIN_TRANS) {
                player.sendMessage(ChatColor.RED + "Minimum transaction is " + BitQuest.MIN_TRANS / 100 + " Bits.");
                return true;
            } else {
                if (fromWallet != null) {
                    final Wallet fromWalletFinal = fromWallet;
                    final int sendAmoutFinal = sendAmount;
                    fromWallet.getBalance(0, new Wallet.GetBalanceCallback() {
                        @Override
                        public void run(final Long unconfirmed_balance) {
                            if(unconfirmed_balance<sendAmoutFinal) {
                                player.sendMessage(ChatColor.RED + "Insufficient balance.");
                            } else {
                                fromWalletFinal.getBalance(5, new Wallet.GetBalanceCallback() {
                                    @Override
                                    public void run(Long balance) {
                                        if(unconfirmed_balance != balance) {
                                            player.sendMessage(ChatColor.YELLOW + "Sending " + args[0] + " Bits to " + args[1] + "...");
                                            try {
                                                String txid=fromWalletFinal.sendFrom(args[1], sendAmoutFinal);
                                                player.sendMessage(ChatColor.GREEN + "Succesfully sent " + args[0] + " Bits to external address.");
                                                player.sendMessage(ChatColor.BLUE+" "+ChatColor.UNDERLINE+ "https://live.blockcypher.com/btc-main/tx/"+txid);
                                                bitQuest.updateScoreboard(player);

                                            } catch (IOException e) {
                                                player.sendMessage(ChatColor.RED + "Transaction failed. Please try again in a few moments.");

                                                e.printStackTrace();
                                            } catch (org.json.simple.parser.ParseException e) {
                                                player.sendMessage(ChatColor.RED + "Transaction failed. Please try again in a few moments.");

                                                e.printStackTrace();
                                            } catch (ParseException e) {
                                                player.sendMessage(ChatColor.RED + "Transaction failed. Please try again in a few moments.");

                                                e.printStackTrace();
                                            }
                                        } else {
                                            player.sendMessage(ChatColor.RED + "You have unconfirmed transactions. please try again later.");
                                        }
                                    }
                                });
                            }
                        }
                    });
                    return true;
                }
                return true;

            }
        } else {
            return false;
        }
    }
}

