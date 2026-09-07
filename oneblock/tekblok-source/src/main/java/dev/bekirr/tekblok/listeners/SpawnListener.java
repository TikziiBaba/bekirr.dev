package dev.bekirr.tekblok.listeners;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class SpawnListener implements Listener {

    private final TekBlokPlugin plugin;

    public SpawnListener(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSpawnManager().isSpawnWorld(player.getWorld())) {
            return;
        }

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("spawn.void-protection", true)) {
            return;
        }

        double voidY = config.getDouble("spawn.void-fall-height", 20.0);
        if (player.getLocation().getY() <= voidY) {
            player.setFallDistance(0);
            if (plugin.getSpawnManager().isSpawnSet()) {
                player.teleport(plugin.getSpawnManager().getSpawnLocation());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.sendMessage(ColorUtils.color("&eLobi boşluğuna düştüğünüz için güvenle spawn'a ışınlandınız."));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (!plugin.getSpawnManager().isSpawnWorld(player.getWorld())) return;

        FileConfiguration config = plugin.getConfig();

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && config.getBoolean("spawn.protection.disable-fall-damage", true)) {
            event.setCancelled(true);
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID && config.getBoolean("spawn.void-protection", true)) {
            event.setCancelled(true);
            player.setFallDistance(0);
            if (plugin.getSpawnManager().isSpawnSet()) {
                player.teleport(plugin.getSpawnManager().getSpawnLocation());
            }
            return;
        }

        if (config.getBoolean("spawn.protection.disable-pvp", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent event) {
        if (!plugin.getSpawnManager().isSpawnWorld(event.getEntity().getWorld())) return;
        if (plugin.getConfig().getBoolean("spawn.protection.disable-pvp", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!plugin.getSpawnManager().isSpawnWorld(event.getEntity().getWorld())) return;
        if (plugin.getConfig().getBoolean("spawn.protection.disable-hunger", true)) {
            event.setCancelled(true);
            if (event.getEntity() instanceof Player) {
                ((Player) event.getEntity()).setFoodLevel(20);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSpawnManager().isSpawnWorld(event.getBlock().getWorld())) return;

        if (player.hasPermission("tekblok.admin")) return;

        if (plugin.getConfig().getBoolean("spawn.protection.disable-block-break", true)) {
            event.setCancelled(true);
            player.sendMessage(ColorUtils.color("&cLobi dünyasında blok kıramazsınız!"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSpawnManager().isSpawnWorld(event.getBlock().getWorld())) return;

        if (player.hasPermission("tekblok.admin")) return;

        if (plugin.getConfig().getBoolean("spawn.protection.disable-block-place", true)) {
            event.setCancelled(true);
            player.sendMessage(ColorUtils.color("&cLobi dünyasında blok koyamazsınız!"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!plugin.getSpawnManager().isSpawnWorld(event.getLocation().getWorld())) return;

        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        boolean firstJoinTp = config.getBoolean("spawn.first-join-teleport", true);
        boolean joinTp = config.getBoolean("spawn.join-teleport", false);

        if ((!player.hasPlayedBefore() && firstJoinTp) || joinTp) {
            if (plugin.getSpawnManager().isSpawnSet()) {
                player.teleport(plugin.getSpawnManager().getSpawnLocation());
            }
        }
    }
}
