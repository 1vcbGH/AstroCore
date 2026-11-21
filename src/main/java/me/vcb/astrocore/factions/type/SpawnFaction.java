package me.vcb.astrocore.factions.type;

import me.vcb.astrocore.utils.Color;
import org.bukkit.entity.Player;

public class SpawnFaction extends SystemFaction {

    public SpawnFaction() {
        super("Spawn");

        this.setColor(Color.translate("&a"));
        this.setDeathban(false);
        this.setSafezone(true);
    }

    @Override
    public boolean shouldCancelPvpTimerEntrance(Player player) {
        return false;
    }
}
