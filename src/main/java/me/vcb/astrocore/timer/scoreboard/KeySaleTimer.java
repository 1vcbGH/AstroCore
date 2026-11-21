package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.timer.type.SystemTimer;

import java.util.concurrent.ScheduledExecutorService;

public class KeySaleTimer extends SystemTimer implements ScoreboardTimer {

    public KeySaleTimer(ScheduledExecutorService executor) {
        super(executor, "KeySaleTimer", 0, true);

        this.setExpiryMessage(Language.SALE_FINISHED.replace("<type>", "Key"));
    }

    @Override
    public String getPlaceholder() {
        return Config.KEYSALE_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry() {
        return "§c" + this.getDynamicTimeLeft();
    }
}
