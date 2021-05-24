package com.bitquest.bitquest.events;

import com.bitquest.bitquest.BitQuest;
import java.io.IOException;
import java.text.ParseException;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * SignEvents
 * Catches sign name changes used for users to claim land.
 */
public class SignEvents implements Listener {
  BitQuest bitQuest;

  public SignEvents(BitQuest plugin) {
    bitQuest = plugin;
  }

  /**
   * onSignChange
   * Fired when a user types on a new sign or changes the content of a sign.
   */
  @EventHandler
  public void onSignChange(SignChangeEvent event)
      throws ParseException, org.json.simple.parser.ParseException, IOException {

    final Player player = event.getPlayer();
    // Check that the world is overworld
    if (!event.getBlock().getWorld().getName().endsWith("_end")) {
      final String specialCharacter = "^";
      final String[] lines = event.getLines();
      final String signText = lines[0] + lines[1] + lines[2] + lines[3];
      Chunk chunk = event.getBlock().getWorld().getChunkAt(event.getBlock().getLocation());

      if (signText.length() > 0
          && signText.substring(0, 1).equals(specialCharacter)
          && signText.substring(signText.length() - 1).equals(specialCharacter)) {

        final String name = signText.substring(1, signText.length() - 1);
        bitQuest.claimLand(name, chunk, player);
      }

    } else if (event.getBlock().getWorld().getName().endsWith("_end")) {
      player.sendMessage(ChatColor.DARK_RED + "No claiming in the end!");
    }
  }
}
