package com.bitquest.bitquest;

import com.evilmidget38.UUIDFetcher;
import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.IOException;
import java.text.ParseException;
import java.util.UUID;

/**
 * Created by cristian on 12/17/15.
 */
public class SignEvents implements Listener {
    BitQuest bitQuest;
    public SignEvents(BitQuest plugin) {
        bitQuest = plugin;
    }
    @EventHandler
    public void onSignChange(SignChangeEvent event) throws ParseException, org.json.simple.parser.ParseException, IOException {

		final Player player = event.getPlayer();
    	// Check that the world is overworld
    	if(!event.getBlock().getWorld().getName().endsWith("_nether") && !event.getBlock().getWorld().getName().endsWith("_end")) {
    		final String specialCharacter = "^";
    		final String[] lines = event.getLines();
    		final String signText = lines[0] + lines[1] + lines[2] + lines[3];
    		Chunk chunk = event.getBlock().getWorld().getChunkAt(event.getBlock().getLocation());
    		final int x=chunk.getX();
    		final int z=chunk.getZ();

    		if (signText.length() > 0 && signText.substring(0,1).equals(specialCharacter) && signText.substring(signText.length()-1).equals(specialCharacter)) {

    			final String name = signText.substring(1,signText.length()-1);

    			if (BitQuest.REDIS.get("chunk" + x + "," + z + "owner") == null) {
    				final User user = new User(player);
    				player.sendMessage(ChatColor.YELLOW + "Claiming land...");
    				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
    				scheduler.runTask(bitQuest, new Runnable() {
    					@Override
    					public void run() {
    						// A villager is born
    						try {
    							Wallet paymentWallet;
    							if (BitQuest.LAND_BITCOIN_ADDRESS != null) {
    								paymentWallet = new Wallet(BitQuest.LAND_BITCOIN_ADDRESS);
    							} else {
    								paymentWallet = bitQuest.wallet;
    							}
    							if (user.wallet.transaction(BitQuest.LAND_PRICE, paymentWallet)) {

    								BitQuest.REDIS.set("chunk" + x + "," + z + "owner", player.getUniqueId().toString());
    								BitQuest.REDIS.set("chunk" + x + "," + z + "name", name);
    								player.sendMessage(ChatColor.GREEN + "Congratulations! You're now the owner of " + name + "!");
									if(bitQuest.messageBuilder!=null) {

										// Create an event
										org.json.JSONObject sentEvent = bitQuest.messageBuilder.event(player.getUniqueId().toString(), "Claim", null);
										org.json.JSONObject sentCharge = bitQuest.messageBuilder.trackCharge(player.getUniqueId().toString(), BitQuest.LAND_PRICE/100,null);


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
    										ChatColor.BOLD + Math.ceil((BitQuest.LAND_PRICE-balance)/100) + ChatColor.RED + " more Bits.");
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
    						;
    					}
    				});

    			}else if (BitQuest.REDIS.get("chunk" + x + "," + z + "owner").equals(player.getUniqueId().toString())) {
					if (name.equals("abandon")) {
                        // Abandon land
                        BitQuest.REDIS.del("chunk" + x + "," + z + "owner");
                        BitQuest.REDIS.del("chunk" + x + "," + z + "name");
                    }else if (name.startsWith("transfer ") && name.length() > 9) {
                        // If the name starts with "trasnfer " and have at lest one more character,
                        // transfer land
                        final String newOwner = name.substring(9);
                        player.sendMessage(ChatColor.YELLOW+"Transfering land to " + newOwner + "...");

                        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                        scheduler.runTaskAsynchronously(bitQuest, new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    UUID newOwnerUUID = UUIDFetcher.getUUIDOf(newOwner);
                                    BitQuest.REDIS.set("chunk" + x + "," + z + "owner", newOwnerUUID.toString());
                                    player.sendMessage(ChatColor.GREEN + "This land now belongs to "+newOwner);
                                } catch (Exception e) {
                                    player.sendMessage(ChatColor.RED + "Could not get uuid of "+ newOwner);
                                }
                            }
                        });

                    }else if (BitQuest.REDIS.get("chunk" + x + "," + z + "name").equals(name)) {
    					player.sendMessage(ChatColor.RED + "You already own this land!");
    				} else {
    					// Rename land
    					player.sendMessage(ChatColor.GREEN + "You renamed this land to " + name + ".");
    					BitQuest.REDIS.set("chunk" + x + "," + z + "name", name);
    				}
    			}
    		}
        
    	} else if(event.getBlock().getWorld().getName().endsWith("_nether")) {
    		player.sendMessage(ChatColor.RED + "No claiming in the nether!");
    	} else if(event.getBlock().getWorld().getName().endsWith("_end")) {
    		player.sendMessage(ChatColor.RED + "No claiming in the end!");
    	}

    }
}

