package dev.bekirr.tekblok.commands;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.Phase;
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

public class TekBlokAdminCommand implements CommandExecutor, TabCompleter {

    private final TekBlokPlugin plugin;

    public TekBlokAdminCommand(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tekblok.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("messages.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendAdminHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload":
                plugin.getConfigManager().reload();
                plugin.getPhaseManager().loadPhases();
                sender.sendMessage(plugin.getConfigManager().getMessage("messages.admin-reload-success"));
                return true;

            case "setlevel":
            case "setphase":
                if (args.length < 3) {
                    sender.sendMessage(ColorUtils.color("&cKullanım: /obadmin setlevel <oyuncu> <aşama_no>"));
                    return true;
                }
                Player targetLvl = Bukkit.getPlayer(args[1]);
                if (targetLvl == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("messages.player-not-found"));
                    return true;
                }
                Island isLvl = plugin.getIslandManager().getIslandByPlayer(targetLvl.getUniqueId());
                if (isLvl == null) {
                    sender.sendMessage(ColorUtils.color("&cBu oyuncunun adası bulunamadı."));
                    return true;
                }
                try {
                    int newLvl = Integer.parseInt(args[2]);
                    isLvl.setCurrentPhaseId(newLvl);
                    plugin.getIslandManager().saveIslands();
                    plugin.getDisplayManager().updateBossBar(isLvl);
                    Phase p = plugin.getPhaseManager().getPhase(newLvl);
                    sender.sendMessage(ColorUtils.color("&a" + targetLvl.getName() + " oyuncusunun aşaması " + p.getName() + " &aolarak ayarlandı."));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtils.color("&cGeçersiz aşama numarası!"));
                }
                return true;

            case "addblocks":
                if (args.length < 3) {
                    sender.sendMessage(ColorUtils.color("&cKullanım: /obadmin addblocks <oyuncu> <miktar>"));
                    return true;
                }
                Player targetBlk = Bukkit.getPlayer(args[1]);
                if (targetBlk == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("messages.player-not-found"));
                    return true;
                }
                Island isBlk = plugin.getIslandManager().getIslandByPlayer(targetBlk.getUniqueId());
                if (isBlk == null) {
                    sender.sendMessage(ColorUtils.color("&cBu oyuncunun adası bulunamadı."));
                    return true;
                }
                try {
                    int addAmt = Integer.parseInt(args[2]);
                    isBlk.setBlocksBroken(isBlk.getBlocksBroken() + addAmt);
                    plugin.getPhaseManager().checkPhaseProgress(isBlk, targetBlk);
                    plugin.getIslandManager().saveIslands();
                    plugin.getDisplayManager().updateBossBar(isBlk);
                    sender.sendMessage(ColorUtils.color("&a" + targetBlk.getName() + " adasına " + addAmt + " blok eklendi. Toplam: " + isBlk.getBlocksBroken()));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtils.color("&cGeçersiz blok miktarı!"));
                }
                return true;

            case "reset":
                if (args.length < 2) {
                    sender.sendMessage(ColorUtils.color("&cKullanım: /obadmin reset <oyuncu>"));
                    return true;
                }
                Player targetRes = Bukkit.getPlayer(args[1]);
                if (targetRes == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("messages.player-not-found"));
                    return true;
                }
                Island isRes = plugin.getIslandManager().getIslandByPlayer(targetRes.getUniqueId());
                if (isRes == null) {
                    sender.sendMessage(ColorUtils.color("&cBu oyuncunun adası bulunamadı."));
                    return true;
                }
                plugin.getIslandManager().resetIsland(isRes);
                sender.sendMessage(ColorUtils.color("&a" + targetRes.getName() + " oyuncusunun adası sıfırlandı."));
                return true;

            case "tp":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ColorUtils.color("&cBu komut sadece oyuncular içindir."));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ColorUtils.color("&cKullanım: /obadmin tp <oyuncu>"));
                    return true;
                }
                Player pAdmin = (Player) sender;
                Player targetTp = Bukkit.getPlayer(args[1]);
                if (targetTp == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("messages.player-not-found"));
                    return true;
                }
                Island isTp = plugin.getIslandManager().getIslandByPlayer(targetTp.getUniqueId());
                if (isTp == null) {
                    sender.sendMessage(ColorUtils.color("&cBu oyuncunun adası bulunamadı."));
                    return true;
                }
                pAdmin.teleport(isTp.getSpawnLocation());
                pAdmin.sendMessage(ColorUtils.color("&a" + targetTp.getName() + " oyuncusunun adasına ışınlandınız."));
                return true;

            default:
                sendAdminHelp(sender);
                return true;
        }
    }

    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.color("&6=== TekBlok Yönetici Komutları ==="));
        sender.sendMessage(ColorUtils.color("&e/obadmin reload &7- Yapılandırma dosyalarını yeniler"));
        sender.sendMessage(ColorUtils.color("&e/obadmin setlevel <oyuncu> <seviye> &7- Aşama seviyesini ayarlar"));
        sender.sendMessage(ColorUtils.color("&e/obadmin addblocks <oyuncu> <miktar> &7- Kırılan blok ekler"));
        sender.sendMessage(ColorUtils.color("&e/obadmin reset <oyuncu> &7- Oyuncunun adasını sıfırlar"));
        sender.sendMessage(ColorUtils.color("&e/obadmin tp <oyuncu> &7- Oyuncunun adasına ışınlar"));
        sender.sendMessage(ColorUtils.color("&6==================================="));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("tekblok.admin")) {
            return new ArrayList<>();
        }
        if (args.length == 1) {
            List<String> subs = Arrays.asList("reload", "setlevel", "addblocks", "reset", "tp");
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && Arrays.asList("setlevel", "addblocks", "reset", "tp").contains(args[0].toLowerCase())) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
