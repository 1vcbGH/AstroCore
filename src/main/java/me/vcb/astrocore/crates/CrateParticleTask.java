package me.vcb.astrocore.crates;

import me.vcb.astrocore.AstroCore;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Runs a per-crate rotating particle ring above the chest block.
 */
public class CrateParticleTask extends BukkitRunnable {

    private static final int    POINTS  = 12;   // particles per ring
    private static final double RADIUS  = 0.65;
    private static final double HEIGHT  = 0.55; // just above block surface

    private final List<Crate> crates;
    private int tick = 0;

    public CrateParticleTask(List<Crate> crates) {
        this.crates = crates;
        this.runTaskTimer(AstroCore.getInstance(), 0L, 4L);
    }

    @Override
    public void run() {
        tick++;
        for (Crate crate : crates) {
            if (!crate.hasLocation()) continue;
            spawnRing(crate);
        }
    }

    private void spawnRing(Crate crate) {
        Location center = crate.getChestLocation().clone().add(0.5, HEIGHT, 0.5);
        World    world  = center.getWorld();
        if (world == null) return;

        Particle particle = crate.getBukkitParticle();
        double   offset   = Math.toRadians(tick * 10); // rotate 10° per 4 ticks

        for (int i = 0; i < POINTS; i++) {
            double angle = offset + (2 * Math.PI / POINTS) * i;
            double x = center.getX() + Math.cos(angle) * RADIUS;
            double z = center.getZ() + Math.sin(angle) * RADIUS;
            world.spawnParticle(particle, x, center.getY(), z, 1, 0, 0, 0, 0);
        }
    }
}
