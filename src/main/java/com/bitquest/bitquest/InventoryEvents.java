package com.bitquest.bitquest;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cristian on 11/27/15.
 */
public class InventoryEvents implements Listener {
    BitQuest bitQuest;

    public InventoryEvents(BitQuest plugin) {
        bitQuest = plugin;

    }
    @EventHandler
    void onInventoryClick(InventoryClickEvent event) throws IOException, ParseException, org.json.simple.parser.ParseException {
        final Player player = (Player) event.getWhoClicked();
        final Inventory inventory = event.getInventory();
        // Merchant inventory
        if(inventory.equals(EntityEvents.marketInventory)) {
            ItemStack clicked = event.getCurrentItem();
            player.closeInventory();
            event.setCancelled(true);
            // TODO: try/catch
            User user=new User(player);
            if(user.wallet.transaction(2000,bitQuest.wallet)==true) {
                player.getInventory().addItem(event.getCurrentItem());
            }


        }
        // compass inventory
        if (inventory.getName().equals("Compass") && !player.hasMetadata("teleporting")) {
            final User bp = new User(player);

            ItemStack clicked = event.getCurrentItem();
            // teleport to other part of the world
            boolean willTeleport = false;
            if (clicked.getItemMeta() != null && clicked.getItemMeta().getDisplayName() != null) {
                int x = 0;
                int z = 0;
                // TODO: Go to the actual destination selected on the inventory, not 0,0
                
                player.sendMessage(ChatColor.GREEN + "Teleporting to " + clicked.getItemMeta().getDisplayName() + "...");
                System.out.println("[teleport] " + player.getName() + " teleported to " + x + "," + z);
                player.closeInventory();

                player.setMetadata("teleporting", new FixedMetadataValue(bitQuest, true));
                Chunk c = new Location(bitQuest.getServer().getWorld("world"), x, 72, z).getChunk();
                if (!c.isLoaded()) {
                    c.load();
                }
                final int tx = x;
                final int tz = z;
                bitQuest.getServer().getScheduler().scheduleSyncDelayedTask(bitQuest, new Runnable() {

                    public void run() {
                        Location location = Bukkit
                                .getServer()
                                .getWorld("world")
                                .getHighestBlockAt(tx, tz).getLocation();
                        player.teleport(location);
                        player.removeMetadata("teleporting", bitQuest);
                    }
                }, 60L);

            }

            event.setCancelled(true);
        }
    }
}
