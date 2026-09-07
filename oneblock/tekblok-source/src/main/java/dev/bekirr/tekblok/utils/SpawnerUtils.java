package dev.bekirr.tekblok.utils;

import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.List;

public class SpawnerUtils {

    public static String getEntityTurkishName(EntityType type) {
        if (type == null) return "Bilinmeyen";
        switch (type) {
            case IRON_GOLEM: return "Demir Golem";
            case BLAZE: return "Blaze";
            case WITHER_SKELETON: return "Wither İskeleti";
            case ENDERMAN: return "Enderman";
            case CREEPER: return "Creeper";
            case SKELETON: return "İskelet";
            case ZOMBIE: return "Zombi";
            case SPIDER: return "Örümcek";
            case CAVE_SPIDER: return "Mağara Örümceği";
            case COW: return "İnek";
            case SHEEP: return "Koyun";
            case PIG: return "Domuz";
            case CHICKEN: return "Tavuk";
            case SLIME: return "Balçık";
            case MAGMA_CUBE: return "Magma Küpü";
            case WITCH: return "Cadı";
            case DROWNED: return "Boğulmuş";
            case HUSK: return "Çöl Zombisi";
            case STRAY: return "Buzul İskeleti";
            default: return type.name();
        }
    }

    public static ItemStack createSpawnerItem(EntityType type, String customName) {
        ItemStack item = new ItemStack(Material.SPAWNER);
        BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
        if (meta != null) {
            if (meta.getBlockState() instanceof CreatureSpawner) {
                CreatureSpawner cs = (CreatureSpawner) meta.getBlockState();
                cs.setSpawnedType(type);
                meta.setBlockState(cs);
            }
            String displayName = customName != null ? customName : ("&e&l" + getEntityTurkishName(type) + " Spawner");
            meta.setDisplayName(ColorUtils.color(displayName));

            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.color("&8-----------------------------"));
            lore.add(ColorUtils.color("&7Tür: &6" + getEntityTurkishName(type)));
            lore.add(ColorUtils.color("&7Bu eşyayı adana yerleştirerek"));
            lore.add(ColorUtils.color("&7otomatik canlı üretimi sağlayabilirsin."));
            lore.add("");
            lore.add(ColorUtils.color("&a★ Çiftçi bu spawnerdan doğanları"));
            lore.add(ColorUtils.color("&a★ otomatik kesip deposuna aktarır!"));
            lore.add(ColorUtils.color("&8-----------------------------"));
            meta.setLore(lore);

            item.setItemMeta(meta);
        }
        return item;
    }

    public static EntityType getEntityTypeFromItem(ItemStack item) {
        if (item == null || item.getType() != Material.SPAWNER) return null;
        if (item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
            if (bsm.getBlockState() instanceof CreatureSpawner) {
                CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();
                if (cs.getSpawnedType() != null && cs.getSpawnedType() != EntityType.UNKNOWN) {
                    return cs.getSpawnedType();
                }
            }
        }

        // İsim veya lore'dan tahmin
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String name = item.getItemMeta().getDisplayName().toLowerCase();
            for (EntityType type : EntityType.values()) {
                if (type.name().toLowerCase().contains(name) || name.contains(getEntityTurkishName(type).toLowerCase())) {
                    return type;
                }
            }
        }
        return EntityType.PIG;
    }
}
