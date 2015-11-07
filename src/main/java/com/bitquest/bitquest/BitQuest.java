package com.bitquest.bitquest;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;

import java.util.logging.Logger;

/**
 * Created by explodi on 11/1/15.
 */



public class BitQuest extends JavaPlugin {
    // let's get constants from the environment
    public final static String BITQUEST_ENV=System.getenv("BITQUEST_ENV");
    public final static Jedis REDIS=new Jedis("localhost",6379);
    public final static Logger LOG=Bukkit.getLogger();
    @Override
    public void onEnable() {
        LOG.info("BitQuest starting...");
        // registers listener classes
        getServer().getPluginManager().registerEvents(new BlockEvents(this),this);
        getServer().getPluginManager().registerEvents(new EntityEvents(this),this);
        // loads config file. If it doesn't exist, creates it.
        // get plugin config
        getDataFolder().mkdir();
        if(!new java.io.File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
    }
}

