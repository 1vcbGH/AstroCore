package me.vcb.astrocore.npc;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.hologram.HologramEntry;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NpcHologram {

    private static final double BASE_OFFSET = 2.1; // above NPC head
    private static final double LINE_GAP    = 0.28;

    private final NpcData data;
    private final List<HologramEntry> entries = new ArrayList<>();

    public NpcHologram(NpcData data) {
        this.data = data;
    }

    public void spawn() {
        destroy();
        if (data.getLocation() == null) return;

        Location base = data.getLocation().clone().add(0, BASE_OFFSET, 0);

        String line = Color.translate(data.getDisplayName());
        HologramEntry entry = new HologramEntry(0, line, base);
        entry.spawnDisplay();
        entries.add(entry);
    }

    public void destroy() {
        entries.forEach(HologramEntry::removeDisplay);
        entries.clear();
    }

    public void showTo(Player p) {
        for (HologramEntry e : entries) {
            var d = e.getDisplay();
            if (d != null && !d.isDead())
                p.showEntity(AstroCore.getInstance(), d);
        }
    }

    public void hideFrom(Player p) {
        for (HologramEntry e : entries) {
            var d = e.getDisplay();
            if (d != null && !d.isDead())
                p.hideEntity(AstroCore.getInstance(), d);
        }
    }

    public boolean isAlive() {
        if (entries.isEmpty()) return false;
        for (HologramEntry e : entries) {
            var d = e.getDisplay();
            if (d == null || d.isDead()) return false;
        }
        return true;
    }
}
