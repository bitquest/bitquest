package com.bitquest.bitquest;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * Created by explodi on 11/1/15.
 * Edited by Xeyler on 11/2/15
 */
public class BlockEvents implements Listener {
	
	BitQuest bitQuest;
	
	public BlockEvents(BitQuest plugin) {
		
		bitQuest = plugin;
		
	}
	
    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
    	if (bitQuest.allowBuild(event.getBlock().getLocation(), event.getPlayer()) == false) {
    		// TODO: Perhaps add the area's name?
    		bitQuest.error(event.getPlayer(), "You may not break blocks here!");
            event.setCancelled(true);
        }
    }
	@EventHandler
	void onBlockPlace(BlockPlaceEvent event) {
        // If block is bed, attempt to claim area for player
        if (event.getBlock().getType() == Material.BED_BLOCK) {
            List<String> areas = bitQuest.REDIS.lrange("areas", 0, -1);

            for (String areaJSON : areas) {
                Gson gson = new Gson();
                JsonObject area = new JsonParser().parse(areaJSON).getAsJsonObject();
                // Check if player already owns a plot
                // In the future this might change as we allow players to get more than one plot.
                if (area.get("owner").getAsString().equals(event.getPlayer().getUniqueId().toString())) {
                    bitQuest.error(event.getPlayer(), "You already own a home plot");
                    event.setCancelled(true);
                    return;
                }
                // checks that new area is far enough from other areas
                if (BitQuest.distance(event.getBlock().getLocation(), new Location(event.getPlayer().getWorld(),area.get("x").getAsDouble(), 0, area.get("z").getAsDouble())) < area.get("size").getAsInt()) {
                    bitQuest.error(event.getPlayer(), "This area is too close to " + area.get("name").getAsString());
                }
            }
            if (bitQuest.createNewArea(event.getBlock().getLocation(), event.getPlayer(), event.getPlayer().getDisplayName() + "'s home", 32)) {
                bitQuest.success(event.getPlayer(), "Congratulations! this is your new home!");
            } else {
                bitQuest.error(event.getPlayer(), "Plot claim error");
            }
        } else {

        	if (bitQuest.allowBuild(event.getBlock().getLocation(), event.getPlayer()) == false) {
        		// TODO: Perhaps add the area's name?
        		bitQuest.error(event.getPlayer(), "You may not place blocks here!");
                event.setCancelled(true);
            }
    		
        }
        
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
					
					delay = bitQuest.rand(80, 140);
					
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
