package me.vcb.astrocore.factions.bank;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Faction bank interest: every interval, each faction's bank balance grows by a
 * configured percentage (capped at the faction's max balance). Configurable in
 * bank.yml.
 */
public class FactionBankInterest {

    private static FactionBankInterest instance;

    private double ratePerHour = 0.02;   // 2% per hour by default
    private int    intervalMinutes = 60; // run every hour
    private int    minBalance = 1000;    // only pay interest above this
    private BukkitTask task;
    private final File file;

    public FactionBankInterest() {
        instance = this;
        this.file = new File(AstroCore.getInstance().getDataFolder(), "bank.yml");
        load();
        // syncTimer takes ticks; intervalMinutes * 60 * 20.
        long ticks = (long) this.intervalMinutes * 60L * 20L;
        this.task = Tasks.syncTimer(this::payInterest, ticks, ticks);
    }

    public static FactionBankInterest getInstance() { return instance; }

    public void disable() { if (this.task != null) this.task.cancel(); }

    private void load() {
        if (!this.file.exists()) {
            YamlConfiguration y = new YamlConfiguration();
            y.set("rate-per-hour", 0.02);
            y.set("interval-minutes", 60);
            y.set("min-balance", 1000);
            try { y.save(this.file); } catch (IOException ignored) {}
        }
        YamlConfiguration y = YamlConfiguration.loadConfiguration(this.file);
        this.ratePerHour = y.getDouble("rate-per-hour", 0.02);
        this.intervalMinutes = Math.max(1, y.getInt("interval-minutes", 60));
        this.minBalance = y.getInt("min-balance", 1000);
    }

    private void payInterest() {
        // Scale rate to the actual interval (rate is "per hour").
        double rate = this.ratePerHour * (this.intervalMinutes / 60.0);
        if (rate <= 0) return;

        for (Faction f : FactionsManager.getInstance().getFactions().values()) {
            if (!(f instanceof PlayerFaction faction)) continue;
            int bal = faction.getBalance();
            if (bal < this.minBalance) continue;

            int gain = (int) Math.floor(bal * rate);
            if (gain <= 0) continue;

            int cap = faction.getUpgradedMaxBalance();
            int newBal = Math.min(cap, bal + gain);
            gain = newBal - bal;
            if (gain <= 0) continue;

            faction.addBalance(gain);
            faction.addLog("&b% &7Interés del banco &8» &a+$" + gain);
            faction.sendMessage(Color.translate("&b&lBanco &7» &aTu facción ganó &6$" + gain
                + " &ade interés &7(" + (int) (rate * 100) + "%)."));
        }
    }
}
