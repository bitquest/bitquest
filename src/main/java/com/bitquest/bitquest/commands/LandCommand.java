package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.ChunkPermission;
import com.bitquest.bitquest.LandChunk;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LandCommand extends CommandAction {
  private BitQuest bitQuest;

  public LandCommand(BitQuest plugin) {
    this.bitQuest = plugin;
  }

  public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
    if (player.getLocation().getWorld().getName().equals("world_the_end")) return false;
    if (player.getLocation().getWorld().getName().equals("world_nether")) return false;
    if (args.length == 0) return false;
    Location location = player.getLocation();
    String subCommand = args[0];
    System.out.println(subCommand);
    int x = location.getChunk().getX();
    int z = location.getChunk().getZ();
    LandChunk chunk;
    try {
      chunk = bitQuest.land.chunk(x, z);
    } catch (Exception e) {
      player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
      e.printStackTrace();
      return true;
    }
    
    if (subCommand.equalsIgnoreCase("rename")) {
      if (chunk == null) {
        player.sendMessage(ChatColor.RED + "This land is not claimed yet.");
      }
      if (args.length == 2) {
        String name = args[1];
        if (chunk.isOwner(player)) {
          try {
            bitQuest.land.rename(x, z, name);
            player.sendMessage(ChatColor.GREEN + "Land renamed to " + name);
          } catch (Exception e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
          }
        } else {
          player.sendMessage(ChatColor.RED + "Only the owner of this land can rename it.");
        }
        return true;
      } else {
        return false;
      }
    } else if (subCommand.equalsIgnoreCase("transfer")) {
      if (args.length == 2) {
        if (chunk.isOwner(player)) {
          for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(args[1])) {
              if (!args[1].equalsIgnoreCase(player.getDisplayName())) {
                player.sendMessage("Changing the land ownership to " + onlinePlayer.getDisplayName() + "...");
                bitQuest.redis.set("chunk" + location.getChunk().getX() + "," + location.getChunk().getZ() + "owner",
                    player.getUniqueId().toString());
              }
            }
          }
          player.sendMessage(ChatColor.DARK_RED + "Cannot find player " + args[1]);
        } else {
          player.sendMessage(ChatColor.DARK_RED + "Only the owner of this land can transfer.");
        }
        return true;
      } else {
        return false;
      }
    } else if (subCommand.equalsIgnoreCase("info")) {
      return true;
    } else if (subCommand.equalsIgnoreCase("claim")) {
      // Claim land
      if (args.length > 1) {
        if (chunk == null) {
          StringBuilder sb = new StringBuilder(args[1]);
          // for (int i = 3; i < args.length; i++){
          // sb.append(" " + args[i]);
          // }
          String claimName = sb.toString().trim();

          if (!location.getWorld().getName().equals("world")) {
            player.sendMessage(ChatColor.DARK_RED + "You cannot claim land here.");
            return true;
          }
          try {
            bitQuest.land.claim(x, z, player.getUniqueId().toString(), claimName);
            player.sendMessage(ChatColor.GREEN + "Congratulations! You are now the owner of " + claimName);
            return true;
          } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + e.getMessage());
            return true;
          }
        } else {
          player.sendMessage(ChatColor.RED + "Land already claimed");
          return false;
        }
      } else {
        player.sendMessage(ChatColor.RED + "You must to specify a name for your land");
        return true;
      }

    } else if (args[0].equalsIgnoreCase("permission")) {
      if (chunk != null && chunk.isOwner(player)) {
        ChunkPermission permission = ChunkPermission.PRIVATE;
        if (args[1].equalsIgnoreCase("public")) permission = ChunkPermission.PUBLIC;
        if (args[1].equalsIgnoreCase("clan")) permission = ChunkPermission.CLAN;
        try {
          bitQuest.land.changePermission(x, z, permission);
          player.sendMessage(ChatColor.GREEN + "Permission of " + chunk.name + " is now " + permission);
        } catch (SQLException e) {
          player.sendMessage(ChatColor.RED + e.getMessage());
          e.printStackTrace();
        }
        return true;
      } else {
        player.sendMessage(ChatColor.DARK_RED + "Only the owner of this location can change its permissions.");
        return false;
      }
    }
    return false;
  }
}
