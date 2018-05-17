package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UpgradeWallet extends CommandAction {
  private BitQuest bitQuest;

  public UpgradeWallet(BitQuest plugin) {
    bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    player.sendMessage(
        "This command is deprecated. If you need to retrieve funds from old wallet please email bitquest@bitquest.co to get help.");
    return true;
  }
}
