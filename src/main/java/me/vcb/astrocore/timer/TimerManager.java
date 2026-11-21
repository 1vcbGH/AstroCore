package me.vcb.astrocore.timer;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.ConfigFile;
import me.vcb.astrocore.timer.abilities.AbilitiesTimer;
import me.vcb.astrocore.timer.abilities.GlobalAbilitiesTimer;
import me.vcb.astrocore.timer.cooldown.CooldownTimer;
import me.vcb.astrocore.timer.cooldown.DtrRegenTimer;
import me.vcb.astrocore.timer.cooldown.FactionFreezeTimer;
import me.vcb.astrocore.timer.cooldown.FactionRallyTimer;
import me.vcb.astrocore.timer.cooldown.RankReviveTimer;
import me.vcb.astrocore.timer.custom.CustomTimer;
import me.vcb.astrocore.timer.scoreboard.AppleTimer;
import me.vcb.astrocore.timer.scoreboard.ArcherTagTimer;
import me.vcb.astrocore.timer.scoreboard.CombatTagTimer;
import me.vcb.astrocore.timer.scoreboard.EnderPearlTimer;
import me.vcb.astrocore.timer.scoreboard.EotwTimer;
import me.vcb.astrocore.timer.scoreboard.GAppleTimer;
import me.vcb.astrocore.timer.scoreboard.HomeTimer;
import me.vcb.astrocore.timer.scoreboard.KeySaleTimer;
import me.vcb.astrocore.timer.scoreboard.LogoutTimer;
import me.vcb.astrocore.timer.scoreboard.PurgeTimer;
import me.vcb.astrocore.timer.scoreboard.PvpClassWarmupTimer;
import me.vcb.astrocore.timer.scoreboard.PvpProtTimer;
import me.vcb.astrocore.timer.scoreboard.SaleTimer;
import me.vcb.astrocore.timer.scoreboard.SotwTimer;
import me.vcb.astrocore.timer.scoreboard.StuckTimer;
import me.vcb.astrocore.timer.scoreboard.TeleportTimer;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.utils.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Getter
public class TimerManager {

    @Getter private static TimerManager instance;

    private ConfigFile timersFile;

    private ScheduledThreadPoolExecutor executor;
    private PvpClassWarmupTimer pvpClassWarmupTimer;
    private GlobalAbilitiesTimer globalAbilitiesTimer;
    private AbilitiesTimer abilitiesTimer;

    private List<ScoreboardTimer> scoreboardTimers;

    private ArcherTagTimer archerTagTimer;
    private CombatTagTimer combatTagTimer;
    private EnderPearlTimer enderPearlTimer;
    private EotwTimer eotwTimer;
    private LogoutTimer logoutTimer;
    private PurgeTimer purgeTimer;
    private PvpProtTimer pvpProtTimer;
    private SotwTimer sotwTimer;
    private HomeTimer homeTimer;
    private SaleTimer saleTimer;
    private KeySaleTimer keySaleTimer;
    private StuckTimer stuckTimer;
    private TeleportTimer teleportTimer;
    private AppleTimer appleTimer;
    private GAppleTimer gAppleTimer;

    private DtrRegenTimer dtrRegenTimer;
    private FactionFreezeTimer factionFreezeTimer;
    private FactionRallyTimer factionRallyTimer;
    private CooldownTimer cooldownTimer;
    private RankReviveTimer rankReviveTimer;
    private CustomTimer customTimer;

    public TimerManager() {
        instance = this;
        this.timersFile = new ConfigFile("timers.yml");

        this.executor = new ScheduledThreadPoolExecutor(1, Tasks.newThreadFactory("Timer Thread - %d"));
        this.executor.setRemoveOnCancelPolicy(true);

        this.globalAbilitiesTimer = new GlobalAbilitiesTimer(this.executor);
        this.abilitiesTimer = new AbilitiesTimer(this.executor);
        this.pvpClassWarmupTimer = new PvpClassWarmupTimer(this.executor);

        this.initializeScoreboardTimers();
        this.initializeCustomAndFactionTimers();
    }

