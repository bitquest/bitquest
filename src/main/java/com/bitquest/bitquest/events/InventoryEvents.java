package com.bitquest.bitquest.events;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.Trade;
import com.bitquest.bitquest.User;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * InventoryEvents
 * Catches events in the user's inventories, chests and merchants.
 */
public class InventoryEvents implements Listener {
  BitQuest bitQuest;
  ArrayList<Trade> trades;

  /**
   * By default, prices are in bits (not satoshi).
   */
  public InventoryEvents(BitQuest plugin) {
    bitQuest = plugin;
    trades = new ArrayList<>();
    trades.add(new Trade(new ItemStack(Material.CLAY_BALL, 64), 100));
    trades.add(new Trade(new ItemStack(Material.COOKED_BEEF, 64), 100));
    trades.add(new Trade(new ItemStack(Material.FENCE, 64), 100));
    trades.add(new Trade(new ItemStack(Material.GLASS, 64), 100));
    trades.add(new Trade(new ItemStack(Material.HAY_BLOCK, 64), 100));
    trades.add(new Trade(new ItemStack(Material.LEATHER, 64), 100));
    trades.add(new Trade(new ItemStack(Material.OBSIDIAN, 64), 200));
    trades.add(new Trade(new ItemStack(Material.RAILS, 64), 100));
    trades.add(new Trade(new ItemStack(Material.POWERED_RAIL, 64), 200));
    trades.add(new Trade(new ItemStack(Material.SAND, 64), 100));
    trades.add(new Trade(new ItemStack(Material.RED_SANDSTONE, 64), 100));
    trades.add(new Trade(new ItemStack(Material.SMOOTH_BRICK, 64), 100));
    trades.add(new Trade(new ItemStack(Material.BLAZE_ROD, 16), 100));
    trades.add(new Trade(new ItemStack(Material.CHORUS_FLOWER, 32), 100));
    trades.add(new Trade(new ItemStack(Material.DIAMOND, 64), 200));
    trades.add(new Trade(new ItemStack(Material.ENDER_STONE, 64), 200));
    trades.add(new Trade(new ItemStack(Material.IRON_INGOT, 64), 200));
    trades.add(new Trade(new ItemStack(Material.NETHERRACK, 64), 200));
    trades.add(new Trade(new ItemStack(Material.SOUL_SAND, 64), 100));
    trades.add(new Trade(new ItemStack(Material.SPONGE, 64), 100));
    trades.add(new Trade(new ItemStack(Material.LOG, 64), 100));
    trades.add(new Trade(new ItemStack(Material.WOOL, 64), 100));
    trades.add(new Trade(new ItemStack(Material.PAPER, 64), 100));
    trades.add(new Trade(new ItemStack(Material.PACKED_ICE, 64), 100));
    trades.add(new Trade(new ItemStack(Material.GOLD_BLOCK, 64), 100));
    trades.add(new Trade(new ItemStack(Material.ARROW, 64), 100));
    trades.add(new Trade(new ItemStack(Material.PRISMARINE, 64), 100));
    trades.add(new Trade(new ItemStack(Material.QUARTZ_BLOCK, 64), 100));
    trades.add(new Trade(new ItemStack(Material.SEA_LANTERN, 64), 100));
    trades.add(new Trade(new ItemStack(Material.GLOWSTONE, 64), 100));
    trades.add(new Trade(new ItemStack(Material.ANVIL, 1), 100));
    trades.add(new Trade(new ItemStack(Material.NETHER_STALK, 64), 100));
    trades.add(new Trade(new ItemStack(Material.LAPIS_ORE, 64), 100));
    trades.add(new Trade(new ItemStack(Material.SADDLE, 64), 100));
    trades.add(new Trade(new ItemStack(Material.SLIME_BALL, 64), 100));
    trades.add(new Trade(new ItemStack(Material.GOLDEN_APPLE, 32, (short) 1), 100));
    trades.add(new Trade(new ItemStack(Material.APPLE, 64), 100));
    trades.add(new Trade(new ItemStack(Material.ELYTRA, 1), 1000));
    trades.add(new Trade(new ItemStack(Material.PURPLE_SHULKER_BOX, 1), 500));
    trades.add(new Trade(new ItemStack(Material.BOOK_AND_QUILL, 64), 100));
    trades.add(new Trade(new ItemStack(Material.CAKE, 64), 100));
    trades.add(new Trade(new ItemStack(Material.DRAGONS_BREATH, 64), 200));
    trades.add(new Trade(new ItemStack(Material.EMPTY_MAP, 64), 100));
    trades.add(new Trade(new ItemStack(Material.PUMPKIN, 64), 100));
  }

