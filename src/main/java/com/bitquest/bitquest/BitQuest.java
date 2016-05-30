package com.bitquest.bitquest;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.mixpanel.mixpanelapi.MessageBuilder;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by explodi on 11/1/15.
 */

public class BitQuest extends JavaPlugin {
    // Connecting to REDIS
    // Links to the administration account via Environment Variables

    public final static UUID ADMIN_UUID = System.getenv("ADMIN_UUID") != null ? UUID.fromString(System.getenv("ADMIN_UUID")) : null;
    public final static String BITCOIN_ADDRESS = System.getenv("BITCOIN_ADDRESS") != null ? System.getenv("BITCOIN_ADDRESS") : null;
    public final static String BITCOIN_PRIVATE_KEY = System.getenv("BITCOIN_PRIVATE_KEY") != null ? System.getenv("BITCOIN_PRIVATE_KEY") : null;
    public final static String BLOCKCYPHER_API_KEY = System.getenv("BLOCKCYPHER_API_KEY") != null ? System.getenv("BLOCKCYPHER_API_KEY") : null;
    public final static String LAND_BITCOIN_ADDRESS = System.getenv("LAND_BITCOIN_ADDRESS") != null ? System.getenv("LAND_BITCOIN_ADDRESS") : null;
    // Support for statsd is planned but not implemented
    public final static String STATSD_HOST = System.getenv("STATSD_HOST") != null ? System.getenv("STATSD_HOST") : null;
    public final static String STATSD_PREFIX = System.getenv("STATSD_PREFIX") != null ? System.getenv("STATSD_PREFIX") : null;
    public final static String STATSD_PORT = System.getenv("STATSD_PORT") != null ? System.getenv("STATSD_PORT") : null;
    // Support for mixpanel analytics
    public final static String MIXPANEL_TOKEN = System.getenv("MIXPANEL_TOKEN") != null ? System.getenv("MIXPANEL_TOKEN") : null;
    public MessageBuilder messageBuilder;
    // REDIS: Look for Environment variables on hostname and port, otherwise defaults to localhost:6379
    public final static String REDIS_HOST = System.getenv("REDIS_1_PORT_6379_TCP_ADDR") != null ? System.getenv("REDIS_1_PORT_6379_TCP_ADDR") : "localhost";
    public final static Integer REDIS_PORT = System.getenv("REDIS_1_PORT_6379_TCP_PORT") != null ? Integer.parseInt(System.getenv("REDIS_1_PORT_6379_TCP_PORT")) : 6379;
    public final static Jedis REDIS = new Jedis(REDIS_HOST, REDIS_PORT);
    // FAILS
    // public final static JedisPool REDIS_POOL = new JedisPool(new JedisPoolConfig(), REDIS_HOST, REDIS_PORT);


    // TODO: Find out why this crashes the server
    // public static ScoreboardManager manager = Bukkit.getScoreboardManager();
    // public static Scoreboard scoreboard = manager.getNewScoreboard();
    public final static int LAND_PRICE=20000;
    // utilities: distance and rand
    public static int distance(Location location1, Location location2) {
        return (int) location1.distance(location2);
    }

