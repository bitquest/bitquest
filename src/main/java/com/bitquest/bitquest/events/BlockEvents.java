package com.bitquest.bitquest.events;

import com.bitquest.bitquest.BitQuest;
import java.util.List;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

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
          event
              .getPlayer()
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
  void onBlockBreak(BlockBreakEvent event) {
    // If block is bedrock, cancel the event
    Block b = event.getBlock();
    Material m = b.getType();
    if(event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase("world_the_end")) {
      if(bitQuest.isModerator(event.getPlayer())) {
        event.setCancelled(false);
      } else {
        event.setCancelled(true);
      }
    } if(event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase("world_nether")) {
      if(bitQuest.isModerator(event.getPlayer())) {
        event.setCancelled(false);
      } else {
        event.setCancelled(true);
      }
    } else if (m.equals(Material.BEDROCK)
        || m.equals(Material.COMMAND)
        || m.equals(Material.COMMAND_CHAIN)
        || m.equals(Material.COMMAND_REPEATING)) {
      event.setCancelled(true);
      // If player is in a no-build zone, cancel the event
    } else if (!bitQuest.canBuild(b.getLocation(), event.getPlayer())) {
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
    } else if (m.equals(Material.BEDROCK)
        || m.equals(Material.COMMAND)
        || m.equals(Material.COMMAND_CHAIN)
        || m.equals(Material.COMMAND_REPEATING)) {
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

	String tempchunk="";
	if (event.getBlock().getLocation().getWorld().getName().equals("world")){
        tempchunk="chunk";
	}//end world lmao @bitcoinjake09
	else if (event.getBlock().getLocation().getWorld().getName().equals("world_nether")){
	tempchunk="netherchunk";
	}//end nether @bitcoinjake09

    if (!blocks.isEmpty()) {
      Block lastBlock = blocks.get(blocks.size() - 1);
      Block nextBlock = lastBlock.getRelative(direction);

      Chunk pistonChunk = piston.getChunk();
      Chunk blockChunk = nextBlock.getChunk();

      String owner1, owner2;
      if ((owner2 =
              BitQuest.REDIS.get(tempchunk+"" + blockChunk.getX() + "," + blockChunk.getZ() + "owner"))
          != null) {
        if ((owner1 =
                BitQuest.REDIS.get(
                    tempchunk+"" + pistonChunk.getX() + "," + pistonChunk.getZ() + "owner"))
            != null) {
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
	String tempchunk="";
	if (event.getBlock().getLocation().getWorld().getName().equals("world")){
        tempchunk="chunk";
	}//end world lmao @bitcoinjake09
	else if (event.getBlock().getLocation().getWorld().getName().equals("world_nether")){
	tempchunk="netherchunk";
	}//end nether @bitcoinjake09

    if (event.isSticky()) {
      Chunk pistonChunk = piston.getChunk();
      Chunk blockChunk = nextBlock.getChunk();

      String owner1, owner2;
      if ((owner2 =
              BitQuest.REDIS.get(tempchunk+"" + blockChunk.getX() + "," + blockChunk.getZ() + "owner"))
          != null) {
        if ((owner1 =
                BitQuest.REDIS.get(
                    tempchunk+"" + pistonChunk.getX() + "," + pistonChunk.getZ() + "owner"))
            != null) {
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
