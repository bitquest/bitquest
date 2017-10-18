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

    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
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
                fromWallet = new User(player).wallet;
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
                    player.sendMessage(ChatColor.YELLOW + "Sending " + args[0] + " Bits to " + args[1] + "...");
                    try {
                        if (fromWallet.sendFrom(args[1], sendAmount)) {
                            player.sendMessage(ChatColor.GREEN + "Succesfully sent " + args[0] + " Bits to external address.");
                            bitQuest.updateScoreboard(player);
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "Transaction failed. Please try again in a few moments.");
                            return true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return true;
                    } catch (org.json.simple.parser.ParseException e) {
                        e.printStackTrace();
                        return true;
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return true;
                    }

                }
                return true;

            }
        } else {
            return false;
        }
    }
}

