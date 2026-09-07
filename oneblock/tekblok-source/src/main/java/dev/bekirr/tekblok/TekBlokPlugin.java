package dev.bekirr.tekblok;

import dev.bekirr.tekblok.commands.CrateCommand;
import dev.bekirr.tekblok.commands.EconomyCommand;
import dev.bekirr.tekblok.commands.FarmerCommand;
import dev.bekirr.tekblok.commands.MarketCommand;
import dev.bekirr.tekblok.commands.SpawnCommand;
import dev.bekirr.tekblok.commands.TekBlokAdminCommand;
import dev.bekirr.tekblok.commands.TekBlokCommand;
import dev.bekirr.tekblok.commands.TradeCommand;
import dev.bekirr.tekblok.commands.VillagerCommand;
import dev.bekirr.tekblok.generator.VoidChunkGenerator;
import dev.bekirr.tekblok.listeners.BlockBreakListener;
import dev.bekirr.tekblok.listeners.BlockProtectListener;
import dev.bekirr.tekblok.listeners.CrateListener;
import dev.bekirr.tekblok.listeners.FarmerHarvestListener;
import dev.bekirr.tekblok.listeners.InventoryClickListener;
import dev.bekirr.tekblok.listeners.PlayerConnectionListener;
import dev.bekirr.tekblok.listeners.SpawnListener;
import dev.bekirr.tekblok.listeners.SpawnerListener;
import dev.bekirr.tekblok.listeners.TradeListener;
import dev.bekirr.tekblok.listeners.VillagerListener;
import dev.bekirr.tekblok.listeners.VoidFallListener;
import dev.bekirr.tekblok.managers.ConfigManager;
import dev.bekirr.tekblok.managers.CrateManager;
import dev.bekirr.tekblok.managers.MinionManager;
import dev.bekirr.tekblok.listeners.MinionListener;
import dev.bekirr.tekblok.commands.MinionCommand;
import dev.bekirr.tekblok.managers.DisplayManager;
import dev.bekirr.tekblok.managers.EconomyManager;
import dev.bekirr.tekblok.managers.FarmerManager;
import dev.bekirr.tekblok.managers.IslandManager;
import dev.bekirr.tekblok.managers.MarketManager;
import dev.bekirr.tekblok.managers.PhaseManager;
import dev.bekirr.tekblok.managers.SpawnManager;
import dev.bekirr.tekblok.managers.TradeManager;
import dev.bekirr.tekblok.managers.VillagerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import dev.bekirr.tekblok.utils.ColorUtils;

import org.bukkit.plugin.java.JavaPlugin;

public class TekBlokPlugin extends JavaPlugin {

    private static TekBlokPlugin instance;
    private ConfigManager configManager;
    private PhaseManager phaseManager;
    private DisplayManager displayManager;
    private IslandManager islandManager;
    private EconomyManager economyManager;
    private FarmerManager farmerManager;
    private MarketManager marketManager;
    private TradeManager tradeManager;
    private SpawnManager spawnManager;
    private VillagerManager villagerManager;
    private CrateManager crateManager;
    private MinionManager minionManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("=========================================");
        getLogger().info(" TekBlok (OneBlock Skyblock) 1.16.1");
        getLogger().info(" Geliştirici: Bekir");
        getLogger().info(" Durum: Etkinleştiriliyor...");
        getLogger().info("=========================================");

        // 1. Yapılandırma ve Yöneticiler
        this.configManager = new ConfigManager(this);
        this.phaseManager = new PhaseManager(this);
        this.displayManager = new DisplayManager(this);
        this.islandManager = new IslandManager(this);
        this.economyManager = new EconomyManager(this);
        this.farmerManager = new FarmerManager(this);
        this.marketManager = new MarketManager(this);
        this.tradeManager = new TradeManager(this);
        this.spawnManager = new SpawnManager(this);
        this.villagerManager = new VillagerManager(this);
        this.crateManager = new CrateManager(this);
        this.minionManager = new MinionManager(this);

