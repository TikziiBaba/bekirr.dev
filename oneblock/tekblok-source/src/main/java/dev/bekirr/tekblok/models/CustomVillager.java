package dev.bekirr.tekblok.models;

import org.bukkit.Location;
import java.util.UUID;

public class CustomVillager {

    private final String id;
    private final String type; // "eritici" veya "takasci"
    private final Location location;
    private UUID entityUUID;

    public CustomVillager(String id, String type, Location location, UUID entityUUID) {
        this.id = id;
        this.type = type.toLowerCase();
        this.location = location;
        this.entityUUID = entityUUID;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public UUID getEntityUUID() {
        return entityUUID;
    }

    public void setEntityUUID(UUID entityUUID) {
        this.entityUUID = entityUUID;
    }
}
