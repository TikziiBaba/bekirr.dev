package dev.bekirr.tekblok.gui;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.IslandFarmer;
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

public class FarmerGUI {

    public static final String FARMER_GUI_TITLE = ColorUtils.color("&8TekBlok &7» &2Ada Çiftçisi");
    public static final String FARMER_BUY_TITLE = ColorUtils.color("&8TekBlok &7» &6Çiftçi Satın Al");

    public static void openFarmerBuyMenu(Player player, TekBlokPlugin plugin, Island island) {
        if (island == null) return;
        Inventory inv = Bukkit.createInventory(null, 27, FARMER_BUY_TITLE);

        ItemStack blackGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, blackGlass);
        }

        double price = plugin.getConfig().getDouble("farmer.buy-price", 5000.0);
        double bal = plugin.getEconomyManager().getBalance(player.getUniqueId());

        // Slot 11: Bilgi Kartı
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ColorUtils.color("&8-----------------------------"));
        infoLore.add(ColorUtils.color("&7Adanızdaki tarım ve spawner süreçlerini"));
        infoLore.add(ColorUtils.color("&7tamamen otomatikleştirin:"));
        infoLore.add("");
        infoLore.add(ColorUtils.color("&a✔ Tarlaları olgunlaştıkça hasat eder & eker"));
        infoLore.add(ColorUtils.color("&a✔ Canavar/Spawner ganimetlerini depolar"));
        infoLore.add(ColorUtils.color("&a✔ Ürünleri tek tıkla satıp para kazandırır"));
        infoLore.add(ColorUtils.color("&8-----------------------------"));
        infoLore.add(ColorUtils.color("&7Satın Alma Ücreti: &e" + plugin.getEconomyManager().format(price)));
        infoLore.add(ColorUtils.color("&7Mevcut Bakiyeniz: &a" + plugin.getEconomyManager().format(bal)));
        infoLore.add(ColorUtils.color("&8-----------------------------"));
        inv.setItem(11, createItem(Material.HAY_BLOCK, ColorUtils.color("&6&lAda Çiftçisi Lisansı"), infoLore));

        // Slot 13: Satın Al Butonu
        List<String> buyLore = new ArrayList<>();
        buyLore.add(ColorUtils.color("&8-----------------------------"));
        buyLore.add(ColorUtils.color("&7Ücret: &e" + plugin.getEconomyManager().format(price)));
        buyLore.add("");
        if (bal >= price) {
            buyLore.add(ColorUtils.color("&a► Tıkla ve Çiftçiyi Satın Al"));
        } else {
            buyLore.add(ColorUtils.color("&cYetersiz Bakiye! Gereken: " + plugin.getEconomyManager().format(price - bal)));
        }
        buyLore.add(ColorUtils.color("&8-----------------------------"));
        inv.setItem(13, createItem(Material.EMERALD_BLOCK, ColorUtils.color("&a&l[ ✔ ÇİFTÇİYİ SATIN AL ]"), buyLore));

        // Slot 15: Vazgeç Butonu
        List<String> cancelLore = new ArrayList<>();
        cancelLore.add(ColorUtils.color("&7Satın alma işlemini iptal et"));
        cancelLore.add(ColorUtils.color("&7ve menüyü kapat."));
        inv.setItem(15, createItem(Material.BARRIER, ColorUtils.color("&c&l[ ✖ VAZGEÇ ]"), cancelLore));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }

    public static void openFarmerMenu(Player player, TekBlokPlugin plugin, Island island) {
        if (island == null) return;
        IslandFarmer farmer = plugin.getFarmerManager().getFarmer(island.getOwnerUUID());
        Inventory inv = Bukkit.createInventory(null, 54, FARMER_GUI_TITLE);

        // Arka plan
        ItemStack darkGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, darkGlass);
        }

        // 1. Tarla Ürünleri (14 Yuva)
        Material[] cropMats = {
                Material.WHEAT, Material.CARROT, Material.POTATO, Material.BEETROOT,
                Material.MELON_SLICE, Material.PUMPKIN, Material.SUGAR_CANE,
                Material.CACTUS, Material.NETHER_WART, Material.COCOA_BEANS,
                Material.RED_MUSHROOM, Material.BROWN_MUSHROOM, Material.SWEET_BERRIES, Material.BAMBOO
        };
        int[] cropSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

        for (int i = 0; i < cropMats.length; i++) {
            Material mat = cropMats[i];
            int count = farmer.getItemCount(mat);
            double price = plugin.getFarmerManager().getPrice(mat);
            double total = count * price;

            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.color("&8-----------------------------"));
            if (count > 0) {
                lore.add(ColorUtils.color("&7Depodaki Miktar: &a" + String.format("%,d", count) + " Adet"));
                lore.add(ColorUtils.color("&7Birim Satış Fiyatı: &e" + plugin.getEconomyManager().format(price)));
                lore.add(ColorUtils.color("&7Toplam Satış Değeri: &6" + plugin.getEconomyManager().format(total)));
                lore.add(ColorUtils.color("&e► Sağ Tık: &f1 Adet Sat &8(&6" + plugin.getEconomyManager().format(price) + "&8)"));
                lore.add(ColorUtils.color("&6► Shift + Sağ Tık: &fHepsini Sat &8(&6" + plugin.getEconomyManager().format(total) + "&8)"));
                lore.add(ColorUtils.color("&b► Sol Tık: &f1 Adet Al (Tekli Çek)"));
                lore.add(ColorUtils.color("&3► Q Tuşu: &f64 Adet Al (Stack Çek)"));
                lore.add(ColorUtils.color("&d► Shift + Sol Tık: &fEnvanter Dolusu Al"));
            } else {
                lore.add(ColorUtils.color("&7Depodaki Miktar: &c0 Adet"));
                lore.add(ColorUtils.color("&7Birim Satış Fiyatı: &e" + plugin.getEconomyManager().format(price)));
                lore.add(ColorUtils.color("&8-----------------------------"));
                lore.add(ColorUtils.color("&7Tarlalar büyüdüğünde veya"));
                lore.add(ColorUtils.color("&7yere atıldığında otomatik toplanır."));
            }

            inv.setItem(cropSlots[i], createItem(mat, ColorUtils.color("&a" + formatMaterialName(mat)), lore));
        }

        // 2. Canavar & Spawner Ganimetleri (14 Yuva)
        Material[] mobMats = {
                Material.ROTTEN_FLESH, Material.BONE, Material.STRING, Material.GUNPOWDER,
                Material.SPIDER_EYE, Material.ENDER_PEARL, Material.BLAZE_ROD, Material.IRON_INGOT,
                Material.GOLD_INGOT, Material.SLIME_BALL, Material.MAGMA_CREAM, Material.LEATHER,
                Material.FEATHER, Material.GHAST_TEAR
        };
        int[] mobSlots = {28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

        for (int i = 0; i < mobMats.length; i++) {
            Material mat = mobMats[i];
            int count = farmer.getItemCount(mat);
            double price = plugin.getFarmerManager().getPrice(mat);
            double total = count * price;

            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.color("&8-----------------------------"));
            if (count > 0) {
                lore.add(ColorUtils.color("&7Depodaki Miktar: &a" + String.format("%,d", count) + " Adet"));
                lore.add(ColorUtils.color("&7Birim Satış Fiyatı: &e" + plugin.getEconomyManager().format(price)));
                lore.add(ColorUtils.color("&7Toplam Satış Değeri: &6" + plugin.getEconomyManager().format(total)));
                lore.add(ColorUtils.color("&8-----------------------------"));
                lore.add(ColorUtils.color("&e► Sağ Tık: &f1 Adet Sat &8(&6" + plugin.getEconomyManager().format(price) + "&8)"));
                lore.add(ColorUtils.color("&6► Shift + Sağ Tık: &fHepsini Sat &8(&6" + plugin.getEconomyManager().format(total) + "&8)"));
                lore.add(ColorUtils.color("&b► Sol Tık: &f1 Adet Al (Tekli Çek)"));
                lore.add(ColorUtils.color("&3► Q Tuşu: &f64 Adet Al (Stack Çek)"));
                lore.add(ColorUtils.color("&d► Shift + Sol Tık: &fEnvanter Dolusu Al"));
            } else {
                lore.add(ColorUtils.color("&7Depodaki Miktar: &c0 Adet"));
                lore.add(ColorUtils.color("&7Birim Satış Fiyatı: &e" + plugin.getEconomyManager().format(price)));
                lore.add(ColorUtils.color("&8-----------------------------"));
                lore.add(ColorUtils.color("&7Canlılar öldüğünde veya"));
                lore.add(ColorUtils.color("&7yere atıldığında otomatik toplanır."));
            }

            inv.setItem(mobSlots[i], createItem(mat, ColorUtils.color("&c" + formatMaterialName(mat)), lore));
        }

        // Slot 46: Çiftçiyi Buraya Getir
        List<String> summonLore = new ArrayList<>();
        summonLore.add(ColorUtils.color("&7Çiftçi Köylüsünü adanızda"));
        summonLore.add(ColorUtils.color("&7şu an durduğunuz konuma ışınlar."));
        summonLore.add("");
        summonLore.add(ColorUtils.color("&a► Buraya çağırmak için tıkla"));
        inv.setItem(46, createItem(Material.HAY_BLOCK, ColorUtils.color("&a&lÇiftçiyi Buraya Getir"), summonLore));

                // Slot 47: Kırıktaş Deposu (Minyon & Ada Deposu)
        Material cobbleMat = Material.COBBLESTONE;
        int cobbleCount = farmer.getItemCount(cobbleMat);
        double cobblePrice = plugin.getFarmerManager().getPrice(cobbleMat);
        double cobbleTotal = cobbleCount * cobblePrice;

        List<String> cobbleLore = new ArrayList<>();
        cobbleLore.add(ColorUtils.color("&8-----------------------------"));
        if (cobbleCount > 0) {
            cobbleLore.add(ColorUtils.color("&7Depodaki Miktar: &a" + String.format("%,d", cobbleCount) + " Adet"));
            cobbleLore.add(ColorUtils.color("&7Birim Satış Fiyatı: &e" + plugin.getEconomyManager().format(cobblePrice)));
            cobbleLore.add(ColorUtils.color("&7Toplam Satış Değeri: &6" + plugin.getEconomyManager().format(cobbleTotal)));
            cobbleLore.add(ColorUtils.color("&8-----------------------------"));
            cobbleLore.add(ColorUtils.color("&e► Sağ Tık: &f1 Adet Sat &8(&6" + plugin.getEconomyManager().format(cobblePrice) + "&8)"));
            cobbleLore.add(ColorUtils.color("&6► Shift + Sağ Tık: &fHepsini Sat &8(&6" + plugin.getEconomyManager().format(cobbleTotal) + "&8)"));
            cobbleLore.add(ColorUtils.color("&b► Sol Tık: &f1 Adet Al (Tekli Çek)"));
            cobbleLore.add(ColorUtils.color("&3► Q Tuşu: &f64 Adet Al (Stack Çek)"));
            cobbleLore.add(ColorUtils.color("&d► Shift + Sol Tık: &fEnvanter Dolusu Al"));
        } else {
            cobbleLore.add(ColorUtils.color("&7Depodaki Miktar: &c0 Adet"));
            cobbleLore.add(ColorUtils.color("&7Birim Satış Fiyatı: &e" + plugin.getEconomyManager().format(cobblePrice)));
            cobbleLore.add(ColorUtils.color("&8-----------------------------"));
            cobbleLore.add(ColorUtils.color("&7Minyonlar blok kırdığında veya"));
            cobbleLore.add(ColorUtils.color("&7yere atıldığında otomatik toplanır."));
        }
        inv.setItem(47, createItem(cobbleMat, ColorUtils.color("&7&l[ KIRIKTAŞ DEPOSU ]"), cobbleLore));

        // Slot 48: Tümünü Sat
        int totalItems = farmer.getTotalItemCount();
        double totalValue = 0.0;
        for (java.util.Map.Entry<Material, Integer> entry : farmer.getStorage().entrySet()) {
            totalValue += entry.getValue() * plugin.getFarmerManager().getPrice(entry.getKey());
        }

        List<String> sellAllLore = new ArrayList<>();
        sellAllLore.add(ColorUtils.color("&8-----------------------------"));
        sellAllLore.add(ColorUtils.color("&7Depodaki Toplam Eşya: &e" + String.format("%,d", totalItems) + " Adet"));
        sellAllLore.add(ColorUtils.color("&7Toplam Satış Geliri: &a" + plugin.getEconomyManager().format(totalValue)));
        sellAllLore.add(ColorUtils.color("&8-----------------------------"));
        if (totalItems > 0) {
            sellAllLore.add(ColorUtils.color("&a► Tek tıkla hepsini satmak için tıkla"));
        } else {
            sellAllLore.add(ColorUtils.color("&cDepoda satılacak ürün bulunmuyor."));
        }
        inv.setItem(48, createItem(Material.EMERALD_BLOCK, ColorUtils.color("&a&l[ TÜM ÜRÜNLERİ SAT ]"), sellAllLore));

        // Slot 49: Bakiye ve İstatistik
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        List<String> statsLore = new ArrayList<>();
        statsLore.add(ColorUtils.color("&8-----------------------------"));
        statsLore.add(ColorUtils.color("&7Mevcut Bakiyeniz: &a" + plugin.getEconomyManager().format(balance)));
        statsLore.add(ColorUtils.color("&7Çiftçiden Toplam Kazanç: &6" + plugin.getEconomyManager().format(farmer.getTotalEarned())));
        statsLore.add(ColorUtils.color("&8-----------------------------"));
        inv.setItem(49, createItem(Material.SUNFLOWER, ColorUtils.color("&e&lBakiye & İstatistik"), statsLore));

        // Slot 50: Otomatik Toplama Aç/Kapa
        boolean active = farmer.isAutoCollect();
        Material toggleMat = active ? Material.LIME_DYE : Material.GRAY_DYE;
        String toggleStatus = active ? "&a&lAÇIK &7(Topluyor)" : "&c&lKAPALI &7(Durduruldu)";
        List<String> toggleLore = new ArrayList<>();
        toggleLore.add(ColorUtils.color("&7Tarla ve canlı ganimetlerini otomatik toplar."));
        toggleLore.add("");
        toggleLore.add(ColorUtils.color("&7Durum: " + toggleStatus));
        toggleLore.add("");
        toggleLore.add(ColorUtils.color("&e► Durumu değiştirmek için tıkla"));
        inv.setItem(50, createItem(toggleMat, ColorUtils.color("&e&lOtomatik Toplama"), toggleLore));

        // Slot 51: Yere Atılanları Toplama Aç/Kapa
        boolean groundActive = farmer.isCollectGroundDrops();
        Material groundMat = groundActive ? Material.HOPPER : Material.DISPENSER;
        String groundStatus = groundActive ? "&a&lAÇIK &7(Yerden Topluyor)" : "&c&lKAPALI &7(Durduruldu)";
        List<String> groundLore = new ArrayList<>();
        groundLore.add(ColorUtils.color("&7Yere atılan veya düşen çiftçi eşyalarını"));
        groundLore.add(ColorUtils.color("&7otomatik olarak çiftçi deposuna aktarır."));
        groundLore.add("");
        groundLore.add(ColorUtils.color("&7Durum: " + groundStatus));
        groundLore.add("");
        groundLore.add(ColorUtils.color("&e► Durumu değiştirmek için tıkla"));
        inv.setItem(51, createItem(groundMat, ColorUtils.color("&6&lYere Düşenleri Topla"), groundLore));

        // Slot 52: Kapat
        List<String> closeLore = new ArrayList<>();
        closeLore.add(ColorUtils.color("&7Menüyü kapatmak için tıklayın."));
        inv.setItem(52, createItem(Material.BARRIER, ColorUtils.color("&cKapat"), closeLore));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
    }

    public static String formatMaterialName(Material mat) {
        switch (mat) {
            case COBBLESTONE: return "Kırıktaş";
            case WHEAT: return "Buğday";
            case CARROT: return "Havuç";
            case POTATO: return "Patates";
            case BEETROOT: return "Pancar";
            case MELON_SLICE: return "Karpuz Dilimi";
            case PUMPKIN: return "Balkabağı";
            case SUGAR_CANE: return "Şeker Kamışı";
            case CACTUS: return "Kaktüs";
            case NETHER_WART: return "Nether Siğili";
            case COCOA_BEANS: return "Kakao Çekirdeği";
            case RED_MUSHROOM: return "Kırmızı Mantar";
            case BROWN_MUSHROOM: return "Kahverengi Mantar";
            case SWEET_BERRIES: return "Tatlı Meyveler";
            case BAMBOO: return "Bambu";
            case ROTTEN_FLESH: return "Çürük Et";
            case BONE: return "Kemik";
            case STRING: return "İp";
            case GUNPOWDER: return "Barut";
            case SPIDER_EYE: return "Örümcek Gözü";
            case ENDER_PEARL: return "Ender İncisi";
            case BLAZE_ROD: return "Blaze Çubuğu";
            case IRON_INGOT: return "Demir Külçesi";
            case GOLD_INGOT: return "Altın Külçesi";
            case SLIME_BALL: return "Balçık Topu";
            case MAGMA_CREAM: return "Magma Kremi";
            case LEATHER: return "Deri";
            case ARROW: return "Ok";
            case WITHER_SKELETON_SKULL: return "Wither İskeleti Kafatası";
            default: return mat.name();
        }
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
