package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.lunarclient.cooldown.CooldownType;
import me.vcb.astrocore.timer.type.PlayerTimer;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.utils.StringUtils.FormatType;
import org.bukkit.entity.Player;

import java.util.concurrent.ScheduledExecutorService;

public class AppleTimer extends PlayerTimer implements ScoreboardTimer {

    public AppleTimer(ScheduledExecutorService executor) {
        super(executor, "AppleTimer", Config.NORMAL_GOLDEN_APPLE_COOLDOWN, true);

        this.setExpiryMessage(Language.PREFIX + Language.NORMAL_APPLE_COOLDOWN_EXPIRED);
        this.setFormat(FormatType.MILLIS_TO_MINUTES);
        this.setLunarCooldownType(CooldownType.APPLE);
    }

    @Override
    public String getPlaceholder() {
        return Config.NORMAL_GOLDEN_APPLE_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry(Player player) {
        return "§c" + this.getTimeLeft(player);
    }
}
