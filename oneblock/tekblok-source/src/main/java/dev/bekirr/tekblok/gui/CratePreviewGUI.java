package dev.bekirr.tekblok.gui;

import dev.bekirr.tekblok.models.Crate;
import dev.bekirr.tekblok.models.CrateReward;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CratePreviewGUI {

    public static final String PREVIEW_PREFIX = ColorUtils.color("&8Kasa İnceleme &7» ");

    public static void open(Player player, Crate crate) {
        String title = PREVIEW_PREFIX + crate.getDisplayName();
        Inventory inv = Bukkit.createInventory(null, 27, title);

        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 18; i < 27; i++) inv.setItem(i, border);

        int slot = 9;
        for (CrateReward reward : crate.getRewards()) {
            if (slot > 17) break;

            ItemStack display = reward.buildItemStack();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(ColorUtils.color("&8------------------------"));
                lore.add(ColorUtils.color("&7Miktar: &e" + reward.getAmount() + " adet"));
                lore.add(ColorUtils.color("&7Kazanma Şansı: &a%" + reward.getChance()));
                lore.add(ColorUtils.color("&8------------------------"));
                meta.setLore(lore);
                display.setItemMeta(meta);
            }

            inv.setItem(slot++, display);
        }

        // Kapat Butonu
        List<String> closeLore = new ArrayList<>();
        closeLore.add(ColorUtils.color("&7Menüyü kapatmak için tıklayın."));
        inv.setItem(22, createItem(Material.BARRIER, ColorUtils.color("&c&lKapat"), closeLore));

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            is.setItemMeta(meta);
        }
        return is;
    }
}
