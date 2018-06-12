package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCommand extends CommandAction {
  private BitQuest bitQuest;

  public SpectateCommand(BitQuest plugin) {
    this.bitQuest = plugin;
  }

            if(Bukkit.getPlayer(args[0]) != null) {
                ((Player) sender).setGameMode(GameMode.SPECTATOR);
                ((Player) sender).setSpectatorTarget(Bukkit.getPlayer(args[0]));
                bitQuest.success(((Player) sender), "You're now spectating " + args[0] + ".");
            } else {
                bitQuest.error(((Player) sender), "Player " + args[0] + " isn't online.");
            }
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /spectate <player>");
            return true;
        }

    }
    return false;
  }
}
