package com.bitquest.bitquest.events;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.BitQuestPlayer;
import com.bitquest.bitquest.LandChunk;
import com.bitquest.bitquest.Wallet;
import io.sentry.Sentry;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

public class EntityEvents implements Listener {
  BitQuest bitQuest;
  StringBuilder rawwelcome = new StringBuilder();
  static String PROBLEM_MESSAGE = "Can't join right now. Come back later";

  private static final List<Material> PROTECTED_BLOCKS = Arrays.asList(Material.CHEST, Material.ACACIA_DOOR,
      Material.BIRCH_DOOR, Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR, Material.SPRUCE_DOOR,
      Material.ACACIA_DOOR, Material.BIRCH_DOOR, Material.FURNACE, Material.BLAST_FURNACE,
      Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.ACACIA_FENCE_GATE,
      Material.JUNGLE_FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.DISPENSER, Material.DROPPER,
      Material.BREWING_STAND, Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX,
      Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX,
      Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX,
      Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.LEGACY_SILVER_SHULKER_BOX,
      Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX, Material.HOPPER);

  private static final List<EntityType> PROTECTED_ENTITIES = Arrays.asList(EntityType.ARMOR_STAND,
      EntityType.ITEM_FRAME, EntityType.PAINTING, EntityType.ENDER_CRYSTAL);

  // TODO: Implement PvP variables somewhere else
  // private int pvar = 0; // pvp area variable @bitcoinjake09

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
      // Right here we need to replace the link variable with a minecraft-compatible
      // link
      line = line.replaceAll("<link>" + link + "<link>", link);

