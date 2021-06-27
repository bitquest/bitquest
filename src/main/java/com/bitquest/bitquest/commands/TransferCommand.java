package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.User;
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
        if (fromWallet != null) {

          player.sendMessage(
              ChatColor.YELLOW + "Sending " + ChatColor.LIGHT_PURPLE + args[0] + " " + BitQuest.DENOMINATION_NAME
                  + ChatColor.YELLOW + " to " + ChatColor.BLUE + args[1] + ChatColor.YELLOW + "...");

          if (fromWallet.send(args[1], sendAmount) == true) {
            player.sendMessage(ChatColor.GREEN + "Succesfully sent " + ChatColor.LIGHT_PURPLE + args[0] + " "
                + BitQuest.DENOMINATION_NAME + ChatColor.GREEN + " to external address.");
          } else {
            player.sendMessage(ChatColor.DARK_RED + "Transaction failed. Please try again later.");
          }
          bitQuest.updateScoreboard(player);
        }
      } catch (Exception e) {
        e.printStackTrace();
        player.sendMessage(ChatColor.RED + e.getMessage());
      }
      return true;
    } else {
      return false;
    }
  }
}
