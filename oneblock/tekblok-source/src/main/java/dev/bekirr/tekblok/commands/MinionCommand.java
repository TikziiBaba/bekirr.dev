package dev.bekirr.tekblok.commands;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.gui.MinionGUI;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.Minion;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MinionCommand implements CommandExecutor, TabCompleter {

    private final TekBlokPlugin plugin;
    public static final double MINION_PRICE = 1500.0; // %50 indirimli fiyat (3000 -> 1500)

    public MinionCommand(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("ver")) {
            if (!sender.hasPermission("tekblok.admin")) {
                sender.sendMessage(ColorUtils.color("&cBu komutu kullanmak icin yetkiniz yok!"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ColorUtils.color("&cKullanim: /minyon ver <oyuncu> [1-3]"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ColorUtils.color("&cOyuncu bulunamadi!"));
                return true;
            }
            int level = 1;
            if (args.length >= 3) {
                try {
                    level = Math.max(1, Math.min(3, Integer.parseInt(args[2])));
                } catch (NumberFormatException ignored) {}
            }
            ItemStack minionItem = plugin.getMinionManager().createMinionItem(level);
            target.getInventory().addItem(minionItem);
            target.sendMessage(ColorUtils.color("&a&l[MINYON] &eYetkili tarafindan size &b&lSeviye " + level + " Kirici Minyon &everildi!"));
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            sender.sendMessage(ColorUtils.color("&a" + target.getName() + " adli oyuncuya Seviye " + level + " minyon verildi."));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player player = (Player) sender;
        Island island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
        if (island == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("messages.no-island"));
            return true;
        }

        Minion minion = plugin.getMinionManager().getMinion(island.getOwnerUUID());

        if (args.length == 0 || args[0].equalsIgnoreCase("menu") || args[0].equalsIgnoreCase("gui")) {
            if (minion != null) {
                MinionGUI.openMenu(player, plugin, minion);
            } else {
                player.sendMessage(ColorUtils.color("&8&m----------------------------------------"));
                player.sendMessage(ColorUtils.color("&b&l★ TEKBLOK KIRICI MINYON SISTEMI ★"));
                player.sendMessage(ColorUtils.color("&7Adaniza koydugunuzda onundeki bloklari otomatik kirar."));
                player.sendMessage(ColorUtils.color("&7Kirilan kiriktaslar otomatik &aAda Ciftcisine &7aktarilir!"));
                player.sendMessage("");
                player.sendMessage(ColorUtils.color("&e• Seviye 1: &fOnundeki 1 blogu kirar"));
                player.sendMessage(ColorUtils.color("&e• Seviye 2: &fOnundeki 2 blogu kirar"));
                player.sendMessage(ColorUtils.color("&e• Seviye 3: &fOnundeki 3 blogu kirar"));
                player.sendMessage("");
                player.sendMessage(ColorUtils.color("&fMinyon Fiyati: &a" + plugin.getEconomyManager().format(MINION_PRICE)));
                player.sendMessage(ColorUtils.color("&aSatın Almak icin: &e/minyon satin-al"));
                player.sendMessage(ColorUtils.color("&8&m----------------------------------------"));
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("satinal") || sub.equals("satin-al") || sub.equals("al") || sub.equals("buy")) {
            if (minion != null) {
                player.sendMessage(ColorUtils.color("&cAdanizda zaten aktif bir minyonunuz kurulu! (Her adaya sadece 1 minyon)"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return true;
            }

            double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
            if (balance < MINION_PRICE) {
                player.sendMessage(ColorUtils.color("&cYetersiz Bakiye! Minyon fiyati: &e" + plugin.getEconomyManager().format(MINION_PRICE) + " &c(Sizde: &e" + plugin.getEconomyManager().format(balance) + "&c)"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return true;
            }

            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(ColorUtils.color("&cEnvanteriniz dolu! Minyonu satin almak icin en az 1 bos yer acin."));
                return true;
            }

            plugin.getEconomyManager().withdraw(player.getUniqueId(), MINION_PRICE);
            ItemStack item = plugin.getMinionManager().createMinionItem(1);
            player.getInventory().addItem(item);

            player.sendMessage(ColorUtils.color("&a&l[MINYON] &eTebrikler! &b&lKirici Minyon (Seviye 1) &esatin aldiniz!"));
            player.sendMessage(ColorUtils.color("&7Minyonu adanizda istediginiz bloga sag tiklayarak yerlestirin."));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            return true;
        }

        if (sub.equals("kaldir") || sub.equals("topla") || sub.equals("pickup")) {
            if (minion == null) {
                player.sendMessage(ColorUtils.color("&cAdanizda kurulu bir minyon bulunamadi!"));
                return true;
            }
            plugin.getMinionManager().pickupMinion(player, minion);
            return true;
        }

        if (sub.equals("bilgi") || sub.equals("info")) {
            if (minion == null) {
                player.sendMessage(ColorUtils.color("&cAdanizda kurulu bir minyon bulunamadi! /minyon satin-al ile edinebilirsiniz."));
                return true;
            }
            player.sendMessage(ColorUtils.color("&8&m----------------------------------------"));
            player.sendMessage(ColorUtils.color("&b&l★ KIRICI MINYON BILGILERI ★"));
            player.sendMessage(ColorUtils.color("&7Seviye: &e" + minion.getLevel() + " &7/ &b3"));
            player.sendMessage(ColorUtils.color("&7Kirilan Toplam Blok: &a" + String.format("%,d", minion.getBlocksBroken()) + " Blok"));
            player.sendMessage(ColorUtils.color("&7Baktigi Yon: &f" + minion.getFacing()));
            player.sendMessage(ColorUtils.color("&7Konum: &fX: " + minion.getLocation().getBlockX() + " Y: " + minion.getLocation().getBlockY() + " Z: " + minion.getLocation().getBlockZ()));
            player.sendMessage(ColorUtils.color("&8&m----------------------------------------"));
            return true;
        }

        player.sendMessage(ColorUtils.color("&eMinyon Komutlari: &f/minyon, /minyon satin-al, /minyon kaldir, /minyon bilgi"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("menu", "satin-al", "kaldir", "bilgi"));
            if (sender.hasPermission("tekblok.admin")) {
                list.add("ver");
            }
            return list.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("ver") && sender.hasPermission("tekblok.admin")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("ver") && sender.hasPermission("tekblok.admin")) {
            return Arrays.asList("1", "2", "3");
        }
        return new ArrayList<>();
    }
}
