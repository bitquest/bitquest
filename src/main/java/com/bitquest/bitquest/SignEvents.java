package com.bitquest.bitquest;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

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
        Player player = event.getPlayer();
        String name = event.getLine(0);
        if(!name.isEmpty()) {
            User user=new User(player);
            player.sendMessage(ChatColor.YELLOW+"Claiming land...");
            if(user.wallet.transaction(10000,bitQuest.wallet)==true) {
                Chunk chunk =player.getWorld().getChunkAt(player.getLocation());
                int x=chunk.getX();
                int z=chunk.getZ();
                bitQuest.REDIS.set("chunk"+x+","+z+"owner",player.getUniqueId().toString());
                bitQuest.REDIS.set("chunk"+x+","+z+"name",name);
                player.sendMessage(ChatColor.GREEN+"Land claimed! you are now owner of "+name);
            } else {
                player.sendMessage(ChatColor.RED+"claim payment failed");
            };
        }

    }
}
