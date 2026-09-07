package dev.bekirr.tekblok.gui;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.MarketCategory;
import dev.bekirr.tekblok.models.MarketItem;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MarketGUI {

    public static final String MARKET_MAIN_TITLE = ColorUtils.color("&8TekBlok &7» &6Market");
    public static final String MARKET_CATEGORY_TITLE_PREFIX = ColorUtils.color("&8Market &7» ");

    public static void openMainMenu(Player player, TekBlokPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, MARKET_MAIN_TITLE);
        ItemStack grayGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, grayGlass);
        }

        // Bakiye göstergesi Slot 4
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        List<String> balLore = new ArrayList<>();
        balLore.add(ColorUtils.color("&7Mevcut Bakiyeniz: &a" + plugin.getEconomyManager().format(balance)));
        balLore.add(ColorUtils.color("&7Para kazanmak için &e/ciftci &7kullanabilirsiniz."));
        inv.setItem(4, createItem(Material.EMERALD, ColorUtils.color("&a&lCüzdan Durumu"), balLore));

        // Kategoriler
        for (MarketCategory cat : plugin.getMarketManager().getCategories()) {
            List<String> catLore = new ArrayList<>();
            catLore.add(ColorUtils.color("&8------------------------"));
            catLore.add(ColorUtils.color("&7" + cat.getDescription()));
            catLore.add(ColorUtils.color("&7Toplam Ürün: &e" + cat.getItems().size() + " adet"));
            catLore.add("");
            catLore.add(ColorUtils.color("&a► Kategoriyi Açmak İçin Tıkla"));
            catLore.add(ColorUtils.color("&8------------------------"));
            inv.setItem(cat.getSlot(), createItem(cat.getIcon(), ColorUtils.color(cat.getName()), catLore));
        }

        // Kapat Slot 22
        List<String> closeLore = new ArrayList<>();
        closeLore.add(ColorUtils.color("&7Menüyü kapatmak için tıklayın."));
        inv.setItem(22, createItem(Material.BARRIER, ColorUtils.color("&c&lKapat"), closeLore));

        player.openInventory(inv);
    }

    public static void openCategoryMenu(Player player, TekBlokPlugin plugin, MarketCategory category) {
        String title = MARKET_CATEGORY_TITLE_PREFIX + category.getName();
        Inventory inv = Bukkit.createInventory(null, 54, title);

        ItemStack grayGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, grayGlass);
        }

        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());

        int slot = 0;
        for (MarketItem item : category.getItems()) {
            if (slot >= 45) break;

            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.color("&8------------------------"));
            lore.add(ColorUtils.color("&7Birim Fiyat: &e" + plugin.getEconomyManager().format(item.getPrice())));
            lore.add("");
            lore.add(ColorUtils.color("&eSol Tık: &f1 Adet Al &8(&6" + plugin.getEconomyManager().format(item.getPrice()) + "&8)"));
            lore.add(ColorUtils.color("&eSağ Tık: &f16 Adet Al &8(&6" + plugin.getEconomyManager().format(item.getPrice() * 16) + "&8)"));
            lore.add(ColorUtils.color("&eShift + Sağ Tık: &f64 Adet Al &8(&6" + plugin.getEconomyManager().format(item.getPrice() * 64) + "&8)"));
            lore.add(ColorUtils.color("&8------------------------"));

            inv.setItem(slot++, createItem(item.getMaterial(), ColorUtils.color(item.getName()), lore, item.isCrateKey()));
        }

        // Bakiye göstergesi Slot 48
        List<String> balLore = new ArrayList<>();
        balLore.add(ColorUtils.color("&7Mevcut Bakiyeniz: &a" + plugin.getEconomyManager().format(balance)));
        inv.setItem(48, createItem(Material.EMERALD, ColorUtils.color("&a&lCüzdan Durumu"), balLore, false));

        // Geri Dön Slot 49
        List<String> backLore = new ArrayList<>();
        backLore.add(ColorUtils.color("&7Kategoriler menüsüne dönmek"));
        backLore.add(ColorUtils.color("&7için buraya tıklayın."));
        inv.setItem(49, createItem(Material.ARROW, ColorUtils.color("&c&lGeri Dön"), backLore, false));

        // Kapat Slot 50
        List<String> closeLore = new ArrayList<>();
        closeLore.add(ColorUtils.color("&7Menüyü kapatmak için tıklayın."));
        inv.setItem(50, createItem(Material.BARRIER, ColorUtils.color("&c&lKapat"), closeLore, false));

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material material, String name, List<String> lore) {
        return createItem(material, name, lore, false);
    }

    private static ItemStack createItem(Material material, String name, List<String> lore, boolean glow) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            if (glow) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
