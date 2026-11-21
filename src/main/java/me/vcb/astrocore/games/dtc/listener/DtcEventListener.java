package me.vcb.astrocore.games.dtc.listener;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.games.dtc.event.DtcDestroyedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DtcEventListener implements Listener {

    public DtcEventListener() {
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    @EventHandler
    public void onDtcDestroy(DtcDestroyedEvent event) {
        Player winner = event.getWinner();
        AstroCore.getInstance().getLootManager().getLootByName("DTC").handleRewards(winner);

        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(winner);

        if(faction != null) {
            faction.incrementPoints(Config.FACTION_TOP_DTC_DESTROY);
        }
    }
}
