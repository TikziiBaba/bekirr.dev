package dev.bekirr.tekblok.managers;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.CustomVillager;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VillagerManager {

    private final TekBlokPlugin plugin;
    private final Map<String, CustomVillager> villagers = new HashMap<>();
    private File configFile;
    private FileConfiguration config;

    public VillagerManager(TekBlokPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
        // Dünyalar tam yüklendikten sonra köylüleri spawn et (1 saniye gecikmeyle)
        Bukkit.getScheduler().runTaskLater(plugin, this::spawnAllVillagers, 20L);
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "villagers.yml");
        if (!configFile.exists()) {
            plugin.saveResource("villagers.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        villagers.clear();
        ConfigurationSection sec = config.getConfigurationSection("villagers");
        if (sec != null) {
            for (String id : sec.getKeys(false)) {
                String type = sec.getString(id + ".type", "eritici");
                String wName = sec.getString(id + ".world", "spawn");
                World world = Bukkit.getWorld(wName);
                if (world != null) {
                    double x = sec.getDouble(id + ".x");
                    double y = sec.getDouble(id + ".y");
                    double z = sec.getDouble(id + ".z");
                    float yaw = (float) sec.getDouble(id + ".yaw");
                    float pitch = (float) sec.getDouble(id + ".pitch");
                    Location loc = new Location(world, x, y, z, yaw, pitch);
                    villagers.put(id, new CustomVillager(id, type, loc, null));
                }
            }
        }
    }

    public void spawnAllVillagers() {
        for (CustomVillager cv : villagers.values()) {
            spawnEntity(cv);
        }
    }

    public Villager spawnEntity(CustomVillager cv) {
        Location loc = cv.getLocation();
        if (loc.getWorld() == null) return null;

        // Önce aynı yerdeki eski NPC'yi temizle
        for (Entity e : loc.getWorld().getNearbyEntities(loc, 1.5, 2.0, 1.5)) {
            if (e instanceof Villager && e.hasMetadata("tekblok_npc_id")) {
                e.remove();
            }
        }

        Villager v = loc.getWorld().spawn(loc, Villager.class);
        v.setAI(false);
        v.setCollidable(false);
        v.setInvulnerable(true);
        v.setSilent(true);
        v.setRemoveWhenFarAway(false);
        v.setCustomNameVisible(true);

        if (cv.getType().equalsIgnoreCase("eritici")) {
            v.setCustomName(ColorUtils.color("&6&lMaden Eritici &8| &eDemirci"));
            v.setProfession(Villager.Profession.ARMORER);
            v.setVillagerType(Villager.Type.PLAINS);
        } else if (cv.getType().equalsIgnoreCase("ada")) {
            v.setCustomName(ColorUtils.color("&b&lAda Işınlayıcısı &8| &3Kaptan"));
            v.setProfession(Villager.Profession.FISHERMAN);
            v.setVillagerType(Villager.Type.SWAMP);
        } else {
            v.setCustomName(ColorUtils.color("&a&lTakasçı Köylü &8| &2Tüccar"));
            v.setProfession(Villager.Profession.CLERIC);
            v.setVillagerType(Villager.Type.DESERT);
        }

        v.setMetadata("tekblok_npc_id", new FixedMetadataValue(plugin, cv.getId()));
        v.setMetadata("tekblok_npc_type", new FixedMetadataValue(plugin, cv.getType()));
        cv.setEntityUUID(v.getUniqueId());
        return v;
    }

    public CustomVillager createVillager(Player player, String type) {
        String id = "npc_" + System.currentTimeMillis();
        Location loc = player.getLocation().clone();
        // Bloğun tam ortasına hizala
        loc.setX(Math.floor(loc.getX()) + 0.5);
        loc.setZ(Math.floor(loc.getZ()) + 0.5);

        CustomVillager cv = new CustomVillager(id, type, loc, null);
        villagers.put(id, cv);
        spawnEntity(cv);
        saveVillagerToConfig(cv);
        return cv;
    }

    public boolean removeClosestVillager(Player player) {
        Location pLoc = player.getLocation();
        CustomVillager closest = null;
        double minDistance = 5.0; // 5 blok mesafe

        for (CustomVillager cv : villagers.values()) {
            if (cv.getLocation().getWorld().equals(pLoc.getWorld())) {
                double dist = cv.getLocation().distance(pLoc);
                if (dist < minDistance) {
                    minDistance = dist;
                    closest = cv;
                }
            }
        }

        if (closest != null) {
            // Canlıyı sil
            for (Entity e : closest.getLocation().getWorld().getNearbyEntities(closest.getLocation(), 2.0, 2.0, 2.0)) {
                if (e instanceof Villager && e.hasMetadata("tekblok_npc_id")) {
                    e.remove();
                }
            }

            villagers.remove(closest.getId());
            config.set("villagers." + closest.getId(), null);
            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().severe("villagers.yml kaydedilemedi: " + e.getMessage());
            }
            return true;
        }

        return false;
    }

    private void saveVillagerToConfig(CustomVillager cv) {
        String path = "villagers." + cv.getId();
        config.set(path + ".type", cv.getType());
        config.set(path + ".world", cv.getLocation().getWorld().getName());
        config.set(path + ".x", cv.getLocation().getX());
        config.set(path + ".y", cv.getLocation().getY());
        config.set(path + ".z", cv.getLocation().getZ());
        config.set(path + ".yaw", cv.getLocation().getYaw());
        config.set(path + ".pitch", cv.getLocation().getPitch());

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("villagers.yml kaydedilemedi: " + e.getMessage());
        }
    }

    public void removeAllEntities() {
        for (CustomVillager cv : villagers.values()) {
            if (cv.getLocation().getWorld() != null) {
                for (Entity e : cv.getLocation().getWorld().getNearbyEntities(cv.getLocation(), 2.0, 2.0, 2.0)) {
                    if (e instanceof Villager && e.hasMetadata("tekblok_npc_id")) {
                        e.remove();
                    }
                }
            }
        }
    }

    public Map<String, CustomVillager> getVillagers() {
        return villagers;
    }
}