    public static int rand(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    public Wallet wallet=null;

    @Override
    public void onEnable() {
        log("BitQuest starting");

        if (ADMIN_UUID == null) {
            log("Warning: You haven't designated a super admin. Launch with ADMIN_UUID env variable to set.");
        }
        // registers listener classes
        getServer().getPluginManager().registerEvents(new ChatEvents(this), this);
        getServer().getPluginManager().registerEvents(new BlockEvents(this), this);
        getServer().getPluginManager().registerEvents(new EntityEvents(this), this);
        getServer().getPluginManager().registerEvents(new InventoryEvents(this), this);
        getServer().getPluginManager().registerEvents(new SignEvents(this), this);
        getServer().getPluginManager().registerEvents(new ServerEvents(this), this);

        // loads config file. If it doesn't exist, creates it.
        // get plugin config
        getDataFolder().mkdir();
        if (!new java.io.File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
        if(BITCOIN_ADDRESS!=null && BITCOIN_PRIVATE_KEY!=null) {
            wallet=new Wallet(BITCOIN_ADDRESS,BITCOIN_PRIVATE_KEY);
            System.out.println("World wallet address is: "+BITCOIN_ADDRESS);
        } else {
            System.out.println("Warning: world wallet address not defined in environment");
        }
        REDIS.configSet("SAVE","900 1 300 10 60 10000");
        if(MIXPANEL_TOKEN!=null) {
            messageBuilder = new MessageBuilder(MIXPANEL_TOKEN);
            System.out.println("Mixpanel support is on");
        }
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()){
                    User user= null;
                    try {
                        user = new User(player);
                       // user.createScoreBoard();
                        user.updateScoreboard();

                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (org.json.simple.parser.ParseException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO: Handle rate limiting
                    }
                }
            }
        }, 0, 120L);
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                // A villager is born
                World world=Bukkit.getWorld("world");
                world.spawnEntity(world.getHighestBlockAt(world.getSpawnLocation()).getLocation(), EntityType.VILLAGER);
            }
        }, 0, 100000L);
        REDIS.set("lastloot","nobody");

    }

    public void log(String msg) {
        Bukkit.getLogger().info(msg);
    }

    public void success(Player recipient, String msg) {
        recipient.sendMessage(ChatColor.GREEN + msg);
       // recipient.playSound(recipient.getLocation(), Sound.ORB_PICKUP, 20, 1);
    }

    public void error(Player recipient, String msg) {
        recipient.sendMessage(ChatColor.RED + msg);
       // recipient.playSound(recipient.getLocation(), Sound.ANVIL_LAND, 7, 1);
    }

    public JsonObject areaForLocation(Location location) {
        List<String> areas = REDIS.lrange("areas", 0, -1);
        for (String areaJSON : areas) {
            JsonObject area = new JsonParser().parse(areaJSON).getAsJsonObject();
            int x = area.get("x").getAsInt();
            int z = area.get("z").getAsInt();
            int size = area.get("size").getAsInt();
            if (location.getX() > (x - size) && location.getX() < (x + size) && location.getZ() > (z - size) && location.getZ() < (z + size)) {
                return area;
            }

        }
        return null;
    }

    public boolean canBuild(Location location, Player player) {
        ArrayList claimusers = REDIS.get("chunk"+location.getChunk().getX()+","+location.getChunk().getZ()+"builders")
        // Declared up here to make it less messy.
        final String playeruuid = player.getUniqueId().toString();
        // returns true if player has permission to build in location
        // TODO: Find out how are we gonna deal with clans and locations, and how/if they are gonna share land resources
        if(isModerator(player)==true) {
            return true;
        } else if (!location.getWorld().getEnvironment().equals(Environment.NORMAL)) {
        	// If theyre not in the overworld, they cant build
        	return false;
        } else if (REDIS.get("chunk"+location.getChunk().getX()+","+location.getChunk().getZ()+"owner")!=null) {
            if (REDIS.get("chunk"+location.getChunk().getX()+","+location.getChunk().getZ()+"owner").equals(player.getUniqueId().toString())) {
                return true;
            } else if (claimusers.contains(playeruuid)) {
              return true;
              }
            else {
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean createNewArea(Location location, Player owner, String name, int size) {
        // write the new area to REDIS
        JsonObject areaJSON = new JsonObject();
        ArrayList claimusers = new ArrayList();
        areaJSON.addProperty("size", size);
        areaJSON.addProperty("owner", owner.getUniqueId().toString());
        areaJSON.addProperty("name", name);
        areaJSON.addProperty("x", location.getX());
        areaJSON.addProperty("z", location.getZ());
        areaJSON.addProperty("uuid", UUID.randomUUID().toString());
        areaJSON.addProperty("builders", claimusers);
        REDIS.lpush("areas", areaJSON.toString());
        // TODO: Check if redis actually appended the area to list and return the success of the operation
        return true;
    }

    public boolean isModerator(Player player) {
            if(REDIS.sismember("moderators",player.getUniqueId().toString())==true) {
                return true;
            } else if(ADMIN_UUID!=null && player.getUniqueId().toString().equals(ADMIN_UUID.toString())) {
                return true;
            }
            return false;

    }

    final int minLandSize = 1;
    final int maxLandSize = 512;
    final int minNameSize = 3;
    final int maxNameSize = 16;

    public void sendWalletInfo(User user) throws ParseException, org.json.simple.parser.ParseException, IOException {
        BitQuest.REDIS.del("balance"+user.player.getUniqueId().toString());

        user.player.sendMessage(ChatColor.BOLD+""+ChatColor.GREEN + "Your Bitcoin Wallet:");
        user.player.sendMessage(ChatColor.GREEN + "Address " + user.getAddress());
        user.player.sendMessage(ChatColor.GREEN + "Balance " + user.wallet.balance() + "SAT");
        user.player.sendMessage(ChatColor.BLUE+""+ChatColor.UNDERLINE + "blockchain.info/address/" + user.wallet.address);

    };

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // we don't allow server commands (yet?)
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            // PLAYER COMMANDS
            if(cmd.getName().equalsIgnoreCase("clan")) {

                if(args[0].equals("new")) {
                    // TODO: Make sure clan names are alphanumeric
                    if(!args[1].isEmpty()) {
                        if(REDIS.get("clan"+player.getUniqueId().toString())==null) {
                            if(REDIS.sismember("clans",args[1])==false) {
                                REDIS.sadd("clans",args[1]);
                                REDIS.set("clan"+player.getUniqueId().toString(),args[1]);
                                player.sendMessage(ChatColor.GREEN+"Congratulations! you are the founder of the "+args[1]+" clan");
                                return true;
                            } else {
                                player.sendMessage(ChatColor.RED+"A clan with the name '"+args[1]+"' already exists.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED+"You already belong to the clan "+REDIS.get("clan"+player.getUniqueId().toString()));
                            return true;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED+"Your new clan needs a name");
                        return true;
                    }
                }
                if(args[0].equals("invite")) {
                    // check that argument is not empty
                    if(!args[1].isEmpty()) {
                        // TODO: you shouldn't be able to invite yourself
                        // check if user is in the uuid database
                        if(REDIS.get("uuid"+args[1])!=null) {
                            // user is in the uuid database
                            if(REDIS.get("clan"+player.getUniqueId().toString())!=null) {
                                // player is part of a clan
                                REDIS.sadd("invitations"+args[1],REDIS.get("uuid"+args[1]));
                                player.sendMessage("You invited "+REDIS.get("name"+REDIS.get("uuid"+args[1]))+" to the "+REDIS.get("clan"+player.getUniqueId().toString())+" clan.");
                                // TODO: send message if user is connected. otherwise show on MOTD
                                return true;
                            }
                        }
                    }
                }
                if(args[0].equals("join")) {
                    // check that argument is not empty
                    if(!args[1].isEmpty()) {
                        if(REDIS.sismember("invitations"+args[1],player.getUniqueId().toString())==true) {
                            // user is invited to join
                            if(REDIS.get("clan"+player.getUniqueId().toString())==null) {
                                // user is not part of any clan
                                REDIS.set("clan"+player.getUniqueId().toString(),args[1]);
                                player.sendMessage(ChatColor.GREEN+"You are now part of the "+REDIS.get("clan"+player.getUniqueId().toString())+" clan!");
                                return true;
                            } else {
                                player.sendMessage(ChatColor.RED+"You already belong to the clan "+REDIS.get("clan"+player.getUniqueId().toString()));
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED+"You are not invited to join the "+args[1]+" clan.");
                            return true;
                        }
                    }
                }
                if(args[0].equals("kick")) {
                    if(!args[1].isEmpty()) {
                        if(REDIS.get("uuid"+args[1])!=null) {
                            // player is in the uuid database
                            if(REDIS.get("clan"+player.getUniqueId().toString()).equals(REDIS.get("clan"+REDIS.get("uuid"+args[1])))) {
                                REDIS.del("clan"+REDIS.get("uuid"+args[1]));
                                player.sendMessage(ChatColor.GREEN+"Player "+args[1]+" was kicked from the "+REDIS.get("clan"+player.getUniqueId().toString()));
                                return true;
                            } else {
                                player.sendMessage(ChatColor.RED+"Player "+args[1]+" is not a member of the clan "+REDIS.get("clan"+player.getUniqueId().toString()));
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED+"Player "+args[1]+" is unknown to this server.");
                            return true;
                        }
                    }
                }
                if(args[0].equals("leave")) {
                    if(REDIS.get("clan"+player.getUniqueId().toString())!=null) {
                        // TODO: when a clan gets emptied, should be removed from the "clans" set
                        player.sendMessage(ChatColor.GREEN+"You are no longer part of the "+REDIS.get("clan"+player.getUniqueId().toString())+" clan");
                        REDIS.del("clan"+player.getUniqueId().toString());
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED+"You don't belong to a clan.");
                        return true;
                    }
                }
                return false;
            }
            if(cmd.getName().equalsIgnoreCase("wallet")) {
                try {
                    User user=new User(player);
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    User user=new User(player);
                    sendWalletInfo(user);
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
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
						if(fromWallet != null && fromWallet.balance() >= sendAmount) {
							player.sendMessage(ChatColor.YELLOW+"Sending " + args[0] + " Bits to "+args[1]+"...");
							for(final OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                                System.out.println(offlinePlayer);
								if(offlinePlayer.getName()!=null && args[1]!=null && offlinePlayer.getName().equals(args[1])) {
									final Wallet finalFromWallet = fromWallet;
									BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                                    final Wallet toWallet = new User(offlinePlayer.getPlayer()).wallet;
                                    REDIS.expire("balance"+player.getUniqueId().toString(),5);
                                    scheduler.runTaskAsynchronously(this, new Runnable() {
				    					@Override
				    					public void run() {
				    						try {

                                                if (finalFromWallet.transaction(sendAmount, toWallet)) {
                                                    player.sendMessage(ChatColor.GREEN + "Succesfully sent " + sendAmount / 100 + " Bits to " + offlinePlayer.getName() + ".");
                                                    if (offlinePlayer.isOnline() == true) {
                                                        offlinePlayer.getPlayer().sendMessage(ChatColor.GREEN + "" + player.getName() + " just sent you " + sendAmount / 100 + " Bits!");
                                                    }
                                                } else {
                                                    player.sendMessage(ChatColor.RED + "Transaction failed. Please try again in a few moments.");
                                                }

                                            } catch (IOException e1) {
												e1.printStackTrace();
											}
				    					}
				    				});
                                    User user=new User(player);
                                    user.createScoreBoard();
                                    user.updateScoreboard();
					            	return true;
								}
							}
							// validate e-mail address
							String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
							java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
							java.util.regex.Matcher m = p.matcher(args[0]);
							if(m.matches()==true) {
						    	// TODO: send money through xapo

							} else {
						    	try {

						        	Wallet toWallet = new Wallet(args[1]);

						        	if(fromWallet.transaction(sendAmount,toWallet)==true) {
						        		player.sendMessage(ChatColor.GREEN+"Succesfully sent "+args[0]+" Bits to external address.");
						            	new User(player).updateScoreboard();
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
            // MODERATOR COMMANDS
            if (isModerator(player)==true) {
                // COMMAND: MOD
                if (cmd.getName().equalsIgnoreCase("mod")) {
                    Set<String> allplayers=REDIS.smembers("players");
                    if(args[0].equals("add")) {
                        // Sub-command: /mod add
                        if(REDIS.get("uuid"+args[1])!=null) {
                            UUID uuid=UUID.fromString(REDIS.get("uuid"+args[1]));
                            REDIS.sadd("moderators",uuid.toString());
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.RED+"Cannot find player "+args[1]);
                            return true;
                        }
                    } else if(args[0].equals("del")) {
                        // Sub-command: /mod del
                        if(REDIS.get("uuid"+args[1])!=null) {
                            UUID uuid=UUID.fromString(REDIS.get("uuid"+args[1]));
                            REDIS.srem("moderators",uuid.toString());
                            return true;
                        }
                        return false;
                    } else if(args[0].equals("list")) {
                        // Sub-command: /mod list
                        Set<String> moderators=REDIS.smembers("moderators");
                        for(String uuid:moderators) {
                            sender.sendMessage(ChatColor.YELLOW+REDIS.get("name"+uuid));
                        }
                        return true;
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
                // PLAYER COMMANDS

            }
        }
        return true;
    }
}
