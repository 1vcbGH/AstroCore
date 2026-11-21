package me.vcb.astrocore.afk;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.hologram.HologramEntry;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * AFK Pool: players standing in the "AFKPool" system-faction claim earn a reward
 * every {@link #INTERVAL_SECONDS}. Shows a live actionbar timer and an info
 * hologram. The zone is defined with /f claimfor AFKPool (axe selection).
 */
public class AfkManager {

    public static final String FACTION_NAME = "AFKPool";
    private static final int INTERVAL_SECONDS = 300;   // 5 minutes
    private static final int REWARD_MONEY     = 1000;  // money per interval
    private static final String HOLO_TAG = "kc_afk_holo";

    private static AfkManager instance;

    private final Map<UUID, Integer> seconds = new HashMap<>();
    private final Set<UUID> inZone = new HashSet<>();
    private BukkitTask task;

    public AfkManager() {
        instance = this;
        this.task = Tasks.syncTimer(this::tick, 20L, 20L);
    }

    public static AfkManager getInstance() { return instance; }

    public void disable() { if (this.task != null) this.task.cancel(); }

    // ── Tick (every second) ───────────────────────────────────────────────

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();

            if (!isInZone(player)) {
                this.seconds.remove(id);
                this.inZone.remove(id);
                continue;
            }

            if (this.inZone.add(id)) {
                // just entered
                player.sendTitle(Color.translate("&d&l✦ Zona AFK ✦"),
                    Color.translate("&fPróxima recompensa: &e" + format(INTERVAL_SECONDS)), 5, 40, 10);
            }

            int s = this.seconds.getOrDefault(id, 0) + 1;
            if (s >= INTERVAL_SECONDS) {
                s = 0;
                reward(player);
            }
            this.seconds.put(id, s);

            int remaining = INTERVAL_SECONDS - s;
            player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(
                Color.translate("&d&lZona AFK &8| &7Próxima recompensa: &e" + format(remaining))));
        }
    }

    private void reward(Player player) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud != null) ud.changeBalance(ud.getBalance() + REWARD_MONEY);
        player.sendMessage(Color.translate("&d&lZona AFK &7» &aRecompensa: &6$" + REWARD_MONEY + "&a."));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);
    }

    private boolean isInZone(Player player) {
        Faction at = ClaimManager.getInstance().getFactionAt(player.getLocation());
        return at != null && at.getName().equalsIgnoreCase(FACTION_NAME);
    }

    // ── Hologram ────────────────────────────────────────────────────────────

    public void spawnHologram(Location loc) {
        String text = String.join("\n",
            Color.translate("&d&l✦ Zona AFK ✦"),
            Color.translate("&fQuedate acá para ganar recompensas"),
            Color.translate("&fcada &d5 minutos&f!"),
            "",
            Color.translate("&d&lPremios"),
            Color.translate("&7» &aDinero &8(&fComún&8)"),
            "",
            Color.translate("&7Usá &f/spawn &7para volver"));
        loc.getWorld().spawn(loc, TextDisplay.class, e -> {
            e.text(HologramEntry.buildComponent(text));
            e.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
            e.setSeeThrough(true);
            e.setPersistent(true);
            e.setShadowed(true);
            e.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
            e.addScoreboardTag(HOLO_TAG);
        });
    }

    /** Removes AFK holograms within a region (used before re-spawning on re-claim). */
    public void clearHolograms(World world, int minX, int maxX, int minZ, int maxZ) {
        world.getEntities().forEach(en -> {
            if (!(en instanceof TextDisplay) || !en.getScoreboardTags().contains(HOLO_TAG)) return;
            int x = en.getLocation().getBlockX(), z = en.getLocation().getBlockZ();
            if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) en.remove();
        });
    }

    /** Removes every AFK hologram in every world. */
    public void clearAllHolograms() {
        for (World w : Bukkit.getWorlds())
            w.getEntities().forEach(en -> {
                if (en instanceof TextDisplay && en.getScoreboardTags().contains(HOLO_TAG)) en.remove();
            });
    }

    private static String format(int totalSeconds) {
        int m = totalSeconds / 60, s = totalSeconds % 60;
        return String.format("%d:%02d", m, s);
    }
}