  @EventHandler
  void onInventoryClick(final InventoryClickEvent event) {
    final Player player = (Player) event.getWhoClicked();
    final Inventory inventory = event.getInventory();

    // Merchant inventory
    if (inventory.getName().equalsIgnoreCase("Market")) {
      if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
        final User user;
        try {
          user = new User(player.getUniqueId());

        } catch (Exception e) {
          e.printStackTrace();
          player.sendMessage(
              ChatColor.DARK_RED + "Problem loading your account. Please try again later.");
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
          try {
            if (hasOpenSlotsFinal) {
              if (user.wallet.payment(bitQuest.wallet.address, satFinal)) {
                if (clicked.getType() == Material.ENCHANTED_BOOK) {
                  bitQuest.books.remove(0);
                }

                ItemStack item = event.getCurrentItem();
                ItemMeta meta = item.getItemMeta();
                ArrayList<String> lore = new ArrayList<String>();
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
                        + "Transaction failed. Please try again in a few moments");
              }
            } else {
              player.sendMessage(ChatColor.DARK_RED + "You don't have space in your inventory");
            }

          } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(
                ChatColor.DARK_RED
                    + "Problem reading your wallet balance. Please try again later.");
            player.closeInventory();
            event.setCancelled(true);
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
        int inventoryStock = bitQuest.MAX_STOCK;

        if (inventoryStock > 0) {
          ItemStack button = new ItemStack(trades.get(i).itemStack);
          ItemMeta meta = button.getItemMeta();
          ArrayList<String> lore = new ArrayList<String>();
          int bitsPrice;
          bitsPrice = (int) (trades.get(i).price
              + (BitQuest.MINER_FEE / BitQuest.DENOMINATION_FACTOR));
          lore.add("Price: " + bitsPrice);
          meta.setLore(lore);
          button.setItemMeta(meta);
          marketInventory.setItem(i, button);
        }
      }
      if (bitQuest.books.size() > 0) {
        ItemStack button = new ItemStack(bitQuest.books.get(0));
        ItemMeta meta = button.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        int bitsPrice;
        bitsPrice = 2;

        lore.add("Price: " + bitsPrice);
        meta.setLore(lore);
        button.setItemMeta(meta);
        marketInventory.setItem(trades.size(), button);
      }
      event.getPlayer().openInventory(marketInventory);
    }
  }

  // @EventHandler(priority = EventPriority.HIGH)
  // public void onInventoryOpen(InventoryOpenEvent event) {
  //   event.setCancelled(false);
  // }

  // @EventHandler(priority = EventPriority.HIGH)
  // public void onInventoryInteract(final InventoryInteractEvent event) {
  //   event.setCancelled(false);
  // }

  /**
   * updates scoreboard if emeralds.
   *
   * @param event picked up item
   */

  @EventHandler
  public void onPlayerPickup(final PlayerPickupItemEvent event) {
    Player player = event.getPlayer();
    ItemStack item = event.getItem().getItemStack();
    Material itemType = item.getType();
    if (((itemType == Material.EMERALD_BLOCK) || (itemType == Material.EMERALD))
        && (BitQuest.REDIS
        .get("currency" + player.getUniqueId().toString())
        .equalsIgnoreCase("emerald"))) {
      try {
        bitQuest.updateScoreboard(player);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }
    /*
  @EventHandler
  public void OnPlayerDropItem(PlayerDropItemEvent event)
  {
      Player player = event.getPlayer();
      if(BitQuest.REDIS.get("currency" + 
      player.getUniqueId().toString()).equalsIgnoreCase("emerald"))
      {
          try { bitQuest.updateScoreboard(player); } catch (Exception e){}
      }
  } 
  */
}
