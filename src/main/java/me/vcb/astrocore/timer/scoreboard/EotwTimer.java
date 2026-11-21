package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.timer.type.SystemTimer;
import me.vcb.astrocore.utils.StringUtils.FormatType;
import org.bukkit.command.CommandSender;

import java.util.concurrent.ScheduledExecutorService;

public class EotwTimer extends SystemTimer implements ScoreboardTimer {

    public EotwTimer(ScheduledExecutorService executor) {
        super(executor, "EotwTimer", 0, true);

        this.setFormat(FormatType.MILLIS_TO_MINUTES);
    }

    public void activate(CommandSender sender, int delay) {
        super.activate(delay, () -> AstroCore.getInstance().getEotwHandler().startEotw(delay));
    }

    @Override
    public String getPlaceholder() {
        return Config.EOTW_START_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry() {
        return "§c" + this.getTimeLeft();
    }
}
