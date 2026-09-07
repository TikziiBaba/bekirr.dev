package dev.bekirr.tekblok.listeners;

import dev.bekirr.tekblok.TekBlokPlugin;
import dev.bekirr.tekblok.gui.CratePreviewGUI;
import dev.bekirr.tekblok.models.Crate;
import dev.bekirr.tekblok.models.CrateLocation;
import dev.bekirr.tekblok.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CrateListener implements Listener {

    private final TekBlokPlugin plugin;

    public CrateListener(TekBlokPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Sadece ana el etkileşimini dinle (çift tetiklenmeyi önler)
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        CrateLocation cl = plugin.getCrateManager().findCrateAt(block.getLocation());
        if (cl == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        Crate crate = plugin.getCrateManager().getCrate(cl.getCrateType());
        if (crate == null) return;

        // 1. SOL TIK: ÖDÜLLERİ İNCELEME
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            CratePreviewGUI.open(player, crate);
            return;
        }

        // 2. SAĞ TIK: KASAYI AÇMA
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Anahtarı kontrol et (Önce elindeki, sonra envanterindeki)
            ItemStack handItem = player.getInventory().getItemInMainHand();
            boolean hasInHand = crate.isMatchingKey(handItem, plugin);

            if (hasInHand) {
                handItem.setAmount(handItem.getAmount() - 1);
                plugin.getCrateManager().startOpening(player, crate, cl.getLocation());
                return;
            }

            // Envanterin geri kalanını tara
            int keySlot = -1;
            ItemStack[] contents = player.getInventory().getStorageContents();
            for (int i = 0; i < contents.length; i++) {
                if (crate.isMatchingKey(contents[i], plugin)) {
                    keySlot = i;
                    break;
                }
            }

            if (keySlot != -1) {
                ItemStack keyStack = contents[keySlot];
                if (keyStack.getAmount() > 1) {
                    keyStack.setAmount(keyStack.getAmount() - 1);
                } else {
                    player.getInventory().setItem(keySlot, null);
                }
                plugin.getCrateManager().startOpening(player, crate, cl.getLocation());
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1.0f, 1.0f);
                block.getWorld().spawnParticle(Particle.SMOKE_NORMAL, block.getLocation().add(0.5, 1.0, 0.5), 10, 0.2, 0.2, 0.2, 0.05);
                player.sendMessage(ColorUtils.color("&cBu kasayı açmak için &e" + crate.getKeyName() + " &cgereklidir!"));
                player.sendMessage(ColorUtils.color("&7Anahtarı &6/market &7menüsünden satın alabilir veya görevlerden kazanabilirsiniz."));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getCrateManager().findCrateAt(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ColorUtils.color("&cKurulu kasalar kırılamaz! Kaldırmak için &e/kasa sil &ckullanın."));
        }
    }
}
