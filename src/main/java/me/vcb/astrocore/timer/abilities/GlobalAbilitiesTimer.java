package me.vcb.astrocore.timer.abilities;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.type.PlayerTimer;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.utils.StringUtils.FormatType;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

public class GlobalAbilitiesTimer extends PlayerTimer implements ScoreboardTimer {

    public GlobalAbilitiesTimer(ScheduledExecutorService executor) {
        super(executor, "GlobalAbilitiesTimer", Config.ABILITIES_GLOBAL_COOLDOWN_DURATION);

        this.setExpiryMessage(Language.PREFIX + Language.ABILITIES_GLOBAL_COOLDOWN_EXPIRED);
        this.setFormat(FormatType.MILLIS_TO_SECONDS);
    }

    @Override
    public void activate(Player player) {
        this.activate(player.getUniqueId());
    }

    @Override
    public void activate(UUID uuid) {
        if(!Config.ABILITIES_GLOBAL_COOLDOWN_ENABLED) return;
        super.activate(uuid);
    }

    @Override
    public String getTimeLeft(Player player) {
        return super.getTimeLeft(player) + 's';
    }

    @Override
    public String getPlaceholder() {
        return Config.ABILITIES_GLOBAL_COOLDOWN_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry(Player player) {
        return this.getTimeLeft(player);
    }
}
