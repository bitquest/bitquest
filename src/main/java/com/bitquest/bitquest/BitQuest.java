package com.bitquest.bitquest;

import com.bitquest.bitquest.commands.BanCommand;
import com.bitquest.bitquest.commands.BanlistCommand;
import com.bitquest.bitquest.commands.ButcherCommand;
import com.bitquest.bitquest.commands.ClanCommand;
import com.bitquest.bitquest.commands.CommandAction;
import com.bitquest.bitquest.commands.CrashtestCommand;
import com.bitquest.bitquest.commands.CurrencyCommand;
import com.bitquest.bitquest.commands.DonateCommand;
import com.bitquest.bitquest.commands.EmergencystopCommand;
import com.bitquest.bitquest.commands.FixAbandonLand;
import com.bitquest.bitquest.commands.HomeCommand;
import com.bitquest.bitquest.commands.KillAllVillagersCommand;
import com.bitquest.bitquest.commands.LandCommand;
import com.bitquest.bitquest.commands.MessageOfTheDayCommand;
import com.bitquest.bitquest.commands.ModCommand;
import com.bitquest.bitquest.commands.PetCommand;
import com.bitquest.bitquest.commands.ProfessionCommand;
import com.bitquest.bitquest.commands.ReportCommand;
import com.bitquest.bitquest.commands.SendCommand;
import com.bitquest.bitquest.commands.SpawnCommand;
import com.bitquest.bitquest.commands.SpectateCommand;
import com.bitquest.bitquest.commands.TradeCommand;
import com.bitquest.bitquest.commands.TransferCommand;
import com.bitquest.bitquest.commands.UnbanCommand;
import com.bitquest.bitquest.commands.WalletCommand;
import com.bitquest.bitquest.events.BlockEvents;
import com.bitquest.bitquest.events.ChatEvents;
import com.bitquest.bitquest.events.EntityEvents;
import com.bitquest.bitquest.events.InventoryEvents;
import com.bitquest.bitquest.events.ServerEvents;
import com.bitquest.bitquest.events.SignEvents;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wither;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import redis.clients.jedis.Jedis;

// Color Table :
// GREEN : Worked, YELLOW : Processing, LIGHT_PURPLE : Any money balance, BLUE : Player name,
// DARK_BLUE UNDERLINE : Link, RED : Server error, DARK_RED : User error, GRAY : Info, DARK_GRAY :
// Clan, DARK_GREEN : Landname

public class BitQuest extends JavaPlugin {
  // TODO: remove env variables not being used anymore
  // Connecting to REDIS
  // Links to the administration account via Environment Variables
  public static final String BITQUEST_ENV = System.getenv("BITQUEST_ENV") != null ? System.getenv("BITQUEST_ENV")
      : "development";
  public static final UUID ADMIN_UUID = System.getenv("ADMIN_UUID") != null
      ? UUID.fromString(System.getenv("ADMIN_UUID"))
      : null;

  public static final String NODE_HOST = System.getenv("BITQUEST_NODE_HOST") != null
      ? System.getenv("BITQUEST_NODE_HOST")
      : "localhost";
  // NODE_PORT: Defaults to Dogecoin Testnet port
  public static final int NODE_PORT = System.getenv("BITQUEST_NODE_PORT") != null
      ? Integer.parseInt(System.getenv("BITQUEST_NODE_PORT"))
      : 44555;
  public static final String SERVER_NAME = System.getenv("BITQUEST_NAME") != null ? System.getenv("BITQUEST_NAME")
      : "BitQuest";
  public static final Long DENOMINATION_FACTOR = System.getenv("DENOMINATION_FACTOR") != null
      ? Long.parseLong(System.getenv("DENOMINATION_FACTOR"))
      : 100L;
  public static final String DENOMINATION_NAME = System.getenv("DENOMINATION_NAME") != null
      ? System.getenv("DENOMINATION_NAME")
      : "DOGE";
  public static final String BLOCKCYPHER_CHAIN = System.getenv("BLOCKCYPHER_CHAIN") != null
      ? System.getenv("BLOCKCYPHER_CHAIN")
      : "btc/test3";
  public static final String NODE_RPC_USERNAME = System.getenv("BITQUEST_NODE_RPC_USER");
  public static final String NODE_RPC_PASSWORD = System.getenv("BITQUEST_NODE_RPC_PASSWORD");
  public static final String DISCORD_HOOK_URL = System.getenv("DISCORD_HOOK_URL");
  public static final String BLOCKCYPHER_API_KEY = System.getenv("BLOCKCYPHER_API_KEY") != null
      ? System.getenv("BLOCKCYPHER_API_KEY")
      : null;
  public static final Long MINER_FEE = System.getenv("MINER_FEE") != null ? Long.parseLong(System.getenv("MINER_FEE"))
      : 10000;

