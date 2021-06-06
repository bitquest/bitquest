package com.bitquest.bitquest;

import java.io.IOException;
import java.text.ParseException;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class User {
  public Wallet wallet;
  private String clan;
  public UUID uuid;
  private BitQuest bitQuest;

  public User(UUID uuid, BitQuest bitQuest)
      throws ParseException, org.json.simple.parser.ParseException, IOException {
    this.uuid = uuid;
    this.bitQuest = bitQuest;
    this.wallet = new Wallet(this.bitQuest.node, uuid.toString());
  }

  // Team walletScoreboardTeam = walletScoreboard.registerNewTeam("wallet");

  private int expFactor = 256;

  //    public void addExperience(int exp) {
  //        BitQuest.REDIS.incrBy("experience.raw."+this.player.getUniqueId().toString(),exp);
  //        setTotalExperience(experience());
  //        System.out.println(exp);
  //    }
  public int experience() {
    if (bitQuest.redis.get("experience.raw." + this.uuid.toString()) == null) {
      return 0;
    } else {
      return Integer.parseInt(bitQuest.redis.get("experience.raw." + this.uuid.toString()));
    }
  }

  public int countEmeralds(Inventory inventory) {

    ItemStack[] items = inventory.getContents();
    int amount = 0;
    for (int i = 0; i < inventory.getSize(); i++) {
      ItemStack tempStack = items[i];
      if ((tempStack != null) && (tempStack.getType() != Material.AIR)) {
        if (tempStack.getType().toString() == "EMERALD_BLOCK") {
          amount += (tempStack.getAmount() * 9);
        } else if (tempStack.getType().toString() == "EMERALD") {
          amount += tempStack.getAmount();
        }
      }
    }
    return amount;
  }

  public boolean removeEmeralds(int amount, Player player) {
    int emCount = this.countEmeralds(player.getInventory());
    int lessEmCount = countEmeralds(player.getInventory()) - amount;
    double tempAmount = (double) amount;
    int emsBack = 0;
    ItemStack[] items = player.getInventory().getContents();
    if (countEmeralds(player.getInventory()) >= amount) {
      while (tempAmount > 0) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
          ItemStack tempStack = items[i];

          if ((tempStack != null) && (tempStack.getType() != Material.AIR)) {

            if ((tempStack.getType().toString() == "EMERALD_BLOCK") && (tempAmount >= 9)) {
              player.getInventory().removeItem(new ItemStack(Material.EMERALD_BLOCK, 1));
              tempAmount = tempAmount - 9;
            }
            if ((tempStack.getType().toString() == "EMERALD_BLOCK") && (tempAmount < 9)) {
              player.getInventory().removeItem(new ItemStack(Material.EMERALD_BLOCK, 1));
              emsBack = (9 - (int) tempAmount); // if 8, ems back = 1
              tempAmount = tempAmount - tempAmount;
              if (emsBack > 0) {
                player.getInventory().addItem(new ItemStack(Material.EMERALD, emsBack));
              }
            }
            if ((tempStack.getType().toString() == "EMERALD") && (tempAmount >= 1)) {
              player.getInventory().removeItem(new ItemStack(Material.EMERALD, 1));
              tempAmount = tempAmount - 1;
            }
          } // end if != Material.AIR
        } // end for loop
      } // end while loop
    } // end (EmCount>=amount)
    emCount = countEmeralds(player.getInventory());
    if ((emCount == lessEmCount) || (tempAmount == 0)) {
      return true;
    }
    return false;
  } // end of remove emeralds

  // start addemeralds to inventory
  public boolean addEmeralds(int amount, Player player) {
    int emCount = countEmeralds(player.getInventory());
    int moreEmCount = countEmeralds(player.getInventory()) + amount;
    double bits = (double) amount;
    double tempAmount = (double) amount;
    int emsBack = 0;
    while (tempAmount >= 0) {
      if (tempAmount >= 9) {
        tempAmount = tempAmount - 9;
        player.getInventory().addItem(new ItemStack(Material.EMERALD_BLOCK, 1));
      }
      if (tempAmount < 9) {
        tempAmount = tempAmount - 1;
        player.getInventory().addItem(new ItemStack(Material.EMERALD, 1));
      }
      emCount = countEmeralds(player.getInventory());
      if ((emCount == moreEmCount)) {
        return true;
      }
    } // end while loop
    return false;
  }
  //
  //    public int getLevel(int exp) {
  //        return (int) Math.floor(Math.sqrt(exp / (float)expFactor));
  //    }
  //
  //    public int getExpForLevel(int level) {
  //        return (int) Math.pow(level,2)*expFactor;
  //    }
  //
  //    public float getExpProgress(int exp) {
  //        int level = getLevel(exp);
  //        int nextlevel = getExpForLevel(level + 1);
  //        int prevlevel = 0;
  //        if(level > 0) {
  //            prevlevel = getExpForLevel(level);
  //        }
  //        float progress = ((exp - prevlevel) / (float) (nextlevel - prevlevel));
  //        return progress;
  //    }
  //
  //
  //
  //
  //    public void setPlayerMaxHealth() {
  //        int health=1+player.getLevel();
  //        if(health>40) health=40;
  //        player.setMaxHealth(health);
  //    }

}
