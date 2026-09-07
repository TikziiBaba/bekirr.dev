package dev.bekirr.tekblok.managers;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.MarketCategory;
import dev.bekirr.tekblok.models.MarketItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class MarketManager {

    private final TekBlokPlugin plugin;
    private final Map<String, MarketCategory> categories = new LinkedHashMap<>();
    private File configFile;
    private FileConfiguration config;

    public MarketManager(TekBlokPlugin plugin) {
        this.plugin = plugin;
        loadMarket();
    }

    public void loadMarket() {
        categories.clear();
        configFile = new File(plugin.getDataFolder(), "market.yml");
        if (!configFile.exists()) {
            plugin.saveResource("market.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        ConfigurationSection catSection = config.getConfigurationSection("categories");
        if (catSection == null) {
            plugin.getLogger().warning("market.yml içerisinde 'categories' bölümü bulunamadı!");
            return;
        }

        for (String catKey : catSection.getKeys(false)) {
            ConfigurationSection sec = catSection.getConfigurationSection(catKey);
            if (sec == null) continue;

            String name = ChatColor.translateAlternateColorCodes('&', sec.getString("name", catKey));
            String iconStr = sec.getString("icon", "CHEST");
            Material iconMat = Material.matchMaterial(iconStr);
            if (iconMat == null) iconMat = Material.CHEST;
            int slot = sec.getInt("slot", 10);
            String desc = ChatColor.translateAlternateColorCodes('&', sec.getString("description", ""));

            List<MarketItem> items = new ArrayList<>();
            ConfigurationSection itemsSec = sec.getConfigurationSection("items");
            if (itemsSec != null) {
                for (String itemKey : itemsSec.getKeys(false)) {
                    ConfigurationSection itemData = itemsSec.getConfigurationSection(itemKey);
                    if (itemData == null) continue;

                    String crateId = itemData.getString("crate");
                    Material itemMat = Material.matchMaterial(itemKey);
                    if (itemMat == null && crateId != null) {
                        itemMat = Material.TRIPWIRE_HOOK;
                    }

                    if (itemMat == null) {
                        plugin.getLogger().warning("market.yml: Geçersiz materyal '" + itemKey + "' atlandı.");
                        continue;
                    }

                    String itemName = ChatColor.translateAlternateColorCodes('&', itemData.getString("name", itemKey));
                    double price = itemData.getDouble("price", 10.0);

                    items.add(new MarketItem(itemMat, itemName, price, crateId));
                }
            }

            categories.put(catKey.toLowerCase(), new MarketCategory(catKey.toLowerCase(), name, iconMat, slot, desc, items));
        }

        plugin.getLogger().info("Market yüklendi: " + categories.size() + " kategori aktif.");
    }

    public Collection<MarketCategory> getCategories() {
        return categories.values();
    }

    public MarketCategory getCategory(String id) {
        return categories.get(id.toLowerCase());
    }

    public boolean buyItem(Player player, MarketItem item, int amount) {
        if (amount <= 0) return false;

        double totalCost = item.getPrice() * amount;
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());

        if (balance < totalCost) {
            player.sendMessage(plugin.getConfigManager().getMessage("messages.market-insufficient-funds")
                    .replace("{cost}", plugin.getEconomyManager().format(totalCost)));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return false;
        }

        Material mat = item.getMaterial();
        int freeSpace = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack == null || stack.getType() == Material.AIR) {
                freeSpace += mat.getMaxStackSize();
            } else if (stack.getType() == mat && stack.getAmount() < mat.getMaxStackSize()) {
                freeSpace += (mat.getMaxStackSize() - stack.getAmount());
            }
        }

        if (freeSpace < amount) {
            player.sendMessage(plugin.getConfigManager().getMessage("messages.market-inventory-full"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return false;
        }

        if (!plugin.getEconomyManager().withdraw(player.getUniqueId(), totalCost)) {
            player.sendMessage(plugin.getConfigManager().getMessage("messages.market-insufficient-funds")
                    .replace("{cost}", plugin.getEconomyManager().format(totalCost)));
            return false;
        }

        if (item.isCrateKey() && plugin.getCrateManager() != null) {
            plugin.getCrateManager().giveKey(player, item.getCrateId(), amount);
        } else {
            player.getInventory().addItem(new ItemStack(mat, amount));
        }

        player.sendMessage(plugin.getConfigManager().getMessage("messages.market-bought-success")
                .replace("{amount}", String.valueOf(amount))
                .replace("{item}", item.getName())
                .replace("{cost}", plugin.getEconomyManager().format(totalCost)));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
        return true;
    }
}
