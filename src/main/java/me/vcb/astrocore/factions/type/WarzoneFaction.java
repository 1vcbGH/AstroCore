package me.vcb.astrocore.factions.type;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.factions.Faction;
import org.bukkit.entity.Player;

public class WarzoneFaction extends Faction {

    public WarzoneFaction() {
        super(Config.WARZONE_NAME);
    }

    @Override
    public boolean shouldCancelPvpTimerEntrance(Player player) {
        return false;
    }
}
