package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanlistCommand extends CommandAction {
  BitQuest bitQuest;

  public BanlistCommand(BitQuest plugin) {
    bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    Set<String> banlist = bitQuest.redis.smembers("banlist");
    for (String uuid : banlist) {
      sender.sendMessage(ChatColor.YELLOW + bitQuest.redis.get("name:" + uuid));
    }
    return true;
  }
}
