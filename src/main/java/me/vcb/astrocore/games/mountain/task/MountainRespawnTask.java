package me.vcb.astrocore.games.mountain.task;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.mountain.MountainData;
import me.vcb.astrocore.games.mountain.MountainManager;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.StringUtils;
import me.vcb.astrocore.utils.StringUtils.FormatType;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.scheduler.BukkitRunnable;

public class MountainRespawnTask extends BukkitRunnable {

    private final MountainManager manager;
    @Getter private long nextRespawn;

    public MountainRespawnTask(MountainManager manager) {
        this.manager = manager;
        this.runTaskTimerAsynchronously(AstroCore.getInstance(), 0L, 20L);
    }

    private int getNextRespawnInSeconds() {
        return (int) ((this.nextRespawn - System.currentTimeMillis()) / 1000);
    }

    public String getNextRespawnString() {
        return Language.MOUNTAIN_NEXT_RESPAWN.replace("<time>", StringUtils
        .formatTime(this.getNextRespawnInSeconds(), FormatType.SECONDS_TO_MINUTES));
    }

    @Override
    public void run() {
        if(this.manager.getMountains().isEmpty()) return;

        int nextInSeconds = this.getNextRespawnInSeconds();

        if(nextInSeconds <= 0) {
            this.nextRespawn = System.currentTimeMillis() + (Config.MOUNTAIN_RESPAWN_INTERVAL * 1000L);

            Language.MOUNTAIN_RESPAWNED.forEach(line -> Messages.sendMessage(line
            .replace("<time>", String.valueOf(Config.MOUNTAIN_RESPAWN_INTERVAL / 60))));

            Tasks.sync(() -> this.manager.getMountains().forEach(MountainData::respawn));
            return;
        }

        int messageInterval = Config.MOUNTAIN_MESSAGE_INTERVAL;

        if(messageInterval != 0 && nextInSeconds % messageInterval == 0) {
            Messages.sendMessage(this.getNextRespawnString());
        }
    }
}
