package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.User;
import com.bitquest.bitquest.Wallet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.text.ParseException;


public class DonateCommand extends CommandAction {
    private BitQuest bitQuest;

    public DonateCommand(BitQuest plugin) {
        bitQuest = plugin;
    }

    public boolean run(CommandSender sender, Command cmd, String label, String[] args, final Player player) {
        if(args.length == 1) {
            try {
                final int bits=Integer.valueOf(args[0]);
                final int sat=bits*bitQuest.DENOMINATION_FACTOR;
                final User user=new User(bitQuest, player);
                user.wallet.getBalance(0, new Wallet.GetBalanceCallback() {
                    @Override
                    public void run(Long balance) {
                        try {
                            if (balance > sat) {
                                if (user.wallet.move("donations", sat)) {
                                    player.sendMessage(ChatColor.GREEN + "Thanks for your support!");

                                } else {
                                    player.sendMessage(ChatColor.RED + "Donation failed");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Not enough balance to donate " + bits);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                return true;
            } catch (ParseException e) {

                e.printStackTrace();
                return true;
            } catch (org.json.simple.parser.ParseException e) {
                e.printStackTrace();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }

        } else {
            player.sendMessage(ChatColor.RED + "Usage: /donate <amount>");
            return true;
        }
    }
}
