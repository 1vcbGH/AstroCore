package me.vcb.astrocore.handlers.block;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class CobwebRemoveHandler extends Handler implements Listener {

    private static final int REMOVAL_LIMIT = 50;

    private final Map<Location, Long> cobwebs;
    private final CobwebRemovalTask removalTask;

    public CobwebRemoveHandler() {
        this.cobwebs = new ConcurrentHashMap<>();
        this.removalTask = new CobwebRemovalTask();
    }

    @Override
    public void disable() {
        this.cobwebs.keySet().forEach(location -> location.getBlock().setType(Material.AIR));
        this.cobwebs.clear();

        if(this.removalTask != null) {
            this.removalTask.cancel();
        }
    }

    private void scheduleForRemoval(Location blockLocation) {
        this.cobwebs.put(blockLocation, System.currentTimeMillis() + (Config.COBWEB_REMOVER_REMOVE_AFTER * 1000L));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        if(Config.COBWEB_REMOVER_ENABLED && block.getType() == Material.COBWEB) {
            this.scheduleForRemoval(event.getBlock().getLocation());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if(Config.COBWEB_REMOVER_ENABLED && block.getType() == Material.COBWEB) {
            this.cobwebs.remove(block.getLocation());
        }
    }

    private class CobwebRemovalTask extends BukkitRunnable {

        public CobwebRemovalTask() {
            this.runTaskTimerAsynchronously(AstroCore.getInstance(), 0L, 20L);
        }

        @Override
        public void run() {
            List<Location> toRemove = new ArrayList<>();
            Iterator<Entry<Location, Long>> iterator = cobwebs.entrySet().iterator();

            while(iterator.hasNext() && toRemove.size() < REMOVAL_LIMIT) {
                Entry<Location, Long> entry = iterator.next();
                if(entry.getValue() > System.currentTimeMillis()) continue;

                toRemove.add(entry.getKey());
                iterator.remove();
            }

            Tasks.sync(() -> toRemove.forEach(location -> location.getBlock().setType(Material.AIR)));
        }
    }
}
