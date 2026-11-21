package me.vcb.astrocore.handlers.leaderboard.event;

import lombok.Getter;
import me.vcb.astrocore.handlers.leaderboard.type.LeaderboardType;
import me.vcb.astrocore.hologram.type.LeaderboardHologramType;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class LeaderboardUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final LeaderboardHologramType type;

    public LeaderboardUpdateEvent(LeaderboardType type) {
        this.type = type.getHologramType();
        Bukkit.getPluginManager().callEvent(this);
    }

    @Override
    public HandlerList getHandlers() { return handlers; }

    public static HandlerList getHandlerList() { return handlers; }
}