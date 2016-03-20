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
    private Player player;
    public User(Player player) throws ParseException, org.json.simple.parser.ParseException, IOException {
        this.player=player;
        if(BitQuest.REDIS.get("public"+this.player.getUniqueId().toString())==null||BitQuest.REDIS.get("private"+this.player.getUniqueId().toString())==null||BitQuest.REDIS.get("address"+this.player.getUniqueId().toString())==null) {
            generateBitcoinAddress();
        }
        this.wallet=new Wallet(BitQuest.REDIS.get("address"+this.player.getUniqueId().toString()),BitQuest.REDIS.get("private"+this.player.getUniqueId().toString()));

    }

    // scoreboard objectives and teams
    public ScoreboardManager scoreboardManager;
    public Scoreboard walletScoreboard;
    // Team walletScoreboardTeam = walletScoreboard.registerNewTeam("wallet");
    public Objective walletScoreboardObjective;
    public void createScoreBoard() {
        scoreboardManager = Bukkit.getScoreboardManager();
        walletScoreboard= scoreboardManager.getNewScoreboard();
        walletScoreboardObjective = walletScoreboard.registerNewObjective("wallet","dummy");

    }

    public void addExperience(int exp) {
        BitQuest.REDIS.incrBy("experience.raw."+this.player.getUniqueId().toString(),exp);
        setTotalExperience(experience());

    }
    public int experience() {
        if(BitQuest.REDIS.get("experience.raw."+this.player.getUniqueId().toString())==null) {
            return 0;
        } else {
            return Integer.parseInt(BitQuest.REDIS.get("experience.raw."+this.player.getUniqueId().toString()));
        }
    }
    public void updateScoreboard() throws ParseException, org.json.simple.parser.ParseException, IOException {
        if (walletScoreboardObjective == null) {
            createScoreBoard();
        }
            walletScoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
            walletScoreboardObjective.setDisplayName(ChatColor.GOLD + ChatColor.BOLD.toString() + "Bit" + ChatColor.GRAY + ChatColor.BOLD.toString() + "Quest");
            Score score = walletScoreboardObjective.getScore(ChatColor.GREEN + "Balance:"); //Get a fake offline player
            score.setScore(wallet.balance()/100);
            player.setScoreboard(walletScoreboard);


    }
    public void setTotalExperience(int rawxp) {
        // xp = the square root of raw exp
        int xp = (int)Math.sqrt((double)rawxp);
        if(xp<1) xp=1;
       // System.out.println(xp);
        /*
        AUTHOR: Dev_Richard (https://www.spigotmc.org/members/dev_richard.38792/)
        DESC: A simple and easy to use class that can get and set a player's total experience points.
        Feel free to use this class in both public and private plugins, however if you release your
        plugin please link to this gist publicly so that others can contribute and benefit from it.
        */
        //Levels 0 through 15
        if(xp >= 0 && xp < 351) {
            //Calculate Everything
            int a = 1; int b = 6; int c = -xp;
            int level = (int) (-b + Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a);
            int xpForLevel = (int) (Math.pow(level, 2) + (6 * level));
            int remainder = xp - xpForLevel;
            int experienceNeeded = (2 * level) + 7;
            float experience = (float) remainder / (float) experienceNeeded;
            experience = round(experience, 2);
           // System.out.println("xpForLevel: " + xpForLevel);
           // System.out.println(experience);

            //Set Everything
            player.setLevel(level);
            player.setExp(experience);
            //Levels 16 through 30
        } else if(xp >= 352 && xp < 1507) {
            //Calculate Everything
            double a = 2.5; double b = -40.5; int c = -xp + 360;
            double dLevel = (-b + Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a);
            int level = (int) Math.floor(dLevel);
            int xpForLevel = (int) (2.5 * Math.pow(level, 2) - (40.5 * level) + 360);
            int remainder = xp - xpForLevel;
            int experienceNeeded = (5 * level) - 38;
            float experience = (float) remainder / (float) experienceNeeded;
            experience = round(experience, 2);
           // System.out.println("xpForLevel: " + xpForLevel);
            // System.out.println(experience);

            //Set Everything
            player.setLevel(level);
            player.setExp(experience);
            //Level 31 and greater
        } else {
            //Calculate Everything
            double a = 4.5; double b = -162.5; int c = -xp + 2220;
            double dLevel = (-b + Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a);
            int level = (int) Math.floor(dLevel);
            int xpForLevel = (int) (4.5 * Math.pow(level, 2) - (162.5 * level) + 2220);
            int remainder = xp - xpForLevel;
            int experienceNeeded = (9 * level) - 158;
            float experience = (float) remainder / (float) experienceNeeded;
            experience = round(experience, 2);
           // System.out.println("xpForLevel: " + xpForLevel);
           // System.out.println(experience);

            //Set Everything
            player.setLevel(level);
            player.setExp(experience);
        }
        setPlayerMaxHealth();
    }
    private float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_DOWN);
        return bd.floatValue();
    }

    public void setPlayerMaxHealth() {
        int health=20+new Double(player.getLevel()/6.4).intValue();
        if(health>40) health=40;
        player.setMaxHealth(health);
    }
    public String getAddress() {
        return BitQuest.REDIS.get("address"+this.player.getUniqueId().toString());
    }
    public void generateBitcoinAddress() throws IOException, ParseException, org.json.simple.parser.ParseException {

        URL url = new URL("https://api.blockcypher.com/v1/btc/main/addrs");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        // System.out.println("\nSending 'POST' request to URL : " + url);
        // System.out.println("Post parameters : " + urlParameters);
        // System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONParser parser = new JSONParser();
        final JSONObject jsonobj = (JSONObject) parser.parse(response.toString());
        System.out.println(response.toString());
        BitQuest.REDIS.set("private"+this.player.getUniqueId().toString(), (String) jsonobj.get("private"));
        BitQuest.REDIS.set("public"+this.player.getUniqueId().toString(), (String) jsonobj.get("public"));
        BitQuest.REDIS.set("address"+this.player.getUniqueId().toString(), (String) jsonobj.get("address"));

    }
    public int bitcoinBalance() throws IOException, org.json.simple.parser.ParseException {

        URL url = new URL("https://api.blockcypher.com/v1/btc/main/addrs/"+getAddress()+"/balance");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        int responseCode = con.getResponseCode();
        // System.out.println("\nSending 'POST' request to URL : " + url);
        // System.out.println("Post parameters : " + urlParameters);
        // System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        JSONParser parser = new JSONParser();
        final JSONObject jsonobj = (JSONObject) parser.parse(response.toString());
        return ((Number) jsonobj.get("final_balance")).intValue();


    }

    private boolean setClan(String tag) {
        // TODO: Write user clan info
        return false;
    }
}
