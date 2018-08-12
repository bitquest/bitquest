package com.bitquest.bitquest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;

public class User {
  public Wallet wallet;
  private String clan;
  public UUID uuid;

  public User(Connection db_con, UUID _uuid) throws ParseException, org.json.simple.parser.ParseException, IOException, SQLException {
    this.uuid = _uuid;
    PreparedStatement pst = db_con.prepareStatement("SELECT * FROM users WHERE uuid='"+this.uuid+"'");
    ResultSet rs = pst.executeQuery();
    if(rs.next()) {
      System.out.print(rs.getInt(1));
      System.out.print(": ");
      System.out.println(rs.getString(2));
    } else {
      System.out.println("[user not found] "+this.uuid);
      this.wallet=BitQuest.generateNewWallet();
      this.wallet.save(this.uuid,db_con);
    }
  }

  // Team walletScoreboardTeam = walletScoreboard.registerNewTeam("wallet");

  private int expFactor = 256;

  //    public void addExperience(int exp) {
  //        BitQuest.REDIS.incrBy("experience.raw."+this.player.getUniqueId().toString(),exp);
  //        setTotalExperience(experience());
  //        System.out.println(exp);
  //    }
  public int experience() {
    if (BitQuest.REDIS.get("experience.raw." + this.uuid.toString()) == null) {
      return 0;
    } else {
      return Integer.parseInt(
          BitQuest.REDIS.get("experience.raw." + this.uuid.toString()));
    }
  }

  public int countEmeralds(Inventory inventory) {

    ItemStack[] items = inventory.getContents();
    int amount = 0;
    for (int i = 0; i < inventory.getSize(); i++) {
      ItemStack TempStack = items[i];
      if ((TempStack != null) && (TempStack.getType() != Material.AIR)) {
        if (TempStack.getType().toString() == "EMERALD_BLOCK") {
          amount += (TempStack.getAmount() * 9);
        } else if (TempStack.getType().toString() == "EMERALD") {
          amount += TempStack.getAmount();
        }
      }
    }
    return amount;
  }

  public boolean removeEmeralds(int amount, Player player) {
    int EmCount = this.countEmeralds(player.getInventory());
    int LessEmCount = countEmeralds(player.getInventory()) - amount;
    double TempAmount = (double) amount;
    int EmsBack = 0;
    ItemStack[] items = player.getInventory().getContents();
    if (countEmeralds(player.getInventory()) >= amount) {
      while (TempAmount > 0) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
          ItemStack TempStack = items[i];

          if ((TempStack != null) && (TempStack.getType() != Material.AIR)) {

            if ((TempStack.getType().toString() == "EMERALD_BLOCK") && (TempAmount >= 9)) {
              player.getInventory().removeItem(new ItemStack(Material.EMERALD_BLOCK, 1));
              TempAmount = TempAmount - 9;
            }
            if ((TempStack.getType().toString() == "EMERALD_BLOCK") && (TempAmount < 9)) {
              player.getInventory().removeItem(new ItemStack(Material.EMERALD_BLOCK, 1));
              EmsBack = (9 - (int) TempAmount); // if 8, ems back = 1
              TempAmount = TempAmount - TempAmount;
              if (EmsBack > 0) {
                player.getInventory().addItem(new ItemStack(Material.EMERALD, EmsBack));
              }
            }
            if ((TempStack.getType().toString() == "EMERALD") && (TempAmount >= 1)) {
              player.getInventory().removeItem(new ItemStack(Material.EMERALD, 1));
              TempAmount = TempAmount - 1;
            }
          } // end if != Material.AIR
        } // end for loop
      } // end while loop
    } // end (EmCount>=amount)
    EmCount = countEmeralds(player.getInventory());
    if ((EmCount == LessEmCount) || (TempAmount == 0)) return true;
    return false;
  } // end of remove emeralds
  // start addemeralds to inventory
  public boolean addEmeralds(int amount, Player player) {
    int EmCount = countEmeralds(player.getInventory());
    int moreEmCount = countEmeralds(player.getInventory()) + amount;
    double bits = (double) amount;
    double TempAmount = (double) amount;
    int EmsBack = 0;
    while (TempAmount >= 0) {
      if (TempAmount >= 9) {
        TempAmount = TempAmount - 9;
        player.getInventory().addItem(new ItemStack(Material.EMERALD_BLOCK, 1));
      }
      if (TempAmount < 9) {
        TempAmount = TempAmount - 1;
        player.getInventory().addItem(new ItemStack(Material.EMERALD, 1));
      }
      EmCount = countEmeralds(player.getInventory());
      if ((EmCount == moreEmCount)) return true;
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
