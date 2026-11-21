package me.vcb.astrocore.handlers.leaderboard.type;

import me.vcb.astrocore.handlers.leaderboard.entry.UuidCacheEntry;
import me.vcb.astrocore.hologram.type.LeaderboardHologramType;

import java.util.NavigableSet;

public interface LeaderboardType {

    String getTitle();
    String getLineFormat();
    LeaderboardHologramType getHologramType();
    NavigableSet<UuidCacheEntry<Integer>> getLeaderboard();
}
