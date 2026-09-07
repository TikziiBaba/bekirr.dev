package dev.bekirr.tekblok.commands;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TradeCommand implements CommandExecutor, TabCompleter {

    private final TekBlokPlugin plugin;

    public TradeCommand(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komut sadece oyuncular tarafından kullanılabilir.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ColorUtils.color("&6===== &e&lTekBlok Takas Sistemi &6====="));
            player.sendMessage(ColorUtils.color("&e/takas <oyuncu> &7- Bir oyuncuya takas teklifi gönderir."));
            player.sendMessage(ColorUtils.color("&e/takas kabul <oyuncu> &7- Gelen takas teklifini onaylar."));
            player.sendMessage(ColorUtils.color("&e/takas reddet <oyuncu> &7- Gelen takas teklifini reddeder."));
            player.sendMessage(ColorUtils.color("&6======================================="));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("kabul") || sub.equals("accept")) {
            if (args.length < 2) {
                player.sendMessage(ColorUtils.color("&cKullanım: &e/takas kabul <oyuncu>"));
                return true;
            }
            Player senderPlayer = Bukkit.getPlayer(args[1]);
            if (senderPlayer == null || !senderPlayer.isOnline()) {
                player.sendMessage(ColorUtils.color("&cTakas teklifi gönderen oyuncu bulunamadı veya çevrimdışı."));
                return true;
            }
            plugin.getTradeManager().acceptRequest(player, senderPlayer);
            return true;
        }

        if (sub.equals("reddet") || sub.equals("deny")) {
            if (args.length < 2) {
                player.sendMessage(ColorUtils.color("&cKullanım: &e/takas reddet <oyuncu>"));
                return true;
            }
            Player senderPlayer = Bukkit.getPlayer(args[1]);
            if (senderPlayer == null || !senderPlayer.isOnline()) {
                player.sendMessage(ColorUtils.color("&cTakas teklifi gönderen oyuncu bulunamadı veya çevrimdışı."));
                return true;
            }
            plugin.getTradeManager().denyRequest(player, senderPlayer);
            return true;
        }

        // Oyuncuya doğrudan istek atma: /takas <oyuncu>
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ColorUtils.color("&cBelirtilen oyuncu bulunamadı veya çevrimdışı."));
            return true;
        }

        plugin.getTradeManager().sendRequest(player, target);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("kabul", "reddet"));
            list.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            return list.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("kabul") || args[0].equalsIgnoreCase("reddet"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
