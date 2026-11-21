package me.vcb.astrocore.crates;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
@Setter
public class Crate {

    private String       id;
    private String       displayName;
    private Location     chestLocation;
    private List<String> hologramLines;
    private String       particle;
    private String       sound;
    private String       keyMaterial    = "TRIPWIRE_HOOK";
    private String       keyDescription = "&7Llave para la crate &e%crate%";
    private List<CrateReward> rewards;

    // Milestone rewards: every <milestoneInterval> opens, run milestoneCommands.
    // 0 = disabled. Placeholders: %player% %crate% %count%
    private int          milestoneInterval = 0;
    private List<String> milestoneCommands;

    // Opening animation style
    private CrateAnimationType animationType = CrateAnimationType.SPIN;

    public Crate(String id) {
        this.id           = id;
        this.displayName  = "&f&l" + id.toUpperCase();
        this.hologramLines = new ArrayList<>();
        this.hologramLines.add("&f&l✦ " + id.toUpperCase() + " &f&l✦");
        this.hologramLines.add("&7Click derecho para abrir");
        this.hologramLines.add("&8Click izquierdo para ver premios");
        this.particle       = "HAPPY_VILLAGER";
        this.sound          = "BLOCK_CHEST_OPEN";
        this.keyMaterial    = "TRIPWIRE_HOOK";
        this.keyDescription = "&7Llave para la crate &e%crate%";
        this.rewards        = new ArrayList<>();
        this.milestoneInterval = 0;
        this.milestoneCommands = new ArrayList<>();
    }

    // ── Util ─────────────────────────────────────────────────────────────────

    public boolean hasLocation() {
        return chestLocation != null && chestLocation.getWorld() != null;
    }

    public Particle getBukkitParticle() {
        try   { return Particle.valueOf(particle); }
        catch (Exception e) { return Particle.HAPPY_VILLAGER; }
    }

    public Sound getBukkitSound() {
        try   { return Sound.valueOf(sound); }
        catch (Exception e) { return Sound.BLOCK_CHEST_OPEN; }
    }

    /** Weighted random roll — only from winnable rewards. */
    public CrateReward rollReward() {
        List<CrateReward> pool = rewards.stream()
            .filter(CrateReward::isWinnable).toList();
        if (pool.isEmpty()) return null;
        int total = pool.stream().mapToInt(CrateReward::getChance).sum();
        if (total <= 0) return pool.get(0);
        int roll = new Random().nextInt(total);
        int acc  = 0;
        for (CrateReward r : pool) {
            acc += r.getChance();
            if (roll < acc) return r;
        }
        return pool.get(pool.size() - 1);
    }

    /** Helper to get reward at a specific grid slot, or null. */
    public CrateReward getRewardAtSlot(int slot) {
        for (CrateReward r : rewards)
            if (r.getSlot() == slot) return r;
        return null;
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void save(ConfigurationSection section) {
        section.set("display-name",    displayName);
        section.set("hologram-lines",  hologramLines);
        section.set("particle",         particle);
        section.set("sound",            sound);
        section.set("key-material",     keyMaterial);
        section.set("key-description",  keyDescription);
        section.set("milestone-interval", milestoneInterval);
        section.set("milestone-commands", milestoneCommands);
        section.set("animation-type",     animationType.name());

        if (chestLocation != null && chestLocation.getWorld() != null) {
            section.set("location.world", chestLocation.getWorld().getName());
            section.set("location.x",     chestLocation.getBlockX());
            section.set("location.y",     chestLocation.getBlockY());
            section.set("location.z",     chestLocation.getBlockZ());
        }

        section.set("rewards", null); // clear old
        ConfigurationSection rewardsSec = section.createSection("rewards");
        for (int i = 0; i < rewards.size(); i++)
            rewards.get(i).save(rewardsSec.createSection(String.valueOf(i)));
    }

    public static Crate load(String id, ConfigurationSection section) {
        Crate c = new Crate(id);
        c.displayName = section.getString("display-name", c.displayName);

        List<String> lines = section.getStringList("hologram-lines");
        if (!lines.isEmpty()) c.hologramLines = new ArrayList<>(lines);

        c.particle       = section.getString("particle",        "HAPPY_VILLAGER");
        c.sound          = section.getString("sound",           "BLOCK_CHEST_OPEN");
        c.keyMaterial    = section.getString("key-material",    "TRIPWIRE_HOOK");
        c.keyDescription = section.getString("key-description", "&7Llave para la crate &e%crate%");
        c.milestoneInterval = section.getInt("milestone-interval", 0);
        c.milestoneCommands = new ArrayList<>(section.getStringList("milestone-commands"));
        c.animationType  = CrateAnimationType.fromName(section.getString("animation-type", "SPIN"));

        ConfigurationSection loc = section.getConfigurationSection("location");
        if (loc != null) {
            String worldName = loc.getString("world", "world");
            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world != null) {
                c.chestLocation = new Location(world,
                    loc.getDouble("x"), loc.getDouble("y"), loc.getDouble("z"));
            }
        }

        ConfigurationSection rewardsSec = section.getConfigurationSection("rewards");
        if (rewardsSec != null) {
            for (String key : rewardsSec.getKeys(false)) {
                ConfigurationSection rs = rewardsSec.getConfigurationSection(key);
                if (rs != null) c.rewards.add(CrateReward.load(rs));
            }
        }
        return c;
    }
}
