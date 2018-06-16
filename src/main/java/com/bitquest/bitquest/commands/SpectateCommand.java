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

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    if (args.length == 1) {
      Player p = (Player) sender;
      if (Bukkit.getPlayer(args[0]) != null) {
        p.setGameMode(GameMode.SPECTATOR);
        p.setSpectatorTarget(Bukkit.getPlayer(args[0]));
        p.sendMessage(
            ChatColor.GREEN
                + "You're now spectating "
                + ChatColor.BLUE
                + args[0]
                + ChatColor.GREEN
                + ".");
      } else {
        p.sendMessage(
            ChatColor.DARK_RED
                + "Player "
                + ChatColor.BLUE
                + args[0]
                + ChatColor.DARK_RED
                + " isn't online.");
      }
      return true;
    }
    return false;
  }
}
