package dev.bekirr.tekblok.managers;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.generator.VoidChunkGenerator;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.Phase;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IslandManager {

    private final TekBlokPlugin plugin;
    private final Map<UUID, Island> islandsByOwner = new HashMap<>();
    private final Map<UUID, UUID> playerToIsland = new HashMap<>();
    private final Map<UUID, UUID> pendingInvites = new HashMap<>(); // targetPlayer -> islandOwner
    private File dataFile;
    private FileConfiguration dataConfig;
    private int nextIslandIndex = 0;

    private World oneBlockWorld;

    public IslandManager(TekBlokPlugin plugin) {
        this.plugin = plugin;
        initWorld();
        loadIslands();
    }

    public void initWorld() {
        String worldName = plugin.getConfig().getString("world.name", "TekBlokWorld");
        oneBlockWorld = Bukkit.getWorld(worldName);
        if (oneBlockWorld == null) {
            plugin.getLogger().info("'" + worldName + "' dünyası oluşturuluyor/yükleniyor (Void Jeneratörü ile)...");
            WorldCreator creator = new WorldCreator(worldName);
            creator.generator(new VoidChunkGenerator());
            creator.environment(World.Environment.NORMAL);
            oneBlockWorld = creator.createWorld();
        }
        if (oneBlockWorld != null) {
            oneBlockWorld.setGameRule(GameRule.DO_MOB_SPAWNING, plugin.getConfig().getBoolean("world.gamerules.doMobSpawning", true));
            oneBlockWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, plugin.getConfig().getBoolean("world.gamerules.doDaylightCycle", true));
            oneBlockWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, plugin.getConfig().getBoolean("world.gamerules.doWeatherCycle", false));
            oneBlockWorld.setGameRule(GameRule.KEEP_INVENTORY, false);
            oneBlockWorld.setSpawnLocation(0, 66, 0);
        }
    }

    public World getOneBlockWorld() {
        if (oneBlockWorld == null) {
            initWorld();
        }
        return oneBlockWorld;
    }

    public Island getIslandByPlayer(UUID uuid) {
        if (uuid == null) return null;
        UUID ownerId = playerToIsland.get(uuid);
        if (ownerId != null && islandsByOwner.containsKey(ownerId)) {
            return islandsByOwner.get(ownerId);
        }
        Island direct = islandsByOwner.get(uuid);
        if (direct != null) {
            playerToIsland.put(uuid, uuid);
            return direct;
        }
        for (Island is : islandsByOwner.values()) {
            if (is.getMembers().contains(uuid)) {
                playerToIsland.put(uuid, is.getOwnerUUID());
                return is;
            }
        }
        return null;
    }

    public void teleportToIsland(Player player) {
        if (player == null) return;
        Island island = getIslandByPlayer(player.getUniqueId());
        if (island == null) {
            island = createIsland(player);
            player.sendMessage(plugin.getConfigManager().getMessage("messages.island-created"));
        } else {
            ensureOneBlockExists(island);
        }
        player.teleport(island.getSpawnLocation());
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.sendMessage(plugin.getConfigManager().getMessage("messages.teleported-to-island"));
    }

    public Island getIslandByLocation(Location location) {
        if (location == null || location.getWorld() == null) return null;
        for (Island island : islandsByOwner.values()) {
            if (island.isOneBlock(location)) {
                return island;
            }
        }
        return null;
    }

    public Island getIslandByArea(Location location) {
        if (location == null || location.getWorld() == null) return null;
        if (oneBlockWorld == null || !location.getWorld().getName().equals(oneBlockWorld.getName())) return null;

        int islandDistance = plugin.getConfig().getInt("island.distance-between", 500);
        int halfDist = islandDistance / 2;

        for (Island island : islandsByOwner.values()) {
            Location center = island.getOneBlockLocation();
            if (center != null &&
                Math.abs(location.getBlockX() - center.getBlockX()) <= halfDist &&
                Math.abs(location.getBlockZ() - center.getBlockZ()) <= halfDist) {
                return island;
            }
        }
        return null;
    }

    public Island createIsland(Player player) {
        UUID uuid = player.getUniqueId();
        if (islandsByOwner.containsKey(uuid)) {
            return islandsByOwner.get(uuid);
        }

        World world = getOneBlockWorld();
        int islandDistance = plugin.getConfig().getInt("island.distance-between", 500);
        int index = nextIslandIndex++;

        // Grid hesabı
        int x, z;
        if (index == 0) {
            x = 0;
            z = 0;
        } else {
            int ring = (int) Math.ceil((Math.sqrt(index + 1) - 1) / 2);
            int sideLength = 2 * ring;
            int offset = index - (int) Math.pow(2 * ring - 1, 2);
            int side = offset / sideLength;
            int posInSide = offset % sideLength;

            switch (side) {
                case 0:
                    x = (ring - posInSide) * islandDistance;
                    z = ring * islandDistance;
                    break;
                case 1:
                    x = -ring * islandDistance;
                    z = (ring - posInSide) * islandDistance;
                    break;
                case 2:
                    x = (-ring + posInSide) * islandDistance;
                    z = -ring * islandDistance;
                    break;
                default:
                    x = ring * islandDistance;
                    z = (-ring + posInSide) * islandDistance;
                    break;
            }
        }

        int y = plugin.getConfig().getInt("island.height", 65);
        Location oneBlockLoc = new Location(world, x, y, z);
        Location spawnLoc = new Location(world, x + 0.5, y + 1.0, z + 0.5, 0f, 0f);

        // Bloğu ve taban katman kayasını oluştur
        boolean bedrockUnder = plugin.getConfig().getBoolean("island.place-bedrock-underneath", true);
        if (bedrockUnder) {
            oneBlockLoc.clone().add(0, -1, 0).getBlock().setType(Material.BEDROCK, false);
        }
        oneBlockLoc.getBlock().setType(Material.GRASS_BLOCK, false);

        Island island = new Island(uuid, player.getName(), oneBlockLoc, spawnLoc, 0, 1, new HashSet<>(), System.currentTimeMillis());
        islandsByOwner.put(uuid, island);
        playerToIsland.put(uuid, uuid);

        ensureOneBlockExists(island);
        if (plugin.getPhaseManager() != null) {
            updateIslandBiome(island, plugin.getPhaseManager().getPhase(1).getBiome());
        }
        saveIslands();
        return island;
    }

    public void ensureOneBlockExists(Island island) {
        if (island == null) return;
        Location loc = island.getOneBlockLocation();
        if (loc == null || loc.getWorld() == null) return;

        // 1. Taban katman kayası kontrolü (Bedrock at Y - 1)
        boolean bedrockUnder = plugin.getConfig().getBoolean("island.place-bedrock-underneath", true);
        if (bedrockUnder) {
            Location bedrockLoc = loc.clone().add(0, -1, 0);
            if (bedrockLoc.getBlock().getType() != Material.BEDROCK) {
                bedrockLoc.getBlock().setType(Material.BEDROCK, false);
            }
        }

        // 2. Tek blok kontrolü (Hava ise hemen aşamaya uygun blok oluştur)
        Block block = loc.getBlock();
        if (block.getType().isAir()) {
            Phase phase = plugin.getPhaseManager().getPhase(island.getCurrentPhaseId());
            Material mat = (phase != null) ? phase.getRandomBlock(new Random()) : Material.GRASS_BLOCK;
            block.setType(mat != null ? mat : Material.GRASS_BLOCK, false);
        }

        // 3. Aşama 14+ ise ve End Portalı henüz kurulmamışsa otomatik inşa et
        if (island.getCurrentPhaseId() >= 6 && !island.isEndPortalBuilt()) {
            plugin.getPhaseManager().buildEndPortal(island);
        }
    }

    public void checkAllIslandsOneBlock() {
        for (Island island : islandsByOwner.values()) {
            Player owner = Bukkit.getPlayer(island.getOwnerUUID());
            boolean anyOnline = (owner != null && owner.isOnline());
            if (!anyOnline) {
                for (UUID m : island.getMembers()) {
                    Player mp = Bukkit.getPlayer(m);
                    if (mp != null && mp.isOnline()) {
                        anyOnline = true;
                        break;
                    }
                }
            }
            if (anyOnline) {
                ensureOneBlockExists(island);
            }
        }
    }

    public void updateIslandBiome(Island island, Biome biome) {
        if (island == null || biome == null) return;
        Location loc = island.getOneBlockLocation();
        if (loc == null || loc.getWorld() == null) return;
        World world = loc.getWorld();

        int cx = loc.getBlockX();
        int cz = loc.getBlockZ();
        int radius = 24;

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                for (int y = 0; y <= 128; y += 8) {
                    world.setBiome(x, y, z, biome);
                }
            }
        }
    }

    public boolean deleteIsland(UUID ownerUUID) {
        Island island = islandsByOwner.remove(ownerUUID);
        if (island == null) {
            return false;
        }

        World mainWorld = Bukkit.getWorlds().get(0);
        Location mainSpawn = mainWorld != null ? mainWorld.getSpawnLocation() : island.getSpawnLocation();

        // 1. Üyeleri temizle ve lobiye ışınla
        playerToIsland.remove(ownerUUID);
        for (UUID memberId : island.getMembers()) {
            playerToIsland.remove(memberId);
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.teleport(mainSpawn);
                member.sendMessage(plugin.getConfigManager().getMessage("messages.island-deleted-member"));
            }
        }

        // 2. Sahibi lobiye ışınla
        Player owner = Bukkit.getPlayer(ownerUUID);
        if (owner != null && owner.isOnline()) {
            owner.teleport(mainSpawn);
            owner.sendMessage(plugin.getConfigManager().getMessage("messages.island-deleted-owner"));
        }

        // 3. BossBar'ları kaldır
        if (plugin.getDisplayManager() != null) {
            plugin.getDisplayManager().removePlayer(ownerUUID);
            for (UUID memberId : island.getMembers()) {
                plugin.getDisplayManager().removePlayer(memberId);
            }
        }

        // 4. Ada alanındaki tüm blokları, sandıkları ve canlıları temizle
        Location loc = island.getOneBlockLocation();
        if (loc != null && loc.getWorld() != null) {
            World world = loc.getWorld();
            int minX = loc.getBlockX() - 60;
            int maxX = loc.getBlockX() + 60;
            int minZ = loc.getBlockZ() - 60;
            int maxZ = loc.getBlockZ() + 60;
            int minY = Math.max(1, loc.getBlockY() - 35);
            int maxY = Math.min(255, loc.getBlockY() + 45);

            for (int cx = (minX >> 4); cx <= (maxX >> 4); cx++) {
                for (int cz = (minZ >> 4); cz <= (maxZ >> 4); cz++) {
                    if (world.isChunkLoaded(cx, cz) || world.loadChunk(cx, cz, false)) {
                        Chunk chunk = world.getChunkAt(cx, cz);
                        for (org.bukkit.entity.Entity e : chunk.getEntities()) {
                            if (!(e instanceof Player)) {
                                e.remove();
                            }
                        }
                    }
                }
            }

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        Block b = world.getBlockAt(x, y, z);
                        if (!b.getType().isAir()) {
                            b.setType(Material.AIR, false);
                        }
                    }
                }
            }
        }

        // 5. Çiftçi verisini temizle
        if (plugin.getFarmerManager() != null) {
            plugin.getFarmerManager().deleteFarmerData(ownerUUID);
        }

        // 6. Dosyaya kaydet
        saveIslands();
        return true;
    }

    public void resetIsland(Island island) {
        ensureOneBlockExists(island);
        updateIslandBiome(island, plugin.getPhaseManager().getPhase(1).getBiome());
        Location blockLoc = island.getOneBlockLocation();
        blockLoc.getBlock().setType(Material.GRASS_BLOCK, false);
        island.setBlocksBroken(0);
        island.setCurrentPhaseId(1);
        saveIslands();

        // Üyeleri ve sahibi ışınla
        for (UUID memberId : island.getMembers()) {
            Player p = Bukkit.getPlayer(memberId);
            if (p != null && p.isOnline()) {
                p.teleport(island.getSpawnLocation());
            }
        }
        Player owner = Bukkit.getPlayer(island.getOwnerUUID());
        if (owner != null && owner.isOnline()) {
            owner.teleport(island.getSpawnLocation());
        }
    }

    public void invitePlayer(Player owner, Player target) {
        pendingInvites.put(target.getUniqueId(), owner.getUniqueId());
    }

    public boolean hasPendingInvite(UUID targetUuid) {
        return pendingInvites.containsKey(targetUuid);
    }

    public UUID getPendingInvite(UUID targetUuid) {
        return pendingInvites.get(targetUuid);
    }

    public void removePendingInvite(UUID targetUuid) {
        pendingInvites.remove(targetUuid);
    }

    public void addMember(Island island, UUID memberUuid) {
        if (island == null || memberUuid == null) return;
        if (islandsByOwner.containsKey(memberUuid)) {
            deleteIsland(memberUuid);
        }
        UUID prevOwner = playerToIsland.get(memberUuid);
        if (prevOwner != null && !prevOwner.equals(island.getOwnerUUID())) {
            Island prevIsland = islandsByOwner.get(prevOwner);
            if (prevIsland != null) {
                prevIsland.removeMember(memberUuid);
            }
        }
        island.addMember(memberUuid);
        playerToIsland.put(memberUuid, island.getOwnerUUID());
        saveIslands();
    }

    public boolean removeMember(Island island, UUID memberUuid) {
        if (island == null || memberUuid == null) return false;
        boolean removed = island.removeMember(memberUuid);
        if (removed) {
            playerToIsland.remove(memberUuid);
            saveIslands();
        }
        return removed;
    }

    public void loadIslands() {
        islandsByOwner.clear();
        playerToIsland.clear();

        dataFile = new File(plugin.getDataFolder(), "data/islands.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("islands.yml dosyası oluşturulamadı: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection section = dataConfig.getConfigurationSection("islands");
        if (section == null) return;

        World world = getOneBlockWorld();

        for (String key : section.getKeys(false)) {
            try {
                UUID ownerUUID = UUID.fromString(key);
                ConfigurationSection isSec = section.getConfigurationSection(key);
                if (isSec == null) continue;

                String ownerName = isSec.getString("owner-name", "Bilinmeyen");
                String wName = isSec.getString("world", world != null ? world.getName() : "TekBlokWorld");
                World w = Bukkit.getWorld(wName);
                if (w == null) w = world;

                int x = isSec.getInt("block.x");
                int y = isSec.getInt("block.y", 65);
                int z = isSec.getInt("block.z");
                Location oneBlockLoc = new Location(w, x, y, z);

                double sx = isSec.getDouble("spawn.x", x + 0.5);
                double sy = isSec.getDouble("spawn.y", y + 1.0);
                double sz = isSec.getDouble("spawn.z", z + 0.5);
                float yaw = (float) isSec.getDouble("spawn.yaw", 0.0);
                float pitch = (float) isSec.getDouble("spawn.pitch", 0.0);
                Location spawnLoc = new Location(w, sx, sy, sz, yaw, pitch);

                int blocksBroken = isSec.getInt("blocks-broken", 0);
                int currentPhaseId = isSec.getInt("current-phase", 1);
                long createdAt = isSec.getLong("created-at", System.currentTimeMillis());

                Set<UUID> members = new HashSet<>();
                List<String> memberList = isSec.getStringList("members");
                for (String mStr : memberList) {
                    try {
                        UUID mUuid = UUID.fromString(mStr);
                        members.add(mUuid);
                        playerToIsland.put(mUuid, ownerUUID);
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                Island island = new Island(ownerUUID, ownerName, oneBlockLoc, spawnLoc, blocksBroken, currentPhaseId, members, createdAt);
                island.setEndPortalBuilt(isSec.getBoolean("end-portal-built", false));
                islandsByOwner.put(ownerUUID, island);
                playerToIsland.put(ownerUUID, ownerUUID);
                ensureOneBlockExists(island);

            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Ada UUID çözümlenemedi: " + key);
            }
        }
        nextIslandIndex = dataConfig.getInt("next-island-index", Math.max(islandsByOwner.size(), 0));
        plugin.getLogger().info("Toplam " + islandsByOwner.size() + " adet ada başarıyla yüklendi. (Sıradaki Ada No: " + nextIslandIndex + ")");
    }

    public void saveIslands() {
        if (dataConfig == null || dataFile == null) return;
        dataConfig.set("next-island-index", nextIslandIndex);
        dataConfig.set("islands", null); // sıfırla ve yeniden yaz

        for (Island island : islandsByOwner.values()) {
            String path = "islands." + island.getOwnerUUID().toString();
            dataConfig.set(path + ".owner-name", island.getOwnerName());
            dataConfig.set(path + ".world", island.getOneBlockLocation().getWorld().getName());

            dataConfig.set(path + ".block.x", island.getOneBlockLocation().getBlockX());
            dataConfig.set(path + ".block.y", island.getOneBlockLocation().getBlockY());
            dataConfig.set(path + ".block.z", island.getOneBlockLocation().getBlockZ());

            dataConfig.set(path + ".spawn.x", island.getSpawnLocation().getX());
            dataConfig.set(path + ".spawn.y", island.getSpawnLocation().getY());
            dataConfig.set(path + ".spawn.z", island.getSpawnLocation().getZ());
            dataConfig.set(path + ".spawn.yaw", island.getSpawnLocation().getYaw());
            dataConfig.set(path + ".spawn.pitch", island.getSpawnLocation().getPitch());

            dataConfig.set(path + ".blocks-broken", island.getBlocksBroken());
            dataConfig.set(path + ".current-phase", island.getCurrentPhaseId());
            dataConfig.set(path + ".created-at", island.getCreatedAt());
            dataConfig.set(path + ".end-portal-built", island.isEndPortalBuilt());

            List<String> memberList = new ArrayList<>();
            for (UUID mUuid : island.getMembers()) {
                memberList.add(mUuid.toString());
            }
            dataConfig.set(path + ".members", memberList);
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("islands.yml kaydedilemedi: " + e.getMessage());
        }
    }

    public Map<UUID, Island> getIslandsByOwner() {
        return islandsByOwner;
    }
}
