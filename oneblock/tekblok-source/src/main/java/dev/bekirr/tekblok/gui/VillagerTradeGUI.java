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

public class VillagerTradeGUI {

    public static final String TRADER_TITLE = ColorUtils.color("&8» &2&lTüccar Takas Menüsü &8«");

    public static class TradeOffer {
        public final Material costMat;
        public final int costAmt;
        public final String costName;
        public final Material giveMat;
        public final int giveAmt;
        public final String giveName;
        public final int slot;

        public TradeOffer(Material costMat, int costAmt, String costName, Material giveMat, int giveAmt, String giveName, int slot) {
            this.costMat = costMat;
            this.costAmt = costAmt;
            this.costName = costName;
            this.giveMat = giveMat;
            this.giveAmt = giveAmt;
            this.giveName = giveName;
            this.slot = slot;
        }
    }

    public static final List<TradeOffer> OFFERS = Arrays.asList(
            new TradeOffer(Material.COAL, 16, "&816x Kömür", Material.IRON_INGOT, 1, "&f1x Demir Külçesi", 11),
            new TradeOffer(Material.IRON_INGOT, 8, "&f8x Demir Külçesi", Material.GOLD_INGOT, 1, "&e1x Altın Külçesi", 12),
            new TradeOffer(Material.GOLD_INGOT, 4, "&e4x Altın Külçesi", Material.DIAMOND, 1, "&b1x Elmas", 13),
            new TradeOffer(Material.DIAMOND, 2, "&b2x Elmas", Material.EMERALD, 1, "&a1x Zümrüt", 14),
            new TradeOffer(Material.EMERALD, 4, "&a4x Zümrüt", Material.NETHERITE_SCRAP, 1, "&41x Netherit Parçası", 15)
    );

    public static void open(Player player, TekBlokPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, TRADER_TITLE);

        ItemStack border = createItem(Material.LIME_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, border);
        }

        for (TradeOffer offer : OFFERS) {
            int has = countItems(player, offer.costMat);
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.color("&8------------------------"));
            lore.add(ColorUtils.color("&7Gereken: &e" + offer.costName));
            lore.add(ColorUtils.color("&7Kazanılacak: &a" + offer.giveName));
            lore.add(ColorUtils.color("&7Çantanızdaki: " + (has >= offer.costAmt ? "&a" : "&c") + has + " adet"));
            lore.add("");
            lore.add(ColorUtils.color("&e► Takas Etmek İçin Tıkla"));
            lore.add(ColorUtils.color("&8------------------------"));

            inv.setItem(offer.slot, createItem(offer.giveMat, ColorUtils.color(offer.giveName), lore));
        }

        player.openInventory(inv);
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

    public static boolean doTrade(Player player, TekBlokPlugin plugin, TradeOffer offer) {
        int has = countItems(player, offer.costMat);
        if (has < offer.costAmt) {
            player.sendMessage(ColorUtils.color("&cTakas için yeterli &e" + offer.costName + " &cyok!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        removeItems(player, offer.costMat, offer.costAmt);
        giveItems(player, offer.giveMat, offer.giveAmt);

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);

        player.sendMessage(ColorUtils.color("&a✔ &e" + offer.costName + " &averildi, &a" + offer.giveName + " &aalındı!"));
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
