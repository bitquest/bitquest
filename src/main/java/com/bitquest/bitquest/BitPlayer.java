package com.bitquest.bitquest;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

/**
 * Created by cristian on 11/6/15.
 */
public class BitPlayer {
    private int exp;
    private String clan;
    private Player player;
    public BitPlayer(Player player) {
        this.player=player;
        loadUserData();
    }
    private boolean loadUserData() {
        try {
            if (BitQuest.REDIS.get(player.getUniqueId().toString()) != null) {
                BitQuest.LOG.info(BitQuest.REDIS.get(player.getUniqueId().toString()));
                return true;
            } else {
                // creates new player data entry and writes it to REDIS
                JsonObject playerData = new JsonObject();
                playerData.addProperty("exp", 0);
                BitQuest.REDIS.set(player.getUniqueId().toString(), playerData.toString());
                return true;
            }
        } catch(final Exception e) {
            BitQuest.LOG.info("Cannot load user data for "+player.getDisplayName());
            return false;
        }
    }
    private boolean setClan(String tag) {

    }
}
