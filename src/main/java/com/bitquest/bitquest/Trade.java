package com.bitquest.bitquest;

import org.bukkit.inventory.ItemStack;

/** Created by cristian on 12/21/15. */
public class Trade {
  public int price;
  public ItemStack itemStack;

  public Trade(ItemStack itemStack, int price) {
    this.itemStack = itemStack;
    this.price = price;
  }
}
