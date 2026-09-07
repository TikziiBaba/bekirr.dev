package dev.bekirr.tekblok.commands;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpawnCommand implements CommandExecutor, TabCompleter {

    private final TekBlokPlugin plugin;

    public SpawnCommand(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("messages.only-players"));
            return true;
        }

        Player player = (Player) sender;
        String cmdName = command.getName().toLowerCase();

        // 1. /setspawn veya /spawnset
        if (cmdName.equals("setspawn") || cmdName.equals("spawnset")) {
            if (!player.hasPermission("tekblok.admin")) {
                player.sendMessage(plugin.getConfigManager().getMessage("messages.no-permission"));
                return true;
            }
            Location loc = player.getLocation();
            plugin.getSpawnManager().setSpawnLocation(loc);
            player.sendMessage(ColorUtils.color("&a✔ Spawn / Lobi noktası başarıyla kaydedildi!"));
            player.sendMessage(ColorUtils.color("&7Dünya: &e" + loc.getWorld().getName() + " &8| &7X: &e" + String.format("%.1f", loc.getX())
                    + " &8| &7Y: &e" + String.format("%.1f", loc.getY()) + " &8| &7Z: &e" + String.format("%.1f", loc.getZ())));
            return true;
        }

        // 2. /spawn [ayarla]
        if (args.length > 0 && (args[0].equalsIgnoreCase("ayarla") || args[0].equalsIgnoreCase("set"))) {
            if (!player.hasPermission("tekblok.admin")) {
                player.sendMessage(plugin.getConfigManager().getMessage("messages.no-permission"));
                return true;
            }
            Location loc = player.getLocation();
            plugin.getSpawnManager().setSpawnLocation(loc);
            player.sendMessage(ColorUtils.color("&a✔ Spawn / Lobi noktası başarıyla kaydedildi!"));
            player.sendMessage(ColorUtils.color("&7Dünya: &e" + loc.getWorld().getName() + " &8| &7X: &e" + String.format("%.1f", loc.getX())
                    + " &8| &7Y: &e" + String.format("%.1f", loc.getY()) + " &8| &7Z: &e" + String.format("%.1f", loc.getZ())));
            return true;
        }

        // /spawn doğrudan lobiye ışınlar
        plugin.getSpawnManager().teleportToSpawn(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender.hasPermission("tekblok.admin") && args.length == 1) {
            List<String> list = new ArrayList<>();
            if ("ayarla".startsWith(args[0].toLowerCase())) list.add("ayarla");
            if ("set".startsWith(args[0].toLowerCase())) list.add("set");
            return list;
        }
        return Collections.emptyList();
    }
}
