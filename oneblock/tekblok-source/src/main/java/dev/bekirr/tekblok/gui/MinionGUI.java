package dev.bekirr.tekblok.gui;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.Minion;
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

public class MinionGUI {

    public static final String MINION_GUI_TITLE = ColorUtils.color("&8TekBlok &7» &b&lKirici Minyon");

    public static void openMenu(Player player, TekBlokPlugin plugin, Minion minion) {
        if (minion == null) return;
        Inventory inv = Bukkit.createInventory(null, 27, MINION_GUI_TITLE);

        // Arka plan
        ItemStack darkGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, darkGlass);
        }

        // Slot 11: Bilgi Karti
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ColorUtils.color("&8-----------------------------"));
        String ownerName = Bukkit.getOfflinePlayer(minion.getOwnerUUID()).getName();
        infoLore.add(ColorUtils.color("&7Ada Sahibi: &e" + (ownerName != null ? ownerName : "Bilinmeyen")));
        infoLore.add(ColorUtils.color("&7Mevcut Seviye: &bSeviye " + minion.getLevel() + " &7/ &a3"));
        infoLore.add(ColorUtils.color("&7Kirilma Menzili: &a" + minion.getLevel() + " Blok Onu"));
        infoLore.add(ColorUtils.color("&7Toplam Kirilan: &e" + String.format("%,d", minion.getBlocksBroken()) + " Blok"));
        infoLore.add(ColorUtils.color("&8-----------------------------"));
        infoLore.add(ColorUtils.color("&7Kirilan kiriktaslar otomatik"));
        infoLore.add(ColorUtils.color("&7ada ciftcisine aktarilir."));
        inv.setItem(11, createItem(Material.DIAMOND_PICKAXE, ColorUtils.color("&b&lKirici Minyon Durumu"), infoLore));

        // Slot 13: Seviye Yukseltme
        int lvl = minion.getLevel();
        if (lvl < 3) {
            double cost = lvl == 1 ? 1250.0 : 2500.0;
            List<String> upLore = new ArrayList<>();
            upLore.add(ColorUtils.color("&8-----------------------------"));
            upLore.add(ColorUtils.color("&7Yeni Seviye: &bSeviye " + (lvl + 1)));
            upLore.add(ColorUtils.color("&7Yeni Menzil: &a" + (lvl + 1) + " Blok Onu"));
            upLore.add(ColorUtils.color("&7Gereken Bakiye: &e" + plugin.getEconomyManager().format(cost)));
            upLore.add(ColorUtils.color("&8-----------------------------"));
            upLore.add(ColorUtils.color("&a► Tiklayarak Seviye Yukselt"));
            inv.setItem(13, createItem(Material.EMERALD, ColorUtils.color("&a&l[ SEVIYE YUKSELT ]"), upLore));
        } else {
            List<String> maxLore = new ArrayList<>();
            maxLore.add(ColorUtils.color("&8-----------------------------"));
            maxLore.add(ColorUtils.color("&7Minyonunuz son seviyeye ulasti!"));
            maxLore.add(ColorUtils.color("&7Maksimum menzilde calisiyor (3 Blok)."));
            maxLore.add(ColorUtils.color("&8-----------------------------"));
            inv.setItem(13, createItem(Material.NETHER_STAR, ColorUtils.color("&6&l[ MAKSIMUM SEVIYE ]"), maxLore));
        }

        // Slot 15: Minyonu Kaldir
        List<String> removeLore = new ArrayList<>();
        removeLore.add(ColorUtils.color("&8-----------------------------"));
        removeLore.add(ColorUtils.color("&7Minyonu yerden kaldirir ve"));
        removeLore.add(ColorUtils.color("&7esya olarak envanterinize verir."));
        removeLore.add(ColorUtils.color("&8-----------------------------"));
        removeLore.add(ColorUtils.color("&c► Minyonu Kaldir ve Geri Al"));
        inv.setItem(15, createItem(Material.REDSTONE_BLOCK, ColorUtils.color("&c&l[ MINYONU KALDIR ]"), removeLore));

        // Slot 22: Kapat
        List<String> closeLore = new ArrayList<>();
        closeLore.add(ColorUtils.color("&7Menuyu kapatmak icin tiklayin."));
        inv.setItem(22, createItem(Material.BARRIER, ColorUtils.color("&cKapat"), closeLore));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
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
