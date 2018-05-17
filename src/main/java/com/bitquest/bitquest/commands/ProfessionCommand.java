package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProfessionCommand extends CommandAction {
  private BitQuest bitQuest;

  public ProfessionCommand(BitQuest plugin) {
    this.bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    if (args.length > 0) {
      String profession = args[0];
      if (profession.equals("rogue")) {
        bitQuest.REDIS.set("profession:" + player.getUniqueId(), profession);
      }
    }
    return true;
  }
}
