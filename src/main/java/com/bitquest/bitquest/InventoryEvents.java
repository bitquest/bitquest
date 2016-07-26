package com.bitquest.bitquest;

import com.google.gson.JsonObject;
import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitScheduler;
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

    public InventoryEvents(BitQuest plugin) {
        bitQuest = plugin;
        trades=new ArrayList<Trade>();
        trades.add(new Trade(new ItemStack(Material.BED,1),20000));
        trades.add(new Trade(new ItemStack(Material.CLAY_BALL,64),20000));
        trades.add(new Trade(new ItemStack(Material.COMPASS,1),20000));
        trades.add(new Trade(new ItemStack(Material.COOKED_BEEF,64),20000));
        trades.add(new Trade(new ItemStack(Material.BOAT,1),20000));
        trades.add(new Trade(new ItemStack(Material.EYE_OF_ENDER,1),20000));
        trades.add(new Trade(new ItemStack(Material.FENCE,64),20000));
        trades.add(new Trade(new ItemStack(Material.GLASS,64),20000));
        trades.add(new Trade(new ItemStack(Material.HAY_BLOCK,32),20000));
        trades.add(new Trade(new ItemStack(Material.LEATHER,64),20000));
        trades.add(new Trade(new ItemStack(Material.OBSIDIAN,32),20000));
   //     trades.add(new Trade(new ItemStack(Material.GOLDEN_RAIL,64),10000)); //cheap price for a faster, AFK railroad development ;)
        trades.add(new Trade(new ItemStack(Material.RAILS,64),20000)); //we still need these to slow down, you know.
        trades.add(new Trade(new ItemStack(Material.SANDSTONE,64),20000));
        trades.add(new Trade(new ItemStack(Material.RED_SANDSTONE,64),20000));
        trades.add(new Trade(new ItemStack(Material.SMOOTH_BRICK,64),20000));
        trades.add(new Trade(new ItemStack(Material.BOW,2),20000));
        trades.add(new Trade(new ItemStack(Material.BLAZE_POWDER,16),20000));
        trades.add(new Trade(new ItemStack(Material.REDSTONE,16),20000));
        trades.add(new Trade(new ItemStack(Material.CHORUS_FLOWER,8),20000));
        trades.add(new Trade(new ItemStack(Material.DIAMOND,8),20000));
        trades.add(new Trade(new ItemStack(Material.ENDER_STONE,16),20000));
        trades.add(new Trade(new ItemStack(Material.IRON_INGOT,64),20000));
        trades.add(new Trade(new ItemStack(Material.NETHERRACK,16),20000));
        trades.add(new Trade(new ItemStack(Material.QUARTZ,64),20000));
        trades.add(new Trade(new ItemStack(Material.SOUL_SAND,16),20000));
        trades.add(new Trade(new ItemStack(Material.SPONGE,8),20000));
        trades.add(new Trade(new ItemStack(Material.WOOD,64),20000));
        trades.add(new Trade(new ItemStack(Material.WOOL,64),20000));
        trades.add(new Trade(new ItemStack(Material.BLAZE_ROD,16),30000));
        trades.add(new Trade(new ItemStack(Material.GOLD_INGOT,64),30000));
        trades.add(new Trade(new ItemStack(Material.GOLDEN_APPLE,6),30000));
        trades.add(new Trade(new ItemStack(Material.ARROW,64),30000));
        trades.add(new Trade(new ItemStack(Material.PRISMARINE,64),30000));
        trades.add(new Trade(new ItemStack(Material.QUARTZ_BLOCK,64),30000));
        trades.add(new Trade(new ItemStack(Material.SEA_LANTERN,64),30000));
        trades.add(new Trade(new ItemStack(Material.GLOWSTONE,64),30000));
        trades.add(new Trade(new ItemStack(Material.EMERALD_BLOCK,32),35000));
        trades.add(new Trade(new ItemStack(Material.LAPIS_ORE,16),40000));
        trades.add(new Trade(new ItemStack(Material.SADDLE,1),50000)); //If we have a lot of horses in Satoshi, we want to ride them!
       // trades.add(new Trade(new ItemStack(Material.DIAMOND_HORSE_ARMOUR,1),55000)); //essential
        trades.add(new Trade(new ItemStack(Material.SHIELD,1),60000)); //epic
        trades.add(new Trade(new ItemStack(Material.ELYTRA,1),100000));


    }
    @EventHandler
    void onInventoryClick(final InventoryClickEvent event) throws IOException, ParseException, org.json.simple.parser.ParseException {
        final Player player = (Player) event.getWhoClicked();
        final Inventory inventory = event.getInventory();
        final User user=new User(player);
        user.setTotalExperience(user.experience());
        // Merchant inventory
        if(inventory.getName().equalsIgnoreCase("Market")) {
        	if(event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                final ItemStack clicked = event.getCurrentItem();
                if(clicked!=null && clicked.getType()!=Material.AIR) {
                    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

                    player.sendMessage(ChatColor.YELLOW + "Purchasing " + clicked.getType() + "...");

                    player.closeInventory();
                    event.setCancelled(true);
                    BitQuest.REDIS.expire("balance"+player.getUniqueId().toString(),5);

                    scheduler.runTaskAsynchronously(bitQuest, new Runnable() {
                        @Override
                        public void run() {

                                try {
                                    int sat = 0;
                                    for (int i = 0; i < trades.size(); i++) {
                                        if (clicked.getType() == trades.get(i).itemStack.getType())
                                            sat = trades.get(i).price;

                                    }
                                    if (sat > 10000 && user.wallet.transaction(sat, bitQuest.wallet)) {
                                        ItemStack item = event.getCurrentItem();
                                        ItemMeta meta = item.getItemMeta();
                                        ArrayList<String> Lore = new ArrayList<String>();
                                        meta.setLore(null);
                                        item.setItemMeta(meta);
                                        player.getInventory().addItem(item);
                                        player.sendMessage(ChatColor.GREEN + "" + clicked.getType() + " purchased");
                                        if (bitQuest.messageBuilder != null) {

                                            // Create an event
                                            org.json.JSONObject sentEvent = bitQuest.messageBuilder.event(player.getUniqueId().toString(), "Purchase", null);


                                            ClientDelivery delivery = new ClientDelivery();
                                            delivery.addMessage(sentEvent);

                                            MixpanelAPI mixpanel = new MixpanelAPI();
                                            mixpanel.deliver(delivery);
                                        }
                                    } else {
                                        user.wallet.updateBalance();
                                        if (user.wallet.balance != user.wallet.confirmedBalance) {
                                            player.sendMessage(ChatColor.RED + "Transaction failed (You have unconfirmed transactions. Please wait ~10 minutes and try again)");
                                        } else if(user.wallet.balance()<sat) {
                                            player.sendMessage(ChatColor.RED + "Transaction failed (Insufficient balance)");
                                        } else {
                                            player.sendMessage(ChatColor.RED + "Transaction failed. Please try again in a few moments");
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                        }
                    });
                }
        		
        	} else {
        		event.setCancelled(true);
        	}

        } else if (inventory.getName().equals("Compass") && !player.hasMetadata("teleporting")) {
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
        } else {
            event.setCancelled(false);
        }
    }
    @EventHandler
    void onInteract(PlayerInteractEntityEvent event) {
        // VILLAGER
        if (event.getRightClicked().getType().equals(EntityType.VILLAGER)) {
            event.setCancelled(true);
            // compass

            // open menu
            Inventory marketInventory = Bukkit.getServer().createInventory(null,  45, "Market");
            for (int i = 0; i < trades.size(); i++) {
                ItemStack button = new ItemStack(trades.get(i).itemStack);
                ItemMeta meta = button.getItemMeta();
                ArrayList<String> lore = new ArrayList<String>();
                lore.add("Price: "+trades.get(i).price/100);
                meta.setLore(lore);
                button.setItemMeta(meta);
                marketInventory.setItem(i, button);
            }
            event.getPlayer().openInventory(marketInventory);
        } else {
            event.setCancelled(false);
        }

    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event)
    {
        event.setCancelled(false);
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryInteract(InventoryInteractEvent event) {
        event.setCancelled(false);
    }
}
