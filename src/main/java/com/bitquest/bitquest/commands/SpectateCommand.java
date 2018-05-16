package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SpectateCommand extends CommandAction {
    private BitQuest bitQuest;

    public SpectateCommand(BitQuest plugin) {
        this.bitQuest = plugin;
    }

    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        if (args.length == 1) {

            if (Bukkit.getPlayer(args[0]) != null) {
                ((Player) sender).setGameMode(GameMode.SPECTATOR);
                ((Player) sender).setSpectatorTarget(Bukkit.getPlayer(args[0]));
                bitQuest.success(((Player) sender), "You're now spectating " + args[0] + ".");
            } else {
                bitQuest.error(((Player) sender), "Player " + args[0] + " isn't online.");
            }
            return true;
        }
        return false;
    }
}
