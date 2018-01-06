package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class ClanCommand extends CommandAction {
    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        if (args.length > 0) {
            String subCommand = args[0];
            if (subCommand.equals("new")) {
                if (args.length > 1) {
                    String clanName = args[1];
                    // check that desired clan name is alphanumeric
                    boolean hasNonAlpha = clanName.matches("^.*[^a-zA-Z0-9 ].*$");
                    if (!hasNonAlpha) {
                        // 16 characters max
                        if (clanName.length() <= 16) {
                            if (!BitQuest.REDIS.exists("clan:" + player.getUniqueId().toString())) {
                                if (!BitQuest.REDIS.sismember("clans", clanName)) {
                                    BitQuest.REDIS.sadd("clans", clanName);
                                    BitQuest.REDIS.set("clan:" + player.getUniqueId().toString(), clanName);
                                    player.sendMessage(ChatColor.GREEN + "Congratulations! you are the founder of the " + clanName + " clan");
                                    player.setPlayerListName(ChatColor.GOLD + "[" + clanName + "] " + ChatColor.WHITE + player.getName());
                                    return true;
                                } else {
                                    player.sendMessage(ChatColor.RED + "A clan with the name '" + clanName + "' already exists.");
                                    return true;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You already belong to the clan " + BitQuest.REDIS.get("clan:" + player.getUniqueId().toString()));
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Error: clan name must have 16 characters max");
                            return true;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Your clan name must only contain letters and numbers");
                        return true;
                    }

                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /clan new <your desired name>");
                    return true;
                }

            }
            if (subCommand.equals("invite")) {
                if (args.length > 1) {
                    String invitedName = args[1];
                    if (invitedName.equals(player.getName())) {
                        player.sendMessage(ChatColor.RED + "You can not invite yourself");
                        return true;
                    }
                    // check that player is in a clan
                    if (BitQuest.REDIS.exists("clan:" + player.getUniqueId().toString())) {
                        String clan = BitQuest.REDIS.get("clan:" + player.getUniqueId().toString());
                        // check if user is in the uuid database
                        if (BitQuest.REDIS.exists("uuid:" + invitedName)) {
                            // check if player already belongs to a clan
                            String uuid = BitQuest.REDIS.get("uuid:" + invitedName);
                            if (!BitQuest.REDIS.exists("clan:" + uuid)) {
                                // check if player is already invited to the clan
                                if (!BitQuest.REDIS.sismember("invitations:" + clan, uuid)) {
                                    BitQuest.REDIS.sadd("invitations:" + clan, uuid);
                                    player.sendMessage(ChatColor.GREEN + "You invited " + invitedName + " to the " + clan + " clan.");
                                    if (Bukkit.getPlayerExact(invitedName) != null) {
                                        Player invitedplayer = Bukkit.getPlayerExact(invitedName);
                                        invitedplayer.sendMessage(ChatColor.GREEN + player.getName() + " invited you to the " + clan + " clan");
                                        invitedplayer.sendMessage(ChatColor.GREEN + player.getName() + " to join, enter: /clan join " + clan );
                                    }
                                    return true;
                                } else {
                                    player.sendMessage(ChatColor.RED + "Player " + invitedName + " is already invited to the clan and must accept the invitation");
                                    return true;
                                }

                            } else {
                                if (BitQuest.REDIS.get("clan:" + uuid).equals(clan)) {
                                    player.sendMessage(ChatColor.RED + "Player " + invitedName + " already belongs to the clan " + clan);

                                } else {
                                    player.sendMessage(ChatColor.RED + "Player " + invitedName + " already belongs to a clan.");

                                }
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "User " + invitedName + " does not play on this server");
                            return true;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't belong to a clan");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /clan invite <player nickname>");
                    return true;
                }
            }
            if (subCommand.equals("join")) {
                // check that argument is not empty
                if (args.length > 1) {
                    String clanName = args[1];
                    // check that player is invited to the clan he wants to join
                    if (BitQuest.REDIS.sismember("invitations:" + clanName, player.getUniqueId().toString())) {
                        // user is invited to join
                        if (!BitQuest.REDIS.exists("clan:" + player.getUniqueId().toString())) {
                            // user is not part of any clan
                            BitQuest.REDIS.srem("invitations:" + clanName, player.getUniqueId().toString());
                            BitQuest.REDIS.set("clan:" + player.getUniqueId().toString(), clanName);
                            player.sendMessage(ChatColor.GREEN + "You are now part of the " + clanName + " clan!");
                            player.setPlayerListName(ChatColor.GOLD + "[" + clanName + "] " + ChatColor.WHITE + player.getName());
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "You already belong to the clan " + BitQuest.REDIS.get("clan:" + player.getUniqueId().toString()));
                            return true;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You are not invited to join the " + clanName + " clan.");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /clan join <clan name>");
                    return true;
                }
            }
            if (args[0].equals("kick")) {
                if (args.length > 1) {
                    String toKick = args[1];
                    // check if player is in the uuid database
                    if (BitQuest.REDIS.exists("uuid:" + toKick)) {
                        String uuid = BitQuest.REDIS.get("uuid:" + toKick);
                        // check if player belongs to a clan
                        if (BitQuest.REDIS.exists("clan:" + player.getUniqueId().toString())) {
                            String clan = BitQuest.REDIS.get("clan:" + player.getUniqueId().toString());
                            // check that kicker and player are in the same clan
                            if (BitQuest.REDIS.get("clan:" + uuid).equals(clan)) {
                                BitQuest.REDIS.del("clan:" + uuid);
                                player.sendMessage(ChatColor.GREEN + "Player " + toKick + " was kicked from the " + clan + " clan.");
                                if (Bukkit.getPlayerExact(toKick) != null) {
                                    Player invitedPlayer = Bukkit.getPlayerExact(toKick);
                                    invitedPlayer.sendMessage(ChatColor.RED + player.getName() + " kick you from the " + clan + " clan");
                                    invitedPlayer.setPlayerListName(invitedPlayer.getName());
                                }
                                return true;
                            } else {
                                player.sendMessage(ChatColor.RED + "Player " + toKick + " is not a member of the clan " + clan);
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You don't belong to any clan.");
                            return true;
                        }

                    } else {
                        player.sendMessage(ChatColor.RED + "Player " + toKick + " does not play on this server.");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /clan kick <player nickname>");
                    return true;
                }
            }
            if (subCommand.equals("leave")) {
                if (BitQuest.REDIS.exists("clan:" + player.getUniqueId().toString())) {
                    // TODO: when a clan gets emptied, should be removed from the "clans" set
                    player.sendMessage(ChatColor.GREEN + "You are no longer part of the " + BitQuest.REDIS.get("clan:" + player.getUniqueId().toString()) + " clan");
                    BitQuest.REDIS.del("clan:" + player.getUniqueId().toString());

                    player.setPlayerListName(player.getName());
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You don't belong to a clan.");
                    return true;
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /clan <new|invite|kick|join|leave>");
            return true;
        }
        return false;
    }
}
