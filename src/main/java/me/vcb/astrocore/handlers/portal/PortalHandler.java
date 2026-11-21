package me.vcb.astrocore.handlers.portal;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.factions.type.SpawnFaction;
import me.vcb.astrocore.handlers.manager.Handler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PortalHandler extends Handler implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onNetherPortal(PlayerPortalEvent event) {
        if(!Config.NETHER_PORTAL_TRANSLATION_ENABLED || event.getCause() != TeleportCause.NETHER_PORTAL) return;

        World fromWorld = event.getFrom().getWorld();

        if(fromWorld.getEnvironment() == Environment.NORMAL) {

            Location newTo = event.getFrom().clone();
            newTo.setWorld(event.getTo().getWorld());
            newTo.setX(newTo.getX() / Config.NETHER_PORTAL_TRANSLATION_VALUE);
            newTo.setZ(newTo.getZ() / Config.NETHER_PORTAL_TRANSLATION_VALUE);

            event.setTo(newTo);

        } else if(fromWorld.getEnvironment() == Environment.NETHER) {

            Location newTo = event.getFrom().clone();
            newTo.setWorld(event.getTo().getWorld());
            newTo.setX(newTo.getX() * Config.NETHER_PORTAL_TRANSLATION_VALUE);
            newTo.setZ(newTo.getZ() * Config.NETHER_PORTAL_TRANSLATION_VALUE);

            event.setTo(newTo);

        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerPortal(PlayerPortalEvent event) {
        World fromWorld = event.getFrom().getWorld();
        World toWorld = event.getTo().getWorld();

        Environment environment = fromWorld.getEnvironment();
        TeleportCause cause = event.getCause();

        if(environment == Environment.NORMAL && cause == TeleportCause.END_PORTAL) {

            Location spawn = Config.WORLD_SPAWNS.get(Environment.THE_END);
            event.setTo(spawn == null ? toWorld.getSpawnLocation() : spawn);

        } else if(environment == Environment.THE_END && cause == TeleportCause.END_PORTAL) {

            Location exit = Config.WORLD_EXITS.get(Environment.THE_END);
            event.setTo(exit == null ? toWorld.getSpawnLocation() : exit);

        } else if(environment == Environment.NETHER && cause == TeleportCause.NETHER_PORTAL) {

            Faction factionAt = ClaimManager.getInstance().getFactionAt(event.getFrom());
            if(!(factionAt instanceof SpawnFaction)) return;

            Location exit = Config.WORLD_EXITS.get(Environment.NETHER);
            event.setTo(exit == null ? toWorld.getSpawnLocation() : exit);

        } else if(environment == Environment.NORMAL && cause == TeleportCause.NETHER_PORTAL) {

            Faction factionAt = ClaimManager.getInstance().getFactionAt(event.getFrom());
            if(!(factionAt instanceof SpawnFaction)) return;

            Location spawn = Config.WORLD_SPAWNS.get(Environment.NETHER);
            event.setTo(spawn == null ? toWorld.getSpawnLocation() : spawn);
        }
    }
}
