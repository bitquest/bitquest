package com.bitquest.bitquest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Created by cristian on 11/1/15.
 */
public class BlockEvents implements Listener {
    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
        // strikes a lightning when player breaks block. Why? testing purposes. This annoying code will be obviously eliminated
        event.setCancelled(true);
        event.getPlayer().getWorld().strikeLightning(event.getPlayer().getLocation());

    }
}
