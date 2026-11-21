package me.vcb.astrocore.handlers.leaderboard.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.leaderboard.entry.UuidCacheEntry;
import me.vcb.astrocore.hologram.type.LeaderboardHologramType;
import me.vcb.astrocore.userdata.event.UserdataValueType;

import java.util.NavigableSet;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum PlayerLeaderboardType implements LeaderboardType {

    KILLS(
        "kills",
        Language.LEADERBOARDS_KILLS_TITLE,
        Language.LEADERBOARDS_KILLS_LINE_FORMAT,
        LeaderboardHologramType.PLAYER_KILLS,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopKills()
    ),
    DEATHS(
        "deaths",
        Language.LEADERBOARDS_DEATHS_TITLE,
        Language.LEADERBOARDS_DEATHS_LINE_FORMAT,
        LeaderboardHologramType.PLAYER_DEATHS,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopDeaths()
    ),
    BALANCE(
        "balance",
        Language.LEADERBOARDS_BALANCE_TITLE,
        Language.LEADERBOARDS_BALANCE_LINE_FORMAT,
        LeaderboardHologramType.PLAYER_BALANCE,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopBalance()
    ),
    HIGHEST_KILLSTREAK(
        "killstreak",
        Language.LEADERBOARDS_KILLSTREAK_TITLE,
        Language.LEADERBOARDS_KILLSTREAK_LINE_FORMAT,
        LeaderboardHologramType.PLAYER_HIGHEST_KILLSTREAK,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopHighestKillstreak()
    ),
    ELO(
        "elo",
        Language.LEADERBOARDS_ELO_TITLE,
        Language.LEADERBOARDS_ELO_LINE_FORMAT,
        LeaderboardHologramType.PLAYER_ELO,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopElo()
    ),
    KOTHS(
        "koths",
        Language.LEADERBOARDS_KOTHS_TITLE,
        Language.LEADERBOARDS_KOTHS_LINE_FORMAT,
        LeaderboardHologramType.PLAYER_KOTHS,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopKothsCapped()
    ),
    BOUNTIES(
        "bounties",
        Language.LEADERBOARDS_BOUNTIES_TITLE,
        Language.LEADERBOARDS_BOUNTIES_LINE_FORMAT,
        LeaderboardHologramType.PLAYER_BOUNTIES,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopBountiesCollected()
    ),
    KD(
        "kd",
        Language.LEADERBOARDS_KD_TITLE,
        Language.LEADERBOARDS_KD_LINE_FORMAT,
        LeaderboardHologramType.PLAYER_KD,
        AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder().getTopKD()
    );

    private final String paramName;
    private final String title;
    private final String lineFormat;
    private final LeaderboardHologramType hologramType;
    private final NavigableSet<UuidCacheEntry<Integer>> leaderboard;

    public static PlayerLeaderboardType getByName(String name) {
        return Stream.of(values()).filter(type -> type.getParamName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static PlayerLeaderboardType getFromUserdataValueType(UserdataValueType type) {
        switch(type) {
            case KILLS: return PlayerLeaderboardType.KILLS;
            case DEATHS: return PlayerLeaderboardType.DEATHS;
            case BALANCE: return PlayerLeaderboardType.BALANCE;
            case HIGHEST_KILLSTREAK: return PlayerLeaderboardType.HIGHEST_KILLSTREAK;
            case ELO: return PlayerLeaderboardType.ELO;
            case KOTHS_CAPPED: return PlayerLeaderboardType.KOTHS;
            case BOUNTIES_COLLECTED: return PlayerLeaderboardType.BOUNTIES;
            case KD: return PlayerLeaderboardType.KD;
        }

        throw new IllegalArgumentException("PlayerLeaderboardType for '" + type.name() + "' couldn't be found");
    }

    public static UserdataValueType getUserdataValueTypeFrom(PlayerLeaderboardType type) {
        switch(type) {
            case KILLS: return UserdataValueType.KILLS;
            case DEATHS: return UserdataValueType.DEATHS;
            case BALANCE: return UserdataValueType.BALANCE;
            case HIGHEST_KILLSTREAK: return UserdataValueType.HIGHEST_KILLSTREAK;
            case ELO: return UserdataValueType.ELO;
            case KOTHS: return UserdataValueType.KOTHS_CAPPED;
            case BOUNTIES: return UserdataValueType.BOUNTIES_COLLECTED;
            case KD: return UserdataValueType.KD;
        }

        throw new IllegalArgumentException("UserdataValueType for '" + type.name() + "' couldn't be found");
    }
}
