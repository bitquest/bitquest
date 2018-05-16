package com.bitquest.bitquest;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
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
    void onBlockCatchFire(BlockIgniteEvent event) {
        if (event.getCause().equals(IgniteCause.FLINT_AND_STEEL)) {
            if (event.getPlayer() != null) {
                if (!bitQuest.canBuild(event.getBlock().getLocation(), event.getPlayer())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to do that!");
                }
            }
        } else if (event.getCause().equals(IgniteCause.SPREAD)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
        // If block is bedrock, cancel the event
        if (event.getBlock().getType().equals(Material.BEDROCK) || event.getBlock().getType().equals(Material.END_BRICKS) || event.getBlock().getType().equals(Material.ENDER_STONE)) {
            event.setCancelled(true);
            // If player is in a no-build zone, cancel the event
        } else if (!bitQuest.canBuild(event.getBlock().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED +  "You may not break blocks here!");
        } else {
            event.setCancelled(false);
        }
    }
    @EventHandler
    void onBlockPlace(BlockPlaceEvent event) {

        // set clan
        // first, we check if the player has permission to build
        if (!bitQuest.canBuild(event.getBlock().getLocation(), event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You may not place blocks here!");
        } else if (event.getBlock().getType().equals(Material.BEDROCK)) {
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "Placing bedrock is not allowed!");
            event.setCancelled(true);
        } else {
            event.setCancelled(false);
        }


    }

    @EventHandler
    void onPistonExtends(BlockPistonExtendEvent event) {
        Block piston = event.getBlock();
        List < Block > blocks = event.getBlocks();
        BlockFace direction = event.getDirection();

        if (!blocks.isEmpty()) {
            Block lastBlock = blocks.get(blocks.size() - 1);
            Block nextBlock = lastBlock.getRelative(direction);

            Chunk pistonChunk = piston.getChunk();
            Chunk blockChunk = nextBlock.getChunk();

            String owner1, owner2;
            if ((owner2 = BitQuest.REDIS.get("chunk" + blockChunk.getX() + "," + blockChunk.getZ() + "owner")) != null) {
                if ((owner1 = BitQuest.REDIS.get("chunk" + pistonChunk.getX() + "," + pistonChunk.getZ() + "owner")) != null) {
                    if (!owner1.equals(owner2)) {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    void onPistonRetract(BlockPistonRetractEvent event) {
        Block piston = event.getBlock();
        BlockFace direction = event.getDirection();
        Block nextBlock = piston.getRelative(direction, -2); // Direction is inverted?

        if (event.isSticky()) {
            Chunk pistonChunk = piston.getChunk();
            Chunk blockChunk = nextBlock.getChunk();

            String owner1, owner2;
            if ((owner2 = BitQuest.REDIS.get("chunk" + blockChunk.getX() + "," + blockChunk.getZ() + "owner")) != null) {
                if ((owner1 = BitQuest.REDIS.get("chunk" + pistonChunk.getX() + "," + pistonChunk.getZ() + "owner")) != null) {
                    if (!owner1.equals(owner2)) {
                        event.setCancelled(true);
                        piston.getRelative(event.getDirection()).setType(Material.AIR);
                    }
                } else {
                    event.setCancelled(true);
                    piston.getRelative(event.getDirection()).setType(Material.AIR);
                }
            }
        }
    }

}
