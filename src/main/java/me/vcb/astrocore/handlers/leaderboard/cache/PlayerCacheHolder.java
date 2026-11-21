package me.vcb.astrocore.handlers.leaderboard.cache;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.vcb.astrocore.handlers.leaderboard.entry.UuidCacheEntry;

import java.util.concurrent.ConcurrentSkipListSet;

@Getter
@AllArgsConstructor
public class PlayerCacheHolder {

    @SerializedName("topKills")
    private ConcurrentSkipListSet<UuidCacheEntry<Integer>> topKills;

    @SerializedName("topDeaths")
    private ConcurrentSkipListSet<UuidCacheEntry<Integer>> topDeaths;

    @SerializedName("topBalance")
    private ConcurrentSkipListSet<UuidCacheEntry<Integer>> topBalance;

    @SerializedName("topHighestKillstreak")
    private ConcurrentSkipListSet<UuidCacheEntry<Integer>> topHighestKillstreak;

    @SerializedName("topElo")
    private ConcurrentSkipListSet<UuidCacheEntry<Integer>> topElo;

    @SerializedName("topKothsCapped")
    private ConcurrentSkipListSet<UuidCacheEntry<Integer>> topKothsCapped;

    @SerializedName("topBountiesCollected")
    private ConcurrentSkipListSet<UuidCacheEntry<Integer>> topBountiesCollected;

    @SerializedName("topKD")
    private ConcurrentSkipListSet<UuidCacheEntry<Integer>> topKD;

    public PlayerCacheHolder() {
        this.topKills = new ConcurrentSkipListSet<>();
        this.topDeaths = new ConcurrentSkipListSet<>();
        this.topBalance = new ConcurrentSkipListSet<>();
        this.topHighestKillstreak = new ConcurrentSkipListSet<>();
        this.topElo = new ConcurrentSkipListSet<>();
        this.topKothsCapped = new ConcurrentSkipListSet<>();
        this.topBountiesCollected = new ConcurrentSkipListSet<>();
        this.topKD = new ConcurrentSkipListSet<>();
    }

    public void clear() {
        this.topKills.clear();
        this.topDeaths.clear();
        this.topBalance.clear();
        this.topHighestKillstreak.clear();
        this.topElo.clear();
        this.topKothsCapped.clear();
        this.topBountiesCollected.clear();
        this.topKD.clear();
    }
}
