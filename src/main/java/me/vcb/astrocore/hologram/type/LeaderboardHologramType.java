package me.vcb.astrocore.hologram.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.leaderboard.entry.UuidCacheEntry;

import java.util.List;
import java.util.NavigableSet;
import java.util.function.Function;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum LeaderboardHologramType {

    PLAYER_KILLS(
        "playerKills",
        Language.HOLOGRAM_TOP_KILLS_LINE_FORMAT,
        Language.HOLOGRAM_TOP_KILLS_HEADER,
        Language.HOLOGRAM_TOP_KILLS_FOOTER,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopKills(),
        String::valueOf
    ),
    PLAYER_DEATHS(
        "playerDeaths",
        Language.HOLOGRAM_TOP_DEATHS_LINE_FORMAT,
        Language.HOLOGRAM_TOP_DEATHS_HEADER,
        Language.HOLOGRAM_TOP_DEATHS_FOOTER,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopDeaths(),
        String::valueOf
    ),
    PLAYER_BALANCE(
        "playerBalance",
        Language.HOLOGRAM_TOP_BALANCE_LINE_FORMAT,
        Language.HOLOGRAM_TOP_BALANCE_HEADER,
        Language.HOLOGRAM_TOP_BALANCE_FOOTER,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopBalance(),
        String::valueOf
    ),
    PLAYER_HIGHEST_KILLSTREAK(
        "playerKillstreak",
        Language.HOLOGRAM_TOP_KILLSTREAK_LINE_FORMAT,
        Language.HOLOGRAM_TOP_KILLSTREAK_HEADER,
        Language.HOLOGRAM_TOP_KILLSTREAK_FOOTER,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopHighestKillstreak(),
        String::valueOf
    ),
    FACTION_KILLS(
        "factionKills",
        Language.HOLOGRAM_FACTION_TOP_KILLS_LINE_FORMAT,
        Language.HOLOGRAM_FACTION_TOP_KILLS_HEADER,
        Language.HOLOGRAM_FACTION_TOP_KILLS_FOOTER,
        AstroCore.getInstance().getLeaderboardHandler().getFactionCacheHolder().getTopKills(),
        String::valueOf
    ),
    FACTION_POINTS(
        "factionPoints",
        Language.HOLOGRAM_FACTION_TOP_POINTS_LINE_FORMAT,
        Language.HOLOGRAM_FACTION_TOP_POINTS_HEADER,
        Language.HOLOGRAM_FACTION_TOP_POINTS_FOOTER,
        AstroCore.getInstance().getLeaderboardHandler().getFactionCacheHolder().getTopPoints(),
        String::valueOf
    ),
    FACTION_BALANCE(
        "factionBalance",
        Language.HOLOGRAM_FACTION_TOP_BALANCE_LINE_FORMAT,
        Language.HOLOGRAM_FACTION_TOP_BALANCE_HEADER,
        Language.HOLOGRAM_FACTION_TOP_BALANCE_FOOTER,
        AstroCore.getInstance().getLeaderboardHandler().getFactionCacheHolder().getTopBalance(),
        String::valueOf
    ),
    FACTION_KOTHS_CAPPED(
        "factionKoths",
        Language.HOLOGRAM_FACTION_TOP_KOTHS_CAPPED_LINE_FORMAT,
        Language.HOLOGRAM_FACTION_TOP_KOTHS_CAPPED_HEADER,
        Language.HOLOGRAM_FACTION_TOP_KOTHS_CAPPED_FOOTER,
        AstroCore.getInstance().getLeaderboardHandler().getFactionCacheHolder().getTopKothsCapped(),
        String::valueOf
    ),
    PLAYER_ELO(
        "playerElo",
        Language.HOLOGRAM_TOP_ELO_LINE_FORMAT,
        Language.HOLOGRAM_TOP_ELO_HEADER,
        Language.HOLOGRAM_TOP_ELO_FOOTER,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopElo(),
        String::valueOf
    ),
    PLAYER_KOTHS(
        "playerKoths",
        Language.HOLOGRAM_TOP_KOTHS_LINE_FORMAT,
        Language.HOLOGRAM_TOP_KOTHS_HEADER,
        Language.HOLOGRAM_TOP_KOTHS_FOOTER,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopKothsCapped(),
        String::valueOf
    ),
    PLAYER_BOUNTIES(
        "playerBounties",
        Language.HOLOGRAM_TOP_BOUNTIES_LINE_FORMAT,
        Language.HOLOGRAM_TOP_BOUNTIES_HEADER,
        Language.HOLOGRAM_TOP_BOUNTIES_FOOTER,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopBountiesCollected(),
        String::valueOf
    ),
    PLAYER_KD(
        "playerKD",
        Language.HOLOGRAM_TOP_KD_LINE_FORMAT,
        Language.HOLOGRAM_TOP_KD_HEADER,
        Language.HOLOGRAM_TOP_KD_FOOTER,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopKD(),
        v -> String.format("%.2f", v / 100.0)
    );

    private final String paramName;
    private final String lineFormat;
    private final List<String> header;
    private final List<String> footer;
    private final NavigableSet<UuidCacheEntry<Integer>> leaderboard;
    private final Function<Integer, String> valueFormatter;

    public String formatValue(int v) {
        return this.valueFormatter.apply(v);
    }

    public static LeaderboardHologramType getByName(String name) {
        return Stream.of(values()).filter(type -> type.getParamName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
