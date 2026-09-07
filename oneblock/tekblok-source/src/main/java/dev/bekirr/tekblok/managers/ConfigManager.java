package dev.bekirr.tekblok.managers;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private final TekBlokPlugin plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private FileConfiguration phasesConfig;
    private File phasesFile;

    public ConfigManager(TekBlokPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        // messages.yml
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // phases.yml
        phasesFile = new File(plugin.getDataFolder(), "phases.yml");
        if (!phasesFile.exists()) {
            plugin.saveResource("phases.yml", false);
        }
        phasesConfig = YamlConfiguration.loadConfiguration(phasesFile);
    }

    public void reload() {
        loadConfigs();
    }

    public String getRawMessage(String path, String def) {
        String msg = messagesConfig.getString(path, def);
        if (msg == null) return def;
        return msg;
    }

    public String getMessage(String path) {
        String prefix = messagesConfig.getString("prefix", "&6[TekBlok]&r ");
        String msg = messagesConfig.getString(path, "&cMesaj bulunamadı: " + path);
        return ColorUtils.color(prefix + msg);
    }

    public String getMessageNoPrefix(String path) {
        String msg = messagesConfig.getString(path, "&cMesaj bulunamadı: " + path);
        return ColorUtils.color(msg);
    }

    public String getPrefix() {
        return ColorUtils.color(messagesConfig.getString("prefix", "&6[TekBlok]&r "));
    }

    public FileConfiguration getPhasesConfig() {
        return phasesConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
}
