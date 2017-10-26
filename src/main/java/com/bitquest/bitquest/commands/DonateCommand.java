package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.User;
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

    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        if(args.length!=1) {
            try {
                int bits=Integer.valueOf(args[0]);
                int sat=bits*100;
                User user=new User(player);
                if(user.wallet.getBalance(0)>sat) {
                    if (user.wallet.move("donations", sat)) {
                        player.sendMessage(ChatColor.GREEN+"Thanks for your support!");

                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED+"Donation failed");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED+"Not enough balance to donate "+bits);
                    return true;
                }
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
            return false;
        }
    }
}
