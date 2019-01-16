package com.bitquest.bitquest;

import com.bitquest.bitquest.commands.*;
import com.bitquest.bitquest.events.*;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.*;
import java.sql.DriverManager;
import java.text.ParseException;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;
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
  public static final String BITQUEST_ENV =
      System.getenv("BITQUEST_ENV") != null ? System.getenv("BITQUEST_ENV") : "development";
  public static final UUID ADMIN_UUID =
      System.getenv("ADMIN_UUID") != null ? UUID.fromString(System.getenv("ADMIN_UUID")) : null;

  public static final String BITCOIN_NODE_HOST =
      System.getenv("BITCOIN_PORT_8332_TCP_ADDR") != null
          ? System.getenv("BITCOIN_PORT_8332_TCP_ADDR")
          : null;
  public static final int BITCOIN_NODE_PORT =
      System.getenv("BITCOIN_PORT_8332_TCP_PORT") != null
          ? Integer.parseInt(System.getenv("BITCOIN_PORT_8332_TCP_PORT"))
          : 8332;
  public static final String SERVERDISPLAY_NAME =
      System.getenv("SERVERDISPLAY_NAME") != null ? System.getenv("SERVERDISPLAY_NAME") : "Bit";
  public static final Long DENOMINATION_FACTOR =
      System.getenv("DENOMINATION_FACTOR") != null
          ? Long.parseLong(System.getenv("DENOMINATION_FACTOR"))
          : 100L;
  public static final String DENOMINATION_NAME =
      System.getenv("DENOMINATION_NAME") != null ? System.getenv("DENOMINATION_NAME") : "Bits";
  public static final String BLOCKCYPHER_CHAIN =
      System.getenv("BLOCKCYPHER_CHAIN") != null ? System.getenv("BLOCKCYPHER_CHAIN") : "btc/test3";
  public static final String BITCOIN_NODE_USERNAME = System.getenv("BITCOIN_ENV_USERNAME");
  public static final String BITCOIN_NODE_PASSWORD = System.getenv("BITCOIN_ENV_PASSWORD");
  public static final String DISCORD_HOOK_URL = System.getenv("DISCORD_HOOK_URL");
  public static final String BLOCKCYPHER_API_KEY =
      System.getenv("BLOCKCYPHER_API_KEY") != null ? System.getenv("BLOCKCYPHER_API_KEY") : null;
  public static final Long MINER_FEE =
          System.getenv("MINER_FEE") != null ? Long.parseLong(System.getenv("MINER_FEE")) : 10000;

  public static final int MAX_STOCK = 100;
  public static final String SERVER_NAME =
      System.getenv("SERVER_NAME") != null ? System.getenv("SERVER_NAME") : "BitQuest";

  // Can save world data in elasticsearch (optional)
  public static final String ELASTICSEARCH_ENDPOINT =
      System.getenv("ELASTICSEARCH_ENDPOINT") != null
          ? System.getenv("ELASTICSEARCH_ENDPOINT")
          : null;
  // REDIS: Look for Environment variables on hostname and port, otherwise defaults to
  // localhost:6379
  public static final String REDIS_HOST =
      System.getenv("REDIS_PORT_6379_TCP_ADDR") != null
          ? System.getenv("REDIS_PORT_6379_TCP_ADDR")
          : "localhost";
  public static final Integer REDIS_PORT =
      System.getenv("REDIS_PORT_6379_TCP_PORT") != null
          ? Integer.parseInt(System.getenv("REDIS_PORT_6379_TCP_PORT"))
          : 6379;
  public static final Jedis REDIS = new Jedis(REDIS_HOST, REDIS_PORT);

  // Default price: 10,000 satoshis or 100 bits
  public static final Long LAND_PRICE =
      System.getenv("LAND_PRICE") != null ? Long.parseLong(System.getenv("LAND_PRICE")) : 10000;
  // Minimum transaction by default is 2000 bits
  public static final Long MINIMUM_TRANSACTION =
      System.getenv("MINIMUM_TRANSACTION") != null
          ? Long.parseLong(System.getenv("MINIMUM_TRANSACTION"))
          : 2000L;

  public static int rand(int min, int max) {
    return min + (int) (Math.random() * ((max - min) + 1));
  }

  public Wallet wallet = null;
  public Player last_loot_player;
  public boolean spookyMode = false;
  // caches is used to reduce the amounts of calls to redis, storing some chunk information in
  // memory
  public HashMap<String, Boolean> land_unclaimed_cache = new HashMap();
  public HashMap<String, String> land_owner_cache = new HashMap();
  public HashMap<String, String> land_permission_cache = new HashMap();
  public HashMap<String, String> land_name_cache = new HashMap();
  public Long wallet_balance_cache = 0L;
  public ArrayList<ItemStack> books = new ArrayList<ItemStack>();
  // when true, server is closed for maintenance and not allowing players to join in.
  public boolean maintenance_mode = false;
  boolean boss_already_spawned=false;
  private Map<String, CommandAction> commands;
  private Map<String, CommandAction> modCommands;
  private Player[] moderators;
  public static long PET_PRICE = 100 * DENOMINATION_FACTOR;

  public static final String db_url =
      "jdbc:postgresql://"
          + System.getenv("POSTGRES_PORT_5432_TCP_ADDR")
          + ":"
          + System.getenv("POSTGRES_PORT_5432_TCP_PORT")
          + "/bitquest";
  public java.sql.Connection db_con;

  @Override
  public void onEnable() {
    log("[startup] BitQuest starting");

    System.out.println("Checking that POSTGRES_PORT_5432_TCP_PORT envoronment variable exists...");
    if (System.getenv("POSTGRES_PORT_5432_TCP_PORT") == null) {
      Bukkit.shutdown();
      System.out.println("Please set the POSTGRES_PORT_5432_TCP_PORT environment variable");
    }
    ;

    try {
      Class.forName("org.postgresql.Driver");
      this.db_con =
          DriverManager.getConnection(
              this.db_url,
              System.getenv("POSTGRES_ENV_POSTGRES_USER"),
              System.getenv("POSTGRES_ENV_POSTGRES_PASSWORD"));
      DBMigrationCheck migration = new DBMigrationCheck(this.db_con);

      if (ADMIN_UUID == null) {
        log("[warning] ADMIN_UUID env variable to is not set.");
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

      // loads world wallet from env variables. If not present, generates a new one each time the
      // server is run.
      if (System.getenv("PRIVATE") != null
          && System.getenv("PUBLIC") != null
          && System.getenv("ADDRESS") != null
          && System.getenv("WIF") != null) {
        wallet =
                new Wallet(
                        System.getenv("PRIVATE"),
                        System.getenv("PUBLIC"),
                        System.getenv("ADDRESS"),
                        System.getenv("WIF"));
        System.out.println("[world wallet] imported from environment");
      } else if (REDIS.exists("private")&&REDIS.exists("public")&&REDIS.exists("address")&&REDIS.exists("wif")){
        wallet =
                new Wallet(
                        REDIS.get("private"),
                        REDIS.get("public"),
                        REDIS.get("address"),
                        REDIS.get("wif"));
      } else {
        wallet = this.generateNewWallet();
        System.out.println("[world wallet] generated new wallet");
      }
      System.out.println("[world wallet] address: " + wallet.address);

      if (BITCOIN_NODE_HOST != null) {
        System.out.println("[startup] checking bitcoin node connection");
        getBlockChainInfo();
      }

      // creates scheduled timers (update balances, etc)
      createScheduledTimers();
      commands = new HashMap<String, CommandAction>();
      commands.put("wallet", new WalletCommand(this));
      commands.put("land", new LandCommand(this));
      commands.put("home", new HomeCommand(this));
      commands.put("clan", new ClanCommand(this));
      commands.put("transfer", new TransferCommand(this));
      commands.put("report", new ReportCommand(this));
      commands.put("send", new SendCommand(this));
      commands.put("currency", new CurrencyCommand(this));
      commands.put("upgradewallet", new UpgradeWallet(this));
      commands.put("donate", new DonateCommand(this));
      commands.put("profession", new ProfessionCommand(this));
      commands.put("spawn", new SpawnCommand(this));
      commands.put("pet", new PetCommand(this));
      modCommands = new HashMap<String, CommandAction>();
      modCommands.put("butcher", new ButcherCommand());
      modCommands.put("killAllVillagers", new KillAllVillagersCommand(this));
      modCommands.put("crashTest", new CrashtestCommand(this));
      modCommands.put("mod", new ModCommand());
      modCommands.put("ban", new BanCommand());
      modCommands.put("unban", new UnbanCommand());
      modCommands.put("banlist", new BanlistCommand());
      modCommands.put("spectate", new SpectateCommand(this));
      modCommands.put("emergencystop", new EmergencystopCommand());
      modCommands.put("fixabandonland", new FixAbandonLand());
      modCommands.put("motd", new MOTDCommand(this));
      // TODO: Remove this command after migrate.
      modCommands.put("migrateclans", new MigrateClansCommand());
      updateLootPoolCache();
      publish_stats();
      REDIS.set("loot:rate:limit", "1");
      REDIS.expire("loot:rate:limit", 10);
      killAllVillagers();
      System.out.println("[startup] finished");

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("[fatal] plugin enable fails");
      Bukkit.shutdown();
    }
  }
  public void updateLootPoolCache() {
    System.out.println("[loot_cache]");
    boss_already_spawned=false;
    try {
      Long balance=wallet.getBalance(0);
      System.out.println("[loot_cache] "+balance);

      if(balance>(LAND_PRICE+MINER_FEE)*2) {
        REDIS.set("loot_cache",balance.toString());
      }
    } catch (IOException e) {
      e.printStackTrace();
      REDIS.del("loot_cache");
      System.out.println("[loot_cache] FAIL");
    } catch (org.json.simple.parser.ParseException e) {
      e.printStackTrace();
      REDIS.del("loot_cache");
      System.out.println("[loot_cache] FAIL");

    }

  }
  static Long witherReward() {
    return (LAND_PRICE);
  }
  public void createBossFight(Location location) {
    try {
      if((BitQuest.rand(0,10)==5)&&wallet.getBalance(0)>400*DENOMINATION_FACTOR) {
        List<Entity> entities = location.getWorld().getEntities();

        for (Entity en : entities) {
          if ((en instanceof Wither)) {
            boss_already_spawned=true;
          }
        }

        if(boss_already_spawned==false&&location.getY()>64) {
          System.out.println("[boss fight] spawn in "+location.getX()+","+location.getY()+","+location.getZ());
          boss_already_spawned=true;
          location.getWorld().spawnEntity(location,EntityType.WITHER);

          for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld().getName().equals(player.getWorld().getName())) {
              player.sendMessage("A boss has spawned! Distance: "+location.distance(player.getLocation()));
              sendDiscordMessage("A Wither has spawned in "+location.getX()+","+location.getY()+","+location.getZ()+" rewards: "+Math.round(witherReward()/DENOMINATION_FACTOR)+" "+DENOMINATION_NAME);
            }
          }

        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (org.json.simple.parser.ParseException e) {
      e.printStackTrace();
    }

  }



  public static final Wallet generateNewWallet()
      throws IOException, org.json.simple.parser.ParseException {
    JSONParser parser = new JSONParser();
    final JSONObject jsonObject = new JSONObject();

    URL url = new URL("https://api.blockcypher.com/v1/" + BLOCKCYPHER_CHAIN + "/addrs");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setConnectTimeout(5000);
    con.setRequestMethod("POST");
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
    JSONObject response_object = (JSONObject) parser.parse(response.toString());
    System.out.println("Created wallet: " + response_object.get("address").toString());
    REDIS.set("private",response_object.get("private").toString());
    REDIS.set("public",response_object.get("public").toString());
    REDIS.set("address",response_object.get("address").toString());
    REDIS.set("wif",response_object.get("wif").toString());

    return new Wallet(
        response_object.get("private").toString(),
        response_object.get("public").toString(),
        response_object.get("address").toString(),
        response_object.get("wif").toString());
  }
  // @todo: make this just accept the endpoint name and (optional) parameters
  public JSONObject getBlockChainInfo() throws org.json.simple.parser.ParseException {
    JSONParser parser = new JSONParser();

    try {
      final JSONObject jsonObject = new JSONObject();
      jsonObject.put("jsonrpc", "1.0");
      jsonObject.put("id", "bitquest");
      jsonObject.put("method", "getblockchaininfo");
      JSONArray params = new JSONArray();
      jsonObject.put("params", params);
      System.out.println("Checking blockchain info...");
      URL url = new URL("http://" + BITCOIN_NODE_HOST + ":" + BITCOIN_NODE_PORT);
      System.out.println(url.toString());
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      String userPassword = BITCOIN_NODE_USERNAME + ":" + BITCOIN_NODE_PASSWORD;
      String encoding = java.util.Base64.getEncoder().encodeToString(userPassword.getBytes());
      con.setRequestProperty("Authorization", "Basic " + encoding);

      con.setRequestMethod("POST");
      con.setRequestProperty("User-Agent", "bitquest plugin");
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

  public void announce(final String message) {
    for (Player player : Bukkit.getOnlinePlayers()) {
      player.sendMessage(message);
    }
  }

  public void updateScoreboard(final Player player) {
    try {
      final User user = new User(this.db_con, player.getUniqueId());
      ScoreboardManager scoreboardManager;
      Scoreboard walletScoreboard;
      Objective walletScoreboardObjective;
      scoreboardManager = Bukkit.getScoreboardManager();
      walletScoreboard = scoreboardManager.getNewScoreboard();
      walletScoreboardObjective = walletScoreboard.registerNewObjective("wallet", "dummy");

      walletScoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

      walletScoreboardObjective.setDisplayName(
          ChatColor.GOLD
              + ChatColor.BOLD.toString()
              + BitQuest.SERVERDISPLAY_NAME
              + ChatColor.GRAY
              + ChatColor.BOLD.toString()
              + "Quest");

      if (BitQuest.BLOCKCYPHER_CHAIN != null) {
        Score score =
            walletScoreboardObjective.getScore(
                ChatColor.GREEN + "Balance:"); // Get a fake offline player
        score.setScore((int) (user.wallet.getBalance(0) / DENOMINATION_FACTOR));

        player.setScoreboard(walletScoreboard);

      } else {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.runTaskAsynchronously(
            this,
            new Runnable() {
              @Override
              public void run() {
                try {
                  Score score =
                      walletScoreboardObjective.getScore(
                          ChatColor.GREEN + "Ems:"); // Get a fake offline player

                  score.setScore(user.countEmeralds(player.getInventory()));
                  player.setScoreboard(walletScoreboard);
                } catch (Exception e) {
                  System.out.println("problems in updatescoreboard");
                }
              }
            });
      } // end emerald here
    } catch (Exception e) {
      System.out.println("[scoreboard] rate limit");
    }
  }

  public void createPet(User user, String pet_name) {
    REDIS.sadd("pet:names", pet_name);
    BitQuest.REDIS.zincrby("player:tx", PET_PRICE, user.uuid.toString());
    long unixTime = System.currentTimeMillis() / 1000L;
    REDIS.set("pet:" + user.uuid.toString() + ":timestamp", Long.toString(unixTime));
    REDIS.set("pet:" + user.uuid.toString(), pet_name);
  }

  public void adoptPet(Player player, String pet_name) {
    try {
      final User user = new User(this.db_con, player.getUniqueId());
      if (user.wallet.getBalance(3) >= PET_PRICE) {
        try {
          if (user.wallet.payment(this.wallet.address, PET_PRICE) == true) {
            createPet(user, pet_name);
            spawnPet(player);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        player.sendMessage(
            ChatColor.RED
                + "You need "
                + PET_PRICE / DENOMINATION_FACTOR
                + " "
                + DENOMINATION_NAME
                + " to adopt a pet.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      Bukkit.shutdown();
    }
  }

  public void spawnPet(Player player) {
    boolean cat_is_found = false;
    String cat_name = REDIS.get("pet:" + player.getUniqueId());
    for (World w : Bukkit.getWorlds()) {
      List<Entity> entities = w.getEntities();
      for (Entity entity : entities) {
        if (entity instanceof Ocelot) {
          if (entity.getCustomName() != null && entity.getCustomName().equals(cat_name)) {
            if (cat_is_found == false) {
              entity.teleport(player.getLocation());
              ((Ocelot) entity).setTamed(true);
              ((Ocelot) entity).setOwner(player);
              cat_is_found = true;
            } else {
              entity.remove();
            }
          }
        }
      }
    }
    if (cat_is_found == false) {
      final Ocelot ocelot =
          (Ocelot) player.getWorld().spawnEntity(player.getLocation(), EntityType.OCELOT);
      ocelot.setCustomName(cat_name);
      ocelot.setCustomNameVisible(true);
    }
    player.setMetadata("pet", new FixedMetadataValue(this, cat_name));
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
    bitQuest
        .getServer()
        .getScheduler()
        .scheduleSyncDelayedTask(
            bitQuest,
            new Runnable() {

              public void run() {
                player.teleport(spawn);
                if (REDIS.exists("pet:" + player.getUniqueId()) == true) {
                  spawnPet(player);
                }

                player.removeMetadata("teleporting", bitQuest);
              }
            },
            60L);
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
    scheduler.scheduleSyncRepeatingTask(
        this,
        new Runnable() {
          @Override
          public void run() {
            // A villager is born
            World world = Bukkit.getWorld("world");
            world.spawnEntity(world.getSpawnLocation(), EntityType.VILLAGER);

          }
        },
        0,
        7200L);



    scheduler.scheduleSyncRepeatingTask(
        this,
        new Runnable() {
          @Override
          public void run() {
            run_season_events();
            publish_stats();
            updateLootPoolCache();
          }
        },
        0,
        30000L);
  }

  public void publish_stats() {
    try {
      Long balance = wallet.getBalance(0);
      REDIS.set("loot:pool", Long.toString(balance));
      if (System.getenv("ELASTICSEARCH_ENDPOINT") != null) {
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject = new JSONObject();

        jsonObject.put("balance", balance);
        jsonObject.put("time", new Date().getTime());
        URL url = new URL(System.getenv("ELASTICSEARCH_ENDPOINT") + "-stats/_doc");
        System.out.println(url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
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
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        System.out.println(response_object);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    int villagerskilled=0;
    villagerskilled+=killAllVillagersInWorld(Bukkit.getWorld("world"));
    villagerskilled+=killAllVillagersInWorld(Bukkit.getWorld("world_the_end"));
    villagerskilled+=killAllVillagersInWorld(Bukkit.getWorld("world_nether"));

    System.out.println("Killed " + villagerskilled + " villagers");
  }

  public void log(String msg) {
    Bukkit.getLogger().info(msg);
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
    if (BitQuest.REDIS.exists("experience.raw." + player.getUniqueId().toString())) {
      rawxp =
          Integer.parseInt(BitQuest.REDIS.get("experience.raw." + player.getUniqueId().toString()));
    }
    // lower factor, experience is easier to get. you can increase to get the opposite effect
    int level = getLevel(rawxp);
    float progress = getExpProgress(rawxp);
    player.setLevel(level);
    player.setExp(progress);
    setPlayerMaxHealth(player);
  }

  public void setPlayerMaxHealth(Player player) {
    // base health=6
    // level health max=
    int health = 8 + (player.getLevel() / 2);
    if (health > 40) health = 40;
    // player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE,
    // player.getLevel(), true));
    player.setMaxHealth(health);
  }

  public void saveLandData(Player player, String name, int x, int z)
      throws ParseException, org.json.simple.parser.ParseException, IOException {
    String chunk = "";
    if (player.getWorld().getName().equals("world")) {
      chunk = "chunk";
    } // end world lmao @bitcoinjake09
    else if (player.getWorld().getName().equals("world_nether")) {
      chunk = "netherchunk";
    } // end nether @bitcoinjake09
    BitQuest.REDIS.zincrby("player:tx", LAND_PRICE, player.getUniqueId().toString());
    BitQuest.REDIS.set(chunk + "" + x + "," + z + "owner", player.getUniqueId().toString());
    BitQuest.REDIS.set(chunk + "" + x + "," + z + "name", name);
    land_owner_cache = new HashMap();
    land_name_cache = new HashMap();
    land_unclaimed_cache = new HashMap();
    player.sendMessage(
        ChatColor.GREEN
            + "Congratulations! You're now the owner of "
            + ChatColor.DARK_GREEN
            + name
            + ChatColor.GREEN
            + "!");
    updateScoreboard(player);
  }
  public boolean validName(final String name) {
    boolean hasNonAlpha = name.matches("^.*[^a-zA-Z0-9 _].*$");
    if(name.isEmpty()||name.length() > 28||hasNonAlpha||name.equalsIgnoreCase("the wilderness")) {
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
    } // end world lmao @bitcoinjake09
    else if (player.getLocation().getWorld().getName().equals("world_nether")) {
      tempchunk = "netherchunk";
    }
    // end nether @bitcoinjake09
    // check that land actually has a name
    final int x = chunk.getX();
    final int z = chunk.getZ();
    System.out.println(
        "[claim] "
            + player.getDisplayName()
            + " wants to claim "
            + x
            + ","
            + z
            + " with name "
            + name);
    if (REDIS.exists(tempchunk + "" + x + "," + z + "owner") == false) {

      if(validName(name)) {


            if (REDIS.get(tempchunk + "" + x + "," + z + "owner") == null) {
              try {

                final User user = new User(this.db_con, player.getUniqueId());
                player.sendMessage(ChatColor.YELLOW + "Claiming land...");
                BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                final BitQuest bitQuest = this;
                if (BitQuest.BLOCKCYPHER_CHAIN != null) {

                  if (user.wallet.payment(this.wallet.address, LAND_PRICE)) {
                    saveLandData(player, name, x, z);
                  } else {

                    player.sendMessage(
                        ChatColor.RED + "Claim payment failed. Please try again later.");
                  }
                } else {
                  int landxprice = 1;
                  if (player.getLocation().getWorld().getName().equals("world_nether")) {
                    landxprice = 4;
                  }
                  int land_price_in_emeralds =
                      (int) ((LAND_PRICE * landxprice) / BitQuest.DENOMINATION_FACTOR);
                  if (user.countEmeralds(player.getInventory()) > land_price_in_emeralds) {
                    if (user.removeEmeralds(land_price_in_emeralds, player)) {
                      saveLandData(player, name, x, z);
                    } else {
                      player.sendMessage(ChatColor.RED + "There was an error.");
                    }

                  } else {
                    player.sendMessage(
                        ChatColor.RED
                            + "You have "
                            + user.countEmeralds(player.getInventory())
                            + ". To buy land you need "
                            + land_price_in_emeralds);
                  }
                }
              } catch (Exception e) {
                e.printStackTrace();
              }

            } else if (BitQuest.REDIS.get(tempchunk + "" + x + "," + z + "name").equals(name)) {
              player.sendMessage(ChatColor.DARK_RED + "You already own this land!");
            } else {
              // Rename land
              BitQuest.REDIS.set(tempchunk + "" + x + "," + z + "name", name);
              player.sendMessage(
                  ChatColor.GREEN
                      + "You renamed this land to "
                      + ChatColor.DARK_GREEN
                      + name
                      + ChatColor.GREEN
                      + ".");
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
    } // end world lmao @bitcoinjake09
    else if (player.getWorld().getName().equals("world_nether")) {
      chunk = "netherchunk";
    } // end nether @bitcoinjake09
    String key =
        chunk + "" + location.getChunk().getX() + "," + location.getChunk().getZ() + "owner";
    if (REDIS.get(key).equals(player.getUniqueId().toString())) {
      // player is the owner of the chunk
      return true;
    } else {
      return false;
    }
  }

  public boolean canBuild(Location location, Player player) {
    // returns true if player has permission to build in location
    // TODO: Find out how are we gonna deal with clans and locations, and how/if they are gonna
    // share land resources
    String chunk = "";
    if (player.getWorld().getName().equals("world")) {
      chunk = "chunk";
    } // end world lmao @bitcoinjake09
    else if (player.getWorld().getName().equals("world_nether")) {
      chunk = "netherchunk";
    } // end nether @bitcoinjake09
    if (!(location.getWorld().getName().equals("world"))
        && !(location.getWorld().getName().equals("world_nether"))) {
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
        String owner_uuid =
            REDIS.get("chunk"
                    + location.getChunk().getX()
                    + ","
                    + location.getChunk().getZ()
                    + "owner");
        String owner_clan = REDIS.get("clan:" + owner_uuid);
        String player_clan = REDIS.get("clan:" + player.getUniqueId().toString());
        if (owner_clan.equals(player_clan)) {
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
    } // end world lmao @bitcoinjake09
    else if (location.getWorld().getName().equals("world_nether")) {
      chunk = "netherchunk";
    } // end nether @bitcoinjake09
    String key =
        chunk + "" + location.getChunk().getX() + "," + location.getChunk().getZ() + "permissions";
    if (land_permission_cache.containsKey(key)) {
      return land_permission_cache.get(key);
    } else if (REDIS.exists(key)) {
      String code = REDIS.get(key);
      land_permission_cache.put(key, code);
      return code;
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
    // TODO: Check if redis actually appended the area to list and return the success of the
    // operation
    return true;
  }

  public boolean isModerator(Player player) {
    if (REDIS.sismember("moderators", player.getUniqueId().toString())) {
      return true;
    } else if (ADMIN_UUID != null
        && player.getUniqueId().toString().equals(ADMIN_UUID.toString())) {
      return true;
    } else {
      return false;
    }
  }

  public void sendWalletInfo(final Player player, final User user) {
    if (BITCOIN_NODE_HOST != null) {
      // TODO: Rewrite send wallet info
    }
    try {
      Long balance = user.wallet.getBalance(0);

      player.sendMessage("Address: " + user.wallet.address);
      player.sendMessage("Balance: " + balance);
      player.sendMessage(
          "URL: " + ChatColor.BLUE + ChatColor.UNDERLINE + ChatColor.BOLD + user.wallet.url());
      player.sendMessage("-----------");

    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(ChatColor.RED + "Error reading wallet. Please try again later.");
    }
  };

  public boolean landIsClaimed(Location location) {
    String chunk = "";
    if (location.getWorld().getName().equals("world")) {
      chunk = "chunk";
    } // end world lmao @bitcoinjake09
    else if (location.getWorld().getName().equals("world_nether")) {
      chunk = "netherchunk";
    } // end nether @bitcoinjake09
    String key =
        chunk + "" + location.getChunk().getX() + "," + location.getChunk().getZ() + "owner";

    if (REDIS.exists(key) == true) {
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
            sender.sendMessage(
                ChatColor.DARK_RED + "You don't have enough permissions to execute this command!");
          }
        }
      }
    }
    return true;
  }

  public boolean isPvP(Location location) {
    if ((landPermissionCode(location).equals("v") == true)
        || (landPermissionCode(location).equals("pv") == true))
    // if(SET_PvP.equals("true"))
    {
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
    if(System.getenv("DISCORD_HOOK_URL")!=null) {
      System.out.println("[discord] "+content);
      try {
          String json = "{\"content\":\""+content+"\"}";

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
          con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

          con.setDoOutput(true);
          OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
          out.write(json);
          out.close();
          int responseCode = con.getResponseCode();
          if(responseCode==200) {
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
