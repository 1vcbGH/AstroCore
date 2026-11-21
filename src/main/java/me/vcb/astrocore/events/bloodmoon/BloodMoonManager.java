package me.vcb.astrocore.events.bloodmoon;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class BloodMoonManager {

    @Getter private static BloodMoonManager instance;

    @Getter private boolean active;
    private int timeRemaining; // seconds

    private BossBar bossBar;
    private BukkitTask timerTask;

    // Original world state to restore on end
    private long originalTime;
    private boolean originalStorm;
    private boolean originalThunder;

    public BloodMoonManager() {
        instance = this;
    }

    public void disable() {
        if(this.active) this.end(false);
        instance = null;
    }

    public boolean start(int seconds) {
        if(this.active) return false;

        this.active = true;
        this.timeRemaining = seconds;

        // Save + set world state
        World world = Bukkit.getWorlds().get(0);
        this.originalTime = world.getTime();
        this.originalStorm = world.hasStorm();
        this.originalThunder = world.isThundering();

        world.setTime(18000); // midnight
        world.setStorm(true);
        world.setThundering(true);
        world.setWeatherDuration(seconds * 20);
        world.setThunderDuration(seconds * 20);

        // Boss bar
        this.bossBar = Bukkit.createBossBar(
            "§4☾ §c§lLUNA SANGRIENTA §4☾  §7| §eELO x2 activo",
            BarColor.RED, BarStyle.SOLID
        );
        for(Player p : Bukkit.getOnlinePlayers()) this.bossBar.addPlayer(p);

        // Announce
        this.broadcastStart();

        // Countdown task
        this.timerTask = Tasks.syncTimer(() -> {
            this.timeRemaining--;
            this.updateBossBarProgress();

            if(this.timeRemaining <= 0) {
                this.end(true);
            }
        }, 20L, 20L);

        return true;
    }

    public void end(boolean announce) {
        if(!this.active) return;
        this.active = false;

        if(this.timerTask != null) {
            this.timerTask.cancel();
            this.timerTask = null;
        }

        if(this.bossBar != null) {
            this.bossBar.removeAll();
            this.bossBar = null;
        }

        // Restore world
        World world = Bukkit.getWorlds().get(0);
        world.setTime(this.originalTime);
        world.setStorm(this.originalStorm);
        world.setThundering(this.originalThunder);

        if(announce) this.broadcastEnd();
    }

    public void addPlayerToBossBar(Player player) {
        if(this.active && this.bossBar != null) {
            this.bossBar.addPlayer(player);
        }
    }

    private void updateBossBarProgress() {
        // bossBar progress updated separately if we had stored max — keep it full
        // (or we can track maxTime and compute ratio)
        if(this.bossBar != null) {
            // title updates every 10s with remaining time
            if(this.timeRemaining % 10 == 0) {
                this.bossBar.setTitle("§4☾ §c§lLUNA SANGRIENTA §4☾  §7| §eELO x2 §8| §7" + formatTime(this.timeRemaining));
            }
        }
    }

    private void broadcastStart() {
        String[] lines = {
            "§4§m                                                  ",
            "   §c§l☾ LUNA SANGRIENTA ☽",
            " ",
            "   §7La luna se tiñe de sangre sobre el servidor.",
            "   §7Durante este evento, §c§ltodo el ELO ganado",
            "   §7por kills §cse duplica§7.",
            " ",
            "§4§m                                                  "
        };
        for(String line : lines) Bukkit.broadcastMessage(line);
    }

    private void broadcastEnd() {
        String[] lines = {
            "§8§m                                                  ",
            "   §7§l☾ Luna Sangrienta §r§7— Terminada",
            "   §7El ELO vuelve a su valor normal.",
            "§8§m                                                  "
        };
        for(String line : lines) Bukkit.broadcastMessage(line);
    }

    private String formatTime(int seconds) {
        if(seconds >= 3600) return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
        if(seconds >= 60) return (seconds / 60) + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }
}
