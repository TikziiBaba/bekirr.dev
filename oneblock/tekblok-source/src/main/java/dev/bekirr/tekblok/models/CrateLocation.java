package dev.bekirr.tekblok.models;

import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class CrateLocation {

    private final String id;
    private final String crateType;
    private final Location location;
    private final List<ArmorStand> holograms = new ArrayList<>();

    public CrateLocation(String id, String crateType, Location location) {
        this.id = id;
        this.crateType = crateType.toLowerCase();
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getCrateType() {
        return crateType;
    }

    public Location getLocation() {
        return location;
    }

    public List<ArmorStand> getHolograms() {
        return holograms;
    }

    public void clearHolograms() {
        for (ArmorStand as : holograms) {
            if (as != null && as.isValid()) {
                as.remove();
            }
        }
        holograms.clear();

        // Also clean up any orphan armor stands at this exact location
        if (location.getWorld() != null) {
            Location checkLoc = location.clone().add(0.5, 1.0, 0.5);
            for (Entity e : location.getWorld().getNearbyEntities(checkLoc, 1.5, 3.0, 1.5)) {
                if (e instanceof ArmorStand && ((ArmorStand) e).isMarker()) {
                    e.remove();
                }
            }
        }
    }

    public void spawnHolograms(Crate crate) {
        clearHolograms();
        if (location.getWorld() == null || crate == null) return;

        List<String> lines = crate.getHologramLines();
        if (lines.isEmpty()) return;

        // Sandığın üstüne satır satır yerleştir (en üstten en alta)
        double startY = location.getY() + 1.2 + (lines.size() * 0.28);

        for (int i = 0; i < lines.size(); i++) {
            Location holoLoc = new Location(location.getWorld(), location.getBlockX() + 0.5, startY - (i * 0.28), location.getBlockZ() + 0.5);
            ArmorStand as = location.getWorld().spawn(holoLoc, ArmorStand.class);
            as.setVisible(false);
            as.setGravity(false);
            as.setMarker(true);
            as.setInvulnerable(true);
            as.setCustomName(ColorUtils.color(lines.get(i)));
            as.setCustomNameVisible(true);
            holograms.add(as);
        }
    }
}
