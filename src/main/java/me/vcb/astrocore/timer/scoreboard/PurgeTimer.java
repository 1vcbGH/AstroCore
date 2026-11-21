package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.timer.type.SystemTimer;
import me.vcb.astrocore.utils.Messages;

import java.util.concurrent.ScheduledExecutorService;

public class PurgeTimer extends SystemTimer implements ScoreboardTimer {

    private PurgeTimerType type;

    public PurgeTimer(ScheduledExecutorService executor) {
        super(executor, "PurgeTimer", 0, true);
    }

    public void activate(PurgeTimerType type, int delay, int purgeDuration) {
        this.type = type;

        if(type == PurgeTimerType.PURGE_TIMER) {
            super.activate(delay, () -> Messages.sendMessage(Language.PURGE_ENDED));
        } else {
            super.activate(delay, () -> AstroCore.getInstance().getPurgeHandler().startPurge(purgeDuration));
        }
    }

    public boolean isActive(PurgeTimerType type) {
        return this.type == type && super.isActive();
    }

    @Override
    public String getPlaceholder() {
        return this.type == PurgeTimerType.INITIAL_DELAY
            ? Config.PURGE_START_PLACEHOLDER : Config.PURGE_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry() {
        return "§c" + this.getDynamicTimeLeft();
    }

    public enum PurgeTimerType {
        INITIAL_DELAY, PURGE_TIMER
    }
}
