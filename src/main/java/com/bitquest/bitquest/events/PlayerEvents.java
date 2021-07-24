package com.bitquest.bitquest.events;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.BitQuestPlayer;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerEvents implements Listener {
  BitQuest bitQuest;
  
  public PlayerEvents(BitQuest plugin) {
    bitQuest = plugin;
  }

  @EventHandler
  public void onPlayerPortal(PlayerPortalEvent event) {
    BitQuest.setGameMode(event.getPlayer(), event.getTo());
  }

  @EventHandler
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    BitQuest.setGameMode(event.getPlayer(), event.getRespawnLocation());
  }

  @EventHandler
  public void onExperienceChange(PlayerExpChangeEvent event) {
    bitQuest.setTotalExperience(event.getPlayer());
    event.setAmount(0);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) throws Exception {
    final Player player = event.getPlayer();
    BitQuest.setGameMode(player, player.getLocation());
    // On dev environment, admin gets op. In production, nobody gets op.
    if (BitQuest.BITQUEST_ENV.equals("development") == true && BitQuest.ADMIN_UUID == null) {
      player.setOp(true);
    }
    bitQuest.setTotalExperience(player);
    final String ip = player.getAddress().toString().split("/")[1].split(":")[0];
    System.out.println("User " + player.getName() + "logged in with IP " + ip);
    bitQuest.redis.set("ip" + player.getUniqueId().toString(), ip);
    bitQuest.redis.set("displayname:" + player.getUniqueId().toString(), player.getDisplayName());
    bitQuest.redis.set("uuid:" + player.getName().toString(), player.getUniqueId().toString());
    bitQuest.redis.set("rate_limit:" + event.getPlayer().getUniqueId(), "1");
    bitQuest.redis.expire("rate_limit:" + event.getPlayer().getUniqueId(), 60);
    final BitQuestPlayer bqPlayer = bitQuest.player(player);
    if (bqPlayer.clan != null) {
      player.setPlayerListName(ChatColor.GOLD + "[" + bqPlayer.clan + "] " + ChatColor.WHITE + player.getName());
    }
    player.sendMessage(ChatColor.YELLOW + "     Welcome to " + bitQuest.SERVER_NAME + "! ");
    if (bitQuest.redis.exists("bitquest:motd") == true) {
      player.sendMessage(bitQuest.redis.get("bitquest:motd"));
    }
    player.sendMessage("The loot pool is: " + bitQuest.wallet.balance(0).toString() + " " + bitQuest.node.chain());
    bitQuest.redis.zincrby("player:login", 1, player.getUniqueId().toString());
    bitQuest.updateScoreboard(player);
    bitQuest.spawnVillager();
  }
}
