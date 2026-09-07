package dev.bekirr.tekblok.models;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PhaseChest {

    private final int weight;
    private final List<ItemStack> items;

    public PhaseChest(int weight, List<ItemStack> items) {
        this.weight = Math.max(1, weight);
        this.items = items != null ? items : new ArrayList<>();
    }

    public int getWeight() {
        return weight;
    }

    public List<ItemStack> getItems() {
        return items;
    }
}
