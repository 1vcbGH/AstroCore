package me.vcb.astrocore.crates;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CrateManager {

    private static final int RENDER_DISTANCE_SQ = 48 * 48;

    @lombok.Getter private static CrateManager instance;

    private final File                    file;
    private final File                    progressFile;
    private final List<Crate>             crates     = new ArrayList<>();
    private final Map<String, CrateHologram> holos   = new HashMap<>();
    private final Map<UUID, Long>         cooldowns  = new HashMap<>();
    // Per-player open counts per crate id: uuid -> (crateId -> count)
    private final Map<UUID, Map<String, Integer>> openCounts = new HashMap<>();
    private final Map<UUID, SpinAnimation> animating = new HashMap<>();
    // Players waiting to type a chat input for the editor
    private final Map<UUID, PendingInput> pendingInputs = new HashMap<>();

    private CrateParticleTask particleTask;
    private BukkitTask        renderTask;

    public CrateManager() {
        instance = this;
        this.file = new File(AstroCore.getInstance().getDataFolder(), "crates.yml");
        this.progressFile = new File(AstroCore.getInstance().getDataFolder(), "crate-progress.yml");
        load();
        loadProgress();
        startTasks();
        Bukkit.getPluginManager().registerEvents(
            new CrateListener(), AstroCore.getInstance());
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    private void startTasks() {
        this.particleTask = new CrateParticleTask(Collections.unmodifiableList(crates));
        // Render holograms every second
        this.renderTask = Tasks.syncTimer(this::tickRender, 20L, 20L);
    }

    public void disable() {
        save();
        if (particleTask != null) particleTask.cancel();
        if (renderTask   != null) renderTask.cancel();
        holos.values().forEach(CrateHologram::destroy);
        holos.clear();
    }

    // ── Hologram render ───────────────────────────────────────────────────────

    private void tickRender() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Crate crate : crates) {
                CrateHologram holo = holos.get(crate.getId());
                if (holo == null) continue;
                if (!holo.isInSameWorld(p)) {
                    if (holo.isViewing(p)) holo.hideFrom(p);
                    continue;
                }
                double dist = holo.distanceSquared(p);
                if (dist <= RENDER_DISTANCE_SQ) {
                    // Ensure TextDisplays are alive, respawn if dead
                    if (!holo.isAlive()) holo.spawn();
                    if (!holo.isViewing(p)) holo.sendTo(p);
                } else if (holo.isViewing(p)) {
                    holo.hideFrom(p);
                }
            }
        }
    }

    public void renderForPlayer(Player player) {
        for (Crate crate : crates) {
            CrateHologram holo = holos.get(crate.getId());
            if (holo == null) continue;
            if (!holo.isInSameWorld(player)) continue;
            if (holo.distanceSquared(player) <= RENDER_DISTANCE_SQ) {
                if (!holo.isAlive()) holo.spawn();
                holo.sendTo(player);
            }
        }
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public Crate createCrate(String id) {
        Crate crate = new Crate(id);
        crates.add(crate);
        CrateHologram holo = new CrateHologram(crate);
        holos.put(id, holo);
        save();
        return crate;
    }

    public boolean deleteCrate(String id) {
        Crate crate = getCrate(id);
        if (crate == null) return false;
        crates.remove(crate);
        CrateHologram holo = holos.remove(id);
        if (holo != null) holo.destroy();
        save();
        return true;
    }

    public Crate getCrate(String id) {
        for (Crate c : crates)
            if (c.getId().equalsIgnoreCase(id)) return c;
        return null;
    }

    public Crate getCrateByLocation(org.bukkit.Location loc) {
        for (Crate c : crates) {
            if (!c.hasLocation()) continue;
            org.bukkit.Location cl = c.getChestLocation();
            if (cl.getWorld().equals(loc.getWorld())
                && cl.getBlockX() == loc.getBlockX()
                && cl.getBlockY() == loc.getBlockY()
                && cl.getBlockZ() == loc.getBlockZ()) return c;
        }
        return null;
    }

    public List<Crate> getCrates() { return Collections.unmodifiableList(crates); }

    /** Destroy existing hologram for a crate (used before moving its location). */
    public void destroyHologram(String crateId) {
        CrateHologram holo = holos.remove(crateId);
        if (holo != null) holo.destroy();
    }

    /** Call after editing hologram lines or location. */
    public void refreshHologram(Crate crate) {
        CrateHologram holo = holos.get(crate.getId());
        if (holo == null) {
            holo = new CrateHologram(crate);
            holos.put(crate.getId(), holo);
        }
        holo.spawn();
        // Push to all nearby viewers immediately
        for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (holo.isInSameWorld(p) && holo.distanceSquared(p) <= 48 * 48)
                holo.sendTo(p);
        }
    }

    // ── Cooldown / animating ──────────────────────────────────────────────────

    public boolean isOnCooldown(UUID uuid) {
        Long last = cooldowns.get(uuid);
        return last != null && System.currentTimeMillis() - last < 500;
    }

    public void setCooldown(UUID uuid) {
        cooldowns.put(uuid, System.currentTimeMillis());
    }

    public boolean isAnimating(UUID uuid) {
        return animating.containsKey(uuid);
    }

    public void setAnimating(UUID uuid, SpinAnimation anim) {
        if (anim == null) animating.remove(uuid);
        else              animating.put(uuid, anim);
    }

    public SpinAnimation getAnimation(UUID uuid) {
        return animating.get(uuid);
    }

    // ── Pending chat input (editor) ───────────────────────────────────────────

    public void setPendingInput(UUID uuid, PendingInput input) {
        if (input == null) pendingInputs.remove(uuid);
        else               pendingInputs.put(uuid, input);
    }

    public PendingInput getPendingInput(UUID uuid) {
        return pendingInputs.get(uuid);
    }

    public boolean hasPendingInput(UUID uuid) {
        return pendingInputs.containsKey(uuid);
    }

    // ── Milestones / open tracking ──────────────────────────────────────────────

    /** Records one open for the player on this crate and fires milestone rewards. */
    public void recordOpenAndCheckMilestone(Player player, Crate crate) {
        Map<String, Integer> perCrate = openCounts.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        int count = perCrate.getOrDefault(crate.getId(), 0) + 1;
        perCrate.put(crate.getId(), count);
        saveProgress();

        int interval = crate.getMilestoneInterval();
        if (interval <= 0 || crate.getMilestoneCommands().isEmpty()) return;
        if (count % interval != 0) return;

        for (String cmd : crate.getMilestoneCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd
                .replace("%player%", player.getName())
                .replace("%crate%", crate.getId())
                .replace("%count%", String.valueOf(count)));
        }
        player.sendMessage(me.vcb.astrocore.utils.Color.translate(
            "§8[§6Crates§8] §a¡Milestone! §7Llevás §e" + count
            + " §7aperturas de §f" + crate.getDisplayName() + " §7— ¡recompensa extra!"));
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
    }

    public int getOpenCount(UUID uuid, String crateId) {
        Map<String, Integer> perCrate = openCounts.get(uuid);
        return perCrate == null ? 0 : perCrate.getOrDefault(crateId, 0);
    }

    /** Give a crate reward instantly without an animation (INSTANT type). */
    public void giveInstantReward(Player player, Crate crate) {
        CrateReward winner = crate.rollReward();
        if (winner == null) {
            player.sendMessage(me.vcb.astrocore.utils.Color.translate("§cEsta crate no tiene premios."));
            return;
        }

        org.bukkit.inventory.ItemStack item = winner.getItem();
        if (item != null) {
            if (player.getInventory().firstEmpty() == -1)
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            else
                player.getInventory().addItem(item);
        }
        for (String cmd : winner.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
        }

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

        recordOpenAndCheckMilestone(player, crate);
    }

    private void loadProgress() {
        if (!progressFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(progressFile);
        var section = yaml.getConfigurationSection("progress");
        if (section == null) return;
        for (String uuidStr : section.getKeys(false)) {
            var perSec = section.getConfigurationSection(uuidStr);
            if (perSec == null) continue;
            Map<String, Integer> perCrate = new HashMap<>();
            for (String crateId : perSec.getKeys(false)) perCrate.put(crateId, perSec.getInt(crateId));
            try { openCounts.put(UUID.fromString(uuidStr), perCrate); } catch (IllegalArgumentException ignored) {}
        }
    }

    private void saveProgress() {
        YamlConfiguration yaml = new YamlConfiguration();
        openCounts.forEach((uuid, perCrate) ->
            perCrate.forEach((crateId, count) ->
                yaml.set("progress." + uuid + "." + crateId, count)));
        try { yaml.save(progressFile); } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Crate crate : crates)
            crate.save(yaml.createSection("crates." + crate.getId()));
        try { yaml.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        var cratesSection = yaml.getConfigurationSection("crates");
        if (cratesSection == null) return;

        for (String id : cratesSection.getKeys(false)) {
            var section = cratesSection.getConfigurationSection(id);
            if (section == null) continue;
            Crate crate = Crate.load(id, section);
            crates.add(crate);
            CrateHologram holo = new CrateHologram(crate);
            holo.spawn();
            holos.put(id, holo);
        }
    }

    // ── Pending input record ──────────────────────────────────────────────────

    public record PendingInput(String crateId, InputType type, int rewardIndex) {
        public enum InputType {
            DISPLAY_NAME, HOLOGRAM_LINE_EDIT, HOLOGRAM_LINE_ADD, PARTICLE, SOUND,
            KEY_DESCRIPTION,
            MILESTONE_INTERVAL, MILESTONE_COMMAND_ADD,
            SLOT_CHANCE, SLOT_COMMAND
        }
    }
}
