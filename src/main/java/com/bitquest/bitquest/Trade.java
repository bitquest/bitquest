package com.bitquest.bitquest;

import org.bukkit.inventory.ItemStack;

public class Trade {
  public int price;
  public ItemStack itemStack;

  public Trade(ItemStack itemStack, int price) {
    this.itemStack = itemStack;
    this.price = price;
  }
}
