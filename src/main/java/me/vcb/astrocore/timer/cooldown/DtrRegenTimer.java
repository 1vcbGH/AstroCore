package me.vcb.astrocore.timer.cooldown;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.timer.type.SystemTimer;
import me.vcb.astrocore.utils.Tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DtrRegenTimer extends SystemTimer {

    private final Set<PlayerFaction> regeneratingFactions;

    public DtrRegenTimer(ScheduledExecutorService executor) {
        super(executor, "DtrRegenTimer", 0, false);

        this.regeneratingFactions = new HashSet<>();
    }

    @Override
    public void cancel() {
        if(this.timer != null) {
            this.timer.cancel(true);
        }

        this.regeneratingFactions.clear();
    }

    public void startRegenTask() {
        this.timer = this.scheduleExpiry();

        for(Faction faction : FactionsManager.getInstance().getFactions().values()) {
            if(!(faction instanceof PlayerFaction)) continue;

            PlayerFaction playerFaction = (PlayerFaction) faction;
            this.addFaction(playerFaction, playerFaction.getDtr());
        }
    }

    public void addFaction(PlayerFaction playerFaction, double factionDtr) {
        if(factionDtr < playerFaction.getMaxDtr() && !playerFaction.isFrozen()) {
            this.regeneratingFactions.add(playerFaction);
        }
    }

    public void removeFaction(PlayerFaction playerFaction) {
        this.regeneratingFactions.remove(playerFaction);
    }

    private void handleDtrUpdate() {
        if(this.regeneratingFactions.isEmpty()) return;

        Tasks.sync(() -> {
            for(PlayerFaction faction : new ArrayList<>(this.regeneratingFactions)) {
                faction.setDtr(faction.getDtr() + Config.FACTION_DTR_REGEN_PER_MINUTE);
            }
        });
    }

    private ScheduledFuture<?> scheduleExpiry() {
        return this.executor.scheduleAtFixedRate(() -> {
            try {
                this.handleDtrUpdate();
            } catch(Throwable t) {
                t.printStackTrace();
            }
        }, 1L, 1L, TimeUnit.MINUTES);
    }
}
