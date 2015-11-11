package com.bitquest.bitquest;

import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by explodi on 11/7/15.
 */
public class EntityEvents implements Listener {
    BitQuest bitQuest;
    StringBuilder welcome = new StringBuilder();

    public EntityEvents(BitQuest plugin) {
        bitQuest = plugin;

        for(String line : bitQuest.getConfig().getStringList("welcomeMessage")) {
        	for (ChatColor color : ChatColor.values()) {
        		line.replaceAll("<"+color.name()+">", color.toString());
        	}
        	welcome.append(line);
    	}
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        User user=new User(event.getPlayer());
    	event.getPlayer().sendMessage(welcome.toString());
    }
	@EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // announce new area
        World world = event.getPlayer().getWorld();
        if (world.getName().endsWith("_nether") == false && world.getName().endsWith("_the_end") == false) {
            JsonObject newarea = bitQuest.areaForLocation(event.getTo());
            JsonObject oldarea = bitQuest.areaForLocation(event.getFrom());
            if ((oldarea==null && newarea!=null)||(oldarea!=null&&newarea==null)) {
                if (newarea == null) {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "[ the wilderness ]");
                } else {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "[ " + newarea.get("name").getAsString() + " ]");
                }
            }
        }
    }
    @EventHandler
    void onEntitySpawn(org.bukkit.event.entity.CreatureSpawnEvent e) {
        Location location = e.getLocation();
        LivingEntity entity = e.getEntity();
        World world = e.getLocation().getWorld();

        if (entity instanceof Monster && e.isCancelled() == false) {
            // Disable mob spawners. Keep mob farmers away
            if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
                e.setCancelled(true);
                return;
            }

            int level=1;

            EntityType entityType = entity.getType();

            // Ghasts somehow work in a totally different wat of Monsters
            // MAYBE TODO: Make ghasts more difficult and rewarding
            if (entity instanceof Ghast) {
            	// TODO: Remove this SpawnReason check, it is redundant
                if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                    e.setCancelled(true);
                    return;
                }
                level = BitQuest.rand(32,64);
                entity.setMetadata("level", new FixedMetadataValue(bitQuest, level));
                entity.setCustomName(String.format("%s lvl %d", e.getEntityType().name(), level));

            }

            // monsters
            if (entity instanceof Monster) {
            	// TODO: Remove this SpawnReason check, it is redundant
                if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                    e.setCancelled(true);
                    return;
                }
                // TODO: Remove this event cancellation check, it is redundant
                if (e.isCancelled() == false) {

                    if (world.getName().endsWith("_nether") == true) {
                        level = BitQuest.rand(1,128);
                    } else if (world.getName().endsWith("_end") == true) {
                        level = BitQuest.rand(1,32);
                    } else {
                        level = BitQuest.rand(1,8);
                    }

                    entity.setMaxHealth(level * 4);
                    entity.setHealth(level * 4);
                    entity.setMetadata("level", new FixedMetadataValue(bitQuest, level));
                    entity.setCustomName(String.format("%s lvl %d", e.getEntityType().name().toLowerCase().replace("_", " "), level));
                }

                // add potion effects
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 2), true);
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2), true);
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 2), true);
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 2), true);
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2), true);
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2), true);
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);

                if (entityType == EntityType.ZOMBIE) {
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), true);
                }
                // some creepers are charged
                if (entityType == EntityType.CREEPER && BitQuest.rand(0, 100) < level) {
                    ((Creeper) entity).setPowered(true);
                }
                // pigzombies are always angry
                if (entity instanceof PigZombie) {
                    PigZombie pigZombie = (PigZombie) entity;
                    pigZombie.setAnger(Integer.MAX_VALUE);
                    ItemStack sword = new ItemStack(Material.GOLD_SWORD);
                    entity.getEquipment().setItemInHand(sword);
                    useRandomEquipment(entity, level);
                }

                // some skeletons are black
                if (entity instanceof Skeleton) {
                    Skeleton skeleton = (Skeleton) entity;
                    if (BitQuest.rand(0, 256) < level) {
                        skeleton.setSkeletonType(Skeleton.SkeletonType.WITHER);
                        ItemStack sword = new ItemStack(Material.IRON_SWORD);
                        useRandomEquipment(skeleton, level);
                    } else {
                        ItemStack bow = new ItemStack(Material.BOW);
                        if (BitQuest.rand(0, 64) < level) {
                            randomEnchantItem(bow);
                        }
                        entity.getEquipment().setItemInHand(bow);
                    }
                }
                // give random equipment
                if (entityType == EntityType.ZOMBIE || e.getEntityType() == EntityType.PIG_ZOMBIE) {
                    useRandomEquipment(entity, level);
                }
                // blazes spawn everywhere on the spawn
                // TODO: Remove this SpawnReason check, it is redundant
                if (entityType == EntityType.PIG_ZOMBIE && e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
                    e.getLocation().getWorld().spawnEntity(location, EntityType.BLAZE);
                }

                if(e.getEntity() instanceof Giant) {
                    if(e.getEntity().hasMetadata("boss")==false) {
                        e.setCancelled(true);
                    }
                }
                // spawn random mobs
                if (e.getEntity() instanceof Monster) {
                    if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL)
                        return;

                        Location spawn = e.getLocation();
                        int num = BitQuest.rand(0, 15);
                        world = spawn.getWorld();

                        switch (num) {
                            case 0:
                                if (world.getName().endsWith("_nether") == true || world.getName().endsWith("_the_end") == true) {
                                    entityType = EntityType.GHAST;
                                } else {
                                    entityType = EntityType.SPIDER;
                                }
                                break;
                            case 1:
                                entityType = EntityType.WITCH;
                                break;
                            case 2:
                                entityType = EntityType.PIG_ZOMBIE;
                                break;
                            case 3:
                                entityType = EntityType.MAGMA_CUBE;
                                break;
                            case 4:
                                entityType = EntityType.BLAZE;
                                break;
                            case 5:
                                entityType = EntityType.SILVERFISH;
                                break;
                            case 6:
                                entityType = EntityType.SILVERFISH;
                                break;
                            case 7:
                                entityType = EntityType.CAVE_SPIDER;
                                break;
                            case 8:
                                entityType = EntityType.ZOMBIE;
                                break;
                            case 9:
                                entityType = EntityType.SKELETON;
                                break;
                            case 10:
                                entityType = EntityType.CREEPER;
                                break;
                            case 11:
                                entityType = EntityType.ENDERMAN;
                                break;
                            case 12:
                                entityType = EntityType.GUARDIAN;
                                break;
                            case 13:
                                entityType = EntityType.ENDERMITE;
                                break;
                            case 14:
                                entityType = EntityType.GIANT;
                                break;
                            default:
                                entityType = EntityType.SPIDER;
                                break;
                        }
                        e.getLocation().getWorld().spawnEntity(spawn, entityType);

                }
            }
        }
    }
    public void useRandomEquipment(LivingEntity entity, int level) {
    	
        // give sword
        if (BitQuest.rand(0, 32) < level) {
            ItemStack sword=new ItemStack(Material.WOODEN_DOOR);
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

        // give helmet
        if (BitQuest.rand(0, 32) < level) {
            ItemStack helmet=new ItemStack(Material.LEATHER_HELMET);
            if (BitQuest.rand(0, 128) < level) helmet = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
            if (BitQuest.rand(0, 128) < level) helmet = new ItemStack(Material.IRON_HELMET);
            if (BitQuest.rand(0, 128) < level) helmet = new ItemStack(Material.DIAMOND_HELMET);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(helmet);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(helmet);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(helmet);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(helmet);
            entity.getEquipment().setHelmet(helmet);
        }

        // give chestplate
        if (BitQuest.rand(0, 32) < level) {
            ItemStack chest=new ItemStack(Material.LEATHER_CHESTPLATE);
            if (BitQuest.rand(0, 128) < level) chest = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
            if (BitQuest.rand(0, 128) < level) chest = new ItemStack(Material.IRON_CHESTPLATE);
            if (BitQuest.rand(0, 128) < level) chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(chest);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(chest);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(chest);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(chest);

            entity.getEquipment().setChestplate(chest);
        }

        // give leggings
        if (BitQuest.rand(0, 128) < level) {
            ItemStack leggings=new ItemStack(Material.LEATHER_LEGGINGS);
            if (BitQuest.rand(0, 128) < level) leggings = new ItemStack(Material.CHAINMAIL_LEGGINGS);
            if (BitQuest.rand(0, 128) < level) leggings = new ItemStack(Material.IRON_LEGGINGS);
            if (BitQuest.rand(0, 128) < level) leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(leggings);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(leggings);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(leggings);
            if (BitQuest.rand(0, 128) < level) randomEnchantItem(leggings);

            entity.getEquipment().setLeggings(leggings);
        }

        // give boots
        if (BitQuest.rand(0, 128) < level) {
            ItemStack boots=new ItemStack(Material.LEATHER_BOOTS);
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
    
    // enchant an item
    public static void randomEnchantItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        Enchantment enchantment=null;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.ARROW_FIRE;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.DAMAGE_ALL;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.ARROW_DAMAGE;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.ARROW_INFINITE;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.ARROW_KNOCKBACK;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.DAMAGE_ARTHROPODS;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.DAMAGE_UNDEAD;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.DIG_SPEED;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.DURABILITY;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.FIRE_ASPECT;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.KNOCKBACK;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.LOOT_BONUS_BLOCKS;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.LOOT_BONUS_MOBS;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.LUCK;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.LURE;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.OXYGEN;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.PROTECTION_ENVIRONMENTAL;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.PROTECTION_EXPLOSIONS;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.PROTECTION_FALL;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.PROTECTION_PROJECTILE;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.PROTECTION_FIRE;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.SILK_TOUCH;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.THORNS;
        if (BitQuest.rand(0,64)==0) enchantment=Enchantment.WATER_WORKER;

        if (enchantment!=null) {
            int level=BitQuest.rand(enchantment.getStartLevel(),enchantment.getMaxLevel());
            meta.addEnchant(enchantment, level, true);
            item.setItemMeta(meta);

        }
    }
}
