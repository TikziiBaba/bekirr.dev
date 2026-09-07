package dev.bekirr.tekblok.gui;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public class SellGUI {

    public static final String SELL_TITLE = ColorUtils.color("&8» &2&lHızlı Satış Sandığı &8«");
    private static final Map<Material, Double> SPECIAL_PRICES = new EnumMap<>(Material.class);

    static {
        // Madenler (Kullanıcı Talebi: Maden ucuz olsun)
        SPECIAL_PRICES.put(Material.COAL, 0.5);
        SPECIAL_PRICES.put(Material.CHARCOAL, 0.5);
        SPECIAL_PRICES.put(Material.COAL_ORE, 0.5);
        SPECIAL_PRICES.put(Material.COAL_BLOCK, 4.5);

        SPECIAL_PRICES.put(Material.IRON_INGOT, 1.0);
        SPECIAL_PRICES.put(Material.IRON_ORE, 1.0);
        SPECIAL_PRICES.put(Material.IRON_NUGGET, 0.11);
        SPECIAL_PRICES.put(Material.IRON_BLOCK, 9.0);

        SPECIAL_PRICES.put(Material.GOLD_INGOT, 1.5);
        SPECIAL_PRICES.put(Material.GOLD_ORE, 1.5);
        SPECIAL_PRICES.put(Material.GOLD_NUGGET, 0.16);
        SPECIAL_PRICES.put(Material.GOLD_BLOCK, 13.5);

        SPECIAL_PRICES.put(Material.REDSTONE, 0.5);
        SPECIAL_PRICES.put(Material.REDSTONE_ORE, 0.5);
        SPECIAL_PRICES.put(Material.REDSTONE_BLOCK, 4.5);

        SPECIAL_PRICES.put(Material.LAPIS_LAZULI, 0.8);
        SPECIAL_PRICES.put(Material.LAPIS_ORE, 0.8);
        SPECIAL_PRICES.put(Material.LAPIS_BLOCK, 7.2);

        SPECIAL_PRICES.put(Material.QUARTZ, 0.8);
        SPECIAL_PRICES.put(Material.NETHER_QUARTZ_ORE, 0.8);
        SPECIAL_PRICES.put(Material.QUARTZ_BLOCK, 3.2);

        SPECIAL_PRICES.put(Material.DIAMOND, 3.0);
        SPECIAL_PRICES.put(Material.DIAMOND_ORE, 3.0);
        SPECIAL_PRICES.put(Material.DIAMOND_BLOCK, 27.0);

        SPECIAL_PRICES.put(Material.EMERALD, 4.0);
        SPECIAL_PRICES.put(Material.EMERALD_ORE, 4.0);
        SPECIAL_PRICES.put(Material.EMERALD_BLOCK, 36.0);

        SPECIAL_PRICES.put(Material.NETHERITE_SCRAP, 8.0);
        SPECIAL_PRICES.put(Material.ANCIENT_DEBRIS, 10.0);
        SPECIAL_PRICES.put(Material.NETHERITE_INGOT, 15.0);
        SPECIAL_PRICES.put(Material.NETHERITE_BLOCK, 135.0);

        // Taş ve Bloklar (0.10 ₺ - 0.20 ₺)
        SPECIAL_PRICES.put(Material.COBBLESTONE, 0.10);
        SPECIAL_PRICES.put(Material.STONE, 0.10);
        SPECIAL_PRICES.put(Material.ANDESITE, 0.10);
        SPECIAL_PRICES.put(Material.DIORITE, 0.10);
        SPECIAL_PRICES.put(Material.GRANITE, 0.10);
        SPECIAL_PRICES.put(Material.SANDSTONE, 0.15);
        SPECIAL_PRICES.put(Material.RED_SANDSTONE, 0.15);
        SPECIAL_PRICES.put(Material.DIRT, 0.10);
        SPECIAL_PRICES.put(Material.GRASS_BLOCK, 0.10);
        SPECIAL_PRICES.put(Material.GRAVEL, 0.12);
        SPECIAL_PRICES.put(Material.SAND, 0.12);
        SPECIAL_PRICES.put(Material.RED_SAND, 0.12);
        SPECIAL_PRICES.put(Material.CLAY, 0.15);
        SPECIAL_PRICES.put(Material.CLAY_BALL, 0.10);
        SPECIAL_PRICES.put(Material.GLASS, 0.15);
        SPECIAL_PRICES.put(Material.OBSIDIAN, 0.40);
        SPECIAL_PRICES.put(Material.CRYING_OBSIDIAN, 0.40);

        // Tarım ve Yiyecek (0.20 ₺ - 0.35 ₺)
        SPECIAL_PRICES.put(Material.WHEAT, 0.20);
        SPECIAL_PRICES.put(Material.WHEAT_SEEDS, 0.10);
        SPECIAL_PRICES.put(Material.CARROT, 0.20);
        SPECIAL_PRICES.put(Material.POTATO, 0.20);
        SPECIAL_PRICES.put(Material.BEETROOT, 0.25);
        SPECIAL_PRICES.put(Material.BEETROOT_SEEDS, 0.10);
        SPECIAL_PRICES.put(Material.MELON_SLICE, 0.15);
        SPECIAL_PRICES.put(Material.MELON, 0.30);
        SPECIAL_PRICES.put(Material.PUMPKIN, 0.30);
        SPECIAL_PRICES.put(Material.SUGAR_CANE, 0.25);
        SPECIAL_PRICES.put(Material.CACTUS, 0.20);
        SPECIAL_PRICES.put(Material.BAMBOO, 0.15);
        SPECIAL_PRICES.put(Material.COCOA_BEANS, 0.25);
        SPECIAL_PRICES.put(Material.NETHER_WART, 0.35);
        SPECIAL_PRICES.put(Material.SWEET_BERRIES, 0.15);
        SPECIAL_PRICES.put(Material.APPLE, 0.25);
        SPECIAL_PRICES.put(Material.BREAD, 0.30);
        SPECIAL_PRICES.put(Material.COOKED_BEEF, 0.35);
        SPECIAL_PRICES.put(Material.COOKED_PORKCHOP, 0.35);
        SPECIAL_PRICES.put(Material.COOKED_MUTTON, 0.35);
        SPECIAL_PRICES.put(Material.COOKED_CHICKEN, 0.30);
        SPECIAL_PRICES.put(Material.BEEF, 0.20);
        SPECIAL_PRICES.put(Material.PORKCHOP, 0.20);
        SPECIAL_PRICES.put(Material.MUTTON, 0.20);
        SPECIAL_PRICES.put(Material.CHICKEN, 0.15);

        // Canavar & Avcı Ganimetleri (0.25 ₺ - 0.40 ₺)
        SPECIAL_PRICES.put(Material.ROTTEN_FLESH, 0.20);
        SPECIAL_PRICES.put(Material.BONE, 0.25);
        SPECIAL_PRICES.put(Material.STRING, 0.25);
        SPECIAL_PRICES.put(Material.SPIDER_EYE, 0.30);
        SPECIAL_PRICES.put(Material.GUNPOWDER, 0.35);
        SPECIAL_PRICES.put(Material.ARROW, 0.15);
        SPECIAL_PRICES.put(Material.FEATHER, 0.15);
        SPECIAL_PRICES.put(Material.LEATHER, 0.30);
        SPECIAL_PRICES.put(Material.ENDER_PEARL, 0.40);
        SPECIAL_PRICES.put(Material.BLAZE_ROD, 0.40);
        SPECIAL_PRICES.put(Material.GHAST_TEAR, 0.40);
        SPECIAL_PRICES.put(Material.SLIME_BALL, 0.35);
        SPECIAL_PRICES.put(Material.MAGMA_CREAM, 0.35);
        SPECIAL_PRICES.put(Material.INK_SAC, 0.20);
    }

    public static void open(Player player, TekBlokPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, SELL_TITLE);
        player.openInventory(inv);
        player.sendMessage(ColorUtils.color("&a&l[SAT] &fSatmak istediğiniz eşyaları sandığa koyun ve menüyü kapatın!"));
    }

    public static double getItemPrice(Material mat) {
        if (mat == null || mat.isAir()) return 0.0;

        // Özel belirlenen fiyat varsa onu kullan
        if (SPECIAL_PRICES.containsKey(mat)) {
            return SPECIAL_PRICES.get(mat);
        }

        // Maden veya cevher türevi olup gözden kaçanlar için ucuz fiyatlandırma
        String name = mat.name();
        if (name.contains("ORE")) {
            return 1.0;
        }
        if (name.contains("INGOT")) {
            return 1.5;
        }
        if (name.contains("LOG") || name.contains("WOOD") || name.contains("STEM")) {
            return 0.20;
        }
        if (name.contains("PLANKS") || name.contains("SLAB") || name.contains("STAIRS")) {
            return 0.10;
        }
        if (name.contains("LEAVES") || name.contains("SAPLING")) {
            return 0.10;
        }
        if (name.contains("WOOL") || name.contains("CARPET")) {
            return 0.15;
        }

        // Kullanıcı Talebi: Maden harici şeyler 0.10 ile 0.40 arası değerine göre para versin, her şey satılsın.
        // Taban fiyat 0.15 ₺, en fazla 0.40 ₺
        int hash = Math.abs(mat.name().hashCode());
        double variedPrice = 0.10 + ((hash % 30) / 100.0); // 0.10 ile 0.39 arasında deterministik fiyat
        return Math.round(variedPrice * 100.0) / 100.0;
    }

    public static void handleClose(InventoryCloseEvent event, TekBlokPlugin plugin) {
        Inventory inv = event.getInventory();
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        int totalCount = 0;
        double totalEarned = 0.0;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                int amount = item.getAmount();
                double unitPrice = getItemPrice(item.getType());
                double itemTotal = unitPrice * amount;

                totalCount += amount;
                totalEarned += itemTotal;
                inv.setItem(i, null); // Satıldı, sandıktan sil
            }
        }

        if (totalCount > 0 && totalEarned > 0) {
            plugin.getEconomyManager().addBalance(player.getUniqueId(), totalEarned);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            player.sendMessage(ColorUtils.color("&a&l[SAT] &fToplam &e" + totalCount + " &fadet eşya satıldı!"));
            player.sendMessage(ColorUtils.color("&a&l[SAT] &fHesabınıza aktarılan bakiye: &a+" + plugin.getEconomyManager().format(totalEarned)));
        }
    }
}
