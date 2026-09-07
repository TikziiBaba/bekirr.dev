package dev.bekirr.tekblok.listeners;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.IslandFarmer;
import dev.bekirr.tekblok.utils.ColorUtils;
import dev.bekirr.tekblok.utils.SpawnerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;

public class SpawnerListener implements Listener {

    private final TekBlokPlugin plugin;

    public SpawnerListener(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.SPAWNER) return;

        Block block = event.getBlockPlaced();
        if (block.getState() instanceof CreatureSpawner) {
            EntityType type = SpawnerUtils.getEntityTypeFromItem(item);
            if (type != null) {
                CreatureSpawner cs = (CreatureSpawner) block.getState();
                cs.setSpawnedType(type);
                cs.update(true, true);

                Player player = event.getPlayer();
                player.sendMessage(ColorUtils.color("&a&l[SPAWNER] &e" + SpawnerUtils.getEntityTurkishName(type) + " Spawnerı &abaşarıyla yerleştirildi!"));
                player.playSound(block.getLocation(), Sound.BLOCK_ANVIL_USE, 0.7f, 1.5f);
                block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().clone().add(0.5, 0.5, 0.5), 15, 0.3, 0.3, 0.3, 0.1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.SPAWNER) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // İpeksi Dokunuş (Silk Touch) ile kırılırsa spawnerı eşya olarak geri ver
        if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
            if (block.getState() instanceof CreatureSpawner) {
                CreatureSpawner cs = (CreatureSpawner) block.getState();
                EntityType type = cs.getSpawnedType();
                ItemStack spawnerItem = SpawnerUtils.createSpawnerItem(type, null);

                event.setExpToDrop(0);
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation().clone().add(0.5, 0.5, 0.5), spawnerItem);
                player.sendMessage(ColorUtils.color("&a&l[SPAWNER] &e" + SpawnerUtils.getEntityTurkishName(type) + " Spawnerı &aipeksi dokunuş ile geri alındı!"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        Entity entity = event.getEntity();
        Location loc = entity.getLocation();

        // Canlının doğduğu adayı bul
        Island island = plugin.getIslandManager().getIslandByArea(loc);
        if (island == null) return;

        IslandFarmer farmer = plugin.getFarmerManager().getFarmer(island.getOwnerUUID());
        // Kullanıcı Talebi: Çiftçi spawnerlardan çıkan canlıları otomatik öldürüp toplasın
        if (!farmer.isUnlocked() || !farmer.isAutoCollect()) {
            return;
        }

        // Çiftçi devredeyse: Canlıyı otomatik kes ve ganimetini çiftçi deposuna aktar
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!entity.isDead() && entity.isValid()) {
                Location eLoc = entity.getLocation().clone().add(0, 0.8, 0);
                if (eLoc.getWorld() != null) {
                    eLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, eLoc, 2, 0.2, 0.2, 0.2, 0.05);
                    eLoc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, eLoc, 4, 0.2, 0.2, 0.2, 0.05);
                }

                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    Player owner = Bukkit.getPlayer(island.getOwnerUUID());
                    if (owner != null && owner.isOnline()) {
                        living.damage(99999.0, owner);
                    } else {
                        living.damage(99999.0);
                    }
                } else {
                    entity.remove();
                }
            }
        }, 2L);
    }
}
