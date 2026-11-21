package me.vcb.astrocore.handlers.kitmap;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class KitmapHandler extends Handler implements Listener {

    private ItemClearTask itemClearTask;

    public KitmapHandler() {
        if(Config.KITMAP_CLEAR_ITEMS_ENABLED) {
            this.itemClearTask = new ItemClearTask();
        }

        TimerManager.getInstance().getPvpProtTimer().clearPvpProtections();
    }

    @Override
    public void disable() {
        if(this.itemClearTask != null) this.itemClearTask.cancel();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if(!Config.KITMAP_DISABLE_ITEM_DROP_IN_SAFEZONE) return;

        if(ClaimManager.getInstance().getFactionAt(event.getPlayer()).isSafezone()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Language.FACTION_PREFIX + Language.KITMAP_DENY_ITEM_DROP);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if(killer == null || !Config.KITMAP_KILL_REWARD_ENABLED) return;

        Config.KITMAP_KILL_REWARD.forEach(command -> Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(), command.replace("<player>", killer.getName())));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(player.hasPlayedBefore()) return;

        Location spawn = Config.WORLD_SPAWNS.get(Environment.NORMAL);
        player.teleport(spawn == null ? player.getWorld().getSpawnLocation() : spawn);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TimerManager.getInstance().getTeleportTimer().cancel(event.getPlayer());
    }

    private static class ItemClearTask extends BukkitRunnable {

        ItemClearTask() {
            this.runTaskTimer(AstroCore.getInstance(), 0L, Config.KITMAP_CLEAR_ITEMS_INTERVAL * 20L);
        }

        @Override
        public void run() {
            for(World world : Bukkit.getWorlds()) {
                world.getEntities().forEach(entity -> {
                    if(entity instanceof Item) {
                        entity.remove();
                    }
                });
            }
        }
    }
}
