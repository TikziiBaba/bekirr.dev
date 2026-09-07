package dev.bekirr.tekblok.listeners;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class VoidFallListener implements Listener {

    private final TekBlokPlugin plugin;

    public VoidFallListener(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null) return;

        World world = to.getWorld();
        if (world == null) return;

        String islandWorldName = plugin.getIslandManager().getOneBlockWorld().getName();
        String spawnWorldName = plugin.getConfig().getString("spawn.world", "spawn");

        // 1. Adada Void Kontrolü
        if (world.getName().equalsIgnoreCase(islandWorldName)) {
            boolean islandProtection = plugin.getConfig().getBoolean("island.void-protection", false);
            int islandFallHeight = plugin.getConfig().getInt("island.void-fall-height", 20);
            if (islandProtection && to.getY() <= islandFallHeight) {
                dev.bekirr.tekblok.models.Island is = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
                if (is == null) {
                    is = plugin.getIslandManager().getIslandByArea(to);
                }
                if (is != null) {
                    player.setFallDistance(0.0f);
                    player.teleport(is.getSpawnLocation());
                    player.sendMessage(ColorUtils.color("&eBoşluğa düşmekten kurtarıldınız!"));
                    return;
                }
            }
            if (to.getY() <= 0 && !player.isDead()) {
                player.setHealth(0.0);
            }
            return;
        }

        // 2. Lobi / Spawn Dünyasında Düşme Koruması (Doğrudan Spawn'a Kurtar)
        if (world.getName().equalsIgnoreCase(spawnWorldName)) {
            boolean spawnProtection = plugin.getConfig().getBoolean("spawn.void-protection", true);
            int spawnFallHeight = plugin.getConfig().getInt("spawn.void-fall-height", 20);
            if (spawnProtection && to.getY() <= spawnFallHeight) {
                player.setFallDistance(0.0f);
                plugin.getSpawnManager().teleportToSpawn(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVoidDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            Player player = (Player) event.getEntity();
            World world = player.getWorld();
            String islandWorldName = plugin.getIslandManager().getOneBlockWorld().getName();
            String spawnWorldName = plugin.getConfig().getString("spawn.world", "spawn");

            // Adada void hasarı iptal EDİLMEZ; oyuncu ölecek ve eşyaları gidecek.
            if (world.getName().equalsIgnoreCase(islandWorldName)) {
                return;
            }

            // Lobi dünyasında void hasarını engelle ve kurtar
            if (world.getName().equalsIgnoreCase(spawnWorldName)) {
                event.setCancelled(true);
                player.setFallDistance(0.0f);
                plugin.getSpawnManager().teleportToSpawn(player);
            }
        }
    }
}
