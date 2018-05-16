package com.bitquest.bitquest;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

/**
 * Created by cristian on 3/20/16.
 */
public class ServerEvents implements Listener {
    BitQuest bitQuest;

    public ServerEvents(BitQuest plugin) {

        bitQuest = plugin;

    }
    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {

        event.setMotd(ChatColor.GOLD + ChatColor.BOLD.toString() + "Bit" + ChatColor.GRAY + ChatColor.BOLD.toString() + "Quest" + ChatColor.RESET + " - The server that runs on Bitcoin ");
    }
}
