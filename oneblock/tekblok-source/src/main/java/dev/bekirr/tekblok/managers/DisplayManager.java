package dev.bekirr.tekblok.managers;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.Phase;
import dev.bekirr.tekblok.utils.ColorUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DisplayManager {

    private final TekBlokPlugin plugin;
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    public DisplayManager(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    public void updateBossBar(Island island) {
        if (!plugin.getConfig().getBoolean("display.bossbar.enabled", true)) {
            return;
        }

        Phase phase = plugin.getPhaseManager().getPhase(island.getCurrentPhaseId());
        Phase prevPhase = island.getCurrentPhaseId() > 1 ? plugin.getPhaseManager().getPhase(island.getCurrentPhaseId() - 1) : null;

        int prevReq = prevPhase != null ? prevPhase.getRequiredBlocks() : 0;
        int targetReq = phase.getRequiredBlocks();
        int span = Math.max(1, targetReq - prevReq);
        int currentInSpan = Math.max(0, island.getBlocksBroken() - prevReq);

        double progress = Math.min(1.0, Math.max(0.0, (double) currentInSpan / span));
        int percent = (int) (progress * 100);

        String titleFormat = plugin.getConfig().getString("display.bossbar.format", "&6{phase_name} &8[&a{broken}&7/&c{required}&8] &e%{percent}");
        String title = ColorUtils.color(titleFormat
                .replace("{phase_id}", String.valueOf(phase.getId()))
                .replace("{phase_name}", phase.getName())
                .replace("{broken}", String.valueOf(island.getBlocksBroken()))
                .replace("{required}", String.valueOf(targetReq))
                .replace("{percent}", String.valueOf(percent)));

        // Sahip için
        updatePlayerBossBar(island.getOwnerUUID(), title, progress, phase.getBarColor(), phase.getBarStyle());

        // Üyeler için
        for (UUID memberId : island.getMembers()) {
            updatePlayerBossBar(memberId, title, progress, phase.getBarColor(), phase.getBarStyle());
        }
    }

    private void updatePlayerBossBar(UUID uuid, String title, double progress, BarColor color, BarStyle style) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }

        BossBar bar = bossBars.computeIfAbsent(uuid, k -> {
            BossBar b = Bukkit.createBossBar(title, color, style);
            b.addPlayer(player);
            return b;
        });

        bar.setTitle(title);
        bar.setColor(color);
        bar.setStyle(style);
        bar.setProgress(progress);
        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
        bar.setVisible(true);
    }

    public void sendActionBar(Player player, Island island) {
        if (!plugin.getConfig().getBoolean("display.actionbar.enabled", true)) {
            return;
        }

        Phase phase = plugin.getPhaseManager().getPhase(island.getCurrentPhaseId());
        Phase prevPhase = island.getCurrentPhaseId() > 1 ? plugin.getPhaseManager().getPhase(island.getCurrentPhaseId() - 1) : null;

        int prevReq = prevPhase != null ? prevPhase.getRequiredBlocks() : 0;
        int targetReq = phase.getRequiredBlocks();
        int span = Math.max(1, targetReq - prevReq);
        int currentInSpan = Math.max(0, island.getBlocksBroken() - prevReq);
        int percent = (int) (Math.min(1.0, (double) currentInSpan / span) * 100);

        String format = plugin.getConfig().getString("display.actionbar.format", "&a+1 Blok &8| &eAşama {phase_id}: {phase_name} &8| &b{broken}&7/&b{required} &8(&e%{percent}&8)");
        String message = ColorUtils.color(format
                .replace("{phase_id}", String.valueOf(phase.getId()))
                .replace("{phase_name}", phase.getName())
                .replace("{broken}", String.valueOf(island.getBlocksBroken()))
                .replace("{required}", String.valueOf(targetReq))
                .replace("{percent}", String.valueOf(percent)));

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    public void removePlayer(UUID uuid) {
        BossBar bar = bossBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }

    public void removeAll() {
        for (BossBar bar : bossBars.values()) {
            bar.removeAll();
        }
        bossBars.clear();
    }
}
