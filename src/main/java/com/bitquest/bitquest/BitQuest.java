package com.bitquest.bitquest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import redis.clients.jedis.Jedis;

/**
 * Created by explodi on 11/1/15.
 */

public class BitQuest extends JavaPlugin {
    // Connecting to REDIS
    // Links to the administration account via Environment Variables
    public final static UUID ADMIN_UUID = System.getenv("ADMIN_UUID") != null ? UUID.fromString(System.getenv("ADMIN_UUID")) : null;
    // If env MOD_OPS exists, server automatically ops moderators
    public final static String MOD_OPS = System.getenv("MOD_OPS") != null ? System.getenv("MOD_OPS") : null;
    // Look for Environment variables on hostname and port, otherwise defaults to localhost:6379
    public final static String REDIS_HOST = System.getenv("REDIS_1_PORT_6379_TCP_ADDR") != null ? System.getenv("REDIS_1_PORT_6379_TCP_ADDR") : "localhost";
    public final static Integer REDIS_PORT = System.getenv("REDIS_1_PORT_6379_TCP_PORT") != null ? Integer.parseInt(System.getenv("REDIS_1_PORT_6379_TCP_PORT")) : 6379;
    public final static Jedis REDIS = new Jedis(REDIS_HOST, REDIS_PORT);

    // utilities: distance and rand
    public static int distance(Location location1, Location location2) {
        return (int) location1.distance(location2);
    }

