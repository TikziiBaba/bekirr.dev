package dev.bekirr.tekblok.managers;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.*;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.entity.EntityType;

import java.util.*;

public class PhaseManager {

    private final TekBlokPlugin plugin;
    private final Map<Integer, Phase> phases = new TreeMap<>();
    private final Random random = new Random();

    public PhaseManager(TekBlokPlugin plugin) {
        this.plugin = plugin;
        loadPhases();
    }

    public static boolean isOre(Material mat) {
        if (mat == null) return false;
        switch (mat) {
            case COAL_ORE:
            case IRON_ORE:
            case GOLD_ORE:
            case REDSTONE_ORE:
            case LAPIS_ORE:
            case DIAMOND_ORE:
            case EMERALD_ORE:
            case NETHER_QUARTZ_ORE:
            case NETHER_GOLD_ORE:
            case ANCIENT_DEBRIS:
                return true;
            default:
                return false;
        }
    }

    public void loadPhases() {
        phases.clear();
        FileConfiguration config = plugin.getConfigManager().getPhasesConfig();
        ConfigurationSection section = config.getConfigurationSection("phases");

        if (section == null) {
            plugin.getLogger().warning("phases.yml dosyasında 'phases' bölümü bulunamadı!");
            return;
        }

        for (String key : section.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                ConfigurationSection phaseSec = section.getConfigurationSection(key);
                if (phaseSec == null) continue;

                String name = ColorUtils.color(phaseSec.getString("name", "Aşama " + id));
                int requiredBlocks = phaseSec.getInt("required-blocks", id * 500);

                String colorStr = phaseSec.getString("bar-color", "GREEN").toUpperCase();
                BarColor barColor;
                try {
                    barColor = BarColor.valueOf(colorStr);
                } catch (IllegalArgumentException e) {
                    barColor = BarColor.GREEN;
                }

                String styleStr = phaseSec.getString("bar-style", "SOLID").toUpperCase();
                BarStyle barStyle;
                try {
                    barStyle = BarStyle.valueOf(styleStr);
                } catch (IllegalArgumentException e) {
                    barStyle = BarStyle.SOLID;
                }

                String iconStr = phaseSec.getString("icon", "GRASS_BLOCK").toUpperCase();
                Material icon = Material.matchMaterial(iconStr);
                if (icon == null) icon = Material.GRASS_BLOCK;

                double chestChance = phaseSec.getDouble("chest-chance", 0.02);
                double mobChance = phaseSec.getDouble("mob-chance", 0.03);

                String biomeStr = phaseSec.getString("biome", "PLAINS").toUpperCase();
                Biome biome;
                try {
                    biome = Biome.valueOf(biomeStr);
                } catch (IllegalArgumentException e) {
                    biome = Biome.PLAINS;
                }

                Phase phase = new Phase(id, name, requiredBlocks, barColor, barStyle, icon, chestChance, mobChance, biome);

                // Load blocks
                double oreMultiplier = plugin.getConfig().getDouble("island.ore-chance-multiplier", 1.6);
                List<Map<?, ?>> blockList = phaseSec.getMapList("blocks");
                Set<Material> presentMats = new HashSet<>();
                for (Map<?, ?> map : blockList) {
                    Object matObj = map.get("material");
                    Object weightObj = map.get("weight");
                    if (matObj != null && weightObj != null) {
                        Material mat = Material.matchMaterial(matObj.toString());
                        int weight = Integer.parseInt(weightObj.toString());
                        if (mat != null) {
                            if (isOre(mat)) {
                                weight = (int) Math.round(weight * oreMultiplier);
                            }
                            phase.addBlock(new PhaseBlock(mat, Math.max(1, weight)));
                            presentMats.add(mat);
                        }
                    }
                }

                // Her aşamada her madenin mutlaka çıkmasını sağla
                Material[] allOres = {
                        Material.COAL_ORE, Material.IRON_ORE, Material.LAPIS_ORE,
                        Material.REDSTONE_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE,
                        Material.EMERALD_ORE, Material.NETHER_QUARTZ_ORE,
                        Material.NETHER_GOLD_ORE, Material.ANCIENT_DEBRIS
                };
                int[] baseOreWeights = {18, 14, 8, 8, 7, 4, 3, 6, 5, 2};
                for (int i = 0; i < allOres.length; i++) {
                    Material oreMat = allOres[i];
                    if (!presentMats.contains(oreMat)) {
                        int w = (int) Math.round(baseOreWeights[i] * oreMultiplier);
                        phase.addBlock(new PhaseBlock(oreMat, Math.max(1, w)));
                    }
                }

                // Load chests
                List<Map<?, ?>> chestList = phaseSec.getMapList("chests");
                for (Map<?, ?> map : chestList) {
                    Object weightObj = map.get("weight");
                    int weight = weightObj != null ? Integer.parseInt(weightObj.toString()) : 10;
                    List<ItemStack> items = new ArrayList<>();
                    Object itemsObj = map.get("items");
                    if (itemsObj instanceof List<?>) {
                        for (Object itemObj : (List<?>) itemsObj) {
                            if (itemObj instanceof Map<?, ?>) {
                                Map<?, ?> itemMap = (Map<?, ?>) itemObj;
                                Object mObj = itemMap.get("material");
                                Object aObj = itemMap.get("amount");
                                if (mObj != null) {
                                    Material m = Material.matchMaterial(mObj.toString());
                                    int amt = aObj != null ? Integer.parseInt(aObj.toString()) : 1;
                                    if (m != null) {
                                        items.add(new ItemStack(m, amt));
                                    }
                                }
                            }
                        }
                    }
                    if (!items.isEmpty()) {
                        phase.addChest(new PhaseChest(weight, items));
                    }
                }

                // Load mobs
                List<Map<?, ?>> mobList = phaseSec.getMapList("mobs");
                for (Map<?, ?> map : mobList) {
                    Object typeObj = map.get("type");
                    Object weightObj = map.get("weight");
                    Object nameObj = map.get("name");
                    Object babyObj = map.get("baby");

                    if (typeObj != null) {
                        try {
                            EntityType type = EntityType.valueOf(typeObj.toString().toUpperCase());
                            int weight = weightObj != null ? Integer.parseInt(weightObj.toString()) : 10;
                            String mobName = nameObj != null ? ColorUtils.color(nameObj.toString()) : null;
                            boolean baby = babyObj != null && Boolean.parseBoolean(babyObj.toString());
                            phase.addMob(new PhaseMob(type, weight, mobName, baby));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }

                phases.put(id, phase);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Geçersiz aşama numarası: " + key);
            }
        }
        plugin.getLogger().info("Toplam " + phases.size() + " adet aşama başarıyla yüklendi!");
    }

    public Phase getPhase(int id) {
        if (phases.isEmpty()) {
            return new Phase(1, "&aBaşlangıç", 200, BarColor.GREEN, BarStyle.SOLID, Material.GRASS_BLOCK, 0.02, 0.03);
        }
        if (phases.containsKey(id)) {
            return phases.get(id);
        }
        int max = getMaxPhaseId();
        if (id > max) {
            return phases.get(max);
        }
        return phases.values().iterator().next();
    }

    public int getMaxPhaseId() {
        if (phases.isEmpty()) return 1;
        return Collections.max(phases.keySet());
    }

    public void checkPhaseProgress(Island island, Player player) {
        Phase currentPhase = getPhase(island.getCurrentPhaseId());
        int maxPhase = getMaxPhaseId();

        if (island.getCurrentPhaseId() < maxPhase && island.getBlocksBroken() >= currentPhase.getRequiredBlocks()) {
            int nextId = island.getCurrentPhaseId() + 1;
            island.setCurrentPhaseId(nextId);
            Phase newPhase = getPhase(nextId);

            // Kutlama efektleri
            Location loc = island.getOneBlockLocation().clone().add(0.5, 1.2, 0.5);
            World world = loc.getWorld();
            if (world != null) {
                // Havai fişek
                try {
                    Firework firework = world.spawn(loc, Firework.class);
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.addEffect(FireworkEffect.builder()
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .withColor(Color.ORANGE, Color.YELLOW, Color.LIME)
                            .withFade(Color.WHITE)
                            .flicker(true)
                            .trail(true)
                            .build());
                    meta.setPower(1);
                    firework.setFireworkMeta(meta);
                } catch (Exception ignored) {
                }

                world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            }

            if (player != null && player.isOnline()) {
                String title = plugin.getConfigManager().getMessageNoPrefix("titles.phase-up-title");
                String subtitle = plugin.getConfigManager().getMessageNoPrefix("titles.phase-up-subtitle")
                        .replace("{phase_name}", newPhase.getName())
                        .replace("{phase_id}", String.valueOf(nextId));

                player.sendTitle(title, subtitle, 10, 70, 20);
                player.sendMessage(plugin.getConfigManager().getMessage("messages.phase-up")
                        .replace("{phase_name}", newPhase.getName())
                        .replace("{phase_id}", String.valueOf(nextId)));
            }

            // End Biyomuna (Aşama 14) gelindiğinde tekbloğun 4 blok altına End Portalı inşa et
            if (nextId >= 14 && !island.isEndPortalBuilt()) {
                buildEndPortal(island);
                if (player != null && player.isOnline()) {
                    player.sendMessage(ColorUtils.color("&5&l[END PORTALI] &dAdanızın 4 blok altına gözleri takılı aktif End Portalı kuruldu!"));
                    player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.0f);
                }
            }

            // Biyom güncelle
            plugin.getIslandManager().updateIslandBiome(island, newPhase.getBiome());

            // BossBar güncelle
            plugin.getDisplayManager().updateBossBar(island);
        }
    }

