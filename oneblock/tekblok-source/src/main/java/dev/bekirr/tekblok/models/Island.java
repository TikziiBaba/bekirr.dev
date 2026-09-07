package dev.bekirr.tekblok.models;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Island {

    private final UUID ownerUUID;
    private String ownerName;
    private final Location oneBlockLocation;
    private final Location spawnLocation;
    private int blocksBroken;
    private int currentPhaseId;
    private final Set<UUID> members;
    private final long createdAt;
    private boolean endPortalBuilt;

    public Island(UUID ownerUUID, String ownerName, Location oneBlockLocation, Location spawnLocation, int blocksBroken, int currentPhaseId, Set<UUID> members, long createdAt) {
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.oneBlockLocation = oneBlockLocation;
        this.spawnLocation = spawnLocation;
        this.blocksBroken = blocksBroken;
        this.currentPhaseId = Math.max(1, currentPhaseId);
        this.members = members != null ? members : new HashSet<>();
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
        this.endPortalBuilt = false;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Location getOneBlockLocation() {
        return oneBlockLocation;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    public void setBlocksBroken(int blocksBroken) {
        this.blocksBroken = Math.max(0, blocksBroken);
    }

    public void incrementBlocksBroken() {
        this.blocksBroken++;
    }

    public int getCurrentPhaseId() {
        return currentPhaseId;
    }

    public void setCurrentPhaseId(int currentPhaseId) {
        this.currentPhaseId = Math.max(1, currentPhaseId);
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public boolean isMemberOrOwner(UUID uuid) {
        if (ownerUUID.equals(uuid)) {
            return true;
        }
        return members.contains(uuid);
    }

    public boolean addMember(UUID uuid) {
        return members.add(uuid);
    }

    public boolean removeMember(UUID uuid) {
        return members.remove(uuid);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isEndPortalBuilt() {
        return endPortalBuilt;
    }

    public void setEndPortalBuilt(boolean endPortalBuilt) {
        this.endPortalBuilt = endPortalBuilt;
    }

    public boolean isOneBlock(Location loc) {
        if (loc == null || oneBlockLocation == null) {
            return false;
        }
        if (loc.getWorld() == null || oneBlockLocation.getWorld() == null) {
            return false;
        }
        if (!loc.getWorld().getName().equalsIgnoreCase(oneBlockLocation.getWorld().getName())) {
            return false;
        }
        return loc.getBlockX() == oneBlockLocation.getBlockX()
                && loc.getBlockY() == oneBlockLocation.getBlockY()
                && loc.getBlockZ() == oneBlockLocation.getBlockZ();
    }
}