        // 2. Dinleyiciler (Listeners)
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockProtectListener(this), this);
        getServer().getPluginManager().registerEvents(new VoidFallListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new FarmerHarvestListener(this), this);
        getServer().getPluginManager().registerEvents(new TradeListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new VillagerListener(this), this);
        getServer().getPluginManager().registerEvents(new CrateListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnerListener(this), this);
        getServer().getPluginManager().registerEvents(new MinionListener(this), this);

        // 3. Komutlar
        PluginCommand adaCmd = getCommand("ada");
        if (adaCmd != null) {
            TekBlokCommand cmd = new TekBlokCommand(this);
            adaCmd.setExecutor(cmd);
            adaCmd.setTabCompleter(cmd);
        }

        PluginCommand obCmd = getCommand("tekblok");
        if (obCmd != null) {
            TekBlokCommand cmd = new TekBlokCommand(this);
            obCmd.setExecutor(cmd);
            obCmd.setTabCompleter(cmd);
        }

        PluginCommand adminCmd = getCommand("obadmin");
        if (adminCmd != null) {
            TekBlokAdminCommand aCmd = new TekBlokAdminCommand(this);
            adminCmd.setExecutor(aCmd);
            adminCmd.setTabCompleter(aCmd);
        }

        PluginCommand spawnCmd = getCommand("spawn");
        if (spawnCmd != null) {
            SpawnCommand sCmd = new SpawnCommand(this);
            spawnCmd.setExecutor(sCmd);
            spawnCmd.setTabCompleter(sCmd);
        }

        PluginCommand setspawnCmd = getCommand("setspawn");
        if (setspawnCmd != null) {
            SpawnCommand sCmd = new SpawnCommand(this);
            setspawnCmd.setExecutor(sCmd);
            setspawnCmd.setTabCompleter(sCmd);
        }

        PluginCommand kasaCmd = getCommand("kasa");
        if (kasaCmd != null) {
            CrateCommand cCmd = new CrateCommand(this);
            kasaCmd.setExecutor(cCmd);
            kasaCmd.setTabCompleter(cCmd);
        }

        PluginCommand koyluCmd = getCommand("koylu");
        if (koyluCmd != null) {
            VillagerCommand vCmd = new VillagerCommand(this);
            koyluCmd.setExecutor(vCmd);
            koyluCmd.setTabCompleter(vCmd);
        }

        PluginCommand ecoCmd = getCommand("para");
        if (ecoCmd != null) {
            EconomyCommand eCmd = new EconomyCommand(this);
            ecoCmd.setExecutor(eCmd);
            ecoCmd.setTabCompleter(eCmd);
        }

        PluginCommand farmCmd = getCommand("ciftci");
        if (farmCmd != null) {
            FarmerCommand fCmd = new FarmerCommand(this);
            farmCmd.setExecutor(fCmd);
            farmCmd.setTabCompleter(fCmd);
        }

        PluginCommand mktCmd = getCommand("market");
        if (mktCmd != null) {
            MarketCommand mCmd = new MarketCommand(this);
            mktCmd.setExecutor(mCmd);
            mktCmd.setTabCompleter(mCmd);
        }

        PluginCommand tradeCmd = getCommand("takas");
        if (tradeCmd != null) {
            TradeCommand tCmd = new TradeCommand(this);
            tradeCmd.setExecutor(tCmd);
            tradeCmd.setTabCompleter(tCmd);
        }

        PluginCommand satCmd = getCommand("sat");
        if (satCmd != null) {
            dev.bekirr.tekblok.commands.SellCommand sCmd = new dev.bekirr.tekblok.commands.SellCommand(this);
            satCmd.setExecutor(sCmd);
            satCmd.setTabCompleter(sCmd);
        }

                PluginCommand minyonCmd = getCommand("minyon");
        if (minyonCmd != null) {
            MinionCommand mCmd = new MinionCommand(this);
            minyonCmd.setExecutor(mCmd);
            minyonCmd.setTabCompleter(mCmd);
        }

        // 4. Otomatik Kayıt Görevi (Her 5 dakikada bir)
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (islandManager != null) {
                islandManager.saveIslands();
            }
            if (economyManager != null) {
                economyManager.saveBalances();
            }
            if (farmerManager != null) {
                farmerManager.saveFarmers();
            }
            if (minionManager != null) {
                minionManager.saveMinions();
            }
        }, 20L * 60 * 5, 20L * 60 * 5);

        // 5. Blok Koruma ve Otomatik Onarım Görevi (Her 2 saniyede bir)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (islandManager != null) {
                islandManager.checkAllIslandsOneBlock();
            }
        }, 20L, 40L);

                // 6. Saatlik Çırak Kasa Anahtarı Dağıtımı (Her 1 saatte bir / 72.000 tick)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            int count = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (crateManager != null) {
                    crateManager.giveKey(p, "cirak", 1);
                }
                p.sendTitle(ColorUtils.color("&a&l+1 ÇIRAK ANAHTARI"), ColorUtils.color("&71 saatlik aktiflik ödülü!"), 10, 60, 20);
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                count++;
            }
            if (count > 0) {
                Bukkit.broadcastMessage(ColorUtils.color("&8&m----------------------------------------"));
                Bukkit.broadcastMessage(ColorUtils.color("&a&l[HEDİYE] &eSunucuda 1 saat geçirdiğiniz için herkese &a&lÇırak Kasa Anahtarı &everildi!"));
                Bukkit.broadcastMessage(ColorUtils.color("&7Spawn alanındaki Çırak Kasasını açıp ödüllerinizi alabilirsiniz!"));
                Bukkit.broadcastMessage(ColorUtils.color("&8&m----------------------------------------"));
            }
        }, 72000L, 72000L);

        getLogger().info("TekBlok eklentisi başarıyla aktif edildi!");
    }

    @Override
    public void onDisable() {
        getLogger().info("TekBlok eklentisi devre dışı bırakılıyor, veriler kaydediliyor...");

        if (displayManager != null) {
            displayManager.removeAll();
        }

        if (crateManager != null) {
            crateManager.removeAllHolograms();
        }

        if (spawnManager != null) {
            spawnManager.clearWelcomeHologram();
        }

        if (villagerManager != null) {
            villagerManager.removeAllEntities();
        }

        if (islandManager != null) {
            islandManager.saveIslands();
        }

        if (economyManager != null) {
            economyManager.saveBalances();
        }

        if (farmerManager != null) {
            farmerManager.saveFarmers();
        }

        if (minionManager != null) {
            minionManager.removeAllEntities();
            minionManager.saveMinions();
        }

        getLogger().info("TekBlok verileri güvenle kaydedildi.");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new VoidChunkGenerator();
    }

    public static TekBlokPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PhaseManager getPhaseManager() {
        return phaseManager;
    }

    public DisplayManager getDisplayManager() {
        return displayManager;
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public FarmerManager getFarmerManager() {
        return farmerManager;
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public VillagerManager getVillagerManager() {
        return villagerManager;
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public MinionManager getMinionManager() {
        return minionManager;
    }
}

