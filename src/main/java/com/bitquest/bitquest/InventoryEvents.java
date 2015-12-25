package com.bitquest.bitquest;

import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cristian on 11/27/15.
 */
public class InventoryEvents implements Listener {
    BitQuest bitQuest;
    ArrayList<Trade> trades;
    public static Inventory marketInventory;

    public InventoryEvents(BitQuest plugin) {
        bitQuest = plugin;
        trades=new ArrayList<Trade>();
        trades.add(new Trade(new ItemStack(Material.DIAMOND,1),2000));
        trades.add(new Trade(new ItemStack(Material.WOOL,16),2000));
        trades.add(new Trade(new ItemStack(Material.COOKED_BEEF,16),2000));
        trades.add(new Trade(new ItemStack(Material.BOOKSHELF,1),2000));
        trades.add(new Trade(new ItemStack(Material.PRISMARINE,64),2000));
        trades.add(new Trade(new ItemStack(Material.SEA_LANTERN,64),2000));
        trades.add(new Trade(new ItemStack(Material.QUARTZ_BLOCK,64),2000));
        trades.add(new Trade(new ItemStack(Material.GLASS,64),2000));
        trades.add(new Trade(new ItemStack(Material.SMOOTH_BRICK,64),2000));
        trades.add(new Trade(new ItemStack(Material.WOOD,64),2000));
        trades.add(new Trade(new ItemStack(Material.FENCE,64),2000));
        trades.add(new Trade(new ItemStack(Material.COMPASS,1),2000));
        trades.add(new Trade(new ItemStack(Material.EYE_OF_ENDER,1),2000));
        trades.add(new Trade(new ItemStack(Material.SANDSTONE,64),2000));
        trades.add(new Trade(new ItemStack(Material.RED_SANDSTONE,64),2000));

        marketInventory = Bukkit.getServer().createInventory(null,  45, "Market");
        for (int i = 0; i < trades.size(); i++) {
            ItemStack button = new ItemStack(trades.get(i).itemStack);
            ItemMeta meta = button.getItemMeta();
            ArrayList<String> lore = new ArrayList<String>();
            lore.add("Price: "+trades.get(i).price+"SAT");
            meta.setLore(lore);
            button.setItemMeta(meta);
            marketInventory.setItem(i, button);
        }
    }
    @EventHandler
    void onInventoryClick(InventoryClickEvent event) throws IOException, ParseException, org.json.simple.parser.ParseException {
        final Player player = (Player) event.getWhoClicked();
        final Inventory inventory = event.getInventory();
        // Merchant inventory
        if(inventory.equals(marketInventory)) {
        	if(event.getRawSlot() < event.getView().getTopInventory().getSize()) {
        		
        		ItemStack clicked = event.getCurrentItem();
                if(clicked.getType()!=Material.AIR) {
                    player.sendMessage(ChatColor.YELLOW+"Purchasing "+clicked.getType()+"...");

                    player.closeInventory();
                    event.setCancelled(true);
                    // TODO: try/catch
                    User user=new User(player);
                    if(user.wallet.transaction(2000,bitQuest.wallet)==true) {
                        ItemStack item = event.getCurrentItem();
                        ItemMeta meta = item.getItemMeta();
                        ArrayList<String> Lore = new ArrayList<String>();
                        meta.setLore(null);
                        item.setItemMeta(meta);
                        player.getInventory().addItem(item);
                        player.sendMessage(ChatColor.GREEN+""+clicked.getType()+" purchased");
                    } else {
                        player.sendMessage(ChatColor.RED+"transaction failed");
                    }
                }

        		
        	} else {
        		event.setCancelled(true);
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
    @EventHandler
    void onInteract(PlayerInteractEntityEvent event) {
        // VILLAGER
        if (event.getRightClicked().getType().equals(EntityType.VILLAGER)) {
            event.setCancelled(true);
            // compass

            // open menu
            event.getPlayer().openInventory(marketInventory);
        }

    }
}
