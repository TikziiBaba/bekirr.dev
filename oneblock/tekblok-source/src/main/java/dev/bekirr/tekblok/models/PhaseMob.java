package dev.bekirr.tekblok.models;

import org.bukkit.entity.EntityType;

public class PhaseMob {

    private final EntityType entityType;
    private final int weight;
    private final String customName;
    private final boolean baby;

    public PhaseMob(EntityType entityType, int weight, String customName, boolean baby) {
        this.entityType = entityType;
        this.weight = Math.max(1, weight);
        this.customName = customName;
        this.baby = baby;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public int getWeight() {
        return weight;
    }

    public String getCustomName() {
        return customName;
    }

    public boolean isBaby() {
        return baby;
    }
}
