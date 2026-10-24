package de.nitrox.levelsystem;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GUICommand {

    private final LevelSystem plugin;

    public GUICommand(LevelSystem plugin) {
        this.plugin = plugin;
    }

    /**
     * /simplelevels gui <identifier>
     */
    public boolean execute(CommandSender sender, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can open the level GUI.");
            return true;
        }

        if (!player.hasPermission("simplelevels.gui")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /simplelevels gui <identifier>");
            return true;
        }

        String id = args[0];
        LevelSystemInstance inst = plugin.getManager().get(id);
        if (inst == null) {
            player.sendMessage(ChatColor.RED + "Unknown LevelSystem: " + id);
            return true;
        }

        if (inst.isWorldDisabled(player.getWorld().getName())) {
            player.sendMessage(ChatColor.RED + "This LevelSystem is disabled in this world.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        int playerLevel = inst.getPlayerLevel(uuid);
        int maxLevel = inst.getMaxLevelForPlayer(player);

        int focusLevel = Math.min(maxLevel, playerLevel + 1);
        for (int lvl = 1; lvl <= playerLevel; lvl++) {
            if (!inst.hasReward(uuid, lvl)) {
                focusLevel = lvl;
                break;
            }
        }

        int initialPage = Math.max(0, (focusLevel - 1) / LevelGUI.SNAKE_SLOTS.length);
        new LevelGUI(inst, player, initialPage).open();
        return true;
    }
}
