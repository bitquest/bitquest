package com.bitquest.bitquest.events;

import com.bitquest.bitquest.BitQuest;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvents implements Listener {

  BitQuest bitQuest;

  public ChatEvents(BitQuest plugin) {
    bitQuest = plugin;
  }

  @EventHandler
  public void onChat(AsyncPlayerChatEvent event) {
    String message = event.getMessage();
    Player sender = event.getPlayer();
    if (message.endsWith("!")) {
      if (message.length() > 1 && message.substring(1, message.length()).trim().length() >= 1) {
        event.setMessage(message);
        bitQuest.sendDiscordMessage(sender.getName() + ": " + message);
        // Spigot replaces "%1$s" with the player's name and "%2$s" with the original message.
        event.setFormat(
            ChatColor.BLUE.toString()
                + sender.getLevel()
                + " "
                + ChatColor.YELLOW
                + "%1$s "
                + ChatColor.WHITE
                + "%2$s");
      } else {
        event.setCancelled(true);
      }
    } else {
      message =
          ChatColor.BLUE
              + ChatColor.BOLD.toString()
              + "Local> "
              + ChatColor.YELLOW
              + sender.getName()
              + " "
              + ChatColor.WHITE
              + message;
      event.setCancelled(true);
      for (Player player : Bukkit.getServer().getOnlinePlayers()) {
        if (
            player.getLocation().getWorld().getName().equals(sender.getLocation().getWorld().getName()) &&
            player.getLocation().distance(sender.getLocation()) < 10000000
        ) {
          player.sendMessage(message);
        }
      }
    }
  }
}
