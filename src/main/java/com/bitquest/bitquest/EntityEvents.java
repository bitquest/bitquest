package com.bitquest.bitquest;

import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by explodi on 11/7/15.
 */
public class EntityEvents implements Listener {
    BitQuest bitQuest;
    StringBuilder rawwelcome = new StringBuilder();
    String PROBLEM_MESSAGE="Can't join right now. Come back later";


    private static final List<Material> PROTECTED_BLOCKS = Arrays.asList(Material.CHEST, Material.ACACIA_DOOR,
	    Material.BIRCH_DOOR,Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR, Material.SPRUCE_DOOR,
	    Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.FURNACE, Material.BURNING_FURNACE,
            Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.FENCE_GATE,
            Material.JUNGLE_FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.DISPENSER, Material.DROPPER,
            Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX,
            Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
            Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.SILVER_SHULKER_BOX,
            Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX);

    private static final List<EntityType> PROTECTED_ENTITIES = Arrays.asList(EntityType.ARMOR_STAND, EntityType.ITEM_FRAME,
            EntityType.PAINTING, EntityType.ENDER_CRYSTAL);
    
    public EntityEvents(BitQuest plugin) {
        bitQuest = plugin;


        
        for (String line : bitQuest.getConfig().getStringList("welcomeMessage")) {
            for (ChatColor color : ChatColor.values()) {
                line = line.replaceAll("<" + color.name() + ">", color.toString());
            }
            // add links
            final Pattern pattern = Pattern.compile("<link>(.+?)</link>");
            final Matcher matcher = pattern.matcher(line);
            matcher.find();
            String link = matcher.group(1);
            // Right here we need to replace the link variable with a minecraft-compatible link
            line = line.replaceAll("<link>" + link + "<link>", link);

            rawwelcome.append(line);
        }
    }


    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        User user = null;
        try {
            user = new User(event.getPlayer());
        } catch (ParseException e) {
            e.printStackTrace();
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,PROBLEM_MESSAGE);

        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,PROBLEM_MESSAGE);

        } catch (IOException e) {
            e.printStackTrace();
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,PROBLEM_MESSAGE);

        }
        Player player=event.getPlayer();

        try {


            BitQuest.REDIS.set("name:"+player.getUniqueId().toString(),player.getName());
            BitQuest.REDIS.set("uuid:"+player.getName().toString(),player.getUniqueId().toString());
            if(BitQuest.REDIS.sismember("banlist",event.getPlayer().getUniqueId().toString())) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER,PROBLEM_MESSAGE);
            }
            if(BitQuest.HD_ROOT_ADDRESS==null) {
                // use regular addresses with private keys stored on redis
                if(!BitQuest.REDIS.exists("address"+player.getUniqueId().toString())&&!BitQuest.REDIS.exists("private"+player.getUniqueId().toString())) {
                    System.out.println("Generating new address...");
                    URL url = new URL("https://api.blockcypher.com/v1/"+BitQuest.BLOCKCHAIN+"/addrs");
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
                    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.flush();
                    wr.close();

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONParser parser = new JSONParser();
                    final JSONObject jsonobj = (JSONObject) parser.parse(response.toString());
                    System.out.println(response.toString());
                    BitQuest.REDIS.set("private"+player.getUniqueId().toString(), (String) jsonobj.get("private"));
                    BitQuest.REDIS.set("public"+player.getUniqueId().toString(), (String) jsonobj.get("public"));
                    BitQuest.REDIS.set("address"+player.getUniqueId().toString(), (String) jsonobj.get("address"));
                }
            } else {
                // use HD address derived from HD_ROOT_ADDRESS
                if(!BitQuest.REDIS.exists("hd:address:"+player.getUniqueId().toString())) {
                    System.out.println("Generating new HD address...");
                    URL url = new URL("https://api.blockcypher.com/v1/"+BitQuest.BLOCKCHAIN+"/wallets/hd/bitquest/addresses/derive?token="+BitQuest.BLOCKCYPHER_API_KEY+"&subchain_index=0");
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
                    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.flush();
                    wr.close();

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONParser parser = new JSONParser();
                    final JSONObject responseobject = (JSONObject) parser.parse(response.toString());
                    System.out.println(response.toString());
                    final JSONArray chains=(JSONArray) responseobject.get("chains");
                    System.out.println(chains);
                    final JSONObject chain=(JSONObject) chains.get(0);
                    System.out.println(chain);
                    final JSONArray chain_addresses=(JSONArray) chain.get("chain_addresses");
                    System.out.println(chain_addresses);
                    final JSONObject wallet=(JSONObject) chain_addresses.get(0);
                    System.out.println(wallet);
                    final String address=(String) wallet.get("address");
                    final String path=(String) wallet.get("path");
                    final String public_key=(String) wallet.get("public");
                    BitQuest.REDIS.set("hd:address:"+player.getUniqueId().toString(), address);
                    BitQuest.REDIS.set("hd:path:"+player.getUniqueId().toString(), path);
                    BitQuest.REDIS.set("hd:public:"+player.getUniqueId().toString(), public_key);
                    user = new User(event.getPlayer());
                    System.out.println(" ------------- new hd wallet for player "+player.getDisplayName()+" ----------");
                    System.out.println(" hd address: "+user.wallet.address);
                    System.out.println(" hd path: "+user.wallet.path);
                    System.out.println(" hd public key: "+user.wallet.public_key);
                    System.out.println(" --------------------------------------------------------------------");
                }
            }
            // test player wallet. kick if it's not working

        } catch (ParseException e) {
            e.printStackTrace();
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,PROBLEM_MESSAGE);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,PROBLEM_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,PROBLEM_MESSAGE);

        }
        try {
            System.out.println("player balance is: "+user.wallet.final_balance());
        } catch (IOException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,PROBLEM_MESSAGE);
            System.out.println("[login] wallet balance update fails");
        } catch (org.json.simple.parser.ParseException e) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,PROBLEM_MESSAGE);
            System.out.println("[login] wallet balance update fails");
        }

    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws IOException, org.json.simple.parser.ParseException, ParseException, JSONException {
        final Player player=event.getPlayer();
        // On dev environment, admin gets op. In production, nobody gets op.

        player.setGameMode(GameMode.SURVIVAL);
        final User user = new User(player);
        bitQuest.updateScoreboard(player);
        user.setTotalExperience(user.experience());
        final String ip=player.getAddress().toString().split("/")[1].split(":")[0];
        System.out.println("User "+player.getName()+"logged in with IP "+ip);
        BitQuest.REDIS.set("ip"+player.getUniqueId().toString(),ip);
        BitQuest.REDIS.set("displayname:"+player.getUniqueId().toString(),player.getDisplayName());
        BitQuest.REDIS.set("uuid:"+player.getName().toString(),player.getUniqueId().toString());
        if (bitQuest.isModerator(player)) {
            if (bitQuest.BITQUEST_ENV.equals("development")==true) {
                player.setOp(true);
            }
            player.sendMessage(ChatColor.YELLOW + "You are a moderator on this server.");
            player.sendMessage(ChatColor.YELLOW + "The world wallet balance is: " + bitQuest.wallet.final_balance() / 100 + " bits");
            player.sendMessage(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "blockchain.info/address/" + bitQuest.wallet.address);
        }

        String welcome = rawwelcome.toString();
        welcome = welcome.replace("<name>", player.getName());
        player.sendMessage(welcome);
        if(BitQuest.REDIS.exists("clan:"+player.getUniqueId().toString())) {
            String clan = BitQuest.REDIS.get("clan:"+player.getUniqueId().toString());
            player.setPlayerListName(ChatColor.GOLD + "[" + clan + "] " + ChatColor.WHITE + player.getName());
        }

        // Prints the user balance
        user.setTotalExperience((Integer) user.experience());

        try {

            // check and set experience
            bitQuest.updateScoreboard(player);


            bitQuest.sendWalletInfo(user);

            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Don't forget to visit the BitQuest Wiki");
            player.sendMessage(ChatColor.YELLOW + "There's tons of useful stuff there!");
            player.sendMessage("");
            player.sendMessage(ChatColor.BLUE + "     " + ChatColor.UNDERLINE + "http://bit.ly/wikibq");
            player.sendMessage("");
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(bitQuest.messageBuilder != null) {
            final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

            scheduler.runTaskAsynchronously(bitQuest, new Runnable() {
                @Override
                public void run() {
                    org.json.JSONObject sentEvent = bitQuest.messageBuilder.event(player.getUniqueId().toString(), "Login", null);
                    org.json.JSONObject props = new org.json.JSONObject();
                    try {
                        props.put("$name", player.getName());
                        props.put("$ip", ip);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    org.json.JSONObject update = bitQuest.messageBuilder.set(player.getUniqueId().toString(), props);


                    ClientDelivery delivery = new ClientDelivery();
                    delivery.addMessage(sentEvent);
                    delivery.addMessage(update);

                    MixpanelAPI mixpanel = new MixpanelAPI();
                    try {
                        mixpanel.deliver(delivery);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

            });


        }


    }


    @EventHandler
    public void onExperienceChange(PlayerExpChangeEvent event) throws ParseException, org.json.simple.parser.ParseException, IOException {    
        event.setAmount(0);
    }
    
    @EventHandler
    public void onEnchantItemEvent(EnchantItemEvent event) throws ParseException, org.json.simple.parser.ParseException, IOException {
        // Simply setting the cost to zero does not work. there are probably
        // checks downstream for this. Instead cancel out the cost.
        // None of this actually changes the bitquest xp anyway, so just make
        // things look correct for the user. This only works for the enchantment table,
        // not the anvil.
        event.getEnchanter().setLevel(event.getEnchanter().getLevel() + event.whichButton() + 1);
        
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) throws ParseException, org.json.simple.parser.ParseException, IOException {
        if(event.getFrom().getChunk()!=event.getTo().getChunk()) {
            bitQuest.updateScoreboard(event.getPlayer());
            if(!event.getFrom().getWorld().getName().endsWith("_nether") && !event.getFrom().getWorld().getName().endsWith("_end")) {
                // announce new area
                int x1=event.getFrom().getChunk().getX();
                int z1=event.getFrom().getChunk().getZ();

                int x2=event.getTo().getChunk().getX();
                int z2=event.getTo().getChunk().getZ();

                String name1=BitQuest.REDIS.get("chunk"+x1+","+z1+"name")!= null ? BitQuest.REDIS.get("chunk"+x1+","+z1+"name") : "the wilderness";
                String name2=BitQuest.REDIS.get("chunk"+x2+","+z2+"name")!= null ? BitQuest.REDIS.get("chunk"+x2+","+z2+"name") : "the wilderness";

                if(name1==null) name1="the wilderness";
                if(name2==null) name2="the wilderness";

                if(!name1.equals(name2)) {
                    if(name2.equals("the wilderness")){
                        event.getPlayer().sendMessage(ChatColor.GRAY+"[ "+name2+" ]");
                    }else{
                        event.getPlayer().sendMessage(ChatColor.YELLOW+"[ "+name2+" ]");
                    }
                }
            }
        }


    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) throws ParseException, org.json.simple.parser.ParseException, IOException {
        bitQuest.updateScoreboard(event.getPlayer());

        if (event.getItem() != null) {
            final Player player=event.getPlayer();
                if (event.getItem().getType() == Material.EYE_OF_ENDER) {
                    if (!player.hasMetadata("teleporting")) {
                        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            if (player.getBedSpawnLocation() != null) {
                                // TODO: tp player home
                                player.sendMessage(ChatColor.GREEN + "Teleporting to your bed...");
                                player.setMetadata("teleporting", new FixedMetadataValue(bitQuest, true));
                                World world = Bukkit.getWorld("world");

                                final Location spawn = player.getBedSpawnLocation();

                                Chunk c = spawn.getChunk();
                                if (!c.isLoaded()) {
                                    c.load();
                                }
                                bitQuest.getServer().getScheduler().scheduleSyncDelayedTask(bitQuest, new Runnable() {

                                    public void run() {
                                        player.teleport(spawn);
                                        player.removeMetadata("teleporting", bitQuest);
                                    }
                                }, 60L);
                            } else {
                                player.sendMessage(ChatColor.RED + "You must sleep in a bed before using the ender eye teleport");
                            }


                        }
                    }
                    event.setCancelled(true);
                }
                if (!player.hasMetadata("teleporting") && event.getItem().getType() == Material.COMPASS) {

                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    // TODO: open the tps inventory
                    player.sendMessage(ChatColor.GREEN+"Teleporting to satoshi town...");
                    player.setMetadata("teleporting", new FixedMetadataValue(bitQuest, true));
                    World world=Bukkit.getWorld("world");

                    final Location spawn=world.getHighestBlockAt(world.getSpawnLocation()).getLocation();

                    Chunk c = spawn.getChunk();
                    if (!c.isLoaded()) {
                        c.load();
                    }
                    bitQuest.getServer().getScheduler().scheduleSyncDelayedTask(bitQuest, new Runnable() {

                        public void run() {
                            player.teleport(spawn);
                            player.removeMetadata("teleporting", bitQuest);
                        }
                    }, 60L);

                }
            }
        }

    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) throws ParseException, org.json.simple.parser.ParseException, IOException {
        if(event.getDamager() instanceof LargeFireball||event.getDamager() instanceof Fireball) {
            // TODO :modify fireball damage
        } else if (event.getDamager() instanceof Player) {
            bitQuest.updateScoreboard((Player) event.getDamager());
            int maxHealth = (int) ((LivingEntity) event.getEntity()).getMaxHealth() * 2;
            int health = (int) (((LivingEntity) event.getEntity()).getHealth() - event.getDamage()) * 2;
            String name = event.getEntity().getName();
            // TODO: Show damage message
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.setDeathMessage(null);
        String spawnkey=spawnKey(event.getEntity().getLocation());
        BitQuest.REDIS.expire(spawnkey,30);
    }
    @EventHandler
    void onEntityDeath(EntityDeathEvent e) throws IOException, ParseException, org.json.simple.parser.ParseException {
        final LivingEntity entity = e.getEntity();

        int level = new Double(entity.getMaxHealth() / 4).intValue();

        if (entity instanceof Monster) {
            final String spawnkey = spawnKey(entity.getLocation());

            BitQuest.REDIS.expire(spawnkey,30000);
            System.out.println("[death] "+spawnkey+", "+level);
            if (e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                final EntityDamageByEntityEvent damage = (EntityDamageByEntityEvent) e.getEntity().getLastDamageCause();
                if (damage.getDamager() instanceof Player && level >= 1) {
                    final Player player = (Player) damage.getDamager();
                    final User user = new User(player);
                    final int money = BitQuest.rand(100, Math.min(128000,Math.max(100,level*100)));
                    final int d20=BitQuest.rand(1,20);
                    System.out.println("lastloot: "+BitQuest.REDIS.get("lastloot"));
                    try {
                        if (d20==20&&bitQuest.wallet.final_balance() > money) {


                            final Wallet userWallet = user.wallet;
                            BitQuest.REDIS.expire("balance" + player.getUniqueId().toString(), 5);


                            try {
                                if (bitQuest.wallet.payment(money, userWallet.address)) {
                                    System.out.println("[loot] " + player.getDisplayName() + ": " + money);
                                    player.sendMessage(ChatColor.GREEN + "You got " + ChatColor.BOLD + money / 100 + ChatColor.GREEN + " bits of loot!");
                                    // player.playSound(player.getLocation(), Sound.LEVEL_UP, 20, 1);
                                    if (bitQuest.messageBuilder != null) {

                                        // Create an event
                                        org.json.JSONObject sentEvent = bitQuest.messageBuilder.event(player.getUniqueId().toString(), "Loot", null);


                                        ClientDelivery delivery = new ClientDelivery();
                                        delivery.addMessage(sentEvent);

                                        MixpanelAPI mixpanel = new MixpanelAPI();
                                        mixpanel.deliver(delivery);
                                    }
                                }

                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }


                        }
                        // Add EXP
                        user.addExperience(level * 2);
                        if (bitQuest.messageBuilder != null) {

                            final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

                            //                        scheduler.runTaskAsynchronously(bitQuest, new Runnable() {
                            //
                            //
                            //                            @Override
                            //                            public void run() {
                            //                                // Create an event
                            //                                org.json.JSONObject sentEvent = bitQuest.messageBuilder.event(player.getUniqueId().toString(), "Kill", null);
                            //
                            //
                            //                                ClientDelivery delivery = new ClientDelivery();
                            //                                delivery.addMessage(sentEvent);
                            //
                            //                                MixpanelAPI mixpanel = new MixpanelAPI();
                            //                                try {
                            //                                    mixpanel.deliver(delivery);
                            //                                } catch (IOException e1) {
                            //                                    e1.printStackTrace();
                            //                                }
                            //                            }
                            //
                            //                        });

                        }
                    } catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }

            } else {
                e.setDroppedExp(0);
            }
        } else {
            e.setDroppedExp(0);
        }

    }

    String spawnKey(Location location) {
        return location.getWorld().getName()+location.getChunk().getX()+","+location.getChunk().getZ()+"spawn";

    }
    // TODO: Right now, entity spawns are cancelled, then replaced with random mob spawns. Perhaps it would be better to
    //          find a way to instead set the EntityType of the event. Is there any way to do that?
    // TODO: Magma Cubes don't get levels or custom names for some reason...
    @EventHandler
    void onEntitySpawn(org.bukkit.event.entity.CreatureSpawnEvent e) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();



        LivingEntity entity = e.getEntity();
        if (entity instanceof Monster) {

            int baselevel=1;

            if(e.getLocation().getWorld().getName().equals("world_nether")) {
                baselevel=4;
            } else if(e.getLocation().getWorld().getName().equals("world_end")) {
                baselevel=16;
            }

            // Disable mob spawners. Keep mob farmers away
            if (e.getSpawnReason() == SpawnReason.SPAWNER) {
                e.setCancelled(true);
            } else if(bitQuest.landIsClaimed(e.getLocation())==false) {
                e.setCancelled(false);
                EntityType entityType = entity.getType();
                // nerf_level makes sure high level mobs are away from the spawn
                int spawn_distance= (int)e.getLocation().getWorld().getSpawnLocation().distance(e.getLocation());
                int level=BitQuest.rand(baselevel,baselevel*8);
                if(level>baselevel) level=baselevel;
                if(level<1) level=1;



                entity.setMaxHealth(level * 4);
                entity.setHealth(level * 4);
                entity.setMetadata("level", new FixedMetadataValue(bitQuest, level));
                entity.setCustomName(String.format("%s lvl %d", WordUtils.capitalizeFully(entityType.name().replace("_", " ")), level));

                // add potion effects
                if (level>2)
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 2), true);
                if (level>4)
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2), true);
                if (level>8)
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 2), true);
                if (level>16)
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 2), true);
                if (level>32)
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2), true);
                if (level>64) {
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2), true);
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);
                }


                // give random equipment
                if (entity instanceof Zombie || entity instanceof PigZombie || entity instanceof Skeleton) {
                    useRandomEquipment(entity, level);
                }

                // some creepers are charged
                if (entity instanceof Creeper && BitQuest.rand(0, 100) < level) {
                    ((Creeper) entity).setPowered(true);
                }

                // pigzombies are always angry
                if (entity instanceof PigZombie) {
                    PigZombie pigZombie = (PigZombie) entity;
                    pigZombie.setAngry(true);
                }

                // some skeletons are black
                if (entity instanceof Skeleton) {
                    Skeleton skeleton = (Skeleton) entity;
                    ItemStack bow = new ItemStack(Material.BOW);
                    if (BitQuest.rand(0, 64) < level) {
                        randomEnchantItem(bow);
                    }
                }
                System.out.println("[spawn mob] "+entityType.name()+" lvl "+level+" spawn distance: "+spawn_distance+" buff level: "+buff_level);
            } else {
                e.setCancelled(true);
            }
        } else {
            e.setCancelled(false);
        }
    }
    @EventHandler
    void onEntityDamage(EntityDamageEvent event) throws ParseException, org.json.simple.parser.ParseException, IOException {

    	// damage by entity
    	if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
            Entity damager = damageEvent.getDamager();
            if (damager instanceof Player || (damager instanceof Arrow && ((Arrow) damager).getShooter() instanceof Player)) {
                Player player;

                if (damager instanceof Arrow) {
                    Arrow arrow = (Arrow) damager;
                    player = (Player) arrow.getShooter();
                } else {
                    player = (Player) damager;
                }

                // Player vs. Protected entities
                if (PROTECTED_ENTITIES.contains(event.getEntity().getType())) {
                    if(!bitQuest.canBuild(event.getEntity().getLocation(), player)){
                        event.setCancelled(true);
                    }
                }

                // Player vs. Animal in claimed location
                if (event.getEntity() instanceof Animals){
                    if(!bitQuest.canBuild(event.getEntity().getLocation(), player)){
                        event.setCancelled(true);
                    }
                }
                // Player vs. Villager
                if (!bitQuest.isModerator(player) && event.getEntity() instanceof Villager) {
                    event.setCancelled(true);
                }
                // PvP is always off
                if (event.getEntity() instanceof Player) {
                    event.setCancelled(true);

                }
            }
        }
    }


    public void useRandomEquipment(LivingEntity entity, int level) {

        // Gives random SWORD
        if (BitQuest.rand(0, 32) < level && !(entity instanceof Skeleton)) {
            ItemStack sword = new ItemStack(Material.WOODEN_DOOR);
            if (BitQuest.rand(0, 128) < level) sword = new ItemStack(Material.WOODEN_DOOR);
            if (BitQuest.rand(0, 128) < level) sword = new ItemStack(Material.IRON_AXE);
            if (BitQuest.rand(0, 128) < level) sword = new ItemStack(Material.WOOD_SWORD);
            if (BitQuest.rand(0, 128) < level) sword = new ItemStack(Material.IRON_SWORD);
            if (BitQuest.rand(0, 128) < level) sword = new ItemStack(Material.DIAMOND_SWORD);

            if (BitQuest.rand(0, 128) < level) randomEnchantItem(sword);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(sword);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(sword);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(sword);

            entity.getEquipment().setItemInHand(sword);
        }

        // Gives random HELMET
        if (BitQuest.rand(0, 32) < level) {
            ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
            if (BitQuest.rand(0, 128) < level) helmet = new ItemStack(Material.CHAINMAIL_HELMET);
            if (BitQuest.rand(0, 128) < level) helmet = new ItemStack(Material.IRON_HELMET);
            if (BitQuest.rand(0, 128) < level) helmet = new ItemStack(Material.DIAMOND_HELMET);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(helmet);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(helmet);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(helmet);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(helmet);

            entity.getEquipment().setHelmet(helmet);
        }

        // Gives random CHESTPLATE
        if (BitQuest.rand(0, 32) < level) {
            ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
            if (BitQuest.rand(0, 128) < level) chest = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
            if (BitQuest.rand(0, 128) < level) chest = new ItemStack(Material.IRON_CHESTPLATE);
            if (BitQuest.rand(0, 128) < level) chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(chest);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(chest);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(chest);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(chest);

            entity.getEquipment().setChestplate(chest);
        }

        // Gives random Leggings
        if (BitQuest.rand(0, 128) < level) {
            ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
            if (BitQuest.rand(0, 128) < level) leggings = new ItemStack(Material.CHAINMAIL_LEGGINGS);
            if (BitQuest.rand(0, 128) < level) leggings = new ItemStack(Material.IRON_LEGGINGS);
            if (BitQuest.rand(0, 128) < level) leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(leggings);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(leggings);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(leggings);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(leggings);

            entity.getEquipment().setLeggings(leggings);
        }

        // Gives Random BOOTS
        if (BitQuest.rand(0, 128) < level) {
            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            if (BitQuest.rand(0, 128) < level) boots = new ItemStack(Material.CHAINMAIL_BOOTS);
            if (BitQuest.rand(0, 128) < level) boots = new ItemStack(Material.IRON_BOOTS);
            if (BitQuest.rand(0, 128) < level) boots = new ItemStack(Material.DIAMOND_BOOTS);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(boots);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(boots);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(boots);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(boots);

            entity.getEquipment().setBoots(boots);
        }
    }

    // Random Enchantment
    public static void randomEnchantItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        Enchantment enchantment = null;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.ARROW_FIRE;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.DAMAGE_ALL;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.ARROW_DAMAGE;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.ARROW_INFINITE;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.ARROW_KNOCKBACK;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.DAMAGE_ARTHROPODS;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.DAMAGE_UNDEAD;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.DIG_SPEED;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.DURABILITY;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.FIRE_ASPECT;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.KNOCKBACK;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.LOOT_BONUS_BLOCKS;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.LOOT_BONUS_MOBS;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.LUCK;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.LURE;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.OXYGEN;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.PROTECTION_ENVIRONMENTAL;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.PROTECTION_EXPLOSIONS;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.PROTECTION_FALL;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.PROTECTION_PROJECTILE;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.PROTECTION_FIRE;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.SILK_TOUCH;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.THORNS;
        if (BitQuest.rand(0, 64) == 0) enchantment = Enchantment.WATER_WORKER;

        if (enchantment != null) {
            int level = BitQuest.rand(enchantment.getStartLevel(), enchantment.getMaxLevel());
            meta.addEnchant(enchantment, level, true);
            item.setItemMeta(meta);

        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) throws ParseException, org.json.simple.parser.ParseException, IOException {
        bitQuest.updateScoreboard(event.getPlayer());

        Block b = event.getClickedBlock();
        Player p = event.getPlayer();
        if(b!=null && PROTECTED_BLOCKS.contains(b.getType())) {
            // If block's inventory has "public" in it, allow the player to interact with it.
            if(b.getState() instanceof InventoryHolder) {
                Inventory blockInventory = ((InventoryHolder) b.getState()).getInventory();
                if(blockInventory.getName().toLowerCase().contains("public")) {
                    return;
                }
            }
            // If player doesn't have permission, disallow the player to interact with it.
            if(!bitQuest.canBuild(b.getLocation(),event.getPlayer())) {
                event.setCancelled(true);
                p.sendMessage(ChatColor.RED+"You don't have permission to do that!");
            }
        }

    }

    @EventHandler
    void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player p = event.getPlayer();
        if (!bitQuest.canBuild(event.getBlockClicked().getLocation(), event.getPlayer())) {
            p.sendMessage(ChatColor.RED+"You don't have permission to do that!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player p = event.getPlayer();
        if (!bitQuest.canBuild(event.getBlockClicked().getLocation(), event.getPlayer())) {
            p.sendMessage(ChatColor.RED+"You don't have permission to do that!");
            event.setCancelled(true);
        }
    }

    @EventHandler
	void onExplode(EntityExplodeEvent event) {
		event.setCancelled(true);
	}

}

