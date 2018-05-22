package com.bitquest.bitquest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

abstract public class CommandAction {
    abstract public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player);
}
