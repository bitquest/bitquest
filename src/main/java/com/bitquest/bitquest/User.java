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
    public Player player;
    public User(Player player) throws ParseException, org.json.simple.parser.ParseException, IOException {
        this.player=player;
        if(BitQuest.REDIS.get("private"+this.player.getUniqueId().toString())!=null&&BitQuest.REDIS.get("address"+this.player.getUniqueId().toString())!=null) {
            this.wallet=new Wallet(BitQuest.REDIS.get("address"+this.player.getUniqueId().toString()),BitQuest.REDIS.get("private"+this.player.getUniqueId().toString()));
        }
    }

    // scoreboard objectives and teams
    public ScoreboardManager scoreboardManager;
    public Scoreboard scoreboard;
    public Objective scoreboardObjective;

    private int expFactor = 256;

    public void createScoreBoard() {
        scoreboardManager = Bukkit.getScoreboardManager();
        scoreboard = scoreboardManager.getNewScoreboard();
        scoreboardObjective = scoreboard.registerNewObjective("wallet","dummy");

    }

    public void addExperience(int exp) {
        BitQuest.REDIS.incrBy("experience.raw."+this.player.getUniqueId().toString(),exp);
        setTotalExperience(experience());
        System.out.println(exp);
    }

    public int experience() {
        if(BitQuest.REDIS.get("experience.raw."+this.player.getUniqueId().toString())==null) {
            return 0;
        } else {
            return Integer.parseInt(BitQuest.REDIS.get("experience.raw."+this.player.getUniqueId().toString()));
        }
    }

    public int getBalance() {
        int balance;
        if(BitQuest.REDIS.exists("balance"+player.getUniqueId().toString())) {
            balance=Integer.parseInt(BitQuest.REDIS.get("balance"+player.getUniqueId().toString()));
        } else {
            balance=wallet.balance();
            BitQuest.REDIS.set("balance"+player.getUniqueId().toString(),Integer.toString(balance));
            BitQuest.REDIS.expire("balance"+player.getUniqueId().toString(),6000);
        }
        return balance;
    }

    public void updateScoreboard() throws ParseException, org.json.simple.parser.ParseException, IOException {
        if (scoreboardObjective == null) {
            createScoreBoard();
        }
        int exp = experience();
        String level = Integer.toString(getLevel(exp));
        String nextLevel = Integer.toString(getLevel(exp) + 1);
        String expToLevel = Integer.toString(getRemainingExpToLevel(exp, getLevel(exp) + 1));

        scoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        scoreboardObjective.setDisplayName(ChatColor.GOLD + ChatColor.BOLD.toString() + "Bit" + ChatColor.GRAY + ChatColor.BOLD.toString() + "Quest");
        Score balanceTitleScore = scoreboardObjective.getScore(ChatColor.GOLD + ChatColor.BOLD.toString() + "Balance");
        Score balanceScore = scoreboardObjective.getScore(Integer.toString(getBalance()/100) + ChatColor.GREEN + " bits");
        Score spaceScore = scoreboardObjective.getScore("                    ");
        Score levelTitleScore = scoreboardObjective.getScore(ChatColor.DARK_GREEN + ChatColor.BOLD.toString() + "Level");
        Score xpToNextLvl = scoreboardObjective.getScore(ChatColor.GRAY + expToLevel+ "xp to next level");
        Score xpScore = scoreboardObjective.getScore(ChatColor.WHITE + level + " [" + progresBarForPercentage(getExpProgress(experience()), 25) + ChatColor.WHITE + "] " + nextLevel);
        balanceTitleScore.setScore(6);
        balanceScore.setScore(5);
        spaceScore.setScore(4);
        levelTitleScore.setScore(3);
        xpToNextLvl.setScore(2);
        xpScore.setScore(1);
        player.setScoreboard(scoreboard);
    }

    private String progresBarForPercentage(float percentage, int numOfBlocks) {
        String bar = "";
        float percentageOfBlock = 1.0f / numOfBlocks;
        ChatColor color;
        if (percentage < percentageOfBlock) {
            color = ChatColor.GRAY;
        } else {
            color = ChatColor.GREEN;
        }
        bar += color;
        for (int i = 1; i < numOfBlocks + 1; i++) {
            float currentPercentage = i * percentageOfBlock;

            if (currentPercentage > percentage && color == ChatColor.GREEN) {
                bar += ChatColor.GRAY;
                color = ChatColor.GRAY;
            }

            bar += "▎";
        }

        return bar;

    }

    public int getRemainingExpToLevel(int exp, int level) {
        int levelExp = getExpForLevel(level);

        return levelExp - exp;
    }

    public int getLevel(int exp) {
        return (int) Math.floor(Math.sqrt(exp / (float)expFactor));
    }

    public int getExpForLevel(int level) {
        return (int) Math.pow(level,2)*expFactor;
    }

    public float getExpProgress(int exp) {
        int level = getLevel(exp);
        int nextlevel = getExpForLevel(level + 1);
        int prevlevel = 0;
        if(level > 0) {
            prevlevel = getExpForLevel(level);
        }
        float progress = ((exp - prevlevel) / (float) (nextlevel - prevlevel));
        return progress;
    }

    public void setTotalExperience(int rawxp) {
        // lower factor, experience is easier to get. you can increase to get the opposite effect
        int level = getLevel(rawxp);
        float progress = getExpProgress(rawxp);

        player.setLevel(level);
        player.setExp(progress);
        setPlayerMaxHealth();

        try {
            updateScoreboard();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public int bitcoinBalance() throws IOException, org.json.simple.parser.ParseException {

        URL url = new URL("https://api.blockcypher.com/v1/"+BitQuest.BLOCKCHAIN+"/addrs/"+getAddress()+"/balance");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        int responseCode = con.getResponseCode();

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
