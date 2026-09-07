package dev.bekirr.tekblok.commands;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.CustomVillager;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VillagerCommand implements CommandExecutor, TabCompleter {

    private final TekBlokPlugin plugin;

    public VillagerCommand(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tekblok.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("messages.no-permission"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("messages.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "olustur":
            case "create":
            case "spawn":
                String type = "eritici";
                if (args.length > 1) {
                    String reqType = args[1].toLowerCase();
                    if (reqType.startsWith("takas") || reqType.startsWith("tuc")) {
                        type = "takasci";
                    } else if (reqType.startsWith("ada") || reqType.startsWith("kap") || reqType.startsWith("isin")) {
                        type = "ada";
                    } else {
                        type = "eritici";
                    }
                }
                CustomVillager cv = plugin.getVillagerManager().createVillager(player, type);
                String name = type.equals("eritici") ? "&6Maden Eritici (Demirci)" :
                        (type.equals("ada") ? "&bAda Işınlayıcısı (Kaptan)" : "&aTakasçı Köylü (Tüccar)");
                player.sendMessage(ColorUtils.color("&a✔ " + name + " &abaşarıyla bulunduğunuz konuma oluşturuldu!"));
                player.sendMessage(ColorUtils.color("&7Sağ tıklayarak menüyü test edebilirsiniz."));
                return true;

            case "sil":
            case "remove":
            case "delete":
                boolean removed = plugin.getVillagerManager().removeClosestVillager(player);
                if (removed) {
                    player.sendMessage(ColorUtils.color("&a✔ En yakındaki özel köylü başarıyla silindi."));
                } else {
                    player.sendMessage(ColorUtils.color("&cYakınınızda (5 blok içinde) silinecek özel köylü bulunamadı!"));
                }
                return true;

            case "liste":
            case "list":
                player.sendMessage(ColorUtils.color("&6=== Aktif Özel Köylüler ==="));
                if (plugin.getVillagerManager().getVillagers().isEmpty()) {
                    player.sendMessage(ColorUtils.color("&7Henüz oluşturulmuş köylü yok."));
                } else {
                    for (CustomVillager v : plugin.getVillagerManager().getVillagers().values()) {
                        String vName = v.getType().equals("eritici") ? "&6Maden Eritici" :
                                (v.getType().equals("ada") ? "&bAda Işınlayıcısı" : "&aTakasçı");
                        player.sendMessage(ColorUtils.color("&8- " + vName + " &7[" + v.getLocation().getWorld().getName() + " "
                                + (int) v.getLocation().getX() + ", " + (int) v.getLocation().getY() + ", " + (int) v.getLocation().getZ() + "]"));
                    }
                }
                return true;

            default:
                sendHelp(player);
                return true;
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(ColorUtils.color("&6=== TekBlok Köylü Yönetimi ==="));
        player.sendMessage(ColorUtils.color("&e/koylu olustur eritici &7- Bulunduğunuz yere Maden Eritici koyar"));
        player.sendMessage(ColorUtils.color("&e/koylu olustur takasci &7- Bulunduğunuz yere Takasçı Köylü koyar"));
        player.sendMessage(ColorUtils.color("&e/koylu olustur ada &7- Bulunduğunuz yere Ada Işınlayıcısı koyar"));
        player.sendMessage(ColorUtils.color("&e/koylu sil &7- En yakındaki (5 blok) özel köylüyü kaldırır"));
        player.sendMessage(ColorUtils.color("&e/koylu liste &7- Tüm özel köylüleri koordinatlarıyla listeler"));
        player.sendMessage(ColorUtils.color("&6=============================="));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("tekblok.admin")) return new ArrayList<>();

        if (args.length == 1) {
            return Arrays.asList("olustur", "sil", "liste").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("olustur")) {
            return Arrays.asList("eritici", "takasci", "ada").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
