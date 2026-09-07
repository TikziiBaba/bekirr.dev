package dev.bekirr.tekblok.models;

import org.bukkit.Material;

import java.util.List;

public class MarketCategory {

    private final String id;
    private final String name;
    private final Material icon;
    private final int slot;
    private final String description;
    private final List<MarketItem> items;

    public MarketCategory(String id, String name, Material icon, int slot, String description, List<MarketItem> items) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.slot = slot;
        this.description = description;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }

    public int getSlot() {
        return slot;
    }

    public String getDescription() {
        return description;
    }

    public List<MarketItem> getItems() {
        return items;
    }
}
