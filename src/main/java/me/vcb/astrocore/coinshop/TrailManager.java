package me.vcb.astrocore.coinshop;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.factions.type.SpawnFaction;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrailManager {

    @Getter private static TrailManager instance;

    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private BukkitTask task;

    public TrailManager() {
        instance = this;
        this.task = Tasks.syncTimer(this::tick, 3L, 3L);
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
            if (ud == null) continue;

            String trailId = ud.getSelectedTrail();
            if (trailId == null || trailId.equalsIgnoreCase("ninguno")) {
                lastLocations.put(player.getUniqueId(), player.getLocation());
                continue;
            }

            TrailType trailType = TrailType.fromId(trailId);
            if (trailType == null || trailType == TrailType.NINGUNO || trailType.getParticle() == null) {
                lastLocations.put(player.getUniqueId(), player.getLocation());
                continue;
            }

            // Trails only work in Spawn
            if (!(ClaimManager.getInstance().getFactionAt(player.getLocation()) instanceof SpawnFaction)) {
                lastLocations.put(player.getUniqueId(), player.getLocation());
                continue;
            }

            Location current = player.getLocation();
            Location last = lastLocations.get(player.getUniqueId());

            if (last != null && last.getWorld() != null && last.getWorld().equals(current.getWorld())
                    && last.distanceSquared(current) > 0.05) {
                Location spawnLoc = last.clone().add(0, 0.1, 0);
                player.getWorld().spawnParticle(
                    trailType.getParticle(),
                    spawnLoc,
                    trailType.getParticleCount(),
                    0.2, 0.1, 0.2,
                    0
                );
            }

            lastLocations.put(player.getUniqueId(), current);
        }
    }

    public void disable() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        lastLocations.clear();
    }
}