    public void buildEndPortal(Island island) {
        if (island == null) return;
        Location obLoc = island.getOneBlockLocation();
        if (obLoc == null || obLoc.getWorld() == null) return;
        World world = obLoc.getWorld();
        int cx = obLoc.getBlockX();
        int cy = obLoc.getBlockY() - 4; // Tekbloğun 4 blok altına
        int cz = obLoc.getBlockZ();

        // 1. Taban: 5x5 End Taş Tuğlası platformu (cy - 1)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.getBlockAt(cx + dx, cy - 1, cz + dz).setType(Material.END_STONE_BRICKS);
            }
        }

        // 2. Köşeler: End Taş Tuğlası
        world.getBlockAt(cx - 2, cy, cz - 2).setType(Material.END_STONE_BRICKS);
        world.getBlockAt(cx + 2, cy, cz - 2).setType(Material.END_STONE_BRICKS);
        world.getBlockAt(cx - 2, cy, cz + 2).setType(Material.END_STONE_BRICKS);
        world.getBlockAt(cx + 2, cy, cz + 2).setType(Material.END_STONE_BRICKS);

        // 3. Çerçeveler: Gözleri takılı (setEye(true)) ve içe dönük
        // Kuzey (Z = cz - 2, X = cx - 1 .. cx + 1), içeri bakan: SOUTH
        for (int x = cx - 1; x <= cx + 1; x++) {
            setEndPortalFrame(world.getBlockAt(x, cy, cz - 2), org.bukkit.block.BlockFace.SOUTH);
        }
        // Güney (Z = cz + 2, X = cx - 1 .. cx + 1), içeri bakan: NORTH
        for (int x = cx - 1; x <= cx + 1; x++) {
            setEndPortalFrame(world.getBlockAt(x, cy, cz + 2), org.bukkit.block.BlockFace.NORTH);
        }
        // Batı (X = cx - 2, Z = cz - 1 .. cz + 1), içeri bakan: EAST
        for (int z = cz - 1; z <= cz + 1; z++) {
            setEndPortalFrame(world.getBlockAt(cx - 2, cy, z), org.bukkit.block.BlockFace.EAST);
        }
        // Doğu (X = cx + 2, Z = cz - 1 .. cz + 1), içeri bakan: WEST
        for (int z = cz - 1; z <= cz + 1; z++) {
            setEndPortalFrame(world.getBlockAt(cx + 2, cy, z), org.bukkit.block.BlockFace.WEST);
        }

        // 4. Merkez: 3x3 Aktif End Portalı
        for (int x = cx - 1; x <= cx + 1; x++) {
            for (int z = cz - 1; z <= cz + 1; z++) {
                world.getBlockAt(x, cy, z).setType(Material.END_PORTAL);
            }
        }

        island.setEndPortalBuilt(true);
        plugin.getIslandManager().saveIslands();
        plugin.getLogger().info(island.getOwnerName() + " oyuncusunun adasına End Portalı başarıyla inşa edildi!");
    }

    private void setEndPortalFrame(org.bukkit.block.Block block, org.bukkit.block.BlockFace face) {
        block.setType(Material.END_PORTAL_FRAME);
        org.bukkit.block.data.BlockData data = block.getBlockData();
        if (data instanceof org.bukkit.block.data.type.EndPortalFrame) {
            org.bukkit.block.data.type.EndPortalFrame frame = (org.bukkit.block.data.type.EndPortalFrame) data;
            frame.setEye(true);
            frame.setFacing(face);
            block.setBlockData(frame);
        }
    }

    public Map<Integer, Phase> getPhases() {
        return phases;
    }

    public Random getRandom() {
        return random;
    }
}
