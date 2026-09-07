package dev.bekirr.tekblok.models;

import org.bukkit.Material;

public class PhaseBlock {

    private final Material material;
    private final int weight;

    public PhaseBlock(Material material, int weight) {
        this.material = material;
        this.weight = Math.max(1, weight);
    }

    public Material getMaterial() {
        return material;
    }

    public int getWeight() {
        return weight;
    }
}
