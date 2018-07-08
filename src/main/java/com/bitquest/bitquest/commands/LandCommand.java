package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LandCommand extends CommandAction {
  private BitQuest bitQuest;

  public LandCommand(BitQuest plugin) {
    this.bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
	String tempchunk="";
	if (player.getLocation().getWorld().getName().equals("world")){
        tempchunk="chunk";
	}//end world lmao @bitcoinjake09
	else if (player.getLocation().getWorld().getName().equals("world_nether")){
	tempchunk="netherchunk";
	}//end nether @bitcoinjake09
    if (args.length == 0) {
      player.sendMessage(
          ChatColor.RED
              + "If you are trying to buy a land, the command is: /land claim nameofyourland");
      return true;

    } else if (bitQuest.rate_limit == false) {
      bitQuest.rate_limit = true;
      if (args[0].equalsIgnoreCase("claim")) {
        if (args.length > 1) {
          StringBuilder sb = new StringBuilder(args[1]);
          //            for (int i = 3; i < args.length; i++){
          //                sb.append(" " + args[i]);
          //            }
          String claimName = sb.toString().trim();

          Location location = player.getLocation();
          if (!(location.getWorld().getName().equals("world"))&&!(location.getWorld().getName().equals("world_nether"))) {
            player.sendMessage(ChatColor.DARK_RED + "You cannot claim land here.");
            return true;
          }

          try {
            bitQuest.claimLand(claimName, location.getChunk(), player);
          } catch (ParseException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Land claim failed. Please try again later.");
            return true;
          } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Land claim failed. Please try again later.");
            return true;
          } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Land claim failed. Please try again later.");
            return true;
          }

        } else {
          player.sendMessage(ChatColor.RED + "You must to specify a name for your land");
          return true;
        }

      } else if (args[0].equalsIgnoreCase("permission")) {
        bitQuest.land_permission_cache = new HashMap();

        Location location = player.getLocation();
        int x = location.getChunk().getX();
        int z = location.getChunk().getZ();

        if (bitQuest.landIsClaimed(location) && bitQuest.isOwner(location, player)) {
          String landname = BitQuest.REDIS.get(tempchunk+ "" + x + "," + z + "name");

          if (args[1].equalsIgnoreCase("public")) {
            BitQuest.REDIS.set(
                tempchunk+ "" + location.getChunk().getX()
                    + ","
                    + location.getChunk().getZ()
                    + "permissions",
                "p");
            player.sendMessage(
                ChatColor.GREEN
                    + "The land "
                    + ChatColor.DARK_GREEN
                    + landname
                    + ChatColor.GREEN
                    + " is now public");
            return true;
          } else if (args[1].equalsIgnoreCase("clan")) {
            BitQuest.REDIS.set(
                tempchunk+ "" + location.getChunk().getX()
                    + ","
                    + location.getChunk().getZ()
                    + "permissions",
                "c");
            player.sendMessage(
                ChatColor.GREEN
                    + "The land "
                    + ChatColor.DARK_GREEN
                    + landname
                    + ChatColor.GREEN
                    + " is now clan-owned");
            return true;
          } else if (args[1].equalsIgnoreCase("private")) {
            BitQuest.REDIS.del(
                tempchunk+ "" + location.getChunk().getX()
                    + ","
                    + location.getChunk().getZ()
                    + "permissions");
            player.sendMessage(
                ChatColor.GREEN
                    + "The land "
                    + ChatColor.DARK_GREEN
                    + landname
                    + ChatColor.GREEN
                    + " is now private");
            return true;
          } else if ((args[1].equalsIgnoreCase("pvp"))&&(args.length==2))  {
            BitQuest.REDIS.set(
                tempchunk+ "" + location.getChunk().getX()
                    + ","
                    + location.getChunk().getZ()
                    + "permissions",
                "v");
            player.sendMessage(
                ChatColor.GREEN
                    + "The land "
                    + ChatColor.DARK_GREEN
                    + landname
                    + ChatColor.GREEN
                    + " is now private PvP");
            return true;
          } else if ((args[1].equalsIgnoreCase("pvp"))&& (args[2].equalsIgnoreCase("public"))) {
            BitQuest.REDIS.set(
                tempchunk+ "" + location.getChunk().getX()
                    + ","
                    + location.getChunk().getZ()
                    + "permissions",
                "pv");
            player.sendMessage(
                ChatColor.GREEN
                    + "The land "
                    + ChatColor.DARK_GREEN
                    + landname
                    + ChatColor.GREEN
                    + " is now public PvP");
            return true;
          } else {
            player.sendMessage(
                ChatColor.DARK_RED + "Only the owner of this location can change its permissions.");
            return true;
          }

        } else {
          player.sendMessage(
              ChatColor.RED
                  + "Connectivity to Blockchain is limited. Please try again in 5 seconds.");

          return true;
        }
      } else {
        player.sendMessage(
            ChatColor.RED
                + "If you want buy claim a land, use /land claim landname. For permissions, use /land permission [public,private,clan]");
      }
    }
    return false;
  }
}
