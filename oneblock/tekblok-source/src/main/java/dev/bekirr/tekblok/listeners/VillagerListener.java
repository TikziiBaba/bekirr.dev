package dev.bekirr.tekblok.listeners;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.gui.SmelterGUI;
import dev.bekirr.tekblok.gui.VillagerTradeGUI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class VillagerListener implements Listener {

    private final TekBlokPlugin plugin;

    public VillagerListener(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof Villager)) return;

        if (!clicked.hasMetadata("tekblok_npc_type")) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        String type = clicked.getMetadata("tekblok_npc_type").get(0).asString();

        if (type.equalsIgnoreCase("eritici")) {
            SmelterGUI.open(player, plugin);
        } else if (type.equalsIgnoreCase("takasci")) {
            VillagerTradeGUI.open(player, plugin);
        } else if (type.equalsIgnoreCase("ada")) {
            plugin.getIslandManager().teleportToIsland(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVillagerDamage(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata("tekblok_npc_type")) {
            event.setCancelled(true);
        }
    }
}
