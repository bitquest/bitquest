package com.bitquest.bitquest;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by explodi on 11/6/15.
 */
public class User {
    private int exp;
    private String clan;
    private Player player;
    public User(Player player) {
        this.player=player;
        loadUserData();
    }
    private boolean loadUserData() {
        try {
            if (BitQuest.REDIS.get(player.getUniqueId().toString()) != null) {
                Bukkit.getLogger().info(BitQuest.REDIS.get(player.getUniqueId().toString()));
                return true;
            } else {
                // creates new player data entry and writes it to REDIS
                JsonObject playerData = new JsonObject();
                playerData.addProperty("exp", 0);
                BitQuest.REDIS.set(player.getUniqueId().toString(), playerData.toString());
                return true;
            }
        } catch(final Exception e) {
            return false;
        }
    }
    private boolean setClan(String tag) {
        // TODO: Write user clan info
        return false;
    }
}
