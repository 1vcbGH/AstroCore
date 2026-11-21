package me.vcb.astrocore.crates.gui;

import me.vcb.astrocore.AstroCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks what GUI a player has open so clicks can be routed correctly.
 *
 * The `transitioning` set prevents InventoryCloseEvent from wiping state
 * when one GUI opens another in the same tick.
 *
 * Each mark* schedules a 1-tick cleanup of the transitioning flag so that
 * if there was no previous inventory open (no close event fires), the flag
 * doesn't get stuck and block the player's inventory after they close the GUI.
 */
public class CrateGuiTracker {

    public enum GuiType { EDITOR_LIST, EDITOR, REWARD_GRID, REWARD_SLOT_CONFIG, PREVIEW, PARTICLE_SELECT, SOUND_SELECT, HOLOGRAM_EDITOR, ANIMATION_SELECT }

    private record GuiState(GuiType type, String crateId, int rewardIndex) {}

    private static final Map<UUID, GuiState> states        = new HashMap<>();
    private static final Set<UUID>           transitioning  = new HashSet<>();

    // ── Mark (always call BEFORE player.openInventory) ────────────────────

    public static void markEditorList(Player p) {
        mark(p, new GuiState(GuiType.EDITOR_LIST, null, -1));
    }

    public static void markEditor(Player p, String crateId) {
        mark(p, new GuiState(GuiType.EDITOR, crateId, -1));
    }

    public static void markRewardGrid(Player p, String crateId) {
        mark(p, new GuiState(GuiType.REWARD_GRID, crateId, -1));
    }

    public static void markRewardSlotConfig(Player p, String crateId, int slot) {
        mark(p, new GuiState(GuiType.REWARD_SLOT_CONFIG, crateId, slot));
    }

    public static void markPreview(Player p, String crateId) {
        mark(p, new GuiState(GuiType.PREVIEW, crateId, -1));
    }

    public static void markParticleSelect(Player p, String crateId) {
        mark(p, new GuiState(GuiType.PARTICLE_SELECT, crateId, -1));
    }

    public static void markSoundSelect(Player p, String crateId) {
        mark(p, new GuiState(GuiType.SOUND_SELECT, crateId, -1));
    }

    public static void markHologramEditor(Player p, String crateId) {
        mark(p, new GuiState(GuiType.HOLOGRAM_EDITOR, crateId, -1));
    }

    public static void markAnimationSelect(Player p, String crateId) {
        mark(p, new GuiState(GuiType.ANIMATION_SELECT, crateId, -1));
    }

    private static void mark(Player p, GuiState state) {
        UUID uuid = p.getUniqueId();
        transitioning.add(uuid);
        states.put(uuid, state);
        // If no previous inventory was open, openInventory() won't fire a close event
        // so the transitioning flag would never be consumed. Clean it up after 1 tick.
        Bukkit.getScheduler().runTaskLater(AstroCore.getInstance(),
            () -> transitioning.remove(uuid), 1L);
    }

    // ── Clear ─────────────────────────────────────────────────────────────

    /**
     * Called by InventoryCloseEvent. Only clears if NOT transitioning.
     */
    public static void clearIfNotTransitioning(Player p) {
        UUID uuid = p.getUniqueId();
        if (transitioning.remove(uuid)) return; // opening new GUI — keep state
        states.remove(uuid);
    }

    /** Hard clear — call on PlayerQuitEvent only. */
    public static void clear(Player p) {
        states.remove(p.getUniqueId());
        transitioning.remove(p.getUniqueId());
    }

    // ── Getters ───────────────────────────────────────────────────────────

    public static GuiType getType(Player p) {
        GuiState s = states.get(p.getUniqueId());
        return s == null ? null : s.type();
    }

    public static String getCrateId(Player p) {
        GuiState s = states.get(p.getUniqueId());
        return s == null ? null : s.crateId();
    }

    public static int getRewardIndex(Player p) {
        GuiState s = states.get(p.getUniqueId());
        return s == null ? -1 : s.rewardIndex();
    }

    public static boolean isInCrateGui(Player p) { return states.containsKey(p.getUniqueId()); }
}
