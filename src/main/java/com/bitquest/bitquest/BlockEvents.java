package com.bitquest.bitquest;

import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;

/**
 * Created by explodi on 11/1/15.
 * Edited by Xeyler on 11/2/15
 */
public class BlockEvents implements Listener {
	
	Plugin bitQuest;
	
	public BlockEvents(Plugin plugin) {
		
		bitQuest = plugin;
		
	}
	
    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
		JsonObject areas=new JsonObject();
		List<String> areasJSON=BitQuest.REDIS.lrange("areas",0,-1);
		// TODO: parse the areas object
    }
	@EventHandler
	void onBlockPlace(BlockPlaceEvent e) {

	}
    // Make a sweet block regeneration ^_^
    @EventHandler
	void onExplode(EntityExplodeEvent event) {
		
		for(Block block : event.blockList()) {
			
			if(block.getType() != Material.AIR) {
			
				final BlockState state = block.getState();
			
				int delay;
				
				if(block.getType().hasGravity()) {
					
					delay = 141;
					
				} else {
					
					delay = 80 + (int)(Math.random()*60);
					
				}
				
				// Set block to air so that no blocks drop
				block.setType(Material.AIR);
			
				// Regenerate all the blocks in a random order
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(bitQuest, new Runnable() {
				
					public void run() {
					
						state.update(true, false);
					
					}
				
				}, delay);
			
			}
			
		}
		
	}
    
}
