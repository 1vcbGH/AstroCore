package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.lunarclient.cooldown.CooldownType;
import me.vcb.astrocore.timer.type.PlayerTimer;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.utils.LocationUtils;
import me.vcb.astrocore.utils.StringUtils.FormatType;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.ScheduledExecutorService;

public class StuckTimer extends PlayerTimer implements ScoreboardTimer {

    public StuckTimer(ScheduledExecutorService executor) {
        super(executor, "StuckTimer", Config.FACTION_STUCK_WARMUP);

        this.setExpiryMessage(Language.FACTION_PREFIX + Language.FACTIONS_STUCK_TELEPORTED);
        this.setFormat(FormatType.MILLIS_TO_MINUTES);
        this.setLunarCooldownType(CooldownType.STUCK);
    }

    public void activate(Player player, Location location) {
        super.activate(player, () -> Tasks.sync(() -> LocationUtils.teleportWithChunkLoad(player, location)));
    }

    @Override
    public void cancel(Player player) {
        super.cancel(player);
        FactionsManager.getInstance().removeStuckInitialLocation(player);
    }

    @Override
    public String getPlaceholder() {
        return Config.STUCK_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry(Player player) {
        return "§c" + this.getTimeLeft(player);
    }
}
