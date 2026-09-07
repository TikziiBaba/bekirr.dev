package dev.bekirr.tekblok.commands;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.gui.SellGUI;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SellCommand implements CommandExecutor, TabCompleter {

    private final TekBlokPlugin plugin;

    public SellCommand(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.color("&cBu komutu sadece oyuncular kullanabilir!"));
            return true;
        }

        Player player = (Player) sender;
        SellGUI.open(player, plugin);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
