package dev.bekirr.tekblok.gui;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SmelterGUI {

    public static final String SMELTER_TITLE = ColorUtils.color("&8» &6&lMaden Eritici &8«");

    public static class Conversion {
        public final Material input;
        public final Material output;
        public final String name;
        public final int slot;

        public Conversion(Material input, Material output, String name, int slot) {
            this.input = input;
            this.output = output;
            this.name = name;
            this.slot = slot;
        }
    }

    public static final List<Conversion> CONVERSIONS = Arrays.asList(
            new Conversion(Material.IRON_ORE, Material.IRON_INGOT, "&fDemir Cevheri &7➔ &fDemir Külçesi", 10),
            new Conversion(Material.GOLD_ORE, Material.GOLD_INGOT, "&eAltın Cevheri &7➔ &eAltın Külçesi", 11),
            new Conversion(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP, "&4Antik Kalıntı &7➔ &4Netherit Parçası", 12),
            new Conversion(Material.COBBLESTONE, Material.STONE, "&7Kırıktaş &7➔ &fDüz Taş", 13),
            new Conversion(Material.SAND, Material.GLASS, "&eKum &7➔ &fCam", 14),
            new Conversion(Material.CLAY_BALL, Material.BRICK, "&bKil Topu &7➔ &cTuğla", 15),
            new Conversion(Material.NETHERRACK, Material.NETHER_BRICK, "&cNether Taşı &7➔ &4Nether Tuğlası", 16)
    );

    public static void open(Player player, TekBlokPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, SMELTER_TITLE);

        ItemStack border = createItem(Material.ORANGE_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, border);
        }

        int coalCount = countCoal(player);

        // Maden Eritme Butonları
        for (Conversion conv : CONVERSIONS) {
            int inCount = countItems(player, conv.input);
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.color("&8------------------------"));
            lore.add(ColorUtils.color("&7Girdi: &e1x " + conv.input.name()));
            lore.add(ColorUtils.color("&7Yakıt: &81x Kömür"));
            lore.add(ColorUtils.color("&7Çıktı: &a1x " + conv.output.name()));
            lore.add(ColorUtils.color("&7Çantanızdaki: &b" + inCount + " adet"));
            lore.add(ColorUtils.color("&7Kömürünüz: " + (coalCount > 0 ? "&a" + coalCount + " adet" : "&cKÖMÜR YOK!")));
            lore.add("");
            lore.add(ColorUtils.color("&eSol Tık: &f1 Adet Erit &7(1 Kömür)"));
            lore.add(ColorUtils.color("&eSağ Tık: &f16 Adet Erit &7(16 Kömür)"));
            lore.add(ColorUtils.color("&eShift + Sağ Tık: &f64 Adet Erit &7(64 Kömür)"));
            lore.add(ColorUtils.color("&8------------------------"));

            inv.setItem(conv.slot, createItem(conv.output, ColorUtils.color(conv.name), lore));
        }

        // Slot 22: Tüm Çantayı Erit Butonu
        List<String> allLore = new ArrayList<>();
        allLore.add(ColorUtils.color("&8------------------------"));
        allLore.add(ColorUtils.color("&7Envanterinizdeki tüm eritilebilir"));
        allLore.add(ColorUtils.color("&7ham madenleri tek tıkla dönüştürür."));
        allLore.add(ColorUtils.color("&7Kural: &eHer 1 maden = 1 kömür gerektirir!"));
        allLore.add(ColorUtils.color("&7Mevcut Kömür: " + (coalCount > 0 ? "&a" + coalCount + " adet" : "&c0 (Eritme yapılamaz)")));
        allLore.add("");
        allLore.add(ColorUtils.color("&6► TÜM ÇANTAYI ERİTMEK İÇİN TIKLA"));
        allLore.add(ColorUtils.color("&8------------------------"));
        inv.setItem(22, createItem(Material.FURNACE, ColorUtils.color("&6&l🔥 TÜM ÇANTAYI ERİT"), allLore));

        player.openInventory(inv);
    }

    public static int countCoal(Player player) {
        int count = 0;
        for (ItemStack is : player.getInventory().getStorageContents()) {
            if (is != null && (is.getType() == Material.COAL || is.getType() == Material.CHARCOAL)) {
                count += is.getAmount();
            }
        }
        return count;
    }

    public static void removeCoal(Player player, int amount) {
        int left = amount;
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack is = contents[i];
            if (is != null && (is.getType() == Material.COAL || is.getType() == Material.CHARCOAL)) {
                if (is.getAmount() <= left) {
                    left -= is.getAmount();
                    contents[i] = null;
                } else {
                    is.setAmount(is.getAmount() - left);
                    left = 0;
                }
                if (left <= 0) break;
            }
        }
        player.getInventory().setStorageContents(contents);
    }

    public static int countItems(Player player, Material mat) {
        int count = 0;
        for (ItemStack is : player.getInventory().getStorageContents()) {
            if (is != null && is.getType() == mat) {
                count += is.getAmount();
            }
        }
        return count;
    }

    public static boolean smeltSingleType(Player player, TekBlokPlugin plugin, Conversion conv, int maxAmount) {
        int has = countItems(player, conv.input);
        if (has <= 0) {
            player.sendMessage(ColorUtils.color("&cÇantanızda eritilecek " + conv.input.name() + " bulunamadı!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return false;
        }

        int coal = countCoal(player);
        if (coal <= 0) {
            player.sendMessage(ColorUtils.color("&cEritme işlemi için yakıt olarak kömür gerekiyor! &7(Her 1 maden = 1 kömür)"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return false;
        }

        int toSmelt = Math.min(has, Math.min(maxAmount, coal));
        removeItems(player, conv.input, toSmelt);
        removeCoal(player, toSmelt);
        giveItems(player, conv.output, toSmelt);

        player.playSound(player.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.6f, 1.5f);
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.05);

        player.sendMessage(ColorUtils.color("&a✔ &e" + toSmelt + "x &a" + conv.input.name() + " &7(" + toSmelt + " kömür kullanılarak) &abaşarıyla eritildi!"));
        return true;
    }

    public static boolean smeltAll(Player player, TekBlokPlugin plugin) {
        int coal = countCoal(player);
        if (coal <= 0) {
            player.sendMessage(ColorUtils.color("&cEritme işlemi için yakıt olarak kömür gerekiyor! &7(Her 1 maden = 1 kömür)"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return false;
        }

        int totalSmelted = 0;
        int remainingCoal = coal;

        for (Conversion conv : CONVERSIONS) {
            if (remainingCoal <= 0) break;
            int has = countItems(player, conv.input);
            if (has > 0) {
                int toSmelt = Math.min(has, remainingCoal);
                removeItems(player, conv.input, toSmelt);
                giveItems(player, conv.output, toSmelt);
                remainingCoal -= toSmelt;
                totalSmelted += toSmelt;
            }
        }

        if (totalSmelted == 0) {
            player.sendMessage(ColorUtils.color("&cÇantanızda eritilecek hiçbir ham maden bulunamadı!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return false;
        }

        int usedCoal = coal - remainingCoal;
        removeCoal(player, usedCoal);

        player.playSound(player.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1.2f, 1.0f);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.8f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 35, 0.5, 0.5, 0.5, 0.1);

        player.sendMessage(ColorUtils.color("&6&l[MADEN ERİTİCİ] &aHarika! &e" + usedCoal + " &akömür kullanılarak toplam &e" + totalSmelted + " &aadet cevher başarıyla külçeye dönüştürüldü!"));
        return true;
    }

    private static void removeItems(Player player, Material mat, int amount) {
        int left = amount;
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack is = contents[i];
            if (is != null && is.getType() == mat) {
                if (is.getAmount() <= left) {
                    left -= is.getAmount();
                    contents[i] = null;
                } else {
                    is.setAmount(is.getAmount() - left);
                    left = 0;
                }
                if (left <= 0) break;
            }
        }
        player.getInventory().setStorageContents(contents);
    }

    private static void giveItems(Player player, Material mat, int amount) {
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(mat, amount));
        if (!leftover.isEmpty()) {
            for (ItemStack is : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), is);
            }
        }
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
