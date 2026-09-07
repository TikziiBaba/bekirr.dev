package dev.bekirr.tekblok.models;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Phase {

    private final int id;
    private final String name;
    private final int requiredBlocks;
    private final BarColor barColor;
    private final BarStyle barStyle;
    private final Material icon;
    private final double chestChance;
    private final double mobChance;
    private final Biome biome;

    private final List<PhaseBlock> blocks = new ArrayList<>();
    private final List<PhaseChest> chests = new ArrayList<>();
    private final List<PhaseMob> mobs = new ArrayList<>();

    private int totalBlockWeight = 0;
    private int totalChestWeight = 0;
    private int totalMobWeight = 0;

    public Phase(int id, String name, int requiredBlocks, BarColor barColor, BarStyle barStyle, Material icon, double chestChance, double mobChance) {
        this(id, name, requiredBlocks, barColor, barStyle, icon, chestChance, mobChance, Biome.PLAINS);
    }

    public Phase(int id, String name, int requiredBlocks, BarColor barColor, BarStyle barStyle, Material icon, double chestChance, double mobChance, Biome biome) {
        this.id = id;
        this.name = name;
        this.requiredBlocks = requiredBlocks;
        this.barColor = barColor;
        this.barStyle = barStyle;
        this.icon = icon != null ? icon : Material.GRASS_BLOCK;
        this.chestChance = chestChance;
        this.mobChance = mobChance;
        this.biome = biome != null ? biome : Biome.PLAINS;
    }

    public void addBlock(PhaseBlock block) {
        blocks.add(block);
        totalBlockWeight += block.getWeight();
    }

    public void addChest(PhaseChest chest) {
        chests.add(chest);
        totalChestWeight += chest.getWeight();
    }

    public void addMob(PhaseMob mob) {
        mobs.add(mob);
        totalMobWeight += mob.getWeight();
    }

    public Material getRandomBlock(Random random) {
        if (blocks.isEmpty()) {
            return Material.GRASS_BLOCK;
        }
        if (totalBlockWeight <= 0) {
            return blocks.get(0).getMaterial();
        }

        int target = random.nextInt(totalBlockWeight);
        int current = 0;
        for (PhaseBlock block : blocks) {
            current += block.getWeight();
            if (target < current) {
                return block.getMaterial();
            }
        }
        return blocks.get(0).getMaterial();
    }

    public PhaseChest getRandomChest(Random random) {
        if (chests.isEmpty()) {
            return null;
        }
        if (totalChestWeight <= 0) {
            return chests.get(0);
        }

        int target = random.nextInt(totalChestWeight);
        int current = 0;
        for (PhaseChest chest : chests) {
            current += chest.getWeight();
            if (target < current) {
                return chest;
            }
        }
        return chests.get(0);
    }

    public PhaseMob getRandomMob(Random random) {
        if (mobs.isEmpty()) {
            return null;
        }
        if (totalMobWeight <= 0) {
            return mobs.get(0);
        }

        int target = random.nextInt(totalMobWeight);
        int current = 0;
        for (PhaseMob mob : mobs) {
            current += mob.getWeight();
            if (target < current) {
                return mob;
            }
        }
        return mobs.get(0);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getRequiredBlocks() {
        return requiredBlocks;
    }

    public BarColor getBarColor() {
        return barColor;
    }

    public BarStyle getBarStyle() {
        return barStyle;
    }

    public Material getIcon() {
        return icon;
    }

    public double getChestChance() {
        return chestChance;
    }

    public double getMobChance() {
        return mobChance;
    }

    public List<PhaseBlock> getBlocks() {
        return blocks;
    }

    public List<PhaseChest> getChests() {
        return chests;
    }

    public List<PhaseMob> getMobs() {
        return mobs;
    }

    public Biome getBiome() {
        return biome != null ? biome : Biome.PLAINS;
    }
}
