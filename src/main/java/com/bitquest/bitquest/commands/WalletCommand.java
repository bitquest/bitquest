package com.bitquest.bitquest.commands;

        import com.bitquest.bitquest.BitQuest;
        import com.bitquest.bitquest.User;

        import java.io.IOException;
        import java.text.ParseException;

        import org.bukkit.ChatColor;
        import org.bukkit.command.Command;
        import org.bukkit.command.CommandSender;
        import org.bukkit.entity.Player;

public class WalletCommand extends CommandAction {
    private BitQuest bitQuest;

    public WalletCommand(BitQuest plugin) {
        bitQuest = plugin;
    }

    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {

        try {
            User user = new User(bitQuest, player);
            bitQuest.sendWalletInfo(user);
            bitQuest.updateScoreboard(player);
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was a problem reading your wallet.");
        }

        return true;

    }
}
