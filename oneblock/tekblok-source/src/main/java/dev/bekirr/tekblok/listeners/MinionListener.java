package dev.bekirr.tekblok.listeners;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.gui.MinionGUI;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.Minion;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class MinionListener implements Listener {

    private final TekBlokPlugin plugin;

    public MinionListener(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!plugin.getMinionManager().isMinionItem(item)) return;

        event.setCancelled(true);

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Block target = clicked.getRelative(event.getBlockFace());
        if (target.getType() != Material.AIR && target.getType() != Material.CAVE_AIR) {
            player.sendMessage(ColorUtils.color("&cMinyon yerlestirmek icin secilen yerin bos olmasi gerekir!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        Island island = plugin.getIslandManager().getIslandByArea(target.getLocation());
        if (island == null || !island.isMemberOrOwner(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("messages.not-island-member"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        // Adada sadece 1 minyon kontrolü
        if (plugin.getMinionManager().getMinion(island.getOwnerUUID()) != null) {
            player.sendMessage(ColorUtils.color("&cAdanizda zaten bir minyon kurulu! (Her adaya sadece 1 minyon)"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        int level = plugin.getMinionManager().getMinionItemLevel(item);
        BlockFace facing = plugin.getMinionManager().getPlayerFacing(player);

        boolean placed = plugin.getMinionManager().placeMinion(player, target.getLocation(), facing, level);
        if (placed) {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof ArmorStand)) return;

        Minion minion = plugin.getMinionManager().getMinionByEntity(clicked.getUniqueId());
        if (minion == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        Island island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
        if (island == null || !island.getOwnerUUID().equals(minion.getOwnerUUID())) {
            player.sendMessage(ColorUtils.color("&cBu minyon sizin adaniza ait degil!"));
            return;
        }

        MinionGUI.openMenu(player, plugin, minion);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof ArmorStand)) return;

        Minion minion = plugin.getMinionManager().getMinionByEntity(clicked.getUniqueId());
        if (minion == null) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) return;
        if (plugin.getMinionManager().getMinionByEntity(event.getEntity().getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(MinionGUI.MINION_GUI_TITLE)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Island island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
        if (island == null) {
            player.closeInventory();
            return;
        }

        Minion minion = plugin.getMinionManager().getMinion(island.getOwnerUUID());
        if (minion == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();
        if (slot == 13) {
            // Seviye Yükselt
            if (plugin.getMinionManager().upgradeMinion(player, minion)) {
                MinionGUI.openMenu(player, plugin, minion);
            }
        } else if (slot == 15) {
            // Minyonu Yerden Kaldır
            if (plugin.getMinionManager().pickupMinion(player, minion)) {
                player.closeInventory();
            }
        } else if (slot == 22) {
            // Kapat
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
        }
    }
}
