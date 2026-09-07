package dev.bekirr.tekblok.listeners;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.gui.CrateOpeningGUI;
import dev.bekirr.tekblok.gui.CratePreviewGUI;
import dev.bekirr.tekblok.gui.FarmerGUI;
import dev.bekirr.tekblok.gui.IslandGUI;
import dev.bekirr.tekblok.gui.MarketGUI;
import dev.bekirr.tekblok.gui.SmelterGUI;
import dev.bekirr.tekblok.gui.VillagerTradeGUI;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.IslandFarmer;
import dev.bekirr.tekblok.models.MarketCategory;
import dev.bekirr.tekblok.models.MarketItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class InventoryClickListener implements Listener {

    private final TekBlokPlugin plugin;

    public InventoryClickListener(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // 1. Ana Ada Menüsü
        if (title.equals(IslandGUI.MAIN_GUI_TITLE)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            if (slot == 4) { // Market
                MarketGUI.openMainMenu(player, plugin);
            } else if (slot == 10) { // Adama Işınlan
                player.closeInventory();
                Island island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
                if (island == null) {
                    island = plugin.getIslandManager().createIsland(player);
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.island-created"));
                } else {
                    plugin.getIslandManager().ensureOneBlockExists(island);
                }
                player.teleport(island.getSpawnLocation());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.sendMessage(plugin.getConfigManager().getMessage("messages.teleported-to-island"));
            } else if (slot == 12) { // Aşamalar Rehberi
                IslandGUI.openPhasesMenu(player, plugin);
            } else if (slot == 13) { // Ada Çiftçisi
                Island island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
                if (island == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.no-island"));
                    player.closeInventory();
                    return;
                }
                IslandFarmer farmer = plugin.getFarmerManager().getFarmer(island.getOwnerUUID());
                if (!farmer.isUnlocked()) {
                    FarmerGUI.openFarmerBuyMenu(player, plugin, island);
                } else {
                    FarmerGUI.openFarmerMenu(player, plugin, island);
                }
            } else if (slot == 14) { // Top 10 Sıralama
                IslandGUI.openTopMenu(player, plugin);
            } else if (slot == 16) { // Adanı Sil
                player.closeInventory();
                player.performCommand("tekblok sil");
            } else if (slot == 22) { // Kapat
                player.closeInventory();
            }
            return;
        }

        // 2. Aşamalar Menüsü
        if (title.equals(IslandGUI.PHASES_GUI_TITLE)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 31 || slot == 40) { // Geri Dön
                IslandGUI.openMainMenu(player, plugin);
            }
            return;
        }

        // 3. Top 10 Menüsü
        if (title.equals(IslandGUI.TOP_GUI_TITLE)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 31) { // Geri Dön
                IslandGUI.openMainMenu(player, plugin);
            }
            return;
        }

        // 4. Çiftçi Satın Alma Menüsü
        if (title.equals(FarmerGUI.FARMER_BUY_TITLE)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            Island island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
            if (island == null) {
                player.closeInventory();
                return;
            }

            if (slot == 11) { // Satın Al Onayla
                boolean bought = plugin.getFarmerManager().buyFarmer(player, island);
                if (bought) {
                    FarmerGUI.openFarmerMenu(player, plugin, island);
                }
            } else if (slot == 15) { // İptal Et
                player.closeInventory();
                player.sendMessage(plugin.getConfigManager().getMessage("messages.farmer-buy-cancelled"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            }
            return;
        }

        // 5. Çiftçi Depolama ve Satış Menüsü
        if (title.equals(FarmerGUI.FARMER_GUI_TITLE)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            Island island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
            if (island == null) {
                player.closeInventory();
                return;
            }
            IslandFarmer farmer = plugin.getFarmerManager().getFarmer(island.getOwnerUUID());

            if (slot == 46) { // Çiftçiyi Buraya Getir
                plugin.getFarmerManager().spawnOrMoveFarmerNPC(island, player.getLocation());
                player.sendMessage(plugin.getConfigManager().getMessage("messages.farmer-summoned"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
                player.closeInventory();
            } else if (slot == 48) { // Tümünü Sat
                double earned = plugin.getFarmerManager().sellAll(player, farmer);
                if (earned > 0) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.farmer-sold-all")
                            .replace("{amount}", plugin.getEconomyManager().format(earned)));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.farmer-empty"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                }
                FarmerGUI.openFarmerMenu(player, plugin, island);
            } else if (slot == 50) { // Otomatik Toplama Aç/Kapa
                farmer.setAutoCollect(!farmer.isAutoCollect());
                plugin.getFarmerManager().saveFarmers();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                FarmerGUI.openFarmerMenu(player, plugin, island);
            } else if (slot == 51) { // Yere Atılanları Topla Aç/Kapa
                farmer.setCollectGroundDrops(!farmer.isCollectGroundDrops());
                plugin.getFarmerManager().saveFarmers();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                FarmerGUI.openFarmerMenu(player, plugin, island);
            } else if (slot == 52) { // Kapat
                player.closeInventory();
            } else {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() != Material.AIR && clickedItem.getType() != Material.BLACK_STAINED_GLASS_PANE) {
                    Material mat = clickedItem.getType();
                    if (plugin.getFarmerManager().isFarmerItem(mat)) {
                        int count = farmer.getItemCount(mat);
                        if (count <= 0) {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                            return;
                        }

                        ClickType click = event.getClick();

                        // 1. SAĞ TIK İLE SATIŞ
                        if (click == ClickType.RIGHT) {
                            // 1 Adet Tekli Sat
                            double earned = plugin.getFarmerManager().sellSingleItem(player, farmer, mat);
                            if (earned > 0) {
                                player.sendMessage(plugin.getConfigManager().getMessage("messages.farmer-sold-item")
                                        .replace("{item}", clickedItem.getItemMeta().getDisplayName())
                                        .replace("{amount}", plugin.getEconomyManager().format(earned)));
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                                FarmerGUI.openFarmerMenu(player, plugin, island);
                            }
                        } else if (click == ClickType.SHIFT_RIGHT) {
                            // O Ürünün Hepsini Sat
                            double earned = plugin.getFarmerManager().sellItem(player, farmer, mat);
                            if (earned > 0) {
                                player.sendMessage(plugin.getConfigManager().getMessage("messages.farmer-sold-item")
                                        .replace("{item}", clickedItem.getItemMeta().getDisplayName())
                                        .replace("{amount}", plugin.getEconomyManager().format(earned)));
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                                FarmerGUI.openFarmerMenu(player, plugin, island);
                            }
                        }

                        // 2. SOL TIK İLE 1 ADET AL
                        else if (click == ClickType.LEFT) {
                            int give = 1;
                            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(mat, give));
                            int actuallyGiven = give - (leftover.isEmpty() ? 0 : leftover.values().iterator().next().getAmount());
                            if (actuallyGiven > 0) {
                                farmer.removeItem(mat, actuallyGiven);
                                plugin.getFarmerManager().saveFarmers();
                                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
                                FarmerGUI.openFarmerMenu(player, plugin, island);
                            } else {
                                player.sendMessage(plugin.getConfigManager().getMessage("messages.market-inventory-full"));
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                            }
                        }

                        // 3. Q TUŞU İLE 64 ADET AL (STACK)
                        else if (click == ClickType.DROP || click == ClickType.CONTROL_DROP) {
                            int give = Math.min(count, 64);
                            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(mat, give));
                            int actuallyGiven = give - (leftover.isEmpty() ? 0 : leftover.values().iterator().next().getAmount());
                            if (actuallyGiven > 0) {
                                farmer.removeItem(mat, actuallyGiven);
                                plugin.getFarmerManager().saveFarmers();
                                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
                                FarmerGUI.openFarmerMenu(player, plugin, island);
                            } else {
                                player.sendMessage(plugin.getConfigManager().getMessage("messages.market-inventory-full"));
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                            }
                        }

                        // 4. SHIFT + SOL TIK İLE ENVANTER DOLUSU AL
                        else if (click == ClickType.SHIFT_LEFT || click == ClickType.MIDDLE) {
                            int transferred = 0;
                            while (farmer.getItemCount(mat) > 0) {
                                int toTry = Math.min(farmer.getItemCount(mat), 64);
                                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(mat, toTry));
                                if (leftover.isEmpty()) {
                                    farmer.removeItem(mat, toTry);
                                    transferred += toTry;
                                } else {
                                    int unadded = leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
                                    int added = toTry - unadded;
                                    if (added > 0) {
                                        farmer.removeItem(mat, added);
                                        transferred += added;
                                    }
                                    break;
                                }
                            }
                            if (transferred > 0) {
                                plugin.getFarmerManager().saveFarmers();
                                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
                                FarmerGUI.openFarmerMenu(player, plugin, island);
                            } else {
                                player.sendMessage(plugin.getConfigManager().getMessage("messages.market-inventory-full"));
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                            }
                        }
                    }
                }
            }
            return;
        }

        // 6. Market Ana Kategoriler Menüsü
        if (title.equals(MarketGUI.MARKET_MAIN_TITLE)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            if (slot == 22) { // Kapat
                player.closeInventory();
                return;
            }

            for (MarketCategory cat : plugin.getMarketManager().getCategories()) {
                if (cat.getSlot() == slot) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    MarketGUI.openCategoryMenu(player, plugin, cat);
                    return;
                }
            }
            return;
        }

        // 7. Market Kategori İçi Eşya Satın Alma Menüsü
        if (title.startsWith(MarketGUI.MARKET_CATEGORY_TITLE_PREFIX)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            if (slot == 49) { // Geri Dön
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                MarketGUI.openMainMenu(player, plugin);
                return;
            }

            if (slot == 50) { // Kapat
                player.closeInventory();
                return;
            }

            if (slot < 0 || slot >= 45) {
                return;
            }

            // Hangi kategori olduğunu bul
            for (MarketCategory cat : plugin.getMarketManager().getCategories()) {
                String expectedTitle = MarketGUI.MARKET_CATEGORY_TITLE_PREFIX + cat.getName();
                if (title.equals(expectedTitle)) {
                    if (slot < cat.getItems().size()) {
                        MarketItem item = cat.getItems().get(slot);
                        int amount = 1;
                        if (event.isRightClick()) {
                            if (event.isShiftClick()) {
                                amount = 64;
                            } else {
                                amount = 16;
                            }
                        } else if (event.isLeftClick()) {
                            amount = 1;
                        }

                        boolean bought = plugin.getMarketManager().buyItem(player, item, amount);
                        if (bought) {
                            MarketGUI.openCategoryMenu(player, plugin, cat);
                        }
                    }
                    return;
                }
            }
            return;
        }

        // 8. Maden Eritici Menüsü
        if (title.equals(SmelterGUI.SMELTER_TITLE)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            if (slot == 22) { // Tüm Çantayı Erit
                SmelterGUI.smeltAll(player, plugin);
                SmelterGUI.open(player, plugin);
                return;
            }

            for (SmelterGUI.Conversion conv : SmelterGUI.CONVERSIONS) {
                if (conv.slot == slot) {
                    int amount = 1;
                    if (event.isRightClick()) {
                        if (event.isShiftClick()) {
                            amount = 64;
                        } else {
                            amount = 16;
                        }
                    } else if (event.isLeftClick()) {
                        amount = 1;
                    }
                    SmelterGUI.smeltSingleType(player, plugin, conv, amount);
                    SmelterGUI.open(player, plugin);
                    return;
                }
            }
            return;
        }

        // 9. Tüccar Takas Menüsü
        if (title.equals(VillagerTradeGUI.TRADER_TITLE)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            for (VillagerTradeGUI.TradeOffer offer : VillagerTradeGUI.OFFERS) {
                if (offer.slot == slot) {
                    VillagerTradeGUI.doTrade(player, plugin, offer);
                    VillagerTradeGUI.open(player, plugin);
                    return;
                }
            }
            return;
        }

        // 10. Kasa İnceleme Menüsü
        if (title.startsWith(CratePreviewGUI.PREVIEW_PREFIX)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 22) {
                player.closeInventory();
            }
            return;
        }

        // 11. Kasa Açılış Animasyonu Menüsü (Eşya çekilmesini engelle)
        if (title.startsWith(CrateOpeningGUI.OPENING_PREFIX)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (title.equals(dev.bekirr.tekblok.gui.SellGUI.SELL_TITLE)) {
            dev.bekirr.tekblok.gui.SellGUI.handleClose(event, plugin);
        }
    }
}
