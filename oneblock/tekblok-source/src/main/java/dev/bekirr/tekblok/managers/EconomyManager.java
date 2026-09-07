package dev.bekirr.tekblok.managers;

import dev.bekirr.tekblok.TekBlokPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final TekBlokPlugin plugin;
    private final Map<UUID, Double> balances = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;
    private final DecimalFormat formatter;

    public EconomyManager(TekBlokPlugin plugin) {
        this.plugin = plugin;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("tr", "TR"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        this.formatter = new DecimalFormat("#,##0.00", symbols);
        loadBalances();
    }

    public void loadBalances() {
        balances.clear();
        dataFile = new File(plugin.getDataFolder(), "data/economy.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("economy.yml dosyası oluşturulamadı: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = dataConfig.getConfigurationSection("balances");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    double bal = section.getDouble(key, 0.0);
                    balances.put(uuid, bal);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        plugin.getLogger().info("Toplam " + balances.size() + " adet oyuncu bakiyesi yüklendi.");
    }

    public void saveBalances() {
        if (dataConfig == null || dataFile == null) return;
        dataConfig.set("balances", null);
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            dataConfig.set("balances." + entry.getKey().toString(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("economy.yml kaydedilemedi: " + e.getMessage());
        }
    }

    public double getBalance(UUID uuid) {
        if (!balances.containsKey(uuid)) {
            double starting = plugin.getConfig().getDouble("economy.starting-balance", 100.0);
            balances.put(uuid, starting);
            return starting;
        }
        return balances.get(uuid);
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, Math.max(0.0, amount));
    }

    public void addBalance(UUID uuid, double amount) {
        if (amount <= 0) return;
        double current = getBalance(uuid);
        balances.put(uuid, current + amount);
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) return false;
        double current = getBalance(uuid);
        if (current < amount) return false;
        balances.put(uuid, current - amount);
        return true;
    }

    public boolean has(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public String format(double amount) {
        String symbol = plugin.getConfig().getString("economy.currency-symbol", "₺");
        return formatter.format(amount) + " " + symbol;
    }
}
