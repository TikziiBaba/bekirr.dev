package dev.bekirr.tekblok.gui;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.Phase;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class IslandGUI {

    public static final String MAIN_GUI_TITLE = ColorUtils.color("&8TekBlok &7» &6Ada Menüsü");
    public static final String PHASES_GUI_TITLE = ColorUtils.color("&8TekBlok &7» &bAşama & Biyom Rehberi");
    public static final String TOP_GUI_TITLE = ColorUtils.color("&8TekBlok &7» &eTop 10 Sıralaması");

    public static void openMainMenu(Player player, TekBlokPlugin plugin) {
        Island island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 27, MAIN_GUI_TITLE);

        // Arka plan camları
        ItemStack grayGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, grayGlass);
        }

        // Slot 4: Market
        List<String> marketLore = new ArrayList<>();
        marketLore.add(ColorUtils.color("&7Tohum, yiyecek, yün, maden"));
        marketLore.add(ColorUtils.color("&7ve canavar eşyaları satın alın."));
        marketLore.add("");
        marketLore.add(ColorUtils.color("&6► Marketi açmak için tıkla"));
        inv.setItem(4, createItem(Material.GOLD_INGOT, ColorUtils.color("&6&lOyun İçi Market"), marketLore));

        // Slot 10: Adama Git
        List<String> tpLore = new ArrayList<>();
        tpLore.add(ColorUtils.color("&7Adanızın tek bloğuna ışınlanmak"));
        tpLore.add(ColorUtils.color("&7için buraya tıklayın."));
        tpLore.add("");
        tpLore.add(ColorUtils.color("&a► Işınlanmak için tıkla"));
        inv.setItem(10, createItem(Material.GRASS_BLOCK, ColorUtils.color("&a&lAdama Işınlan"), tpLore));

        // Slot 11: Aşama Bilgisi
        if (island != null) {
            Phase phase = plugin.getPhaseManager().getPhase(island.getCurrentPhaseId());
            int remaining = Math.max(0, phase.getRequiredBlocks() - island.getBlocksBroken());

            List<String> statsLore = new ArrayList<>();
            statsLore.add(ColorUtils.color("&8------------------------"));
            statsLore.add(ColorUtils.color("&7Mevcut Aşama: &e" + phase.getName()));
            statsLore.add(ColorUtils.color("&7Biyom: &b" + phase.getBiome().name()));
            statsLore.add(ColorUtils.color("&7Kırılan Blok: &a" + island.getBlocksBroken()));
            statsLore.add(ColorUtils.color("&7Gereken Blok: &b" + phase.getRequiredBlocks()));
            statsLore.add(ColorUtils.color("&7Kalan Blok: &c" + remaining));
            statsLore.add(ColorUtils.color("&8------------------------"));
            inv.setItem(11, createItem(Material.BOOK, ColorUtils.color("&6&lAşama İlerlemesi"), statsLore));
        } else {
            List<String> noIslandLore = new ArrayList<>();
            noIslandLore.add(ColorUtils.color("&cHenüz bir adanız yok!"));
            noIslandLore.add(ColorUtils.color("&e/tekblok git &7yazarak başlayın."));
            inv.setItem(11, createItem(Material.BARRIER, ColorUtils.color("&c&lAda Bulunamadı"), noIslandLore));
        }

        // Slot 12: Aşamalar Rehberi (15 Aşama)
        List<String> phasesLore = new ArrayList<>();
        phasesLore.add(ColorUtils.color("&77 farklı aşamayı, biyomları ve"));
        phasesLore.add(ColorUtils.color("&7blok türlerini detaylı inceleyin."));
        phasesLore.add("");
        phasesLore.add(ColorUtils.color("&e► Rehberi görmek için tıkla"));
        inv.setItem(12, createItem(Material.COMPASS, ColorUtils.color("&b&lAşamalar & Biyomlar"), phasesLore));

        // Slot 13: Ada Çiftçisi
        List<String> farmerLore = new ArrayList<>();
        farmerLore.add(ColorUtils.color("&7Tarlaları ve spawner ganimetlerini"));
        farmerLore.add(ColorUtils.color("&7otomatik toplar ve depolar."));
        farmerLore.add("");
        if (plugin.getEconomyManager() != null) {
            double bal = plugin.getEconomyManager().getBalance(player.getUniqueId());
            farmerLore.add(ColorUtils.color("&7Bakiyeniz: &a" + plugin.getEconomyManager().format(bal)));
            farmerLore.add("");
        }
        farmerLore.add(ColorUtils.color("&a► Çiftçi menüsünü açmak için tıkla"));
        inv.setItem(13, createItem(Material.HAY_BLOCK, ColorUtils.color("&2&lAda Çiftçisi"), farmerLore));

        // Slot 14: Top 10 Sıralama
        List<String> topLore = new ArrayList<>();
        topLore.add(ColorUtils.color("&7Sunucudaki en çok blok kıran"));
        topLore.add(ColorUtils.color("&7lider oyuncuları görüntüleyin."));
        topLore.add("");
        topLore.add(ColorUtils.color("&e► Sıralamayı görmek için tıkla"));
        inv.setItem(14, createItem(Material.EMERALD, ColorUtils.color("&e&lTop 10 Sıralaması"), topLore));

        // Slot 15: Üyeler ve Bilgi
        List<String> memberLore = new ArrayList<>();
        if (island != null) {
            memberLore.add(ColorUtils.color("&7Ada Sahibi: &e" + island.getOwnerName()));
            memberLore.add(ColorUtils.color("&7Üye Sayısı: &b" + island.getMembers().size()));
            memberLore.add("");
            memberLore.add(ColorUtils.color("&7Arkadaş eklemek için:"));
            memberLore.add(ColorUtils.color("&e/tekblok davet <oyuncu>"));
        } else {
            memberLore.add(ColorUtils.color("&cAdanız bulunmuyor."));
        }
        inv.setItem(15, createItem(Material.PLAYER_HEAD, ColorUtils.color("&6&lAda Üyeleri"), memberLore));

        // Slot 16: Adanı Sil
        List<String> deleteLore = new ArrayList<>();
        deleteLore.add(ColorUtils.color("&c&lDİKKAT: &7Adanızı ve tüm"));
        deleteLore.add(ColorUtils.color("&7ilerlemenizi kalıcı olarak siler!"));
        deleteLore.add("");
        deleteLore.add(ColorUtils.color("&4► Silme onayı başlatmak için tıkla"));
        inv.setItem(16, createItem(Material.TNT, ColorUtils.color("&4&lAdanı Sil"), deleteLore));

        // Slot 22: Kapat
        List<String> closeLore = new ArrayList<>();
        closeLore.add(ColorUtils.color("&7Menüyü kapatmak için tıklayın."));
        inv.setItem(22, createItem(Material.BARRIER, ColorUtils.color("&cKapat"), closeLore));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
    }

    public static void openPhasesMenu(Player player, TekBlokPlugin plugin) {
        Island island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
        int currentPhaseId = island != null ? island.getCurrentPhaseId() : 1;

        Inventory inv = Bukkit.createInventory(null, 36, PHASES_GUI_TITLE);

        // Arka plan
        ItemStack blackGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, blackGlass);
        }

        // 7 Aşama Yuvaları (Satır 2: 10..16)
        int[] slots = {10, 11, 12, 13, 14, 15, 16};

        int index = 0;
        for (Phase phase : plugin.getPhaseManager().getPhases().values()) {
            if (index >= slots.length) break;

            boolean isUnlocked = phase.getId() <= currentPhaseId;
            boolean isCurrent = phase.getId() == currentPhaseId;

            Material iconMat = isUnlocked ? phase.getIcon() : Material.GRAY_DYE;
            String status = isCurrent ? "&a&l[ŞU ANKİ AŞAMA]" : (isUnlocked ? "&2&l[TAMAMLANDI]" : "&c&l[KİLİTLİ]");

            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.color("&8------------------------"));
            lore.add(ColorUtils.color("&7Durum: " + status));
            lore.add(ColorUtils.color("&7Aşama: &f#" + phase.getId()));
            lore.add(ColorUtils.color("&7Biyom: &b" + phase.getBiome().name()));
            lore.add(ColorUtils.color("&7Hedef Blok: &e" + (phase.getRequiredBlocks() >= 999999 ? "Sonsuz" : phase.getRequiredBlocks())));
            lore.add(ColorUtils.color("&7Sandık Şansı: &6%" + (int) (phase.getChestChance() * 100)));
            lore.add(ColorUtils.color("&7Canavar Şansı: &c%" + (int) (phase.getMobChance() * 100)));
            lore.add(ColorUtils.color("&8------------------------"));

            inv.setItem(slots[index], createItem(iconMat, ColorUtils.color("&e" + phase.getName()), lore));
            index++;
        }

        // Geri Dön butonu (Slot 40)
        List<String> backLore = new ArrayList<>();
        backLore.add(ColorUtils.color("&7Ana menüye geri dönmek için tıkla."));
        inv.setItem(31, createItem(Material.ARROW, ColorUtils.color("&c« Geri Dön"), backLore));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
    }

    public static void openTopMenu(Player player, TekBlokPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 36, TOP_GUI_TITLE);

        ItemStack blackGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, blackGlass);
        }

        List<Island> sorted = new ArrayList<>(plugin.getIslandManager().getIslandsByOwner().values());
        sorted.sort((a, b) -> Integer.compare(b.getBlocksBroken(), a.getBlocksBroken()));

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 20, 22, 24};
        int count = Math.min(slots.length, sorted.size());

        for (int i = 0; i < count; i++) {
            Island is = sorted.get(i);
            Material icon;
            String rankPrefix;

            if (i == 0) {
                icon = Material.GOLD_BLOCK;
                rankPrefix = "&6&l#1. ";
            } else if (i == 1) {
                icon = Material.IRON_BLOCK;
                rankPrefix = "&f&l#2. ";
            } else if (i == 2) {
                icon = Material.BRICKS;
                rankPrefix = "&e&l#3. ";
            } else {
                icon = Material.PAPER;
                rankPrefix = "&7#" + (i + 1) + ". ";
            }

            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.color("&8------------------------"));
            lore.add(ColorUtils.color("&7Oyuncu: &f" + is.getOwnerName()));
            lore.add(ColorUtils.color("&7Kırılan Blok: &a" + String.format("%,d", is.getBlocksBroken())));
            lore.add(ColorUtils.color("&7Aşama: &eAşama " + is.getCurrentPhaseId()));
            lore.add(ColorUtils.color("&8------------------------"));
            lore.add(ColorUtils.color("&e► Adasını ziyaret etmek için sohbetten:"));
            lore.add(ColorUtils.color("&b/tekblok ziyaret " + is.getOwnerName()));

            inv.setItem(slots[i], createItem(icon, ColorUtils.color(rankPrefix + is.getOwnerName()), lore));
        }

        if (sorted.isEmpty()) {
            List<String> emptyLore = new ArrayList<>();
            emptyLore.add(ColorUtils.color("&7Henüz sunucuda kayıtlı ada yok."));
            inv.setItem(13, createItem(Material.BARRIER, ColorUtils.color("&cKayıt Yok"), emptyLore));
        }

        // Geri Dön butonu (Slot 31)
        List<String> backLore = new ArrayList<>();
        backLore.add(ColorUtils.color("&7Ana menüye geri dönmek için tıkla."));
        inv.setItem(31, createItem(Material.ARROW, ColorUtils.color("&c« Geri Dön"), backLore));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
    }

    private static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtils.color(name));
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
