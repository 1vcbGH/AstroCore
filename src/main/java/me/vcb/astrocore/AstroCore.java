package me.vcb.astrocore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import me.vcb.astrocore.abilities.AbilitiesManager;
import me.vcb.astrocore.classes.manager.PvpClassManager;
import me.vcb.astrocore.commands.manager.CommandManager;
import me.vcb.astrocore.commands.manager.SubCommandExecutor;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.ConfigFile;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.database.MongoManager;
import me.vcb.astrocore.database.impl.MongoClaimManager;
import me.vcb.astrocore.database.impl.MongoFactionsManager;
import me.vcb.astrocore.database.impl.MongoUserdataManager;
import me.vcb.astrocore.deathban.DeathbanManager;
import me.vcb.astrocore.economy.EconomyManager;
import me.vcb.astrocore.economy.SignShopManager;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.factions.commands.FactionsCommandExecutor;
import me.vcb.astrocore.games.conquest.ConquestManager;
import me.vcb.astrocore.games.conquest.commands.ConquestCommandExecutor;
import me.vcb.astrocore.games.dragon.EnderDragonManager;
import me.vcb.astrocore.games.dragon.commands.EnderDragonCommandExecutor;
import me.vcb.astrocore.games.dtc.DtcManager;
import me.vcb.astrocore.games.dtc.commands.DtcCommandExecutor;
import me.vcb.astrocore.games.king.KillTheKingManager;
import me.vcb.astrocore.games.king.commands.KillTheKingCommandExecutor;
import me.vcb.astrocore.games.koth.KothManager;
import me.vcb.astrocore.games.koth.commands.KothCommandExecutor;
import me.vcb.astrocore.games.loot.LootManager;
import me.vcb.astrocore.games.loot.commands.LootCommandExecutor;
import me.vcb.astrocore.games.mountain.MountainManager;
import me.vcb.astrocore.games.mountain.commands.MountainCommandExecutor;
import me.vcb.astrocore.games.schedule.ScheduleManager;
import me.vcb.astrocore.games.schedule.commands.ScheduleCommandExecutor;
import me.vcb.astrocore.glass.GlassManager;
import me.vcb.astrocore.handlers.*;
import me.vcb.astrocore.handlers.block.AutoSmeltHandler;
import me.vcb.astrocore.handlers.block.CrowbarHandler;
import me.vcb.astrocore.handlers.block.FilterHandler;
import me.vcb.astrocore.handlers.chat.ChatControlHandler;
import me.vcb.astrocore.handlers.chat.ChatHandler;
import me.vcb.astrocore.handlers.chat.MessagingHandler;
import me.vcb.astrocore.handlers.death.DeathMessageHandler;
import me.vcb.astrocore.handlers.kitmap.KillstreakHandler;
import me.vcb.astrocore.handlers.leaderboard.LeaderboardHandler;
import me.vcb.astrocore.handlers.limiter.PotionLimiterHandler;
import me.vcb.astrocore.handlers.logger.CombatLoggerHandler;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.handlers.manager.HandlerManager;
import me.vcb.astrocore.handlers.portal.EndPortalHandler;
import me.vcb.astrocore.handlers.rank.RankReviveHandler;
import me.vcb.astrocore.handlers.rank.ReclaimHandler;
import me.vcb.astrocore.handlers.restore.InventoryData;
import me.vcb.astrocore.handlers.restore.InventoryRestoreManager;
import me.vcb.astrocore.handlers.salvage.SalvageHandler;
import me.vcb.astrocore.handlers.settings.SettingsHandler;
import me.vcb.astrocore.handlers.staff.FreezeHandler;
import me.vcb.astrocore.handlers.staff.NotesHandler;
import me.vcb.astrocore.handlers.staff.RebootHandler;
import me.vcb.astrocore.handlers.staff.StaffChatHandler;
import me.vcb.astrocore.handlers.staff.WarpsHandler;
import me.vcb.astrocore.handlers.timer.EotwHandler;
import me.vcb.astrocore.handlers.timer.PurgeHandler;
import me.vcb.astrocore.handlers.timer.SotwHandler;
import me.vcb.astrocore.hologram.HologramManager;
import me.vcb.astrocore.partner.PartnerManager;
import me.vcb.astrocore.rank.RankListener;
import me.vcb.astrocore.rank.RankManager;
import me.vcb.astrocore.hologram.command.HologramCommandExecutor;
import me.vcb.astrocore.integration.vault.Economy_AstroCore;
import me.vcb.astrocore.kits.KitsManager;
import me.vcb.astrocore.kits.commands.KitCommandExecutor;
import me.vcb.astrocore.kits.commands.KitEditorCommandExecutor;
import me.vcb.astrocore.lunarclient.LunarClientManager;
import me.vcb.astrocore.scoreboard.ScoreboardManager;
import me.vcb.astrocore.selection.SelectionManager;
import me.vcb.astrocore.staffmode.StaffModeManager;
import me.vcb.astrocore.staffmode.VanishManager;
import me.vcb.astrocore.tab.TabManager;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.userdata.UserdataManager;
import me.vcb.astrocore.userdata.settings.Settings;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import me.vcb.astrocore.utils.Datastore;
import me.vcb.astrocore.utils.GsonUtils;
import me.vcb.astrocore.utils.ManagerEnabler;
import me.vcb.astrocore.utils.PlayerUtils;
import me.vcb.astrocore.utils.gson.FactionTypeAdapter;
import me.vcb.astrocore.utils.gson.LocationTypeAdapter;
import me.vcb.astrocore.utils.gson.LootTypeAdapter;
import me.vcb.astrocore.utils.gson.PlayerTypeAdapter;
import me.vcb.astrocore.utils.gson.RestoreTypeAdapter;
import me.vcb.astrocore.utils.gson.SettingsTypeAdapter;
import me.vcb.astrocore.utils.item.ItemUtils;
import me.vcb.astrocore.utils.nms.NmsUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Getter
public class AstroCore extends JavaPlugin {

