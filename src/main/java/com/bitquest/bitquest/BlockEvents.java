package com.bitquest.bitquest;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;

/**
 * Created by cristian on 11/1/15.
 * Edited by Xeyler on 11/2/15
 */
public class BlockEvents implements Listener {
	
	Plugin instance;
	
	public BlockEvents(Plugin plugin) {
		
		instance = plugin;
		
	}
	
    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
    	
        // strikes a lightning when player breaks block. Why? testing purposes. This annoying code will be obviously eliminated
        event.setCancelled(true);
        Player player = event.getPlayer();
        
        // Lightning at broken block
        player.getWorld().strikeLightning(event.getBlock().getLocation());
        // Explosion at broken block with a size of 4
		player.getWorld().createExplosion(event.getBlock().getLocation(), 4);

    }
    
    // Make a sweet block regeneration ^_^
    @EventHandler
	void onExplode(EntityExplodeEvent event) {
		
		for(Block block : event.blockList()) {
			
			if(block.getType() != Material.AIR) {
			
				final BlockState state = block.getState();
			
				int delay;
				
				if(block.getType().equals(Material.SAND) || block.getType().equals(Material.GRAVEL)) {
					
					delay = 141;
					
				} else {
					
					delay = 80 + (int)(Math.random()*140);
					
				}
				
				// Set block to air so that no blocks drop
				block.setType(Material.AIR);
			
				// Regenerate all the blocks in a random order
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
				
					public void run() {
					
						state.update(true, false);
					
					}
				
				}, delay);
			
			}
			
		}
		
	}
    
}