  public static final int MAX_STOCK = 100;

  // REDIS: Look for Environment variables on hostname and port, otherwise
  // defaults to
  // localhost:6379
  public static final String REDIS_HOST = System.getenv("BITQUEST_REDIS_HOST") != null
      ? System.getenv("BITQUEST_REDIS_HOST")
      : "redis";
  public static final Integer REDIS_PORT = System.getenv("BITQUEST_REDIS_PORT") != null
      ? Integer.parseInt(System.getenv("BITQUEST_REDIS_PORT"))
      : 6379;

  // Default price: 10,000 satoshis or 100 bits
  public static final Double LAND_PRICE = System.getenv("LAND_PRICE") != null
      ? Double.parseDouble(System.getenv("LAND_PRICE"))
      : 10.0;

  // Minimum transaction by default is 2000 bits
  public static final Long MINIMUM_TRANSACTION = System.getenv("MINIMUM_TRANSACTION") != null
      ? Long.parseLong(System.getenv("MINIMUM_TRANSACTION"))
      : 2000L;

  public static final String SENTRY_DSN = System.getenv("SENTRY_DSN");

  public static int rand(int min, int max) {
    return min + (int) (Math.random() * ((max - min) + 1));
  }

  public Wallet wallet = null;
  public Player lastLootPlayer;
  public boolean spookyMode = false;
  // caches is used to reduce the amounts of calls to redis, storing some chunk
  // information in
  // memory
  public HashMap<String, Boolean> landUnclaimedCache = new HashMap<String, Boolean>();
  public HashMap<String, String> landOwnerCache = new HashMap<String, String>();
  public HashMap<String, String> landPermissionCache = new HashMap<String, String>();
  public HashMap<String, String> landNameCache = new HashMap<String, String>();
  public Long walletBalanceCache = 0L;
  public ArrayList<ItemStack> books = new ArrayList<ItemStack>();
  // when true, server is closed for maintenance and not allowing players to join
  // in.
  public boolean maintenanceMode = false;
  boolean bossAlreadySpawned = false;
  private Map<String, CommandAction> commands;
  private Map<String, CommandAction> modCommands;
  public static long PET_PRICE = 100 * DENOMINATION_FACTOR;
  public Jedis redis;
  public Node node;
  public LandOwnership land;