    @Getter private static AstroCore instance;
    @Setter private boolean fullyEnabled;

    private Gson gson;

    @Setter private ConfigFile config;
    @Setter private ConfigFile language;
    @Setter private ConfigFile scoreboardFile;
    @Setter private ConfigFile tabFile;
    @Setter private ConfigFile classesFile;
    @Setter private ConfigFile limitersFile;
    @Setter private ConfigFile abilitiesFile;
    @Setter private ConfigFile itemsFile;
    private ConfigFile utilitiesFile;

    @Setter private MongoManager mongoManager;
    @Setter private UserdataManager userdataManager;
    private DeathbanManager deathbanManager;
    private EconomyManager economyManager;
    private GlassManager glassManager;
    private InventoryRestoreManager inventoryRestoreManager;
    private KitsManager kitsManager;
    private LunarClientManager lunarClientManager;
    private PvpClassManager pvpClassManager;
    private SelectionManager selectionManager;
    private SignShopManager signShopManager;
    private StaffModeManager staffModeManager;
    private VanishManager vanishManager;
    private AbilitiesManager abilitiesManager;
    private ScoreboardManager scoreboardManager;
    @Setter private TabManager tabManager;
    private HandlerManager handlerManager;

    private ConquestManager conquestManager;
    private EnderDragonManager enderDragonManager;
    private DtcManager dtcManager;
    private KillTheKingManager killTheKingManager;
    private KothManager kothManager;
    private MountainManager mountainManager;
    private LootManager lootManager;
    private ScheduleManager scheduleManager;

    private CommandManager commandManager;
    private ConquestCommandExecutor conquestCommandExecutor;
    private EnderDragonCommandExecutor enderDragonCommandExecutor;
    private DtcCommandExecutor dtcCommandExecutor;
    private FactionsCommandExecutor factionsCommandExecutor;
    private HologramCommandExecutor hologramCommandExecutor;
    private KitCommandExecutor kitCommandExecutor;
    private KitEditorCommandExecutor kitEditorCommandExecutor;
    private KillTheKingCommandExecutor killTheKingCommandExecutor;
    private KothCommandExecutor kothCommandExecutor;
    private MountainCommandExecutor mountainCommandExecutor;
    private LootCommandExecutor lootCommandExecutor;
    private ScheduleCommandExecutor scheduleCommandExecutor;

