package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PetCommand extends CommandAction {
  private BitQuest bitQuest;

  public PetCommand(BitQuest plugin) {
    this.bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    if (args[0] == null) {
      player.sendMessage("Your pet needs a name!");
      return false;
    } else if ((args[0].equalsIgnoreCase("on"))
        && (BitQuest.REDIS
            .get("petIsOn" + player.getUniqueId().toString())
            .equalsIgnoreCase("off"))) {
      BitQuest.REDIS.set("petIsOn" + player.getUniqueId().toString(), "on");
      player.sendMessage("Your pet is on!");
      bitQuest.spawnPet(player);
      return true;
    } else if ((args[0].equalsIgnoreCase("off"))
        && (BitQuest.REDIS
            .get("petIsOn" + player.getUniqueId().toString())
            .equalsIgnoreCase("on"))) {
      BitQuest.REDIS.set("petIsOn" + player.getUniqueId().toString(), "off");
      player.sendMessage("Your pet is off!");
      bitQuest.spawnPet(player);
      return true;
    } else if (args[0].isEmpty()) {
      player.sendMessage("Your pet needs a cool name!");
      return false;
    } else if (args[0].matches("^.*[^a-zA-Z0-9 _].*$")) {
      player.sendMessage("Please use only aplhanumeric characters.");
      return false;
    } else if (args[0].length() >= 20) {
      player.sendMessage("That name is too long!");
      return false;
    } else {
      if (bitQuest.REDIS.sismember("pet:names", args[0])) {
        player.sendMessage(ChatColor.RED + "A pet with that name already exists.");
      } else if ((args[0].equalsIgnoreCase("off")) || (args[0].equalsIgnoreCase("on"))) {
        player.sendMessage("You can not choose that as a name!");
      } else {
        try {
          bitQuest.adoptPet(player, args[0]);

        } catch (Exception e) {
          System.out.println(e);
          player.sendMessage(ChatColor.DARK_RED + "FAIL. Please try again later.");
        }
      }

      return true;
    }
  }
}
