package me.vcb.astrocore.timer.cooldown;

import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.type.PlayerTimer;
import me.vcb.astrocore.utils.StringUtils.FormatType;

import java.util.concurrent.ScheduledExecutorService;

public class RankReviveTimer extends PlayerTimer {

    public RankReviveTimer(ScheduledExecutorService executor) {
        super(executor, "RankReviveTimer", 0, true);

        this.setExpiryMessage(Language.PREFIX + Language.RANK_REVIVE_COOLDOWN_EXPIRED);
        this.setFormat(FormatType.MILLIS_TO_MINUTES);
    }
}
