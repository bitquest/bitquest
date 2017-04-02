package com.bitquest.bitquest;

import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

/**
 * Created by cristian on 12/21/15.
 */
public class Trade {
    public int price;
    public ItemStack itemStack;
    public boolean has_stock;
    static int MAX_STOCK=10;
    public Trade(ItemStack itemStack,int price) {
        this.itemStack=itemStack;
        this.price=price;
        this.has_stock=false;
    }
    public Trade(ItemStack itemStack,int price,boolean has_stock) {
        this.itemStack=itemStack;
        this.price=price;
        this.has_stock=has_stock;
    }
    public int price_for_stock(Jedis REDIS) {
        int stock =Integer.valueOf(REDIS.get("stock:"+itemStack.getType()));
        if(stock>MAX_STOCK) {
            return 100;
        } else if(stock<1) {
            return MAX_STOCK*100;
        } else {
            return Math.max(100,(MAX_STOCK-stock)*100);
        }
    }
    public boolean will_buy(Jedis REDIS) {
        int stock =Integer.valueOf(REDIS.get("stock:"+itemStack.getType()));
        if(stock>MAX_STOCK) {
            return false;
        } else {
            return true;
        }
    }

}
