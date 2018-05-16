package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;

public class MigrateClansCommand extends CommandAction {
    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        List < String > clans = new ArrayList < String > (BitQuest.REDIS.smembers("clans"));
        Map < String, List < String >> clansMembers = new HashMap < String, List < String >> ();

        ScanParams scanParams = new ScanParams();
        scanParams.match("clan:*[^:members]");
        String cursor = ScanParams.SCAN_POINTER_START;

        do {
            ScanResult < String > scanResult = BitQuest.REDIS.scan(cursor, scanParams);
            List < String > result = scanResult.getResult();

            for (String key: result) {
                String clan = BitQuest.REDIS.get(key);
                if (!clansMembers.containsKey(clan)) {
                    clansMembers.put(clan, new ArrayList < String > ());
                }

                String uuid = key.split(":")[1];
                clansMembers.get(clan).add(uuid);
            }

            cursor = scanResult.getStringCursor();
        } while (!cursor.equals(ScanParams.SCAN_POINTER_START));

        for (String clan: clans) {
            if (!clansMembers.containsKey(clan)) {
                BitQuest.REDIS.srem("clans", clan);
                BitQuest.REDIS.del("invitations:" + clan);
                player.sendMessage(ChatColor.GRAY + "Clan " + ChatColor.DARK_GRAY + clan + ChatColor.GRAY + " is empty. Deleted");
            } //GREEN : Worked, YELLOW : Processing, LIGHT_PURPLE : Any money balance, BLUE : Player name, DARK_BLUE UNDERLINE : Link, RED : Server error, DARK_RED : User error, GRAY : Info, DARK_GRAY : Clan
        }

        for (Map.Entry < String, List < String >> entry: clansMembers.entrySet()) {
            String clan = entry.getKey();
            for (String member: entry.getValue()) {
                BitQuest.REDIS.sadd("clan:" + clan + ":members", member);
                player.sendMessage(ChatColor.GREEN + "Player " + ChatColor.BLUE + member + ChatColor.GREEN + " added to clan " + ChatColor.DARK_GRAY + clan);
            }
        }

        player.sendMessage(ChatColor.GREEN + "Clans migrated.");
        return true;
    }
}
