package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KillAllVillagersCommand extends CommandAction {
  private BitQuest bitQuest;

  public KillAllVillagersCommand(BitQuest plugin) {
    this.bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    bitQuest.killAllVillagers();
    return true;
  }
}
