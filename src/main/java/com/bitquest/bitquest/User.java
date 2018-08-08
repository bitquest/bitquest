package com.bitquest.bitquest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;

public class User {
  public Wallet wallet;
  private String clan;
  private BitQuest bitQuest;
  public Player player;

  public User(BitQuest _bitQuest, Player player) throws ParseException, org.json.simple.parser.ParseException, IOException, SQLException {
    this.player = player;
    this.bitQuest = _bitQuest;
    // this.wallet = new Wallet(this.player.getUniqueId().toString());

    PreparedStatement pst = this.bitQuest.db_con.prepareStatement("SELECT * FROM users");
    ResultSet rs = pst.executeQuery();

      while (rs.next()) {
        System.out.print(rs.getInt(1));
        System.out.print(": ");
        System.out.println(rs.getString(2));
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
    if (BitQuest.REDIS.get("experience.raw." + this.player.getUniqueId().toString()) == null) {
      return 0;
    } else {
      return Integer.parseInt(
          BitQuest.REDIS.get("experience.raw." + this.player.getUniqueId().toString()));
    }
  }

  public int countEmeralds() {

    ItemStack[] items = this.player.getInventory().getContents();
    int amount = 0;
    for (int i = 0; i < this.player.getInventory().getSize(); i++) {
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

  public boolean removeEmeralds(int amount) {
    int EmCount = this.countEmeralds();
    int LessEmCount = countEmeralds() - amount;
    double TempAmount = (double) amount;
    int EmsBack = 0;
    ItemStack[] items = this.player.getInventory().getContents();
    if (countEmeralds() >= amount) {
      while (TempAmount > 0) {
        for (int i = 0; i < this.player.getInventory().getSize(); i++) {
          ItemStack TempStack = items[i];

          if ((TempStack != null) && (TempStack.getType() != Material.AIR)) {

            if ((TempStack.getType().toString() == "EMERALD_BLOCK") && (TempAmount >= 9)) {
              this.player.getInventory().removeItem(new ItemStack(Material.EMERALD_BLOCK, 1));
              TempAmount = TempAmount - 9;
            }
            if ((TempStack.getType().toString() == "EMERALD_BLOCK") && (TempAmount < 9)) {
              this.player.getInventory().removeItem(new ItemStack(Material.EMERALD_BLOCK, 1));
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
    EmCount = countEmeralds();
    if ((EmCount == LessEmCount) || (TempAmount == 0)) return true;
    return false;
  } // end of remove emeralds
  // start addemeralds to inventory
  public boolean addEmeralds(int amount) {
    int EmCount = countEmeralds();
    int moreEmCount = countEmeralds() + amount;
    double bits = (double) amount;
    double TempAmount = (double) amount;
    int EmsBack = 0;
    while (TempAmount >= 0) {
      if (TempAmount >= 9) {
        TempAmount = TempAmount - 9;
        this.player.getInventory().addItem(new ItemStack(Material.EMERALD_BLOCK, 1));
      }
      if (TempAmount < 9) {
        TempAmount = TempAmount - 1;
        this.player.getInventory().addItem(new ItemStack(Material.EMERALD, 1));
      }
      EmCount = countEmeralds();
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
