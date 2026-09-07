package dev.bekirr.tekblok.gui;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.Crate;
import dev.bekirr.tekblok.models.CrateReward;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CrateOpeningGUI {

    public static final String OPENING_PREFIX = ColorUtils.color("&8Açılıyor: ");

    public static void start(Player player, TekBlokPlugin plugin, Crate crate, Location crateLocation) {
        String title = OPENING_PREFIX + crate.getDisplayName();
        Inventory inv = Bukkit.createInventory(null, 27, title);

        ItemStack blackGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 9; i++) inv.setItem(i, blackGlass);
        for (int i = 18; i < 27; i++) inv.setItem(i, blackGlass);

        // İbreler
        inv.setItem(4, createItem(Material.HOPPER, ColorUtils.color("&e&l▼ KAZANILAN EŞYA ▼"), null));
        inv.setItem(22, createItem(Material.REDSTONE_TORCH, ColorUtils.color("&e&l▲ KAZANILAN EŞYA ▲"), null));

        // 1. Rastgele çark eşyalarını hazırla (Toplam 45 eşya)
        int totalItems = 45;
        int finalOffset = 30; // Çarkın duracağı ofset
        // Slot 13, 9-17 aralığında 4. indekstir (9, 10, 11, 12, [13], 14, 15, 16, 17)
        int winningIndex = finalOffset + 4;

        CrateReward winningReward = crate.getRandomReward();
        List<CrateReward> rollItems = new ArrayList<>();
        List<CrateReward> allRewards = crate.getRewards();

        Random rng = new Random();
        for (int i = 0; i < totalItems; i++) {
            if (i == winningIndex) {
                rollItems.add(winningReward);
            } else {
                rollItems.add(allRewards.get(rng.nextInt(allRewards.size())));
            }
        }

        // Başlangıç ekranını doldur
        for (int col = 0; col < 9; col++) {
            inv.setItem(9 + col, rollItems.get(col).buildItemStack());
        }

        player.openInventory(inv);

        // 2. Animasyon Zamanlayıcısı
        new BukkitRunnable() {
            int step = 0;
            int currentOffset = 0;
            int delayTicks = 1;
            int ticksPassed = 0;

            @Override
            public void run() {
                if (!player.isOnline() || !player.getOpenInventory().getTitle().equals(title)) {
                    // Oyuncu menüyü kapatsa bile ödülü teslim et!
                    winningReward.giveReward(player, plugin);
                    cancel();
                    return;
                }

                ticksPassed++;
                if (ticksPassed < delayTicks) {
                    return;
                }
                ticksPassed = 0;

                // Slotları kaydır
                currentOffset++;
                for (int col = 0; col < 9; col++) {
                    int itemIdx = currentOffset + col;
                    if (itemIdx < rollItems.size()) {
                        inv.setItem(9 + col, rollItems.get(itemIdx).buildItemStack());
                    }
                }

                step++;
                float pitch = 0.8f + ((float) step / finalOffset) * 1.0f;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch);

                // Kademeli yavaşlama mekaniği
                int remaining = finalOffset - step;
                if (remaining <= 3) {
                    delayTicks = 8;
                } else if (remaining <= 7) {
                    delayTicks = 5;
                } else if (remaining <= 14) {
                    delayTicks = 3;
                } else if (remaining <= 22) {
                    delayTicks = 2;
                } else {
                    delayTicks = 1;
                }

                // Çark durdu!
                if (step >= finalOffset) {
                    cancel();

                    // Kazananı parlat
                    ItemStack limeGlass = createItem(Material.LIME_STAINED_GLASS_PANE, " ", null);
                    inv.setItem(3, limeGlass);
                    inv.setItem(5, limeGlass);
                    inv.setItem(21, limeGlass);
                    inv.setItem(23, limeGlass);

                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);

                    // Kasada havai fişek patlat
                    if (crateLocation != null && crateLocation.getWorld() != null) {
                        Location fwLoc = crateLocation.clone().add(0.5, 1.2, 0.5);
                        Firework fw = crateLocation.getWorld().spawn(fwLoc, Firework.class);
                        FireworkMeta fm = fw.getFireworkMeta();
                        fm.setPower(1);
                        fm.addEffect(FireworkEffect.builder()
                                .with(FireworkEffect.Type.BALL_LARGE)
                                .withColor(Color.YELLOW, Color.ORANGE, Color.PURPLE)
                                .withFade(Color.WHITE)
                                .trail(true)
                                .flicker(true)
                                .build());
                        fw.setFireworkMeta(fm);
                    }

                    // Ödülü teslim et
                    winningReward.giveReward(player, plugin);

                    // Nadir ödül duyurusu (şansı %15 ve altıysa)
                    if (winningReward.getChance() <= 15) {
                        String rName = winningReward.getName() != null ? winningReward.getName() : winningReward.getMaterial().name();
                        Bukkit.broadcastMessage(ColorUtils.color("&6&l[ŞANSLI OYUNCU] &e" + player.getName()
                                + " &7oyuncusu, &6" + crate.getDisplayName() + "&7'dan &a" + rName + " &7kazandı!"));
                    }

                    // 2.5 saniye sonra menüyü kapat
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline() && player.getOpenInventory().getTitle().equals(title)) {
                            player.closeInventory();
                        }
                    }, 50L);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
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
