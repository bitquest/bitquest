package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnbanCommand extends CommandAction {
  private final BitQuest bitQuest;

  public UnbanCommand(BitQuest plugin) {
    this.bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    if (args.length == 1) {
      String playerName = args[0];
      if (bitQuest.redis.exists("uuid:" + playerName)) {
        String uuid = bitQuest.redis.get("uuid:" + playerName);
        bitQuest.redis.srem("banlist", uuid);
        sender.sendMessage(
            ChatColor.GREEN
                + "Player "
                + ChatColor.BLUE
                + playerName
                + ChatColor.GREEN
                + " has been unbanned.");

      } else {
        player.sendMessage(ChatColor.RED + "Usage: /unban <player>");
        return true;
      }
    }
    return false;
  }
}
