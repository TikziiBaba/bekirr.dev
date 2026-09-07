package dev.bekirr.tekblok.listeners;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.gui.FarmerGUI;
import dev.bekirr.tekblok.managers.FarmerManager;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.IslandFarmer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class FarmerHarvestListener implements Listener {

    private final TekBlokPlugin plugin;

    public FarmerHarvestListener(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        Location loc = event.getBlock().getLocation();
        Island island = plugin.getIslandManager().getIslandByArea(loc);
        if (island == null) return;

        IslandFarmer farmer = plugin.getFarmerManager().getFarmer(island.getOwnerUUID());
        if (!farmer.isUnlocked() || !farmer.isAutoCollect()) return;

        Material newType = event.getNewState().getType();

        // 1. Balkabağı & Karpuz
        if (newType == Material.PUMPKIN) {
            farmer.addItem(Material.PUMPKIN, 1);
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR, false);
            return;
        }
        if (newType == Material.MELON) {
            farmer.addItem(Material.MELON, 1);
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR, false);
            return;
        }

        // 2. Kaktüs & Şeker Kamışı
        if (newType == Material.CACTUS) {
            farmer.addItem(Material.CACTUS, 1);
            event.setCancelled(true);
            return;
        }
        if (newType == Material.SUGAR_CANE) {
            farmer.addItem(Material.SUGAR_CANE, 1);
            event.setCancelled(true);
            return;
        }
        if (newType == Material.BAMBOO) {
            farmer.addItem(Material.BAMBOO, 1);
            event.setCancelled(true);
            return;
        }
        if (newType == Material.KELP || newType == Material.KELP_PLANT) {
            farmer.addItem(Material.KELP, 1);
            event.setCancelled(true);
            return;
        }

        // 3. Olgunlaşan tarlalar (Buğday, Havuç, Patates, Pancar, vb.)
        BlockData data = event.getNewState().getBlockData();
        if (data instanceof Ageable) {
            Ageable ageable = (Ageable) data;
            if (ageable.getAge() >= ageable.getMaximumAge()) {
                Material baseMat = event.getBlock().getType();

                if (baseMat == Material.WHEAT) {
                    farmer.addItem(Material.WHEAT, 1);
                    farmer.addItem(Material.WHEAT_SEEDS, 2);
                } else if (baseMat == Material.CARROTS) {
                    farmer.addItem(Material.CARROT, 3);
                } else if (baseMat == Material.POTATOES) {
                    farmer.addItem(Material.POTATO, 3);
                } else if (baseMat == Material.BEETROOTS) {
                    farmer.addItem(Material.BEETROOT, 2);
                    farmer.addItem(Material.BEETROOT_SEEDS, 1);
                } else if (baseMat == Material.NETHER_WART) {
                    farmer.addItem(Material.NETHER_WART, 3);
                } else if (baseMat == Material.COCOA) {
                    farmer.addItem(Material.COCOA_BEANS, 3);
                } else if (baseMat == Material.SWEET_BERRY_BUSH) {
                    farmer.addItem(Material.SWEET_BERRIES, 2);
                }

                // Hasat et ve otomatik olarak yeniden ek (yaşı 0'a çek)
                ageable.setAge(0);
                event.setCancelled(true);
                event.getBlock().setBlockData(ageable, false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) return;

        Island island = plugin.getIslandManager().getIslandByArea(entity.getLocation());
        if (island == null) return;

        IslandFarmer farmer = plugin.getFarmerManager().getFarmer(island.getOwnerUUID());
        if (!farmer.isUnlocked() || !farmer.isAutoCollect()) return;

        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack drop = iterator.next();
            if (drop != null && drop.getType() != Material.AIR) {
                farmer.addItem(drop.getType(), drop.getAmount());
                iterator.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(org.bukkit.event.player.PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item itemDrop = event.getItemDrop();
        Location loc = itemDrop.getLocation();

        Island island = plugin.getIslandManager().getIslandByArea(loc);
        if (island == null) return;

        IslandFarmer farmer = plugin.getFarmerManager().getFarmer(island.getOwnerUUID());
        if (!farmer.isUnlocked() || !farmer.isAutoCollect() || !farmer.isCollectGroundDrops()) return;

        ItemStack stack = itemDrop.getItemStack();
        if (stack != null && plugin.getFarmerManager().isFarmerItem(stack.getType())) {
            farmer.addItem(stack.getType(), stack.getAmount());
            itemDrop.remove();
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.5f);
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText("§a[Çiftçi] Yere atılan " + stack.getAmount() + "x " + FarmerGUI.formatMaterialName(stack.getType()) + " depolandı!"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Location loc = item.getLocation();

        Island island = plugin.getIslandManager().getIslandByArea(loc);
        if (island == null) return;

        IslandFarmer farmer = plugin.getFarmerManager().getFarmer(island.getOwnerUUID());
        if (!farmer.isUnlocked() || !farmer.isAutoCollect() || !farmer.isCollectGroundDrops()) return;

        ItemStack stack = item.getItemStack();
        if (stack != null && plugin.getFarmerManager().isFarmerItem(stack.getType())) {
            farmer.addItem(stack.getType(), stack.getAmount());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (clicked instanceof Villager) {
            Villager v = (Villager) clicked;
            if (FarmerManager.FARMER_NPC_NAME.equals(v.getCustomName())) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                Island island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
                if (island == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.no-island"));
                    return;
                }
                IslandFarmer farmer = plugin.getFarmerManager().getFarmer(island.getOwnerUUID());
                if (!farmer.isUnlocked()) {
                    FarmerGUI.openFarmerBuyMenu(player, plugin, island);
                } else {
                    FarmerGUI.openFarmerMenu(player, plugin, island);
                }
            }
        }
    }
}
