package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.lunarclient.cooldown.CooldownType;
import me.vcb.astrocore.timer.type.PlayerTimer;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.utils.LocationUtils;
import me.vcb.astrocore.utils.StringUtils.FormatType;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.ScheduledExecutorService;

public class HomeTimer extends PlayerTimer implements ScoreboardTimer {

    public HomeTimer(ScheduledExecutorService executor) {
        super(executor, "HomeTimer", 0);

        this.setExpiryMessage(Language.FACTION_PREFIX + Language.FACTIONS_HOME_TELEPORTED);
        this.setFormat(FormatType.MILLIS_TO_SECONDS);
        this.setLunarCooldownType(CooldownType.HOME);
    }

    public void activate(Player player, int delay, Location location) {
        super.activate(player, delay, () -> Tasks.sync(() -> LocationUtils.teleportWithChunkLoad(player, location)));
    }

    @Override
    public String getPlaceholder() {
        return Config.HOME_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry(Player player) {
        return "§c" + this.getTimeLeft(player) + "s";
    }
}
