package me.vcb.astrocore.events.eloboost;

import lombok.Getter;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class EloBoostManager {

    @Getter private static EloBoostManager instance;

    @Getter private boolean active;
    @Getter private float multiplier;
    private int totalSeconds;
    private int timeRemaining;
    private BossBar bossBar;
    private BukkitTask timerTask;

    public EloBoostManager() {
        instance = this;
    }

    public boolean start(int seconds, float multiplier) {
        if (this.active) return false;
        this.active     = true;
        this.multiplier = multiplier;
        this.totalSeconds   = seconds;
        this.timeRemaining  = seconds;

        this.bossBar = Bukkit.createBossBar(
            buildBarTitle(), BarColor.YELLOW, BarStyle.SEGMENTED_10
        );
        Bukkit.getOnlinePlayers().forEach(p -> this.bossBar.addPlayer(p));

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§e§l★ ELO BOOST ACTIVADO §e§l★");
        Bukkit.broadcastMessage("  §7ELO por kill: §a×" + fmtMult() + " §7durante §e" + fmtTime(seconds));
        Bukkit.broadcastMessage("");

        this.timerTask = Tasks.syncTimer(() -> {
            this.timeRemaining--;
            double prog = Math.max(0.0, (double) this.timeRemaining / this.totalSeconds);
            this.bossBar.setProgress(prog);
            this.bossBar.setTitle(buildBarTitle());
            if (this.timeRemaining <= 0) this.end(true);
        }, 20L, 20L);

        return true;
    }

    public void end(boolean natural) {
        if (!this.active) return;
        this.active = false;
        if (this.timerTask != null) { this.timerTask.cancel(); this.timerTask = null; }
        if (this.bossBar  != null) { this.bossBar.removeAll(); this.bossBar  = null; }
        if (natural) Bukkit.broadcastMessage("§e§l★ ELO BOOST §7terminado. ELO volvió a normal.");
    }

    public void addPlayer(Player player) {
        if (this.active && this.bossBar != null) this.bossBar.addPlayer(player);
    }

    public void disable() {
        this.end(false);
        instance = null;
    }

    private String buildBarTitle() {
        return "§e§l⚡ ELO BOOST §7×" + fmtMult() + " §8| §7" + fmtTime(this.timeRemaining) + " restante";
    }

    private String fmtMult() {
        return multiplier == (int) multiplier ? String.valueOf((int) multiplier) : String.valueOf(multiplier);
    }

    private String fmtTime(int s) {
        if (s >= 3600) return (s / 3600) + "h " + ((s % 3600) / 60) + "m";
        if (s >= 60)   return (s / 60) + "m " + (s % 60) + "s";
        return s + "s";
    }
}
