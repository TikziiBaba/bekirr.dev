package dev.bekirr.tekblok.models;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.UUID;

public class Minion {

    private final UUID ownerUUID;
    private Location location;
    private BlockFace facing;
    private int level;
    private int blocksBroken;
    private UUID armorStandUUID;

    public Minion(UUID ownerUUID, Location location, BlockFace facing, int level, int blocksBroken, UUID armorStandUUID) {
        this.ownerUUID = ownerUUID;
        this.location = location;
        this.facing = facing != null ? facing : BlockFace.NORTH;
        this.level = Math.max(1, Math.min(3, level));
        this.blocksBroken = Math.max(0, blocksBroken);
        this.armorStandUUID = armorStandUUID;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public BlockFace getFacing() {
        return facing;
    }

    public void setFacing(BlockFace facing) {
        this.facing = facing;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(3, level));
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    public void setBlocksBroken(int blocksBroken) {
        this.blocksBroken = blocksBroken;
    }

    public void incrementBlocksBroken() {
        this.blocksBroken++;
    }

    public UUID getArmorStandUUID() {
        return armorStandUUID;
    }

    public void setArmorStandUUID(UUID armorStandUUID) {
        this.armorStandUUID = armorStandUUID;
    }
}
