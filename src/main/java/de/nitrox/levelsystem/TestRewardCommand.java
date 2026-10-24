package de.nitrox.levelsystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestRewardCommand {

    private final LevelSystem plugin;

    public TestRewardCommand(LevelSystem plugin) {
        this.plugin = plugin;
    }

    /**
     * /simplelevels testreward <identifier> <level> [player]
     */
    public boolean execute(CommandSender sender, String[] args) {

        if (!sender.hasPermission("simplelevels.testreward")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /simplelevels testreward <identifier> <level> [player]");
            return true;
        }

        String id = args[0];
        LevelSystemInstance inst = plugin.getManager().get(id);
        if (inst == null) {
            sender.sendMessage(ChatColor.RED + "Unknown LevelSystem: " + id);
            return true;
        }

        int level;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid level: " + args[1]);
            return true;
        }

        if (level < 1) {
            sender.sendMessage(ChatColor.RED + "Level must be at least 1.");
            return true;
        }

        Player target;
        if (args.length == 3) {
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not online: " + args[2]);
                return true;
            }
        } else {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player: /simplelevels testreward <identifier> <level> <player>");
                return true;
            }
            target = p;
        }

        boolean executed = inst.executeReward(target, level);
        if (!executed) {
            sender.sendMessage(ChatColor.RED + "No reward configured for level " + level + " in system " + id + ".");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "Executed test reward for level " + level + " in system " + id + " for " + target.getName() + ".");
        return true;
    }
}
