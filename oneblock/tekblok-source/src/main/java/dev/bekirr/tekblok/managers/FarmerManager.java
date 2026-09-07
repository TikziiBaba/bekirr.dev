package dev.bekirr.tekblok.managers;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.IslandFarmer;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FarmerManager {

    public static final String FARMER_NPC_NAME = ColorUtils.color("&a&l[ ÇİFTÇİ ] &e(Tıkla)");

    private final TekBlokPlugin plugin;
    private final Map<UUID, IslandFarmer> farmers = new HashMap<>();
    private final Map<Material, Double> itemPrices = new EnumMap<>(Material.class);
    private File dataFile;
    private FileConfiguration dataConfig;

    public FarmerManager(TekBlokPlugin plugin) {
        this.plugin = plugin;
        loadPrices();
        loadFarmers();
    }

    public void loadPrices() {
        itemPrices.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection sec = config.getConfigurationSection("farmer.prices");

        // Varsayılan fiyatlar
        Map<Material, Double> defaults = new EnumMap<>(Material.class);
        // Tarlalar
        defaults.put(Material.COBBLESTONE, 0.5);
        defaults.put(Material.WHEAT, 2.0);
        defaults.put(Material.WHEAT_SEEDS, 0.2);
        defaults.put(Material.CARROT, 2.5);
        defaults.put(Material.POTATO, 2.5);
        defaults.put(Material.BEETROOT, 3.0);
        defaults.put(Material.BEETROOT_SEEDS, 0.2);
        defaults.put(Material.MELON_SLICE, 1.0);
        defaults.put(Material.MELON, 8.0);
        defaults.put(Material.PUMPKIN, 5.0);
        defaults.put(Material.SUGAR_CANE, 3.5);
        defaults.put(Material.CACTUS, 3.0);
        defaults.put(Material.NETHER_WART, 5.0);
        defaults.put(Material.COCOA_BEANS, 4.0);
        defaults.put(Material.RED_MUSHROOM, 3.0);
        defaults.put(Material.BROWN_MUSHROOM, 3.0);
        defaults.put(Material.SWEET_BERRIES, 1.5);
        defaults.put(Material.BAMBOO, 1.0);
        defaults.put(Material.KELP, 1.0);
        defaults.put(Material.DRIED_KELP, 1.5);
        defaults.put(Material.SEA_PICKLE, 4.0);
        defaults.put(Material.CHORUS_FRUIT, 3.0);
        defaults.put(Material.APPLE, 3.0);
        defaults.put(Material.POISONOUS_POTATO, 0.5);

        // Moblar ve Her Türlü Canlı Ganimetleri
        defaults.put(Material.ROTTEN_FLESH, 1.5);
        defaults.put(Material.BONE, 2.5);
        defaults.put(Material.STRING, 3.0);
        defaults.put(Material.SPIDER_EYE, 4.0);
        defaults.put(Material.GUNPOWDER, 5.0);
        defaults.put(Material.ENDER_PEARL, 10.0);
        defaults.put(Material.BLAZE_ROD, 12.0);
        defaults.put(Material.IRON_INGOT, 15.0);
        defaults.put(Material.GOLD_INGOT, 25.0);
        defaults.put(Material.GOLD_NUGGET, 2.5);
        defaults.put(Material.SLIME_BALL, 8.0);
        defaults.put(Material.MAGMA_CREAM, 9.0);
        defaults.put(Material.ARROW, 1.0);
        defaults.put(Material.FEATHER, 1.0);
        defaults.put(Material.LEATHER, 4.0);
        defaults.put(Material.PORKCHOP, 2.5);
        defaults.put(Material.COOKED_PORKCHOP, 5.0);
        defaults.put(Material.BEEF, 3.0);
        defaults.put(Material.COOKED_BEEF, 6.0);
        defaults.put(Material.CHICKEN, 2.0);
        defaults.put(Material.COOKED_CHICKEN, 4.5);
        defaults.put(Material.MUTTON, 2.5);
        defaults.put(Material.COOKED_MUTTON, 5.0);
        defaults.put(Material.RABBIT, 3.0);
        defaults.put(Material.COOKED_RABBIT, 6.0);
        defaults.put(Material.RABBIT_FOOT, 15.0);
        defaults.put(Material.RABBIT_HIDE, 3.0);
        defaults.put(Material.EGG, 1.5);
        defaults.put(Material.TURTLE_EGG, 25.0);
        defaults.put(Material.SCUTE, 20.0);
        defaults.put(Material.COD, 2.5);
        defaults.put(Material.COOKED_COD, 5.0);
        defaults.put(Material.SALMON, 3.0);
        defaults.put(Material.COOKED_SALMON, 6.0);
        defaults.put(Material.TROPICAL_FISH, 5.0);
        defaults.put(Material.PUFFERFISH, 8.0);
        defaults.put(Material.PRISMARINE_SHARD, 4.0);
        defaults.put(Material.PRISMARINE_CRYSTALS, 6.0);
        defaults.put(Material.GHAST_TEAR, 25.0);
        defaults.put(Material.INK_SAC, 3.0);
        defaults.put(Material.WITHER_SKELETON_SKULL, 150.0);
        defaults.put(Material.NETHER_STAR, 500.0);
        defaults.put(Material.SHULKER_SHELL, 40.0);
        defaults.put(Material.PHANTOM_MEMBRANE, 10.0);
        defaults.put(Material.DRAGON_BREATH, 50.0);
        defaults.put(Material.TOTEM_OF_UNDYING, 200.0);
        defaults.put(Material.EMERALD, 40.0);
        defaults.put(Material.NAUTILUS_SHELL, 30.0);
        defaults.put(Material.TRIDENT, 250.0);
        defaults.put(Material.GLOWSTONE_DUST, 3.0);
        defaults.put(Material.REDSTONE, 3.0);
        defaults.put(Material.SUGAR, 2.0);
        defaults.put(Material.GLASS_BOTTLE, 1.0);
        defaults.put(Material.STICK, 0.5);
        defaults.put(Material.HONEY_BOTTLE, 5.0);
        defaults.put(Material.HONEYCOMB, 4.0);
        defaults.put(Material.WITHER_ROSE, 20.0);

        // Tüm Yünler
        defaults.put(Material.WHITE_WOOL, 2.0);
        defaults.put(Material.ORANGE_WOOL, 2.0);
        defaults.put(Material.MAGENTA_WOOL, 2.0);
        defaults.put(Material.LIGHT_BLUE_WOOL, 2.0);
        defaults.put(Material.YELLOW_WOOL, 2.0);
        defaults.put(Material.LIME_WOOL, 2.0);
        defaults.put(Material.PINK_WOOL, 2.0);
        defaults.put(Material.GRAY_WOOL, 2.0);
        defaults.put(Material.LIGHT_GRAY_WOOL, 2.0);
        defaults.put(Material.CYAN_WOOL, 2.0);
        defaults.put(Material.PURPLE_WOOL, 2.0);
        defaults.put(Material.BLUE_WOOL, 2.0);
        defaults.put(Material.BROWN_WOOL, 2.0);
        defaults.put(Material.GREEN_WOOL, 2.0);
        defaults.put(Material.RED_WOOL, 2.0);
        defaults.put(Material.BLACK_WOOL, 2.0);

        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat != null) {
                    double price = sec.getDouble(key, 1.0);
                    itemPrices.put(mat, price);
                }
            }
        }

        // Eksik olanları varsayılandan tamamla
        for (Map.Entry<Material, Double> entry : defaults.entrySet()) {
            if (!itemPrices.containsKey(entry.getKey())) {
                itemPrices.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void loadFarmers() {
        farmers.clear();
        dataFile = new File(plugin.getDataFolder(), "data/farmers.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("farmers.yml oluşturulamadı: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = dataConfig.getConfigurationSection("farmers");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                UUID ownerUUID = UUID.fromString(key);
                ConfigurationSection fSec = section.getConfigurationSection(key);
                if (fSec == null) continue;

                boolean requirePurchase = plugin.getConfig().getBoolean("farmer.require-purchase", true);
                boolean unlocked = fSec.getBoolean("unlocked", !requirePurchase);
                boolean autoCollect = fSec.getBoolean("auto-collect", true);
                boolean collectGroundDrops = fSec.getBoolean("collect-ground-drops", true);
                double totalEarned = fSec.getDouble("total-earned", 0.0);

                Location npcLoc = null;
                if (fSec.contains("npc.world")) {
                    World w = Bukkit.getWorld(fSec.getString("npc.world"));
                    if (w != null) {
                        double x = fSec.getDouble("npc.x");
                        double y = fSec.getDouble("npc.y");
                        double z = fSec.getDouble("npc.z");
                        float yaw = (float) fSec.getDouble("npc.yaw");
                        float pitch = (float) fSec.getDouble("npc.pitch");
                        npcLoc = new Location(w, x, y, z, yaw, pitch);
                    }
                }

                IslandFarmer farmer = new IslandFarmer(ownerUUID, unlocked, autoCollect, collectGroundDrops, totalEarned, npcLoc);

                ConfigurationSection storSec = fSec.getConfigurationSection("storage");
                if (storSec != null) {
                    for (String matKey : storSec.getKeys(false)) {
                        Material mat = Material.matchMaterial(matKey);
                        if (mat != null) {
                            int count = storSec.getInt(matKey, 0);
                            if (count > 0) {
                                farmer.addItem(mat, count);
                            }
                        }
                    }
                }

                farmers.put(ownerUUID, farmer);
            } catch (IllegalArgumentException ignored) {
            }
        }
        plugin.getLogger().info("Toplam " + farmers.size() + " adet Çiftçi verisi yüklendi.");
    }

    public void saveFarmers() {
        if (dataConfig == null || dataFile == null) return;
        dataConfig.set("farmers", null);

        for (IslandFarmer farmer : farmers.values()) {
            String path = "farmers." + farmer.getIslandOwner().toString();
            dataConfig.set(path + ".unlocked", farmer.isUnlocked());
            dataConfig.set(path + ".auto-collect", farmer.isAutoCollect());
            dataConfig.set(path + ".collect-ground-drops", farmer.isCollectGroundDrops());
            dataConfig.set(path + ".total-earned", farmer.getTotalEarned());

            if (farmer.getNpcLocation() != null && farmer.getNpcLocation().getWorld() != null) {
                dataConfig.set(path + ".npc.world", farmer.getNpcLocation().getWorld().getName());
                dataConfig.set(path + ".npc.x", farmer.getNpcLocation().getX());
                dataConfig.set(path + ".npc.y", farmer.getNpcLocation().getY());
                dataConfig.set(path + ".npc.z", farmer.getNpcLocation().getZ());
                dataConfig.set(path + ".npc.yaw", farmer.getNpcLocation().getYaw());
                dataConfig.set(path + ".npc.pitch", farmer.getNpcLocation().getPitch());
            }

            for (Map.Entry<Material, Integer> entry : farmer.getStorage().entrySet()) {
                if (entry.getValue() > 0) {
                    dataConfig.set(path + ".storage." + entry.getKey().name(), entry.getValue());
                }
            }
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("farmers.yml kaydedilemedi: " + e.getMessage());
        }
    }

    public IslandFarmer getFarmer(UUID islandOwner) {
        return farmers.computeIfAbsent(islandOwner, uuid -> {
            boolean requirePurchase = plugin.getConfig().getBoolean("farmer.require-purchase", true);
            return new IslandFarmer(uuid, !requirePurchase, true, true, 0.0, null);
        });
    }

    public boolean buyFarmer(Player player, Island island) {
        if (player == null || island == null) return false;
        IslandFarmer farmer = getFarmer(island.getOwnerUUID());
        if (farmer.isUnlocked()) return true;

        double cost = plugin.getConfig().getDouble("farmer.buy-price", 500.0);
        if (!plugin.getEconomyManager().has(player.getUniqueId(), cost)) {
            return false;
        }

        plugin.getEconomyManager().withdraw(player.getUniqueId(), cost);
        farmer.setUnlocked(true);
        saveFarmers();
        return true;
    }

    public void deleteFarmerData(UUID islandOwner) {
        IslandFarmer farmer = farmers.remove(islandOwner);
        if (farmer != null && farmer.getNpcLocation() != null && farmer.getNpcLocation().getWorld() != null) {
            for (Entity e : farmer.getNpcLocation().getWorld().getNearbyEntities(farmer.getNpcLocation(), 5, 5, 5)) {
                if (e instanceof Villager && FARMER_NPC_NAME.equals(e.getCustomName())) {
                    e.remove();
                }
            }
        }
        saveFarmers();
    }

    public boolean isFarmerItem(Material mat) {
        return mat != null && (itemPrices.containsKey(mat) || mat.isItem());
    }

    public double getPrice(Material mat) {
        return itemPrices.getOrDefault(mat, 1.0);
    }

    public Map<Material, Double> getItemPrices() {
        return itemPrices;
    }

    public double sellSingleItem(Player player, IslandFarmer farmer, Material mat) {
        int count = farmer.getItemCount(mat);
        if (count <= 0) return 0.0;

        double unitPrice = getPrice(mat);
        farmer.removeItem(mat, 1);
        farmer.addTotalEarned(unitPrice);
        plugin.getEconomyManager().addBalance(player.getUniqueId(), unitPrice);
        saveFarmers();
        return unitPrice;
    }

    public double sellItem(Player player, IslandFarmer farmer, Material mat) {
        int count = farmer.getItemCount(mat);
        if (count <= 0) return 0.0;

        double unitPrice = getPrice(mat);
        double totalEarned = count * unitPrice;

        farmer.removeItem(mat, count);
        farmer.addTotalEarned(totalEarned);
        plugin.getEconomyManager().addBalance(player.getUniqueId(), totalEarned);
        saveFarmers();
        return totalEarned;
    }

    public double sellAll(Player player, IslandFarmer farmer) {
        if (farmer.isEmpty()) return 0.0;

        double totalEarned = 0.0;
        for (Map.Entry<Material, Integer> entry : new HashMap<>(farmer.getStorage()).entrySet()) {
            double unitPrice = getPrice(entry.getKey());
            totalEarned += entry.getValue() * unitPrice;
        }

        farmer.clearStorage();
        farmer.addTotalEarned(totalEarned);
        plugin.getEconomyManager().addBalance(player.getUniqueId(), totalEarned);
        saveFarmers();
        return totalEarned;
    }

    public Villager spawnOrMoveFarmerNPC(Island island, Location targetLoc) {
        if (island == null || targetLoc == null || targetLoc.getWorld() == null) return null;
        IslandFarmer farmer = getFarmer(island.getOwnerUUID());

        // Eski NPC'yi temizle
        if (farmer.getNpcLocation() != null && farmer.getNpcLocation().getWorld() != null) {
            for (Entity e : farmer.getNpcLocation().getWorld().getNearbyEntities(farmer.getNpcLocation(), 8, 8, 8)) {
                if (e instanceof Villager && FARMER_NPC_NAME.equals(e.getCustomName())) {
                    e.remove();
                }
            }
        }

        Location spawnLoc = targetLoc.clone();
        Villager villager = (Villager) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.VILLAGER);
        villager.setCustomName(FARMER_NPC_NAME);
        villager.setCustomNameVisible(true);
        villager.setProfession(Villager.Profession.FARMER);
        villager.setVillagerType(Villager.Type.PLAINS);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.setCollidable(false);

        farmer.setNpcLocation(spawnLoc);
        saveFarmers();
        return villager;
    }
}
