package dev.bekirr.tekblok.commands;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.gui.CratePreviewGUI;
import dev.bekirr.tekblok.models.Crate;
import dev.bekirr.tekblok.models.CrateLocation;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CrateCommand implements CommandExecutor, TabCompleter {

    private final TekBlokPlugin plugin;

    public CrateCommand(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "olustur":
            case "set":
            case "create":
                if (!sender.hasPermission("tekblok.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("messages.no-permission"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("messages.only-players"));
                    return true;
                }
                Player pCreate = (Player) sender;
                String cType = args.length > 1 ? args[1].toLowerCase() : "cirak";
                Crate crateToCreate = plugin.getCrateManager().getCrate(cType);
                if (crateToCreate == null) {
                    pCreate.sendMessage(ColorUtils.color("&cGeçersiz kasa türü! Geçerli türler: &e"
                            + plugin.getCrateManager().getCrates().stream().map(Crate::getId).collect(Collectors.joining(", "))));
                    return true;
                }

                CrateLocation cl = plugin.getCrateManager().createCrate(pCreate, cType);
                Location loc = cl.getLocation();
                pCreate.sendMessage(ColorUtils.color("&a✔ " + crateToCreate.getDisplayName() + " &abaşarıyla oluşturuldu!"));
                pCreate.sendMessage(ColorUtils.color("&7Konum: &e" + loc.getWorld().getName() + " " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()));
                pCreate.sendMessage(ColorUtils.color("&7Hologram ve parçacık efektleri aktif edildi."));
                return true;

            case "sil":
            case "remove":
            case "delete":
                if (!sender.hasPermission("tekblok.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("messages.no-permission"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("messages.only-players"));
                    return true;
                }
                Player pRemove = (Player) sender;
                boolean removed = plugin.getCrateManager().removeCrate(pRemove);
                if (removed) {
                    pRemove.sendMessage(ColorUtils.color("&a✔ Baktığınız kasa ve hologramı başarıyla kaldırıldı."));
                } else {
                    pRemove.sendMessage(ColorUtils.color("&cBaktığınız blok geçerli bir kasa değil! Lütfen kasaya bakarak deneyin."));
                }
                return true;

            case "anahtarver":
            case "key":
            case "givekey":
                if (!sender.hasPermission("tekblok.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("messages.no-permission"));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ColorUtils.color("&cKullanım: /kasa anahtarver <oyuncu> <kasa_tipi> [adet]"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("messages.player-not-found"));
                    return true;
                }
                String kType = args[2].toLowerCase();
                Crate crate = plugin.getCrateManager().getCrate(kType);
                if (crate == null) {
                    sender.sendMessage(ColorUtils.color("&cGeçersiz kasa türü! Türler: &e"
                            + plugin.getCrateManager().getCrates().stream().map(Crate::getId).collect(Collectors.joining(", "))));
                    return true;
                }
                int count = 1;
                if (args.length > 3) {
                    try {
                        count = Integer.parseInt(args[3]);
                    } catch (NumberFormatException ignored) {}
                }
                plugin.getCrateManager().giveKey(target, kType, count);
                sender.sendMessage(ColorUtils.color("&a" + target.getName() + " oyuncusuna " + count + " adet " + crate.getKeyName() + " &averildi."));
                target.sendMessage(ColorUtils.color("&aEnvanterinize &e" + count + " adet " + crate.getKeyName() + " &aeklendi!"));
                return true;

            case "liste":
            case "list":
                if (!sender.hasPermission("tekblok.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("messages.no-permission"));
                    return true;
                }
                sender.sendMessage(ColorUtils.color("&6=== Aktif Kurulu Kasalar ==="));
                if (plugin.getCrateManager().getCrateLocations().isEmpty()) {
                    sender.sendMessage(ColorUtils.color("&7Henüz kurulmuş kasa bulunmuyor."));
                } else {
                    for (CrateLocation cLoc : plugin.getCrateManager().getCrateLocations().values()) {
                        Crate cr = plugin.getCrateManager().getCrate(cLoc.getCrateType());
                        String name = cr != null ? cr.getDisplayName() : cLoc.getCrateType();
                        Location l = cLoc.getLocation();
                        sender.sendMessage(ColorUtils.color("&8- " + name + " &7[" + l.getWorld().getName() + " "
                                + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + "]"));
                    }
                }
                return true;

            case "incele":
            case "bilgi":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("messages.only-players"));
                    return true;
                }
                Player pPreview = (Player) sender;
                String prevType = args.length > 1 ? args[1].toLowerCase() : "cirak";
                Crate prevCrate = plugin.getCrateManager().getCrate(prevType);
                if (prevCrate != null) {
                    CratePreviewGUI.open(pPreview, prevCrate);
                } else {
                    pPreview.sendMessage(ColorUtils.color("&cGeçersiz kasa türü!"));
                }
                return true;

            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.color("&6=== TekBlok Kasa Komutları ==="));
        if (sender.hasPermission("tekblok.admin")) {
            sender.sendMessage(ColorUtils.color("&e/kasa olustur <tip> &7- Baktığınız bloğu kasaya dönüştürür"));
            sender.sendMessage(ColorUtils.color("&e/kasa sil &7- Baktığınız kasayı kaldırır"));
            sender.sendMessage(ColorUtils.color("&e/kasa anahtarver <oyuncu> <tip> [adet] &7- Kasa anahtarı verir"));
            sender.sendMessage(ColorUtils.color("&e/kasa liste &7- Tüm aktif kasaları listeler"));
        }
        sender.sendMessage(ColorUtils.color("&e/kasa bilgi <tip> &7- Kasa ödüllerini inceler"));
        sender.sendMessage(ColorUtils.color("&6=============================="));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("tekblok.admin")) {
                list.addAll(Arrays.asList("olustur", "sil", "anahtarver", "liste", "bilgi"));
            } else {
                list.add("bilgi");
            }
            return list.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("olustur") || args[0].equalsIgnoreCase("bilgi"))) {
            return plugin.getCrateManager().getCrates().stream()
                    .map(Crate::getId)
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("anahtarver")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("anahtarver")) {
            return plugin.getCrateManager().getCrates().stream()
                    .map(Crate::getId)
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
