package dev.bekirr.tekblok.listeners;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.utils.ColorUtils;

import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.Phase;
import dev.bekirr.tekblok.models.PhaseChest;
import dev.bekirr.tekblok.models.PhaseMob;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockBreakListener implements Listener {

    private final TekBlokPlugin plugin;
    private final Random random = new Random();

    public BlockBreakListener(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        Player player = event.getPlayer();

        Island island = plugin.getIslandManager().getIslandByLocation(loc);
        if (island == null) {
            // TekBlok dışındaki herhangi bir blok kırıldığında da eşyalar otomatik envantere gelir
            Island areaIsland = plugin.getIslandManager().getIslandByArea(loc);
            if (areaIsland != null) {
                if (!player.hasPermission("tekblok.admin") && !areaIsland.isMemberOrOwner(player.getUniqueId())) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.not-island-member"));
                    return;
                }
            }

            if (player.getGameMode() != GameMode.CREATIVE) {
                if (block.getState() instanceof Chest) {
                    Chest chest = (Chest) block.getState();
                    for (ItemStack item : chest.getInventory().getContents()) {
                        if (item != null && item.getType() != Material.AIR) {
                            for (ItemStack remaining : player.getInventory().addItem(item).values()) {
                                player.getWorld().dropItemNaturally(player.getLocation(), remaining);
                            }
                        }
                    }
                    chest.getInventory().clear();
                }

                Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand(), player);
                event.setDropItems(false);
                for (ItemStack drop : drops) {
                    if (drop != null && !drop.getType().isAir()) {
                        for (ItemStack rem : player.getInventory().addItem(drop).values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), rem);
                        }
                    }
                }
            }
            return;
        }

        // 1. Üyelik / Yetki Kontrolü
        if (!player.hasPermission("tekblok.admin") && !island.isMemberOrOwner(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessage("messages.not-island-member"));
            return;
        }

        // 2. Kritik: Bloğun hiçbir zaman HAVA (AIR) olmaması için olayı iptal ediyoruz.
        // Bu sayede blok kırıldığı an oyuncu aşağı düşmez, 0-tick düşme engellenir.
        event.setCancelled(true);

        boolean autoPickup = plugin.getConfig().getBoolean("island.auto-pickup-drops", true);

        // 3. Eğer kırılan blok bir Sandık (Chest) ise içindeki eşyaları ve sandığı güvenle topla
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            for (ItemStack item : chest.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    if (autoPickup) {
                        for (ItemStack remaining : player.getInventory().addItem(item).values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), remaining);
                        }
                    } else {
                        player.getWorld().dropItemNaturally(loc.clone().add(0.5, 1.0, 0.5), item);
                    }
                }
            }
            chest.getInventory().clear();

            // Sandık bloğunu da envantere ver
            ItemStack chestItem = new ItemStack(Material.CHEST, 1);
            if (autoPickup) {
                for (ItemStack rem : player.getInventory().addItem(chestItem).values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), rem);
                }
            } else {
                player.getWorld().dropItemNaturally(loc.clone().add(0.5, 1.0, 0.5), chestItem);
            }
        } else {
            // Normal blok ise dropları topla
            if (player.getGameMode() != GameMode.CREATIVE) {
                Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand(), player);

                // Eğer vanilya alet gerektirdiği için (örneğin kazmasız taş kırma) drop boş dönerse,
                // oyuncunun emeği boşa gitmesin ve tek blokta tıkanmasın diye varsayılan bloğu ver
                if (drops == null || drops.isEmpty()) {
                    drops = getFallbackDrops(block.getType());
                }

                if (autoPickup) {
                    for (ItemStack drop : drops) {
                        for (ItemStack remaining : player.getInventory().addItem(drop).values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), remaining);
                        }
                    }
                } else {
                    for (ItemStack drop : drops) {
                        player.getWorld().dropItemNaturally(loc.clone().add(0.5, 1.0, 0.5), drop);
                    }
                }

                // Deneyim puanı (EXP)
                int exp = event.getExpToDrop();
                if (exp > 0) {
                    player.giveExp(exp);
                }
            }
        }

        // Alet yıpranması (SADECE gerçek aletler zarar görür, bloklar/eşyalar asla silinmez)
        damageTool(player);

        // Ses ve efekt
        Location centerLoc = loc.clone().add(0.5, 0.5, 0.5);
        player.getWorld().playEffect(centerLoc, Effect.STEP_SOUND, block.getType());

        // İstatistik artışı
        island.incrementBlocksBroken();

        // Aşama ilerleme kontrolü
        plugin.getPhaseManager().checkPhaseProgress(island, player);
        Phase phase = plugin.getPhaseManager().getPhase(island.getCurrentPhaseId());

                // 4. Yeni bloğu / sandığı / canavarı oluştur
        // A) Şanslı Sandık
        if (random.nextDouble() < phase.getChestChance()) {
            PhaseChest chestLoot = phase.getRandomChest(random);
            block.setType(Material.CHEST, false);

            int memberCount = 1 + (island.getMembers() != null ? island.getMembers().size() : 0);

            if (block.getState() instanceof Chest) {
                Chest chestState = (Chest) block.getState();
                chestState.getInventory().clear();
                if (chestLoot != null) {
                    List<Integer> availableSlots = new ArrayList<>();
                    for (int s = 0; s < chestState.getInventory().getSize(); s++) {
                        availableSlots.add(s);
                    }
                    Collections.shuffle(availableSlots, random);
                    int slotIndex = 0;

                    for (ItemStack original : chestLoot.getItems()) {
                        if (slotIndex >= availableSlots.size()) break;
                        ItemStack item = original.clone();
                        if (item.getType().getMaxStackSize() > 1) {
                            int baseAmt = item.getAmount();
                            int scaledAmt = Math.min(item.getMaxStackSize(), Math.max(1, (int) Math.round(baseAmt * (1.0 + (memberCount - 1) * 0.5))));
                            item.setAmount(scaledAmt);
                            chestState.getInventory().setItem(availableSlots.get(slotIndex++), item);
                        } else {
                            chestState.getInventory().setItem(availableSlots.get(slotIndex++), item);
                            if (memberCount > 1 && slotIndex < availableSlots.size()) {
                                chestState.getInventory().setItem(availableSlots.get(slotIndex++), item.clone());
                            }
                        }
                    }
                }
            }

            // Sandık geldiğinde oyuncu üzerindeyse güvenli şekilde sandığın üstüne konumlandır ve düşüş mesafesini sıfırla
            Location pLoc = player.getLocation();
            if (isStandingOn(pLoc, loc)) {
                pLoc.setY(loc.getY() + 1.0);
                player.teleport(pLoc);
                player.setFallDistance(0.0f);
            }

            player.getWorld().playSound(centerLoc, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, centerLoc.clone().add(0, 0.7, 0), 10, 0.3, 0.3, 0.3, 0.05);

            if (memberCount > 1) {
                player.sendMessage(ColorUtils.color("&6&l[ŞANSLI SANDIK] &eAda üye sayınıza göre (&b" + memberCount + " Kişi&e) güçlendirilmiş ganimet çıktı!"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("messages.lucky-chest-spawned"));
            }
        }
        // B) Canavar / Canlı Doğma Kontrolü (Düşürülmüş Oran)
        else if (random.nextDouble() < (phase.getMobChance() * plugin.getConfig().getDouble("island.mob-chance-multiplier", 0.5))) {
            PhaseMob mobData = phase.getRandomMob(random);
            Material nextMat = phase.getRandomBlock(random);
            block.setType(nextMat != null ? nextMat : Material.GRASS_BLOCK, false);

            if (mobData != null) {
                Location mobSpawnLoc = findSafeMobSpawnLocation(loc);
                Entity entity = player.getWorld().spawnEntity(mobSpawnLoc, mobData.getEntityType());

                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    // KRİTİK: Canlının oyuncuyu iterek boşluğa düşürmesini engelle
                    living.setCollidable(false);

                    if (mobData.getCustomName() != null && !mobData.getCustomName().isEmpty()) {
                        living.setCustomName(mobData.getCustomName());
                        living.setCustomNameVisible(true);
                    }
                }

                player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, mobSpawnLoc, 15, 0.2, 0.5, 0.2, 0.02);
                player.getWorld().playSound(mobSpawnLoc, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 1.2f);
                player.sendMessage(plugin.getConfigManager().getMessage("messages.mob-spawned"));
            }

            Location pLoc = player.getLocation();
            if (isStandingOn(pLoc, loc)) {
                pLoc.setY(loc.getY() + 1.0);
                player.teleport(pLoc);
                player.setFallDistance(0.0f);
            }
        }
        // C) Normal Blok Yenileme
        else {
            Material nextMat = phase.getRandomBlock(random);
            block.setType(nextMat != null ? nextMat : Material.GRASS_BLOCK, false);
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, centerLoc.clone().add(0, 0.6, 0), 4, 0.2, 0.2, 0.2, 0.01);

            Location pLoc = player.getLocation();
            if (isStandingOn(pLoc, loc)) {
                player.setFallDistance(0.0f);
            }
        }

        // 5. Taban katman kayasını (Bedrock) her ihtimale karşı garantiye al
        Location bedrockLoc = loc.clone().add(0, -1, 0);
        if (bedrockLoc.getBlock().getType() != Material.BEDROCK) {
            bedrockLoc.getBlock().setType(Material.BEDROCK, false);
        }

        // 6. HUD güncellemeleri
        plugin.getDisplayManager().sendActionBar(player, island);
        plugin.getDisplayManager().updateBossBar(island);
    }

    private void damageTool(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || tool.getType().isAir()) return;

        // KRİTİK DÜZELTME:
        // Sadece maxDurability değeri 0'dan BÜYÜK olan GERÇEK aletler (kazma, balta, kürek vb.) yıpranabilir!
        // Bloklar (Toprak, Taş, Odun vb.) ve diğer eşyaların maxDurability değeri 0'dır.
        // Eğer maxDurability kontrol edilmezse, eldeki toprak/blok alet sanılıp 1 vuruşta yok oluyordu.
        short maxDurability = tool.getType().getMaxDurability();
        if (maxDurability <= 0) {
            return;
        }

        if (tool.getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) tool.getItemMeta();
            int unbreaking = tool.getEnchantmentLevel(Enchantment.DURABILITY);
            if (unbreaking > 0) {
                if (random.nextInt(unbreaking + 1) != 0) {
                    return; // Kırılmazlık büyüsü hasarı engelledi
                }
            }
            int newDamage = damageable.getDamage() + 1;
            if (newDamage >= maxDurability) {
                player.getInventory().setItemInMainHand(null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            } else {
                damageable.setDamage(newDamage);
                tool.setItemMeta((ItemMeta) damageable);
            }
        }
    }

    private Collection<ItemStack> getFallbackDrops(Material material) {
        List<ItemStack> list = new ArrayList<>();
        if (material == null || material.isAir()) return list;

        switch (material) {
            case STONE:
                list.add(new ItemStack(Material.COBBLESTONE, 1));
                break;
            case COAL_ORE:
                list.add(new ItemStack(Material.COAL, 1));
                break;
            case IRON_ORE:
                list.add(new ItemStack(Material.IRON_ORE, 1));
                break;
            case GOLD_ORE:
                list.add(new ItemStack(Material.GOLD_ORE, 1));
                break;
            case DIAMOND_ORE:
                list.add(new ItemStack(Material.DIAMOND, 1));
                break;
            case EMERALD_ORE:
                list.add(new ItemStack(Material.EMERALD, 1));
                break;
            case NETHER_QUARTZ_ORE:
                list.add(new ItemStack(Material.QUARTZ, 1));
                break;
            case ANCIENT_DEBRIS:
                list.add(new ItemStack(Material.ANCIENT_DEBRIS, 1));
                break;
            case SNOW_BLOCK:
                list.add(new ItemStack(Material.SNOWBALL, 4));
                break;
            case OAK_LEAVES:
            case SPRUCE_LEAVES:
            case BIRCH_LEAVES:
            case JUNGLE_LEAVES:
            case ACACIA_LEAVES:
            case DARK_OAK_LEAVES:
                list.add(new ItemStack(material, 1));
                break;
            default:
                if (material.isItem()) {
                    list.add(new ItemStack(material, 1));
                }
                break;
        }
        return list;
    }

    private boolean isStandingOn(Location playerLoc, Location blockLoc) {
        if (playerLoc == null || blockLoc == null) return false;
        if (playerLoc.getWorld() == null || !playerLoc.getWorld().equals(blockLoc.getWorld())) return false;
        double px = playerLoc.getX();
        double py = playerLoc.getY();
        double pz = playerLoc.getZ();
        int bx = blockLoc.getBlockX();
        int by = blockLoc.getBlockY();
        int bz = blockLoc.getBlockZ();
        return (px >= bx - 0.4 && px <= bx + 1.4)
                && (pz >= bz - 0.4 && pz <= bz + 1.4)
                && (py >= by && py <= by + 2.5);
    }

    private Location findSafeMobSpawnLocation(Location oneBlockLoc) {
        World w = oneBlockLoc.getWorld();
        int x = oneBlockLoc.getBlockX();
        int y = oneBlockLoc.getBlockY();
        int z = oneBlockLoc.getBlockZ();

        int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
        for (int[] off : offsets) {
            Block b = w.getBlockAt(x + off[0], y, z + off[1]);
            Block above = w.getBlockAt(x + off[0], y + 1, z + off[1]);
            if (b.getType().isSolid() && above.getType().isAir()) {
                return new Location(w, x + off[0] + 0.5, y + 1.0, z + off[1] + 0.5);
            }
        }
        return oneBlockLoc.clone().add(0.5, 1.0, 0.5);
    }
}
