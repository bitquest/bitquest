package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends CommandAction {
  private BitQuest bitQuest;

  public SpawnCommand(BitQuest plugin) {
    this.bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    bitQuest.teleportToSpawn(player);
    return true;
  }
}
