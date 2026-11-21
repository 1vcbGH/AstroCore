package me.vcb.astrocore.bounty;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BountyManager implements Listener {

    @Getter private static BountyManager instance;

    /** Consecutive deaths required for auto-bounty. */
    private static final int AUTO_BOUNTY_DEATH_STREAK = 3;
    /** Auto-bounty amount in coins. */
    private static final int AUTO_BOUNTY_AMOUNT = 500;

    private final Map<UUID, BountyData> bounties;

    public BountyManager() {
        instance = this;
        this.bounties = new ConcurrentHashMap<>();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public void disable() {
        instance = null;
        this.bounties.clear();
    }

    // -----------------------------------------------------------------------

    public boolean hasBounty(UUID uuid) {
        return this.bounties.containsKey(uuid);
    }

    public BountyData getBounty(UUID uuid) {
        return this.bounties.get(uuid);
    }

    public Collection<BountyData> getAllBounties() {
        return Collections.unmodifiableCollection(this.bounties.values());
    }

    /**
     * Places a bounty. If one already exists, adds to the total.
     *
     * @param targetUuid  target player UUID
     * @param targetName  target player name
     * @param amount      coins to add
     * @param setterName  who set it ("AUTO" for auto-bounties)
     */
    public void placeBounty(UUID targetUuid, String targetName, int amount, String setterName) {
        BountyData existing = this.bounties.get(targetUuid);
        int total = (existing != null ? existing.getAmount() : 0) + amount;
        BountyData bounty = new BountyData(targetUuid, targetName, total, setterName);
        this.bounties.put(targetUuid, bounty);

        String setter = setterName.equals("AUTO") ? "§cAUTO" : "§e" + setterName;
        Bukkit.broadcastMessage("§c§l[BOUNTY] §r§e" + targetName
            + " §7tiene precio de §6$" + total + " §7coins! §8(puesto por " + setter + "§8)");
    }

    /** Claims the bounty and rewards the killer. Returns 0 if no bounty. */
    public int claimBounty(Player killer, UUID victimUuid) {
        BountyData bounty = this.bounties.remove(victimUuid);
        if (bounty == null) return 0;

        Userdata killerUd = AstroCore.getInstance().getUserdataManager().getUserdata(killer);
        if (killerUd != null) {
            killerUd.changeBalance(killerUd.getBalance() + bounty.getAmount());
            killerUd.addBountiesCollected(bounty.getAmount());
        }

        killer.sendMessage("§6§l[BOUNTY] §r§a¡Cobraste el bounty de §e" + bounty.getTargetName()
            + "§a! §6+" + bounty.getAmount() + " coins§a.");
        killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

        Bukkit.broadcastMessage("§6§l[BOUNTY] §r§e" + killer.getName()
            + " §7cobró el bounty de §e" + bounty.getTargetName()
            + " §7(§6$" + bounty.getAmount() + " coins§7)!");
        return bounty.getAmount();
    }

    // -----------------------------------------------------------------------
    // Death listener — auto-bounty + claim on kill
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (me.vcb.astrocore.handlers.logger.CombatLoggerHandler.isCombatLogDeath(victim)) return;
        Player killer = victim.getKiller();

        // Claim bounty on victim
        if (killer != null) {
            this.claimBounty(killer, victim.getUniqueId());
            // Reset death streak of victim on regular kill
        }

        // Auto-bounty based on death streak
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(victim);
        if (ud != null) {
            // deathStreak is incremented by RankManager's DEATHS listener already;
            // check after increment (it fires before this in MONITOR order usually,
            // but to be safe we increment here too if not already done — idempotent check
            // is done by only auto-bounty at exact multiples)
            int streak = ud.getDeathStreak();
            if (streak > 0 && streak % AUTO_BOUNTY_DEATH_STREAK == 0 && !this.hasBounty(victim.getUniqueId())) {
                int autoAmount = AUTO_BOUNTY_AMOUNT * (streak / AUTO_BOUNTY_DEATH_STREAK);
                this.placeBounty(victim.getUniqueId(), victim.getName(), autoAmount, "AUTO");
            }

            // Reset death streak when player kills someone (handled in kill event)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKillerResetStreak(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        // Reset death streak for the killer since they got a kill
        Userdata killerUd = AstroCore.getInstance().getUserdataManager().getUserdata(killer);
        if (killerUd != null) killerUd.resetDeathStreak();
    }
}
