package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;


public class LandCommand extends CommandAction {
    private BitQuest bitQuest;

    public LandCommand(BitQuest plugin) {
        this.bitQuest = plugin;
    }

    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        if (bitQuest.rate_limit == false) {
            bitQuest.rate_limit = true;
            if (args[0].equalsIgnoreCase("claim")) {
                StringBuilder sb = new StringBuilder(args[1]);
                //            for (int i = 3; i < args.length; i++){
                //                sb.append(" " + args[i]);
                //            }
                String claimName = sb.toString().trim();

                Location location = player.getLocation();
                if (!location.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                    player.sendMessage(ChatColor.DARK_RED + "You cannot claim land here.");
                    return true;
                }

                try {
                    bitQuest.claimLand(claimName, location.getChunk(), player);
                } catch (ParseException e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "Land claim failed. Please try again later.");
                    return true;
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "Land claim failed. Please try again later.");
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "Land claim failed. Please try again later.");
                    return true;
                }
                return true;
            } else if (args[0].equalsIgnoreCase("permission")) {
                bitQuest.land_permission_cache = new HashMap();

                Location location = player.getLocation();
                int x = location.getChunk().getX();
                int z = location.getChunk().getZ();
                if (bitQuest.landIsClaimed(location) && bitQuest.isOwner(location, player)) {
                    String landname = BitQuest.REDIS.get("chunk" + x + "," + z + "name");

                    if (args[1].equalsIgnoreCase("public")) {
                        BitQuest.REDIS.set("chunk" + location.getChunk().getX() + "," + location.getChunk().getZ() + "permissions", "p");
                        player.sendMessage(ChatColor.GREEN + "the land " + ChatColor.DARK_GREEN + landname + ChatColor.GREEN + " is now public");
                        return true;
                    } else if (args[1].equalsIgnoreCase("clan")) {
                        BitQuest.REDIS.set("chunk" + location.getChunk().getX() + "," + location.getChunk().getZ() + "permissions", "c");
                        player.sendMessage(ChatColor.GREEN + "the land " + ChatColor.DARK_GREEN + landname + ChatColor.GREEN + " is now clan-owned");
                        return true;
                    } else if (args[1].equalsIgnoreCase("private")) {
                        BitQuest.REDIS.del("chunk" + location.getChunk().getX() + "," + location.getChunk().getZ() + "permissions");
                        player.sendMessage(ChatColor.GREEN + "the land " + ChatColor.DARK_GREEN + landname + ChatColor.GREEN + " is now private");
                        return true;
                    } else {
                        return false;
                    }

                } else {
                    player.sendMessage(ChatColor.DARK_RED + "Only the owner of this location can change its permissions.");
                    return true;
                }
            }
            return false;
        } else {
            player.sendMessage(ChatColor.RED + "Connectivity to Blockchain is limited. Please try again in 5 seconds.");
            return true;
        }

    }
}
