package com.bitquest.bitquest.events;

import com.bitquest.bitquest.BitQuest;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
          event.getPlayer()
              .sendMessage(ChatColor.DARK_RED + "You don't have permission to do that!");
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
  void onTrample(PlayerInteractEvent event) {
    // This is related to trampling
    if (event.getAction().equals(Action.PHYSICAL)) {
      // Get the soil block
      Block soilBlock = event.getClickedBlock();
      // Check if the block is SOIL
      if (soilBlock.getType() == Material.LEGACY_SOIL) {
        // Check if moderator
        if (!bitQuest.isModerator(event.getPlayer())) {
          // If the player can't build there cancel it
          if (!bitQuest.canBuild(soilBlock.getLocation(), event.getPlayer())) {
            event.setCancelled(true);
          } else {
            event.setCancelled(false);
          }
        }
      }
    }
  }

  @EventHandler
  void onBlockBreak(BlockBreakEvent event) {
    Block b = event.getBlock();
    Material m = b.getType();
      // If block is bedrock, cancel the event
    if(m.equals(Material.BEDROCK)) {
      event.setCancelled(true);
      return;
    }
    Environment environment = event.getBlock().getLocation().getWorld().getEnvironment();
    if (environment == Environment.NETHER || environment == Environment.THE_END) {
      event.setCancelled(true);
      return;
    }
    if (!bitQuest.canBuild(b.getLocation(), event.getPlayer())) {
      event.setCancelled(true);
    } else {
      event.setCancelled(false);
    }
  }

  @EventHandler
  void onBlockPlace(BlockPlaceEvent event) {
    // set clan
    // first, we check if the player has permission to build
    Block b = event.getBlock();
    Material m = b.getType();
    if (!bitQuest.canBuild(b.getLocation(), event.getPlayer())) {
      event.setCancelled(true);
      event.getPlayer().sendMessage(ChatColor.DARK_RED + "You may not place blocks here!");
    } else if (m.equals(Material.BEDROCK) || m.equals(Material.LEGACY_COMMAND) ||
        m.equals(Material.LEGACY_COMMAND_CHAIN)
        || m.equals(Material.LEGACY_COMMAND_REPEATING)) {
      event.getPlayer().sendMessage(ChatColor.DARK_RED + "Placing that block is not allowed!");
      event.setCancelled(true);
    } else {
      event.setCancelled(false);
    }
  }

  @EventHandler
  void onPistonExtends(BlockPistonExtendEvent event) {
    Block piston = event.getBlock();
    List<Block> blocks = event.getBlocks();
    BlockFace direction = event.getDirection();

    String tempchunk = "";
    if (event.getBlock().getLocation().getWorld().getName().equals("world")) {
      tempchunk = "chunk";
    } else if (event.getBlock().getLocation().getWorld().getName().equals("world_nether")) {
      tempchunk = "netherchunk";
    }

    if (!blocks.isEmpty()) {
      Block lastBlock = blocks.get(blocks.size() - 1);
      Block nextBlock = lastBlock.getRelative(direction);

      Chunk pistonChunk = piston.getChunk();
      Chunk blockChunk = nextBlock.getChunk();

      String owner1;
      String owner2;
      if ((owner2 = bitQuest.redis
          .get(tempchunk + "" + blockChunk.getX() + "," + blockChunk.getZ() + "owner")) != null) {
        if ((owner1 = bitQuest.redis
            .get(tempchunk + "" + pistonChunk.getX() + "," + pistonChunk.getZ() + "owner")) !=
            null) {
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
    Block nextBlock = piston.getRelative(direction, -2); // Direction is
    // inverted?
    String tempchunk = "";
    if (event.getBlock().getLocation().getWorld().getName().equals("world")) {
      tempchunk = "chunk";
    } else if (event.getBlock().getLocation().getWorld().getName().equals("world_nether")) {
      tempchunk = "netherchunk";
    }
    
    if (event.isSticky()) {
      Chunk pistonChunk = piston.getChunk();
      Chunk blockChunk = nextBlock.getChunk();

      String owner1;
      String owner2;
      if ((owner2 = bitQuest.redis
          .get(tempchunk + "" + blockChunk.getX() + "," + blockChunk.getZ() + "owner")) != null) {
        if ((owner1 = bitQuest.redis
            .get(tempchunk + "" + pistonChunk.getX() + "," + pistonChunk.getZ() + "owner")) !=
            null) {
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
