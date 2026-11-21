package me.vcb.astrocore.rank;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.events.bloodmoon.BloodMoonManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class RankListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        // Small delay so Userdata is fully loaded before applying prefix
        AstroCore.getInstance().getServer().getScheduler().runTaskLater(
            AstroCore.getInstance(),
            () -> {
                RankManager.getInstance().applyOnJoin(event.getPlayer());
                BloodMoonManager bm = BloodMoonManager.getInstance();
                if(bm != null) bm.addPlayerToBossBar(event.getPlayer());
                me.vcb.astrocore.events.eloboost.EloBoostManager eb =
                    me.vcb.astrocore.events.eloboost.EloBoostManager.getInstance();
                if(eb != null) eb.addPlayer(event.getPlayer());
            },
            5L
        );
    }
}