  @Override
  public void onEnable() {
    log("startup", "BitQuest starting");
    redis = new Jedis(REDIS_HOST, REDIS_PORT);
    this.node = new Node();
    this.node.host = BitQuest.NODE_HOST;
    this.node.port = BitQuest.NODE_PORT;
    this.node.rpcUsername = BitQuest.NODE_RPC_USERNAME;
    this.node.rpcPassword = BitQuest.NODE_RPC_PASSWORD;
    this.land = new LandOwnership(redis);
    try {

      if (ADMIN_UUID == null) {
        log("warning", "ADMIN_UUID env variable is not set.");
      }

      // registers listener classes
      getServer().getPluginManager().registerEvents(new ChatEvents(this), this);
      getServer().getPluginManager().registerEvents(new BlockEvents(this), this);
      getServer().getPluginManager().registerEvents(new EntityEvents(this), this);
      getServer().getPluginManager().registerEvents(new InventoryEvents(this), this);
      getServer().getPluginManager().registerEvents(new SignEvents(this), this);
      getServer().getPluginManager().registerEvents(new ServerEvents(this), this);

      // player does not lose inventory on death
      System.out.println("[startup] sending command gamerule keepInventory on");

      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule keepInventory on");

      // loads config file. If it doesn't exist, creates it.
      getDataFolder().mkdir();
      System.out.println("[startup] checking default config file");

      if (!new java.io.File(getDataFolder(), "config.yml").exists()) {
        saveDefaultConfig();
        System.out.println("[startup] config file does not exist. creating default.");
      }
      log("startup", "Redis host is: " + REDIS_HOST);
      log("startup", "Redis port is: " + REDIS_PORT);

      try {
        JSONObject blockchainInfo = node.getBlockchainInfo();
        Double verificationProgress = (Double) blockchainInfo.get("verificationprogress");
        log("node", "verificartion progesss: " + verificationProgress);
      } catch (Exception e) {
        log("fatal", e.getMessage());
      }

      this.wallet = new Wallet(node, "loot");
      BitQuest.log("loot", wallet.address());
      // creates scheduled timers (update balances, etc)
      createScheduledTimers();
      commands = new HashMap<String, CommandAction>();
      commands.put("wallet", new WalletCommand(this));
      commands.put("land", new LandCommand(this));
      commands.put("home", new HomeCommand(this));
      commands.put("clan", new ClanCommand(this));
      commands.put("transfer", new TransferCommand(this));
      commands.put("trade", new TradeCommand(this));
      commands.put("report", new ReportCommand(this));
      commands.put("send", new SendCommand(this));
      commands.put("currency", new CurrencyCommand(this));
      commands.put("donate", new DonateCommand(this));
      commands.put("profession", new ProfessionCommand(this));
      commands.put("spawn", new SpawnCommand(this));
      commands.put("pet", new PetCommand(this));
      modCommands = new HashMap<String, CommandAction>();
      modCommands.put("butcher", new ButcherCommand());
      modCommands.put("killAllVillagers", new KillAllVillagersCommand(this));
      modCommands.put("crashTest", new CrashtestCommand(this));
      modCommands.put("mod", new ModCommand(this));
      modCommands.put("ban", new BanCommand(this));
      modCommands.put("unban", new UnbanCommand(this));
      modCommands.put("banlist", new BanlistCommand(this));
      modCommands.put("spectate", new SpectateCommand(this));
      modCommands.put("emergencystop", new EmergencystopCommand());
      modCommands.put("fixabandonland", new FixAbandonLand(this));
      modCommands.put("motd", new MessageOfTheDayCommand(this));
      // TODO: Re enable loot pool cache
      // updateLootPoolCache();
      redis.set("loot:rate:limit", "1");
      redis.expire("loot:rate:limit", 10);
      killAllVillagers();
      System.out.println("[startup] finished");

    } catch (Exception e) {
      e.printStackTrace();
      BitQuest.log("fatal", "Enabling plugin fails. Server is shutting down.");
      Bukkit.shutdown();
    }
  }

  public void updateLootPoolCache() throws Exception {
    System.out.println("[loot_cache]");
    bossAlreadySpawned = false;
    try {
      Double balance = wallet.balance(0);
      System.out.println("[loot_cache] " + balance);

      if (balance > (LAND_PRICE + MINER_FEE) * 2) {
        redis.set("loot_cache", balance.toString());
      }
    } catch (IOException e) {
      e.printStackTrace();
      redis.del("loot_cache");
      System.out.println("[loot_cache] FAIL");
    } catch (org.json.simple.parser.ParseException e) {
      e.printStackTrace();
      redis.del("loot_cache");
      System.out.println("[loot_cache] FAIL");

    }

  }

