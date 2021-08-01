package com.bitquest.bitquest.events;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.Trade;
import com.bitquest.bitquest.User;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
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
    // Food
    trades.add(new Trade(new ItemStack(Material.APPLE, 64), 1));
    trades.add(new Trade(new ItemStack(Material.COOKED_BEEF, 64), 10));
    trades.add(new Trade(new ItemStack(Material.CAKE, 1), 1));

    // Basic weapons
    trades.add(new Trade(new ItemStack(Material.BOW, 1), 1));
    trades.add(new Trade(new ItemStack(Material.ARROW, 64), 1));

    // Logs
    trades.add(new Trade(new ItemStack(Material.OAK_LOG, 64), 10));
    trades.add(new Trade(new ItemStack(Material.ACACIA_LOG, 64), 10));
    trades.add(new Trade(new ItemStack(Material.BIRCH_LOG, 64), 10));
    trades.add(new Trade(new ItemStack(Material.DARK_OAK_LOG, 64), 10));
    trades.add(new Trade(new ItemStack(Material.STRIPPED_OAK_LOG, 64), 10));
    trades.add(new Trade(new ItemStack(Material.JUNGLE_LOG, 64), 10));
    trades.add(new Trade(new ItemStack(Material.SPRUCE_LOG, 64), 10));
    
    // Construction
    trades.add(new Trade(new ItemStack(Material.WHITE_WOOL, 64), 10));
    trades.add(new Trade(new ItemStack(Material.PAPER, 64), 10));
    trades.add(new Trade(new ItemStack(Material.PACKED_ICE, 64), 10));
    trades.add(new Trade(new ItemStack(Material.GOLD_BLOCK, 64), 10));
    trades.add(new Trade(new ItemStack(Material.PRISMARINE, 64), 10));
    trades.add(new Trade(new ItemStack(Material.QUARTZ_BLOCK, 64), 10));
    trades.add(new Trade(new ItemStack(Material.QUARTZ, 64), 1));
    trades.add(new Trade(new ItemStack(Material.SEA_LANTERN, 64), 10));
    trades.add(new Trade(new ItemStack(Material.GLOWSTONE, 64), 10));
    trades.add(new Trade(new ItemStack(Material.ANVIL, 1), 10));
    trades.add(new Trade(new ItemStack(Material.LAPIS_ORE, 64), 10));
    trades.add(new Trade(new ItemStack(Material.SADDLE, 64), 10));
    trades.add(new Trade(new ItemStack(Material.SLIME_BALL, 64), 10));
    trades.add(new Trade(new ItemStack(Material.CLAY_BALL, 64), 10));
    trades.add(new Trade(new ItemStack(Material.GLASS, 64), 10));
    trades.add(new Trade(new ItemStack(Material.HAY_BLOCK, 64), 10));
    trades.add(new Trade(new ItemStack(Material.LEATHER, 64), 10));
    trades.add(new Trade(new ItemStack(Material.OBSIDIAN, 64), 20));
    trades.add(new Trade(new ItemStack(Material.RAIL, 64), 1));
    trades.add(new Trade(new ItemStack(Material.POWERED_RAIL, 64), 10));
    trades.add(new Trade(new ItemStack(Material.SAND, 64), 10));
    trades.add(new Trade(new ItemStack(Material.RED_SANDSTONE, 64), 10));
    trades.add(new Trade(new ItemStack(Material.BLAZE_ROD, 16), 10));
    trades.add(new Trade(new ItemStack(Material.DIAMOND, 64), 10));
    trades.add(new Trade(new ItemStack(Material.IRON_INGOT, 2), 1));

    // Others
    trades.add(new Trade(new ItemStack(Material.ELYTRA, 1), 100));
    trades.add(new Trade(new ItemStack(Material.PURPLE_SHULKER_BOX, 1), 50));
    trades.add(new Trade(new ItemStack(Material.MAP, 64), 10));
    trades.add(new Trade(new ItemStack(Material.PUMPKIN, 64), 1));
    trades.add(new Trade(new ItemStack(Material.CHORUS_FLOWER, 64), 10));
  }

  @EventHandler
  void inventoryCloseEvent(InventoryCloseEvent event) {
    if (event.getPlayer() instanceof Player) {
      bitQuest.setTotalExperience((Player) event.getPlayer());
    }
  }

  @EventHandler
  void onInteract(PlayerInteractEntityEvent event) {
    // VILLAGER
    if (event.getRightClicked().getType().equals(EntityType.VILLAGER)) {
      event.setCancelled(true);
      // compass

      // open menu
      Merchant merchant = Bukkit.createMerchant("Market");
      // create list of merchant recipes:
      List<MerchantRecipe> merchantRecipes = new ArrayList<MerchantRecipe>();      
      for (int i = 0; i < trades.size(); i++) {
        Trade trade = trades.get(i);
        MerchantRecipe recipe = new MerchantRecipe(trade.itemStack, 10000);
        recipe.addIngredient(new ItemStack(Material.EMERALD, Math.min(64,trade.price)));
        merchantRecipes.add(recipe);
      }
      merchant.setRecipes(merchantRecipes);
      event.getPlayer().openMerchant(merchant, true);
      // if (bitQuest.books.size() > 0) {
      //   ItemStack button = new ItemStack(bitQuest.books.get(0));
      //   ItemMeta meta = button.getItemMeta();
      //   ArrayList<String> lore = new ArrayList<String>();
      //   int bitsPrice;
      //   bitsPrice = 2;

      //   lore.add("Price: " + bitsPrice);
      //   meta.setLore(lore);
      //   button.setItemMeta(meta);
      //   marketInventory.setItem(trades.size(), button);
      // }
      // event.getPlayer().openInventory(marketInventory);
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
        && (bitQuest.redis
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
