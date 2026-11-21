package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.glass.GlassManager.GlassType;
import me.vcb.astrocore.lunarclient.cooldown.CooldownType;
import me.vcb.astrocore.timer.type.PlayerTimer;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.utils.StringUtils.FormatType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

public class CombatTagTimer extends PlayerTimer implements ScoreboardTimer {

    public CombatTagTimer(ScheduledExecutorService executor) {
        super(executor, "CombatTagTimer", Config.COMBAT_TAG_DURATION);

        this.setExpiryMessage(Language.PREFIX + Language.COMBAT_TAG_EXPIRED);
        this.setFormat(FormatType.MILLIS_TO_MINUTES);
        this.setLunarCooldownType(CooldownType.COMBAT_TAG);
    }

    @Override
    public void activate(Player player) {
        this.activate(player, this.delay);
    }

    @Override
    public void activate(Player player, int delay) {
        this.activate(player.getUniqueId(), delay);
    }

    @Override
    public void activate(UUID uuid, int delay) {
        super.cancel(uuid);

        super.activate(uuid, delay, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) return;

            AstroCore.getInstance().getGlassManager().clearGlassVisuals(player, GlassType.SPAWN_WALL);
        });
    }

    @Override
    public void cancel(Player player) {
        super.cancel(player);
        AstroCore.getInstance().getGlassManager().clearGlassVisuals(player, GlassType.SPAWN_WALL);
    }

    @Override
    public String getPlaceholder() {
        return Config.COMBAT_TAG_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry(Player player) {
        return "§c" + this.getTimeLeft(player);
    }
}
