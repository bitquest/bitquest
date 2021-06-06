package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.User;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DonateCommand extends CommandAction {
  private BitQuest bitQuest;

  public DonateCommand(BitQuest plugin) {
    bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, final Player player) {
    if (args.length == 1) {
      if (System.getenv("DONATION_ADDRESS") != null) {
        try {
          final Double amount = Double.parseDouble(args[0]);
          final User user = new User(player.getUniqueId(), bitQuest);
          final Double balance = user.wallet.balance(0);

          if (user.wallet.payment(System.getenv("DONATION_ADDRESS"), amount)) {
            player.sendMessage(ChatColor.GREEN + "Thanks for your support!");
            bitQuest.updateScoreboard(player);
          } else {
            player.sendMessage(ChatColor.RED + "Donation failed");
          }


          return true;
        } catch (Exception e) {
          System.out.println(e);
          player.sendMessage(ChatColor.RED + "Command failed.");
          return true;
        }
      } else {
        player.sendMessage(ChatColor.RED + "Donations are disabled in this server");
        return true;
      }

    } else {
      return false;
    }
  }
}
