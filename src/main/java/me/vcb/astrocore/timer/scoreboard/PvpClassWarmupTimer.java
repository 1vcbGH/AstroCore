package me.vcb.astrocore.timer.scoreboard;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.vcb.astrocore.classes.manager.PvpClass;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.timer.type.PlayerTimer;
import me.vcb.astrocore.utils.StringUtils;
import me.vcb.astrocore.utils.Tasks;
import me.vcb.astrocore.utils.Tasks.Callable;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PvpClassWarmupTimer extends PlayerTimer {

    private final Table<UUID, String, ScheduledFuture<?>> warmups;

    public PvpClassWarmupTimer(ScheduledExecutorService executor) {
        super(executor, "PvpClassWarmupTimer", 0);

        this.warmups = HashBasedTable.create();
        this.setFormat(StringUtils.FormatType.MILLIS_TO_SECONDS);
    }

    @Override
    public void disable() {
        this.warmups.values().forEach(future -> future.cancel(true));
        this.warmups.clear();
    }

    public void activate(Player player, int delay, PvpClass pvpClass) {
        this.activate(player.getUniqueId(), delay, pvpClass);
    }

    public void activate(UUID uuid, int delay, PvpClass pvpClass) {
        String name = pvpClass.getName();
        if(delay <= 0 || this.isActive(uuid, name)) return;

        this.warmups.put(uuid, name, this.scheduleExpiry(uuid, name, delay,
            () -> Tasks.sync(() -> pvpClass.activateClass(uuid))));
    }

    public void cancel(Player player, String pvpClassName) {
        this.cancel(player.getUniqueId(), pvpClassName);
    }

    private void cancel(UUID uuid, String pvpClassName) {
        if(!this.isActive(uuid, pvpClassName)) return;
        this.warmups.remove(uuid, pvpClassName).cancel(true);
    }

    public boolean isActive(Player player, String pvpClassName) {
        return this.isActive(player.getUniqueId(), pvpClassName);
    }

    private boolean isActive(UUID uuid, String pvpClassName) {
        return this.warmups.contains(uuid, pvpClassName);
    }

    public long getCooldown(Player player, String pvpClassName) {
        return this.getCooldown(player.getUniqueId(), pvpClassName);
    }

    private long getCooldown(UUID uuid, String pvpClassName) {
        return this.warmups.get(uuid, pvpClassName).getDelay(TimeUnit.MILLISECONDS);
    }

    public String getTimeLeft(Player player, String pvpClassName) {
        return StringUtils.formatTime(this.getCooldown(player, pvpClassName), this.format);
    }

    public String getPlaceholder() {
        return Config.PVPCLASS_WARMUP_PLACEHOLDER;
    }

    public String getScoreboardEntry(Player player, String pvpClassName) {
        return "§c" + this.getTimeLeft(player, pvpClassName) + "s";
    }

    private ScheduledFuture<?> scheduleExpiry(UUID uuid, String pvpClassName, int delay, Callable callable) {
        return this.executor.schedule(() -> {
            try {
                this.warmups.remove(uuid, pvpClassName);
                callable.call();
            } catch(Throwable t) {
                t.printStackTrace();
            }
        }, delay, TimeUnit.SECONDS);
    }
}