    private AutoSmeltHandler autoSmeltHandler;
    private me.vcb.astrocore.handlers.chat.AutoModHandler autoModHandler;
    private me.vcb.astrocore.handlers.chat.BroadcastSchedulerHandler broadcastSchedulerHandler;
    private BottleHandler bottleHandler;
    private ChatControlHandler chatControlHandler;
    private CombatLoggerHandler combatLoggerHandler;
    private CrowbarHandler crowbarHandler;
    private DeathMessageHandler deathMessageHandler;
    private me.vcb.astrocore.handlers.death.DeathRecapHandler deathRecapHandler;
    private EndPortalHandler endPortalHandler;
    private EotwHandler eotwHandler;
    private FilterHandler filterHandler;
    private FreezeHandler freezeHandler;
    private InventoryHandler inventoryHandler;
    private KillstreakHandler killstreakHandler;
    private LeaderboardHandler leaderboardHandler;
    private MapkitHandler mapkitHandler;
    private MessagingHandler messagingHandler;
    private NotesHandler notesHandler;
    private PotionLimiterHandler potionLimiterHandler;
    private PurgeHandler purgeHandler;
    private RankReviveHandler rankReviveHandler;
    private RebootHandler rebootHandler;
    private ReclaimHandler reclaimHandler;
    private SalvageHandler salvageHandler;
    private SettingsHandler settingsHandler;
    private SotwHandler sotwHandler;
    private SpawnTeleportHandler spawnTeleportHandler;
    private StaffChatHandler staffChatHandler;
    private StatsHandler statsHandler;
    private WarpsHandler warpsHandler;
    private HologramManager hologramManager;
    private PartnerManager partnerManager;
    private RankManager rankManager;
    private me.vcb.astrocore.bounty.BountyManager bountyManager;
    private me.vcb.astrocore.lottery.LotteryManager lotteryManager;
    private me.vcb.astrocore.friends.FriendsManager friendsManager;
    @Getter private me.vcb.astrocore.disguise.DisguiseManager disguiseManager;
    private me.vcb.astrocore.events.bloodmoon.BloodMoonManager bloodMoonManager;
    private me.vcb.astrocore.events.hunterprey.HunterPreyManager hunterPreyManager;
    @Getter private me.vcb.astrocore.events.eloboost.EloBoostManager eloBoostManager;
    @Getter private me.vcb.astrocore.report.ReportManager reportManager;
    @Getter private me.vcb.astrocore.customenchants.CustomEnchantManager customEnchantManager;
    @Getter private me.vcb.astrocore.customenchants.CustomEnchantHandler customEnchantHandler;
    @Getter private me.vcb.astrocore.handlers.killstreak.KillstreakRewardManager killstreakRewardManager;
    @Getter private me.vcb.astrocore.sanctions.SanctionManager sanctionManager;
    @Getter private me.vcb.astrocore.punishments.PunishmentManager punishmentManager;
    @Getter private me.vcb.astrocore.punishments.gui.PunishGui punishGui;
    @Getter private me.vcb.astrocore.factions.falltrap.FalltrapManager falltrapManager;
    @Getter private me.vcb.astrocore.factions.base.BaseManager baseManager;
    @Getter private me.vcb.astrocore.factions.base.SkyBaseManager skyBaseManager;
    @Getter private me.vcb.astrocore.perks.PerksMenu perksMenu;
    @Getter private me.vcb.astrocore.afk.AfkManager afkManager;
    @Getter private me.vcb.astrocore.factions.bank.FactionBankInterest factionBankInterest;
    @Getter private me.vcb.astrocore.api.WebApiManager webApiManager;
    @Getter private me.vcb.astrocore.handlers.TpsWatchdog tpsWatchdog;
    @Getter private me.vcb.astrocore.killeffects.KillEffectHandler killEffectHandler;
    @Getter private me.vcb.astrocore.supplydrop.SupplyDropManager supplyDropManager;
    @Getter private me.vcb.astrocore.coinshop.TrailManager trailManager;
    @Getter private me.vcb.astrocore.crates.CrateManager crateManager;
    @Getter private me.vcb.astrocore.npc.NpcManager npcManager;

