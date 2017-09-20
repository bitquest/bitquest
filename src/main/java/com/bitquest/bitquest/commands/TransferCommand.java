package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import com.bitquest.bitquest.User;
import com.bitquest.bitquest.Wallet;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.text.ParseException;


public class TransferCommand extends CommandAction {
    private BitQuest bitQuest;

    public TransferCommand (BitQuest plugin) {
        bitQuest = plugin;
    }

    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        if(args.length == 2) {
            for(char c : args[0].toCharArray()) {
                if(!Character.isDigit(c))
                    return false;
            }
            int sendAmount=0;
            try {
                sendAmount = Integer.valueOf(args[0])*100;
            } catch(NumberFormatException e) {
                return false;
            }
            System.out.println(sendAmount);
            Wallet fromWallet = null;
            try {
                fromWallet = new User(player).wallet;
            } catch (ParseException e1) {
                e1.printStackTrace();
            } catch (org.json.simple.parser.ParseException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                if( sendAmount < BitQuest.MIN_TRANS) {
                    player.sendMessage(ChatColor.RED+"Minimum transaction is "+BitQuest.MIN_TRANS/100+" Bits.");
                    return true;
                } else try {
                    if(fromWallet.final_balance()<sendAmount) {
                        player.sendMessage(ChatColor.RED+"You don't have enough balance.");
                        System.out.println("not enough balance: "+fromWallet.final_balance()+" vs. "+sendAmount);
                        return true;
                    } else if(fromWallet != null) {
                        player.sendMessage(ChatColor.YELLOW+"Sending " + args[0] + " Bits to "+args[1]+"...");

                        // validate e-mail address
                        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
                        java.util.regex.Matcher m = p.matcher(args[1]);
                        if(m.matches()) {
                            if(fromWallet.email_transaction(sendAmount,args[1])) {
                                player.sendMessage(ChatColor.GREEN+"Succesfully sent "+args[0]+" Bits to "+args[1]);
                                return true;
                            } else {
                                player.sendMessage(ChatColor.RED+"Transaction failed. Please try again in a few moments.");
                                return true;
                            }
                        } else {
                            try {

                                Wallet toWallet = new Wallet(args[1]);

                                if(fromWallet.create_blockcypher_transaction(sendAmount,toWallet.address)) {
                                    player.sendMessage(ChatColor.GREEN+"Succesfully sent "+args[0]+" Bits to external address.");
                                    bitQuest.updateScoreboard(player);
                                } else {
                                    player.sendMessage(ChatColor.RED+"Transaction failed. Please try again in a few moments.");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                player.sendMessage(ChatColor.RED+"Transaction failed. Please try again in a few moments.");
                            } catch (org.json.simple.parser.ParseException e) {
                                e.printStackTrace();
                                player.sendMessage(ChatColor.RED+"Transaction failed. Please try again in a few moments.");
                            } catch (ParseException e) {
                                e.printStackTrace();
                                player.sendMessage(ChatColor.RED+"Transaction failed. Please try again in a few moments.");
                            }

                        }
                        return true;
                    }
                } catch (org.json.simple.parser.ParseException e) {
                    player.sendMessage(ChatColor.RED+"Transaction failed. Please try again in a few moments.");

                    e.printStackTrace();
                    return true;
                }
                return true;
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED+"Transaction failed. Please try again in a few moments.");

                e.printStackTrace();
                return true;

            }
        }
        return false;
    }
}