      rawwelcome.append(line);
    }
  }

  @EventHandler
  public void onPlayerLogin(PlayerLoginEvent event) {
    try {
      Player player = event.getPlayer();
      // When Development mode is on (default) all users have admin access
      player.setOp(BitQuest.BITQUEST_ENV.equals("development"));
      bitQuest.redis.set("name:" + player.getUniqueId().toString(), player.getName());
      bitQuest.redis.set("uuid:" + player.getName().toString(), player.getUniqueId().toString());
      if (bitQuest.redis.sismember("banlist", event.getPlayer().getUniqueId().toString())) {
        System.out.println("kicking banned player " + event.getPlayer().getDisplayName());
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
            "You are temporarily banned. Please contact bitquest@bitquest.co");
      }
      if (bitQuest.redis.exists("rate_limit:" + event.getPlayer().getUniqueId()) == true) {
        Long ttl = bitQuest.redis.ttl("rate_limit:" + event.getPlayer().getUniqueId());
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Please try again in " + ttl + " seconds.");
      }

    } catch (Exception e) {
      e.printStackTrace();
      bitQuest.redis.set("rate_limit:" + event.getPlayer().getUniqueId(), "1");
      bitQuest.redis.expire("rate_limit:" + event.getPlayer().getUniqueId(), 60);
      event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
          "The server is in limited capacity at this moment. Please try again later.");
    }
  }

  @EventHandler
  public void onEnchantItemEvent(EnchantItemEvent event) {
    // Simply setting the cost to zero does not work. there are probably
    // checks downstream for this. Instead cancel out the cost.
    // None of this actually changes the bitquest xp anyway, so just make
    // things look correct for the user. This only works for the enchantment table,
    // not the anvil.
    // event.getEnchanter().setLevel(event.getEnchanter().getLevel() + event.whichButton() + 1);
    bitQuest.setTotalExperience(event.getEnchanter());
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event)
      throws ParseException, org.json.simple.parser.ParseException, IOException {

    if (
          event.getTo().getWorld().getEnvironment() == Environment.NORMAL && 
          event.getFrom().getChunk() != event.getTo().getChunk()) {
      try {
        String fromChunkName = bitQuest.landIsClaimed(event.getFrom()) ? bitQuest.land.chunk(event.getFrom()).name : "the wilderness";
        String toChunkName = bitQuest.landIsClaimed(event.getTo()) ? bitQuest.land.chunk(event.getTo()).name : "the wilderness";
        if (!fromChunkName.equals(toChunkName)) {
          if (toChunkName.equals("the wilderness")) {
            event.getPlayer().sendMessage(ChatColor.GRAY + "[ " + toChunkName + " ]");
          } else {
            event.getPlayer().sendMessage(ChatColor.YELLOW + "[ " + toChunkName + " ]");
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @EventHandler
  public void itemConsume(PlayerItemConsumeEvent event) {
    ItemStack item = event.getItem();
    if (item != null && item.hasItemMeta()) {
      if (item.getItemMeta() instanceof PotionMeta) {
        PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
        PotionData potionData = potionMeta.getBasePotionData();
        if (potionData.getType() == PotionType.WATER) {
          Player player = event.getPlayer();
          if (player != null) {
            PlayerInventory inventory = player.getInventory();
            ItemStack helmet = inventory.getHelmet();
            if (helmet != null && helmet.getType() == Material.PUMPKIN) {
              Map<Enchantment, Integer> enchantments = helmet.getEnchantments();
              for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                if (entry.getKey().equals(Enchantment.BINDING_CURSE)) {
                  inventory.setHelmet(null);
                  player.getWorld().dropItemNaturally(player.getLocation(), helmet);
                  player.sendMessage("You are finally free of the " + ChatColor.BOLD + ChatColor.GOLD + "Pumpkin "
                      + ChatColor.GRAY + ChatColor.ITALIC + "curse");
                }
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    event.setKeepInventory(true);
    event.setKeepLevel(true);
    event.setDeathMessage(null);
  }

  @EventHandler
  void onEntityDeath(EntityDeathEvent event) {
    final LivingEntity entity = event.getEntity();
    event.setDroppedExp(0);
    if (entity instanceof Player) {
      event.getDrops().clear();
    }
    if (entity instanceof Monster && event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
      if (entity.hasMetadata("level")) {
        int level = entity.getMetadata("level").get(0).asInt();
        if (level < 1) level = 1;
        if (level > BitQuest.maxLevel()) level = BitQuest.maxLevel();
        final EntityDamageByEntityEvent damage = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
        Player player = null;
        if (damage.getDamager() instanceof Player) {
          player = (Player) damage.getDamager();
        } else if (damage.getDamager() instanceof Arrow) {
          Arrow arrow = (Arrow) damage.getDamager();
          if (arrow.getShooter() instanceof Player) {
            player = (Player) arrow.getShooter();
            BitQuest.debug("arrow",player.getName());
          }
        }
        if (player != null) {
          // Award experience
          BitQuest.log("kill", player.getName() + " killed " + entity.getName());
          event.setDroppedExp(level);

          try {
            if (player.getLevel() < BitQuest.maxLevel()) {
              int experience = level * 10;
              if (player.getLocation().getWorld().getEnvironment() == Environment.NETHER) experience = experience * 2;
              if (player.getLocation().getWorld().getEnvironment() == Environment.THE_END) experience = experience * 4;
              bitQuest.player(player).addExperience(experience);
              bitQuest.redis.del(BitQuestPlayer.cachedExperienceKey(player));
            }
            bitQuest.setTotalExperience(player);
          } catch (Exception e) {
            e.printStackTrace();
          }

          // Award random weapon
          String randomWeaponTimerKey = "timer:randomWeapon:" + player.getUniqueId().toString();
          if (!bitQuest.redis.exists(randomWeaponTimerKey)) {
            if (BitQuest.rand(1,5) == 1) {
              event.getDrops().add(randomWeapon(level));
            } else {
              event.getDrops().add(randomArmor(level));
            }
            bitQuest.redis.set(randomWeaponTimerKey,"1");
            bitQuest.redis.expire(randomWeaponTimerKey, BitQuest.rand(60,600));
          }


          // Award loot if random timer is expired
          String lootTimerKey = "timer:loot:" + player.getUniqueId().toString();
          if (player != null && bitQuest.redis.exists(lootTimerKey) == false) {
            try {
              Double loot =  Double.valueOf(BitQuest.rand(1,level * 10));         
              if (BitQuest.rand(1,5) == 1 && bitQuest.wallet.balance(3) > loot) {
                bitQuest.redis.set(lootTimerKey, "1");
                bitQuest.redis.expire(lootTimerKey, BitQuest.rand(60,600));
                Wallet wallet = new Wallet(bitQuest.node, player.getUniqueId().toString());
                if (bitQuest.wallet.send(wallet.address(), loot)) {
                  BitQuest.log("loot", loot.toString() + " --> " + player.getDisplayName());
                  player.sendMessage(ChatColor.GREEN + " you looted " + loot.toString() + " " + BitQuest.DENOMINATION_NAME);
                  bitQuest.updateScoreboard(player);
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        } 
      }
    }
  }

  String spawnKey(Location location) {
    return location.getWorld().getName() + location.getChunk().getX() + "," + location.getChunk().getZ() + "spawn";
  }

  // TODO: Right now, entity spawns are cancelled, then replaced with random mob
  // spawns. Perhaps it
  // would be better to
  // find a way to instead set the EntityType of the event. Is there any way to do
  // that?
  // TODO: Magma Cubes don't get levels or custom names for some reason...
  @EventHandler
  void onEntitySpawn(org.bukkit.event.entity.CreatureSpawnEvent event) {
    Location location = event.getLocation();
    World world = location.getWorld();
    LivingEntity entity = event.getEntity();
    int minLevel = 1;
    int maxLevel = 10;
    int spawnDistance = (int) world.getSpawnLocation().distance(location);
    if (world.getEnvironment() == Environment.NETHER) {
      minLevel = 10;
      maxLevel = 20;
    } else if (world.getEnvironment() == Environment.THE_END) {
      minLevel = 20;
      maxLevel = 50;
    } else {
      minLevel = 1;
      maxLevel = 10;
    }

    EntityType entityType = entity.getType();
    // max level is 128
    int level = Math.min(maxLevel, BitQuest.rand(minLevel, minLevel + (spawnDistance / 200)));

    // only allow one wither per world
    if (entityType == EntityType.WITHER) {
      if (event.getLocation().getWorld().getEnvironment() != Environment.NORMAL) event.setCancelled(true);
      for (Entity e : event.getLocation().getWorld().getEntities()) {
        if (e.getType() == EntityType.WITHER) event.setCancelled(true);
      }
    }

    // Do not spawn some mobs on overworld
    if (world.getEnvironment() == Environment.NORMAL) {
      if (entity instanceof Zoglin) event.setCancelled(true);
      if (entity instanceof Phantom) event.setCancelled(true);
      if (entity instanceof Wither) event.setCancelled(true);
    }

    // Disable mob spawners
    if (event.getSpawnReason() == SpawnReason.SPAWNER) {
      event.setCancelled(true);
    }

    boolean isEnemy = false;
    if (entity instanceof Ghast) isEnemy = true;
    if (entity instanceof Giant) isEnemy = true;
    if (entity instanceof Monster) isEnemy = true;

    if (isEnemy && !event.isCancelled()) {
      // Do not spawn monsters on claimed land
      if (world.getEnvironment() == Environment.NORMAL) {
        try {
          if (bitQuest.landIsClaimed(location)) {
            event.setCancelled(true);
            return;
          }
        } catch (Exception e) {
          e.printStackTrace();
          event.setCancelled(true);
        }
      
      }
      if (event.isCancelled() == false) {
        if (level < 1) {
          level = 1;
        }
        entity.setMetadata("level", new FixedMetadataValue(bitQuest, level));
        entity.setCustomName(
            String.format("%s lvl %d", WordUtils.capitalizeFully(entityType.name().replace("_", " ")), level));
        // entity.setMaxHealth(1 + level);
        AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        attribute.setBaseValue(level);
        entity.setHealth(level);

        // add potion effects
        ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
        int amplifier = Math.max(1, level / 10);
        if (BitQuest.rand(1, 100) < level) effects.add(new PotionEffect(PotionEffectType.ABSORPTION, 999999, amplifier, false, false));
        if (BitQuest.rand(1, 100) < level) effects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, Math.min(amplifier,4), false, false));
        if (BitQuest.rand(1, 100) < level) effects.add(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 1, false, false));
        if (BitQuest.rand(1, 100) < level) effects.add(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999, Math.min(amplifier,2), false, false));
        if (BitQuest.rand(1, 100) < level) effects.add(new PotionEffect(PotionEffectType.JUMP, 999999, amplifier, false, false));
        if (BitQuest.rand(1, 100) < level) effects.add(new PotionEffect(PotionEffectType.REGENERATION, 999999, Math.min(amplifier,5), false, false));
        if (BitQuest.rand(1, 100) < level) effects.add(new PotionEffect(PotionEffectType.SPEED, 999999, amplifier, false, false));
        entity.addPotionEffects(effects);

        // give random equipment
        if (entity instanceof Zombie || entity instanceof PigZombie || entity instanceof Skeleton) {
          useRandomEquipment(entity, level);
        }

        // some creepers are charged
        if (entity instanceof Creeper && BitQuest.rand(0, level) > 5) {
          ((Creeper) entity).setPowered(true);
        }

        // pigzombies are always angry
        if (entity instanceof PigZombie) {
          PigZombie pigZombie = (PigZombie) entity;
          pigZombie.setAngry(true);
        }

        if (entity instanceof Skeleton) {
          Skeleton skeleton = (Skeleton) entity;
          ItemStack bow = new ItemStack(Material.BOW);
          randomEnchantItem(bow, level);
          skeleton.getEquipment().setItemInMainHand(bow);
        }

        // spawn extra mobs
        EntityType extraMobType = null;
        List<EntityType> overworldMobs = Arrays.asList(
            EntityType.WITCH,
            EntityType.SILVERFISH,
            EntityType.WITHER
        );
        List<EntityType> netherMobs = Arrays.asList(
            EntityType.ZOGLIN,
            EntityType.GHAST,
            EntityType.WITHER_SKELETON,
            EntityType.HOGLIN
        );
        List<EntityType> endMobs = Arrays.asList(
            EntityType.GHAST,
            EntityType.WITHER_SKELETON,
            EntityType.HOGLIN,
            EntityType.ZOGLIN,
            EntityType.PIGLIN_BRUTE,
            EntityType.RAVAGER
        );
        
        
        if (BitQuest.rand(1,20) == 1) {
          Random rand = new Random();
          if (location.getWorld().getEnvironment() == Environment.NORMAL) extraMobType = overworldMobs.get(rand.nextInt(overworldMobs.size()));
          if (location.getWorld().getEnvironment() == Environment.NETHER) extraMobType = netherMobs.get(rand.nextInt(netherMobs.size()));
          if (location.getWorld().getEnvironment() == Environment.THE_END) extraMobType = endMobs.get(rand.nextInt(endMobs.size()));

        }
        if (extraMobType != null) world.spawnEntity(location,extraMobType);
        entity.setMetadata("level", new FixedMetadataValue(bitQuest, Integer.toString(level)));
        BitQuest.log("spawn", location.getWorld().getName() + " " + entity.getCustomName());
      }
    }
    
  }

  @EventHandler
  void onEntityDamage(EntityDamageEvent event)
      throws ParseException, org.json.simple.parser.ParseException, IOException {

    // damage by entity
    if (event instanceof EntityDamageByEntityEvent) {
      EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
      Entity damager = damageEvent.getDamager();
      if (damager instanceof Arrow) {
        Arrow arrow = (Arrow) damager;
        if (arrow.getShooter() instanceof Entity) {
          damager = (Entity) arrow.getShooter();
        }
      }
      if (damager instanceof Player) {
        // player damage
        Player player;

        if (damager instanceof Arrow) {
          Arrow arrow = (Arrow) damager;
          player = (Player) arrow.getShooter();
        } else {
          player = (Player) damager;
        }

        // Player vs. Protected entities
        if (PROTECTED_ENTITIES.contains(event.getEntity().getType())) {
          if (!bitQuest.canBuild(event.getEntity().getLocation(), player)) {
            event.setCancelled(true);
          }
        }

        // Player vs. Giant
        if (event.getEntity() instanceof Giant) {
          Vector v = damager.getLocation().toVector().subtract(event.getEntity().getLocation().toVector()).normalize();
          event.getEntity().setVelocity(v);

          event.getEntity().getLocation().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.ZOMBIE);
          ((Giant) event.getEntity()).setTarget((Player) damager);
        }
        // Player vs. Animal in claimed location
        if (event.getEntity() instanceof Animals) {
          if (!bitQuest.canBuild(event.getEntity().getLocation(), player)) {
            event.setCancelled(true);
          }
        }
        // Player vs. Villager
        if (!bitQuest.isModerator(player) && event.getEntity() instanceof Villager) {
          event.setCancelled(true);

        } else if (event.getEntity() instanceof Player) {
          // PvP is off in overworld and nether

          if (event.getEntity().getWorld().getName().endsWith("_end")) {
            event.setCancelled(false);
          } else {
            event.setCancelled(true);
          }
        }
        if (event.getEntity() instanceof LivingEntity) {
          LivingEntity damaged = (LivingEntity)event.getEntity();
          System.out.println(damager.getName() + " -> " + event.getDamage() + " " + damaged.getHealth() + "/" + damaged.getMaxHealth());
        }
      } else {
        if (damageEvent.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
          Projectile p = (Projectile) damageEvent.getDamager(); // Cast projectile to
          if (p.getShooter() instanceof Ghast) {
            damageEvent.setDamage(200.0f);
          }
        }
      }
    }
  }

  public void useRandomEquipment(LivingEntity entity, int level) {

    // Random sword
    if (!(entity instanceof Skeleton)) {
      Material swordMaterial = null;
      if (BitQuest.rand(0, level) > 2) swordMaterial = Material.WOODEN_SWORD;
      if (BitQuest.rand(0, level) > 4) swordMaterial = Material.STONE_SWORD;
      if (BitQuest.rand(0, level) > 6) swordMaterial = Material.GOLDEN_SWORD;
      if (BitQuest.rand(0, level) > 8) swordMaterial = Material.IRON_SWORD;
      if (BitQuest.rand(0, level) > 10) swordMaterial = Material.DIAMOND_SWORD;
      if (BitQuest.rand(0, level) > 12) swordMaterial = Material.NETHERITE_SWORD;
      if (swordMaterial != null) {
        ItemStack sword = new ItemStack(swordMaterial);
        randomEnchantItem(sword, level);
        entity.getEquipment().setItemInMainHand(sword);
      }
    }

    // Random helmet
    Material helmetMaterial = null;

    if (BitQuest.rand(0, level) > 2) helmetMaterial = Material.LEATHER_HELMET;
    if (BitQuest.rand(0, level) > 4) helmetMaterial = Material.CHAINMAIL_HELMET;
    if (BitQuest.rand(0, level) > 6) helmetMaterial = Material.GOLDEN_HELMET;
    if (BitQuest.rand(0, level) > 8) helmetMaterial = Material.IRON_HELMET;
    if (BitQuest.rand(0, level) > 10) helmetMaterial = Material.DIAMOND_HELMET;
    if (BitQuest.rand(0, level) > 12) helmetMaterial = Material.NETHERITE_HELMET;
    if (helmetMaterial != null) {
      ItemStack helmet = new ItemStack(helmetMaterial);
      randomEnchantItem(helmet, level);
      entity.getEquipment().setHelmet(helmet);
    }

    // Gives random CHESTPLATE
    Material chestplateMaterial = null;
    if (BitQuest.rand(0, level) > 2) chestplateMaterial = Material.LEATHER_CHESTPLATE;
    if (BitQuest.rand(0, level) > 4) chestplateMaterial = Material.CHAINMAIL_CHESTPLATE;
    if (BitQuest.rand(0, level) > 6) chestplateMaterial = Material.GOLDEN_CHESTPLATE;
    if (BitQuest.rand(0, level) > 8) chestplateMaterial = Material.IRON_CHESTPLATE;
    if (BitQuest.rand(0, level) > 10) chestplateMaterial = Material.DIAMOND_CHESTPLATE;
    if (BitQuest.rand(0, level) > 12) chestplateMaterial = Material.NETHERITE_CHESTPLATE;
    if (chestplateMaterial != null) {
      ItemStack chest = new ItemStack(chestplateMaterial);
      randomEnchantItem(chest, level);
      entity.getEquipment().setChestplate(chest);
    }

    // Gives random Leggings
    Material leggingsMaterial = null;
    if (BitQuest.rand(0, level) > 2) leggingsMaterial = Material.LEATHER_LEGGINGS;
    if (BitQuest.rand(0, level) > 4) leggingsMaterial = Material.CHAINMAIL_LEGGINGS;
    if (BitQuest.rand(0, level) > 6) leggingsMaterial = Material.GOLDEN_LEGGINGS;
    if (BitQuest.rand(0, level) > 8) leggingsMaterial = Material.IRON_LEGGINGS;
    if (BitQuest.rand(0, level) > 10) leggingsMaterial = Material.DIAMOND_LEGGINGS;
    if (BitQuest.rand(0, level) > 12) leggingsMaterial = Material.NETHERITE_LEGGINGS;
    if (leggingsMaterial != null) {
      ItemStack leggings = new ItemStack(leggingsMaterial);
      randomEnchantItem(leggings, level);
      entity.getEquipment().setLeggings(leggings);
    }

    // Gives Random BOOTS
    Material bootMaterial = null;
    if (BitQuest.rand(0, level) > 2) bootMaterial = Material.LEATHER_BOOTS;
    if (BitQuest.rand(0, level) > 4) bootMaterial = Material.CHAINMAIL_BOOTS;
    if (BitQuest.rand(0, level) > 6) bootMaterial = Material.GOLDEN_BOOTS;
    if (BitQuest.rand(0, level) > 8) bootMaterial = Material.IRON_BOOTS;
    if (BitQuest.rand(0, level) > 10) bootMaterial = Material.DIAMOND_BOOTS;
    if (BitQuest.rand(0, level) > 12) bootMaterial = Material.NETHERITE_BOOTS;
    if (bootMaterial != null) {
      ItemStack boots = new ItemStack(bootMaterial);
      randomEnchantItem(boots, level);
      entity.getEquipment().setBoots(boots);
    }
  }

  public static boolean isArmour(ItemStack stack) {
    if (stack.getType() == Material.DIAMOND_LEGGINGS) return true;
    if (stack.getType() == Material.DIAMOND_BOOTS) return true;
    if (stack.getType() == Material.DIAMOND_CHESTPLATE) return true;
    if (stack.getType() == Material.DIAMOND_HELMET) return true;
    if (stack.getType() == Material.IRON_HELMET) return true;
    if (stack.getType() == Material.IRON_BOOTS) return true;
    if (stack.getType() == Material.IRON_CHESTPLATE) return true;
    if (stack.getType() == Material.IRON_LEGGINGS) return true;
    if (stack.getType() == Material.CHAINMAIL_HELMET) return true;
    if (stack.getType() == Material.CHAINMAIL_BOOTS) return true;
    if (stack.getType() == Material.CHAINMAIL_CHESTPLATE) return true;
    if (stack.getType() == Material.CHAINMAIL_LEGGINGS) return true;
    if (stack.getType() == Material.LEATHER_HELMET) return true;
    if (stack.getType() == Material.LEATHER_BOOTS) return true;
    if (stack.getType() == Material.LEATHER_CHESTPLATE) return true;
    if (stack.getType() == Material.LEATHER_LEGGINGS) return true;    
    return false;
  }

  public static boolean isWeapon(ItemStack stack) {
    if (stack.getType() == Material.NETHERITE_SWORD) return true;
    if (stack.getType() == Material.DIAMOND_SWORD) return true;
    if (stack.getType() == Material.GOLDEN_SWORD) return true;
    if (stack.getType() == Material.IRON_SWORD) return true;
    if (stack.getType() == Material.WOODEN_SWORD) return true;
    return false;
  }

  public static boolean isPickaxe(ItemStack stack) {
    if (stack.getType() == Material.IRON_PICKAXE) return true;
    if (stack.getType() == Material.STONE_PICKAXE) return true;
    if (stack.getType() == Material.GOLDEN_PICKAXE) return true;
    if (stack.getType() == Material.NETHERITE_PICKAXE) return true;
    if (stack.getType() == Material.DIAMOND_PICKAXE) return true;
    return false;
  }
  
  // Random Enchantment
  public static void randomEnchantItem(ItemStack item, int level) {
    ItemMeta meta = item.getItemMeta();
    ArrayList<Enchantment> enchantments = new ArrayList<Enchantment>();
    if (item.getType() == Material.BOW) {
      if (BitQuest.rand(1, level) > 2) enchantments.add(Enchantment.ARROW_DAMAGE);
      if (BitQuest.rand(1, level) > 4) enchantments.add(Enchantment.ARROW_FIRE);
      if (BitQuest.rand(1, level) > 6) enchantments.add(Enchantment.FIRE_ASPECT);
    }
    if (item.getType() == Material.DIAMOND_SHOVEL) {
      if (BitQuest.rand(1, level) > 10) enchantments.add(Enchantment.DIG_SPEED);
    }
    if (isWeapon(item)) {
      if (BitQuest.rand(1, level) > 2) enchantments.add(Enchantment.DAMAGE_ARTHROPODS);
      if (BitQuest.rand(1, level) > 4) enchantments.add(Enchantment.DAMAGE_UNDEAD);
      if (BitQuest.rand(1, level) > 8) enchantments.add(Enchantment.DAMAGE_ALL);
      if (BitQuest.rand(1, level) > 10) enchantments.add(Enchantment.FIRE_ASPECT);
      if (BitQuest.rand(1, level) > 12) enchantments.add(Enchantment.KNOCKBACK);
      if (BitQuest.rand(1, level) > 14) enchantments.add(Enchantment.LOOT_BONUS_MOBS);
    }
    if (isPickaxe(item)) {
      if (BitQuest.rand(1, level) > 10) enchantments.add(Enchantment.DURABILITY);
      if (BitQuest.rand(1, level) > 12) enchantments.add(Enchantment.LUCK);
      if (BitQuest.rand(1, level) > 14) enchantments.add(Enchantment.LOOT_BONUS_BLOCKS);
      if (BitQuest.rand(1, level) > 16) enchantments.add(Enchantment.SILK_TOUCH);
    }
    if (isArmour(item)) {
      if (BitQuest.rand(1, level) > 2) enchantments.add(Enchantment.OXYGEN);
      if (BitQuest.rand(1, level) > 4) enchantments.add(Enchantment.PROTECTION_ENVIRONMENTAL);
      if (BitQuest.rand(1, level) > 6) enchantments.add(Enchantment.PROTECTION_EXPLOSIONS);
      if (BitQuest.rand(1, level) > 8) enchantments.add(Enchantment.PROTECTION_FALL);
      if (BitQuest.rand(1, level) > 10) enchantments.add(Enchantment.PROTECTION_PROJECTILE);
      if (BitQuest.rand(1, level) > 12) enchantments.add(Enchantment.ARROW_KNOCKBACK);
      if (BitQuest.rand(1, level) > 14) enchantments.add(Enchantment.PROTECTION_FIRE);
      if (BitQuest.rand(1, level) > 16) enchantments.add(Enchantment.DEPTH_STRIDER);
      if (BitQuest.rand(1, level) > 18) enchantments.add(Enchantment.WATER_WORKER);
      if (BitQuest.rand(1, level) > 20) enchantments.add(Enchantment.THORNS);
    }
    for (Enchantment enchantment : enchantments) {
      meta.addEnchant(enchantment, BitQuest.rand(enchantment.getStartLevel(), enchantment.getMaxLevel()), true);
    }
    item.setItemMeta(meta);
  }

  // Random Weapon
  public static ItemStack randomWeapon(final int level) {
    Material material = Material.BOW;
    if (BitQuest.rand(1, 5) < level) material = Material.WOODEN_SWORD;
    if (BitQuest.rand(1, 10) < level) material = Material.IRON_SWORD;
    if (BitQuest.rand(1, 25) < level) material = Material.DIAMOND_SWORD;
    if (BitQuest.rand(1, 100) < level) material = Material.NETHERITE_SWORD;
    ItemStack itemStack = new ItemStack(material,1);
    randomEnchantItem(itemStack, level);
    return itemStack;
  }

  // Random Armor

  public static ItemStack randomArmor(final int level) {
    Material material = Material.LEATHER_BOOTS;
    if (BitQuest.rand(1, level) > 1) material = Material.LEATHER_HELMET;
    if (BitQuest.rand(1, level) > 1) material = Material.LEATHER_LEGGINGS;
    if (BitQuest.rand(1, level) > 1) material = Material.LEATHER_CHESTPLATE;
    if (BitQuest.rand(1, level) > 2) material = Material.CHAINMAIL_CHESTPLATE;
    if (BitQuest.rand(1, level) > 2) material = Material.CHAINMAIL_BOOTS;
    if (BitQuest.rand(1, level) > 2) material = Material.CHAINMAIL_HELMET;
    if (BitQuest.rand(1, level) > 2) material = Material.CHAINMAIL_LEGGINGS;
    if (BitQuest.rand(1, level) > 4) material = Material.IRON_CHESTPLATE;
    if (BitQuest.rand(1, level) > 4) material = Material.IRON_BOOTS;
    if (BitQuest.rand(1, level) > 4) material = Material.IRON_HELMET;
    if (BitQuest.rand(1, level) > 4) material = Material.IRON_LEGGINGS;
    if (BitQuest.rand(1, level) > 8) material = Material.DIAMOND_CHESTPLATE;
    if (BitQuest.rand(1, level) > 8) material = Material.DIAMOND_BOOTS;
    if (BitQuest.rand(1, level) > 8) material = Material.DIAMOND_HELMET;
    if (BitQuest.rand(1, level) > 8) material = Material.DIAMOND_LEGGINGS;
    if (BitQuest.rand(1, level) > 16) material = Material.NETHERITE_BOOTS;
    if (BitQuest.rand(1, level) > 16) material = Material.NETHERITE_HELMET;
    if (BitQuest.rand(1, level) > 16) material = Material.NETHERITE_LEGGINGS;
    if (BitQuest.rand(1, level) > 16) material = Material.NETHERITE_CHESTPLATE;
    BitQuest.log("randomArmor", material.name() + "level " + level);
    ItemStack itemStack = new ItemStack(material,1);
    randomEnchantItem(itemStack, level);
    return itemStack;
  }

  @EventHandler
  public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
    Player player = event.getPlayer();
    ArmorStand stand = event.getRightClicked();

    if (!bitQuest.canBuild(stand.getLocation(), player)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    Entity entity = event.getRightClicked();

    if (PROTECTED_ENTITIES.contains(entity.getType())) {
      if (!bitQuest.canBuild(entity.getLocation(), player)) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Block b = event.getClickedBlock();
    Player p = event.getPlayer();
    List<Material> allowedBlocks = Arrays.asList(
        Material.CRAFTING_TABLE
    );
    if (b != null) {
      if (allowedBlocks.contains(b.getType())) {
        // Some blocks are allowed to use on land claimed by other players
        event.setCancelled(false);
      } else {
        // If player doesn't have permission, disallow the player to interact with it.
        try {
          if (!bitQuest.canBuild(b.getLocation(), event.getPlayer())) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.DARK_RED + "You don't have permission to do that!");
          }
        } catch (Exception e) {
          e.printStackTrace();
          Sentry.captureException(e);
          event.setCancelled(true);
        }
      }
    }
  }

  @EventHandler
  void onPlayerBucketFill(PlayerBucketFillEvent event) {
    Player p = event.getPlayer();
    if (!bitQuest.canBuild(event.getBlockClicked().getLocation(), event.getPlayer())) {
      p.sendMessage(ChatColor.DARK_RED + "You don't have permission to do that!");
      event.setCancelled(true);
    }
  }


  @EventHandler
  void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
    Player p = event.getPlayer();
    if (!bitQuest.canBuild(event.getBlockClicked().getLocation(), event.getPlayer())) {
      p.sendMessage(ChatColor.DARK_RED + "You don't have permission to do that!");
      event.setCancelled(true);
    }
  }

  @EventHandler
  void onExplode(EntityExplodeEvent event) {
    event.setCancelled(true);
  }
}
