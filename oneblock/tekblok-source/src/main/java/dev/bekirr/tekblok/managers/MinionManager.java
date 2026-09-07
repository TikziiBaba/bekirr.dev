package dev.bekirr.tekblok.managers;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.IslandFarmer;
import dev.bekirr.tekblok.models.Minion;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MinionManager {

    private final TekBlokPlugin plugin;
    private final Map<UUID, Minion> minionsByOwner = new HashMap<>();
    private final Map<UUID, UUID> entityToOwner = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;
    private BukkitTask breakTask;

    public MinionManager(TekBlokPlugin plugin) {
        this.plugin = plugin;
        loadMinions();
        startBreakTask();
    }

    public void loadMinions() {
        minionsByOwner.clear();
        entityToOwner.clear();

        dataFile = new File(plugin.getDataFolder(), "data/minions.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("minions.yml olusturulamadi: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection section = dataConfig.getConfigurationSection("minions");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                UUID ownerUUID = UUID.fromString(key);
                ConfigurationSection mSec = section.getConfigurationSection(key);
                if (mSec == null) continue;

                String wName = mSec.getString("world", "TekBlokWorld");
                World world = Bukkit.getWorld(wName);
                if (world == null) continue;

                int x = mSec.getInt("x");
                int y = mSec.getInt("y");
                int z = mSec.getInt("z");
                Location loc = new Location(world, x, y, z);

                String facingStr = mSec.getString("facing", "NORTH");
                BlockFace facing;
                try {
                    facing = BlockFace.valueOf(facingStr);
                } catch (IllegalArgumentException e) {
                    facing = BlockFace.NORTH;
                }

                int level = mSec.getInt("level", 1);
                int broken = mSec.getInt("broken", 0);

                UUID asUUID = null;
                if (mSec.contains("entity-uuid")) {
                    try {
                        asUUID = UUID.fromString(mSec.getString("entity-uuid"));
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                Minion minion = new Minion(ownerUUID, loc, facing, level, broken, asUUID);
                minionsByOwner.put(ownerUUID, minion);

                // Eger ArmorStand hala dunyada mevcut degilse veya yuklenmemisse yeniden olustur
                respawnArmorStand(minion);

            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Minyon UUID okunamadi: " + key);
            }
        }
        plugin.getLogger().info("Toplam " + minionsByOwner.size() + " adet Minyon yuklendi.");
    }

    public void saveMinions() {
        if (dataConfig == null || dataFile == null) return;
        dataConfig.set("minions", null);

        for (Minion m : minionsByOwner.values()) {
            String path = "minions." + m.getOwnerUUID().toString();
            dataConfig.set(path + ".world", m.getLocation().getWorld().getName());
            dataConfig.set(path + ".x", m.getLocation().getBlockX());
            dataConfig.set(path + ".y", m.getLocation().getBlockY());
            dataConfig.set(path + ".z", m.getLocation().getBlockZ());
            dataConfig.set(path + ".facing", m.getFacing().name());
            dataConfig.set(path + ".level", m.getLevel());
            dataConfig.set(path + ".broken", m.getBlocksBroken());
            if (m.getArmorStandUUID() != null) {
                dataConfig.set(path + ".entity-uuid", m.getArmorStandUUID().toString());
            }
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("minions.yml kaydedilemedi: " + e.getMessage());
        }
    }

    public ItemStack createMinionItem(int level) {
        ItemStack item = new ItemStack(Material.ARMOR_STAND);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtils.color("&b&lKirici Minyon &8[&eSeviye " + level + "&8]"));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.color("&8-----------------------------"));
            lore.add(ColorUtils.color("&7Yere koydugunuzda onundeki"));
            lore.add(ColorUtils.color("&7bloklari otomatik olarak kirar!"));
            lore.add("");
            lore.add(ColorUtils.color("&7Minyon Turu: &fKirici / Tasci"));
            lore.add(ColorUtils.color("&7Seviye: &e" + level + " &7/ &b3"));
            lore.add(ColorUtils.color("&7Kirilma Menzili: &a" + level + " Blok Onu"));
            lore.add(ColorUtils.color("&7Kirilan kiriktaslar otomatik"));
            lore.add(ColorUtils.color("&7ada ciftcisine aktarilir!"));
            lore.add("&8-----------------------------");
            lore.add(ColorUtils.color("&eSag Tik: &fZemine Minyonu Koy"));
            lore.add(ColorUtils.color("&c(Her adaya sadece 1 adet)"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isMinionItem(ItemStack item) {
        if (item == null || item.getType() != Material.ARMOR_STAND || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().contains("Kirici Minyon");
    }

    public int getMinionItemLevel(ItemStack item) {
        if (!isMinionItem(item)) return 1;
        String name = item.getItemMeta().getDisplayName();
        if (name.contains("Seviye 3")) return 3;
        if (name.contains("Seviye 2")) return 2;
        return 1;
    }

    public BlockFace getPlayerFacing(Player player) {
        float yaw = player.getLocation().getYaw();
        yaw = (yaw % 360 + 360) % 360;
        if (yaw >= 45 && yaw < 135) {
            return BlockFace.WEST;
        } else if (yaw >= 135 && yaw < 225) {
            return BlockFace.NORTH;
        } else if (yaw >= 225 && yaw < 315) {
            return BlockFace.EAST;
        } else {
            return BlockFace.SOUTH;
        }
    }

    public boolean placeMinion(Player player, Location loc, BlockFace facing, int level) {
        Island island = plugin.getIslandManager().getIslandByArea(loc);
        if (island == null || !island.isMemberOrOwner(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("messages.not-island-member"));
            return false;
        }

        UUID ownerId = island.getOwnerUUID();
        if (minionsByOwner.containsKey(ownerId)) {
            player.sendMessage(ColorUtils.color("&cAdanizda zaten bir minyon kurulu! (Adada en fazla 1 minyon olabilir)"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return false;
        }

        Location asLoc = loc.clone().add(0.5, 0.0, 0.5);
        float yaw = 0f;
        if (facing == BlockFace.NORTH) yaw = 180f;
        else if (facing == BlockFace.SOUTH) yaw = 0f;
        else if (facing == BlockFace.EAST) yaw = 270f;
        else if (facing == BlockFace.WEST) yaw = 90f;
        asLoc.setYaw(yaw);

        ArmorStand as = (ArmorStand) asLoc.getWorld().spawnEntity(asLoc, EntityType.ARMOR_STAND);
        setupArmorStand(as, level);

        Minion minion = new Minion(ownerId, loc, facing, level, 0, as.getUniqueId());
        minionsByOwner.put(ownerId, minion);
        entityToOwner.put(as.getUniqueId(), ownerId);
        saveMinions();

        player.sendMessage(ColorUtils.color("&a&l[MINYON] &eKirici Minyon basariyla yerlestirildi! &7(Onundeki bloklari kirar)"));
        player.playSound(loc, Sound.BLOCK_ANVIL_USE, 0.7f, 1.2f);
        loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, asLoc.clone().add(0, 0.8, 0), 12, 0.3, 0.3, 0.3, 0.05);
        return true;
    }

    public void removeMinion(UUID ownerId) {
        Minion minion = minionsByOwner.remove(ownerId);
        if (minion == null) return;

        if (minion.getArmorStandUUID() != null) {
            entityToOwner.remove(minion.getArmorStandUUID());
            Entity e = Bukkit.getEntity(minion.getArmorStandUUID());
            if (e != null) {
                e.remove();
            } else if (minion.getLocation() != null && minion.getLocation().getWorld() != null) {
                for (Entity nearby : minion.getLocation().getWorld().getNearbyEntities(minion.getLocation().add(0.5, 0.5, 0.5), 2, 2, 2)) {
                    if (nearby instanceof ArmorStand && nearby.getCustomName() != null && nearby.getCustomName().contains("Kirici Minyon")) {
                        nearby.remove();
                    }
                }
            }
        }
        saveMinions();
    }

    public boolean pickupMinion(Player player, Minion minion) {
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ColorUtils.color("&cEnvanteriniz dolu! Minyonu almak icin yer acin."));
            return false;
        }

        ItemStack minionItem = createMinionItem(minion.getLevel());
        player.getInventory().addItem(minionItem);
        removeMinion(minion.getOwnerUUID());

        player.sendMessage(ColorUtils.color("&a&l[MINYON] &eMinyon basariyla yerden kaldirildi ve envanterinize verildi!"));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
        return true;
    }

    public boolean upgradeMinion(Player player, Minion minion) {
        if (minion.getLevel() >= 3) {
            player.sendMessage(ColorUtils.color("&cMinyon zaten maksimum seviyede (Seviye 3)!"));
            return false;
        }

        double cost = minion.getLevel() == 1 ? 1250.0 : 2500.0;
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());

        if (balance < cost) {
            player.sendMessage(ColorUtils.color("&cYetersiz Bakiye! Seviye atlamak icin: &e" + plugin.getEconomyManager().format(cost)));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return false;
        }

        plugin.getEconomyManager().withdraw(player.getUniqueId(), cost);
        minion.setLevel(minion.getLevel() + 1);

        // ArmorStand gorunumunu guncelle
        respawnArmorStand(minion);
        saveMinions();

        player.sendMessage(ColorUtils.color("&a&l[MINYON] &eTebrikler! Minyonunuz &b&lSeviye " + minion.getLevel() + " &eoldu! (Menzil: " + minion.getLevel() + " Blok)"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
        if (minion.getLocation().getWorld() != null) {
            minion.getLocation().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, minion.getLocation().clone().add(0.5, 1.0, 0.5), 20, 0.4, 0.4, 0.4, 0.1);
        }
        return true;
    }

    private void setupArmorStand(ArmorStand as, int level) {
        as.setSmall(true);
        as.setArms(true);
        as.setBasePlate(false);
        as.setInvulnerable(true);
        as.setGravity(false);
        as.setCollidable(false);
        as.setCustomName(ColorUtils.color("&b&lKirici Minyon &8[&eSeviye " + level + "&8]"));
        as.setCustomNameVisible(true);

        // Ekipmanlar
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta lMeta = (LeatherArmorMeta) helmet.getItemMeta();
        if (lMeta != null) {
            lMeta.setColor(level == 1 ? Color.fromRGB(0, 150, 255) : (level == 2 ? Color.fromRGB(200, 200, 200) : Color.fromRGB(0, 255, 255)));
            helmet.setItemMeta(lMeta);
        }

        ItemStack tool;
        if (level == 1) {
            tool = new ItemStack(Material.WOODEN_PICKAXE);
        } else if (level == 2) {
            tool = new ItemStack(Material.IRON_PICKAXE);
        } else {
            tool = new ItemStack(Material.DIAMOND_PICKAXE);
        }

        as.getEquipment().setHelmet(helmet);
        as.getEquipment().setItemInMainHand(tool);
    }

    public void respawnArmorStand(Minion minion) {
        if (minion.getLocation() == null || minion.getLocation().getWorld() == null) return;
        World world = minion.getLocation().getWorld();

        // Eski entity varsa temizle
        if (minion.getArmorStandUUID() != null) {
            Entity old = Bukkit.getEntity(minion.getArmorStandUUID());
            if (old != null) {
                old.remove();
            }
        }

        Location asLoc = minion.getLocation().clone().add(0.5, 0.0, 0.5);
        float yaw = 0f;
        if (minion.getFacing() == BlockFace.NORTH) yaw = 180f;
        else if (minion.getFacing() == BlockFace.SOUTH) yaw = 0f;
        else if (minion.getFacing() == BlockFace.EAST) yaw = 270f;
        else if (minion.getFacing() == BlockFace.WEST) yaw = 90f;
        asLoc.setYaw(yaw);

        ArmorStand as = (ArmorStand) world.spawnEntity(asLoc, EntityType.ARMOR_STAND);
        setupArmorStand(as, minion.getLevel());
        minion.setArmorStandUUID(as.getUniqueId());
        entityToOwner.put(as.getUniqueId(), minion.getOwnerUUID());
    }

    public void startBreakTask() {
        if (breakTask != null) {
            breakTask.cancel();
        }

        // Her 50 tickte bir (2.5 saniyede bir blok kirma dongusu)
        breakTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Minion m : minionsByOwner.values()) {
                Location loc = m.getLocation();
                if (loc == null || loc.getWorld() == null) continue;
                World world = loc.getWorld();

                if (!world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                    continue;
                }

                BlockFace face = m.getFacing();
                Island island = plugin.getIslandManager().getIslandByPlayer(m.getOwnerUUID());
                IslandFarmer farmer = island != null ? plugin.getFarmerManager().getFarmer(island.getOwnerUUID()) : null;

                boolean brokeAny = false;
                for (int dist = 1; dist <= m.getLevel(); dist++) {
                    Block target = loc.getBlock().getRelative(face, dist);
                    Material mat = target.getType();

                    if (mat.isSolid() && mat != Material.BEDROCK && mat != Material.BARRIER
                            && !mat.name().contains("CHEST") && !mat.name().contains("SHULKER")
                            && !mat.name().contains("COMMAND")) {

                        Collection<ItemStack> drops = target.getDrops();
                        if (drops.isEmpty() && (mat == Material.STONE || mat == Material.COBBLESTONE)) {
                            drops = Collections.singletonList(new ItemStack(Material.COBBLESTONE, 1));
                        }

                        for (ItemStack drop : drops) {
                            if (drop == null || drop.getType().isAir()) continue;

                            // Kirilan kiriktaslar otomatik Ciftciye aktarilir
                            if (drop.getType() == Material.COBBLESTONE) {
                                if (farmer != null && farmer.isUnlocked() && farmer.isAutoCollect()) {
                                    farmer.addItem(Material.COBBLESTONE, drop.getAmount());
                                } else {
                                    world.dropItemNaturally(target.getLocation().add(0.5, 0.5, 0.5), drop);
                                }
                            } else {
                                if (farmer != null && farmer.isUnlocked() && farmer.isAutoCollect() && plugin.getFarmerManager().isFarmerItem(drop.getType())) {
                                    farmer.addItem(drop.getType(), drop.getAmount());
                                } else {
                                    world.dropItemNaturally(target.getLocation().add(0.5, 0.5, 0.5), drop);
                                }
                            }
                        }

                        world.playEffect(target.getLocation(), Effect.STEP_SOUND, mat);
                        target.setType(Material.AIR, false);
                        m.incrementBlocksBroken();
                        brokeAny = true;
                    }
                }

                if (brokeAny) {
                    world.playSound(loc, Sound.BLOCK_STONE_BREAK, 0.6f, 1.2f);
                }
            }
        }, 60L, 50L);
    }

    public void removeAllEntities() {
        if (breakTask != null) {
            breakTask.cancel();
        }
        for (Minion m : minionsByOwner.values()) {
            if (m.getArmorStandUUID() != null) {
                Entity e = Bukkit.getEntity(m.getArmorStandUUID());
                if (e != null) {
                    e.remove();
                }
            }
        }
    }

    public Minion getMinionByEntity(UUID entityUUID) {
        UUID owner = entityToOwner.get(entityUUID);
        return owner != null ? minionsByOwner.get(owner) : null;
    }

    public Minion getMinion(UUID ownerUUID) {
        return minionsByOwner.get(ownerUUID);
    }

    public Map<UUID, Minion> getMinions() {
        return minionsByOwner;
    }
}
