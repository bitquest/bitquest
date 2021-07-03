package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.Wallet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SendCommand extends CommandAction {
  private BitQuest bitQuest;

  public SendCommand(BitQuest plugin) {
    bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, final Player player) {
    if (args.length == 2) {
      // args[1] == amount to send
      for (char c : args[1].toCharArray()) {
        if (!Character.isDigit(c)) { 
          return false; 
        }
      }
      final Double amount = Double.parseDouble(args[1]);
      final Double minAmount = 1.0; // Minimum amount that can be sent
      final Double maxAmount = 1000.0; // Minimum amount that can be sent
      if (amount > maxAmount) {
        return false;
      }
      if (amount < minAmount) {
        return false;
      }
      for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        if (onlinePlayer.getName().equalsIgnoreCase(args[0]) && !args[0].equalsIgnoreCase(player.getDisplayName())) {
          final Wallet senderWallet = new Wallet(bitQuest.node, player.getUniqueId().toString());
          final Wallet receiverWallet = new Wallet(bitQuest.node, onlinePlayer.getUniqueId().toString());
          try {
            senderWallet.send(receiverWallet.address(), amount);
            bitQuest.updateScoreboard(onlinePlayer);
            bitQuest.updateScoreboard(player);
            player.sendMessage(
                ChatColor.GREEN
                    + "You sent "
                    + ChatColor.LIGHT_PURPLE
                    + amount
                    + " "
                    + BitQuest.DENOMINATION_NAME
                    + ChatColor.GREEN
                    + " to user "
                    + ChatColor.BLUE
                    + onlinePlayer.getName());
            onlinePlayer.sendMessage(
                  ChatColor.GREEN
                      + "You got "
                      + ChatColor.LIGHT_PURPLE
                      + amount
                      + " "
                      + BitQuest.DENOMINATION_NAME
                      + ChatColor.GREEN
                      + " from user "
                      + ChatColor.BLUE
                      + player.getName());
            return true;
          } catch (Exception e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
            System.out.println(e);
            return true;
          }
        }
      }
    } 
    return false;
  }
}
