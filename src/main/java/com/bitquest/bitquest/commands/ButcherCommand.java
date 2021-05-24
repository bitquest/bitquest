package com.bitquest.bitquest.commands;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ButcherCommand extends CommandAction {
  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    Chunk c = player.getLocation().getChunk();
    for (World w : Bukkit.getWorlds()) {
      List<Entity> entities = w.getEntities();
      int killed = 0;
      for (Entity entity : entities) {
        if (entity instanceof Player == false && entity.getLocation().getChunk().getX() == c.getX()
            && entity.getLocation().getChunk().getZ() == c.getZ()) {
          killed = killed + 1;
          entity.remove();
          System.out.println("[butcher] removed " + entity.getName());
        }
      }
      player.sendMessage(ChatColor.GREEN + "Killed " + killed + " entities on " + w.getName());
    }
    return false;
  }
}
