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
            if(bitQuest.REDIS.exists("hd:address:"+user.player.getUniqueId().toString())==true) {
                String address=bitQuest.REDIS.get("hd:address:"+user.player.getUniqueId().toString());

                if(user.wallet.legacy_wallet_balance(address)>0)
                    user.player.sendMessage(ChatColor.GREEN + "You have an old wallet: " +ChatColor.WHITE+address);
                int legacy_wallet_balance=user.wallet.legacy_wallet_balance(address);
                user.player.sendMessage(ChatColor.GREEN + "SAT: " +ChatColor.WHITE+legacy_wallet_balance);
                Wallet upgrades_wallet=new Wallet("bitquest_upgrades");
                if(upgrades_wallet.move(player.getUniqueId().toString(),legacy_wallet_balance)==true) {
                    user.player.sendMessage(ChatColor.GREEN + "Moved " +legacy_wallet_balance+" SAT to new account");
                    bitQuest.REDIS.del("hd:address:"+user.player.getUniqueId().toString());
                    return true;
                } else {
                    user.player.sendMessage(ChatColor.RED+"Upgrade failed.");
                    return true; ad
                }
            } else {
                user.player.sendMessage(ChatColor.RED + "Wallet upgrade failed.");
                return true;
            }

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
