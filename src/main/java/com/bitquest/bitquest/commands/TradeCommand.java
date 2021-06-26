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

  public boolean run(
      CommandSender sender, Command cmd, String label, final String[] args, final Player player) {
    Wallet wallet = new Wallet(bitQuest.node,player.getUniqueId().toString());
    try {
      wallet.send(this.bitQuest.wallet.address(),10.0);
      player.getInventory().addItem(new ItemStack(Material.EMERALD,10));
    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(ChatColor.RED + e.getMessage());
    }
    return true;
  }
  
}
