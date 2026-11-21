package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.event.AstroCoreKickEvent;
import me.vcb.astrocore.handlers.event.AstroCoreKickEvent.KickType;
import me.vcb.astrocore.lunarclient.cooldown.CooldownType;
import me.vcb.astrocore.timer.type.PlayerTimer;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.utils.PlayerUtils;
import me.vcb.astrocore.utils.StringUtils.FormatType;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.entity.Player;

import java.util.concurrent.ScheduledExecutorService;

public class LogoutTimer extends PlayerTimer implements ScoreboardTimer {

    public LogoutTimer(ScheduledExecutorService executor) {
        super(executor, "LogoutTimer", Config.LOGOUT_DELAY);

        this.setFormat(FormatType.MILLIS_TO_SECONDS);
        this.setLunarCooldownType(CooldownType.LOGOUT);
    }

    @Override
    public void activate(Player player) {
        super.activate(player, () -> Tasks.sync(() -> {
            player.setMetadata("logout", PlayerUtils.TRUE_METADATA_VALUE);

            AstroCoreKickEvent event = new AstroCoreKickEvent(player, KickType.LOGOUT, Language.LOGOUT_KICK_MESSAGE);

            if(!event.isCancelled()) {
                player.kickPlayer(Language.LOGOUT_KICK_MESSAGE);
            }
        }));
    }

    @Override
    public String getPlaceholder() {
        return Config.LOGOUT_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry(Player player) {
        return "§c" + this.getTimeLeft(player) + "s";
    }
}
