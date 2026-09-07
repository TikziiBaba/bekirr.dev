package dev.bekirr.tekblok.commands;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.gui.FarmerGUI;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.IslandFarmer;
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

public class FarmerCommand implements CommandExecutor, TabCompleter {

    private final TekBlokPlugin plugin;

    public FarmerCommand(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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

        IslandFarmer farmer = plugin.getFarmerManager().getFarmer(island.getOwnerUUID());

        // Çiftçi henüz satın alınmadıysa önce satın alma menüsünü aç
        if (!farmer.isUnlocked()) {
            FarmerGUI.openFarmerBuyMenu(player, plugin, island);
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("menu") || args[0].equalsIgnoreCase("gui")) {
            FarmerGUI.openFarmerMenu(player, plugin, island);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("getir") || sub.equals("cagir") || sub.equals("summon")) {
            plugin.getFarmerManager().spawnOrMoveFarmerNPC(island, player.getLocation());
            player.sendMessage(plugin.getConfigManager().getMessage("messages.farmer-summoned"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
            return true;
        }

        if (sub.equals("sat") || sub.equals("sell")) {
            double earned = plugin.getFarmerManager().sellAll(player, farmer);
            if (earned > 0) {
                player.sendMessage(plugin.getConfigManager().getMessage("messages.farmer-sold-all")
                        .replace("{amount}", plugin.getEconomyManager().format(earned)));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("messages.farmer-empty"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            }
            return true;
        }

        if (sub.equals("durum") || sub.equals("toggle")) {
            farmer.setAutoCollect(!farmer.isAutoCollect());
            plugin.getFarmerManager().saveFarmers();
            String status = farmer.isAutoCollect() ? "§aAçık" : "§cKapalı";
            player.sendMessage("§eÇiftçi otomatik toplama durumu: " + status);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return true;
        }

        FarmerGUI.openFarmerMenu(player, plugin, island);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = Arrays.asList("menu", "getir", "sat", "durum");
            return list.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
