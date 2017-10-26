package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.User;
import com.bitquest.bitquest.Wallet;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import java.text.ParseException;

import java.io.IOException;


public class UpgradeWallet extends CommandAction {
    private BitQuest bitQuest;

    public UpgradeWallet(BitQuest plugin) {
        bitQuest = plugin;
    }

    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        /***
        TODO: Complete command code
         ***/
        User user= null;
        try {
            user = new User(player);
            String address=bitQuest.REDIS.get("hd:address:"+user.player.getUniqueId().toString());

            if(user.wallet.legacy_wallet_balance(address)>0)
                user.player.sendMessage(ChatColor.GREEN + "You have an old wallet: " +ChatColor.WHITE+address);
            int legacy_wallet_balance=user.wallet.legacy_wallet_balance(address);
            user.player.sendMessage(ChatColor.GREEN + "SAT: " +ChatColor.WHITE+legacy_wallet_balance);
            return true;
        } catch (ParseException e) {
            user.player.sendMessage(ChatColor.RED + "Wallet upgrade failed.");
            e.printStackTrace();
            return true;
        } catch (org.json.simple.parser.ParseException e) {
            user.player.sendMessage(ChatColor.RED + "Wallet upgrade failed.");
            e.printStackTrace();
            return true;
        } catch (IOException e) {
            user.player.sendMessage(ChatColor.RED + "Wallet upgrade failed.");
            e.printStackTrace();
            return true;
        }

    }
}
