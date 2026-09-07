package dev.bekirr.tekblok.managers;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.models.TradeSession;
import dev.bekirr.tekblok.utils.ColorUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeManager {

    private final TekBlokPlugin plugin;
    private final Map<UUID, UUID> pendingRequests = new HashMap<>(); // Target -> Sender
    private final Map<UUID, Long> requestTimestamps = new HashMap<>();
    private final Map<UUID, TradeSession> activeSessions = new HashMap<>();

    public TradeManager(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isInTrade(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public TradeSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    public void sendRequest(Player sender, Player target) {
        if (sender.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(ColorUtils.color("&cKendinizle takas yapamazsınız!"));
            return;
        }

        if (isInTrade(sender)) {
            sender.sendMessage(ColorUtils.color("&cZaten aktif bir takas oturumundasınız!"));
            return;
        }

        if (isInTrade(target)) {
            sender.sendMessage(ColorUtils.color("&cBu oyuncu şu anda başka bir takas oturumunda!"));
            return;
        }

        pendingRequests.put(target.getUniqueId(), sender.getUniqueId());
        requestTimestamps.put(target.getUniqueId(), System.currentTimeMillis());

        sender.sendMessage(ColorUtils.color("&a" + target.getName() + " &7adlı oyuncuya takas teklifi gönderildi. Yanıt bekleniyor..."));
        sender.playSound(sender.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);

        // Butonlu İnteraktif Mesaj
        TextComponent message = new TextComponent(ColorUtils.color(
                "&6===== &e&lTekBlok Güvenli Eşya Takası &6=====\n" +
                "&e" + sender.getName() + " &7sizinle eşya takası yapmak istiyor!\n" +
                "&7Kabul etmek veya reddetmek için aşağıdaki butonlara tıklayın:\n\n"
        ));

        TextComponent acceptBtn = new TextComponent(ColorUtils.color("&a&l[ ✔ TAKASI KABUL ET ]"));
        acceptBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/takas kabul " + sender.getName()));
        acceptBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ColorUtils.color("&aTakas menüsünü açmak için tıkla!")).create()));

        TextComponent space = new TextComponent("    ");

        TextComponent denyBtn = new TextComponent(ColorUtils.color("&c&l[ ✖ REDDET ]"));
        denyBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/takas reddet " + sender.getName()));
        denyBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ColorUtils.color("&cTakas teklifini reddetmek için tıkla.")).create()));

        TextComponent footer = new TextComponent(ColorUtils.color("\n\n&7İstek &e60 saniye &7boyunca geçerlidir.\n&6========================================="));

        message.addExtra(acceptBtn);
        message.addExtra(space);
        message.addExtra(denyBtn);
        message.addExtra(footer);

        target.spigot().sendMessage(message);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
    }

    public void acceptRequest(Player target, Player sender) {
        UUID expectedSender = pendingRequests.get(target.getUniqueId());
        if (expectedSender == null || !expectedSender.equals(sender.getUniqueId())) {
            target.sendMessage(ColorUtils.color("&cBekleyen bir takas teklifiniz bulunmuyor."));
            return;
        }

        Long time = requestTimestamps.get(target.getUniqueId());
        if (time == null || System.currentTimeMillis() - time > 60000L) {
            target.sendMessage(ColorUtils.color("&cTakas teklifinin süresi dolmuş!"));
            pendingRequests.remove(target.getUniqueId());
            requestTimestamps.remove(target.getUniqueId());
            return;
        }

        if (isInTrade(sender) || isInTrade(target)) {
            target.sendMessage(ColorUtils.color("&cOyunculardan biri zaten takasta!"));
            return;
        }

        pendingRequests.remove(target.getUniqueId());
        requestTimestamps.remove(target.getUniqueId());

        TradeSession session = new TradeSession(plugin, sender, target);
        activeSessions.put(sender.getUniqueId(), session);
        activeSessions.put(target.getUniqueId(), session);

        session.open();
    }

    public void denyRequest(Player target, Player sender) {
        UUID expectedSender = pendingRequests.get(target.getUniqueId());
        if (expectedSender == null || !expectedSender.equals(sender.getUniqueId())) {
            target.sendMessage(ColorUtils.color("&cBekleyen bir takas teklifiniz bulunmuyor."));
            return;
        }

        pendingRequests.remove(target.getUniqueId());
        requestTimestamps.remove(target.getUniqueId());

        target.sendMessage(ColorUtils.color("&cTakas teklifini reddettiniz."));
        sender.sendMessage(ColorUtils.color("&c" + target.getName() + " takas teklifinizi reddetti."));
        sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }

    public void endSession(TradeSession session) {
        if (session == null) return;
        activeSessions.remove(session.getPlayer1().getUniqueId());
        activeSessions.remove(session.getPlayer2().getUniqueId());
    }
}
