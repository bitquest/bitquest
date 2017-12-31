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
        // Villager Prices
        // By default, prices are in bits (not satoshi)
        bitQuest = plugin;
        trades=new ArrayList<Trade>();
        trades.add(new Trade(new ItemStack(Material.CLAY_BALL,16),5));
        trades.add(new Trade(new ItemStack(Material.COMPASS,1),50));
        trades.add(new Trade(new ItemStack(Material.COOKED_BEEF,16),5));
        trades.add(new Trade(new ItemStack(Material.EYE_OF_ENDER,1),20));
        trades.add(new Trade(new ItemStack(Material.FENCE,16),10));
        trades.add(new Trade(new ItemStack(Material.GLASS,32),15));
        trades.add(new Trade(new ItemStack(Material.HAY_BLOCK,16),1250));
        trades.add(new Trade(new ItemStack(Material.LEATHER,16),50));
        trades.add(new Trade(new ItemStack(Material.OBSIDIAN,8),25));
        trades.add(new Trade(new ItemStack(Material.RAILS,16),750));
        trades.add(new Trade(new ItemStack(Material.SANDSTONE,16),15));
        trades.add(new Trade(new ItemStack(Material.RED_SANDSTONE,16),25));
        trades.add(new Trade(new ItemStack(Material.SMOOTH_BRICK,16),25));
        trades.add(new Trade(new ItemStack(Material.BLAZE_ROD,2),50));
        trades.add(new Trade(new ItemStack(Material.CHORUS_FLOWER,2),150));
        trades.add(new Trade(new ItemStack(Material.DIAMOND,12),75));
        trades.add(new Trade(new ItemStack(Material.ENDER_STONE,4),50));
        trades.add(new Trade(new ItemStack(Material.IRON_BLOCK,8),50));
        trades.add(new Trade(new ItemStack(Material.IRON_INGOT,16),15));
        trades.add(new Trade(new ItemStack(Material.NETHERRACK,8),50));
        trades.add(new Trade(new ItemStack(Material.QUARTZ,16),1750));
        trades.add(new Trade(new ItemStack(Material.SOUL_SAND,8),50));
        trades.add(new Trade(new ItemStack(Material.SPONGE,2),50));
        trades.add(new Trade(new ItemStack(Material.LOG,16),5));
        trades.add(new Trade(new ItemStack(Material.WOOL,16),25));
        trades.add(new Trade(new ItemStack(Material.PAPER,16),1250));
        trades.add(new Trade(new ItemStack(Material.PACKED_ICE,16),50));
        trades.add(new Trade(new ItemStack(Material.GOLD_BLOCK,4),175));
        trades.add(new Trade(new ItemStack(Material.GOLDEN_APPLE,1),50));
        trades.add(new Trade(new ItemStack(Material.ARROW,16),5));
        trades.add(new Trade(new ItemStack(Material.PRISMARINE,16),75));
        trades.add(new Trade(new ItemStack(Material.QUARTZ_BLOCK,16),75));
        trades.add(new Trade(new ItemStack(Material.SEA_LANTERN,16),75));
        trades.add(new Trade(new ItemStack(Material.GLOWSTONE,16),75));
        trades.add(new Trade(new ItemStack(Material.ANVIL, 1),25));
        trades.add(new Trade(new ItemStack(Material.ENDER_PEARL, 1),10));
        trades.add(new Trade(new ItemStack(Material.EMERALD_BLOCK,10),90));
        trades.add(new Trade(new ItemStack(Material.NETHER_STALK,4),50));
        trades.add(new Trade(new ItemStack(Material.LAPIS_ORE,4),50));
        trades.add(new Trade(new ItemStack(Material.SADDLE,1),30));
        trades.add(new Trade(new ItemStack(Material.SLIME_BALL,4),25));
        trades.add(new Trade(new ItemStack(Material.GOLDEN_APPLE, 3, (short)1),300));
        trades.add(new Trade(new ItemStack(Material.APPLE,16),50));
        trades.add(new Trade(new ItemStack(Material.ELYTRA,1),1000));
        trades.add(new Trade(new ItemStack(Material.PURPLE_SHULKER_BOX,1),1000));
        trades.add(new Trade(new ItemStack(Material.BOOK_AND_QUILL,4),100));
        trades.add(new Trade(new ItemStack(Material.CAKE,4),25));
        trades.add(new Trade(new ItemStack(Material.DRAGONS_BREATH,2),100));
        trades.add(new Trade(new ItemStack(Material.EMPTY_MAP,3),200));
        trades.add(new Trade(new ItemStack(Material.PUMPKIN,3),1));



    }
    @EventHandler
    void onInventoryClick(final InventoryClickEvent event) throws IOException, ParseException, org.json.simple.parser.ParseException {
        final Player player = (Player) event.getWhoClicked();
        final Inventory inventory = event.getInventory();
        final User user=new User(bitQuest, player);
        user.setTotalExperience(user.experience());
        // Merchant inventory
        if(inventory.getName().equalsIgnoreCase("Market")) {
            if(event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                // player buys
                final ItemStack clicked = event.getCurrentItem();
                if(clicked!=null && clicked.getType()!=Material.AIR) {
                    System.out.println("[purchase] "+player.getName()+" <- "+clicked.getType());
                    player.sendMessage(ChatColor.YELLOW + "Purchasing " + clicked.getType() + "...");

                    player.closeInventory();
                    event.setCancelled(true);


                    try {
                        int sat = 0;
                        Trade trade=null;
                        for (int i = 0; i < trades.size(); i++) {
                            if (clicked.getType() == trades.get(i).itemStack.getType()) {
                                sat = trades.get(i).price;
                                trade=trades.get(i);


                            }

                        }

                        boolean hasOpenSlots = false;
                        for (ItemStack item : player.getInventory().getContents()) {
                            if (item == null || (item.getType() == clicked.getType() && item.getAmount() + clicked.getAmount() < item.getMaxStackSize())) {
                                hasOpenSlots = true;
                                break;
                            }
                        }
                        final boolean hasOpenSlotsFinal = hasOpenSlots;
                        final long satFinal = sat*BitQuest.DENOMINATION_FACTOR;
                        user.wallet.getBalance(0, new Wallet.GetBalanceCallback() {
                            @Override
                            public void run(Long balance) {
                                try {
                                    if (balance >= satFinal) {
                                        if (hasOpenSlotsFinal) {
                                            if (user.wallet.move("bitquest_market", satFinal)) {

                                                ItemStack item = event.getCurrentItem();
                                                ItemMeta meta = item.getItemMeta();
                                                ArrayList<String> Lore = new ArrayList<String>();
                                                meta.setLore(null);
                                                item.setItemMeta(meta);
                                                player.getInventory().addItem(item);
                                                player.sendMessage(ChatColor.GREEN + "You bought " + clicked.getType() + " for " + satFinal / 100);


                                                bitQuest.updateScoreboard(player);
                                                if (bitQuest.messageBuilder != null) {

                                                    // Create an event
                                                    org.json.JSONObject sentEvent = bitQuest.messageBuilder.event(player.getUniqueId().toString(), "Purchase", null);
                                                    org.json.JSONObject sentCharge = bitQuest.messageBuilder.trackCharge(player.getUniqueId().toString(), satFinal / 100, null);


                                                    ClientDelivery delivery = new ClientDelivery();
                                                    delivery.addMessage(sentEvent);
                                                    delivery.addMessage(sentCharge);


                                                    MixpanelAPI mixpanel = new MixpanelAPI();
                                                    mixpanel.deliver(delivery);
                                                }

                                            } else {
                                                player.sendMessage(ChatColor.RED + "Transaction failed. Please try again in a few moments (ERROR 1)");
                                            }
                                        } else {
                                            player.sendMessage(ChatColor.RED + "You don't have space in your inventory");
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.RED + "You don't have enough bits");

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "Transaction failed. Please try again in a few moments (ERROR 2)");
                    }
                }
            
            }

        } else if (inventory.getName().equals("Compass") && !player.hasMetadata("teleporting")) {
            final User bp = new User(bitQuest, player);

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
            Inventory marketInventory = Bukkit.getServer().createInventory(null,  54, "Market");
            for (int i = 0; i < trades.size(); i++) {
                int inventory_stock=bitQuest.MAX_STOCK;

                if(inventory_stock>0) {
                    ItemStack button = new ItemStack(trades.get(i).itemStack);
                    ItemMeta meta = button.getItemMeta();
                    ArrayList<String> lore = new ArrayList<String>();
                    int bits_price;
                    bits_price=trades.get(i).price;

                    lore.add("Price: "+bits_price);
                    meta.setLore(lore);
                    button.setItemMeta(meta);
                    marketInventory.setItem(i, button);
                }

            }
            event.getPlayer().openInventory(marketInventory);
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
