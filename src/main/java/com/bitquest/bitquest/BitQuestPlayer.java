package com.bitquest.bitquest;

import org.bukkit.entity.Player;

/**
 * Created by cristian on 11/6/15.
 */
public class BitQuestPlayer {
    private int exp;
    private Player player;
    public BitQuestPlayer(Player player) {
        this.player=player;
        BitQuest.LOG.info(BitQuest.REDIS.get(player.getUniqueId().toString()));
    }
}
