package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.LegacyWallet;
import com.bitquest.bitquest.User;
import com.bitquest.bitquest.Wallet;

import java.io.IOException;
import java.text.ParseException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TransferCommand extends CommandAction {
    private BitQuest bitQuest;

    public TransferCommand(BitQuest plugin) {
        bitQuest = plugin;
    }

    public boolean run(CommandSender sender, Command cmd, String label, final String[] args, final Player player) {
        if (args.length == 2) {
            try {
                if (args[0].length() > 8) {
                    // maximum transfer is 8 digits
                    return false;
                }
                for (char c : args[0].toCharArray()) {
                    if (!Character.isDigit(c)) return false;
                }
                Long sendAmount;
                sendAmount = Long.parseLong(args[0]) * BitQuest.DENOMINATION_FACTOR;


                System.out.println(sendAmount);
                Wallet fromWallet = null;
                fromWallet = new User(bitQuest.db_con, player.getUniqueId()).wallet;

                if (sendAmount < (BitQuest.MINIMUM_TRANSACTION * BitQuest.DENOMINATION_FACTOR)) {
                    player.sendMessage(
                            ChatColor.DARK_RED
                                    + "Minimum transaction is "
                                    + ChatColor.LIGHT_PURPLE
                                    + BitQuest.MINIMUM_TRANSACTION
                                    + " "
                                    + BitQuest.DENOMINATION_NAME
                                    + ChatColor.GREEN
                                    + ".");
                    return true;
                } else {
                    if (fromWallet != null) {
                        final LegacyWallet fromWalletFinal = new LegacyWallet(player.getUniqueId().toString());
                        final Long sendAmountFinal = sendAmount;
                        final Long unconfirmed_balance = fromWallet.getBalance(0);

                        if (unconfirmed_balance < sendAmountFinal) {
                            player.sendMessage(ChatColor.DARK_RED + "Insufficient balance.");
                        } else {
                            final long balance = fromWalletFinal.getBalance(5);
                            if (unconfirmed_balance != balance) {
                                player.sendMessage(
                                        ChatColor.YELLOW
                                                + "Sending "
                                                + ChatColor.LIGHT_PURPLE
                                                + args[0]
                                                + " "
                                                + BitQuest.DENOMINATION_NAME
                                                + ChatColor.YELLOW
                                                + " to "
                                                + ChatColor.BLUE
                                                + args[1]
                                                + ChatColor.YELLOW
                                                + "...");
                                String txid = fromWalletFinal.sendFrom(args[1], sendAmountFinal);
                                player.sendMessage(
                                        ChatColor.GREEN
                                                + "Succesfully sent "
                                                + ChatColor.LIGHT_PURPLE
                                                + args[0]
                                                + " "
                                                + BitQuest.DENOMINATION_NAME
                                                + ChatColor.GREEN
                                                + " to external address.");
                                player.sendMessage(
                                        ChatColor.DARK_BLUE
                                                + " "
                                                + ChatColor.UNDERLINE
                                                + "https://live.blockcypher.com/btc-main/tx/"
                                                + txid);
                                bitQuest.updateScoreboard(player);

                            } else {
                                player.sendMessage(
                                        ChatColor.RED
                                                + "You have unconfirmed transactions. please try again later.");
                            }
                        }
                    }
                    ;
                }
            } catch (Exception e) {
                player.sendMessage(ChatColor.DARK_RED+"FAIL");
                System.out.println(e);
            }
        } else {
            return false;
        }
        return true;
    };

}
