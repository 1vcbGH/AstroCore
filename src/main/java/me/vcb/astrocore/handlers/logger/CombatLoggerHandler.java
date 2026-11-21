package me.vcb.astrocore.handlers.logger;

import lombok.Setter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.handlers.event.CombatLoggerSpawnEvent;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatLoggerHandler extends Handler implements Listener {

    /** UUIDs currently going through a combat-logger synthetic death — all other PlayerDeathEvent handlers must skip these. */
    private static final java.util.Set<UUID> CLOG_DYING = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    @Setter private boolean kickAll;
    private final Map<UUID, CombatLogger> combatLoggers;

    public CombatLoggerHandler() {
        this.combatLoggers = new HashMap<>();
        NmsUtils.getInstance().registerCombatLogger(Config.COMBAT_LOGGER_TYPE);
    }

    @Override
    public void disable() {
        this.combatLoggers.values().forEach(CombatLogger::removeCombatLogger);
        this.combatLoggers.clear();
    }

    public CombatLogger removeCombatLogger(UUID uuid) {
        return this.combatLoggers.remove(uuid);
    }

    /** Returns true if this death should skip normal stat/message processing (already handled by NPC death). */
    public static boolean isCombatLogDeath(Player player) {
        return CLOG_DYING.contains(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.removeMetadata("Logout", AstroCore.getInstance());

        CombatLogger combatLogger = this.removeCombatLogger(player.getUniqueId());
        if (combatLogger == null) return;

        if (combatLogger.wasKilled()) {
            // NPC already processed kills/stats/messages — just show death screen, suppress duplicate processing
            Location deathLoc = combatLogger.getKillLocation();
            if (deathLoc != null) player.teleport(deathLoc);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            // Mark so PlayerDeathEvent handlers skip reprocessing
            CLOG_DYING.add(player.getUniqueId());
            player.setHealth(0.0);
            return;
        }

        player.setHealth(combatLogger.getCombatLoggerHealth());
        player.teleport(combatLogger.getCombatLoggerLocation());

        combatLogger.handleEffectChanges(player);
        combatLogger.removeCombatLogger();
    }

    /** At LOWEST: suppress drops/message. At MONITOR: clean up the set. Other handlers check isCombatLogDeath(). */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCombatLogDeathLow(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!CLOG_DYING.contains(player.getUniqueId())) return;
        event.setKeepInventory(true);
        event.setDroppedExp(0);
        event.getDrops().clear();
        event.setDeathMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCombatLogDeathMonitor(PlayerDeathEvent event) {
        CLOG_DYING.remove(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (this.kickAll || player.getHealth() == 0.0 || player.hasPermission("astrocore.combatlogger.bypass")) return;

        if (player.hasMetadata("logout")) {
            player.removeMetadata("logout", AstroCore.getInstance());
            return;
        }

        if (TimerManager.getInstance().getPvpProtTimer().isActive(player)) return;
        if (!TimerManager.getInstance().getCombatTagTimer().isActive(player)) return;
        if (AstroCore.getInstance().getSotwHandler().isUnderSotwProtection(player)) return;
        if (AstroCore.getInstance().getStaffModeManager().isInStaffModeOrVanished(player)) return;
        if (ClaimManager.getInstance().getFactionAt(player).isSafezone()) return;

        CombatLoggerSpawnEvent loggerEvent = new CombatLoggerSpawnEvent(player);

        if (!loggerEvent.isCancelled()) {
            this.combatLoggers.put(player.getUniqueId(), NmsUtils.getInstance()
                .spawnCombatLogger(player.getWorld(), player, Config.COMBAT_LOGGER_TYPE));
        }
    }
}
