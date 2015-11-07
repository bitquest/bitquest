package com.bitquest.bitquest;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Created by explodi on 11/1/15.
 */



public class BitQuest extends JavaPlugin {
	
    public Logger log = Bukkit.getLogger();
    @Override
    public void onEnable() {
        log.info("BitQuest starting...");
        // registers listener classes
        getServer().getPluginManager().registerEvents(new BlockEvents(this),this);
        // loads config file. If it doesn't exist, creates it.
        // get plugin config
        getDataFolder().mkdir();
        if(!new java.io.File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
    }

}