    @Override
    public void onEnable() {
        instance = this;

        Config.START_TIME = System.currentTimeMillis();
        NmsUtils.init();

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        this.log(" ");
        this.log("&9    _    ____ _____ ____   ___   ____ ___  ____  _____");
        this.log("&9   / \\  / ___|_   _|  _ \\ / _ \\ / ___/ _ \\|  _ \\| ____|");
        this.log("&9  / _ \\ \\___ \\ | | | |_) | | | | |  | | | | |_) |  _|");
        this.log("&9 / ___ \\ ___) || | |  _ <| |_| | |__| |_| |  _ <| |___");
        this.log("&9/_/   \\_\\____/ |_| |_| \\_\\\\___/ \\____\\___/|_| \\_\\_____|");
        this.log(" ");
        this.log("&9AstroCore  &8·  &9v" + this.getDescription().getVersion() + "  &8·  &7by &9BlackNova &7& &91vcb");
        this.log(" ");

        // Only write defaults if the file is missing — never overwrite the admin's edits.
        if (!new java.io.File(this.getDataFolder(), "abilities.yml").exists())  this.saveResource("abilities.yml", false);
        if (!new java.io.File(this.getDataFolder(), "scoreboard.yml").exists()) this.saveResource("scoreboard.yml", false);
        if (!new java.io.File(this.getDataFolder(), "tab.yml").exists())        this.saveResource("tab.yml", false);

        try {
            this.config = new ConfigFile("config.yml");

            this.language = new ConfigFile("language.yml");
            this.scoreboardFile = new ConfigFile("scoreboard.yml");
            this.tabFile = new ConfigFile("tab.yml");
            this.classesFile = new ConfigFile("classes.yml");
            this.limitersFile = new ConfigFile("limiters.yml");
            this.abilitiesFile = new ConfigFile("abilities.yml");
            this.itemsFile = new ConfigFile("items.yml");
            this.utilitiesFile = new ConfigFile("utilities.yml");
        } catch(RuntimeException e) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.log("");
        this.log("- &7Using &c" + Datastore.DATASTORE + " &7datastore.");
        this.log("");

        this.beforeInit();

        if(!this.checkLunarClient()) return;

        try {
            this.setupDatastore();
            
            this.setupManagers();
            this.partnerManager = new PartnerManager();
            this.rankManager = new RankManager();
            this.bountyManager = new me.vcb.astrocore.bounty.BountyManager();
            this.lotteryManager = new me.vcb.astrocore.lottery.LotteryManager();
            this.friendsManager = new me.vcb.astrocore.friends.FriendsManager();
            this.disguiseManager = new me.vcb.astrocore.disguise.DisguiseManager();
            this.bloodMoonManager = new me.vcb.astrocore.events.bloodmoon.BloodMoonManager();
            this.hunterPreyManager = new me.vcb.astrocore.events.hunterprey.HunterPreyManager();
            this.eloBoostManager = new me.vcb.astrocore.events.eloboost.EloBoostManager();
            this.reportManager = new me.vcb.astrocore.report.ReportManager();
            this.customEnchantManager = new me.vcb.astrocore.customenchants.CustomEnchantManager();
            this.customEnchantHandler = new me.vcb.astrocore.customenchants.CustomEnchantHandler();
            this.killstreakRewardManager = new me.vcb.astrocore.handlers.killstreak.KillstreakRewardManager();
            this.sanctionManager   = new me.vcb.astrocore.sanctions.SanctionManager();
            this.punishmentManager = new me.vcb.astrocore.punishments.PunishmentManager();
            this.punishGui         = new me.vcb.astrocore.punishments.gui.PunishGui();
            Bukkit.getPluginManager().registerEvents(new me.vcb.astrocore.punishments.PunishmentListener(), this);
            this.falltrapManager   = new me.vcb.astrocore.factions.falltrap.FalltrapManager();
            this.baseManager       = new me.vcb.astrocore.factions.base.BaseManager();
            this.skyBaseManager    = new me.vcb.astrocore.factions.base.SkyBaseManager();
            this.perksMenu         = new me.vcb.astrocore.perks.PerksMenu();
            new me.vcb.astrocore.elevator.ElevatorManager();
            this.afkManager        = new me.vcb.astrocore.afk.AfkManager();
            new me.vcb.astrocore.factions.upgrades.FactionUpgradesGui();
            new me.vcb.astrocore.factions.vault.FactionVaultManager();
            new me.vcb.astrocore.factions.leaderboard.FactionLeaderboardGui();
            new me.vcb.astrocore.missions.MissionsManager();
            new me.vcb.astrocore.battlepass.BattlePassManager();
            new me.vcb.astrocore.shop.ShopManager();
            this.factionBankInterest = new me.vcb.astrocore.factions.bank.FactionBankInterest();
            this.webApiManager = new me.vcb.astrocore.api.WebApiManager();
            this.tpsWatchdog       = new me.vcb.astrocore.handlers.TpsWatchdog();
            this.killEffectHandler = new me.vcb.astrocore.killeffects.KillEffectHandler();
            this.supplyDropManager = new me.vcb.astrocore.supplydrop.SupplyDropManager();
            this.trailManager  = new me.vcb.astrocore.coinshop.TrailManager();
            this.crateManager  = new me.vcb.astrocore.crates.CrateManager();
            this.npcManager    = new me.vcb.astrocore.npc.NpcManager();
            Bukkit.getPluginManager().registerEvents(new RankListener(), this);

            // Auto-save cada 5 minutos (6000 ticks)
            Tasks.syncTimer(() -> {
                Bukkit.getOnlinePlayers().forEach(p ->
                    Tasks.async(() -> this.userdataManager.saveUserdata(p.getUniqueId(), false)));
            }, 6000L, 6000L);
            this.setupHandlers();
        } catch(Exception e) {
            this.log("&4===&c=============================================&4===");
            this.log("   &cError occurred while enabling AstroCore. Error:");
            this.log("");

            e.printStackTrace();

            this.log("");
            this.log("&4===&c=============================================&4===");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if(!this.fullyEnabled) return;

        this.log("&3===&b=============================================&3===");
        this.log("- &7Disabling AstroCore " + this.getDescription().getVersion());
        this.log("");

        if(this.tpsWatchdog      != null) this.tpsWatchdog.cancel();
        if(this.supplyDropManager != null) this.supplyDropManager.cancelAutoTask();
        if(this.trailManager  != null) this.trailManager.disable();
        if(this.crateManager  != null) this.crateManager.disable();
        if(this.npcManager    != null) this.npcManager.disable();
        if(this.afkManager    != null) this.afkManager.disable();
        if(this.factionBankInterest != null) this.factionBankInterest.disable();
        if(this.webApiManager != null) this.webApiManager.disable();
        if(this.disguiseManager != null) this.disguiseManager.disable();
        if(this.lotteryManager != null) this.lotteryManager.disable();
        this.scoreboardManager.disable();
        if(Config.TAB_ENABLED) this.tabManager.disable();

        ClaimManager.getInstance().disable();
        FactionsManager.getInstance().disable();
        TimerManager.getInstance().disable();
        this.userdataManager.disable();

        this.disableManagers();
        this.disableGameManagers();
        this.disableCommandManagers();

        if(Datastore.DATASTORE == Datastore.MONGO) {
            this.mongoManager.disable();
        }

        NmsUtils.getInstance().disable();
        Bukkit.getServicesManager().unregisterAll(this);

        this.log("&3===&b=============================================&3===");
    }

    private void disableManagers() {
        this.deathbanManager.disable();
        this.handlerManager.disable();
        this.glassManager.disable();
        this.inventoryRestoreManager.disable();
        this.kitsManager.disable();
        this.pvpClassManager.disable();
        this.selectionManager.disable();
        this.signShopManager.disable();
        this.staffModeManager.disable();
        this.vanishManager.disable();
        this.abilitiesManager.disable();
        this.hologramManager.disable();

        if(this.lunarClientManager != null) {
            this.lunarClientManager.disable();
        }
    }

    private void disableGameManagers() {
        this.conquestManager.disable();
        this.enderDragonManager.disable();
        this.dtcManager.disable();
        this.killTheKingManager.disable();
        this.kothManager.disable();
        this.mountainManager.disable();
        this.lootManager.disable();
        this.scheduleManager.disable();
        if(this.bloodMoonManager  != null) this.bloodMoonManager.disable();
        if(this.hunterPreyManager != null) this.hunterPreyManager.disable();
        if(this.eloBoostManager   != null) this.eloBoostManager.disable();
        if(this.customEnchantHandler != null) this.customEnchantHandler.disable();
    }

    private void disableCommandManagers() {
        this.commandManager.disable();
        this.conquestCommandExecutor.disable();
        this.enderDragonCommandExecutor.disable();
        this.dtcCommandExecutor.disable();
        this.factionsCommandExecutor.disable();
        this.hologramCommandExecutor.disable();
        this.kitCommandExecutor.disable();
        this.kitEditorCommandExecutor.disable();
        this.killTheKingCommandExecutor.disable();
        this.kothCommandExecutor.disable();
        this.mountainCommandExecutor.disable();
        this.lootCommandExecutor.disable();
        this.scheduleCommandExecutor.disable();
    }

    private void beforeInit() {
        ChatHandler.setup();
        ItemUtils.setupItemStackMaps();
        PlayerUtils.setupMetadataValue();

        this.registerGson();
        this.registerVaultSupport();

        new Config();
        new Language();

        if(Config.TAB_ENABLED && Bukkit.getMaxPlayers() < 60) {
            this.log("&4===&c=============================================&4===");
            this.log("     &cIncrease your slot number to at least 60");
            this.log("          &cfor your tab to work properly!");
            this.log("&4===&c=============================================&4===");
            this.log("");
        }
    }
    
    private boolean checkLunarClient() {
        if(Config.LUNAR_CLIENT_API_ENABLED && !Bukkit.getPluginManager().isPluginEnabled("LunarClient-API")) {
            this.log("&4===&c=============================================&4===");
            this.log("       &cLunar client support is enabled, but");
            this.log("            &cLunarClient-API is missing!");
            this.log("&4===&c=============================================&4===");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }
        
        return true;
    }

    private void setupDatastore() {
        if(Datastore.DATASTORE == Datastore.JSON) {
            new FactionsManager();
            new ClaimManager();
            this.userdataManager = new UserdataManager();
        } else {
            this.mongoManager = new MongoManager();

            new MongoFactionsManager();
            new MongoClaimManager();
            this.userdataManager = new MongoUserdataManager();
        }

        new TimerManager();
    }

    private void setupManagers() throws Exception {
        for(Field field : this.getClass().getDeclaredFields()) {
            if(!ManagerEnabler.class.isAssignableFrom(field.getType()) && field.getType().getSuperclass() != SubCommandExecutor.class) continue;

            field.setAccessible(true);

            Constructor<?> constructor = field.getType().getDeclaredConstructor();
            field.set(this, constructor.newInstance());
        }

        if(Config.TAB_ENABLED) {
            this.tabManager = new TabManager();
        }

        if(Config.LUNAR_CLIENT_API_ENABLED && Bukkit.getPluginManager().isPluginEnabled("LunarClient-API")) {
            this.lunarClientManager = new LunarClientManager();
        }
    }

    private void setupHandlers() throws Exception {
        for(Field field : this.getClass().getDeclaredFields()) {
            if(field.getType().getSuperclass() != Handler.class) continue;

            field.setAccessible(true);
            field.set(this, this.handlerManager.getHandler(field.getType()));
        }

        this.log("");
        this.log("- &bIssue tracker&7: github.com/1vcb/AstroCore-Issues");
        this.log("- &bEnabled. Took &a" + (System.currentTimeMillis() - Config.START_TIME) + " &bms.");
        this.log("&3===&b=============================================&3===");

        this.fullyEnabled = true;
    }

    public void registerGson() {
        this.gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
            .enableComplexMapKeySerialization().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
            .registerTypeAdapter(GsonUtils.PLAYER_TYPE, new PlayerTypeAdapter())
            .registerTypeAdapter(GsonUtils.FACTION_TYPE, new FactionTypeAdapter())
            .registerTypeAdapter(GsonUtils.LOOT_TYPE, new LootTypeAdapter())
            .registerTypeAdapter(Location.class, new LocationTypeAdapter())
            .registerTypeAdapter(InventoryData.class, new RestoreTypeAdapter())
            .registerTypeAdapter(Settings.class, new SettingsTypeAdapter())
            .registerTypeAdapter(java.time.LocalDateTime.class, new me.vcb.astrocore.utils.gson.LocalDateTimeTypeAdapter())
            .registerTypeAdapter(GsonUtils.HOLOGRAMS_TYPE, new me.vcb.astrocore.utils.gson.HologramTypeAdapter())
        .create();
    }

    public void registerVaultSupport() {
        Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
        if(vaultPlugin == null || !vaultPlugin.isEnabled()) return;

        Bukkit.getServicesManager().register(Economy.class, new Economy_AstroCore(), this, ServicePriority.Highest);
    }

    public void log(String message) {
        Bukkit.getConsoleSender().sendMessage(Color.translate(message));
    }

    public ClassLoader getPluginClassLoader() {
        return this.getClassLoader();
    }
}
