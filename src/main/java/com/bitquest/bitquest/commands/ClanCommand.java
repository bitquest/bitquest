package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.BitQuestPlayer;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanCommand extends CommandAction {
  private BitQuest bitQuest;

  public ClanCommand(BitQuest plugin) {
    bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    if (args.length > 0) {
      String subCommand = args[0];
      try {
        BitQuestPlayer bqPlayer = bitQuest.players.player(player.getUniqueId().toString());
        if (subCommand.equals("new")) {
          if (args.length > 1) {
            String clanName = args[1];     
            if (bitQuest.players.createClan(player.getUniqueId().toString(), clanName)) {
              player.sendMessage(ChatColor.GREEN + "You have founded the clan " + clanName);
            } else {
              player.sendMessage(ChatColor.RED + "Cannot create clan.");
            }
          } else {
            player.sendMessage(ChatColor.RED + "Usage: /clan new <your desired name>");
            return true;
          }
        }
        if (subCommand.equals("invite")) {
          if (args.length > 1) {
            String invitedName = args[1];
            for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
              if (onlinePlayer.getName().equalsIgnoreCase(invitedName) && !invitedName.equalsIgnoreCase(player.getDisplayName())) {
                BitQuestPlayer invitedPlayer = bitQuest.players.player(onlinePlayer.getUniqueId().toString());
                if (bqPlayer.inviteToClan(invitedPlayer)) {
                  player.sendMessage(ChatColor.GREEN + "Player " + onlinePlayer.getDisplayName() + " was invited to " + bqPlayer.clan);
                  onlinePlayer.sendMessage(ChatColor.GREEN + "Player " + player.getDisplayName() + " has invited you to the clan " + bqPlayer.clan);
                  onlinePlayer.sendMessage(ChatColor.GREEN + "To accept, write /clan join " + bqPlayer.clan);
                  return true;
                } else {
                  player.sendMessage(ChatColor.RED + "Cannot Invite Player");
                  return true;
                }
              }
            }
            player.sendMessage(ChatColor.RED + "Can't find player " + invitedName);
            return true;
          } else {
            player.sendMessage(ChatColor.RED + "Usage: /clan invite <player nickname>");
            return true;
          }
        }
        if (subCommand.equals("join")) {
          // check that argument is not empty
          if (args.length > 1) {
            String clanName = args[1];
            try {
              if (bitQuest.players.joinClan(player.getUniqueId().toString(), clanName)) {
                player.sendMessage(ChatColor.GREEN + "You have joined the clan " + clanName);
              } else {
                player.sendMessage(ChatColor.RED + "You cannot join that clan.");
              }
            } catch (Exception e) {
              player.sendMessage(e.getMessage());
              e.printStackTrace();
              return true;
            }
          } else {
            player.sendMessage(ChatColor.RED + "Usage: /clan join <clan name>");
            return true;
          }
        }
        if (args[0].equals("kick")) {
          if (args.length > 1) {
            String toKick = args[1];
            // check if player is in the uuid database
            if (bitQuest.redis.exists("uuid:" + toKick)) {
              String uuid = bitQuest.redis.get("uuid:" + toKick);
              // check if player belongs to a clan
              if (bitQuest.redis.exists("clan:" + player.getUniqueId().toString())) {
                String clan = bitQuest.redis.get("clan:" + player.getUniqueId().toString());
                // check that kicker and player are in the same clan
                if (bitQuest.redis.get("clan:" + uuid).equals(clan)) {
                  bitQuest.redis.del("clan:" + uuid);
                  bitQuest.redis.srem("clan:" + clan + ":members", uuid);
                  removeEmptyClan(clan);
                  player.sendMessage(
                      ChatColor.GREEN
                          + "Player "
                          + toKick
                          + " was kicked from the "
                          + clan
                          + " clan.");
                  if (Bukkit.getPlayerExact(toKick) != null) {
                    Player invitedPlayer = Bukkit.getPlayerExact(toKick);
                    invitedPlayer.sendMessage(
                        ChatColor.RED + player.getName() + " kick you from the " + clan + " clan");
                    invitedPlayer.setPlayerListName(invitedPlayer.getName());
                  }
                  return true;
                } else {
                  player.sendMessage(
                      ChatColor.RED + "Player " + toKick + " is not a member of the clan " + clan);
                  return true;
                }
              } else {
                player.sendMessage(ChatColor.RED + "You don't belong to any clan.");
                return true;
              }

            } else {
              player.sendMessage(
                  ChatColor.RED + "Player " + toKick + " does not play on this server.");
              return true;
            }
          } else {
            player.sendMessage(ChatColor.RED + "Usage: /clan kick <player nickname>");
            return true;
          }
        }
        if (subCommand.equals("leave")) {
          if (bqPlayer.leaveClan()) {
            player.sendMessage(ChatColor.RED + "You have left " + bqPlayer.clan);
          } else {
            player.sendMessage(ChatColor.RED + "Error");
          }
        }
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        player.sendMessage(ChatColor.RED + e.getMessage());
        e.printStackTrace();
        return true;
      }
    } else {
      player.sendMessage(ChatColor.RED + "Usage: /clan <new|invite|kick|join|leave>");
      return false;
    }
    return false;
  }

  private void removeEmptyClan(String clan) {
    if (bitQuest.redis.scard("clan:" + clan + ":members") == 0) {
      bitQuest.redis.del("clan:" + clan + ":members");
      bitQuest.redis.srem("clans", clan);
      bitQuest.redis.del("invitations:" + clan);
    }
  }
}
