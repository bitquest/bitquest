package com.bitquest.bitquest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.io.IOException;

/**
 * Created by cristian on 11/7/15.
 */
public class EntityEvents implements Listener {
    Plugin bitQuest;

    public EntityEvents(Plugin plugin) {
        bitQuest = plugin;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws IOException {

    }
}
