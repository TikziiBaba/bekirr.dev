package dev.bekirr.tekblok.models;

import org.bukkit.Material;

public class MarketItem {

    private final Material material;
    private final String name;
    private final double price;
    private final String crateId;

    public MarketItem(Material material, String name, double price) {
        this(material, name, price, null);
    }

    public MarketItem(Material material, String name, double price, String crateId) {
        this.material = material;
        this.name = name;
        this.price = price;
        this.crateId = crateId;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getCrateId() {
        return crateId;
    }

    public boolean isCrateKey() {
        return crateId != null && !crateId.isEmpty();
    }
}
