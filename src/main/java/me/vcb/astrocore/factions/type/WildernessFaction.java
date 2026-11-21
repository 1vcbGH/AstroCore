package me.vcb.astrocore.factions.type;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.factions.Faction;
import org.bukkit.entity.Player;

public class WildernessFaction extends Faction {

    public WildernessFaction() {
        super(Config.WILDERNESS_NAME);
    }

    @Override
    public boolean shouldCancelPvpTimerEntrance(Player player) {
        return false;
    }
}
