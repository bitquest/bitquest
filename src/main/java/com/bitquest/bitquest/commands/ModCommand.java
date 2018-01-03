package com.bitquest.bitquest.commands;


import com.bitquest.bitquest.BitQuest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class ModCommand extends CommandAction {
    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        if (args.length > 0) {
            if (args[0].equals("add")) {
                // Sub-command: /mod add
                if (args.length > 1) {
                    if (BitQuest.REDIS.exists("uuid:" + args[1])) {
                        UUID uuid = UUID.fromString(BitQuest.REDIS.get("uuid:" + args[1]));
                        BitQuest.REDIS.sadd("moderators", uuid.toString());
                        sender.sendMessage(ChatColor.GREEN + BitQuest.REDIS.get("name:" + uuid) + " added to moderators group");

                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Cannot find player " + args[1]);
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /mod add <player>");
                    return true;
                }
            } else if (args[0].equals("remove")) {
                // Sub-command: /mod del
                if (args.length > 1) {
                    if (BitQuest.REDIS.exists("uuid:" + args[1])) {
                        UUID uuid = UUID.fromString(BitQuest.REDIS.get("uuid:" + args[1]));
                        BitQuest.REDIS.srem("moderators", uuid.toString());
                        return true;
                    }
                    return false;
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /mod remove <player>");
                    return true;
                }
            } else if (args[0].equals("list")) {
                // Sub-command: /mod list
                Set<String> moderators = BitQuest.REDIS.smembers("moderators");
                for (String uuid : moderators) {
                    sender.sendMessage(ChatColor.YELLOW + BitQuest.REDIS.get("name:" + uuid));
                }
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Usage: /mod <add|remove>");
                return true;
            }
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /mod <add|remove>");
            return true;
        }
    }
}
