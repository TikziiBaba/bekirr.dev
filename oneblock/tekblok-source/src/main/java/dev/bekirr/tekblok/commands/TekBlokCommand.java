package dev.bekirr.tekblok.commands;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.gui.FarmerGUI;
import dev.bekirr.tekblok.gui.IslandGUI;
import dev.bekirr.tekblok.gui.MarketGUI;
import dev.bekirr.tekblok.models.Island;
import dev.bekirr.tekblok.models.IslandFarmer;
import dev.bekirr.tekblok.models.Phase;
import dev.bekirr.tekblok.utils.ColorUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TekBlokCommand implements CommandExecutor, TabCompleter {

    private final TekBlokPlugin plugin;

    public TekBlokCommand(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("messages.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            IslandGUI.openMainMenu(player, plugin);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "menu":
            case "gui":
                IslandGUI.openMainMenu(player, plugin);
                return true;

            case "ciftci":
            case "farmer":
                Island cIsland = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
                if (cIsland == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.no-island"));
                    return true;
                }
                IslandFarmer cFarmer = plugin.getFarmerManager().getFarmer(cIsland.getOwnerUUID());
                if (!cFarmer.isUnlocked()) {
                    FarmerGUI.openFarmerBuyMenu(player, plugin, cIsland);
                } else {
                    FarmerGUI.openFarmerMenu(player, plugin, cIsland);
                }
                return true;

            case "market":
            case "shop":
                MarketGUI.openMainMenu(player, plugin);
                return true;

            case "takas":
            case "trade":
                if (args.length < 2) {
                    player.sendMessage(ColorUtils.color("&cKullanım: &e/ada takas <oyuncu> &7veya &e/takas <oyuncu>"));
                    return true;
                }
                Player tPlayer = Bukkit.getPlayer(args[1]);
                if (tPlayer == null || !tPlayer.isOnline()) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.player-not-found"));
                    return true;
                }
                plugin.getTradeManager().sendRequest(player, tPlayer);
                return true;

            case "bakiye":
            case "para":
            case "bal":
            case "money":
                double pBal = plugin.getEconomyManager().getBalance(player.getUniqueId());
                player.sendMessage(plugin.getConfigManager().getMessage("messages.balance-self")
                        .replace("{balance}", plugin.getEconomyManager().format(pBal)));
                return true;

            case "git":
            case "go":
            case "tp":
                Island island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
                if (island == null) {
                    island = plugin.getIslandManager().createIsland(player);
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.island-created"));
                } else {
                    plugin.getIslandManager().ensureOneBlockExists(island);
                }
                player.teleport(island.getSpawnLocation());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.sendMessage(plugin.getConfigManager().getMessage("messages.teleported-to-island"));
                plugin.getDisplayManager().updateBossBar(island);
                return true;

            case "olustur":
            case "create":
                Island existing = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
                if (existing != null) {
                    if (existing.getOwnerUUID().equals(player.getUniqueId())) {
                        player.sendMessage(ColorUtils.color("&cZaten bir adanız var! Adanıza gitmek için: &e/ada git"));
                    } else {
                        player.sendMessage(ColorUtils.color("&cZaten &e" + existing.getOwnerName() + " &cadlı oyuncunun adasında üyesiniz! Yeni ada oluşturamazsınız. (Ayrılmak için: &e/ada ayril&c)"));
                    }
                    return true;
                }
                Island newIsland = plugin.getIslandManager().createIsland(player);
                player.sendMessage(plugin.getConfigManager().getMessage("messages.island-created"));
                player.teleport(newIsland.getSpawnLocation());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.sendMessage(plugin.getConfigManager().getMessage("messages.teleported-to-island"));
                plugin.getDisplayManager().updateBossBar(newIsland);
                return true;

            case "seviye":
            case "bilgi":
            case "info":
                Island myIsland = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
                if (myIsland == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.no-island"));
                    return true;
                }
                Phase phase = plugin.getPhaseManager().getPhase(myIsland.getCurrentPhaseId());
                int remaining = Math.max(0, phase.getRequiredBlocks() - myIsland.getBlocksBroken());

                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.info-header"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.info-phase")
                        .replace("{phase_id}", String.valueOf(phase.getId()))
                        .replace("{phase_name}", phase.getName()));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.info-blocks")
                        .replace("{broken}", String.valueOf(myIsland.getBlocksBroken()))
                        .replace("{required}", String.valueOf(phase.getRequiredBlocks())));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.info-remaining")
                        .replace("{remaining}", String.valueOf(remaining)));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.info-footer"));
                return true;

            case "davet":
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.invite-usage"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.player-not-found"));
                    return true;
                }
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.cannot-invite-self"));
                    return true;
                }
                Island hostIsland = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
                if (hostIsland == null || !hostIsland.getOwnerUUID().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.must-be-island-owner"));
                    return true;
                }

                plugin.getIslandManager().invitePlayer(player, target);
                player.sendMessage(plugin.getConfigManager().getMessage("messages.invite-sent").replace("{player}", target.getName()));

                // Davet alan oyuncuya butonlu interaktif mesaj gönder
                target.sendMessage(" ");
                target.sendMessage(plugin.getConfigManager().getMessage("messages.invite-received").replace("{player}", player.getName()));

                TextComponent acceptBtn = new TextComponent(ColorUtils.color("&a&l[✔ KABUL ET]"));
                acceptBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ada kabul"));
                acceptBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ColorUtils.color("&aDaveti kabul edip adaya katılmak için tıklayın!")).create()));

                TextComponent denyBtn = new TextComponent(ColorUtils.color("&c&l[✖ REDDET]"));
                denyBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ada reddet"));
                denyBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ColorUtils.color("&cDaveti geri çevirmek için tıklayın!")).create()));

                TextComponent inviteBar = new TextComponent("   ");
                inviteBar.addExtra(acceptBtn);
                inviteBar.addExtra(new TextComponent("     "));
                inviteBar.addExtra(denyBtn);

                target.spigot().sendMessage(inviteBar);
                target.sendMessage(" ");
                target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                return true;

            case "kabul":
            case "accept":
                if (!plugin.getIslandManager().hasPendingInvite(player.getUniqueId())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.no-pending-invite"));
                    return true;
                }
                UUID inviterOwner = plugin.getIslandManager().getPendingInvite(player.getUniqueId());
                Island inviterIsland = plugin.getIslandManager().getIslandsByOwner().get(inviterOwner);
                if (inviterIsland == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.invite-expired"));
                    plugin.getIslandManager().removePendingInvite(player.getUniqueId());
                    return true;
                }

                plugin.getIslandManager().addMember(inviterIsland, player.getUniqueId());
                plugin.getIslandManager().removePendingInvite(player.getUniqueId());

                player.teleport(inviterIsland.getSpawnLocation());
                player.sendMessage(plugin.getConfigManager().getMessage("messages.invite-accepted").replace("{owner}", inviterIsland.getOwnerName()));

                Player ownerP = Bukkit.getPlayer(inviterOwner);
                if (ownerP != null && ownerP.isOnline()) {
                    ownerP.sendMessage(plugin.getConfigManager().getMessage("messages.member-joined").replace("{player}", player.getName()));
                }
                plugin.getDisplayManager().updateBossBar(inviterIsland);
                return true;

            case "reddet":
            case "deny":
                if (!plugin.getIslandManager().hasPendingInvite(player.getUniqueId())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.no-pending-invite"));
                    return true;
                }
                UUID senderOwner = plugin.getIslandManager().getPendingInvite(player.getUniqueId());
                plugin.getIslandManager().removePendingInvite(player.getUniqueId());
                player.sendMessage(plugin.getConfigManager().getMessage("messages.invite-denied"));

                Player senderP = Bukkit.getPlayer(senderOwner);
                if (senderP != null && senderP.isOnline()) {
                    senderP.sendMessage(plugin.getConfigManager().getMessage("messages.invite-denied-sender").replace("{player}", player.getName()));
                }
                return true;

            case "ayril":
            case "leave":
                Island curIsland = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
                if (curIsland == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.no-island"));
                    return true;
                }
                if (curIsland.getOwnerUUID().equals(player.getUniqueId())) {
                    player.sendMessage(ColorUtils.color("&cAda sahibi sizsiniz, adadan ayrılamazsınız! Adanızı silmek için: &e/ada sil"));
                    return true;
                }
                plugin.getIslandManager().removeMember(curIsland, player.getUniqueId());
                player.sendMessage(ColorUtils.color("&e" + curIsland.getOwnerName() + " &7adlı oyuncunun adasından başarıyla ayrıldınız."));
                Player ownerPlayer = Bukkit.getPlayer(curIsland.getOwnerUUID());
                if (ownerPlayer != null && ownerPlayer.isOnline()) {
                    ownerPlayer.sendMessage(ColorUtils.color("&c" + player.getName() + " &7adanızdan ayrıldı."));
                }
                plugin.getDisplayManager().removePlayer(player.getUniqueId());
                plugin.getSpawnManager().teleportToSpawn(player);
                return true;

            case "at":
            case "kick":
                if (args.length < 2) {
                    player.sendMessage(ColorUtils.color("&cKullanım: &e/ada at <oyuncu>"));
                    return true;
                }
                Island ownIsland = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
                if (ownIsland == null || !ownIsland.getOwnerUUID().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.must-be-island-owner"));
                    return true;
                }
                String kickTargetName = args[1];
                UUID kickTargetUUID = null;
                Player kickTargetPlayer = Bukkit.getPlayer(kickTargetName);
                if (kickTargetPlayer != null) {
                    kickTargetUUID = kickTargetPlayer.getUniqueId();
                } else {
                    for (UUID mUuid : ownIsland.getMembers()) {
                        org.bukkit.OfflinePlayer off = Bukkit.getOfflinePlayer(mUuid);
                        if (off.getName() != null && off.getName().equalsIgnoreCase(kickTargetName)) {
                            kickTargetUUID = mUuid;
                            break;
                        }
                    }
                }
                if (kickTargetUUID == null || !ownIsland.getMembers().contains(kickTargetUUID)) {
                    player.sendMessage(ColorUtils.color("&cBu oyuncu adanızın üyesi değil!"));
                    return true;
                }
                plugin.getIslandManager().removeMember(ownIsland, kickTargetUUID);
                player.sendMessage(ColorUtils.color("&a" + kickTargetName + " &7başarıyla adadan çıkarıldı."));
                if (kickTargetPlayer != null && kickTargetPlayer.isOnline()) {
                    kickTargetPlayer.sendMessage(ColorUtils.color("&c" + player.getName() + " &7sizi adasından çıkardı!"));
                    plugin.getDisplayManager().removePlayer(kickTargetUUID);
                    plugin.getSpawnManager().teleportToSpawn(kickTargetPlayer);
                }
                return true;

            case "sifirla":
            case "reset":
                Island resIsland = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
                if (resIsland == null || !resIsland.getOwnerUUID().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.must-be-island-owner"));
                    return true;
                }
                plugin.getIslandManager().resetIsland(resIsland);
                player.sendMessage(plugin.getConfigManager().getMessage("messages.island-reset"));
                plugin.getDisplayManager().updateBossBar(resIsland);
                return true;

            case "sil":
            case "delete":
                Island delIsland = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
                if (delIsland == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.no-island"));
                    return true;
                }
                if (!delIsland.getOwnerUUID().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.must-be-island-owner"));
                    return true;
                }

                if (args.length >= 2 && args[1].equalsIgnoreCase("onay")) {
                    boolean ok = plugin.getIslandManager().deleteIsland(player.getUniqueId());
                    if (ok) {
                        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.7f, 1.0f);
                    }
                    return true;
                }

                // Onay iste ve interaktif butonlar sun
                player.sendMessage(" ");
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.island-delete-warning"));
                player.sendMessage(ColorUtils.color("&7Bu işlem &cgeri alınamaz&7! Adanız, bloklarınız ve tüm ilerlemeniz tamamen silinecektir."));

                TextComponent confirmBtn = new TextComponent(ColorUtils.color("&4&l[ ⚠ ADAMI SİL VE ONAYLA ]"));
                confirmBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ada sil onay"));
                confirmBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ColorUtils.color("&cAdanızı kalıcı olarak silmek için tıklayın!")).create()));

                TextComponent cancelBtn = new TextComponent(ColorUtils.color("&a&l[ VAZGEÇ ]"));
                cancelBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ada"));
                cancelBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ColorUtils.color("&aİptal et ve menüye dön")).create()));

                TextComponent bar = new TextComponent("   ");
                bar.addExtra(confirmBtn);
                bar.addExtra(new TextComponent("     "));
                bar.addExtra(cancelBtn);

                player.spigot().sendMessage(bar);
                player.sendMessage(" ");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return true;

            case "top":
            case "siralama":
            case "leaderboard":
                List<Island> sorted = new ArrayList<>(plugin.getIslandManager().getIslandsByOwner().values());
                sorted.sort((a, b) -> Integer.compare(b.getBlocksBroken(), a.getBlocksBroken()));

                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.top-header"));
                if (sorted.isEmpty()) {
                    player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.top-empty"));
                } else {
                    int count = Math.min(10, sorted.size());
                    for (int i = 0; i < count; i++) {
                        Island is = sorted.get(i);
                        player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.top-entry")
                                .replace("{rank}", String.valueOf(i + 1))
                                .replace("{player}", is.getOwnerName())
                                .replace("{blocks}", String.format("%,d", is.getBlocksBroken()))
                                .replace("{phase}", String.valueOf(is.getCurrentPhaseId())));
                    }
                }
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.top-footer"));
                return true;

            case "ziyaret":
            case "visit":
                if (args.length < 2) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.visit-usage"));
                    return true;
                }
                Player targetVisit = Bukkit.getPlayer(args[1]);
                UUID targetUUID = null;
                String targetName = args[1];

                if (targetVisit != null && targetVisit.isOnline()) {
                    targetUUID = targetVisit.getUniqueId();
                    targetName = targetVisit.getName();
                } else {
                    for (Island is : plugin.getIslandManager().getIslandsByOwner().values()) {
                        if (is.getOwnerName().equalsIgnoreCase(args[1])) {
                            targetUUID = is.getOwnerUUID();
                            targetName = is.getOwnerName();
                            break;
                        }
                    }
                }

                if (targetUUID == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.player-not-found"));
                    return true;
                }

                if (targetUUID.equals(player.getUniqueId())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.cannot-visit-self"));
                    return true;
                }

                Island visitIsland = plugin.getIslandManager().getIslandByPlayer(targetUUID);
                if (visitIsland == null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("messages.player-has-no-island"));
                    return true;
                }

                player.teleport(visitIsland.getSpawnLocation());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.sendMessage(plugin.getConfigManager().getMessage("messages.visiting-island").replace("{player}", targetName));

                if (targetVisit != null && targetVisit.isOnline()) {
                    targetVisit.sendMessage(plugin.getConfigManager().getMessage("messages.visitor-arrived").replace("{player}", player.getName()));
                }
                return true;

            case "yardim":
            case "help":
            default:
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-header"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-menu"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-go"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-info"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-spawn"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-invite"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-accept"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-deny"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-visit"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-top"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-farmer"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-market"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-trade"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-balance"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-reset"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-delete"));
                player.sendMessage(plugin.getConfigManager().getMessageNoPrefix("messages.help-footer"));
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = Arrays.asList(
                    "menu", "git", "olustur", "ayril", "at", "seviye", "bilgi",
                    "ciftci", "market", "takas", "bakiye", "para", "davet", "kabul", "reddet",
                    "ziyaret", "top", "siralama", "sifirla", "sil", "yardim"
            );
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("davet") || args[0].equalsIgnoreCase("at") || args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("ziyaret") || args[0].equalsIgnoreCase("visit") || args[0].equalsIgnoreCase("takas") || args[0].equalsIgnoreCase("trade"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("sil") || args[0].equalsIgnoreCase("delete"))) {
            return Arrays.asList("onay").stream().filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
