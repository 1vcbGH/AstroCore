package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.scoreboard.ScoreboardManager;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.timer.type.SystemTimer;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ScheduledExecutorService;

public class SotwTimer extends SystemTimer implements ScoreboardTimer {

    public SotwTimer(ScheduledExecutorService executor) {
        super(executor, "SotwTimer", Config.SOTW_DEFAULT_TIME * 60, true);
    }

    @Override
    public void activate(int delay) {
        super.activate(delay, () -> {
            AstroCore.getInstance().getSotwHandler().showSotwInvisiblePlayers();
            ScoreboardManager.getInstance().updateTabRelations(Bukkit.getOnlinePlayers(), false);

            Messages.sendMessage(Language.SOTW_ENDED);
        });
    }

    @Override
    public String getPlaceholder(Player player) {
        if(AstroCore.getInstance().getSotwHandler().isPlayerSotwEnabled(player)) {
            return Config.SOTW_ENABLE_PLACEHOLDER;
        } else {
            return Config.SOTW_PLACEHOLDER;
        }
    }

    @Override
    public String getScoreboardEntry(Player player) {
        return "§c" + this.getDynamicTimeLeft();
    }
}
