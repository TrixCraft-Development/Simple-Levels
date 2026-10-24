package de.nitrox.levelsystem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {

    private final LevelSystem plugin;

    public PlayerListener(LevelSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getManager().updateXpBar(e.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        plugin.getManager().updateXpBar(e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getManager().updateXpBar(player), 1L);
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent e) {
        Player player = e.getPlayer();
        if (plugin.getManager().hasActiveXpBarInWorld(player.getWorld().getName())) {
            e.setAmount(0);
            plugin.getManager().updateXpBar(player);
        }
    }
}