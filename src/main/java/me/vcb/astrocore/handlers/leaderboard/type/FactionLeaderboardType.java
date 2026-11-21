package me.vcb.astrocore.handlers.leaderboard.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.event.FactionDataType;
import me.vcb.astrocore.handlers.leaderboard.entry.UuidCacheEntry;
import me.vcb.astrocore.hologram.type.LeaderboardHologramType;

import java.util.NavigableSet;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum FactionLeaderboardType implements LeaderboardType {

    FACTION_KILLS(
        "kills",
        Language.LEADERBOARDS_FACTION_KILLS_TITLE,
        Language.LEADERBOARDS_FACTION_KILLS_LINE_FORMAT,
        LeaderboardHologramType.FACTION_KILLS,
        AstroCore.getInstance().getLeaderboardHandler().getFactionCacheHolder().getTopKills()
    ),
    FACTION_POINTS(
        "points",
        Language.LEADERBOARDS_FACTION_POINTS_TITLE,
        Language.LEADERBOARDS_FACTION_POINTS_LINE_FORMAT,
        LeaderboardHologramType.FACTION_POINTS,
        AstroCore.getInstance().getLeaderboardHandler().getFactionCacheHolder().getTopPoints()
    ),
    FACTION_BALANCE(
        "balance",
        Language.LEADERBOARDS_FACTION_BALANCE_TITLE,
        Language.LEADERBOARDS_FACTION_BALANCE_LINE_FORMAT,
        LeaderboardHologramType.FACTION_BALANCE,
        AstroCore.getInstance().getLeaderboardHandler().getFactionCacheHolder().getTopBalance()
    ),
    FACTION_KOTHS_CAPPED(
        "koths",
        Language.LEADERBOARDS_FACTION_TOP_KOTHS_CAPPED_TITLE,
        Language.LEADERBOARDS_FACTION_TOP_KOTHS_CAPPED_LINE_FORMAT,
        LeaderboardHologramType.FACTION_KOTHS_CAPPED,
        AstroCore.getInstance().getLeaderboardHandler().getFactionCacheHolder().getTopKothsCapped()
    );

    private final String paramName;
    private final String title;
    private final String lineFormat;
    private final LeaderboardHologramType hologramType;
    private final NavigableSet<UuidCacheEntry<Integer>> leaderboard;

    public static FactionLeaderboardType getByName(String name) {
        return Stream.of(values()).filter(type -> type.getParamName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static FactionLeaderboardType getFromFactionDataType(FactionDataType type) {
        switch(type) {
            case KILLS: return FactionLeaderboardType.FACTION_KILLS;
            case POINTS: return FactionLeaderboardType.FACTION_POINTS;
            case BALANCE: return FactionLeaderboardType.FACTION_BALANCE;
            case KOTHS_CAPPED: return FactionLeaderboardType.FACTION_KOTHS_CAPPED;
        }

        throw new IllegalArgumentException("FactionLeaderboardType for '" + type.name() + "' couldn't be found");
    }

    public static FactionDataType getFactionDataTypeFrom(FactionLeaderboardType type) {
        switch(type) {
            case FACTION_KILLS: return FactionDataType.KILLS;
            case FACTION_POINTS: return FactionDataType.POINTS;
            case FACTION_BALANCE: return FactionDataType.BALANCE;
            case FACTION_KOTHS_CAPPED: return FactionDataType.KOTHS_CAPPED;
        }

        throw new IllegalArgumentException("FactionDataType for '" + type.name() + "' couldn't be found");
    }
}
