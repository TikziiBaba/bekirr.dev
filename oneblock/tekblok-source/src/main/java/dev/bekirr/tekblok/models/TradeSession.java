package dev.bekirr.tekblok.models;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TradeSession {

    public static final String TRADE_TITLE = ColorUtils.color("&8TekBlok &7» &6Takas Menüsü");

    public static final Set<Integer> P1_SLOTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            0, 1, 2, 3,
            9, 10, 11, 12,
            18, 19, 20, 21,
            27, 28, 29, 30
    )));

    public static final Set<Integer> P2_SLOTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            5, 6, 7, 8,
            14, 15, 16, 17,
            23, 24, 25, 26,
            32, 33, 34, 35
    )));

    public static final Set<Integer> DIVIDER_SLOTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            4, 13, 22, 31, 40, 48, 50
    )));

    public static final int[] P1_BUTTON_SLOTS = {45, 46, 47};
    public static final int[] P2_BUTTON_SLOTS = {51, 52, 53};
    public static final int CANCEL_SLOT = 49;

    private final TekBlokPlugin plugin;
    private final Player player1;
    private final Player player2;
    private final Inventory inventory;

    private boolean player1Ready = false;
    private boolean player2Ready = false;
    private boolean completed = false;
    private boolean cancelled = false;
    private BukkitTask countdownTask = null;
    private int countdown = 3;

    public TradeSession(TekBlokPlugin plugin, Player player1, Player player2) {
        this.plugin = plugin;
        this.player1 = player1;
        this.player2 = player2;
        this.inventory = Bukkit.createInventory(null, 54, TRADE_TITLE);

        setupInventory();
    }

    private void setupInventory() {
        // Ayırıcı camlar
        ItemStack grayGlass = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int slot : DIVIDER_SLOTS) {
            inventory.setItem(slot, grayGlass);
        }

        // Oyuncu 1 Bilgi / Durum (Slot 36..39)
        updatePlayer1Status();

        // Oyuncu 2 Bilgi / Durum (Slot 41..44)
        updatePlayer2Status();

        // İptal Butonu
        List<String> cancelLore = new ArrayList<>();
        cancelLore.add(ColorUtils.color("&7Takası iptal etmek ve tüm"));
        cancelLore.add(ColorUtils.color("&7eşyalarınızı geri almak için tıklayın."));
        inventory.setItem(CANCEL_SLOT, createGuiItem(Material.BARRIER, ColorUtils.color("&c&l[ ✖ TAKASI İPTAL ET ]"), cancelLore));

        // Onay Butonları
        updateButtons();
    }

    public void open() {
        player1.openInventory(inventory);
        player2.openInventory(inventory);
        player1.playSound(player1.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        player2.playSound(player2.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }

    public void updateButtons() {
        // P1 Butonları
        Material p1Mat = player1Ready ? Material.LIME_TERRACOTTA : Material.RED_TERRACOTTA;
        String p1Title = player1Ready ? ColorUtils.color("&a&l[ ✔ ONAYLANDI ]") : ColorUtils.color("&c&l[ ONAYLA ]");
        List<String> p1Lore = new ArrayList<>();
        p1Lore.add(ColorUtils.color("&7Oyuncu: &e" + player1.getName()));
        p1Lore.add(ColorUtils.color("&7Durum: " + (player1Ready ? "&aHazır &7(Tıklanırsa iptal olur)" : "&cHazır Değil &7(Onaylamak için tıkla)")));
        ItemStack p1Btn = createGuiItem(p1Mat, p1Title, p1Lore);
        for (int s : P1_BUTTON_SLOTS) {
            inventory.setItem(s, p1Btn);
        }

        // P2 Butonları
        Material p2Mat = player2Ready ? Material.LIME_TERRACOTTA : Material.RED_TERRACOTTA;
        String p2Title = player2Ready ? ColorUtils.color("&a&l[ ✔ ONAYLANDI ]") : ColorUtils.color("&c&l[ ONAYLA ]");
        List<String> p2Lore = new ArrayList<>();
        p2Lore.add(ColorUtils.color("&7Oyuncu: &e" + player2.getName()));
        p2Lore.add(ColorUtils.color("&7Durum: " + (player2Ready ? "&aHazır &7(Tıklanırsa iptal olur)" : "&cHazır Değil &7(Onaylamak için tıkla)")));
        ItemStack p2Btn = createGuiItem(p2Mat, p2Title, p2Lore);
        for (int s : P2_BUTTON_SLOTS) {
            inventory.setItem(s, p2Btn);
        }

        updatePlayer1Status();
        updatePlayer2Status();
    }

    public void updatePlayer1Status() {
        Material mat = player1Ready ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String name = player1Ready ? ColorUtils.color("&a" + player1.getName() + " - Onayladı") : ColorUtils.color("&c" + player1.getName() + " - Bekleniyor");
        ItemStack item = createGuiItem(mat, name, null);
        for (int i = 36; i <= 39; i++) {
            inventory.setItem(i, item);
        }
    }

    public void updatePlayer2Status() {
        Material mat = player2Ready ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String name = player2Ready ? ColorUtils.color("&a" + player2.getName() + " - Onayladı") : ColorUtils.color("&c" + player2.getName() + " - Bekleniyor");
        ItemStack item = createGuiItem(mat, name, null);
        for (int i = 41; i <= 44; i++) {
            inventory.setItem(i, item);
        }
    }

    public void togglePlayer1Ready() {
        player1Ready = !player1Ready;
        player1.playSound(player1.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        player2.playSound(player2.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        updateButtons();
        checkBothReady();
    }

    public void togglePlayer2Ready() {
        player2Ready = !player2Ready;
        player1.playSound(player1.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        player2.playSound(player2.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        updateButtons();
        checkBothReady();
    }

    public void resetReadyStatus() {
        if (player1Ready || player2Ready) {
            player1Ready = false;
            player2Ready = false;
            cancelCountdown();
            updateButtons();
            player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.7f);
            player2.playSound(player2.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.7f);
            player1.sendMessage(ColorUtils.color("&e[Takas] &7Eşyalar değiştirildiği için onaylar sıfırlandı!"));
            player2.sendMessage(ColorUtils.color("&e[Takas] &7Eşyalar değiştirildiği için onaylar sıfırlandı!"));
        }
    }

    private void checkBothReady() {
        if (player1Ready && player2Ready) {
            startCountdown();
        } else {
            cancelCountdown();
        }
    }

    private void startCountdown() {
        cancelCountdown();
        countdown = 3;

        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (completed || cancelled) {
                cancelCountdown();
                return;
            }

            if (!player1Ready || !player2Ready) {
                cancelCountdown();
                return;
            }

            if (countdown > 0) {
                player1.sendMessage(ColorUtils.color("&e[Takas] &aTakas &f" + countdown + " &asaniye içinde tamamlanıyor..."));
                player2.sendMessage(ColorUtils.color("&e[Takas] &aTakas &f" + countdown + " &asaniye içinde tamamlanıyor..."));
                player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
                player2.playSound(player2.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
                countdown--;
            } else {
                completeTrade();
            }
        }, 0L, 20L);
    }

    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    public synchronized void completeTrade() {
        if (completed || cancelled) return;
        completed = true;
        cancelCountdown();

        // 1. Eşyaları topla
        List<ItemStack> p1Items = new ArrayList<>();
        for (int slot : P1_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                p1Items.add(item.clone());
                inventory.setItem(slot, null);
            }
        }

        List<ItemStack> p2Items = new ArrayList<>();
        for (int slot : P2_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                p2Items.add(item.clone());
                inventory.setItem(slot, null);
            }
        }

        // 2. Takas et (P1'e P2 eşyalarını, P2'ye P1 eşyalarını ver)
        for (ItemStack item : p2Items) {
            HashMap<Integer, ItemStack> leftover = player1.getInventory().addItem(item);
            for (ItemStack left : leftover.values()) {
                player1.getWorld().dropItemNaturally(player1.getLocation(), left);
            }
        }

        for (ItemStack item : p1Items) {
            HashMap<Integer, ItemStack> leftover = player2.getInventory().addItem(item);
            for (ItemStack left : leftover.values()) {
                player2.getWorld().dropItemNaturally(player2.getLocation(), left);
            }
        }

        player1.sendMessage(ColorUtils.color("&6★ &aTakas başarıyla tamamlandı! Eşyalar envanterinize aktarıldı."));
        player2.sendMessage(ColorUtils.color("&6★ &aTakas başarıyla tamamlandı! Eşyalar envanterinize aktarıldı."));
        player1.playSound(player1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player2.playSound(player2.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

        player1.closeInventory();
        player2.closeInventory();
        plugin.getTradeManager().endSession(this);
    }

    public synchronized void cancelTrade() {
        if (completed || cancelled) return;
        cancelled = true;
        cancelCountdown();

        // P1 eşyalarını iade et
        for (int slot : P1_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                inventory.setItem(slot, null);
                HashMap<Integer, ItemStack> leftover = player1.getInventory().addItem(item);
                for (ItemStack left : leftover.values()) {
                    player1.getWorld().dropItemNaturally(player1.getLocation(), left);
                }
            }
        }

        // P2 eşyalarını iade et
        for (int slot : P2_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                inventory.setItem(slot, null);
                HashMap<Integer, ItemStack> leftover = player2.getInventory().addItem(item);
                for (ItemStack left : leftover.values()) {
                    player2.getWorld().dropItemNaturally(player2.getLocation(), left);
                }
            }
        }

        player1.sendMessage(ColorUtils.color("&cTakas iptal edildi. Eşyalarınız iade edildi."));
        player2.sendMessage(ColorUtils.color("&cTakas iptal edildi. Eşyalarınız iade edildi."));
        player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        player2.playSound(player2.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);

        player1.closeInventory();
        player2.closeInventory();
        plugin.getTradeManager().endSession(this);
    }

    private ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isPlayer1(Player p) {
        return player1.getUniqueId().equals(p.getUniqueId());
    }

    public boolean isPlayer2(Player p) {
        return player2.getUniqueId().equals(p.getUniqueId());
    }
}
