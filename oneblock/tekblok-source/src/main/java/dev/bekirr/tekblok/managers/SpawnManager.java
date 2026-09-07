package dev.bekirr.tekblok.managers;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpawnManager {

    private final TekBlokPlugin plugin;
    private Location spawnLocation;
    private final List<ArmorStand> welcomeHolograms = new ArrayList<>();
    private File spawnDataFile;
    private FileConfiguration spawnDataConfig;

    public SpawnManager(TekBlokPlugin plugin) {
        this.plugin = plugin;
        initSpawnData();
        initSpawnWorld();
        loadSpawnLocation();

        // Dünyalar tam yüklendikten 1.5 saniye sonra hologramı oluştur
        Bukkit.getScheduler().runTaskLater(plugin, this::spawnWelcomeHologram, 30L);
    }

    private void initSpawnData() {
        File dataDir = new File(plugin.getDataFolder(), "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        spawnDataFile = new File(dataDir, "spawn.yml");
        if (spawnDataFile.exists()) {
            spawnDataConfig = YamlConfiguration.loadConfiguration(spawnDataFile);
        } else {
            spawnDataConfig = new YamlConfiguration();
        }
    }

    public void initSpawnWorld() {
        FileConfiguration config = plugin.getConfig();
        String worldName = config.getString("spawn.world", "spawn");
        boolean autoLoad = config.getBoolean("spawn.auto-load-world", true);

        if (autoLoad && Bukkit.getWorld(worldName) == null) {
            File worldDir = new File(Bukkit.getWorldContainer(), worldName);
            if (worldDir.exists()) {
                plugin.getLogger().info("Lobi dünyası '" + worldName + "' yükleniyor...");
                World world = Bukkit.createWorld(new WorldCreator(worldName));
                if (world != null) {
                    world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                    world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                    world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                    world.setGameRule(GameRule.KEEP_INVENTORY, true);
                    plugin.getLogger().info("Lobi dünyası '" + worldName + "' başarıyla yüklendi!");
                }
            } else {
                plugin.getLogger().warning("Lobi dünyası klasörü '" + worldName + "' bulunamadı!");
            }
        }
    }

    public void loadSpawnLocation() {
        if (spawnDataFile == null) {
            initSpawnData();
        }

        // 1. Önce kalıcı data/spawn.yml dosyasından yükle (config.yml güncellemelerinden asla etkilenmez)
        if (spawnDataFile != null && spawnDataFile.exists() && spawnDataConfig.getBoolean("spawn.set", false)) {
            String wName = spawnDataConfig.getString("spawn.world", "spawn");
            World world = Bukkit.getWorld(wName);
            if (world == null && new File(Bukkit.getWorldContainer(), wName).exists()) {
                world = Bukkit.createWorld(new WorldCreator(wName));
            }

            if (world != null) {
                double x = spawnDataConfig.getDouble("spawn.x", 0.5);
                double y = spawnDataConfig.getDouble("spawn.y", 65.0);
                double z = spawnDataConfig.getDouble("spawn.z", 0.5);
                float yaw = (float) spawnDataConfig.getDouble("spawn.yaw", 0.0);
                float pitch = (float) spawnDataConfig.getDouble("spawn.pitch", 0.0);
                this.spawnLocation = new Location(world, x, y, z, yaw, pitch);
                world.setSpawnLocation((int) x, (int) y, (int) z);
                plugin.getLogger().info("Spawn lokasyonu data/spawn.yml'den başarıyla yüklendi: " + wName + " (" + (int)x + ", " + (int)y + ", " + (int)z + ")");
                return;
            }
        }

        // 2. data/spawn.yml henüz yoksa config.yml'den oku
        FileConfiguration config = plugin.getConfig();
        if (config.getBoolean("spawn.location.set", false)) {
            String wName = config.getString("spawn.location.world", "spawn");
            World world = Bukkit.getWorld(wName);
            if (world == null && new File(Bukkit.getWorldContainer(), wName).exists()) {
                world = Bukkit.createWorld(new WorldCreator(wName));
            }

            if (world != null) {
                double x = config.getDouble("spawn.location.x", 0.5);
                double y = config.getDouble("spawn.location.y", 65.0);
                double z = config.getDouble("spawn.location.z", 0.5);
                float yaw = (float) config.getDouble("spawn.location.yaw", 0.0);
                float pitch = (float) config.getDouble("spawn.location.pitch", 0.0);
                this.spawnLocation = new Location(world, x, y, z, yaw, pitch);
                world.setSpawnLocation((int) x, (int) y, (int) z);
                plugin.getLogger().info("Spawn lokasyonu config.yml'den yüklendi: " + wName + " (" + (int)x + ", " + (int)y + ", " + (int)z + ")");

                // data/spawn.yml'ye de kalıcı olarak kaydet
                saveToDataFile(this.spawnLocation);
            }
        }
    }

    public void setSpawnLocation(Location loc) {
        this.spawnLocation = loc.clone();

        // 1. config.yml'ye kaydet
        FileConfiguration config = plugin.getConfig();
        config.set("spawn.location.set", true);
        config.set("spawn.location.world", loc.getWorld().getName());
        config.set("spawn.location.x", loc.getX());
        config.set("spawn.location.y", loc.getY());
        config.set("spawn.location.z", loc.getZ());
        config.set("spawn.location.yaw", loc.getYaw());
        config.set("spawn.location.pitch", loc.getPitch());
        plugin.saveConfig();

        // 2. data/spawn.yml dosyasına kalıcı kaydet
        saveToDataFile(loc);

        // 3. Minecraft Vanilla dünya doğuş noktasını da senkronize et
        if (loc.getWorld() != null) {
            loc.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }

        plugin.getLogger().info("Spawn lokasyonu kalıcı olarak kaydedildi ve dünya spawn noktası güncellendi.");

        // 4. Yeni konuma göre hologramı yenile
        spawnWelcomeHologram();
    }

    private void saveToDataFile(Location loc) {
        try {
            if (spawnDataFile == null || spawnDataConfig == null) {
                initSpawnData();
            }
            spawnDataConfig.set("spawn.set", true);
            spawnDataConfig.set("spawn.world", loc.getWorld().getName());
            spawnDataConfig.set("spawn.x", loc.getX());
            spawnDataConfig.set("spawn.y", loc.getY());
            spawnDataConfig.set("spawn.z", loc.getZ());
            spawnDataConfig.set("spawn.yaw", loc.getYaw());
            spawnDataConfig.set("spawn.pitch", loc.getPitch());
            spawnDataConfig.save(spawnDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("data/spawn.yml kaydedilemedi: " + e.getMessage());
        }
    }

    public Location getSpawnLocation() {
        if (spawnLocation == null) {
            loadSpawnLocation();
        }
        if (spawnLocation == null) {
            String wName = plugin.getConfig().getString("spawn.world", "spawn");
            World world = Bukkit.getWorld(wName);
            if (world != null) {
                return world.getSpawnLocation();
            }
        }
        return spawnLocation;
    }

    public boolean isSpawnSet() {
        return spawnLocation != null;
    }

    public boolean teleportToSpawn(Player player) {
        Location loc = getSpawnLocation();
        if (loc == null || loc.getWorld() == null) {
            player.sendMessage(ColorUtils.color("&cSpawn noktası henüz belirlenmemiş!"));
            if (player.hasPermission("tekblok.admin")) {
                player.sendMessage(ColorUtils.color("&eYönetici: &7İstediğiniz yere gidip &6/setspawn &7veya &6/spawn ayarla &7yazın."));
            }
            return false;
        }

        player.teleport(loc);
        player.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        player.sendTitle(ColorUtils.color("&6&lSPAWN"), ColorUtils.color("&eLobiye hoş geldiniz!"), 10, 30, 10);
        player.sendMessage(ColorUtils.color("&aLobi spawn noktasına ışınlandınız."));
        return true;
    }

    public boolean isSpawnWorld(World world) {
        if (world == null) return false;
        String spawnWorldName = plugin.getConfig().getString("spawn.world", "spawn");
        return world.getName().equalsIgnoreCase(spawnWorldName);
    }

    public void clearWelcomeHologram() {
        for (ArmorStand as : welcomeHolograms) {
            if (as != null && as.isValid()) {
                as.remove();
            }
        }
        welcomeHolograms.clear();

        // Spawn etrafındaki eski orphan zırh askılarını temizle (hem eski 4 blok hem yeni 10 blok konumunu, tüm yükseklikleri temizle)
        Location spawn = getSpawnLocation();
        if (spawn != null && spawn.getWorld() != null) {
            double rad = Math.toRadians(spawn.getYaw());
            for (double dist : new double[]{4.0, 10.0}) {
                double dx = -Math.sin(rad) * dist;
                double dz = Math.cos(rad) * dist;
                Location holoCenter = spawn.clone().add(dx, 1.2, dz);

                for (Entity e : spawn.getWorld().getNearbyEntities(holoCenter, 4.0, 6.0, 4.0)) {
                    if (e instanceof ArmorStand && ((ArmorStand) e).isMarker()) {
                        e.remove();
                    }
                }
            }
        }
    }

    public void spawnWelcomeHologram() {
        clearWelcomeHologram();

        Location spawn = getSpawnLocation();
        if (spawn == null || spawn.getWorld() == null) return;

        // Kullanıcı Talebi: Spawn alınan yerin 10 blok ilerisine, olduğu yerden 2 blok aşağıya (0.2 Y)
        // Önemli yazılar yeşil (&a / &2), diğerleri beyaz (&f)
        double rad = Math.toRadians(spawn.getYaw());
        double dx = -Math.sin(rad) * 10.0;
        double dz = Math.cos(rad) * 10.0;
        Location holoBase = spawn.clone().add(dx, 0.2, dz);

        List<String> lines = Arrays.asList(
                "&a&l★ TEKBLOK SKYBLOCK SUNUCUSU ★",
                "&fHoş Geldin! &aAdana gitmek &fiçin &a/ada &fyaz veya &aKaptan&f'a tıkla.",
                "&fTek bloğu kırarak &ayeni biyomların &fve aşamaların kilidini aç.",
                "&fMadenlerini &aDemirci Köylü&f'de kömür yakıtı ile külçeye dönüştür.",
                "&fEşyalarını hızlıca satıp para kazanmak için &a/sat &fsandığını kullan.",
                "&fOtomatik hasat ve spawner kesimi için &a/ciftci &fsatın almayı unutma!",
                "&fÖzel &aspawnerlar &fve efsanevi ödüller için &a/kasa &fanahtarlarını edin.",
                "&aKeyifli Oyunlar &fve Bol Şanslar Dileriz!"
        );

        double startY = holoBase.getY() + (lines.size() * 0.28);

        for (int i = 0; i < lines.size(); i++) {
            Location lineLoc = new Location(holoBase.getWorld(), holoBase.getX(), startY - (i * 0.28), holoBase.getZ());
            ArmorStand as = holoBase.getWorld().spawn(lineLoc, ArmorStand.class);
            as.setVisible(false);
            as.setGravity(false);
            as.setMarker(true);
            as.setInvulnerable(true);
            as.setCustomName(ColorUtils.color(lines.get(i)));
            as.setCustomNameVisible(true);
            welcomeHolograms.add(as);
        }

        plugin.getLogger().info("Spawn bilgilendirme hologramı başarıyla oluşturuldu.");
    }
}
