package com.bitquest.bitquest;

import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

import com.evilmidget38.UUIDFetcher;
import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by explodi on 11/1/15.
 */

public class  BitQuest extends JavaPlugin {
    // TODO: remove env variables not being used anymore
    // Connecting to REDIS
    // Links to the administration account via Environment Variables
    public final static String BITQUEST_ENV = System.getenv("BITQUEST_ENV") != null ? System.getenv("BITQUEST_ENV") : "development";
    public final static UUID ADMIN_UUID = System.getenv("ADMIN_UUID") != null ? UUID.fromString(System.getenv("ADMIN_UUID")) : null;
    public final static String BITCOIN_ADDRESS = System.getenv("BITCOIN_ADDRESS") != null ? System.getenv("BITCOIN_ADDRESS") : null;
    public final static String BITCOIN_PRIVATE_KEY = System.getenv("BITCOIN_PRIVATE_KEY") != null ? System.getenv("BITCOIN_PRIVATE_KEY") : null;
    public final static String BITCOIN_PUBLIC_KEY = System.getenv("BITCOIN_PUBLIC_KEY") != null ? System.getenv("BITCOIN_PUBLIC_KEY") : null;
    public final static String BLOCKCYPHER_API_KEY = System.getenv("BLOCKCYPHER_API_KEY") != null ? System.getenv("BLOCKCYPHER_API_KEY") : null;
    public final static String XAPO_API_KEY = System.getenv("XAPO_API_KEY") != null ? System.getenv("XAPO_API_KEY") : null;
    public final static String XAPO_SECRET = System.getenv("XAPO_SECRET") != null ? System.getenv("XAPO_SECRET") : null;
    public final static String CASHOUT_ADDRESS = System.getenv("CASHOUT_ADDRESS") != null ? System.getenv("CASHOUT_ADDRESS") : null;
    public final static String CASHOUT_PRIVATE_KEY = System.getenv("CASHOUT_PRIVATE_KEY") != null ? System.getenv("CASHOUT_PRIVATE_KEY") : null;
    public final static String CASHOUT_PUBLIC_KEY = System.getenv("CASHOUT_PUBLIC_KEY") != null ? System.getenv("CASHOUT_PUBLIC_KEY") : null;


    public final static String LAND_BITCOIN_ADDRESS = System.getenv("LAND_BITCOIN_ADDRESS") != null ? System.getenv("LAND_BITCOIN_ADDRESS") : null;

    public final static String MINER_FEE_ADDRESS = System.getenv("MINER_FEE_ADDRESS") != null ? System.getenv("MINER_FEE_ADDRESS") : null;

    // if BLOCKCHAIN is set, users can choose a blockchain supported by BlockCypher (very useful for development on testnet, or maybe DogeQuest?)
    public final static String BLOCKCHAIN = System.getenv("BLOCKCHAIN") != null ? System.getenv("BLOCKCHAIN") : "btc/main";

    // Support for the bitcore full node and insight-api.
    public final static String BITCORE_HOST = System.getenv("BITCORE_HOST") != null ? System.getenv("BITCORE_HOST") : null;

    // Support for statsd is optional but really cool
    public final static String STATSD_HOST = System.getenv("STATSD_HOST") != null ? System.getenv("STATSD_HOST") : null;
    public final static String STATSD_PREFIX = System.getenv("STATSD_PREFIX") != null ? System.getenv("STATSD_PREFIX") : "bitquest";
    public final static String STATSD_PORT = System.getenv("STATSD_PORT") != null ? System.getenv("STATSD_PORT") : "8125";
    // Support for mixpanel analytics
    public final static String MIXPANEL_TOKEN = System.getenv("MIXPANEL_TOKEN") != null ? System.getenv("MIXPANEL_TOKEN") : null;
    public MessageBuilder messageBuilder;
    // Support for slack bot
    public final static String SLACK_BOT_AUTH_TOKEN = System.getenv("SLACK_BOT_AUTH_TOKEN") != null ? System.getenv("SLACK_BOT_AUTH_TOKEN") : null;
    public final static String SLACK_BOT_REPORTS_CHANNEL = System.getenv("SLACK_BOT_REPORTS_CHANNEL") != null ? System.getenv("SLACK_BOT_REPORTS_CHANNEL") : "reports";
    public SlackSession slackBotSession;
    // REDIS: Look for Environment variables on hostname and port, otherwise defaults to localhost:6379
    public final static String REDIS_HOST = System.getenv("REDIS_1_PORT_6379_TCP_ADDR") != null ? System.getenv("REDIS_1_PORT_6379_TCP_ADDR") : "localhost";
    public final static Integer REDIS_PORT = System.getenv("REDIS_1_PORT_6379_TCP_PORT") != null ? Integer.parseInt(System.getenv("REDIS_1_PORT_6379_TCP_PORT")) : 6379;
    public final static Jedis REDIS = new Jedis(REDIS_HOST, REDIS_PORT);
    // FAILS
    // public final static JedisPool REDIS_POOL = new JedisPool(new JedisPoolConfig(), REDIS_HOST, REDIS_PORT);


