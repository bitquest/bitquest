package com.bitquest.bitquest.events;

import com.bitquest.bitquest.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

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
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.json.simple.parser.ParseException;


public class InventoryEvents implements Listener {
    BitQuest bitQuest;
    ArrayList<Trade> trades;

    public InventoryEvents(BitQuest plugin) {
        // Villager Prices
        // By default, prices are in bits (not satoshi)
        bitQuest = plugin;
        trades = new ArrayList<Trade>();
        trades.add(new Trade(new ItemStack(Material.CLAY_BALL, 32), 2));
        trades.add(new Trade(new ItemStack(Material.COOKED_BEEF, 64), 2));
        trades.add(new Trade(new ItemStack(Material.FENCE, 64), 4));
        trades.add(new Trade(new ItemStack(Material.GLASS, 64), 4));
        trades.add(new Trade(new ItemStack(Material.HAY_BLOCK, 8), 4));
        trades.add(new Trade(new ItemStack(Material.LEATHER, 8), 1));
        trades.add(new Trade(new ItemStack(Material.OBSIDIAN, 8), 1));
        trades.add(new Trade(new ItemStack(Material.RAILS, 8), 1));
        trades.add(new Trade(new ItemStack(Material.POWERED_RAIL, 4), 1));
        trades.add(new Trade(new ItemStack(Material.SAND, 64), 1));
        trades.add(new Trade(new ItemStack(Material.RED_SANDSTONE, 8), 1));
        trades.add(new Trade(new ItemStack(Material.SMOOTH_BRICK, 16), 1));
        trades.add(new Trade(new ItemStack(Material.BLAZE_ROD, 1), 2));
        trades.add(new Trade(new ItemStack(Material.CHORUS_FLOWER, 1), 5));
        trades.add(new Trade(new ItemStack(Material.DIAMOND, 64), 32));
        trades.add(new Trade(new ItemStack(Material.ENDER_STONE, 1), 4));
        trades.add(new Trade(new ItemStack(Material.IRON_INGOT, 8), 1));
        trades.add(new Trade(new ItemStack(Material.NETHERRACK, 1), 2));
        trades.add(new Trade(new ItemStack(Material.QUARTZ, 8), 1));
        trades.add(new Trade(new ItemStack(Material.SOUL_SAND, 64), 40));
        trades.add(new Trade(new ItemStack(Material.SPONGE, 16), 30));
        trades.add(new Trade(new ItemStack(Material.LOG, 64), 1));
        trades.add(new Trade(new ItemStack(Material.WOOL, 64), 5));
        trades.add(new Trade(new ItemStack(Material.PAPER, 1), 5));
        trades.add(new Trade(new ItemStack(Material.PACKED_ICE, 1), 2));
        trades.add(new Trade(new ItemStack(Material.GOLD_BLOCK, 1), 2));
        trades.add(new Trade(new ItemStack(Material.ARROW, 32), 1));
        trades.add(new Trade(new ItemStack(Material.PRISMARINE, 2), 1));
        trades.add(new Trade(new ItemStack(Material.QUARTZ_BLOCK, 2), 1));
        trades.add(new Trade(new ItemStack(Material.SEA_LANTERN, 2), 1));
        trades.add(new Trade(new ItemStack(Material.GLOWSTONE, 2), 1));
        trades.add(new Trade(new ItemStack(Material.ANVIL, 1), 2));
        trades.add(new Trade(new ItemStack(Material.ENDER_PORTAL_FRAME, 12), 1));

        //trades.add(new Trade(new ItemStack(Material.EMERALD_BLOCK, 1), 1));
        trades.add(new Trade(new ItemStack(Material.NETHER_STALK, 2), 1));
        trades.add(new Trade(new ItemStack(Material.LAPIS_ORE, 2), 1));
        trades.add(new Trade(new ItemStack(Material.SADDLE, 1), 2));
        trades.add(new Trade(new ItemStack(Material.SLIME_BALL, 2), 1));
        trades.add(new Trade(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1), 20));
        trades.add(new Trade(new ItemStack(Material.APPLE, 64), 10));
        trades.add(new Trade(new ItemStack(Material.ELYTRA, 1), 200));
        trades.add(new Trade(new ItemStack(Material.PURPLE_SHULKER_BOX, 1), 500));
        trades.add(new Trade(new ItemStack(Material.BOOK_AND_QUILL, 2), 5));
        trades.add(new Trade(new ItemStack(Material.CAKE, 2), 1));
        trades.add(new Trade(new ItemStack(Material.DRAGONS_BREATH, 1), 5));
        trades.add(new Trade(new ItemStack(Material.EMPTY_MAP, 1), 3));
        trades.add(new Trade(new ItemStack(Material.PUMPKIN, 6), 1));
    }

    @EventHandler
    void onInventoryClick(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory inventory = event.getInventory();


        // Merchant inventory
        if (inventory.getName().equalsIgnoreCase("Market")) {
            if(bitQuest.REDIS.exists("rate_limit:"+player.getUniqueId())==true) {
                player.sendMessage(ChatColor.DARK_RED+"Please try again in "+bitQuest.REDIS.ttl("rate_limit:"+player.getUniqueId())+" seconds.");
                player.closeInventory();
                event.setCancelled(true);
                return;

            } else if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                final User user;
                try {
                    user = new User(bitQuest.db_con, player.getUniqueId());

                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.DARK_RED+"Problem loading your account. Please try again later.");
                    player.closeInventory();
                    event.setCancelled(true);
                    return;
                }
                // player buys
                final ItemStack clicked = event.getCurrentItem();
                if (clicked != null && clicked.getType() != Material.AIR) {
                    System.out.println("[purchase] " + player.getName() + " <- " + clicked.getType());
                    player.sendMessage(ChatColor.YELLOW + "Purchasing " + clicked.getType() + "...");
                    player.closeInventory();
                    event.setCancelled(true);
                    int sat = 0;
                    Trade trade = null;
                    for (int i = 0; i < trades.size(); i++) {
                        if (clicked.getType() == trades.get(i).itemStack.getType()) {
                            sat = trades.get(i).price;
                            trade = trades.get(i);
                        }
                    }
                    boolean hasOpenSlots = false;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item == null
                                || (item.getType() == clicked.getType()
                                && item.getAmount() + clicked.getAmount() < item.getMaxStackSize())) {
                            hasOpenSlots = true;
                            break;
                        }
                    }
                    final boolean hasOpenSlotsFinal = hasOpenSlots;
                    final long satFinal = sat * BitQuest.DENOMINATION_FACTOR;
                    if (bitQuest.BLOCKCYPHER_CHAIN != null) {

                        try {
                            if (user.wallet.getBalance(3) >= satFinal) {

                                if (hasOpenSlotsFinal) {

                                    if (user.wallet.payment(bitQuest.wallet.address, satFinal)) {
                                        if (clicked.getType() == Material.ENCHANTED_BOOK)
                                            bitQuest.books.remove(0);

                                        ItemStack item = event.getCurrentItem();
                                        ItemMeta meta = item.getItemMeta();
                                        ArrayList<String> Lore = new ArrayList<String>();
                                        meta.setLore(null);
                                        item.setItemMeta(meta);
                                        player.getInventory().addItem(item);
                                        player.sendMessage(
                                                ChatColor.GREEN
                                                        + "You bought "
                                                        + clicked.getType()
                                                        + " for "
                                                        + ChatColor.LIGHT_PURPLE
                                                        + satFinal / 100
                                                        + " (+ miner fees)");

                                        bitQuest.updateScoreboard(player);


                                    } else {
                                        player.sendMessage(
                                                ChatColor.RED
                                                        + "Transaction failed. Please try again in a few moments (ERROR 1)");
                                    }
                                } else {
                                    player.sendMessage(
                                            ChatColor.DARK_RED + "You don't have space in your inventory");
                                }
                            } else {
                                player.sendMessage(ChatColor.DARK_RED + "You don't have enough " + BitQuest.DENOMINATION_NAME);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.DARK_RED + "Problem reading your wallet balance. Please try again later.");
                            player.closeInventory();
                            event.setCancelled(true);

                        }

                    } else {
                        // emeralds
                        try {
                            if (user.countEmeralds(player.getInventory()) >= satFinal / 100) {

                                if (hasOpenSlotsFinal) {
                                    if (user.removeEmeralds((int) satFinal / 100,player)) {
                                        if (clicked.getType() == Material.ENCHANTED_BOOK)
                                            bitQuest.books.remove(0);

                                        ItemStack item = event.getCurrentItem();
                                        ItemMeta meta = item.getItemMeta();
                                        ArrayList<String> Lore = new ArrayList<String>();
                                        meta.setLore(null);
                                        item.setItemMeta(meta);
                                        player.getInventory().addItem(item);
                                        player.sendMessage(
                                                ChatColor.GREEN
                                                        + "You bought "
                                                        + clicked.getType()
                                                        + " for "
                                                        + ChatColor.LIGHT_PURPLE
                                                        + satFinal / 100);

                                        bitQuest.updateScoreboard(player);


                                    } else {
                                        player.sendMessage(
                                                ChatColor.RED
                                                        + "Transaction failed. Please try again in a few moments (ERROR 1)");
                                    }
                                } else {
                                    player.sendMessage(
                                            ChatColor.DARK_RED + "You don't have space in your inventory");
                                }
                            } else {
                                player.sendMessage(ChatColor.DARK_RED + "You don't have enough ems");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    @EventHandler
    void onInteract(PlayerInteractEntityEvent event) {
        // VILLAGER
        if (event.getRightClicked().getType().equals(EntityType.VILLAGER)) {
            event.setCancelled(true);
            // compass

            // open menu

            Inventory marketInventory = Bukkit.getServer().createInventory(null, 54, "Market");
            for (int i = 0; i < trades.size(); i++) {
                int inventory_stock = bitQuest.MAX_STOCK;

                if (inventory_stock > 0) {
                    ItemStack button = new ItemStack(trades.get(i).itemStack);
                    ItemMeta meta = button.getItemMeta();
                    ArrayList<String> lore = new ArrayList<String>();
                    int bits_price;
                    bits_price = trades.get(i).price;

                    lore.add("Price: " + bits_price);
                    meta.setLore(lore);
                    button.setItemMeta(meta);
                    marketInventory.setItem(i, button);
                }
            }
            if (bitQuest.books.size() > 0) {
                ItemStack button = new ItemStack(bitQuest.books.get(0));
                ItemMeta meta = button.getItemMeta();
                ArrayList<String> lore = new ArrayList<String>();
                int bits_price;
                bits_price = 2;

                lore.add("Price: " + bits_price);
                meta.setLore(lore);
                button.setItemMeta(meta);
                marketInventory.setItem(trades.size(), button);
            }
            event.getPlayer().openInventory(marketInventory);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryInteract(InventoryInteractEvent event) {
        event.setCancelled(false);
    }

    //@bitcoinjake09 updates scoreboard if emeralds
    @EventHandler
    public void OnPlayerPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        Material itemType = item.getType();
        if (((itemType == Material.EMERALD_BLOCK) || (itemType == Material.EMERALD)) && (BitQuest.REDIS.get("currency" + player.getUniqueId().toString()).equalsIgnoreCase("emerald"))) {
            try {
                bitQuest.updateScoreboard(player);
            } catch (Exception e) {
            }
        }
    }
	/*
    @EventHandler
    public void OnPlayerDropItem(PlayerDropItemEvent event)
    {  
        Player player = event.getPlayer();
        if(BitQuest.REDIS.get("currency"+player.getUniqueId().toString()).equalsIgnoreCase("emerald"))
        {
            try { bitQuest.updateScoreboard(player); } catch (Exception e){}
        }
    } */
}
