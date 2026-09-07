package dev.bekirr.tekblok.commands;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.gui.MarketGUI;
import dev.bekirr.tekblok.models.MarketCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MarketCommand implements CommandExecutor, TabCompleter {

    private final TekBlokPlugin plugin;

    public MarketCommand(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                plugin.getMarketManager().loadMarket();
                sender.sendMessage("§aMarket yapılandırması (market.yml) başarıyla yenilendi.");
                return true;
            }
            sender.sendMessage("Bu komut sadece oyuncular tarafından kullanılabilir.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload") && player.hasPermission("tekblok.admin")) {
                plugin.getMarketManager().loadMarket();
                player.sendMessage("§aMarket yapılandırması (market.yml) başarıyla yenilendi.");
                return true;
            }

            MarketCategory cat = plugin.getMarketManager().getCategory(args[0]);
            if (cat != null) {
                MarketGUI.openCategoryMenu(player, plugin, cat);
                return true;
            }
        }

        MarketGUI.openMainMenu(player, plugin);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = plugin.getMarketManager().getCategories().stream()
                    .map(MarketCategory::getId)
                    .collect(Collectors.toList());
            if (sender.hasPermission("tekblok.admin")) {
                list.add("reload");
            }
            return list.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
