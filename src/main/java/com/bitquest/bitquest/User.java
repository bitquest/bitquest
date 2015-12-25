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
        scoreboardManager = Bukkit.getScoreboardManager();
        walletScoreboard= scoreboardManager.getNewScoreboard();
        walletScoreboardObjective = walletScoreboard.registerNewObjective("wallet","dummy");
    }
    // scoreboard objectives and teams
    public ScoreboardManager scoreboardManager;
    public Scoreboard walletScoreboard;
    // Team walletScoreboardTeam = walletScoreboard.registerNewTeam("wallet");
    public Objective walletScoreboardObjective;

    public void addExperience(int exp) {
        BitQuest.REDIS.incrBy("exp"+this.player.getUniqueId().toString(),exp);
        updateLevels();
        player.setTotalExperience(experience());

    }
    public int experience() {
        if(BitQuest.REDIS.get("exp"+this.player.getUniqueId().toString())==null) {
            return 0;
        } else {
            return Integer.parseInt(BitQuest.REDIS.get("exp"+this.player.getUniqueId().toString()));
        }
    }
    public void updateScoreboard() throws ParseException, org.json.simple.parser.ParseException, IOException {
        walletScoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        walletScoreboardObjective.setDisplayName(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Wallet");
        Score score = walletScoreboardObjective.getScore(ChatColor.GREEN + "Balance:"); //Get a fake offline player
        score.setScore(new User(player).wallet.balance()/100);
        player.setScoreboard(walletScoreboard);
    }

    public void updateLevels() {
        int factor=4;
        int exp=player.getTotalExperience();
        double maxexp=2000*Math.pow((factor*127),2);
        if(exp>maxexp) {
            player.setTotalExperience(new Double(maxexp).intValue());
        }
        int level=1;
        double rawlevel=((Math.sqrt(exp/2000))/factor)+1;
        level=new Double(rawlevel).intValue();
        player.setLevel(level);
        player.setExp((float) (rawlevel-level));
        setPlayerMaxHealth();
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
