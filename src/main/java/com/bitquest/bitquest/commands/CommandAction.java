package com.bitquest.bitquest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class CommandAction {
  public abstract boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player);
}
