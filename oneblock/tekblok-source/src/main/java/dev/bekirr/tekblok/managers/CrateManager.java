package dev.bekirr.tekblok.managers;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.gui.CrateOpeningGUI;
import dev.bekirr.tekblok.models.Crate;
import dev.bekirr.tekblok.models.CrateLocation;
import dev.bekirr.tekblok.models.CrateReward;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CrateManager {

    private final TekBlokPlugin plugin;
    private final Map<String, Crate> crates = new HashMap<>();
    private final Map<String, CrateLocation> crateLocations = new HashMap<>();
    private File configFile;
    private FileConfiguration config;
    private BukkitTask particleTask;

    public CrateManager(TekBlokPlugin plugin) {
        this.plugin = plugin;
        loadCrates();
        startParticleTask();
        // Dünyalar yüklendikten 1 saniye sonra hologramları oluştur
        Bukkit.getScheduler().runTaskLater(plugin, this::spawnAllHolograms, 25L);
    }

    public void loadCrates() {
        crates.clear();
        configFile = new File(plugin.getDataFolder(), "crates.yml");
        if (!configFile.exists()) {
            plugin.saveResource("crates.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // 1. Kasa Tanımlarını Yükle
        ConfigurationSection crateSec = config.getConfigurationSection("crates");
        if (crateSec != null) {
            for (String key : crateSec.getKeys(false)) {
                ConfigurationSection cData = crateSec.getConfigurationSection(key);
                if (cData == null) continue;

                String dName = cData.getString("display-name", key);
                String kName = cData.getString("key-name", key + " Anahtarı");
                String kMatStr = cData.getString("key-material", "TRIPWIRE_HOOK");
                Material kMat = Material.matchMaterial(kMatStr);
                if (kMat == null) kMat = Material.TRIPWIRE_HOOK;

                List<String> kLore = cData.getStringList("key-lore");
                String partStr = cData.getString("particle", "FLAME");
                Particle part;
                try {
                    part = Particle.valueOf(partStr.toUpperCase());
                } catch (Exception e) {
                    part = Particle.FLAME;
                }
                int partCount = cData.getInt("particle-count", 8);
                List<String> holoLines = cData.getStringList("hologram");

                List<CrateReward> rewards = new ArrayList<>();
                List<Map<?, ?>> rewList = cData.getMapList("rewards");
                for (Map<?, ?> rMap : rewList) {
                    String type = (String) rMap.get("type");
                    String matStr = (String) rMap.get("material");
                    Material mat = matStr != null ? Material.matchMaterial(matStr) : Material.DIAMOND;
                    int amount = rMap.get("amount") != null ? ((Number) rMap.get("amount")).intValue() : 1;
                    String rName = (String) rMap.get("name");
                    String enchant = (String) rMap.get("enchant");
                    int chance = rMap.get("chance") != null ? ((Number) rMap.get("chance")).intValue() : 10;
                    String mob = (String) rMap.get("mob");
                    rewards.add(new CrateReward(type, mat, amount, rName, enchant, chance, mob));
                }

                crates.put(key.toLowerCase(), new Crate(key, dName, kName, kMat, kLore, part, partCount, holoLines, rewards));
            }
        }

        // 2. Kurulu Kasa Konumlarını Yükle
        crateLocations.clear();
        ConfigurationSection locSec = config.getConfigurationSection("locations");
        if (locSec != null) {
            for (String locId : locSec.getKeys(false)) {
                String cType = locSec.getString(locId + ".crate", "cirak");
                String wName = locSec.getString(locId + ".world", "spawn");
                World world = Bukkit.getWorld(wName);
                if (world != null) {
                    int x = locSec.getInt(locId + ".x");
                    int y = locSec.getInt(locId + ".y");
                    int z = locSec.getInt(locId + ".z");
                    Location loc = new Location(world, x, y, z);
                    crateLocations.put(locId, new CrateLocation(locId, cType, loc));
                }
            }
        }

        plugin.getLogger().info("Kasa sistemi yüklendi: " + crates.size() + " kasa türü, " + crateLocations.size() + " aktif kasa.");
    }

    public void spawnAllHolograms() {
        for (CrateLocation cl : crateLocations.values()) {
            Crate c = crates.get(cl.getCrateType());
            if (c != null) {
                cl.spawnHolograms(c);
            }
        }
    }

    public void removeAllHolograms() {
        for (CrateLocation cl : crateLocations.values()) {
            cl.clearHolograms();
        }
    }

    private void startParticleTask() {
        if (particleTask != null) {
            particleTask.cancel();
        }

        particleTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            double angle = 0;
            int ticks = 0;

            @Override
            public void run() {
                angle += 0.20;
                if (angle >= 2 * Math.PI) {
                    angle = 0;
                }
                ticks++;

                for (CrateLocation cl : crateLocations.values()) {
                    Location loc = cl.getLocation();
                    if (loc.getWorld() == null) continue;

                    Crate crate = crates.get(cl.getCrateType());
                    if (crate == null) continue;

                    World w = loc.getWorld();
                    double centerX = loc.getBlockX() + 0.5;
                    double centerY = loc.getY();
                    double centerZ = loc.getBlockZ() + 0.5;

                    // 1. Çift Yükselen Spiral Hortumu (Double Helix)
                    for (int step = 0; step < 3; step++) {
                        double hOffset = (ticks * 0.08 + step * 0.6) % 2.2;
                        double curAngle = angle + (hOffset * 2.5);
                        double r = 0.85;

                        double x1 = centerX + Math.cos(curAngle) * r;
                        double z1 = centerZ + Math.sin(curAngle) * r;
                        double y1 = centerY + hOffset;
                        w.spawnParticle(crate.getParticle(), x1, y1, z1, 1, 0, 0, 0, 0);

                        // Karşıt spiral kolu
                        double x2 = centerX + Math.cos(curAngle + Math.PI) * r;
                        double z2 = centerZ + Math.sin(curAngle + Math.PI) * r;
                        w.spawnParticle(crate.getParticle(), x2, y1, z2, 1, 0, 0, 0, 0);
                    }

                    // 2. Kasanın Gövdesi Etrafında Dönen Yatay Işık Halkası (Halo Ring)
                    double ringRadius = 1.1;
                    for (int i = 0; i < 4; i++) {
                        double rAngle = -angle + (i * Math.PI / 2.0);
                        double rx = centerX + Math.cos(rAngle) * ringRadius;
                        double rz = centerZ + Math.sin(rAngle) * ringRadius;
                        double ry = centerY + 0.5 + Math.sin(angle * 2.0) * 0.15;
                        w.spawnParticle(Particle.ENCHANTMENT_TABLE, rx, ry, rz, 1, 0, 0, 0, 0.5);
                    }

                    // 3. Havada süzülme taban aurası (Alttan yükselen pırıltılar)
                    w.spawnParticle(Particle.PORTAL, centerX, centerY - 0.2, centerZ, 2, 0.2, 0.1, 0.2, 0.02);

                    // 4. Periyodik Büyü Patlaması (Her 15 tickte bir abartılı patlama)
                    if (ticks % 15 == 0) {
                        w.spawnParticle(Particle.SPELL_INSTANT, centerX, centerY + 0.6, centerZ, 8, 0.3, 0.3, 0.3, 0.05);
                        w.spawnParticle(crate.getParticle(), centerX, centerY + 0.6, centerZ, 12, 0.4, 0.4, 0.4, 0.08);
                    }
                }
            }
        }, 20L, 2L);
    }

    public Crate getCrate(String id) {
        return crates.get(id.toLowerCase());
    }

    public Collection<Crate> getCrates() {
        return crates.values();
    }

    public CrateLocation findCrateAt(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        for (CrateLocation cl : crateLocations.values()) {
            Location cLoc = cl.getLocation();
            if (cLoc.getWorld() != null && cLoc.getWorld().equals(loc.getWorld())
                    && cLoc.getBlockX() == loc.getBlockX()
                    && cLoc.getBlockY() == loc.getBlockY()
                    && cLoc.getBlockZ() == loc.getBlockZ()) {
                return cl;
            }
        }
        return null;
    }

    public CrateLocation createCrate(Player player, String crateType) {
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            // Hedef blok yoksa oyuncunun baktığı yerin 1 blok üstüne havada kur
            targetBlock = player.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.UP);
        } else {
            // Hedef bloğun üstünde havada süzülecek şekilde kur
            targetBlock = targetBlock.getRelative(org.bukkit.block.BlockFace.UP);
        }

        // Bloğu Ender Sandığı yap (havada süzülen gizemli kasa)
        targetBlock.setType(Material.ENDER_CHEST);

        Location bLoc = targetBlock.getLocation();
        String id = "crate_" + bLoc.getWorld().getName() + "_" + bLoc.getBlockX() + "_" + bLoc.getBlockY() + "_" + bLoc.getBlockZ();

        CrateLocation cl = new CrateLocation(id, crateType, bLoc);
        crateLocations.put(id, cl);

        Crate crate = getCrate(crateType);
        if (crate != null) {
            cl.spawnHolograms(crate);
        }

        // Yapılandırmaya kaydet
        config.set("locations." + id + ".crate", crateType);
        config.set("locations." + id + ".world", bLoc.getWorld().getName());
        config.set("locations." + id + ".x", bLoc.getBlockX());
        config.set("locations." + id + ".y", bLoc.getBlockY());
        config.set("locations." + id + ".z", bLoc.getBlockZ());

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("crates.yml kaydedilemedi: " + e.getMessage());
        }

        return cl;
    }

    public boolean removeCrate(Player player) {
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null) return false;

        CrateLocation cl = findCrateAt(targetBlock.getLocation());
        if (cl != null) {
            cl.clearHolograms();
            crateLocations.remove(cl.getId());
            config.set("locations." + cl.getId(), null);

            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().severe("crates.yml kaydedilemedi: " + e.getMessage());
            }

            targetBlock.setType(Material.AIR);
            return true;
        }

        return false;
    }

    public boolean giveKey(Player player, String crateType, int amount) {
        Crate c = getCrate(crateType);
        if (c == null) return false;

        ItemStack key = c.createKey(plugin, amount);
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(key);
        if (!leftover.isEmpty()) {
            for (ItemStack is : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), is);
            }
        }
        return true;
    }

    public void startOpening(Player player, Crate crate, Location crateLoc) {
        CrateOpeningGUI.start(player, plugin, crate, crateLoc);
    }

    public Map<String, CrateLocation> getCrateLocations() {
        return crateLocations;
    }
}
