package com.bitquest.bitquest;

import org.bukkit.entity.Player;

public class LandChunk {
  public String owner;
  public ChunkPermission permission;
  public String name;
  
  public boolean isOwner(Player player) {
    return player.getUniqueId().toString().equals(this.owner);
  }
}