    public static int rand(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    @Override
    public void onEnable() {
        log("BitQuest starting...");
        if (ADMIN_UUID == null) {
            log("Warning: You haven't designated a super admin. Launch with ADMIN_UUID env variable to set.");
        }
        // registers listener classes
        getServer().getPluginManager().registerEvents(new BlockEvents(this), this);
        getServer().getPluginManager().registerEvents(new EntityEvents(this), this);

        // loads config file. If it doesn't exist, creates it.
        // get plugin config
        getDataFolder().mkdir();
        if (!new java.io.File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
    }

    public void log(String msg) {
        Bukkit.getLogger().info(msg);
    }

    public void success(Player recipient, String msg) {
        recipient.sendMessage(ChatColor.GREEN + msg);
        recipient.playSound(recipient.getLocation(), Sound.ORB_PICKUP, 20, 1);
    }

    public void error(Player recipient, String msg) {
        recipient.sendMessage(ChatColor.RED + msg);
        recipient.playSound(recipient.getLocation(), Sound.ANVIL_LAND, 7, 1);
    }

    public JsonObject areaForLocation(Location location) {
        List<String> areas = REDIS.lrange("areas", 0, -1);
        for (String areaJSON : areas) {
            JsonObject area = new JsonParser().parse(areaJSON).getAsJsonObject();
            int x = area.get("x").getAsInt();
            int z = area.get("z").getAsInt();
            int size = area.get("size").getAsInt();
            if (location.getX() > (x - size) && location.getX() < (x + size) && location.getZ() > (z - size) && location.getZ() < (z + size)) {
                return area;
            }

        }
        return null;
    }

    public boolean canBuild(Location location, Player player) {
        // returns true if player has permission to build in location
        // TODO: Find out how are we gonna deal with clans and locations, and how/if they are gonna share land resources
        if (areaForLocation(location) != null) {
            if (areaForLocation(location).get("owner").getAsString().equals(player.getUniqueId().toString())) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean createNewArea(Location location, Player owner, String name, int size) {
        // write the new area to REDIS
        JsonObject areaJSON = new JsonObject();
        areaJSON.addProperty("size", size);
        areaJSON.addProperty("owner", owner.getUniqueId().toString());
        areaJSON.addProperty("name", name);
        areaJSON.addProperty("x", location.getX());
        areaJSON.addProperty("z", location.getZ());
        areaJSON.addProperty("uuid", UUID.randomUUID().toString());
        REDIS.lpush("areas", areaJSON.toString());
        // TODO: Check if redis actually appended the area to list and return the success of the operation
        return true;
    }

    public boolean isModerator(Player player) {

            Set<String> moderators=REDIS.smembers("moderators");

            for(String uuid : moderators) {

                if(player.getUniqueId().toString().equals(uuid)) {
                    return true;
                }
            }
            if(ADMIN_UUID!=null && player.getUniqueId().toString().equals(ADMIN_UUID.toString())) {
                return true;
            }
            return false;

    }

    final int minLandSize = 1;
    final int maxLandSize = 512;
    final int minNameSize = 3;
    final int maxNameSize = 16;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // we don't allow server commands (yet?)
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (isModerator(player)==true) {
                // COMMAND: MOD
                if (cmd.getName().equalsIgnoreCase("mod")) {
                    Set<String> allplayers=REDIS.smembers("players");
                    if(args[0].equals("add")) {
                        // Sub-command: /mod add
                        if(REDIS.get("uuid"+args[1])!=null) {
                            UUID uuid=UUID.fromString(REDIS.get("uuid"+args[1]));
                            REDIS.sadd("moderators",uuid.toString());
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.RED+"Cannot find player "+args[1]);
                            return true;
                        }
                    } else if(args[0].equals("del")) {
                        // Sub-command: /mod del
                        if(REDIS.get("uuid"+args[1])!=null) {
                            UUID uuid=UUID.fromString(REDIS.get("uuid"+args[1]));
                            REDIS.srem("moderators",uuid.toString());
                            return true;
                        }
                        return false;
                    } else if(args[0].equals("list")) {
                        // Sub-command: /mod list
                        Set<String> moderators=REDIS.smembers("moderators");
                        for(String uuid:moderators) {
                            sender.sendMessage(ChatColor.YELLOW+REDIS.get("name"+uuid));
                        }
                        return true;
                    }
                }
                // COMMAND: ADDTOWN
                // Creates a new town. Towns are different from player houses
                // TODO: Establish differences between towns and houses
                if (cmd.getName().equalsIgnoreCase("addtown")) {
                    if (args.length > 1) {
                        // first, check that arg[0] (size) is an integer
                        try {
                            int size = Integer.parseInt(args[0]);
                            if (size >= minLandSize && size <= maxLandSize) {
                                StringBuilder builder = new StringBuilder();
                                for (int i = 1; i < args.length; i++) {
                                    builder.append(args[i]).append(" ");
                                }
                                String name = builder.toString().trim();

                                // ensure that name is alphanumeric and is within character limits
                                if (!name.matches("^.*[^a-zA-Z0-9 ].*$") && name.length() >= minNameSize && name.length() <= maxNameSize) {
                                    if (createNewArea(player.getLocation(), player, name, size)) {
                                        success(player, "Area '" + name + "' was created.");
                                    } else {
                                        error(player, "Error creating area");
                                    }
                                    return true;
                                } else {
                                    error(player, "Invalid land name! Must be " + minNameSize + "-" + maxNameSize + " characters!");
                                    return false;
                                }
                            } else {
                                error(player, "Invalid land size! Must be " + minLandSize + "-" + maxLandSize + "!");
                                return false;
                            }
                        } catch (Exception e) {
                            error(player, "Invalid land size! Must be " + minLandSize + "-" + maxLandSize + "!" + e.getLocalizedMessage());
                            return false;
                        }
                    } else {
                        error(player, "Please specify area name and size!");
                        return false;
                    }
                }
                // COMMAND: DELTOWN
                // deltown command for deleting the town you're currently in
                if (cmd.getName().equalsIgnoreCase("deltown")) {
                    JsonObject area = areaForLocation(((Player) sender).getLocation());
                    if (area != null) {
                        sender.sendMessage(REDIS.lrem("areas", 1, area.toString()).toString());
                    } else {
                        sender.sendMessage("0");
                    }
                    return true;
                }
                if (cmd.getName().equalsIgnoreCase("spectate") && args.length == 1) {
                	
                	if(Bukkit.getPlayer(args[0]) != null) {
                		((Player) sender).setGameMode(GameMode.SPECTATOR);
                    	((Player) sender).setSpectatorTarget(Bukkit.getPlayer(args[0]));
                    	success(((Player) sender), "You're now spectating " + args[0] + ".");
                	} else {
                		error(((Player) sender), "Player " + args[0] + " isn't online.");
                	}
                	return true;
                }
            }

            error((Player) sender, "You don't have permission to do that!");
            return true;
        }
        sender.sendMessage(ChatColor.RED+"This command is for players only!");
        return true;
    }
}

