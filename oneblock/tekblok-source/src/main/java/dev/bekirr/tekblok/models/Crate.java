package dev.bekirr.tekblok.models;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Crate {

    private final String id;
    private final String displayName;
    private final String keyName;
    private final Material keyMaterial;
    private final List<String> keyLore;
    private final Particle particle;
    private final int particleCount;
    private final List<String> hologramLines;
    private final List<CrateReward> rewards;
    private final Random random = new Random();

    public Crate(String id, String displayName, String keyName, Material keyMaterial, List<String> keyLore,
                 Particle particle, int particleCount, List<String> hologramLines, List<CrateReward> rewards) {
        this.id = id.toLowerCase();
        this.displayName = displayName;
        this.keyName = keyName;
        this.keyMaterial = keyMaterial != null ? keyMaterial : Material.TRIPWIRE_HOOK;
        this.keyLore = keyLore != null ? keyLore : new ArrayList<>();
        this.particle = particle != null ? particle : Particle.FLAME;
        this.particleCount = particleCount > 0 ? particleCount : 8;
        this.hologramLines = hologramLines != null ? hologramLines : new ArrayList<>();
        this.rewards = rewards != null ? rewards : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getKeyName() {
        return keyName;
    }

    public Material getKeyMaterial() {
        return keyMaterial;
    }

    public List<String> getKeyLore() {
        return keyLore;
    }

    public Particle getParticle() {
        return particle;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public List<String> getHologramLines() {
        return hologramLines;
    }

    public List<CrateReward> getRewards() {
        return rewards;
    }

    public ItemStack createKey(TekBlokPlugin plugin, int amount) {
        ItemStack item = new ItemStack(keyMaterial, Math.min(amount, 64));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtils.color(keyName));
            List<String> coloredLore = new ArrayList<>();
            for (String l : keyLore) {
                coloredLore.add(ColorUtils.color(l));
            }
            meta.setLore(coloredLore);
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            NamespacedKey nKey = new NamespacedKey(plugin, "crate_key");
            meta.getPersistentDataContainer().set(nKey, PersistentDataType.STRING, id);

            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isMatchingKey(ItemStack item, TekBlokPlugin plugin) {
        if (item == null || item.getType() != keyMaterial || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        NamespacedKey nKey = new NamespacedKey(plugin, "crate_key");
        if (meta.getPersistentDataContainer().has(nKey, PersistentDataType.STRING)) {
            String cId = meta.getPersistentDataContainer().get(nKey, PersistentDataType.STRING);
            return id.equalsIgnoreCase(cId);
        }

        // Fallback: Display name check
        if (meta.hasDisplayName()) {
            String plainExpected = ColorUtils.color(keyName);
            return meta.getDisplayName().equals(plainExpected);
        }
        return false;
    }

    public CrateReward getRandomReward() {
        if (rewards.isEmpty()) return null;

        int totalWeight = 0;
        for (CrateReward r : rewards) {
            totalWeight += r.getChance();
        }

        if (totalWeight <= 0) {
            return rewards.get(random.nextInt(rewards.size()));
        }

        int target = random.nextInt(totalWeight) + 1;
        int current = 0;
        for (CrateReward r : rewards) {
            current += r.getChance();
            if (target <= current) {
                return r;
            }
        }
        return rewards.get(0);
    }
}
