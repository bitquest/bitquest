package com.bitquest.bitquest.commands;


import com.bitquest.bitquest.User;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.text.ParseException;

public class FaucetCommand extends CommandAction {
    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        User user= null;
        try {
            user = new User(player);
            if(user.wallet.getTestnetCoins()) {
                player.sendMessage(ChatColor.GREEN+"Some testnet coins were delivered to your wallet.");
            } else {
                player.sendMessage(ChatColor.RED+"There was an error getting testnet coins.");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
