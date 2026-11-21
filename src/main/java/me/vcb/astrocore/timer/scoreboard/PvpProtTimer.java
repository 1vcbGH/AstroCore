package me.vcb.astrocore.timer.scoreboard;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.glass.GlassManager.GlassType;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.timer.type.PlayerTimer;
import me.vcb.astrocore.timer.type.ScoreboardTimer;
import me.vcb.astrocore.utils.StringUtils;
import me.vcb.astrocore.utils.StringUtils.FormatType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

public class PvpProtTimer extends PlayerTimer implements ScoreboardTimer {

    private Map<UUID, Long> paused;

    public PvpProtTimer(ScheduledExecutorService executor) {
        super(executor, "PvpProtTimer", Config.PVP_PROTECTION_DURATION, true);

        this.setExpiryMessage(Language.PREFIX + Language.PVP_PROT_PROTECTION_EXPIRED);
    }

    @Override
    protected void loadTimer() {
        this.paused = new HashMap<>();

        ConfigurationSection section = TimerManager.getInstance().getTimersFile().getSection("PvpProtTimer");
        if(section == null) return;

        section.getKeys(false).forEach(key -> {
            UUID uuid = UUID.fromString(key);
            this.paused.put(uuid, section.getLong(key));

            Player player = Bukkit.getPlayer(uuid);
            if(player == null) return;

            Faction faction = ClaimManager.getInstance().getFactionAt(player);
            if(!faction.isSafezone()) this.pause(player, false);
        });
    }

    @Override
    public void saveTimer() {
        super.saveTimer();

        ConfigurationSection section = TimerManager.getInstance().getTimersFile().getSection("PvpProtTimer");
        this.paused.forEach((uuid, time) -> section.set(uuid.toString(), time));
    }

    public void clearPvpProtections() {
        this.paused.clear();

        this.players.values().forEach(future -> future.cancel(true));
        this.players.clear();
    }

    public void activate(Player player, Location location) {
        if(this.delay <= 0 || AstroCore.getInstance().getEotwHandler().isActive()) return;
        this.activate(player, this.delay, location);
    }

    public void activate(Player player, int delay, Location location) {
        if(delay <= 0) return;

        if(ClaimManager.getInstance().getFactionAt(location).isSafezone()) {
            this.paused.put(player.getUniqueId(), delay * 1000L);
        } else {
            this.activate(player, delay, () -> AstroCore.getInstance().getGlassManager()
                .clearGlassVisuals(player, GlassType.CLAIM_WALL));
        }
    }

    public void pause(Player player, boolean pause) {
        this.pause(player, pause, player.getLocation());
    }

    public void pause(Player player, boolean pause, Location location) {
        if(!this.isActive(player)) return;

        if(pause) {
            if(this.isPaused(player)) return;

            this.paused.put(player.getUniqueId(), this.getCooldown(player));
            super.cancel(player);
        } else {
            if(super.isActive(player)) return;

            this.activate(player, this.getSecondsLeft(player), location);

            if(super.isActive(player)) {
                this.paused.remove(player.getUniqueId());
            }
        }
    }

    private int getSecondsLeft(Player player) {
        return this.paused.get(player.getUniqueId()).intValue() / 1000;
    }

    private boolean isPaused(Player player) {
        return this.paused.containsKey(player.getUniqueId());
    }

    @Override
    public void cancel(Player player) {
        super.cancel(player);
        this.paused.remove(player.getUniqueId());

        AstroCore.getInstance().getGlassManager().clearGlassVisuals(player, GlassType.CLAIM_WALL);
    }

    @Override
    public boolean isActive(Player player) {
        return super.isActive(player) || this.isPaused(player);
    }

    @Override
    protected long getCooldown(Player player) {
        if(this.isPaused(player)) {
            return this.paused.get(player.getUniqueId());
        } else {
            return super.getCooldown(player);
        }
    }

    @Override
    public String getTimeLeft(Player player) {
        long remaining = this.getCooldown(player);

        if(remaining < 3600000L) {
            return StringUtils.formatTime(remaining, FormatType.MILLIS_TO_MINUTES);
        } else {
            return StringUtils.formatTime(remaining, FormatType.MILLIS_TO_HOURS);
        }
    }

    @Override
    public String getPlaceholder() {
        return Config.PVP_PROTECTION_PLACEHOLDER;
    }

    @Override
    public String getScoreboardEntry(Player player) {
        return "§c" + this.getTimeLeft(player);
    }
}
