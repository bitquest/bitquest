package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.ChatColor;
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
    if (args.length == 1) {
      String profession = args[0];
      if (profession.equals("rogue")) {
        bitQuest.redis.set("profession:" + player.getUniqueId(), profession);
      }
    } else {
      player.sendMessage(ChatColor.RED + "Wrong argument count !");
    }
    return true;
  }
}
