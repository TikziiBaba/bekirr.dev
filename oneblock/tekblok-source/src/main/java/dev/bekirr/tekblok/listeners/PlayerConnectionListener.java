package dev.bekirr.tekblok.listeners;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final TekBlokPlugin plugin;

    public PlayerConnectionListener(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Island island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
        String oneBlockWorldName = plugin.getIslandManager().getOneBlockWorld().getName();

        if (island != null) {
            if (island.getOwnerUUID().equals(player.getUniqueId())) {
                island.setOwnerName(player.getName());
            }
            plugin.getIslandManager().ensureOneBlockExists(island);
            plugin.getDisplayManager().updateBossBar(island);
        }

        // Eğer oyuncu TekBlokWorld dünyasında (adasında) değilse, doğrudan lobi spawn'ında başlat
        if (!player.getWorld().getName().equalsIgnoreCase(oneBlockWorldName)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    plugin.getSpawnManager().teleportToSpawn(player);
                }
            }, 5L);
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Island island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());

        // Kullanıcı Talebi: Aşağı düşüp ölünce adada doğayım
        if (island != null) {
            plugin.getIslandManager().ensureOneBlockExists(island);
            org.bukkit.Location respawnLoc = island.getSpawnLocation();
            if (respawnLoc != null && respawnLoc.getWorld() != null) {
                event.setRespawnLocation(respawnLoc);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        plugin.getDisplayManager().updateBossBar(island);
                    }
                }, 5L);
                return;
            }
        }

        // Adası yoksa lobi spawn noktasına gönder
        org.bukkit.Location spawnLoc = plugin.getSpawnManager().getSpawnLocation();
        if (spawnLoc != null && spawnLoc.getWorld() != null) {
            event.setRespawnLocation(spawnLoc);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getDisplayManager().removePlayer(event.getPlayer().getUniqueId());
    }
}
