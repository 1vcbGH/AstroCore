package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.timer.type.SystemTimer;

import java.util.concurrent.ScheduledExecutorService;

public class SaleTimer extends SystemTimer implements ScoreboardTimer {

    public SaleTimer(ScheduledExecutorService executor) {
        super(executor, "SaleTimer", 0, true);

        this.setExpiryMessage(Language.SALE_FINISHED.replace("<type>", "Store"));
    }

    @Override
    public String getPlaceholder() {
        return Config.SALE_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry() {
        return "§c" + this.getDynamicTimeLeft();
    }
}
