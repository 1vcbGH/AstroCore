package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.type.PlayerTimer;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import org.bukkit.entity.Player;

import java.util.concurrent.ScheduledExecutorService;

public class GAppleTimer extends PlayerTimer implements ScoreboardTimer {

    public GAppleTimer(ScheduledExecutorService executor) {
        super(executor, "GAppleTimer", Config.ENCHANTED_GOLDEN_APPLE_COOLDOWN, true);

        this.setExpiryMessage(Language.PREFIX + Language.ENCHANTED_APPLE_COOLDOWN_EXPIRED);
    }

    @Override
    public String getPlaceholder() {
        return Config.ENCHANTED_GOLDEN_APPLE_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry(Player player) {
        return "§c" + this.getDynamicTimeLeft(player);
    }
}
