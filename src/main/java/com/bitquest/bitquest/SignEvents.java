package com.bitquest.bitquest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by cristian on 12/17/15.
 */
public class SignEvents implements Listener {
    BitQuest bitQuest;
    public SignEvents(BitQuest plugin) {
        bitQuest = plugin;
    }
    @EventHandler
    public void onSignChange(SignChangeEvent event) throws ParseException, org.json.simple.parser.ParseException, IOException {
        final Player player = event.getPlayer();
        final String name = event.getLine(0);
        Chunk chunk =player.getWorld().getChunkAt(player.getLocation());
        final int x=chunk.getX();
        final int z=chunk.getZ();
        if(!name.isEmpty() && bitQuest.REDIS.get("chunk"+x+","+z+"owner")==null) {
            final User user=new User(player);
            player.sendMessage(ChatColor.YELLOW+"Claiming land...");
            BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(bitQuest, new Runnable() {
                @Override
                public void run() {
                    // A villager is born
                    try {
                        Wallet paymentWallet;
                        if(bitQuest.LAND_BITCOIN_ADDRESS!=null) {
                            paymentWallet=new Wallet(bitQuest.LAND_BITCOIN_ADDRESS);
                        } else {
                            paymentWallet=bitQuest.wallet;
                        }
                        if(user.wallet.transaction(bitQuest.LAND_PRICE, paymentWallet)) {

                            bitQuest.REDIS.set("chunk"+x+","+z+"owner",player.getUniqueId().toString());
                            bitQuest.REDIS.set("chunk"+x+","+z+"name",name);
                            player.sendMessage(ChatColor.GREEN+"Congratulations! You're now the owner of " + name + "!");
                            new User(player).updateScoreboard();
                        } else {
                            int balance=new User(player).wallet.balance();
                            if(balance<bitQuest.LAND_PRICE) {
                                player.sendMessage(ChatColor.RED+"You don't have enough money! You need "+(bitQuest.LAND_PRICE-balance)/100+" more Bits.");
                            } else {
                                player.sendMessage(ChatColor.RED+"Claim payment failed. Please try again later.");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (org.json.simple.parser.ParseException e) {
                        e.printStackTrace();
                    }
                    ;
                }
            }, 1L);

        } else {
            if(name.isEmpty()==true) {
                player.sendMessage(ChatColor.RED+"your new land needs a name");
            }
        }

    }
}
