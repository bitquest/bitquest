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


public class SendCommand extends CommandAction {
    private BitQuest bitQuest;

    public SendCommand(BitQuest plugin) {
        bitQuest = plugin;
    }

    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        /***********************************************************
         /send
         a player-to-player-transaction
         ***********************************************************/
        if(args.length==2) {
            for(char c : args[0].toCharArray()) {
                if(!Character.isDigit(c))
                    return false;
            }
            int bits=Integer.valueOf(args[0]);
            if(bits>0&&bits<=10000) {
                int sat=bits*100;
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if(onlinePlayer.getName().equalsIgnoreCase(args[1])) {
                        if (!args[1].equalsIgnoreCase(player.getDisplayName())) {

                            try {
                                final User user=new User(player);
                                User user_tip=new User(onlinePlayer);
                                if(user.wallet.move(user_tip.player.getUniqueId().toString(),sat)) {
                                    bitQuest.updateScoreboard(onlinePlayer);
                                    bitQuest.updateScoreboard(player);
                                    player.sendMessage(ChatColor.GREEN+"You sent "+bits+" bits to user "+onlinePlayer.getName());
                                    onlinePlayer.sendMessage(ChatColor.GREEN+"You got "+bits+" bits from user "+player.getName());
                                    return true;
                                } else {
                                    player.sendMessage(ChatColor.RED+"Tip failed.");
                                    return true;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                                player.sendMessage(ChatColor.RED+"Tip failed.");
                                return true;
                            } catch (org.json.simple.parser.ParseException e) {
                                e.printStackTrace();
                                player.sendMessage(ChatColor.RED+"Tip failed.");
                                return true;
                            } catch (IOException e) {
                                e.printStackTrace();
                                player.sendMessage(ChatColor.RED+"Tip failed.");
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED+"You cannot send to yourself!");
                        }
                    }
                }
                player.sendMessage(ChatColor.RED+"Player "+args[1]+" is not online");

                return true;
            } else {
                player.sendMessage("Minimum tip is 1 bit. Maximum is 10000");
                return true;
            }
        } else {
            return false;
        }
    }
}
