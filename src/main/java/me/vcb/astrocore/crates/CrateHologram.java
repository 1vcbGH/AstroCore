package me.vcb.astrocore.crates;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.hologram.HologramEntry;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Lightweight hologram tied to a crate — uses the same TextDisplay system
 * as the existing HologramEntry but managed independently of HologramManager.
 */
public class CrateHologram {

    private static final double LINE_GAP    = 0.28;
    private static final double BASE_OFFSET = 1.6; // above chest block

    private final Crate              crate;
    private final List<HologramEntry> entries  = new ArrayList<>();
    private final Set<UUID>           viewers  = new HashSet<>();

    public CrateHologram(Crate crate) {
        this.crate = crate;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void spawn() {
        destroy();
        if (!crate.hasLocation()) return;

        Location base = crate.getChestLocation().clone()
            .add(0.5, BASE_OFFSET, 0.5);

        for (String rawLine : crate.getHologramLines()) {
            String translated = Color.translate(rawLine);
            HologramEntry entry = new HologramEntry(0, translated, base.clone());
            entry.spawnDisplay();
            entries.add(entry);
            base.subtract(0, LINE_GAP, 0);
        }
    }

    public void destroy() {
        entries.forEach(HologramEntry::removeDisplay);
        entries.clear();
        viewers.clear();
    }

    /** Rebuild hologram lines (after editing lines). */
    public void refresh() {
        spawn();
        for (UUID uuid : viewers) {
            Player p = org.bukkit.Bukkit.getPlayer(uuid);
            if (p != null) sendTo(p);
        }
    }

    // ── Visibility ────────────────────────────────────────────────────────────

    public void sendTo(Player player) {
        viewers.add(player.getUniqueId());
        for (HologramEntry entry : entries) {
            var display = entry.getDisplay();
            if (display != null && !display.isDead())
                player.showEntity(AstroCore.getInstance(), display);
        }
    }

    public void hideFrom(Player player) {
        viewers.remove(player.getUniqueId());
        for (HologramEntry entry : entries) {
            var display = entry.getDisplay();
            if (display != null && !display.isDead())
                player.hideEntity(AstroCore.getInstance(), display);
        }
    }

    public boolean isViewing(Player player) {
        return viewers.contains(player.getUniqueId());
    }

    /** Returns false if any TextDisplay entity has died (chunk unload, restart). */
    public boolean isAlive() {
        if (entries.isEmpty()) return false;
        for (HologramEntry entry : entries) {
            var display = entry.getDisplay();
            if (display == null || display.isDead()) return false;
        }
        return true;
    }

    public boolean isInSameWorld(Player player) {
        if (!crate.hasLocation()) return false;
        return player.getWorld().getName().equals(crate.getChestLocation().getWorld().getName());
    }

    public double distanceSquared(Player player) {
        try { return crate.getChestLocation().distanceSquared(player.getLocation()); }
        catch (Exception e) { return Double.MAX_VALUE; }
    }
}