    public void disable() {
        this.archerTagTimer.disable();
        this.combatTagTimer.disable();
        this.enderPearlTimer.disable();
        this.homeTimer.disable();
        this.logoutTimer.disable();
        this.purgeTimer.disable();
        this.pvpProtTimer.disable();
        this.saleTimer.disable();
        this.keySaleTimer.disable();
        this.sotwTimer.disable();
        this.eotwTimer.disable();
        this.stuckTimer.disable();
        this.appleTimer.disable();
        this.gAppleTimer.disable();
        this.pvpClassWarmupTimer.disable();

        this.dtrRegenTimer.disable();
        this.factionFreezeTimer.disable();
        this.factionRallyTimer.disable();
        this.cooldownTimer.disable();
        this.rankReviveTimer.disable();
        this.customTimer.disable();

        this.scoreboardTimers.clear();
        this.timersFile.save();

        this.executor.shutdownNow();
    }

    public void deleteAllTimers() {
        this.scoreboardTimers.forEach(timer -> ((Timer) timer).disable());

        this.cooldownTimer.disable();
        this.rankReviveTimer.disable();
        this.customTimer.disable();

        this.timersFile.getFile().delete();
        this.timersFile = new ConfigFile("timers.yml");

        int deleted = this.scoreboardTimers.size() + 3;
        AstroCore.getInstance().log("- &cCleared &e" + deleted + " &ccooldown timers.");
    }

    private void initializeScoreboardTimers() {
        this.scoreboardTimers = new ArrayList<>();

        this.scoreboardTimers.add(this.saleTimer = new SaleTimer(this.executor));
        this.scoreboardTimers.add(this.keySaleTimer = new KeySaleTimer(this.executor));
        this.scoreboardTimers.add(this.sotwTimer = new SotwTimer(this.executor));
        this.scoreboardTimers.add(this.eotwTimer = new EotwTimer(this.executor));
        this.scoreboardTimers.add(this.purgeTimer = new PurgeTimer(this.executor));
        this.scoreboardTimers.add(this.pvpProtTimer = new PvpProtTimer(this.executor));
        this.scoreboardTimers.add(this.combatTagTimer = new CombatTagTimer(this.executor));
        this.scoreboardTimers.add(this.enderPearlTimer = new EnderPearlTimer(this.executor));
        this.scoreboardTimers.add(this.archerTagTimer = new ArcherTagTimer(this.executor));
        this.scoreboardTimers.add(this.homeTimer = new HomeTimer(this.executor));
        this.scoreboardTimers.add(this.stuckTimer = new StuckTimer(this.executor));
        this.scoreboardTimers.add(this.teleportTimer = new TeleportTimer(this.executor));
        this.scoreboardTimers.add(this.logoutTimer = new LogoutTimer(this.executor));

        this.gAppleTimer = new GAppleTimer(this.executor);
        this.appleTimer = new AppleTimer(this.executor);

        if(Config.ENCHANTED_GOLDEN_APPLE_ON_SCOREBOARD) this.scoreboardTimers.add(this.gAppleTimer);
        if(Config.NORMAL_GOLDEN_APPLE_ON_SCOREBOARD) this.scoreboardTimers.add(this.appleTimer);
    }

    private void initializeCustomAndFactionTimers() {
        this.factionRallyTimer = new FactionRallyTimer(this.executor);
        this.cooldownTimer = new CooldownTimer(this.executor);
        this.rankReviveTimer = new RankReviveTimer(this.executor);
        this.customTimer = new CustomTimer(this.executor);

        this.dtrRegenTimer = new DtrRegenTimer(this.executor);
        this.factionFreezeTimer = new FactionFreezeTimer(this.executor);

        this.dtrRegenTimer.startRegenTask();
    }
}
