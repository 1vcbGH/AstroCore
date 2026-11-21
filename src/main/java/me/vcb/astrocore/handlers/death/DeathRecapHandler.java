package me.vcb.astrocore.handlers.death;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeathRecapHandler extends Handler implements Listener {

    // Stores last 5 damage entries per player
    private final Map<UUID, List<DamageEntry>> damageLog = new java.util.HashMap<>();

    @Override
    public void disable() {
        damageLog.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        String source = "Ambiente";
        if(event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
            if(e.getDamager() instanceof Player) {
                source = ((Player) e.getDamager()).getName();
            } else if(e.getDamager() instanceof Projectile) {
                Projectile proj = (Projectile) e.getDamager();
                if(proj.getShooter() instanceof Player) {
                    source = ((Player) proj.getShooter()).getName() + " (flecha)";
                }
            } else {
                source = formatCause(e.getDamager().getType().name());
            }
        } else {
            source = formatCause(event.getCause().name());
        }

        List<DamageEntry> log = damageLog.computeIfAbsent(victim.getUniqueId(), k -> new ArrayList<>());
        log.add(new DamageEntry(source, event.getFinalDamage()));
        if(log.size() > 5) log.remove(0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<DamageEntry> log = damageLog.remove(player.getUniqueId());
        if(log == null || log.isEmpty()) return;

        player.sendMessage(Color.translate("&8&m                                          "));
        // Combat stats
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        String kdStr = "";
        if (ud != null) {
            int kills = ud.getKills();
            int deaths = ud.getDeaths() + 1; // +1 porque aún no se registró esta muerte
            String kd = deaths == 0 ? String.valueOf(kills) : String.format("%.2f", (double) kills / deaths);
            int streak = ud.getKillstreak();
            kdStr = " &8| &eK/D: &a" + kd + " &8| &eRacha: &c" + streak;
        }
        player.sendMessage(Color.translate("  &c&lDeath Recap &7- &fÚltimos golpes recibidos:" + kdStr));
        for(int i = log.size() - 1; i >= 0; i--) {
            DamageEntry entry = log.get(i);
            player.sendMessage(Color.translate("  &7* &e" + entry.source + " &7- &c" + String.format("%.1f", entry.damage) + " &7daño (&c"
                + String.format("%.1f", entry.damage / 2.0) + " &7corazones)"));
        }
        player.sendMessage(Color.translate("&8&m                                          "));
    }

    private String formatCause(String cause) {
        switch(cause) {
            case "FALL": return "Caida";
            case "FIRE": case "FIRE_TICK": return "Fuego";
            case "LAVA": return "Lava";
            case "DROWNING": return "Ahogamiento";
            case "VOID": return "Vacio";
            case "POISON": return "Veneno";
            case "WITHER": return "Wither";
            case "BLOCK_EXPLOSION": return "Explosion (TNT)";
            case "ENTITY_EXPLOSION": return "Explosion";
            default: return cause.replace("_", " ").toLowerCase();
        }
    }

    private static class DamageEntry {
        final String source;
        final double damage;

        DamageEntry(String source, double damage) {
            this.source = source;
            this.damage = damage;
        }
    }
}
