package com.bitquest.bitquest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

import com.bitquest.bitquest.commands.*;
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

import org.json.simple.JSONArray;
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
    public final static String HD_ROOT_ADDRESS = System.getenv("HD_ROOT_ADDRESS") != null ? System.getenv("HD_ROOT_ADDRESS") : null;
    public final static String WORLD_ADDRESS = System.getenv("WORLD_ADDRESS") != null ? System.getenv("WORLD_ADDRESS") : "n3hptFs8MBUa39gVjPnP5H1xQEt1ezbHCE";
    public final static String WORLD_PRIVATE_KEY = System.getenv("WORLD_PRIVATE_KEY") != null ? System.getenv("WORLD_PRIVATE_KEY") : "76e8a7eb479256c68f59f66c7b744891bc2f632ff3c7a3f69a5c4aeccda687e3";
    public final static String WORLD_PUBLIC_KEY = System.getenv("WORLD_PUBLIC_KEY") != null ? System.getenv("WORLD_PUBLIC_KEY") : "76e8a7eb479256c68f59f66c7b744891bc2f632ff3c7a3f69a5c4aeccda687e3";
    public final static String BITCOIN_NODE_HOST = System.getenv("BITCOIN_NODE_HOST") != null ? System.getenv("BITCOIN_NODE_HOST") : "localhost";
    public final static int BITCOIN_NODE_PORT = System.getenv("BITCOIN_NODE_PORT") != null ? Integer.parseInt(System.getenv("BITCOIN_NODE_PORT")) : 18332;
    public final static String BITCOIN_NODE_USERNAME = System.getenv("BITCOIN_NODE_USERNAME");
    public final static String BITCOIN_NODE_PASSWORD = System.getenv("BITCOIN_NODE_PASSWORD");

    public final static String BLOCKCYPHER_API_KEY = System.getenv("BLOCKCYPHER_API_KEY") != null ? System.getenv("BLOCKCYPHER_API_KEY") : null;
    public final static String XAPO_API_KEY = System.getenv("XAPO_API_KEY") != null ? System.getenv("XAPO_API_KEY") : null;
    public final static String XAPO_SECRET = System.getenv("XAPO_SECRET") != null ? System.getenv("XAPO_SECRET") : null;
    public final static int MAX_STOCK=100;

    public final static String LAND_ADDRESS = System.getenv("LAND_ADDRESS") != null ? System.getenv("LAND_ADDRESS") : null;

    public final static String MINER_FEE_ADDRESS = System.getenv("MINER_FEE_ADDRESS") != null ? System.getenv("MINER_FEE_ADDRESS") : null;

    // if BLOCKCHAIN is set, users can choose a blockchain supported by BlockCypher (very useful for development on testnet, or maybe DogeQuest?)
    public final static String BLOCKCHAIN = System.getenv("BLOCKCHAIN") != null ? System.getenv("BLOCKCHAIN") : "btc/test3";

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
    public final static int LAND_PRICE = System.getenv("LAND_PRICE") != null ? Integer.parseInt(System.getenv("LAND_PRICE")) : 10000;

    public final static int MIN_TRANS=200000;
    // utilities: distance and rand
    public static int distance(Location location1, Location location2) {
        return (int) location1.distance(location2);
    }

    public static int rand(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }
    public StatsDClient statsd;
    public Wallet wallet=null;

    private Map<String, CommandAction> commands;
    private Map<String, CommandAction> modCommands;

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
        wallet=new Wallet("bitquest_market");
        try {
            getBlockChainInfo();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
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

        commands = new HashMap<String, CommandAction>();
        commands.put("wallet", new WalletCommand(this));
        commands.put("land", new LandCommand(this));
        commands.put("clan", new ClanCommand());
        commands.put("transfer", new TransferCommand(this));
        commands.put("report", new ReportCommand(this));
        commands.put("send", new SendCommand(this));
        commands.put("upgradeWallet", new UpgradeWallet());

        modCommands = new HashMap<String, CommandAction>();
        modCommands.put("butcher", new ButcherCommand());
        modCommands.put("killAllVillagers", new KillAllVillagersCommand(this));
        modCommands.put("crashTest", new CrashtestCommand(this));
        modCommands.put("mod", new ModCommand());
        modCommands.put("faucet", new FaucetCommand());
        modCommands.put("ban", new BanCommand());
        modCommands.put("unban", new UnbanCommand());
        modCommands.put("banlist", new BanlistCommand());
        modCommands.put("spectate", new SpectateCommand(this));
        modCommands.put("emergencystop", new EmergencystopCommand());
    }
    // @todo: make this just accept the endpoint name and (optional) parameters
    public JSONObject getBlockChainInfo() throws org.json.simple.parser.ParseException {
        JSONParser parser = new JSONParser();

        try {
            final JSONObject jsonObject=new JSONObject();
            jsonObject.put("jsonrpc","1.0");
            jsonObject.put("id","bitquest");
            jsonObject.put("method","getblockchaininfo");
            JSONArray params=new JSONArray();
            jsonObject.put("params",params);
            System.out.println("Checking blockchain info...");
            URL url = new URL("http://"+BITCOIN_NODE_HOST+":"+BITCOIN_NODE_PORT);
            System.out.println(url.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            String userPassword = BITCOIN_NODE_USERNAME + ":" + BITCOIN_NODE_PASSWORD;
            String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
            con.setRequestProperty("Authorization", "Basic " + encoding);

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write(jsonObject.toString());
            out.close();

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
            return (JSONObject) parser.parse(response.toString());
        } catch (IOException e) {
            System.out.println("problem connecting with bitcoin node");
            System.out.println(e);
            // Unable to call API?
        }

        return new JSONObject(); // just give them an empty object
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
        int final_balance=0;
        if(REDIS.exists("final_balance:"+player.getUniqueId())) {
            final_balance=Integer.parseInt(REDIS.get("final_balance:"+player.getUniqueId()));
        }
        if(statsd!=null) {
            statsd.gauge(BITQUEST_ENV+".balance."+user.player.getName(),final_balance);

        }

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
            statsd.gauge(BITQUEST_ENV+".wallet_balance",wallet.getBalance());
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
            // check that desired area name doesn't have non-alphanumeric characters
            boolean hasNonAlpha = name.matches("^.*[^a-zA-Z0-9 _].*$");
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

                            if (user.wallet.payment(BitQuest.LAND_PRICE, BitQuest.LAND_ADDRESS)) {

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
                                int balance = user.wallet.getBalance();
                                if (balance < BitQuest.LAND_PRICE) {
                                    player.sendMessage(ChatColor.RED + "You don't have enough money! You need " +
                                            ChatColor.BOLD + (int) Math.ceil((BitQuest.LAND_PRICE - balance) / 100) + ChatColor.RED + " more Bits.");
                                } else {
                                    player.sendMessage(ChatColor.RED + "Claim payment failed. Please try again later.");
                                }
                            }
                        } catch (IOException e) {
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

                            if (REDIS.exists("uuid:" + newOwner)) {
                                String newOwnerUUID = REDIS.get("uuid:" + newOwner);
                                BitQuest.REDIS.set("chunk" + x + "," + z + "owner", newOwnerUUID);
                                player.sendMessage(ChatColor.GREEN + "This land now belongs to " + newOwner);
                            } else {
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
    public String chain_name() {
        if(this.BLOCKCHAIN.equals("btc/main")) {
            return "Bitcoin";
        } else if(this.BLOCKCHAIN.equals("doge/main")) {
            return "Dogecoin";
        } else if(this.BLOCKCHAIN.equals("btc/test3")) {
            return "Testnet";
        } else {
            return "Blockchain";
        }
    }


    public void sendWalletInfo(User user) throws ParseException, org.json.simple.parser.ParseException, IOException {
        // int chainHeight = user.wallet.getBlockchainHeight();
        // BitQuest.REDIS.del("balance:"+user.player.getUniqueId().toString());
        user.player.sendMessage(ChatColor.BOLD+""+ChatColor.GREEN + "Your "+chain_name()+" Address: "+ChatColor.WHITE+user.wallet.getAccountAddress());

//        user.player.sendMessage(ChatColor.GREEN + "Confirmed Balance: " +ChatColor.WHITE+ user.wallet.balance/100 + " Bits");
//        user.player.sendMessage(ChatColor.GREEN + "Unconfirmed Balance: " +ChatColor.WHITE+user.wallet.unconfirmedBalance/100 + " Bits");
        user.player.sendMessage(ChatColor.GREEN + "Final Balance: "+ChatColor.WHITE + BitQuest.REDIS.get("final_balance:"+user.wallet.getAccountAddress()) + " Satoshi");
        // user.player.sendMessage(ChatColor.YELLOW + "On-Chain Wallet Info:");
        //  user.player.sendMessage(ChatColor.YELLOW + " "); // spacing to let these URLs breathe a little
        //    user.player.sendMessage(ChatColor.YELLOW + " ");

        if(user.wallet.url()!=null) {
            user.player.sendMessage(ChatColor.BLUE+""+ChatColor.UNDERLINE + user.wallet.url());
        }

        //      user.player.sendMessage(ChatColor.YELLOW + " ");
//        user.player.sendMessage(ChatColor.YELLOW+"Blockchain Height: " + Integer.toString(chainHeight));
        if(BITQUEST_ENV.equalsIgnoreCase("development")) {
            user.player.sendMessage(ChatColor.GREEN + "Payment Balance: " +ChatColor.WHITE+ user.wallet.payment_balance()/100 + " Bits");
            if(REDIS.exists("address"+user.player.getUniqueId().toString())) {
                user.player.sendMessage(ChatColor.GREEN + "Old wallet: " +ChatColor.WHITE+ REDIS.get("address"+user.player.getUniqueId().toString()));
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
            for(Map.Entry<String, CommandAction> entry : commands.entrySet()) {
                if (cmd.getName().equalsIgnoreCase(entry.getKey())) {
                    entry.getValue().run(sender, cmd, label, args, player);
                }
            }

            // MODERATOR COMMANDS
            for(Map.Entry<String, CommandAction> entry : modCommands.entrySet()) {
                if (cmd.getName().equalsIgnoreCase(entry.getKey())) {
                    if (isModerator(player)) {
                        entry.getValue().run(sender, cmd, label, args, player);
                    } else {
                        sender.sendMessage("You don't have enough permissions to execute this command!");
                    }
                }
            }
        }
        return true;
    }

    public void crashtest() {
        this.setEnabled(false);
    }
}

