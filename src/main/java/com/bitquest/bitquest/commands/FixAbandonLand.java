package com.bitquest.bitquest.commands;

import com.bitquest.bitquest.BitQuest;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// this is an old command proposed by AltQuest, I re add it here.

// created by @BitcoinJake09 5/14/18 THIS IS TESTED!!!! and seems to be correct!! i added an extra
// feature also, you can now just run /fixabandonland to see list of land with owners and
// permissions in redis and what would be removed. then can also run as /fixabandonland <true|yes>
public class FixAbandonLand extends CommandAction {
  BitQuest bitQuest;

  public FixAbandonLand(BitQuest plugin) {
    bitQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    int xySize = 0;
    Set<String> ownerList = bitQuest.redis.keys("chunk*owner");
    Set<String> permissionsList = bitQuest.redis.keys("*permissions");
    String[] xys = new String[ownerList.size()];
    String[] subPerms = new String[permissionsList.size()];
    for (String tempOwnerList : ownerList) {
      xys[xySize] = tempOwnerList.substring(0, tempOwnerList.length() - 5);
      sender.sendMessage(
          ChatColor.DARK_RED
              + bitQuest.redis.get(xys[xySize] + "name")
              + " is owned by: "
              + (bitQuest.redis.get(tempOwnerList)));
      xySize++;
    }

    xySize = 0;

    for (String tempPermissionsList : permissionsList) {
      subPerms[xySize] = tempPermissionsList.substring(0, tempPermissionsList.length() - 11);
      sender.sendMessage(
          ChatColor.YELLOW
              + tempPermissionsList
              + " is set to: "
              + (bitQuest.redis.get(subPerms[xySize] + "permissions")));
      xySize++;
    }

    for (int i = 0; i <= permissionsList.size() - 1; i++) {
      if ((bitQuest.redis.get(subPerms[i] + "owner")) == null) {
        sender.sendMessage(
            ChatColor.GREEN
                + "To Be Removed: "
                + subPerms[i]
                + "permissions is set to: "
                + bitQuest.redis.get(subPerms[i] + "permissions"));
      }
    }
    if ((args[0].equalsIgnoreCase("true")) || (args[0].equalsIgnoreCase("yes"))) {
      for (int i = 0; i <= permissionsList.size() - 1; i++) {
        if ((bitQuest.redis.get(subPerms[i] + "owner")) == null) {
          bitQuest.redis.del(subPerms[i] + "permissions");
          sender.sendMessage(ChatColor.GREEN + "Removed: " + subPerms[i] + "permissions");
        }
      }
    }

    sender.sendMessage(ChatColor.GREEN + "finished");
    return true;
  }
}
