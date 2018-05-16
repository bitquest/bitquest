package com.bitquest.bitquest;

import com.google.gson.JsonObject;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

/**
 * Created by explodi on 11/6/15.
 */
public class User {
    public Wallet wallet;
    private String clan;
    private BitQuest bitQuest;
    public Player player;
    public User(BitQuest plugin, Player player) throws ParseException, org.json.simple.parser.ParseException, IOException {
        this.player = player;
        this.bitQuest = plugin;
        this.wallet = new Wallet(this.bitQuest, this.player.getUniqueId().toString());
        //        if(BitQuest.REDIS.exists("hd:address:"+this.player.getUniqueId().toString())&&BitQuest.REDIS.exists("hd:path:"+this.player.getUniqueId().toString())&&BitQuest.REDIS.exists("hd:public:"+this.player.getUniqueId().toString())) {
        //            this.wallet=new Wallet(
        //                    BitQuest.REDIS.get("hd:address:"+this.player.getUniqueId().toString()),
        //                    BitQuest.REDIS.get("hd:path:"+this.player.getUniqueId().toString()),
        //                    BitQuest.REDIS.get("hd:public:"+this.player.getUniqueId().toString()));
        //
        //        }

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
            return Integer.parseInt(BitQuest.REDIS.get("experience.raw." + this.player.getUniqueId().toString()));
        }
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
