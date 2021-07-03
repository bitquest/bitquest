package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageOfTheDayCommand extends CommandAction {
  private BitQuest bitQuest;

  public MessageOfTheDayCommand(BitQuest plugin) {
    this.bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    String motd = "";
    for (String arg : args) {
      motd += arg + " ";
    }
    bitQuest.redis.set("motd", motd);
    player.sendMessage(ChatColor.GREEN + "Message changed.");
    return true;
  }
}
