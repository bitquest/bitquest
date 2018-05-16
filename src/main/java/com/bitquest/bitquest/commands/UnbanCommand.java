package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class UnbanCommand extends CommandAction {
    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        if (args.length == 1) {
            String playerName = args[0];
            if (BitQuest.REDIS.exists("uuid:" + playerName)) {
                String uuid = BitQuest.REDIS.get("uuid:" + playerName);
                BitQuest.REDIS.srem("banlist", uuid);
                sender.sendMessage(ChatColor.GREEN + "Player " + playerName + " has been unbanned.");

                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Can't find player " + playerName);
                return true;
            }

        }
        return false;
    }
}
