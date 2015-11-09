package com.bitquest.bitquest;

import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
	
	BitQuest bitQuest;
	
	public BlockEvents(BitQuest plugin) {
		
		bitQuest = plugin;
		
	}
	
    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
		JsonObject area=bitQuest.areaForLocation(event.getBlock().getLocation());
		if(area!=null) {
            // TODO: check if user is owner of that plot, otherwise cancel the event
			bitQuest.log(area.toString());
		}
    }
	@EventHandler
	void onBlockPlace(BlockPlaceEvent e) {
        // If block is bed, attempt to claim area for player
        if (e.getBlock().getType() == Material.BED_BLOCK) {
            List<String> areas = bitQuest.REDIS.lrange("areas", 0, -1);

            for (String areaJSON : areas) {
                Gson gson = new Gson();
                JsonObject area = new JsonParser().parse(areaJSON).getAsJsonObject();
                // Check if player already owns a plot
                // In the future this might change as we allow players to get more than one plot.
                if (area.get("owner").getAsString().equals(e.getPlayer().getUniqueId().toString())) {
                    bitQuest.error(e.getPlayer(), "You already own a home plot");
                    e.setCancelled(true);
                    return;
                }
                // checks that new area is far enough from other areas
                if (BitQuest.distance(e.getBlock().getLocation(), new Location(area.get("x").getAsDouble(), 0, area.get("z").getAsDouble())) < area.get("size").getAsInt()) {
                    bitQuest.error(e.getPlayer(), "This area is too close from " + area.get("name").getAsString());
                }
            }
            if (bitQuest.createNewArea(e.getBlock().getLocation(), e.getPlayer(), e.getPlayer().getDisplayName() + "'s home", 32)) {
                bitQuest.success(e.getPlayer(), "Congratulations! this is your new home!");
            } else {
                bitQuest.error(e.getPlayer(), "Plot claim error");
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
