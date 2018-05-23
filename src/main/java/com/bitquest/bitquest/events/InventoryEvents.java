package com.bitquest.bitquest.events;

import com.bitquest.bitquest.*;
import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import java.io.IOException;
import java.text.ParseException;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class InventoryEvents implements Listener {
  BitQuest bitQuest;
  ArrayList<Trade> trades;

  public InventoryEvents(BitQuest plugin) {
    // Villager Prices
    // By default, prices are in bits (not satoshi)
    bitQuest = plugin;
    trades = new ArrayList<Trade>();
    trades.add(new Trade(new ItemStack(Material.CLAY_BALL, 16), 1));
    trades.add(new Trade(new ItemStack(Material.COOKED_BEEF, 32), 1));
    trades.add(new Trade(new ItemStack(Material.FENCE, 16), 1));
    trades.add(new Trade(new ItemStack(Material.GLASS, 32), 2));
    trades.add(new Trade(new ItemStack(Material.HAY_BLOCK, 2), 1));
    trades.add(new Trade(new ItemStack(Material.LEATHER, 8), 1));
    trades.add(new Trade(new ItemStack(Material.OBSIDIAN, 8), 1));
    trades.add(new Trade(new ItemStack(Material.RAILS, 8), 1));
    trades.add(new Trade(new ItemStack(Material.POWERED_RAIL, 4), 1));
    trades.add(new Trade(new ItemStack(Material.SANDSTONE, 16), 1));
    trades.add(new Trade(new ItemStack(Material.RED_SANDSTONE, 8), 1));
    trades.add(new Trade(new ItemStack(Material.SMOOTH_BRICK, 16), 1));
    trades.add(new Trade(new ItemStack(Material.BLAZE_ROD, 1), 2));
    trades.add(new Trade(new ItemStack(Material.CHORUS_FLOWER, 1), 5));
    trades.add(new Trade(new ItemStack(Material.DIAMOND, 4), 1));
    trades.add(new Trade(new ItemStack(Material.ENDER_STONE, 1), 4));
    trades.add(new Trade(new ItemStack(Material.IRON_BLOCK, 1), 8));
    trades.add(new Trade(new ItemStack(Material.IRON_INGOT, 8), 1));
    trades.add(new Trade(new ItemStack(Material.NETHERRACK, 1), 2));
    trades.add(new Trade(new ItemStack(Material.QUARTZ, 8), 1));
    trades.add(new Trade(new ItemStack(Material.SOUL_SAND, 2), 1));
    trades.add(new Trade(new ItemStack(Material.SPONGE, 1), 2));
    trades.add(new Trade(new ItemStack(Material.LOG, 64), 1));
    trades.add(new Trade(new ItemStack(Material.WOOL, 16), 1));
    trades.add(new Trade(new ItemStack(Material.PAPER, 1), 4));
    trades.add(new Trade(new ItemStack(Material.PACKED_ICE, 1), 2));
    trades.add(new Trade(new ItemStack(Material.GOLD_BLOCK, 1), 2));
    trades.add(new Trade(new ItemStack(Material.GOLDEN_APPLE, 1), 5));
    trades.add(new Trade(new ItemStack(Material.ARROW, 32), 1));
    trades.add(new Trade(new ItemStack(Material.PRISMARINE, 2), 1));
    trades.add(new Trade(new ItemStack(Material.QUARTZ_BLOCK, 2), 1));
    trades.add(new Trade(new ItemStack(Material.SEA_LANTERN, 2), 1));
    trades.add(new Trade(new ItemStack(Material.GLOWSTONE, 2), 1));
    trades.add(new Trade(new ItemStack(Material.ANVIL, 1), 2));
    trades.add(new Trade(new ItemStack(Material.EMERALD_BLOCK, 1), 1));
    trades.add(new Trade(new ItemStack(Material.NETHER_STALK, 2), 1));
    trades.add(new Trade(new ItemStack(Material.LAPIS_ORE, 2), 1));
    trades.add(new Trade(new ItemStack(Material.SADDLE, 1), 2));
    trades.add(new Trade(new ItemStack(Material.SLIME_BALL, 2), 1));
    trades.add(new Trade(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1), 20));
    trades.add(new Trade(new ItemStack(Material.APPLE, 4), 1));
    trades.add(new Trade(new ItemStack(Material.ELYTRA, 1), 200));
    trades.add(new Trade(new ItemStack(Material.PURPLE_SHULKER_BOX, 1), 500));
    trades.add(new Trade(new ItemStack(Material.BOOK_AND_QUILL, 1), 2));
    trades.add(new Trade(new ItemStack(Material.CAKE, 2), 1));
    trades.add(new Trade(new ItemStack(Material.DRAGONS_BREATH, 1), 5));
    trades.add(new Trade(new ItemStack(Material.EMPTY_MAP, 1), 3));
    trades.add(new Trade(new ItemStack(Material.PUMPKIN, 6), 1));
  }

  @EventHandler
  void onInventoryClick(final InventoryClickEvent event)
      throws IOException, ParseException, org.json.simple.parser.ParseException {
    final Player player = (Player) event.getWhoClicked();
    final Inventory inventory = event.getInventory();
    final User user = new User(bitQuest, player);

    // Merchant inventory
    if (inventory.getName().equalsIgnoreCase("Market")) {
      if (bitQuest.rate_limit == false) {
        bitQuest.rate_limit = true;
        if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
          // player buys
          final ItemStack clicked = event.getCurrentItem();
          if (clicked != null && clicked.getType() != Material.AIR) {
            System.out.println("[purchase] " + player.getName() + " <- " + clicked.getType());
            player.sendMessage(ChatColor.YELLOW + "Purchasing " + clicked.getType() + "...");

            player.closeInventory();
            event.setCancelled(true);

            try {
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
              user.wallet.getBalance(
                  0,
                  new Wallet.GetBalanceCallback() {
                    @Override
                    public void run(Long balance) {
                      try {
                        if (balance >= satFinal) {

                          if (hasOpenSlotsFinal) {
                            if (user.wallet.move("bitquest_market", satFinal)) {
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
                              if (bitQuest.messageBuilder != null) {

                                // Create an event
                                org.json.JSONObject sentEvent =
                                    bitQuest.messageBuilder.event(
                                        player.getUniqueId().toString(), "Purchase", null);
                                org.json.JSONObject sentCharge =
                                    bitQuest.messageBuilder.trackCharge(
                                        player.getUniqueId().toString(), satFinal / 100, null);

                                ClientDelivery delivery = new ClientDelivery();
                                delivery.addMessage(sentEvent);
                                delivery.addMessage(sentCharge);

                                MixpanelAPI mixpanel = new MixpanelAPI();
                                mixpanel.deliver(delivery);
                              }

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
                          player.sendMessage(ChatColor.DARK_RED + "You don't have enough bits");
                        }
                      } catch (Exception e) {
                        e.printStackTrace();
                      }
                    }
                  });

            } catch (IllegalArgumentException e) {
              e.printStackTrace();
              player.sendMessage(
                  ChatColor.RED
                      + "Transaction failed. Please try again in a few moments (ERROR 2)");
            }
          }

        } else {
          // player sells (experimental)

          final ItemStack clicked = event.getCurrentItem();
          if (clicked != null && clicked.getType() == Material.ENCHANTED_BOOK) {
            event.setCancelled(true);
            player.closeInventory();
            System.out.println("[sell] " + player.getName() + " -> " + clicked.getType());
            player.getInventory().removeItem(clicked);
            player.sendMessage(ChatColor.YELLOW + "Selling " + clicked.getType() + "...");
            bitQuest.wallet.getBalance(
                0,
                new Wallet.GetBalanceCallback() {
                  @Override
                  public void run(Long balance) {
                    if (balance > 1024 * bitQuest.DENOMINATION_FACTOR) {

                      try {
                        System.out.println(balance);
                        if (bitQuest.wallet.move(
                            player.getUniqueId().toString(), 1 * bitQuest.DENOMINATION_FACTOR)) {
                          player.sendMessage(ChatColor.GREEN + "You sold a book");
                          bitQuest.updateScoreboard(player);
                          bitQuest.books.add(clicked);
                        }
                      } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        player.sendMessage(
                            ChatColor.RED
                                + "Transaction failed. Please try again in a few moments (ERROR 2)");
                      } catch (org.json.simple.parser.ParseException e) {
                        e.printStackTrace();
                        player.sendMessage(
                            ChatColor.RED
                                + "Transaction failed. Please try again in a few moments (ERROR 5)");

                      } catch (IOException e) {
                        e.printStackTrace();
                        player.sendMessage(
                            ChatColor.RED
                                + "Transaction failed. Please try again in a few moments (ERROR 6)");

                      } catch (ParseException e) {
                        e.printStackTrace();
                      }
                    } else {
                      player.sendMessage(ChatColor.RED + "I'm not buying right now");
                    }
                  }

                  //                        Trade trade = null;
                  //                        int sat = 0;
                  //                        for (int i = 0; i < trades.size(); i++) {
                  //                            if (clicked.getType() ==
                  // trades.get(i).itemStack.getType() && trades.get(i).has_stock == true) {
                  //                                sat = trades.get(i).price;
                  //                                trade = trades.get(i);
                  //                                if (trades.get(i).has_stock == true) {
                  //
                  //                                    sat =
                  // trades.get(i).price_for_stock(bitQuest.REDIS);
                  //                                }
                  //                            }
                  //
                  //                        }
                  //
                  //                        if (sat >= 100 && trade != null) {
                  //                            if (trade.has_stock == true &&
                  // trade.will_buy(bitQuest.REDIS)) {
                  //                                player.closeInventory();
                  //
                  //                                System.out.println("[sell] " + player.getName()
                  // + " -> " + clicked.getType());
                  //                                player.sendMessage(ChatColor.YELLOW + "Selling "
                  // + clicked.getType() + "...");
                  //                                if (bitQuest.wallet.payment(sat,
                  // user.wallet.address)) {
                  //
                  // player.getInventory().removeItem(trade.itemStack);
                  //
                  //                                    player.sendMessage(ChatColor.GREEN + "You
                  // sold " + clicked.getType() + " for " + ChatColor.LIGHT_PURPLE + sat / 100);
                  //                                    bitQuest.REDIS.incr("stock:" +
                  // trade.itemStack.getType());
                  //                                    System.out.println("[sell] stock: " +
                  // bitQuest.REDIS.get("stock:" + trade.itemStack.getType()));
                  //                                    bitQuest.updateScoreboard(player);
                  //                                    bitQuest.sendMetric("price." +
                  // clicked.getType(), trade.price_for_stock(bitQuest.REDIS));
                  //                                }
                  //                            } else {
                  //                                event.setCancelled(true);
                  //                                player.closeInventory();
                  //                                player.updateInventory();
                  //                                player.sendMessage(ChatColor.RED + "I have too
                  // much " + clicked.getType() + "...");
                  //
                  //                            }
                  //                        } else {
                  //                            event.setCancelled(true);
                  //                            player.closeInventory();
                  //                            player.updateInventory();
                  //                            player.sendMessage(ChatColor.DARK_RED + "I'm not
                  // buying " + clicked.getType() + "...");
                  //                        }

                });
          } else {
            player.sendMessage(
                ChatColor.RED + "Transaction failed. Please try again in a few moments (ERROR 4)");
          }
        }
      } else {
        player.sendMessage(
            ChatColor.RED + "Transaction failed. Please try again in a few moments (ERROR 3)");
        player.closeInventory();
        event.setCancelled(true);
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

        player.sendMessage(
            ChatColor.GREEN + "Teleporting to " + clicked.getItemMeta().getDisplayName() + "...");
        System.out.println("[teleport] " + player.getName() + " teleported to " + x + "," + z);
        player.closeInventory();

        player.setMetadata("teleporting", new FixedMetadataValue(bitQuest, true));
        Chunk c = new Location(bitQuest.getServer().getWorld("world"), x, 72, z).getChunk();
        if (!c.isLoaded()) {
          c.load();
        }
        final int tx = x;
        final int tz = z;
        bitQuest
            .getServer()
            .getScheduler()
            .scheduleSyncDelayedTask(
                bitQuest,
                new Runnable() {

                  public void run() {
                    Location location =
                        Bukkit.getServer()
                            .getWorld("world")
                            .getHighestBlockAt(tx, tz)
                            .getLocation();
                    player.teleport(location);
                    player.removeMetadata("teleporting", bitQuest);
                  }
                },
                60L);
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
}
