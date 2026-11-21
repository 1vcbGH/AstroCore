package me.vcb.astrocore.supplydrop;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.hologram.impl.StaticHologram;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

@Getter
public class SupplyDrop {

    public static final int LIFETIME_SECONDS = 15 * 60; // 15 min

    private final Location chestLocation;
    private final List<ItemStack> loot;
    private boolean opened = false;

    private StaticHologram hologram;
    private BossBar bossBar;
    private BukkitTask tickTask;
    private int secondsLeft;

    private Runnable onExpire;

    public SupplyDrop(Location chestLocation, List<ItemStack> loot, Runnable onExpire) {
        this.chestLocation = chestLocation.clone();
        this.loot = loot;
        this.secondsLeft = LIFETIME_SECONDS;
        this.onExpire = onExpire;

        chestLocation.getBlock().setType(Material.CHEST);

        spawnHolograms();

        spawnBossBar();
        startTick();
    }

    // ── Holograms ─────────────────────────────────────────────────────────

    private void spawnHolograms() {
        // Top line at Y+1.9, second line 0.3 below — StaticHologram subtracts 0.3 per line
        Location holoLoc = chestLocation.clone().add(0.5, 1.9, 0.5);

        int fakeId = -( (int)(chestLocation.getBlockX() * 31 + chestLocation.getBlockZ()) & 0x7FFFFFFF);
        hologram = new StaticHologram(fakeId, holoLoc) {
            @Override public void createHologramLines() {
                this.getEntries().clear();
                Location loc = holoLoc.clone();
                addEntry("&6&l✈ SUPPLY DROP &6&l✈", loc);
                addEntry("&eDesaparece en: &c" + formatTime(secondsLeft), loc = getLineLocation(loc));
            }
        };
        hologram.createHologramLines();

        // Register into HologramManager so HologramRenderTask handles show/hide automatically
        AstroCore.getInstance().getHologramManager().addHologram(hologram);

        // Show immediately to all online players
        Bukkit.getOnlinePlayers().forEach(hologram::sendHologram);
    }

    private void updateHolograms() {
        if (hologram == null) return;
        hologram.updateMessage(1, "&eDesaparece en: &c" + formatTime(secondsLeft));
        hologram.refreshForViewers();
    }

    public void removeHolograms() {
        if (hologram == null) return;
        hologram.forEachViewer(hologram::removeHologram);
        hologram.getEntries().forEach(e -> e.removeDisplay());
        AstroCore.getInstance().getHologramManager().removeHologramById(hologram.getId());
        hologram = null;
    }

    // ── BossBar ───────────────────────────────────────────────────────────

    private void spawnBossBar() {
        bossBar = Bukkit.createBossBar(buildBossBarTitle(), BarColor.YELLOW, BarStyle.SEGMENTED_10);
        bossBar.setProgress(1.0);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
    }

    private String buildBossBarTitle() {
        return "§6§l✈ Supply Drop §8| §eX:" + chestLocation.getBlockX()
            + " Z:" + chestLocation.getBlockZ()
            + " §8| §c" + formatTime(secondsLeft);
    }

    public void addPlayerToBossBar(Player player) {
        if (bossBar != null) bossBar.addPlayer(player);
    }

    private void updateBossBar() {
        if (bossBar == null) return;
        bossBar.setTitle(buildBossBarTitle());
        bossBar.setProgress(Math.max(0.0, (double) secondsLeft / LIFETIME_SECONDS));
        bossBar.setColor(secondsLeft <= 120 ? BarColor.RED : BarColor.YELLOW);
    }

    private void removeBossBar() {
        if (bossBar == null) return;
        bossBar.removeAll();
        bossBar = null;
    }

    // ── Tick ──────────────────────────────────────────────────────────────

    private void startTick() {
        tickTask = Tasks.syncTimer(() -> {
            secondsLeft--;
            updateHolograms();
            updateBossBar();

            if (secondsLeft <= 0) {
                tickTask.cancel();
                onExpire.run();
            }
        }, 20L, 20L);
    }

    // ── Cleanup ───────────────────────────────────────────────────────────

    public void destroy() {
        if (tickTask != null) { tickTask.cancel(); tickTask = null; }
        removeHolograms();
        removeBossBar();
        Block b = chestLocation.getBlock();
        if (b.getType() == Material.CHEST) b.setType(Material.AIR);
    }

    public void markOpened() { this.opened = true; }

    // ── Utils ─────────────────────────────────────────────────────────────

    static String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%d:%02d", m, s);
    }
}
