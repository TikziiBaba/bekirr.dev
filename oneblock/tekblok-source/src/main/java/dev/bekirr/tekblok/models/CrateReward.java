package dev.bekirr.tekblok.models;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.utils.ColorUtils;
import dev.bekirr.tekblok.utils.SpawnerUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;

public class CrateReward {

    private final String type; // "ITEM" veya "MONEY"
    private final Material material;
    private final int amount;
    private final String name;
    private final String enchant;
    private final int chance;
    private final String mob;

    public CrateReward(String type, Material material, int amount, String name, String enchant, int chance, String mob) {
        this.type = type != null ? type.toUpperCase() : "ITEM";
        this.material = material != null ? material : Material.CHEST;
        this.amount = amount > 0 ? amount : 1;
        this.name = name;
        this.enchant = enchant;
        this.chance = chance > 0 ? chance : 10;
        this.mob = mob;
    }

    public CrateReward(String type, Material material, int amount, String name, String enchant, int chance) {
        this(type, material, amount, name, enchant, chance, null);
    }

    public String getType() {
        return type;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public String getEnchant() {
        return enchant;
    }

    public int getChance() {
        return chance;
    }

    public String getMob() {
        return mob;
    }

    public ItemStack buildItemStack() {
        if ("MONEY".equalsIgnoreCase(type)) {
            ItemStack gold = new ItemStack(Material.GOLD_INGOT, Math.min(amount / 50 + 1, 64));
            ItemMeta meta = gold.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ColorUtils.color(name != null ? name : "&a" + amount + " ₺ Bakiye"));
                meta.setLore(Collections.singletonList(ColorUtils.color("&7Otomatik olarak hesabınıza yatırılır.")));
                gold.setItemMeta(meta);
            }
            return gold;
        }

        if (material == Material.SPAWNER && mob != null && !mob.isEmpty()) {
            try {
                EntityType eType = EntityType.valueOf(mob.toUpperCase());
                ItemStack spawner = SpawnerUtils.createSpawnerItem(eType, name);
                spawner.setAmount(Math.min(amount, 64));
                return spawner;
            } catch (Exception ignored) {
            }
        }

        ItemStack item = new ItemStack(material, Math.min(amount, material.getMaxStackSize()));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null && !name.isEmpty()) {
                meta.setDisplayName(ColorUtils.color(name));
            }
            if (enchant != null && !enchant.isEmpty()) {
                for (String encStr : enchant.split(";")) {
                    String[] parts = encStr.split(":");
                    if (parts.length == 2) {
                        try {
                            Enchantment enc = Enchantment.getByName(parts[0].toUpperCase());
                            int lvl = Integer.parseInt(parts[1]);
                            if (enc != null) {
                                meta.addEnchant(enc, lvl, true);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void giveReward(Player player, TekBlokPlugin plugin) {
        if ("MONEY".equalsIgnoreCase(type)) {
            plugin.getEconomyManager().addBalance(player.getUniqueId(), amount);
            player.sendMessage(ColorUtils.color("&6&l[KASA] &aTebrikler! Hesabınıza &e" + plugin.getEconomyManager().format(amount) + " &ayatırıldı!"));
        } else {
            ItemStack toGive = buildItemStack();
            toGive.setAmount(amount);
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(toGive);
            if (!leftover.isEmpty()) {
                for (ItemStack is : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), is);
                }
                player.sendMessage(ColorUtils.color("&eÇantanız dolu olduğu için bazı ödüller yere bırakıldı!"));
            }
            player.sendMessage(ColorUtils.color("&6&l[KASA] &aTebrikler! &e" + (name != null ? name : toGive.getType().name()) + " &akazandınız!"));
        }
    }
}
