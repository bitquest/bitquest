package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.Wallet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TradeCommand extends CommandAction {
  private BitQuest bitQuest;

  public TradeCommand(BitQuest plugin) {
    bitQuest = plugin;
  }

  public boolean run(CommandSender sender, Command cmd, String label, final String[] args, final Player player) {
    if (player.getInventory().firstEmpty() == -1) {
      player.sendMessage(ChatColor.RED + "Inventory is full");
      return false;
    }
    Wallet wallet = new Wallet(bitQuest.node, player.getUniqueId().toString());
    // Check that first argument is a number
    for (char c : args[0].toCharArray()) {
      if (!Character.isDigit(c)) {
        return false;
      }
    }
    final Double tradeAmount = Double.parseDouble(args[0]);
    if (tradeAmount < 1) {
      return false;
    }
    try {
      wallet.send(this.bitQuest.wallet.address(), tradeAmount);
      player.getInventory().addItem(new ItemStack(Material.EMERALD, tradeAmount.intValue()));
      player.sendMessage(ChatColor.GREEN + "You traded " + tradeAmount.toString() + " " + BitQuest.DENOMINATION_NAME
          + " for emeralds.");
    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(ChatColor.RED + e.getMessage());
    }
    return true;
  }

}