  static Double witherReward() {
    return (LAND_PRICE);
  }

  public void createBossFight(Location location) {
    try {
      if (redis.exists("loot_cache") && Double.parseDouble(redis.get("loot_cache")) > 400 * DENOMINATION_FACTOR) {
        List<Entity> entities = location.getWorld().getEntities();

        for (Entity en : entities) {
          if ((en instanceof Wither)) {
            bossAlreadySpawned = true;
          }
        }

        if (bossAlreadySpawned == false && location.getY() > 64) {
          System.out
              .println("[boss fight] spawn in " + location.getX() + "," + location.getY() + "," + location.getZ());
          bossAlreadySpawned = true;
          location.getWorld().spawnEntity(location, EntityType.WITHER);

          for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getName().equals(player.getWorld().getName())) {
              player.sendMessage("A boss has spawned! Distance: " + location.distance(player.getLocation()));
              sendDiscordMessage(
                  "A Wither has spawned in " + location.getX() + "," + location.getY() + "," + location.getZ()
                      + " rewards: " + Math.round(witherReward() / DENOMINATION_FACTOR) + " " + DENOMINATION_NAME);
            }
          }

        }
      }
    } catch (Exception e) {
      e.printStackTrace();

    }

  }

  public void announce(final String message) {
    for (Player player : Bukkit.getOnlinePlayers()) {
      player.sendMessage(message);
    }
  }

  public void updateScoreboard(final Player player) {
    try {
      final Wallet wallet = new Wallet(this.node, player.getUniqueId().toString());
      ScoreboardManager scoreboardManager;
      Scoreboard walletScoreboard;
      Objective balanceObjective;
      // Double confirmedBalance = wallet.balance(3);
      scoreboardManager = Bukkit.getScoreboardManager();
      walletScoreboard = scoreboardManager.getNewScoreboard();
      balanceObjective = walletScoreboard.registerNewObjective("wallet", "dummy");
      balanceObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
      balanceObjective.setDisplayName(ChatColor.GOLD + ChatColor.BOLD.toString() + BitQuest.SERVER_NAME);
      Score score = balanceObjective.getScore(ChatColor.GREEN + this.node.chain()); // Get a fake offline player
      Double unconfirmedBalance = wallet.balance(0);
      score.setScore((int) Math.round(unconfirmedBalance));
      player.setScoreboard(walletScoreboard);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void createPet(User user, String petName) {
    redis.sadd("pet:names", petName);
    redis.zincrby("player:tx", PET_PRICE, user.uuid.toString());
    long unixTime = System.currentTimeMillis() / 1000L;
    redis.set("pet:" + user.uuid.toString() + ":timestamp", Long.toString(unixTime));
    redis.set("pet:" + user.uuid.toString(), petName);
  }

  public void adoptPet(Player player, String petName) {
    try {
      final User user = new User(player.getUniqueId(), this);
      if (user.wallet.balance(3) >= PET_PRICE) {
        try {
          if (user.wallet.send(wallet.address(), (double) PET_PRICE) == true) {
            createPet(user, petName);
            spawnPet(player);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        player.sendMessage(ChatColor.RED + "You need " + PET_PRICE / DENOMINATION_FACTOR + " " + DENOMINATION_NAME
            + " to adopt a pet.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      Bukkit.shutdown();
    }
  }

  public void spawnPet(Player player) {
    boolean catIsFound = false;
    String catName = redis.get("pet:" + player.getUniqueId());
    for (World w : Bukkit.getWorlds()) {
      List<Entity> entities = w.getEntities();
      for (Entity entity : entities) {
        if (entity instanceof Ocelot) {
          if (entity.getCustomName() != null && entity.getCustomName().equals(catName)) {
            if (catIsFound == false) {
              entity.teleport(player.getLocation());
              // ((Ocelot) entity).setTamed(true);
              // ((Ocelot) entity).setOwner(player);
              catIsFound = true;
            } else {
              entity.remove();
            }
          }
        }
      }
    }
    if (catIsFound == false) {
      final Ocelot ocelot = (Ocelot) player.getWorld().spawnEntity(player.getLocation(), EntityType.OCELOT);
      ocelot.setCustomName(catName);
      ocelot.setCustomNameVisible(true);
    }
    player.setMetadata("pet", new FixedMetadataValue(this, catName));
  }

  public void teleportToSpawn(Player player) {
    BitQuest bitQuest = this;
    // TODO: open the tps inventory
    player.sendMessage(ChatColor.GREEN + "Teleporting to spawn...");
    player.setMetadata("teleporting", new FixedMetadataValue(bitQuest, true));
    World world = Bukkit.getWorld("world");

    final Location spawn = world.getSpawnLocation();

    Chunk c = spawn.getChunk();
    if (!c.isLoaded()) {
      c.load();
    }
    bitQuest.getServer().getScheduler().scheduleSyncDelayedTask(bitQuest, new Runnable() {

      public void run() {
        player.teleport(spawn);
        if (redis.exists("pet:" + player.getUniqueId()) == true) {
          spawnPet(player);
        }

        player.removeMetadata("teleporting", bitQuest);
      }
    }, 60L);
  }

  public void createScheduledTimers() {
    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
      @Override
      public void run() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
          User user = null;
          try {
            updateScoreboard(player);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }, 0, 1200L);
    scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
      @Override
      public void run() {
        // A villager is born
        World world = Bukkit.getWorld("world");
        world.spawnEntity(world.getSpawnLocation(), EntityType.VILLAGER);

      }
    }, 0, 30000L);

  }

  public void run_season_events() {
    java.util.Date date = new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int month = cal.get(Calendar.MONTH);
    if (month == 9) {
      World world = this.getServer().getWorld("world");
      world.setTime(20000);
      world.setStorm(false);
      spookyMode = true;
    } else {
      spookyMode = false;
    }
  }

  public void removeAllEntities() {
    World w = Bukkit.getWorld("world");
    List<Entity> entities = w.getEntities();
    int entitiesremoved = 0;
    for (Entity entity : entities) {
      entity.remove();
      entitiesremoved = entitiesremoved + 1;
    }
    System.out.println("Killed " + entitiesremoved + " entities");
  }

  public int killAllVillagersInWorld(World w) {
    List<Entity> entities = w.getEntities();
    int villagerskilled = 0;
    for (Entity entity : entities) {
      if ((entity instanceof Villager)) {
        villagerskilled = villagerskilled + 1;
        ((Villager) entity).remove();
      }
      if ((entity instanceof Giant)) {
        villagerskilled = villagerskilled + 1;
        ((Giant) entity).remove();
      }
      if ((entity instanceof Wither)) {
        villagerskilled = villagerskilled + 1;
        ((Wither) entity).remove();
      }
    }
    return villagerskilled;

  }

  public void killAllVillagers() {
    int villagerskilled = 0;
    villagerskilled += killAllVillagersInWorld(Bukkit.getWorld("world"));
    villagerskilled += killAllVillagersInWorld(Bukkit.getWorld("world_the_end"));
    villagerskilled += killAllVillagersInWorld(Bukkit.getWorld("world_nether"));

    System.out.println("Killed " + villagerskilled + " villagers");
  }

  public static final void log(String tag, String msg) {
    System.out.println("[" + tag + "] " + msg);
  }

  public static final void debug(String tag, String msg) {
    if (!BitQuest.BITQUEST_ENV.equals("production")) {
      System.out.println("[" + tag + "] " + msg);
    }
  }

  public int getLevel(int exp) {
    return (int) Math.floor(Math.sqrt(exp / (float) 256));
  }

  public int getExpForLevel(int level) {
    return (int) Math.pow(level, 2) * 256;
  }

  public float getExpProgress(int exp) {
    int level = getLevel(exp);
    int nextlevel = getExpForLevel(level + 1);
    int prevlevel = 0;
    if (level > 0) {
      prevlevel = getExpForLevel(level);
    }
    float progress = ((exp - prevlevel) / (float) (nextlevel - prevlevel));
    return progress;
  }

  public void setTotalExperience(Player player) {
    int rawxp = 0;
    if (redis.exists("experience.raw." + player.getUniqueId().toString())) {
      rawxp = Integer.parseInt(redis.get("experience.raw." + player.getUniqueId().toString()));
    }
    // lower factor, experience is easier to get. you can increase to get the
    // opposite effect
    int level = getLevel(rawxp);
    float progress = getExpProgress(rawxp);
    player.setLevel(level);
    player.setExp(progress);
    setPlayerMaxHealth(player);
  }

  public void setPlayerMaxHealth(Player player) {
    // base health=6
    // level health max=
    int health = 1 + (player.getLevel() / 2);
    if (health > 40) {
      health = 40;
    }
    // player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
    // Integer.MAX_VALUE,
    // player.getLevel(), true));
    player.setMaxHealth(health);
  }

  public void saveLandData(Player player, String name, int x, int z)
      throws ParseException, org.json.simple.parser.ParseException, IOException {
    String chunk = "";
    if (player.getWorld().getName().equals("world")) {
      chunk = "chunk";
    } else if (player.getWorld().getName().equals("world_nether")) {
      chunk = "netherchunk";
    }
    redis.zincrby("player:tx", LAND_PRICE, player.getUniqueId().toString());
    redis.set(chunk + "" + x + "," + z + "owner", player.getUniqueId().toString());
    redis.set(chunk + "" + x + "," + z + "name", name);
    landOwnerCache = new HashMap();
    landNameCache = new HashMap();
    landUnclaimedCache = new HashMap();
    player.sendMessage(ChatColor.GREEN + "Congratulations! You're now the owner of " + ChatColor.DARK_GREEN + name
        + ChatColor.GREEN + "!");
    updateScoreboard(player);
  }

  public boolean validName(final String name) {
    boolean hasNonAlpha = name.matches("^.*[^a-zA-Z0-9 _].*$");
    if (name.isEmpty() || name.length() > 28 || hasNonAlpha || name.equalsIgnoreCase("the wilderness")) {
      return false;
    } else {
      return true;
    }
  }

  public void claimLand(final String name, Chunk chunk, final Player player)
      throws ParseException, org.json.simple.parser.ParseException, IOException {
    String tempchunk = "";
    if (player.getLocation().getWorld().getName().equals("world")) {
      tempchunk = "chunk";
    } else if (player.getLocation().getWorld().getName().equals("world_nether")) {
      tempchunk = "netherchunk";
    }
    // end nether @bitcoinjake09
    // check that land actually has a name
    final int x = chunk.getX();
    final int z = chunk.getZ();
    System.out.println("[claim] " + player.getDisplayName() + " wants to claim " + x + "," + z + " with name " + name);
    if (redis.exists(tempchunk + "" + x + "," + z + "owner") == false) {

      if (validName(name)) {

        if (redis.get(tempchunk + "" + x + "," + z + "owner") == null) {
          player.sendMessage(ChatColor.YELLOW + "Claiming land...");
          BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
          final BitQuest bitQuest = this;
          final Wallet wallet = new Wallet(this.node, player.getUniqueId().toString());
          try {
            wallet.send(this.wallet.address(), LAND_PRICE);
            saveLandData(player, name, x, z);
          } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + e.getMessage());
          }
        } else if (redis.get(tempchunk + "" + x + "," + z + "name").equals(name)) {
          player.sendMessage(ChatColor.DARK_RED + "You already own this land!");
        } else {
          // Rename land
          redis.set(tempchunk + "" + x + "," + z + "name", name);
          player.sendMessage(
              ChatColor.GREEN + "You renamed this land to " + ChatColor.DARK_GREEN + name + ChatColor.GREEN + ".");
        }

      } else {
        player.sendMessage(ChatColor.DARK_RED + "Invalid name.");
      }
    } else {
      player.sendMessage(ChatColor.DARK_RED + "This area is already claimed.");
    }
  }

  public boolean isOwner(Location location, Player player) {
    String chunk = "";
    if (player.getWorld().getName().equals("world")) {
      chunk = "chunk";
    } else if (player.getWorld().getName().equals("world_nether")) {
      chunk = "netherchunk";
    }
    String key = chunk + "" + location.getChunk().getX() + "," + location.getChunk().getZ() + "owner";
    if (redis.get(key).equals(player.getUniqueId().toString())) {
      // player is the owner of the chunk
      return true;
    } else {
      return false;
    }
  }

  public boolean canBuild(Location location, Player player) {
    // returns true if player has permission to build in location
    // TODO: Find out how are we gonna deal with clans and locations, and how/if
    // they are gonna
    // share land resources
    String chunk = "";
    if (player.getWorld().getName().equals("world")) {
      chunk = "chunk";
    } else if (player.getWorld().getName().equals("world_nether")) {
      chunk = "netherchunk";
    } // end nether @bitcoinjake09
    if (!(location.getWorld().getName().equals("world")) && !(location.getWorld().getName().equals("world_nether"))) {
      // If theyre not in the overworld, they cant build
      return false;
    } else if (landIsClaimed(location)) {
      if (isOwner(location, player)) {
        return true;
      } else if (landPermissionCode(location).equals("p")) {
        return true;
      } else if (landPermissionCode(location).equals("pv")) {
        return true; // public pvp @BitcoinJake09
      } else if (landPermissionCode(location).equals("v")) {
        return true; // pvp @BitcoinJake09
      } else if (landPermissionCode(location).equals("c")) {
        String ownerUuid = redis.get("chunk" + location.getChunk().getX() + "," + location.getChunk().getZ() + "owner");
        String ownerClan = redis.get("clan:" + ownerUuid);
        String playerClan = redis.get("clan:" + player.getUniqueId().toString());
        if (ownerClan.equals(playerClan)) {
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
    // v = PvP(private cant build) by @bitcoinjake09
    // pv= public PvP(can build) by @bitcoinjake09
    // n = no permissions (private)
    String chunk = "";
    if (location.getWorld().getName().equals("world")) {
      chunk = "chunk";
    } else if (location.getWorld().getName().equals("world_nether")) {
      chunk = "netherchunk";
    }
    String key = chunk + "" + location.getChunk().getX() + "," + location.getChunk().getZ() + "permissions";
    if (landPermissionCache.containsKey(key)) {
      return landPermissionCache.get(key);
    } else if (redis.exists(key)) {
      String code = redis.get(key);
      landPermissionCache.put(key, code);
      return code;
    } else {
      return "n";
    }
  }

  public boolean createNewArea(Location location, Player owner, String name, int size) {
    // write the new area to REDIS
    JsonObject areaJson = new JsonObject();
    areaJson.addProperty("size", size);
    areaJson.addProperty("owner", owner.getUniqueId().toString());
    areaJson.addProperty("name", name);
    areaJson.addProperty("x", location.getX());
    areaJson.addProperty("z", location.getZ());
    areaJson.addProperty("uuid", UUID.randomUUID().toString());
    redis.lpush("areas", areaJson.toString());
    // TODO: Check if redis actually appended the area to list and return the
    // success of the
    // operation
    return true;
  }

  public boolean isModerator(Player player) {
    if (redis.sismember("moderators", player.getUniqueId().toString())) {
      return true;
    } else if (ADMIN_UUID != null && player.getUniqueId().toString().equals(ADMIN_UUID.toString())) {
      return true;
    } else {
      return false;
    }
  }

  public void sendWalletInfo(final Player player) {
    try {
      Wallet wallet = new Wallet(this.node, player.getUniqueId().toString());
      Double unconfirmedBalance = wallet.balance(0);
      Double confirmedBalance = wallet.balance(3);
      player.sendMessage(ChatColor.BOLD + "Your Wallet");
      player.sendMessage("Balance: " + confirmedBalance);
      if (unconfirmedBalance.equals(confirmedBalance)) {
        player.sendMessage("Unconfirmed Balance: " + unconfirmedBalance);
      }
      player.sendMessage("Add " + BitQuest.DENOMINATION_NAME + ":");
      player.sendMessage(ChatColor.BLUE + "" + ChatColor.UNDERLINE + wallet.addressUrl());
    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(ChatColor.RED + "Error reading wallet. Please try again later.");
    }
  }

  ;

  public boolean landIsClaimed(Location location) {
    String chunk = "";
    if (location.getWorld().getName().equals("world")) {
      chunk = "chunk";
    } else if (location.getWorld().getName().equals("world_nether")) {
      chunk = "netherchunk";
    }
    String key = chunk + "" + location.getChunk().getX() + "," + location.getChunk().getZ() + "owner";

    if (redis.exists(key) == true) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    // we don't allow server commands (yet?)
    if (sender instanceof Player) {
      final Player player = (Player) sender;
      // PLAYER COMMANDS
      for (Map.Entry<String, CommandAction> entry : commands.entrySet()) {
        if (cmd.getName().equalsIgnoreCase(entry.getKey())) {
          entry.getValue().run(sender, cmd, label, args, player);
        }
      }

      // MODERATOR COMMANDS
      for (Map.Entry<String, CommandAction> entry : modCommands.entrySet()) {
        if (cmd.getName().equalsIgnoreCase(entry.getKey())) {
          if (isModerator(player)) {
            entry.getValue().run(sender, cmd, label, args, player);
          } else {
            sender.sendMessage(ChatColor.DARK_RED + "You don't have enough permissions to execute this command!");
          }
        }
      }
    }
    return true;
  }

  public boolean isPvP(Location location) {
    if ((landPermissionCode(location).equals("v") == true) || (landPermissionCode(location).equals("pv") == true)) {
      return true;
    } // returns true. it is a pvp or public pvp and if SET_PvP is true

    return false; // not pvp
  }

  public String urlenEncode(String en) throws UnsupportedEncodingException {
    return URLEncoder.encode(en, "UTF-8");
  }

  public String urlenDecode(String en) throws UnsupportedEncodingException {
    return URLDecoder.decode(en, "UTF-8");
  }

  public boolean sendDiscordMessage(String content) {
    if (System.getenv("DISCORD_HOOK_URL") != null) {
      System.out.println("[discord] " + content);
      try {

        JSONParser parser = new JSONParser();

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", content);
        CookieHandler.setDefault(new CookieManager());

        URL url = new URL(System.getenv("DISCORD_HOOK_URL"));
        HttpsURLConnection con = null;

        System.setProperty("http.agent", "");

        con = (HttpsURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Cookie", "bitquest=true");
        con.setRequestProperty("User-Agent",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US;" + " rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        String json = "{\"content\":\"" + content + "\"}";
        out.write(json);
        out.close();
        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
          BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
          String inputLine;
          StringBuffer response = new StringBuffer();

          while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
          }
          in.close();
          System.out.println(response.toString());
          return true;
        } else {
          BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
          String inputLine;
          StringBuffer response = new StringBuffer();

          while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
          }
          in.close();
          System.out.println(response.toString());
          return false;
        }

      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    }
    return false;

  }

  public void crashtest() {
    this.setEnabled(false);
  }
}
