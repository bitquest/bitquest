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
    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        /***********************************************************
         /upgradewallet
         attempts to transfer funds from old (BQ2.0) wallet to
         the new HD (BQ2.1) wallet via BlockCypher's
         microtransaction endpoint
         ***********************************************************/
        String fail_message="Cannot make transaction at this moment. Please try again later...";
        player.sendMessage(ChatColor.YELLOW+"Searching for lost wallet...");
        if(BitQuest.REDIS.exists("address"+player.getUniqueId().toString())&&BitQuest.REDIS.exists("private"+player.getUniqueId().toString())) {
            Wallet old_wallet=new Wallet(
                    BitQuest.REDIS.get("address"+player.getUniqueId().toString()),
                    BitQuest.REDIS.get("private"+player.getUniqueId().toString()));
            player.sendMessage(ChatColor.YELLOW+"Found wallet "+old_wallet.address+"! looking for bits...");
            try {
                JSONObject balance=old_wallet.get_blockcypher_balance();
                int confirmed_balance=((Number)balance.get("balance")).intValue();
                player.sendMessage(ChatColor.YELLOW+"Confirmed balance in lost wallet is "+confirmed_balance+" sat");

                if(confirmed_balance>100) {
                    int transaction_balance=Math.min(4000000,confirmed_balance);
                    try {
                        User user=new User(player);
                        player.sendMessage(ChatColor.YELLOW+"Sending "+transaction_balance/100+" bits to "+user.wallet.address);

                        if(old_wallet.blockcypher_microtransaction(transaction_balance,user.wallet.address)==true) {
                            player.sendMessage(ChatColor.GREEN+"Transaction successful.");

                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED+fail_message);

                            return true;
                        }
                    } catch (ParseException e) {
                        player.sendMessage(ChatColor.RED+"Error loading new wallet.");
                        e.printStackTrace();
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED+"Not enough balance for recovery. If you think this is an error e-mail bitquest@bitquest.co");

                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED+fail_message);
                return true;
            } catch (org.json.simple.parser.ParseException e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED+fail_message);
                return true;
            }


        } else {
            player.sendRawMessage(ChatColor.RED + "Old wallet not found. If you think this is an error please contact bitquest@bitquest.co");
            return true;
        }
    }
}
