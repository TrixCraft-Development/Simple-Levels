package de.nitrox.levelsystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LevelGUI implements InventoryHolder {

    public static final int[] SNAKE_SLOTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            17,
            26, 25, 24, 23, 22, 21, 20, 19, 18,
            27,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    };

    public static final int SLOT_PREV_PAGE = 45;
    public static final int SLOT_INFO = 49;
    public static final int SLOT_NEXT_PAGE = 53;

    private final LevelSystemInstance system;
    private final Player player;
    private int page;
    private final Inventory inventory;
    private final Map<Integer, Integer> slotToLevel = new HashMap<>();

    public LevelGUI(LevelSystemInstance system, Player player, int page) {
        this.system = system;
        this.player = player;
        this.page = Math.max(0, page);
        String title = ChatColor.DARK_GRAY + "Levels: " + ChatColor.AQUA + system.getId()
                + ChatColor.GRAY + " (Page " + (this.page + 1) + ")";
        this.inventory = Bukkit.createInventory(this, 54, title);
        render();
    }

    public void render() {
        inventory.clear();
        slotToLevel.clear();

        ItemStack filler = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null, false);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        ItemStack pathTrack = createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " ", null, false);
        for (int slot : SNAKE_SLOTS) {
            inventory.setItem(slot, pathTrack);
        }

        UUID uuid = player.getUniqueId();
        int playerXP = system.getPlayerXP(uuid);
        int playerLevel = system.getPlayerLevel(uuid);
        int maxLevel = system.getMaxLevelForPlayer(player);
        int currentInProgressLevel = playerLevel + 1;

        int startLevel = page * SNAKE_SLOTS.length + 1;

        for (int i = 0; i < SNAKE_SLOTS.length; i++) {
            int level = startLevel + i;
            if (level > maxLevel) {
                break;
            }

            int slot = SNAKE_SLOTS[i];
            slotToLevel.put(slot, level);
            inventory.setItem(slot, createLevelItem(level, playerLevel, currentInProgressLevel, playerXP, uuid));
        }

        // Info item
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "System: " + ChatColor.AQUA + system.getId());
        infoLore.add(ChatColor.GRAY + "Current Level: " + ChatColor.GREEN + playerLevel + ChatColor.GRAY + "/" + ChatColor.GREEN + maxLevel);
        infoLore.add(ChatColor.GRAY + "Current XP: " + ChatColor.LIGHT_PURPLE + playerXP);
        if (currentInProgressLevel <= maxLevel) {
            int needed = Math.max(0, system.getRequiredXPForLevel(currentInProgressLevel) - playerXP);
            infoLore.add(ChatColor.GRAY + "XP to Level " + currentInProgressLevel + ": " + ChatColor.YELLOW + needed);
        } else {
            infoLore.add(ChatColor.GOLD + "Maximum level reached!");
        }
        inventory.setItem(SLOT_INFO, createItem(
                Material.EXPERIENCE_BOTTLE,
                ChatColor.GOLD + "Your Progress",
                infoLore,
                false
        ));

        // Pagination buttons
        if (page > 0) {
            inventory.setItem(SLOT_PREV_PAGE, createItem(
                    Material.ARROW,
                    ChatColor.YELLOW + "« Previous Page",
                    List.of(ChatColor.GRAY + "Go to page " + page),
                    false
            ));
        }

        if (startLevel + SNAKE_SLOTS.length <= maxLevel) {
            inventory.setItem(SLOT_NEXT_PAGE, createItem(
                    Material.ARROW,
                    ChatColor.YELLOW + "Next Page »",
                    List.of(ChatColor.GRAY + "Go to page " + (page + 2)),
                    false
            ));
        }
    }

    private ItemStack createLevelItem(int level, int playerLevel, int currentInProgressLevel, int playerXP, UUID uuid) {
        boolean unlocked = level <= playerLevel;
        boolean claimed = system.hasReward(uuid, level);
        boolean isCurrent = (level == currentInProgressLevel);
        int requiredXP = system.getRequiredXPForLevel(level);
        String perm = system.getLevelPermission(level);
        boolean hasPerm = (perm == null || player.hasPermission(perm));

        Material material;
        boolean glow = false;
        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.GRAY + "Level: " + ChatColor.WHITE + level);
        lore.add(ChatColor.GRAY + "Required XP: " + ChatColor.YELLOW + requiredXP);

        if (isCurrent) {
            int missingXP = Math.max(0, requiredXP - playerXP);
            lore.add(ChatColor.GRAY + "Your XP: " + ChatColor.LIGHT_PURPLE + playerXP + ChatColor.GRAY + "/" + ChatColor.YELLOW + requiredXP);
            lore.add(ChatColor.GRAY + "Missing XP: " + ChatColor.LIGHT_PURPLE + missingXP);
            if (!hasPerm) {
                lore.add(ChatColor.RED + "Requires permission: " + ChatColor.GRAY + perm);
            }
        } else if (!unlocked) {
            int missingXP = Math.max(0, requiredXP - playerXP);
            lore.add(ChatColor.GRAY + "Missing XP: " + ChatColor.LIGHT_PURPLE + missingXP);
            if (!hasPerm) {
                lore.add(ChatColor.RED + "Requires permission: " + ChatColor.GRAY + perm);
            }
        }

        List<String> rewardDisplay = system.getRewardDisplay(level);
        if (!rewardDisplay.isEmpty()) {
            lore.add("");
            lore.add(ChatColor.GOLD + "Reward:");
            for (String line : rewardDisplay) {
                lore.add(ChatColor.DARK_GRAY + "• " + ChatColor.WHITE + line);
            }
        }

        if (unlocked && claimed) {
            material = Material.LIME_STAINED_GLASS_PANE;
            lore.add("");
            lore.add(ChatColor.GREEN + "✔ Unlocked & Claimed");
        } else if (unlocked) {
            material = Material.YELLOW_STAINED_GLASS_PANE;
            lore.add("");
            lore.add(ChatColor.YELLOW + "Unlocked — Reward ready!");
            lore.add(ChatColor.GOLD + "» Click to claim reward!");
        } else if (isCurrent) {
            material = Material.YELLOW_STAINED_GLASS_PANE;
            glow = true;
            lore.add("");
            lore.add(ChatColor.YELLOW + "⚡ Currently at this level");
        } else {
            material = Material.GRAY_STAINED_GLASS_PANE;
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✖ Not reached yet");
        }

        String displayName = system.getDisplayForLevel(level);
        return createItem(material, displayName, lore, glow);
    }

    private ItemStack createItem(Material material, String name, List<String> lore, boolean glow) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            if (glow) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }

    public LevelSystemInstance getSystem() {
        return system;
    }

    public Player getPlayer() {
        return player;
    }

    public int getPage() {
        return page;
    }

    public Integer getLevelAtSlot(int slot) {
        return slotToLevel.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
