package com.bitquest.bitquest;

import com.google.gson.JsonObject;

import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by explodi on 11/7/15.
 */
public class EntityEvents implements Listener {
    BitQuest bitQuest;
    StringBuilder rawwelcome = new StringBuilder();

    public EntityEvents(BitQuest plugin) {
        bitQuest = plugin;

        for(String line : bitQuest.getConfig().getStringList("welcomeMessage")) {
        	for (ChatColor color : ChatColor.values()) {
        		line.replaceAll("<"+color.name()+">", color.toString());
        	}
        	// add links
    		final Pattern pattern = Pattern.compile("<link>(.+?)</link>");
    		final Matcher matcher = pattern.matcher(line);
    		matcher.find();
    		String link = matcher.group(1);
    		// Right here we need to replace the link variable with a minecraft-compatible link
    		line.replaceAll("<link>" + link + "<link>", link);
    		
        	rawwelcome.append(line);
    	}
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        User user=new User(event.getPlayer());
        String welcome = rawwelcome.toString();
        welcome.replace("<name>", event.getPlayer().getName());
    	event.getPlayer().sendMessage(welcome);
    }
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) throws ParseException, org.json.simple.parser.ParseException, IOException {
        User user=new User(event.getPlayer());
        if(user.getAddress()==null) {
            user.generateBitcoinAddress();
        }

    }
	@EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // announce new area
        World world = event.getPlayer().getWorld();
        if (world.getName().endsWith("_nether") == false && world.getName().endsWith("_the_end") == false) {
            JsonObject newarea = bitQuest.areaForLocation(event.getTo());
            if(newarea!=null) {
                if((event.getPlayer().hasMetadata("area")==true && newarea.get("uuid").getAsString().equals(event.getPlayer().getMetadata("area").get(0).asString())==false)||event.getPlayer().hasMetadata("area")==false) {
                    event.getPlayer().setMetadata("area",new FixedMetadataValue(bitQuest,newarea.get("uuid").getAsString()));

                    event.getPlayer().sendMessage(ChatColor.YELLOW + "[ " + newarea.get("name").getAsString() + " ]");
                }
            } else {
                if(event.getPlayer().hasMetadata("area")==true) {
                    event.getPlayer().removeMetadata("area",bitQuest);
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "[ the wilderness ]");
                }
            }

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
	public void onClick(PlayerInteractEvent event) {
		if(event.getItem().getType() == Material.COMPASS) {
			if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
				// TODO: tp player home
				bitQuest.success(event.getPlayer(), "You left-clicked a compass!");
			}
			if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				// TODO: open the tps inventory
				bitQuest.success(event.getPlayer(), "You right-clicked a compass!");
			}
		}
	}
	@EventHandler
	public void onAttack(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player) {
			int maxHealth = (int) ((LivingEntity)event.getEntity()).getMaxHealth()*2;
			int health = (int) (((LivingEntity)event.getEntity()).getHealth()-event.getDamage())*2;
			String name = event.getEntity().getName();
			event.getDamager().sendMessage(ChatColor.BOLD + name + " - " + health + "/" + maxHealth);
		}
	}
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		event.setKeepInventory(true);
		event.setKeepLevel(true);
		event.setDeathMessage(null);
	}
	// TODO: Right now, entity spawns are cancelled, then replaced with random mob spawns. Perhaps it would be better to
	//          find a way to instead set the EntityType of the event. Is there any way to do that?
	// TODO: Magma Cubes don't get levels or custom names for some reason...
    @EventHandler
    void onEntitySpawn(org.bukkit.event.entity.CreatureSpawnEvent e) {
        LivingEntity entity = e.getEntity();
        if(bitQuest.areaForLocation(e.getLocation())!=null) {
            e.setCancelled(true);
        } else if (entity instanceof Monster) {
            // Disable mob spawners. Keep mob farmers away
            if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            	e.setCancelled(true);
            	World world = e.getLocation().getWorld();
                EntityType entityType = entity.getType();
                
                int num = BitQuest.rand(0, 13);
                // change the mob type to a random type
                switch (num) {
                	case 0:
                		// if the world is nether/end, spawn a ghast. else, spawn a spider.
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
                    	entityType = EntityType.CAVE_SPIDER;
                    	break;
                    case 7:
                    	entityType = EntityType.ZOMBIE;
                    	break;
                    case 8:
                    	entityType = EntityType.SKELETON;
                    	break;
                    case 9:
                    	entityType = EntityType.CREEPER;
                    	break;
                    case 10:
                    	entityType = EntityType.ENDERMAN;
                    	break;
                    case 11:
                    	entityType = EntityType.GUARDIAN;
                    	break;
                    case 12:
                    	entityType = EntityType.ENDERMITE;
                    	break;
                    default:
                    	entityType = EntityType.SPIDER;
                    	break;
                }
                // we spawn another entity with the custom type
                // the entity should have a CUSTOM SpawnReason, so it is handled below
                world.spawnEntity(entity.getLocation(), entityType);
                
            // if spawn cause was a plugin
            } else if (e.getSpawnReason() == SpawnReason.CUSTOM) {
            	World world = e.getLocation().getWorld();
                EntityType entityType = entity.getType();

                int level = 1;

                // give a random lvl depending on world
                if (world.getName().endsWith("_nether") == true) {
                	level = BitQuest.rand(1,128);
                } else if (world.getName().endsWith("_end") == true) {
                	level = BitQuest.rand(1,64);
                } else {
                	level = BitQuest.rand(1,16);
                }

                entity.setMaxHealth(level * 4);
                entity.setHealth(level * 4);
                entity.setMetadata("level", new FixedMetadataValue(bitQuest, level));
                entity.setCustomName(String.format("%s lvl %d", WordUtils.capitalizeFully(entityType.name().replace("_", " ")), level));

                // add potion effects
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 2), true);
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2), true);
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 2), true);
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 2), true);
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2), true);
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2), true);
                if(BitQuest.rand(0,128) < level) entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);
             
                // give random equipment
                if (entity instanceof Zombie || entity instanceof PigZombie || entity instanceof Skeleton ) {
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
                	if (BitQuest.rand(0, 256) < level) {
                		skeleton.setSkeletonType(Skeleton.SkeletonType.WITHER);
                	} else {
                		ItemStack bow = new ItemStack(Material.BOW);
                		if (BitQuest.rand(0, 64) < level) {
                			randomEnchantItem(bow);
                		}
                		entity.getEquipment().setItemInHand(bow);
                	}
                }

            // if spawn cause was something else
            } else {	
                e.setCancelled(true);
                return;
            } 
        }
    }
    @EventHandler
    void onEntityDamage(EntityDamageEvent event) {
        double rawdamage = event.getDamage();

        int damagerlevel = 1;
        int damagedlevel = 1;


        // damage by entity
        if (event instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

            // damager is player
            if (((EntityDamageByEntityEvent) event).getDamager() instanceof Player) {
                Player player = (Player) ((EntityDamageByEntityEvent) event).getDamager();
                damagerlevel = player.getLevel();

                if (event.getEntity() instanceof ItemFrame) {
                    if (bitQuest.canBuild(event.getEntity().getLocation(), player) == false) {
                        event.setCancelled(true);
                    }
                }
                // Player vs. Horse
                if (event.getEntity() instanceof Horse && player.isOp() == false) {
                    Horse horse=(Horse)event.getEntity();
                    if (horse.hasMetadata("owner")) {
                        if (horse.getMetadata("owner").get(0).asString().equals(player.getUniqueId().toString())) {
                            event.setCancelled(false);
                        } else {
                            event.setCancelled(true);
                        }
                    }
                } else if (event.getEntity() instanceof Animals) {
                    if (bitQuest.canBuild(event.getEntity().getLocation(), player) == false) {
                        event.setCancelled(true);
                    }

                }
                // Player vs. Villager
                if (event.getEntity() instanceof Villager && player.isOp() == false) {
                    event.setCancelled(true);
                }

                // PvP
                if (event.getEntity() instanceof Player) {
                    // TODO: Define how PvP is going to work (arenas?)
                    event.setCancelled(true);
                }
                // damaged is monster
                if (event.getEntity() instanceof Monster) {
                    Monster monster = (Monster) event.getEntity();
                    damagedlevel = new Double(monster.getMaxHealth() / 4).intValue();

                    if (monster.hasMetadata("level")) {
                        damagedlevel = monster.getMetadata("level").get(0).asInt();
                    }

                }
            }
            // damager is monster
            if (((EntityDamageByEntityEvent) event).getDamager() instanceof Monster) {
                Monster monster = (Monster) ((EntityDamageByEntityEvent) event).getDamager();
                damagerlevel = new Double(monster.getMaxHealth() / 4).intValue();
                if (monster.hasMetadata("level")) {
                    damagerlevel = monster.getMetadata("level").get(0).asInt();

                }
                // monster vs player
                if (event.getEntity() instanceof Player) {
                    Player damaged = (Player) event.getEntity();
                    damagedlevel = damaged.getLevel();
                }
            }
            // damager is projectile
            if (((EntityDamageByEntityEvent) event).getDamager() instanceof Projectile) {
                final Projectile projectile = (Projectile) ((EntityDamageByEntityEvent) event).getDamager();

                // shooter is player
                if (projectile.getShooter() instanceof Player) {
                    Player shooter = (Player) projectile.getShooter();
                    damager = shooter;
                    damagerlevel = shooter.getLevel();

                    // shoot villagers
                    if (event.getEntity() instanceof Villager) {
                        event.setCancelled(true);
                    }
                    // shoot monsters
                    if(event.getEntity() instanceof Giant) {
                        event.setCancelled(true);
                    }
                    if (event.getEntity() instanceof Monster) {
                        Monster monster = (Monster) event.getEntity();
                        damagedlevel = new Double(monster.getMaxHealth() / 4).intValue();

                        if (monster.hasMetadata("damage")) {
                            int damage = monster.getMetadata("damage").get(0).asInt();
                            damage = damage + 1;
                            monster.setMetadata("damage", new FixedMetadataValue(bitQuest, damage));
                        } else {
                            monster.setMetadata("damage", new FixedMetadataValue(bitQuest, 1));
                        }



                    }

                }
                // shooter is monster
                if (projectile.getShooter() instanceof Monster) {
                    Monster shooter = (Monster) projectile.getShooter();

                    damager = shooter;
                    damagerlevel = new Double(shooter.getMaxHealth() / 8).intValue();
                    if (shooter.hasMetadata("level")) {
                        damagerlevel = shooter.getMetadata("level").get(0).asInt();
                    }

                }
                // shooter is ghast
                if(projectile.getShooter() instanceof Ghast) {
                    damagerlevel=32;
                }
                // do not harm
                if (projectile.getShooter() instanceof Player && event.getEntity() instanceof Horse) {
                    event.setCancelled(true);
                }
            }

            // begins to recalculate damage
            double attack = 0;
            double defense = 0;
            boolean miss=false;

            // attacker phase
            int d20 = bitQuest.rand(1, 20);

            if(d20>4) {
                // hit
                attack=damagerlevel;
                if(d20>18) {
                    attack=attack*2;
                }
            } else {
                // miss
                rawdamage=0;
                miss=true;
            }


            // victim phase
            d20 = bitQuest.rand(1, 20);
            if(d20>4) {
                // hit
                defense=damagedlevel;
            } else {
                // miss
                defense=0;
            }




            double finaldamage = attack + rawdamage - defense;

            if (finaldamage < 0) {
                finaldamage = 0;
            }
            if(miss==true) {
                event.setCancelled(true);
            }
            event.setDamage(finaldamage);

            Player player = null;

            // damage notification if is op
            if (event.getEntity() instanceof Player) {
                player = (Player) event.getEntity();

            }
            if (damager instanceof Player) {
                // adds experience per damage
                player = (Player) damager;
                int factor = 0;
                if (event.getEntity() instanceof Monster) {
                    factor = 32;
                }
                if (event.getEntity() instanceof Monster && event.getEntity().hasMetadata("boss")) {
                    factor = factor * 2;
                }


                if (finaldamage > 0 && factor > 0) {
                    (new User(player)).addExperience(damagedlevel * factor);
                }

            }
        } else {
            // damage is not done between entities
            if (event.getEntity() instanceof Monster) {
                Monster monster = (Monster) event.getEntity();
                if (monster.hasMetadata("damage")) {
                    int damage = monster.getMetadata("damage").get(0).asInt();
                    damage = damage + 1;
                    monster.setMetadata("damage", new FixedMetadataValue(bitQuest, damage));
                } else {
                    monster.setMetadata("damage", new FixedMetadataValue(bitQuest, 1));
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
