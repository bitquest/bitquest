package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.Wallet;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TransferCommand extends CommandAction {
  private BitQuest bitQuest;

  public TransferCommand(BitQuest plugin) {
    bitQuest = plugin;
  }

  public boolean run(CommandSender sender, Command cmd, String label, final String[] args, final Player player) {
    if (args.length == 2) {
      // Check that first argument is a number
      for (char c : args[0].toCharArray()) {
        if (!Character.isDigit(c)) {
          return false;
        }
      }
      final Double sendAmount = Double.parseDouble(args[0]);
      Wallet fromWallet = new Wallet(bitQuest.node, player.getUniqueId().toString());
      try {
        fromWallet.send(args[1], sendAmount);
        player.sendMessage(ChatColor.GREEN + "Succesfully sent " + ChatColor.LIGHT_PURPLE + args[0] + " "
            + BitQuest.DENOMINATION_NAME + ChatColor.GREEN + " to external address.");
        bitQuest.updateScoreboard(player);
        return true;
      } catch (Exception e) {
        e.printStackTrace();
        player.sendMessage(ChatColor.RED + e.getMessage());
        return false;
      }
    } else {
      return false;
    }
  }
}
