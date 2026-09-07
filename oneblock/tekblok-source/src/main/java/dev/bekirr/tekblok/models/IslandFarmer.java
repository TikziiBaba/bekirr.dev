package dev.bekirr.tekblok.models;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class IslandFarmer {

    private final UUID islandOwner;
    private boolean unlocked;
    private boolean autoCollect;
    private boolean collectGroundDrops = true;
    private final Map<Material, Integer> storage = new EnumMap<>(Material.class);
    private double totalEarned;
    private Location npcLocation;

    public IslandFarmer(UUID islandOwner) {
        this(islandOwner, false, true, true, 0.0, null);
    }

    public IslandFarmer(UUID islandOwner, boolean unlocked, boolean autoCollect, double totalEarned, Location npcLocation) {
        this(islandOwner, unlocked, autoCollect, true, totalEarned, npcLocation);
    }

    public IslandFarmer(UUID islandOwner, boolean unlocked, boolean autoCollect, boolean collectGroundDrops, double totalEarned, Location npcLocation) {
        this.islandOwner = islandOwner;
        this.unlocked = unlocked;
        this.autoCollect = autoCollect;
        this.collectGroundDrops = collectGroundDrops;
        this.totalEarned = totalEarned;
        this.npcLocation = npcLocation;
    }

    public UUID getIslandOwner() {
        return islandOwner;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean isAutoCollect() {
        return autoCollect;
    }

    public void setAutoCollect(boolean autoCollect) {
        this.autoCollect = autoCollect;
    }

    public boolean isCollectGroundDrops() {
        return collectGroundDrops;
    }

    public void setCollectGroundDrops(boolean collectGroundDrops) {
        this.collectGroundDrops = collectGroundDrops;
    }

    public double getTotalEarned() {
        return totalEarned;
    }

    public void addTotalEarned(double earned) {
        this.totalEarned += earned;
    }

    public Location getNpcLocation() {
        return npcLocation;
    }

    public void setNpcLocation(Location npcLocation) {
        this.npcLocation = npcLocation;
    }

    public Map<Material, Integer> getStorage() {
        return storage;
    }

    public int getItemCount(Material mat) {
        return storage.getOrDefault(mat, 0);
    }

    public void addItem(Material mat, int amount) {
        if (amount <= 0 || mat == null) return;
        int current = getItemCount(mat);
        storage.put(mat, current + amount);
    }

    public void removeItem(Material mat, int amount) {
        if (amount <= 0 || mat == null) return;
        int current = getItemCount(mat);
        if (current <= amount) {
            storage.remove(mat);
        } else {
            storage.put(mat, current - amount);
        }
    }

    public void clearStorage() {
        storage.clear();
    }

    public boolean isEmpty() {
        return storage.isEmpty();
    }

    public int getTotalItemCount() {
        int sum = 0;
        for (int count : storage.values()) {
            sum += count;
        }
        return sum;
    }
}
