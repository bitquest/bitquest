package com.bitquest.bitquest.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EmergencystopCommand extends CommandAction {
    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        StringBuilder message = new StringBuilder();
        message.append(sender.getName())
            .append(" has shut down the server for emergency reasons");

        if (args.length > 0) {
            message.append(": ");
            for (String word: args) {
                message.append(word).append(" ");
            }
        }
        for (Player currentPlayer: Bukkit.getOnlinePlayers()) {
            currentPlayer.kickPlayer(message.toString());
        }

        Bukkit.shutdown();
        return true;
    }
}
