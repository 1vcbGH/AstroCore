package me.vcb.astrocore.rank;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.games.koth.event.KothCappedEvent;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.userdata.UserdataManager;
import me.vcb.astrocore.userdata.event.UserdataValueChangeEvent;
import me.vcb.astrocore.userdata.event.UserdataValueType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class RankManager implements Listener {

    @Getter private static RankManager instance;

    // Top-3 tags shown in chat
    public static final String[] TOP_TAGS = {
        " §6[🏆 #1]",
        " §7[🥈 #2]",
        " §c[🥉 #3]"
    };

    public RankManager() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public void disable() {
        instance = null;
    }

    // -----------------------------------------------------------------------
    // Top-3 (computed live — fast enough for game servers)
    // -----------------------------------------------------------------------

    /**
     * Returns 1, 2, or 3 if the UUID is in the global ELO top-3.
     * Returns 0 otherwise. Computed live from loaded userdatas.
     */
    public int getTopPosition(UUID uuid) {
        UserdataManager um = AstroCore.getInstance().getUserdataManager();
        List<EloEntry> entries = new ArrayList<>();
        for (Userdata ud : um.getAllLoadedUserdata()) {
            entries.add(new EloEntry(ud.getUuid(), ud.getElo()));
        }
        entries.sort(Comparator.comparingInt(EloEntry::getElo).reversed());
        for (int i = 0; i < Math.min(3, entries.size()); i++) {
            if (entries.get(i).getUuid().equals(uuid)) return i + 1;
        }
        return 0;
    }

    /** Returns the top-3 tag string for a player, or empty string if not in top 3. */
    public String getChatTag(UUID uuid) {
        int pos = this.getTopPosition(uuid);
        return (pos >= 1 && pos <= 3) ? TOP_TAGS[pos - 1] : "";
    }

    private static class EloEntry {
        private final UUID uuid;
        private final int elo;
        EloEntry(UUID uuid, int elo) { this.uuid = uuid; this.elo = elo; }
        UUID getUuid() { return uuid; }
        int getElo() { return elo; }
    }

    // -----------------------------------------------------------------------
    // Rank resolution & assignment
    // -----------------------------------------------------------------------

    public PlayerRank getRank(Userdata userdata) {
        if (userdata.getRank() == null) {
            PlayerRank computed = PlayerRank.fromElo(userdata.getElo());
            userdata.setRank(computed);
        }
        return userdata.getRank();
    }

    public void setRank(Player player, Userdata userdata, PlayerRank rank) {
        userdata.setRank(rank);
        this.applyTeamPrefix(player, rank);
    }

    public void checkRankUp(Player player, Userdata userdata) {
        PlayerRank current = userdata.getRank() == null ? PlayerRank.MORTAL : userdata.getRank();
        PlayerRank earned = PlayerRank.fromElo(userdata.getElo());

        if (earned.ordinal() > current.ordinal()) {
            userdata.setRank(earned);
            this.applyTeamPrefix(player, earned);
            this.sendRankUpNotification(player, earned);
        } else if (earned.ordinal() < current.ordinal()) {
            userdata.setRank(earned);
            this.applyTeamPrefix(player, earned);
            player.sendMessage("§c§l[Rango] §r§7Tu ELO bajó. Ahora eres " + earned.getColoredName() + "§7.");
        }
    }

    // -----------------------------------------------------------------------
    // ELO helpers
    // -----------------------------------------------------------------------

    public void applyDeathElo(Player victim) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(victim);
        if (ud == null) return;
        ud.removeElo(PlayerRank.ELO_PER_DEATH);
        victim.sendMessage("§c-§e" + PlayerRank.ELO_PER_DEATH + " ELO §7(muerte) §8| §7ELO: §e" + ud.getElo());
        this.checkRankUp(victim, ud);
    }

    // -----------------------------------------------------------------------
    // Scoreboard team prefix
    // -----------------------------------------------------------------------

    public void applyTeamPrefix(Player player, PlayerRank rank) {
        if (player == null || !player.isOnline()) return;
        me.vcb.astrocore.scoreboard.ScoreboardManager.getInstance().updateTabRelations(player, false);
    }

    // -----------------------------------------------------------------------
    // Rank-up notification
    // -----------------------------------------------------------------------

    private void sendRankUpNotification(Player player, PlayerRank rank) {
        player.sendTitle(rank.getColor() + "✦ " + rank.getDisplayName() + " ✦", "§7¡Has subido de rango!", 10, 60, 20);
        player.sendMessage("§6§l[Rango] §r§7¡Felicitaciones! Ahora eres " + rank.getColoredName() + "§7!");
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        if (rank == PlayerRank.OLYMPUS) {
            Bukkit.broadcastMessage("§c§l[OLYMPUS] §r§e" + player.getName() + " §7ha alcanzado el rango máximo §c§lOlympus§7! §c✦");
        }
    }

    // -----------------------------------------------------------------------
    // Global ELO leaderboard
    // -----------------------------------------------------------------------

    public void sendGlobalLeaderboard(CommandSender sender, int limit) {
        UserdataManager um = AstroCore.getInstance().getUserdataManager();

        List<Userdata> sorted = new ArrayList<>(um.getAllLoadedUserdata());
        sorted.sort(Comparator.comparingInt(Userdata::getElo).reversed());

        String[] posLabels = { "§6[🏆]", "§7[🥈]", "§c[🥉]" };

        sender.sendMessage("§8§m                                        ");
        sender.sendMessage("§6§l  🏆 TOP ELO GLOBAL");
        sender.sendMessage("§8§m                                        ");

        int count = Math.min(limit, sorted.size());
        for (int i = 0; i < count; i++) {
            Userdata ud = sorted.get(i);
            PlayerRank rank = ud.getRank() != null ? ud.getRank() : PlayerRank.fromElo(ud.getElo());
            String pos = i < 3 ? posLabels[i] : "§7  " + (i + 1) + ".";
            sender.sendMessage(pos + " §f" + ud.getName()
                + " §8| §eELO: §f" + ud.getElo()
                + " §8| " + rank.getColoredName()
                + " §8| §eKills: §f" + ud.getKills());
        }

        if (sorted.isEmpty()) sender.sendMessage("§7  No hay datos todavía.");
        sender.sendMessage("§8§m                                        ");
    }

    // -----------------------------------------------------------------------
    // Events
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKothCapped(KothCappedEvent event) {
        Player capper = event.getCapper();
        if (capper == null || !capper.isOnline()) return;
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(capper);
        if (ud == null) return;
        ud.addElo(PlayerRank.ELO_PER_KOTH);
        capper.sendMessage("§a+§e" + PlayerRank.ELO_PER_KOTH + " ELO §7(KOTH cap) §8| §7ELO: §e" + ud.getElo());
        this.checkRankUp(capper, ud);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUserdataKill(UserdataValueChangeEvent event) {
        if (event.getType() != UserdataValueType.KILLS) return;
        Userdata userdata = event.getUserdata();
        Player player = userdata.getPlayer();
        if (player == null || !player.isOnline()) return;

        me.vcb.astrocore.events.bloodmoon.BloodMoonManager bm =
            me.vcb.astrocore.events.bloodmoon.BloodMoonManager.getInstance();
        me.vcb.astrocore.events.eloboost.EloBoostManager eb =
            me.vcb.astrocore.events.eloboost.EloBoostManager.getInstance();

        boolean bloodMoon = bm != null && bm.isActive();
        float boostMult = (eb != null && eb.isActive()) ? eb.getMultiplier() : 1.0f;

        float baseMult = bloodMoon ? 2.0f : 1.0f;
        float finalMult = Math.max(baseMult, boostMult); // use whichever is higher
        int gain = Math.round(PlayerRank.ELO_PER_KILL * finalMult);

        userdata.addElo(gain);
        String extra = bloodMoon ? " §4☾ §cx2"
            : (eb != null && eb.isActive()) ? " §e⚡ §e×" + eb.getMultiplier() : "";
        player.sendMessage("§a+§e" + gain + " ELO §7(kill)" + extra + " §8| §7ELO: §e" + userdata.getElo());
        this.checkRankUp(player, userdata);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUserdataDeath(UserdataValueChangeEvent event) {
        if (event.getType() != UserdataValueType.DEATHS) return;
        Userdata userdata = event.getUserdata();
        Player player = userdata.getPlayer();
        if (player == null || !player.isOnline()) return;
        userdata.removeElo(PlayerRank.ELO_PER_DEATH);
        player.sendMessage("§c-§e" + PlayerRank.ELO_PER_DEATH + " ELO §7(muerte) §8| §7ELO: §e" + userdata.getElo());
        this.checkRankUp(player, userdata);
        userdata.incrementDeathStreak();
    }

    // -----------------------------------------------------------------------
    // Apply on join
    // -----------------------------------------------------------------------

    public void applyOnJoin(Player player) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;
        if (ud.getElo() <= 0) ud.addElo(PlayerRank.BASE_ELO);
        PlayerRank rank = this.getRank(ud);
        this.applyTeamPrefix(player, rank);
    }
}
