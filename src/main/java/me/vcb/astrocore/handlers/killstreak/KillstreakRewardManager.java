package me.vcb.astrocore.handlers.killstreak;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class KillstreakRewardManager implements Listener {

    // milestone → coins reward
    // Balanced: rewards increase but stay reasonable
    private static final Map<Integer, Integer> MILESTONES = new LinkedHashMap<>();

    static {
        MILESTONES.put(5,   150);
        MILESTONES.put(10,  300);
        MILESTONES.put(15,  600);
        MILESTONES.put(20,  1000);
        MILESTONES.put(25,  1500);
        MILESTONES.put(30,  2500);
        MILESTONES.put(35,  3500);
        MILESTONES.put(40,  5000);
        MILESTONES.put(50,  8000);
        MILESTONES.put(75,  15000);
        MILESTONES.put(100, 25000);
    }

    // Milestones that get a server-wide broadcast (10+)
    private static final int BROADCAST_THRESHOLD = 10;

    public KillstreakRewardManager() {
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (me.vcb.astrocore.handlers.logger.CombatLoggerHandler.isCombatLogDeath(victim)) return;
        Player killer = victim.getKiller();

        // Reset victim killstreak
        Userdata victimData = AstroCore.getInstance().getUserdataManager().getUserdata(victim);
        if(victimData != null && victimData.getKillstreak() > 0) {
            int lost = victimData.getKillstreak();
            victimData.resetKillstreak();
            victim.sendMessage("§c§lRACHA §r§7Tu racha de §e" + lost + " kills §7se perdió.");
        }

        // Reward killer
        if(killer == null || killer == victim) return;

        Userdata killerData = AstroCore.getInstance().getUserdataManager().getUserdata(killer);
        if(killerData == null) return;

        killerData.addKillstreak();
        int streak = killerData.getKillstreak();

        Integer reward = MILESTONES.get(streak);
        if(reward == null) return;

        this.giveReward(killer, killerData, streak, reward);
    }

    public void giveReward(Player player, Userdata userdata, int streak, int coins) {
        AstroCore.getInstance().getEconomyManager().addBalance(player, coins);

        // Personal message
        player.sendMessage("");
        player.sendMessage("§6§l★ RECOMPENSA DE RACHA §6§l★");
        player.sendMessage("  §7Racha: §e§l" + streak + " kills");
        player.sendMessage("  §7Coins ganados: §e+" + coins);
        player.sendMessage("  §7Balance: §e" + (userdata.getBalance()) + " coins");
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);

        // Coin rain animation
        this.spawnCoinRain(player);

        // Broadcast for big streaks
        if(streak >= BROADCAST_THRESHOLD) {
            me.vcb.astrocore.userdata.Userdata kd = AstroCore.getInstance().getUserdataManager().getUserdata(player);
            me.vcb.astrocore.coinshop.KillStreakMessageType msgType = null;
            if (kd != null && kd.getSelectedKillstreakMessage() != null) {
                msgType = me.vcb.astrocore.coinshop.KillStreakMessageType.fromId(kd.getSelectedKillstreakMessage());
            }
            if (msgType == null) msgType = me.vcb.astrocore.coinshop.KillStreakMessageType.DEFAULT;
            Bukkit.broadcastMessage(msgType.build(player.getName(), streak, coins));
        }
    }

    private String getMedal(int streak) {
        if(streak >= 30) return "★★★";
        if(streak >= 20) return "★★";
        if(streak >= 10) return "★";
        return "•";
    }

    private void spawnCoinRain(Player player) {
        Random rng = new Random();
        ItemStack coin = new ItemStack(Material.GOLD_NUGGET);
        Location base = player.getLocation().add(0, 3.5, 0);
        List<Item> spawned = new ArrayList<>();

        // Spawn 20 nuggets in a fountain pattern above the player
        for(int i = 0; i < 20; i++) {
            double ox = (rng.nextDouble() - 0.5) * 2.5;
            double oy = rng.nextDouble() * 1.5;
            double oz = (rng.nextDouble() - 0.5) * 2.5;

            Item item = player.getWorld().dropItem(base.clone().add(ox, oy, oz), coin);
            item.setPickupDelay(Integer.MAX_VALUE); // can't be picked up
            item.setGlowing(true);

            // Slight upward burst then gravity pulls them down
            item.setVelocity(new Vector(
                (rng.nextDouble() - 0.5) * 0.25,
                rng.nextDouble() * 0.2,
                (rng.nextDouble() - 0.5) * 0.25
            ));

            spawned.add(item);
        }

        // Sound: coin jingle
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 0.8f);
        Tasks.syncLater(() ->
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.1f),
        4L);

        // Remove all coins after 2.5 seconds (50 ticks)
        Tasks.syncLater(() -> spawned.forEach(Entity::remove), 50L);
    }

    public Map<Integer, Integer> getMilestones() {
        return MILESTONES;
    }
}
