package dev.bekirr.tekblok.listeners;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.TradeSession;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class TradeListener implements Listener {

    private final TekBlokPlugin plugin;

    public TradeListener(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.equals(TradeSession.TRADE_TITLE)) return;

        TradeSession session = plugin.getTradeManager().getSession(player);
        if (session == null || session.isCompleted() || session.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        int rawSlot = event.getRawSlot();

        // 1. İptal Butonu
        if (rawSlot == TradeSession.CANCEL_SLOT) {
            event.setCancelled(true);
            session.cancelTrade();
            return;
        }

        // 2. Oyuncu 1 Onay Butonları
        for (int slot : TradeSession.P1_BUTTON_SLOTS) {
            if (rawSlot == slot) {
                event.setCancelled(true);
                if (session.isPlayer1(player)) {
                    session.togglePlayer1Ready();
                } else {
                    player.sendMessage(ColorUtils.color("&cBu onay butonu diğer oyuncuya aittir!"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                }
                return;
            }
        }

        // 3. Oyuncu 2 Onay Butonları
        for (int slot : TradeSession.P2_BUTTON_SLOTS) {
            if (rawSlot == slot) {
                event.setCancelled(true);
                if (session.isPlayer2(player)) {
                    session.togglePlayer2Ready();
                } else {
                    player.sendMessage(ColorUtils.color("&cBu onay butonu diğer oyuncuya aittir!"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                }
                return;
            }
        }

        // 4. Ayırıcı ve Durum Camları (Engellenecek yuvalar)
        if (TradeSession.DIVIDER_SLOTS.contains(rawSlot) || (rawSlot >= 36 && rawSlot <= 39) || (rawSlot >= 41 && rawSlot <= 44)) {
            event.setCancelled(true);
            return;
        }

        // 5. Oyuncunun Kendi Envanteri (rawSlot >= 54)
        if (rawSlot >= 54) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
                ItemStack current = event.getCurrentItem();
                if (current != null && current.getType() != Material.AIR) {
                    Set<Integer> targetSlots = session.isPlayer1(player) ? TradeSession.P1_SLOTS : TradeSession.P2_SLOTS;
                    for (int targetSlot : targetSlots) {
                        ItemStack inTop = session.getInventory().getItem(targetSlot);
                        if (inTop == null || inTop.getType() == Material.AIR) {
                            session.getInventory().setItem(targetSlot, current.clone());
                            event.setCurrentItem(null);
                            session.resetReadyStatus();
                            return;
                        }
                    }
                    player.sendMessage(ColorUtils.color("&cTakas teklif alanınızda boş yer kalmadı!"));
                }
            }
            return;
        }

        // 6. Üst Takas Menüsü Tıklamaları (rawSlot < 54)
        if (TradeSession.P1_SLOTS.contains(rawSlot)) {
            if (!session.isPlayer1(player)) {
                event.setCancelled(true);
                player.sendMessage(ColorUtils.color("&cKarşı oyuncunun teklif eşyalarına dokunamazsınız!"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return;
            }
            Bukkit.getScheduler().runTask(plugin, session::resetReadyStatus);
        } else if (TradeSession.P2_SLOTS.contains(rawSlot)) {
            if (!session.isPlayer2(player)) {
                event.setCancelled(true);
                player.sendMessage(ColorUtils.color("&cKarşı oyuncunun teklif eşyalarına dokunamazsınız!"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return;
            }
            Bukkit.getScheduler().runTask(plugin, session::resetReadyStatus);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.equals(TradeSession.TRADE_TITLE)) return;

        TradeSession session = plugin.getTradeManager().getSession(player);
        if (session == null || session.isCompleted() || session.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        Set<Integer> allowedSlots = session.isPlayer1(player) ? TradeSession.P1_SLOTS : TradeSession.P2_SLOTS;
        for (int slot : event.getRawSlots()) {
            if (slot < 54 && !allowedSlots.contains(slot)) {
                event.setCancelled(true);
                return;
            }
        }

        Bukkit.getScheduler().runTask(plugin, session::resetReadyStatus);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        if (title.equals(TradeSession.TRADE_TITLE)) {
            TradeSession session = plugin.getTradeManager().getSession(player);
            if (session != null && !session.isCompleted() && !session.isCancelled()) {
                session.cancelTrade();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        TradeSession session = plugin.getTradeManager().getSession(player);
        if (session != null && !session.isCompleted() && !session.isCancelled()) {
            session.cancelTrade();
        }
    }
}
