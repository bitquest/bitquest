package com.bitquest.bitquest;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvents implements Listener {
	
	BitQuest bitQuest;
	
	public ChatEvents(BitQuest plugin) {
		bitQuest = plugin;
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		
		// Spigot replaces "%2$s" with the original message.
		String message = event.getMessage();
		Player sender = event.getPlayer();

		if(message.startsWith("@")) {
			event.setCancelled(true);
			if(message.length() > 1 && message.substring(1, message.length()).trim().length() >= 1) {
				event.setMessage(event.getMessage().substring(1, message.length()));
				String clan = sender.getScoreboard().getPlayerTeam(sender).getPrefix();
				clan = clan.trim();
				clan = clan.substring(1, clan.length() - 1);
				event.setFormat(ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + clan + "> " + ChatColor.YELLOW + sender.getName() + " " + ChatColor.WHITE + "%2$s");
				Set<Player> teammates = new HashSet<Player>();
				if(sender.getScoreboard().getPlayerTeam(sender) != null) {
					for(OfflinePlayer player : sender.getScoreboard().getPlayerTeam(sender).getPlayers()) {
						if(player.isOnline()) {
							teammates.add(player.getPlayer());
						}
					}
				} else {
					sender.sendMessage(ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "Clan> " + ChatColor.RED + "You aren't in a clan, silly!");
					return;
				}
			
				if(teammates.size() <= 1) {
					sender.sendMessage(ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "Clan> " + ChatColor.RED + "You have no online clanmates!");
				} else {
					for(Player teammate : teammates) {
						teammate.sendMessage(filterWordsForPlayer(event.getFormat(), teammate));
					}
					System.out.println(ChatColor.stripColor(event.getFormat()));
				}
			}
		} else if(message.startsWith("!")) {
			if(message.length() > 1 && message.substring(1, message.length()).trim().length() >= 1) {
				event.setMessage(message.substring(1, message.length()));
				event.setFormat(ChatColor.BLUE.toString() + sender.getLevel() + " " + ChatColor.YELLOW + sender.getName() + " " + ChatColor.WHITE + "%2$s");
			} else {
				event.setCancelled(true);
			}
		} else {
			event.setFormat(ChatColor.BLUE + ChatColor.BOLD.toString() + "Local> " + ChatColor.YELLOW + sender.getName() + " " + ChatColor.WHITE + event.getMessage());
			event.setCancelled(true);
			Set<Player> recipients = new HashSet<Player>();
			recipients.add(sender);
			for(Entity entity : sender.getNearbyEntities(100,100,100)) {
				
				if (entity instanceof Player) {
					recipients.add((Player) entity);
				}
				
			} 
			
			if(recipients.size() <= 1) {
				sender.sendMessage(ChatColor.BLUE + ChatColor.BOLD.toString() + "Local> " + ChatColor.RED + "Nobody is within earshot! Try shouting.");
				sender.sendMessage(ChatColor.BLUE + ChatColor.BOLD.toString() + "Local> " + ChatColor.RED + "Shout by placing a ! before messages.");
			} else {
				for(Player recipient : recipients) {
					recipient.sendMessage(filterWordsForPlayer(event.getFormat(), recipient));
				}
				System.out.println(ChatColor.stripColor(event.getFormat()));
			}
			
		}
		
	}

	private String filterWordsForPlayer(String msg, Player player) {
		String newMsg = msg;
		Set<String> words = BitQuest.REDIS.smembers("filter" + player.getUniqueId().toString());

		for(String word : words) {
			String[] split = word.split("\\|");
			// Case insensitive replace
			newMsg = newMsg.replaceAll("(?i)" + Pattern.quote(split[0]), split[1]);
		}

		return newMsg;
	}
	
}