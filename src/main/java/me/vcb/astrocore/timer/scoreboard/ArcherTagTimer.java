package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.scoreboard.ScoreboardManager;
import me.vcb.astrocore.timer.type.PlayerTimer;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.utils.StringUtils.FormatType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

public class ArcherTagTimer extends PlayerTimer implements ScoreboardTimer {

    public ArcherTagTimer(ScheduledExecutorService executor) {
        super(executor, "ArcherTagTimer", Config.ARCHER_TAG_DURATION);

        this.setExpiryMessage(Language.PREFIX + Language.ARCHER_TAG_EXPIRED_MESSAGE);
        this.setFormat(FormatType.MILLIS_TO_SECONDS);
    }

    @Override
    public void activate(Player player) {
        this.activate(player.getUniqueId());
    }

    @Override
    public void activate(UUID uuid) {
        super.cancel(uuid);

        super.activate(uuid, () -> {
            Player player = Bukkit.getPlayer(uuid);

            if(player != null) {
                ScoreboardManager.getInstance().updateTabRelations(player, false);
            }
        });
    }

    @Override
    public String getPlaceholder() {
        return Config.ARCHER_TAG_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry(Player player) {
        return "§c" + this.getTimeLeft(player) + "s";
    }
}
