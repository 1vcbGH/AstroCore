package me.vcb.astrocore.handlers.staff;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.event.AstroCoreKickEvent;
import me.vcb.astrocore.handlers.event.AstroCoreKickEvent.KickType;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.StringUtils;
import me.vcb.astrocore.utils.StringUtils.FormatType;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class RebootHandler extends Handler {

    private RebootTask rebootTask;

    public boolean isRebooting() {
        return this.rebootTask != null;
    }

    public void startReboot(int time) {
        this.rebootTask = new RebootTask(time);
    }

    public void cancelReboot() {
        this.rebootTask.cancel();
        this.rebootTask = null;

        Language.REBOOT_CANCELED.forEach(Messages::sendMessage);
    }

    public String getScoreboardEntry() {
        return StringUtils.formatTime(this.rebootTask.getSeconds(), FormatType.SECONDS_TO_MINUTES);
    }

    private static class RebootTask extends BukkitRunnable {

        @Getter private int seconds;

        RebootTask(int seconds) {
            this.seconds = seconds;
            this.broadcast();

            this.runTaskTimerAsynchronously(AstroCore.getInstance(), 0L, 20L);
        }

        @Override
        public void run() {
            if(--this.seconds <= 0) {
                this.cancel();

                Tasks.sync(() -> {
                    this.kickOnlinePlayers();
                    this.setupRestartScript();
                });

                Tasks.syncLater(() -> Bukkit.getServer().shutdown(), 20L);
                return;
            }

            if(this.seconds <= 10 || this.seconds == 15 || this.seconds == 30 || this.seconds == 45 || this.seconds % 60 == 0) {
                this.broadcast();
            }
        }

        private void broadcast() {
            Language.REBOOT_BROADCAST.forEach(line -> Messages.sendMessage(line
                .replace("<time>", StringUtils.formatDurationWords(this.seconds * 1000L))));
        }

        private void kickOnlinePlayers() {
            String kickReason = Language.REBOOT_PLAYER_KICK_MESSAGE;

            Bukkit.getOnlinePlayers().forEach(player -> {
                AstroCoreKickEvent event = new AstroCoreKickEvent(player, KickType.REBOOT, kickReason);

                if(!event.isCancelled()) {
                    player.kickPlayer(kickReason);
                }
            });
        }

        private String getRestartScriptPath() {
            try {
                File spigotYml = new File("spigot.yml");
                if (spigotYml.exists()) {
                    YamlConfiguration cfg = YamlConfiguration.loadConfiguration(spigotYml);
                    return cfg.getString("settings.restart-script", "./start.sh");
                }
            } catch (Exception ignored) {}
            return "./start.sh";
        }

        private void setupRestartScript() {
            File restartScript = new File(this.getRestartScriptPath());

            Thread shutdownHook = new Thread(() -> {
                try {
                    String os = System.getProperty("os.name").toLowerCase();

                    if (os.contains("win")) {
                        Runtime.getRuntime().exec("cmd /c start " + restartScript.getPath());
                    } else {
                        Runtime.getRuntime().exec(new String[] { "sh", restartScript.getPath() });
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            });

            shutdownHook.setDaemon(true);
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }
}
