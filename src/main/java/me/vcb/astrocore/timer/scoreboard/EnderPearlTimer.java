package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.lunarclient.cooldown.CooldownType;
import me.vcb.astrocore.timer.type.PlayerTimer;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.utils.StringUtils.FormatType;
import org.bukkit.entity.Player;

import java.util.concurrent.ScheduledExecutorService;

public class EnderPearlTimer extends PlayerTimer implements ScoreboardTimer {

    public EnderPearlTimer(ScheduledExecutorService executor) {
        super(executor, "EnderPearlTimer", Config.ENDER_PEARL_COOLDOWN_TIME);

        this.setExpiryMessage(Language.PREFIX + Language.ENDERPEARL_COOLDOWN_EXPIRED);
        this.setFormat(FormatType.MILLIS_TO_SECONDS);
        this.setLunarCooldownType(CooldownType.ENDERPEARL);
    }

    @Override
    public String getPlaceholder() {
        return Config.ENDERPEARL_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry(Player player) {
        return "§d" + this.getTimeLeft(player) + "s";
    }
}
