package me.vcb.astrocore.hologram.task;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.hologram.Hologram;
import me.vcb.astrocore.hologram.HologramManager;
import me.vcb.astrocore.hologram.impl.LeaderboardHologram;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HologramRenderTask extends BukkitRunnable {

    private static final int DISTANCE = 48 * 48;
    private final HologramManager handler;

    public HologramRenderTask(HologramManager handler) {
        this.handler = handler;
        this.runTaskTimer(AstroCore.getInstance(), 0L, 20L);
    }

    public void renderOrRemoveHolograms(Player player) {
        for (Hologram hologram : this.handler.getHolograms()) {
            this.renderOrRemoveHologram(player, hologram);
        }
    }

    private void renderOrRemoveHologram(Player player, Hologram hologram) {
        if (!hologram.isInSameWorld(player)) return;

        UUID playerUuid = player.getUniqueId();
        Set<UUID> viewers = hologram.getViewers();
        int distance = this.getHologramDistance(player, hologram);

        if (distance > DISTANCE && viewers.contains(playerUuid)) {
            viewers.remove(playerUuid);
            hologram.removeHologram(player);
            hologram.hideItemDisplay(player);
        } else if (distance <= DISTANCE && !viewers.contains(playerUuid)) {
            viewers.add(playerUuid);
            hologram.sendHologram(player);
            hologram.sendItemDisplay(player);
        }
    }

    private int getHologramDistance(Player player, Hologram hologram) {
        try {
            return (int) Math.round(hologram.getLocation().distanceSquared(player.getLocation()));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public void run() {
        List<Hologram> holograms = this.handler.getHolograms();
        if (holograms == null || holograms.isEmpty()) return;

        for (Hologram hologram : holograms) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.renderOrRemoveHologram(player, hologram);
            }

            if (hologram instanceof LeaderboardHologram leaderboard) {
                if (leaderboard.getUpdate().get()) {
                    leaderboard.updateHologramLines();
                    leaderboard.refreshForViewers();
                }
            }

            hologram.tickItemDisplayRotation();
        }
    }
}