    // public static ScoreboardManager manager = Bukkit.getScoreboardManager();
    // public static Scoreboard scoreboard = manager.getNewScoreboard();
    public final static int LAND_PRICE=20000;
    public final static int MIN_TRANS=20000;
    // utilities: distance and rand
    public static int distance(Location location1, Location location2) {
        return (int) location1.distance(location2);
    }

    public static int rand(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }
    public StatsDClient statsd;
    public Wallet wallet=null;

    @Override
    public void onEnable() {
        log("BitQuest starting");
        log("Using the "+BitQuest.BLOCKCHAIN+" blockchain");
        REDIS.set("STARTUP","1");
        REDIS.expire("STARTUP",300);
        if (ADMIN_UUID == null) {
            log("Warning: You haven't designated a super admin. Launch with ADMIN_UUID env variable to set.");
        }
        if(STATSD_HOST!=null && STATSD_PORT!=null) {
            statsd = new NonBlockingStatsDClient("bitquest", STATSD_HOST , new Integer(STATSD_PORT));
            System.out.println("StatsD support is on.");
        }
        // registers listener classes
        getServer().getPluginManager().registerEvents(new ChatEvents(this), this);
        getServer().getPluginManager().registerEvents(new BlockEvents(this), this);
        getServer().getPluginManager().registerEvents(new EntityEvents(this), this);
        getServer().getPluginManager().registerEvents(new InventoryEvents(this), this);
        getServer().getPluginManager().registerEvents(new SignEvents(this), this);
        getServer().getPluginManager().registerEvents(new ServerEvents(this), this);

        // player does not lose inventory on death
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule keepInventory on");

        // loads config file. If it doesn't exist, creates it.
        getDataFolder().mkdir();
        if (!new java.io.File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }

        // loads world wallet
        if(BITCOIN_ADDRESS!=null) {
            wallet=new Wallet(BITCOIN_ADDRESS);
            System.out.println("World wallet address is: "+wallet.address);
        } else {
            System.out.println("Server is shutting down because BITCOIN_ADDRESS is not set");
            Bukkit.shutdown();
        }
        // sets the redis save intervals
        REDIS.configSet("SAVE","900 1 300 10 60 10000");

        // initialize mixpanel (optional)
        if(MIXPANEL_TOKEN!=null) {
            messageBuilder = new MessageBuilder(MIXPANEL_TOKEN);
            System.out.println("Mixpanel support is on");
        }
        if (SLACK_BOT_AUTH_TOKEN != null) {
            slackBotSession = SlackSessionFactory.createWebSocketSlackSession(SLACK_BOT_AUTH_TOKEN);
            try {
                slackBotSession.connect();
            } catch (IOException e) {
                System.out.println("Slack bot connection failed with error: " + e.getMessage());
            }
        }
        // Removes all entities on server restart. This is a workaround for when large numbers of entities grash the server. With the release of Minecraft 1.11 and "max entity cramming" this will be unnecesary.
        //     removeAllEntities();
        killAllVillagers();
        createScheduledTimers();


        // creates scheduled timers (update balances, etc)
        createScheduledTimers();

    }
    public void updateScoreboard(Player player) throws ParseException, org.json.simple.parser.ParseException, IOException {
        ScoreboardManager scoreboardManager;
        Scoreboard walletScoreboard;
        Objective walletScoreboardObjective;
        scoreboardManager = Bukkit.getScoreboardManager();
        walletScoreboard= scoreboardManager.getNewScoreboard();
        walletScoreboardObjective = walletScoreboard.registerNewObjective("wallet","dummy");

        User user=new User(player);

        walletScoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        walletScoreboardObjective.setDisplayName(ChatColor.GOLD + ChatColor.BOLD.toString() + "Bit" + ChatColor.GRAY + ChatColor.BOLD.toString() + "Quest");
        Score score = walletScoreboardObjective.getScore(ChatColor.GREEN + "Balance:"); //Get a fake offline player

        int final_balance=Integer.parseInt(REDIS.get("final_balance:"+user.wallet.address));

        score.setScore(final_balance/100);
        player.setScoreboard(walletScoreboard);
    }
    public void createScheduledTimers() {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

//        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
//            @Override
//            public void run() {
//                for (Player player : Bukkit.getServer().getOnlinePlayers()){
//                    User user= null;
//                    try {
//                        // user.createScoreBoard();
//                        updateScoreboard(player);
//
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    } catch (org.json.simple.parser.ParseException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        // TODO: Handle rate limiting
//                    }
//                }
//            }
//        }, 0, 120L);
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                // A villager is born
                World world=Bukkit.getWorld("world");
                world.spawnEntity(world.getHighestBlockAt(world.getSpawnLocation()).getLocation(), EntityType.VILLAGER);
            }
        }, 0, 72000L);
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if(statsd!=null) {
                    sendWorldMetrics();
                }
            }
        }, 0, 1200L);
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if(statsd!=null) {
                    sendWalletMetrics();
                }
            }
        }, 0, 12000L);
        REDIS.set("lastloot","nobody");


    }
    public void sendMetric(String name,int value) {
        statsd.gauge(BITQUEST_ENV+"."+name,value);

    }
    public void sendWorldMetrics() {
        statsd.gauge(BITQUEST_ENV+".players",Bukkit.getServer().getOnlinePlayers().size());
        statsd.gauge(BITQUEST_ENV+".entities_world",Bukkit.getServer().getWorld("world").getEntities().size());
        statsd.gauge(BITQUEST_ENV+".entities_nether",Bukkit.getServer().getWorld("world_nether").getEntities().size());
        statsd.gauge(BITQUEST_ENV+".entities_the_end",Bukkit.getServer().getWorld("world_the_end").getEntities().size());
    }
    public  void sendWalletMetrics() {
        try {
            statsd.gauge(BITQUEST_ENV+".wallet_balance",wallet.final_balance());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
    }
    public void removeAllEntities() {
        World w=Bukkit.getWorld("world");
        List<Entity> entities = w.getEntities();
        int entitiesremoved=0;
        for ( Entity entity : entities){
            entity.remove();
            entitiesremoved=entitiesremoved+1;

        }
        System.out.println("Killed "+entitiesremoved+" entities");
    }
    public void killAllVillagers() {
        World w=Bukkit.getWorld("world");
        List<Entity> entities = w.getEntities();
        int villagerskilled=0;
        for ( Entity entity : entities){
            if ((entity instanceof Villager)) {
                villagerskilled=villagerskilled+1;
                ((Villager)entity).remove();
            }
        }
        System.out.println("Killed "+villagerskilled+" villagers");

    }
    public void log(String msg) {
        Bukkit.getLogger().info(msg);
    }

    public void success(Player recipient, String msg) {
        recipient.sendMessage(ChatColor.GREEN + msg);
    }

    public void error(Player recipient, String msg) {
        recipient.sendMessage(ChatColor.RED + msg);
    }

    public void claimLand(String name, Chunk chunk, Player player) throws ParseException, org.json.simple.parser.ParseException, IOException {
        // check that land actually has a name
        final int x = chunk.getX();
        final int z = chunk.getZ();
        System.out.println("[claim] "+player.getDisplayName()+" wants to claim "+x+","+z+" with name "+name);

        if (!name.isEmpty()) {
            // check that desired area name is alphanumeric
            boolean hasNonAlpha = name.matches("^.*[^a-zA-Z0-9 ].*$");
            if (!hasNonAlpha) {
                // 16 characters max
                if (name.length() <= 16) {


                    if (name.equalsIgnoreCase("the wilderness")) {
                        player.sendMessage(ChatColor.RED + "You cannot name your land that.");
                        return;
                    }
                    if (REDIS.get("chunk" + x + "," + z + "owner") == null) {
                        final User user = new User(player);
                        player.sendMessage(ChatColor.YELLOW + "Claiming land...");
                        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                        BitQuest bitQuest = this;

                        try {
                            Wallet paymentWallet;
                            if (BitQuest.LAND_BITCOIN_ADDRESS != null) {
                                paymentWallet = new Wallet(BitQuest.LAND_BITCOIN_ADDRESS);
                            } else {
                                paymentWallet = bitQuest.wallet;
                            }
                            if (user.wallet.payment(BitQuest.LAND_PRICE, paymentWallet.address)) {

                                BitQuest.REDIS.set("chunk" + x + "," + z + "owner", player.getUniqueId().toString());
                                BitQuest.REDIS.set("chunk" + x + "," + z + "name", name);
                                player.sendMessage(ChatColor.GREEN + "Congratulations! You're now the owner of " + name + "!");
                                if (bitQuest.messageBuilder != null) {

                                    // Create an event
                                    org.json.JSONObject sentEvent = bitQuest.messageBuilder.event(player.getUniqueId().toString(), "Claim", null);
                                    org.json.JSONObject sentCharge = bitQuest.messageBuilder.trackCharge(player.getUniqueId().toString(), BitQuest.LAND_PRICE / 100, null);


                                    ClientDelivery delivery = new ClientDelivery();
                                    delivery.addMessage(sentEvent);
                                    delivery.addMessage(sentCharge);


                                    MixpanelAPI mixpanel = new MixpanelAPI();
                                    mixpanel.deliver(delivery);
                                }
                            } else {
                                int balance = new User(player).wallet.balance();
                                if (balance < BitQuest.LAND_PRICE) {
                                    player.sendMessage(ChatColor.RED + "You don't have enough money! You need " +
                                            ChatColor.BOLD + Math.ceil((BitQuest.LAND_PRICE - balance) / 100) + ChatColor.RED + " more Bits.");
                                } else {
                                    player.sendMessage(ChatColor.RED + "Claim payment failed. Please try again later.");
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (org.json.simple.parser.ParseException e) {
                            e.printStackTrace();
                        }


                    } else if (REDIS.get("chunk" + x + "," + z + "owner").equals(player.getUniqueId().toString()) || isModerator(player)) {
                        if (name.equals("abandon")) {
                            // Abandon land
                            BitQuest.REDIS.del("chunk" + x + "," + z + "owner");
                            BitQuest.REDIS.del("chunk" + x + "," + z + "name");
                        } else if (name.startsWith("transfer ") && name.length() > 9) {
                            // If the name starts with "transfer " and has at least one more character,
                            // transfer land
                            final String newOwner = name.substring(9);
                            player.sendMessage(ChatColor.YELLOW + "Transfering land to " + newOwner + "...");


                            try {
                                UUID newOwnerUUID = UUIDFetcher.getUUIDOf(newOwner);
                                BitQuest.REDIS.set("chunk" + x + "," + z + "owner", newOwnerUUID.toString());
                                player.sendMessage(ChatColor.GREEN + "This land now belongs to " + newOwner);
                            } catch (Exception e) {
                                player.sendMessage(ChatColor.RED + "Could not find " + newOwner + ". Did you misspell their name?");
                            }

                        } else if (BitQuest.REDIS.get("chunk" + x + "," + z + "name").equals(name)) {
                            player.sendMessage(ChatColor.RED + "You already own this land!");
                        } else {
                            // Rename land
                            player.sendMessage(ChatColor.GREEN + "You renamed this land to " + name + ".");
                            BitQuest.REDIS.set("chunk" + x + "," + z + "name", name);
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED+"Your land name must be 16 characters max");
                }
            } else {
                player.sendMessage(ChatColor.RED+"Your land name must contain only letters and numbers");
            }
        } else {
            player.sendMessage(ChatColor.RED+"Your land must have a name");
        }
    }
    public boolean isOwner(Location location, Player player) {
        if (landIsClaimed(location)) {
            if (REDIS.get("chunk" + location.getChunk().getX() + "," + location.getChunk().getZ() + "owner").equals(player.getUniqueId().toString())) {
                // player is the owner of the chunk
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public boolean canBuild(Location location, Player player) {
        // returns true if player has permission to build in location
        // TODO: Find out how are we gonna deal with clans and locations, and how/if they are gonna share land resources
        if(isModerator(player)) {
            return true;
        } else if (!location.getWorld().getEnvironment().equals(Environment.NORMAL)) {
            // If theyre not in the overworld, they cant build
            return false;
        } else if (landIsClaimed(location)) {
            if(isOwner(location,player)) {
                return true;
            } else if(landPermissionCode(location).equals("p")) {
                return true;
            } else if(landPermissionCode(location).equals("c")) {
                String owner_uuid=REDIS.get("chunk" + location.getChunk().getX() + "," + location.getChunk().getZ() + "owner");
                System.out.println(owner_uuid);
                String owner_clan=REDIS.get("clan:"+owner_uuid);
                System.out.println(owner_clan);
                String player_clan=REDIS.get("clan:"+player.getUniqueId().toString());
                System.out.println(player_clan);
                if(owner_clan.equals(player_clan)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    public String landPermissionCode(Location location) {
        // permission codes:
        // p = public
        // c = clan
        // n = no permissions (private)
        if(REDIS.exists("chunk"+location.getChunk().getX()+","+location.getChunk().getZ()+"permissions")) {
            return REDIS.get("chunk"+location.getChunk().getX()+","+location.getChunk().getZ()+"permissions");
        } else {
            return "n";
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
        if(REDIS.sismember("moderators",player.getUniqueId().toString())) {
            return true;
        } else if(ADMIN_UUID!=null && player.getUniqueId().toString().equals(ADMIN_UUID.toString())) {
            return true;
        }
        return false;

    }
    public void updatePlayerTeam(Player player) {

    }

    public void sendWalletInfo(User user) throws ParseException, org.json.simple.parser.ParseException, IOException {
        // int chainHeight = user.wallet.getBlockchainHeight();
        user.wallet.updateBalance();
        // BitQuest.REDIS.del("balance:"+user.player.getUniqueId().toString());
        if(this.BLOCKCHAIN.equals("btc/main")) {
            user.player.sendMessage(ChatColor.BOLD+""+ChatColor.GREEN + "Your Bitcoin Address: "+ChatColor.WHITE+user.wallet.address);
        } else {
            user.player.sendMessage(ChatColor.BOLD+""+ChatColor.GREEN + "Your Testnet Address: "+ChatColor.WHITE+user.wallet.address);
        }
//        user.player.sendMessage(ChatColor.GREEN + "Confirmed Balance: " +ChatColor.WHITE+ user.wallet.balance/100 + " Bits");
//        user.player.sendMessage(ChatColor.GREEN + "Unconfirmed Balance: " +ChatColor.WHITE+user.wallet.unconfirmedBalance/100 + " Bits");
        user.player.sendMessage(ChatColor.GREEN + "Final Balance: "+ChatColor.WHITE + BitQuest.REDIS.get("final_balance:"+user.wallet.address) + " Bits");
        // user.player.sendMessage(ChatColor.YELLOW + "On-Chain Wallet Info:");
        //  user.player.sendMessage(ChatColor.YELLOW + " "); // spacing to let these URLs breathe a little
        user.player.sendMessage(ChatColor.BLUE+""+ChatColor.UNDERLINE + "blockchain.info/address/" + user.wallet.address);
        //    user.player.sendMessage(ChatColor.YELLOW + " ");
        //      user.player.sendMessage(ChatColor.BLUE+""+ChatColor.UNDERLINE + "live.blockcypher.com/btc/address/" + user.wallet.address);
        //      user.player.sendMessage(ChatColor.YELLOW + " ");
//        user.player.sendMessage(ChatColor.YELLOW+"Blockchain Height: " + Integer.toString(chainHeight));
        if(BITQUEST_ENV.equalsIgnoreCase("development")) {
            user.player.sendMessage(ChatColor.GREEN + "Payment Balance: " +ChatColor.WHITE+ user.wallet.payment_balance()/100 + " Bits");
            if(REDIS.exists("address"+user.player.getUniqueId().toString())) {
                user.player.sendMessage(ChatColor.GREEN + "Old wallet: " +ChatColor.WHITE+ REDIS.get("address"+user.player.getUniqueId().toString()));
                user.player.sendMessage(ChatColor.BLUE+""+ChatColor.UNDERLINE + "blockchain.info/address/" + REDIS.get("address"+user.player.getUniqueId().toString()));
            }
        }
    };
    public boolean landIsClaimed(Location location) {
        return REDIS.exists("chunk"+location.getChunk().getX()+","+location.getChunk().getZ()+"owner");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // we don't allow server commands (yet?)
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            // PLAYER COMMANDS
            if(cmd.getName().equalsIgnoreCase("land")) {
                if(args[0].equalsIgnoreCase("claim")) {
                    Location location=player.getLocation();
                    try {
                        claimLand(args[1],location.getChunk(),player);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED+"Land claim failed. Please try again later.");
                        return true;
                    } catch (org.json.simple.parser.ParseException e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED+"Land claim failed. Please try again later.");
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED+"Land claim failed. Please try again later.");
                        return true;
                    }
                    return true;
                } else if(args[0].equalsIgnoreCase("permission")) {
                    Location location=player.getLocation();
                    int x=location.getChunk().getX();
                    int z=location.getChunk().getZ();
                    if(isOwner(location,player)) {
                        String landname= REDIS.get("chunk"+x+","+z+"name");

                        if(args[1].equalsIgnoreCase("public")) {
                            REDIS.set("chunk"+location.getChunk().getX()+","+location.getChunk().getZ()+"permissions","p");
                            player.sendMessage(ChatColor.GREEN+"the land "+landname+" is now public");
                            return true;
                        } else if(args[1].equalsIgnoreCase("clan")) {
                            REDIS.set("chunk" + location.getChunk().getX() + "," + location.getChunk().getZ() + "permissions", "c");
                            player.sendMessage(ChatColor.GREEN + "the land " + landname + " is now clan-owned");
                            return true;
                        } else if(args[1].equalsIgnoreCase("private")) {
                            REDIS.del("chunk" + location.getChunk().getX() + "," + location.getChunk().getZ() + "permissions");
                            player.sendMessage(ChatColor.GREEN + "the land " + landname + " is now private");
                            return true;
                        } else {
                            return false;
                        }

                    } else {
                        player.sendMessage(ChatColor.RED+"Only the owner of this location can change its permissions.");
                        return true;
                    }
                }
            }
            if(cmd.getName().equalsIgnoreCase("clan")) {
                if (args.length > 0) {
                    if (args[0].equals("new")) {
                        if (args.length > 1) {
                            // check that desired clan name is alphanumeric
                            boolean hasNonAlpha = args[1].matches("^.*[^a-zA-Z0-9 ].*$");
                            if (!hasNonAlpha) {
                                // 16 characters max
                                if (args[1].length() <= 16) {

                                    if (REDIS.get("clan:" + player.getUniqueId().toString()) == null) {
                                        if (!REDIS.sismember("clans", args[1])) {
                                            REDIS.sadd("clans", args[1]);
                                            REDIS.set("clan:" + player.getUniqueId().toString(), args[1]);
                                            player.sendMessage(ChatColor.GREEN + "Congratulations! you are the founder of the " + args[1] + " clan");
                                            return true;
                                        } else {
                                            player.sendMessage(ChatColor.RED + "A clan with the name '" + args[1] + "' already exists.");
                                            return true;
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.RED + "You already belong to the clan " + REDIS.get("clan:" + player.getUniqueId().toString()));
                                        return true;
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "Error: clan name must have 16 characters max");
                                    return true;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Your clan name must only contain letters and numbers");
                                return true;
                            }

                        } else {
                            player.sendMessage(ChatColor.RED + "Usage: /clan new <your desired name>");
                            return true;
                        }

                    }
                    if (args[0].equals("invite")) {
                        // check that argument is not empty


                        if (args.length > 1) {
                            // TODO: you shouldn't be able to invite yourself
                            // check that player is in a clan
                            if (REDIS.exists("clan:" + player.getUniqueId().toString())) {
                                String clan = REDIS.get("clan:" + player.getUniqueId().toString());
                                // check if user is in the uuid database
                                if (REDIS.exists("uuid:" + args[1])) {
                                    // check if player already belongs to a clan
                                    String uuid = REDIS.get("uuid:" + args[1]);
                                    if (!REDIS.exists("clan:" + uuid)) {
                                        // check if player is already invited to the clan
                                        if (!REDIS.sismember("invitations:" + clan, uuid)) {
                                            REDIS.sadd("invitations:" + clan, uuid);
                                            player.sendMessage(ChatColor.GREEN + "You invited " + args[1] + " to the " + clan + " clan.");
                                            if (Bukkit.getPlayerExact(args[1]) != null) {
                                                Player invitedplayer = Bukkit.getPlayerExact(args[1]);
                                                invitedplayer.sendMessage(ChatColor.GREEN + player.getDisplayName() + " invited you to the " + clan + " clan");
                                            }
                                            return true;
                                        } else {
                                            player.sendMessage(ChatColor.RED + "Player " + args[1] + " is already invited to the clan and must accept the invitation");
                                            return true;
                                        }

                                    } else {
                                        if (REDIS.get("clan:" + uuid).equals(clan)) {
                                            player.sendMessage(ChatColor.RED + "Player " + args[1] + " already belongs to the clan " + clan);

                                        } else {
                                            player.sendMessage(ChatColor.RED + "Player " + args[1] + " already belongs to a clan.");

                                        }
                                        return true;
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "User " + args[1] + " does not play on this server");
                                    return true;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You don't belong to a clan");
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Usage: /clan invite <player nickname>");
                            return true;
                        }
                    }
                    if (args[0].equals("join")) {
                        // check that argument is not empty
                        if (args.length > 1) {
                            // check that player is invited to the clan he wants to join
                            if (REDIS.sismember("invitations:" + args[1], player.getUniqueId().toString())) {
                                // user is invited to join
                                if (REDIS.get("clan:" + player.getUniqueId().toString()) == null) {
                                    // user is not part of any clan
                                    REDIS.srem("invitations:"+ args[1], player.getUniqueId().toString());
                                    REDIS.set("clan:" + player.getUniqueId().toString(), args[1]);
                                    player.sendMessage(ChatColor.GREEN + "You are now part of the " + REDIS.get("clan:" + player.getUniqueId().toString()) + " clan!");
                                    return true;
                                } else {
                                    player.sendMessage(ChatColor.RED + "You already belong to the clan " + REDIS.get("clan:" + player.getUniqueId().toString()));
                                    return true;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You are not invited to join the " + args[1] + " clan.");
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Usage: /clan join <clan name>");
                            return true;
                        }
                    }
                    if (args[0].equals("kick")) {
                        if (args.length > 1) {
                            // check if player is in the uuid database

                            if (REDIS.exists("uuid:" + args[1])) {
                                String uuid = REDIS.get("uuid:" + args[1]);
                                // check if player belongs to a clan
                                if (REDIS.exists("clan:" + player.getUniqueId().toString())) {
                                    String clan = REDIS.get("clan:" + player.getUniqueId().toString());
                                    // check that kicker and player are in the same clan
                                    if (REDIS.get("clan:" + uuid).equals(clan)) {
                                        REDIS.del("clan:" + uuid);
                                        player.sendMessage(ChatColor.GREEN + "Player " + args[1] + " was kicked from the " + clan + " clan.");
                                        return true;
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Player " + args[1] + " is not a member of the clan " + clan);
                                        return true;
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "You don't belong to any clan.");
                                    return true;
                                }

                            } else {
                                player.sendMessage(ChatColor.RED + "Player " + args[1] + " does not play on this server.");
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Usage: /clan kick <player nickname>");
                            return true;
                        }
                    }
                    if (args[0].equals("leave")) {
                        if (REDIS.exists("clan:" + player.getUniqueId().toString())) {
                            // TODO: when a clan gets emptied, should be removed from the "clans" set
                            player.sendMessage(ChatColor.GREEN + "You are no longer part of the " + REDIS.get("clan:" + player.getUniqueId().toString()) + " clan");
                            REDIS.del("clan:" + player.getUniqueId().toString());
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "You don't belong to a clan.");
                            return true;
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /clan <new|invite|kick|join|leave>");
                    return true;
                }
                return false;
            }
            if(cmd.getName().equalsIgnoreCase("wallet")) {
                try {
                    User user=new User(player);
                    System.out.println("[/wallet] final balance: "+user.wallet.final_balance());
                    sendWalletInfo(user);
                    updateScoreboard(player);
                } catch (ParseException e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED+"There was a problem reading your wallet.");
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED+"There was a problem reading your wallet.");

                } catch (IOException e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED+"There was a problem reading your wallet.");

                }

                return true;
            }
            if(cmd.getName().equalsIgnoreCase("transfer")) {
                if(args.length == 2) {
                    final int sendAmount = Integer.valueOf(args[0])*100;
                    Wallet fromWallet = null;
                    try {
                        fromWallet = new User(player).wallet;
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    } catch (org.json.simple.parser.ParseException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    try {
                        if(fromWallet != null && fromWallet.balance() >= sendAmount && sendAmount >= MIN_TRANS) {
                            player.sendMessage(ChatColor.YELLOW+"Sending " + args[0] + " Bits to "+args[1]+"...");
                            for(final OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                                System.out.println(offlinePlayer);
                                if(offlinePlayer.getName()!=null && args[1]!=null && offlinePlayer.getName().equals(args[1])) {
                                    final Wallet finalFromWallet = fromWallet;
                                    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                                    final Wallet toWallet = new User(offlinePlayer.getPlayer()).wallet;

                                    try {

                                        if (finalFromWallet.transaction(sendAmount, toWallet)) {
                                            player.sendMessage(ChatColor.GREEN + "Succesfully sent " + sendAmount / 100 + " Bits to " + offlinePlayer.getName() + ".");
                                            if (offlinePlayer.isOnline()) {
                                                offlinePlayer.getPlayer().sendMessage(ChatColor.GREEN + "" + player.getName() + " just sent you " + sendAmount / 100 + " Bits!");
                                            }
                                        } else {
                                            player.sendMessage(ChatColor.RED + "Transaction failed. Please try again in a few moments.");
                                        }

                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }


                                    updateScoreboard(player);
                                    return true;
                                }
                            }
                            // validate e-mail address
                            String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
                            java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
                            java.util.regex.Matcher m = p.matcher(args[0]);
                            if(m.matches()) {
                                // TODO: send money through xapo

                            } else {
                                try {

                                    Wallet toWallet = new Wallet(args[1]);

                                    if(fromWallet.create_blockcypher_transaction(sendAmount,toWallet.address)) {
                                        player.sendMessage(ChatColor.GREEN+"Succesfully sent "+args[0]+" Bits to external address.");
                                        updateScoreboard(player);
                                    } else {
                                        player.sendMessage(ChatColor.RED+"Transaction failed. Please try again in a few moments.");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (org.json.simple.parser.ParseException e) {
                                    e.printStackTrace();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                            return true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (org.json.simple.parser.ParseException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
            if (cmd.getName().equalsIgnoreCase("report")) {
                if (slackBotSession != null && slackBotSession.isConnected()) {
                    if (args.length >= 2) {
                        String badPlayer = args[0];
                        String message = args[1];
                        for (int i = 2; i < args.length; i++) {
                            message += " ";
                            message += args[i];
                        }

                        if (REDIS.exists("uuid:" + badPlayer)) {
                            String uuid = REDIS.get("uuid:" + badPlayer);
                            String slackMessage = "Player " + player.getName() + " reports " + badPlayer + " (" + uuid + ") because: " + message;
                            SlackChannel channel = slackBotSession.findChannelByName(SLACK_BOT_REPORTS_CHANNEL);
                            if (channel != null) {
                                slackBotSession.sendMessage(channel, slackMessage);
                                String playerMessage = ChatColor.GREEN + "The report has been send to a moderator. Thanks for making " +
                                        ChatColor.GOLD + ChatColor.BOLD +"Bit" + ChatColor.GRAY + ChatColor.BOLD + "Quest" +
                                        ChatColor.RESET + ChatColor.GREEN + " a better place.";
                                player.sendMessage(playerMessage);
                                return true;
                            } else {
                                player.sendMessage(ChatColor.RED + "There was a problem sending the report. Please try again later.");
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Player " + badPlayer + " does not play on this server.");
                            return true;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Usage: /report <player> <reason>");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "The /report command is not active.");
                    return true;
                }
            }

            /***********************************************************
                /upgradewallet
                attempts to transfer funds from old (BQ2.0) wallet to
                the new HD (BQ2.1) wallet via BlockCypher's
                microtransaction endpoint
             ***********************************************************/
            if(cmd.getName().equalsIgnoreCase("upgradewallet")) {
                String fail_message="Cannot make transaction at this moment. Please try again later...";
                player.sendMessage(ChatColor.YELLOW+"Searching for lost wallet...");
                if(REDIS.exists("address"+player.getUniqueId().toString())&&REDIS.exists("private"+player.getUniqueId().toString())) {
                    Wallet old_wallet=new Wallet(
                            REDIS.get("address"+player.getUniqueId().toString()),
                            REDIS.get("private"+player.getUniqueId().toString()));
                    player.sendMessage(ChatColor.YELLOW+"Found wallet "+old_wallet.address+"! looking for bits...");
                    try {
                        JSONObject balance=old_wallet.get_blockcypher_balance();
                        int confirmed_balance=((Number)balance.get("balance")).intValue();
                        player.sendMessage(ChatColor.YELLOW+"Confirmed balance in lost wallet is "+confirmed_balance+" sat");

                        if(confirmed_balance>100) {
                            int transaction_balance=Math.min(4000000,confirmed_balance);
                            try {
                                User user=new User(player);
                                player.sendMessage(ChatColor.YELLOW+"Sending "+transaction_balance/100+" bits to "+user.wallet.address);

                                if(old_wallet.blockcypher_microtransaction(transaction_balance,user.wallet.address)==true) {
                                    player.sendMessage(ChatColor.GREEN+"Transaction successful.");

                                    return true;
                                } else {
                                    player.sendMessage(ChatColor.RED+fail_message);

                                    return true;
                                }
                            } catch (ParseException e) {
                                player.sendMessage(ChatColor.RED+"Error loading new wallet.");
                                e.printStackTrace();
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED+"Not enough balance for recovery. If you think this is an error e-mail bitquest@bitquest.co");

                            return true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED+fail_message);
                        return true;
                    } catch (org.json.simple.parser.ParseException e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED+fail_message);
                        return true;
                    }


                } else {
                    player.sendRawMessage(ChatColor.RED+"Old wallet not found. If you think this is an error please contact bitquest@bitquest.co");
                }
            }

                // MODERATOR COMMANDS
            if (isModerator(player)) {
                // COMMAND: MOD
                if (cmd.getName().equalsIgnoreCase("butcher")) {
                    Chunk c=player.getLocation().getChunk();
                    for(World w:Bukkit.getWorlds()) {
                        List<Entity> entities = w.getEntities();
                        int killed=0;
                        for ( Entity entity : entities){
                            if(entity instanceof Player) {

                            } else if (entity.getLocation().getChunk().getX()==c.getX()&&entity.getLocation().getChunk().getZ()==c.getZ()) {
                                killed=killed+1;
                                entity.remove();
                                System.out.println("[butcher] removed "+entity.getName());
                            }
                        }
                        player.sendMessage(ChatColor.GREEN+"Killed "+killed+" entities");

                    }

                }
                if (cmd.getName().equalsIgnoreCase("killAllVillagers")) {
                    killAllVillagers();
                }
                if(cmd.getName().equalsIgnoreCase("crashtest")) {
                    this.setEnabled(false);
                }
                if (cmd.getName().equalsIgnoreCase("mod")) {
                    if(args[0].equals("add")) {
                        // Sub-command: /mod add

                        if(REDIS.exists("uuid:"+args[1])) {
                            UUID uuid=UUID.fromString(REDIS.get("uuid:"+args[1]));
                            REDIS.sadd("moderators",uuid.toString());
                            sender.sendMessage(ChatColor.GREEN+REDIS.get("name:"+uuid)+" added to moderators group");

                            return true;
                        } else {
                            sender.sendMessage(ChatColor.RED+"Cannot find player "+args[1]);
                            return true;
                        }
                    } else if(args[0].equals("remove")) {
                        // Sub-command: /mod del
                        if(REDIS.exists("uuid:"+args[1])) {
                            UUID uuid=UUID.fromString(REDIS.get("uuid:"+args[1]));
                            REDIS.srem("moderators",uuid.toString());
                            return true;
                        }
                        return false;
                    } else if(args[0].equals("list")) {
                        // Sub-command: /mod list
                        Set<String> moderators=REDIS.smembers("moderators");
                        for(String uuid:moderators) {
                            sender.sendMessage(ChatColor.YELLOW+REDIS.get("name:"+uuid));
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
                if (cmd.getName().equalsIgnoreCase("ban") && args.length==1) {
                    if(REDIS.get("uuid"+args[0])!=null) {
                        UUID uuid=UUID.fromString(REDIS.get("uuid"+args[0]));
                        REDIS.sadd("banlist",uuid.toString());
                        Player kickedout=Bukkit.getPlayer(args[0]);
                        if(kickedout!=null) {
                            kickedout.kickPlayer("Sorry.");
                        }
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED+"Can't find player "+args[0]);
                        return true;
                    }

                }
                if (cmd.getName().equalsIgnoreCase("unban") && args.length==1) {
                    if(REDIS.get("uuid"+args[0])!=null) {
                        UUID uuid=UUID.fromString(REDIS.get("uuid"+args[0]));
                        REDIS.srem("banlist",uuid.toString());

                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED+"Can't find player "+args[0]);
                        return true;
                    }

                }
                if (cmd.getName().equalsIgnoreCase("banlist")) {
                    Set<String> banlist=REDIS.smembers("banlist");
                    for(String uuid:banlist) {
                        sender.sendMessage(ChatColor.YELLOW+REDIS.get("name"+uuid));
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
                if (cmd.getName().equalsIgnoreCase("emergencystop")) {
                    StringBuilder message = new StringBuilder();
                    message.append(sender.getName())
                            .append(" has shut down the server for emergency reasons");

                    if (args.length > 0) {
                        message.append(": ");
                        for (String word : args) {
                            message.append(word).append(" ");
                        }
                    }
                    for (Player currentPlayer : Bukkit.getOnlinePlayers()) {
                        currentPlayer.kickPlayer(message.toString());
                    }

                    Bukkit.shutdown();
                    return true;
                }

            } else {
                sender.sendMessage("You don't have enough permissions to execute this command!");
            }
            if (cmd.getName().equalsIgnoreCase("faucet")) {
                User user= null;
                try {
                    user = new User(player);
                    if(user.wallet.getTestnetCoins()) {
                        player.sendMessage(ChatColor.GREEN+"Some testnet coins were delivered to your wallet.");
                    } else {
                        player.sendMessage(ChatColor.RED+"There was an error getting testnet coins.");
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}

