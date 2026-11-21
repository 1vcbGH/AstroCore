package me.vcb.astrocore.userdata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.rank.PlayerRank;
import me.vcb.astrocore.userdata.event.UserdataValueChangeEvent;
import me.vcb.astrocore.userdata.event.UserdataValueType;
import me.vcb.astrocore.userdata.settings.Settings;
import org.apache.commons.lang.time.FastDateFormat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Userdata {

    private UUID uuid;
    private String name;

    private int kills;
    private int deaths;
    private int killstreak;
    private int highestKillstreak;

    private int balance;
    private int lives;
    private int spawnCredits;

    private boolean reclaimUsed;
    private PlayerRank rank;
    private int elo;
    private int kothsCapped;
    private int bountiesCollected;
    private int falltrapTokens;   // tokens to use /f falltrap (bought in /perks)
    private int baseTokens;       // tokens to use /f base (bought in /perks)
    private int skyBaseTokens;    // tokens to use /f skybase (bought in /perks)
    private String selectedDeathMessage;
    private java.util.List<String> purchasedDeathMessages;
    private Settings settings;

    // Tags system
    private java.util.Set<String> ownedTags;  // tag IDs owned (rango always owned)
    private String activeTagId;               // currently equipped tag ID (null = rango)

    // --- transient (not persisted) ---
    private transient int deathStreak;

    private String lastIp;
    private String selectedKillEffect;       // nombre del enum KillEffect, null = ninguno
    private java.util.Set<String> ownedKillEffects; // IDs de kill effects comprados

    // Daily missions
    private java.util.Map<String, Integer> missionProgress;
    private java.util.Set<String> missionsClaimed;
    private long missionsDay;
    // Battlepass
    private int battlepassXp;
    private java.util.Set<Integer> battlepassClaimed;

    private List<UUID> ignoring;
    private List<UUID> friends;
    private List<UUID> pendingFriendRequests; // requests recibidas (persistentes)
    private List<String> notes;
    private List<String> lastKills;
    private List<String> lastDeaths;
    private Map<String, Long> kitDelays;

    // Cosmetics: Trails
    private String selectedTrail;
    private java.util.Set<String> ownedTrails;

    // Cosmetics: Chat Colors
    private String selectedChatColor;
    private java.util.Set<String> ownedChatColors;

    // Cosmetics: Killstreak Messages
    private String selectedKillstreakMessage;
    private java.util.Set<String> ownedKillstreakMessages;

    private transient Location lastLocation;

    public Userdata(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        // Set fields directly to avoid firing async events during login
        this.kills = 0;
        this.deaths = 0;
        this.highestKillstreak = 0;
        this.balance = Config.DEFAULT_BALANCE_PLAYER;

        this.elo = me.vcb.astrocore.rank.PlayerRank.BASE_ELO;
        this.lives = Config.DEFAULT_LIVES;
        this.spawnCredits = Config.SPAWN_CREDITS_INITIAL_SPAWN_CREDITS;
        this.settings = new Settings();
        this.purchasedDeathMessages = new ArrayList<>();
        this.ownedTags = new java.util.HashSet<>();
        this.ownedTags.add("rango");
        this.activeTagId = "rango";
        this.ignoring = new ArrayList<>();
        this.friends = new ArrayList<>();
        this.pendingFriendRequests = new ArrayList<>();
        this.notes = new ArrayList<>();
        this.lastKills = new ArrayList<>();
        this.lastDeaths = new ArrayList<>();
        this.kitDelays = new HashMap<>();
        this.ownedTrails = new java.util.HashSet<>();
        this.ownedChatColors = new java.util.HashSet<>();
        this.ownedChatColors.add("default");
        this.ownedKillstreakMessages = new java.util.HashSet<>();
        this.ownedKillstreakMessages.add("default");
    }

    public int getElo() {
        return this.elo <= 0 ? me.vcb.astrocore.rank.PlayerRank.BASE_ELO : this.elo;
    }

    public void addElo(int amount) {
        this.elo = Math.max(0, this.getElo() + amount);
        new UserdataValueChangeEvent(this, UserdataValueType.ELO);
    }

    public void removeElo(int amount) {
        this.elo = Math.max(0, this.getElo() - amount);
        new UserdataValueChangeEvent(this, UserdataValueType.ELO);
    }

    public void addKothCapped() {
        this.kothsCapped++;
        new UserdataValueChangeEvent(this, UserdataValueType.KOTHS_CAPPED);
    }

    public void addBountiesCollected(int amount) {
        this.bountiesCollected += amount;
        new UserdataValueChangeEvent(this, UserdataValueType.BOUNTIES_COLLECTED);
    }

    public int getDeathStreak() { return this.deathStreak; }
    public void incrementDeathStreak() { this.deathStreak++; }
    public void resetDeathStreak() { this.deathStreak = 0; }

    public java.util.List<String> getPurchasedDeathMessages() {
        if (this.purchasedDeathMessages == null) this.purchasedDeathMessages = new ArrayList<>();
        return this.purchasedDeathMessages;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public void changeBalance(int value) {
        this.balance = value;
        new UserdataValueChangeEvent(this, UserdataValueType.BALANCE);
    }

    public void updateKillStats(String killMessage) {
        this.addKill();
        this.addLastKill(killMessage);
    }

    public void addKill() {
        this.changeKills(this.kills + 1);
    }

    public void changeKills(int value) {
        this.kills = value;
        new UserdataValueChangeEvent(this, UserdataValueType.KILLS);
        new UserdataValueChangeEvent(this, UserdataValueType.KD);
    }

    public void updateDeathStats(String killMessage) {
        this.addDeath();
        this.addLastDeath(killMessage);
    }

    public void addDeath() {
        this.changeDeaths(this.deaths + 1);
    }

    public void changeDeaths(int value) {
        this.deaths = value;
        new UserdataValueChangeEvent(this, UserdataValueType.DEATHS);
        new UserdataValueChangeEvent(this, UserdataValueType.KD);
    }

    public void changeHighestKillstreak(int value) {
        this.highestKillstreak = value;
        new UserdataValueChangeEvent(this, UserdataValueType.HIGHEST_KILLSTREAK);
    }

    public void addKillstreak() {
        this.killstreak++;

        if(this.killstreak > this.highestKillstreak) {
            this.changeHighestKillstreak(this.killstreak);
        }
    }

    public int resetKillstreak() {
        int temp = this.killstreak;
        this.killstreak = 0;

        return temp;
    }

    // -----------------------------------------------------------------------
    // Tags
    // -----------------------------------------------------------------------

    public java.util.Set<String> getOwnedTags() {
        if(this.ownedTags == null) {
            this.ownedTags = new java.util.HashSet<>();
            this.ownedTags.add("rango");
        }
        return this.ownedTags;
    }

    public boolean ownsTag(me.vcb.astrocore.tags.ChatTag tag) {
        return tag.isFree() || this.getOwnedTags().contains(tag.getId());
    }

    public void giveTag(me.vcb.astrocore.tags.ChatTag tag) {
        this.getOwnedTags().add(tag.getId());
    }

    public String getActiveTagId() {
        return this.activeTagId == null ? "rango" : this.activeTagId;
    }

    public me.vcb.astrocore.tags.ChatTag getActiveTag() {
        if(this.activeTagId == null || this.activeTagId.equals("ninguno")) return null;
        return me.vcb.astrocore.tags.ChatTag.fromId(this.activeTagId);
    }

    public void equipTag(me.vcb.astrocore.tags.ChatTag tag) {
        this.activeTagId = tag == null ? "ninguno" : tag.getId();
    }

    /** True if the player has no tag equipped (hidden). */
    public boolean isTagHidden() {
        return this.activeTagId == null || this.activeTagId.equals("ninguno");
    }

    public void addLives(int amount) {
        this.lives += amount;
    }

    public void subtractLives(int amount) {
        this.lives -= amount;
    }

    public void decrementSpawnCredits() {
        this.spawnCredits--;
    }

    public void changeSpawnCredits(int value) {
        this.spawnCredits = value;
    }

    public java.util.Set<String> getOwnedKillEffects() {
        return this.ownedKillEffects;
    }

    public java.util.Set<String> getOrCreateOwnedKillEffects() {
        if (this.ownedKillEffects == null) this.ownedKillEffects = new java.util.HashSet<>();
        return this.ownedKillEffects;
    }

    public List<UUID> getPendingFriendRequests() {
        if (this.pendingFriendRequests == null) this.pendingFriendRequests = new ArrayList<>();
        return this.pendingFriendRequests;
    }

    public void addPendingFriendRequest(UUID from) {
        if (!this.getPendingFriendRequests().contains(from)) this.getPendingFriendRequests().add(from);
    }

    public void removePendingFriendRequest(UUID from) {
        this.getPendingFriendRequests().remove(from);
    }

    public List<UUID> getFriends() {
        if (this.friends == null) this.friends = new ArrayList<>();
        return this.friends;
    }

    public boolean isFriendWith(UUID uuid) {
        return this.getFriends().contains(uuid);
    }

    public void addFriend(UUID uuid) {
        if (!this.getFriends().contains(uuid)) this.getFriends().add(uuid);
    }

    public void removeFriend(UUID uuid) {
        this.getFriends().remove(uuid);
    }

    public boolean isIgnoring(Player player) {
        return this.ignoring.contains(player.getUniqueId());
    }

    public void addIgnoring(Player player) {
        this.ignoring.add(player.getUniqueId());
    }

    public void removeIgnoring(Player player) {
        this.ignoring.remove(player.getUniqueId());
    }

    public void addLastKill(String killMessage) {
        this.addLastKillOrDeathEntry(this.lastKills, killMessage);
    }

    public void addLastDeath(String deathMessage) {
        this.addLastKillOrDeathEntry(this.lastDeaths, deathMessage);
    }

    public java.util.Set<String> getOwnedTrails() {
        if (this.ownedTrails == null) this.ownedTrails = new java.util.HashSet<>();
        return this.ownedTrails;
    }

    public java.util.Set<String> getOwnedChatColors() {
        if (this.ownedChatColors == null) {
            this.ownedChatColors = new java.util.HashSet<>();
            this.ownedChatColors.add("default");
        }
        return this.ownedChatColors;
    }

    public java.util.Set<String> getOwnedKillstreakMessages() {
        if (this.ownedKillstreakMessages == null) {
            this.ownedKillstreakMessages = new java.util.HashSet<>();
            this.ownedKillstreakMessages.add("default");
        }
        return this.ownedKillstreakMessages;
    }

    private void addLastKillOrDeathEntry(List<String> list, String message) {
        FastDateFormat fastDateFormat = FastDateFormat.getInstance(Config
            .DATE_FORMAT, Config.TIMEZONE, Locale.ENGLISH);

        message = message.replace('§', '&');

        if(list.size() < 5) {
            list.add(0, fastDateFormat.format(System.currentTimeMillis()) + " - " + message);
            return;
        }

        list.remove(4);
        list.add(0, fastDateFormat.format(System.currentTimeMillis()) + " - " + message);
    }
}
