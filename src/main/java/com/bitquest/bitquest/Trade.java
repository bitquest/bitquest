package com.bitquest.bitquest;

import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

public class Trade {
    public int price;
    public ItemStack itemStack;
    public Trade(ItemStack itemStack, int price) {
        this.itemStack = itemStack;
        this.price = price;
    }
}
