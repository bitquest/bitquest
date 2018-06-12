package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.User;
import com.bitquest.bitquest.Wallet;
import java.io.IOException;
import java.text.ParseException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SendCommand extends CommandAction {
  private BitQuest bitQuest;

  public SendCommand(BitQuest plugin) {
    bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, final Player player) {
    if (bitQuest.rate_limit == false) {
      bitQuest.rate_limit = true;

      /**
       * ********************************************************* /send a off-chain
       * player-to-player-transaction *********************************************************
       */
      int MAX_SEND = 10000; // to be multiplied by DENOMINATION_FACTOR
      if (args.length == 2) {
        for (char c : args[0].toCharArray()) {
          if (!Character.isDigit(c)) return false;
        }
        if (args[0].length() > 8) {
          // maximum send is 8 digits
          return false;
        }
        final Long amount = Long.parseLong(args[0]);
        final Long sat = amount * BitQuest.DENOMINATION_FACTOR;

        if (amount != 0 && amount <= MAX_SEND) {
          for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(args[1])) {
              if (!args[1].equalsIgnoreCase(player.getDisplayName())) {
                final User user;
                try {
                  user = new User(bitQuest, player);
                } catch (ParseException e) {
                  e.printStackTrace();
                  player.sendMessage(ChatColor.RED + "Tip failed.");
                  return true;
                } catch (org.json.simple.parser.ParseException e) {
                  e.printStackTrace();
                  player.sendMessage(ChatColor.RED + "Tip failed.");
                  return true;
                } catch (IOException e) {
                  e.printStackTrace();
                  player.sendMessage(ChatColor.RED + "Tip failed.");
                  return true;
                }

                user.wallet.getBalance(
                    0,
                    new Wallet.GetBalanceCallback() {
                      @Override
                      public void run(Long balance) {
                        if (balance >= sat) {
                          try {
                            User user_tip = new User(bitQuest, onlinePlayer);
                            if (user.wallet.move(user_tip.player.getUniqueId().toString(), sat)) {
                              bitQuest.updateScoreboard(onlinePlayer);
                              bitQuest.updateScoreboard(player);
                              player.sendMessage(
                                  ChatColor.GREEN
                                      + "You sent "
                                      + ChatColor.LIGHT_PURPLE
                                      + amount
                                      + " "
                                      + BitQuest.DENOMINATION_NAME
                                      + ChatColor.GREEN
                                      + " to user "
                                      + ChatColor.BLUE
                                      + onlinePlayer.getName());
                              onlinePlayer.sendMessage(
                                  ChatColor.GREEN
                                      + "You got "
                                      + ChatColor.LIGHT_PURPLE
                                      + amount
                                      + " "
                                      + BitQuest.DENOMINATION_NAME
                                      + ChatColor.GREEN
                                      + " from user "
                                      + ChatColor.BLUE
                                      + player.getName());
                            } else {
                              player.sendMessage(ChatColor.RED + "Tip failed.");
                            }
                          } catch (ParseException e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "Tip failed.");
                          } catch (org.json.simple.parser.ParseException e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "Tip failed.");
                          } catch (IOException e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "Tip failed.");
                          }
                        } else {
                          player.sendMessage(ChatColor.DARK_RED + "Not enough balance");
                        }
                      }
                    });
                return true;

                    return true;
                } else {
                    player.sendMessage("Minimum tip is 1 " + BitQuest.DENOMINATION_NAME + ". Maximum is " + MAX_SEND);
                    return true;
                }
            } else {
                player.sendMessage(ChatColor.RED + "Usage: /send <amount> <player>");
                return true;

            }
          }
          player.sendMessage(
              ChatColor.DARK_RED
                  + "Player "
                  + ChatColor.BLUE
                  + args[1]
                  + ChatColor.DARK_RED
                  + " is not online");

          return true;
        } else {
          player.sendMessage(ChatColor.DARK_RED + "Minimum tip is 1 bit. Maximum is " + MAX_SEND);
          return true;
        }
      } else {
        return false;
      }
    } else {
      player.sendMessage(
          ChatColor.RED + "Connectivity to Blockchain is limited. Please try again in 5 seconds.");
      return true;
    }
  }
}
