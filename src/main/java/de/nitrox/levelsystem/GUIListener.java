package de.nitrox.levelsystem;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof LevelGUI gui)) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(top)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getSlot();
        LevelSystemInstance system = gui.getSystem();

        if (slot == LevelGUI.SLOT_PREV_PAGE && gui.getPage() > 0) {
            new LevelGUI(system, player, gui.getPage() - 1).open();
            return;
        }

        if (slot == LevelGUI.SLOT_NEXT_PAGE) {
            int maxLevel = system.getMaxLevelForPlayer(player);
            if ((gui.getPage() + 1) * LevelGUI.SNAKE_SLOTS.length < maxLevel) {
                new LevelGUI(system, player, gui.getPage() + 1).open();
            }
            return;
        }

        Integer level = gui.getLevelAtSlot(slot);
        if (level == null) {
            return;
        }

        int playerLevel = system.getPlayerLevel(player.getUniqueId());
        if (level > playerLevel) {
            player.sendMessage(ChatColor.RED + "You have not reached Level " + level + " yet.");
            return;
        }

        if (system.hasReward(player.getUniqueId(), level)) {
            player.sendMessage(ChatColor.YELLOW + "You have already claimed the reward for Level " + level + ".");
            return;
        }

        if (system.claimReward(player, level)) {
            player.sendMessage(ChatColor.GREEN + "Claimed reward for Level " + level + "!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            gui.render();
        } else {
            player.sendMessage(ChatColor.RED + "You do not have permission to claim this level's reward.");
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof LevelGUI) {
            event.setCancelled(true);
        }
    }
}
