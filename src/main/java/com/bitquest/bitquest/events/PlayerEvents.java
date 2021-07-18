package com.bitquest.bitquest.events;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.GameMode;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class PlayerEvents implements Listener {
  BitQuest bitQuest;
  
  public PlayerEvents(BitQuest plugin) {
    bitQuest = plugin;
  }

  @EventHandler
  public void onServerListPing(PlayerPortalEvent event) {
    BitQuest.log("portal", event.getPlayer().getName());
    if (event.getTo().getWorld().getEnvironment() == Environment.NORMAL) {
      event.getPlayer().setGameMode(GameMode.SURVIVAL);
    }
  }
}
