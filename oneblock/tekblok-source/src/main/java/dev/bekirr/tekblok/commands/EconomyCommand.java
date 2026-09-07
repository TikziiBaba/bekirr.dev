package dev.bekirr.tekblok.commands;

import dev.bekirr.tekblok.TekBlokPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EconomyCommand implements CommandExecutor, TabCompleter {

    private final TekBlokPlugin plugin;

    public EconomyCommand(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length >= 3 && args[0].equalsIgnoreCase("ver")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("Oyuncu bulunamadı.");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[2]);
                    plugin.getEconomyManager().addBalance(target.getUniqueId(), amount);
                    sender.sendMessage(target.getName() + " adlı oyuncuya " + plugin.getEconomyManager().format(amount) + " eklendi.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("Geçersiz miktar.");
                }
                return true;
            }
            sender.sendMessage("Bu komut konsoldan tam olarak kullanılamaz.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            double bal = plugin.getEconomyManager().getBalance(player.getUniqueId());
            player.sendMessage(plugin.getConfigManager().getMessage("messages.balance-self")
                    .replace("{balance}", plugin.getEconomyManager().format(bal)));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("gonder") || sub.equals("pay")) {
            if (args.length < 3) {
                player.sendMessage(plugin.getConfigManager().getMessage("messages.pay-usage"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(plugin.getConfigManager().getMessage("messages.player-not-found"));
                return true;
            }
            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().getMessage("messages.cannot-pay-self"));
                return true;
            }
            double amount;
            try {
                amount = Double.parseDouble(args[2]);
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getConfigManager().getMessage("messages.invalid-amount"));
                return true;
            }

            if (!plugin.getEconomyManager().has(player.getUniqueId(), amount)) {
                player.sendMessage(plugin.getConfigManager().getMessage("messages.insufficient-balance"));
                return true;
            }

            plugin.getEconomyManager().withdraw(player.getUniqueId(), amount);
            plugin.getEconomyManager().addBalance(target.getUniqueId(), amount);

            player.sendMessage(plugin.getConfigManager().getMessage("messages.pay-sent")
                    .replace("{player}", target.getName())
                    .replace("{amount}", plugin.getEconomyManager().format(amount)));
            target.sendMessage(plugin.getConfigManager().getMessage("messages.pay-received")
                    .replace("{player}", player.getName())
                    .replace("{amount}", plugin.getEconomyManager().format(amount)));

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            return true;
        }

        if (player.hasPermission("tekblok.admin")) {
            if (sub.equals("ver") || sub.equals("add")) {
                if (args.length < 3) {
                    player.sendMessage("§cKullanım: §e/para ver <oyuncu> <miktar>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.player-not-found"));
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[2]);
                    plugin.getEconomyManager().addBalance(target.getUniqueId(), amount);
                    player.sendMessage("§a" + target.getName() + " §7adlı oyuncuya §e" + plugin.getEconomyManager().format(amount) + " §7eklendi.");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cGeçersiz miktar.");
                }
                return true;
            }

            if (sub.equals("al") || sub.equals("take")) {
                if (args.length < 3) {
                    player.sendMessage("§cKullanım: §e/para al <oyuncu> <miktar>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.player-not-found"));
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[2]);
                    plugin.getEconomyManager().withdraw(target.getUniqueId(), amount);
                    player.sendMessage("§a" + target.getName() + " §7adlı oyuncudan §e" + plugin.getEconomyManager().format(amount) + " §7alındı.");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cGeçersiz miktar.");
                }
                return true;
            }

            if (sub.equals("ayarla") || sub.equals("set")) {
                if (args.length < 3) {
                    player.sendMessage("§cKullanım: §e/para ayarla <oyuncu> <miktar>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.player-not-found"));
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[2]);
                    plugin.getEconomyManager().setBalance(target.getUniqueId(), amount);
                    player.sendMessage("§a" + target.getName() + " §7adlı oyuncunun bakiyesi §e" + plugin.getEconomyManager().format(amount) + " §7olarak ayarlandı.");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cGeçersiz miktar.");
                }
                return true;
            }
        }

        // Başka bir oyuncunun bakiyesi kontrol ediliyorsa
        Player other = Bukkit.getPlayer(args[0]);
        if (other != null && other.isOnline()) {
            double bal = plugin.getEconomyManager().getBalance(other.getUniqueId());
            player.sendMessage(plugin.getConfigManager().getMessage("messages.balance-other")
                    .replace("{player}", other.getName())
                    .replace("{balance}", plugin.getEconomyManager().format(bal)));
            return true;
        }

        player.sendMessage("§cKullanım: §e/para [gonder <oyuncu> <miktar>]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("gonder", "pay"));
            if (sender.hasPermission("tekblok.admin")) {
                list.addAll(Arrays.asList("ver", "al", "ayarla"));
            }
            list.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            return list.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("gonder") || args[0].equalsIgnoreCase("pay") || args[0].equalsIgnoreCase("ver") || args[0].equalsIgnoreCase("al") || args[0].equalsIgnoreCase("ayarla"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
