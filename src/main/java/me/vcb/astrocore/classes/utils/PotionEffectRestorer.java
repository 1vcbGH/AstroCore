package me.vcb.astrocore.classes.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.classes.manager.PvpClass;
import me.vcb.astrocore.classes.manager.PvpClassManager;
import me.vcb.astrocore.utils.ServerUtils;
import me.vcb.astrocore.utils.Tasks;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class PotionEffectRestorer implements Listener {

    private static final long INFINITE_DURATION = 12_000L;
    private final PvpClassManager pvpClassManager;

    private final Table<UUID, PotionEffectType, EffectRestore> restorers;
    private final Map<UUID, EffectCache[]> playerEffectCache;

    public PotionEffectRestorer(PvpClassManager pvpClassManager) {
        this.pvpClassManager = pvpClassManager;

        this.restorers = HashBasedTable.create();
        this.playerEffectCache = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
        Bukkit.getOnlinePlayers().forEach(this::cachePlayerEffects);
    }

    public void disable() {
        this.restorers.clear();
        this.playerEffectCache.clear();
    }

    public EffectCache[] getCachedEffects(Player player) {
        return this.playerEffectCache.get(player.getUniqueId());
    }

    public void removeEffectCache(Player player) {
        this.playerEffectCache.remove(player.getUniqueId());
    }

    public PotionEffect getPotionEffectToRestore(Player player, PotionEffectType effectType) {
        EffectRestore effectRestore = this.restorers.remove(player.getUniqueId(), effectType);
        return effectRestore != null && effectRestore.getCondition().test(player) ? effectRestore.getEffect() : null;
    }

    private void queueEffectRestore(Player player, PotionEffect toAdd, PotionEffect current) {
        int durationDiff = toAdd.getDuration() - current.getDuration();

        if(toAdd.getAmplifier() == current.getAmplifier() && durationDiff < 5) return;
        if(current.getDuration() < INFINITE_DURATION && toAdd.getAmplifier() <= current.getAmplifier()) return;

        EffectRestore effectRestore = this.createEffectRestore(player, current);
        this.restorers.put(player.getUniqueId(), current.getType(), effectRestore);
    }

    private EffectRestore createEffectRestore(Player player, PotionEffect current) {
        PvpClass pvpClass = this.pvpClassManager.getActivePvpClass(player);
        Predicate<Player> condition = futurePlayer -> true;

        if(current.getDuration() > INFINITE_DURATION && pvpClass != null) {
            condition = futurePlayer -> pvpClass.isActive(futurePlayer);
        }

        return new EffectRestore(current, condition);
    }

    private PotionEffect correctEffectDuration(EffectCache effectCache) {
        PotionEffect current = effectCache.getEffect();
        long cacheTimestamp = effectCache.getCacheTimestamp();

        int tickDifference = (int) (Math.abs((System.currentTimeMillis() - cacheTimestamp)) / 50);
        return new PotionEffect(current.getType(), current.getDuration() - tickDifference, current.getAmplifier());
    }

    private void handleEffectRestore(Player player, PotionEffectType effectType) {
        PotionEffect effect = this.getPotionEffectToRestore(player, effectType);
        if(effect == null) return;

        Tasks.sync(() -> NmsUtils.getInstance().addPotionEffect(player, effect));
    }

    private EffectCache getPlayerCachedEffect(Player player, PotionEffectType effectType) {
        EffectCache[] effectCache = this.getCachedEffects(player);
        return effectCache != null ? effectCache[effectType.getId() - 1] : null;
    }

    public void cachePlayerEffects(Player player) {
        Collection<PotionEffect> currentEffects = player.getActivePotionEffects();
        EffectCache[] effectCache = new EffectCache[23];

        long currentTime = System.currentTimeMillis();

        for(PotionEffect potionEffect : currentEffects) {
            effectCache[potionEffect.getType().getId() - 1] = new EffectCache(potionEffect, currentTime);
        }

        this.playerEffectCache.put(player.getUniqueId(), effectCache);
    }

    private void addPotionEffectToCache(Player player, PotionEffect potionEffect) {
        if(potionEffect == null) return;

        EffectCache[] effectCache = this.getCachedEffects(player);

        if(effectCache != null) {
            EffectCache cached = effectCache[potionEffect.getType().getId() - 1];
            long currentTime = System.currentTimeMillis();

            if(cached != null) {
                cached.setEffect(potionEffect);
                cached.setCacheTimestamp(currentTime);
            } else {
                effectCache[potionEffect.getType().getId() - 1] = new EffectCache(potionEffect, currentTime);
            }
        }
    }

    private void removePotionEffectFromCache(Player player, PotionEffectType effectType) {
        EffectCache[] effectCache = this.getCachedEffects(player);
        if(effectCache == null) return;

        EffectCache cached = effectCache[effectType.getId() - 1];

        if(cached != null) {
            cached.setEffect(null);
        }
    }

    public void removePlayerEffect(Player player, PotionEffectType effectType) {
        PotionEffect effectToRestore = this.getPotionEffectToRestore(player, effectType);

        if(effectToRestore == null) {
            player.removePotionEffect(effectType);
        } else {
            this.removePotionEffectFromCache(player, effectType);
            NmsUtils.getInstance().addPotionEffect(player, effectToRestore);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        EntityPotionEffectEvent.Action action = event.getAction();

        if(action == EntityPotionEffectEvent.Action.ADDED || action == EntityPotionEffectEvent.Action.CHANGED) {
            PotionEffect toAdd = event.getNewEffect();
            if(toAdd == null) return;

            EffectCache effectCache = this.getPlayerCachedEffect(player, toAdd.getType());
            if(effectCache != null && effectCache.getEffect() != null) {
                PotionEffect newEffect = this.correctEffectDuration(effectCache);
                this.queueEffectRestore(player, toAdd, newEffect);
            }

            this.addPotionEffectToCache(player, toAdd);
        } else if(action == EntityPotionEffectEvent.Action.CLEARED) {
            PotionEffect old = event.getOldEffect();
            if(old == null) return;

            this.handleEffectRestore(player, old.getType());
            this.removePotionEffectFromCache(player, old.getType());
        } else if(action == EntityPotionEffectEvent.Action.REMOVED) {
            PotionEffect old = event.getOldEffect();
            if(old == null) return;

            UUID uuid = player.getUniqueId();
            PotionEffectType effectType = old.getType();

            Tasks.sync(() -> {
                Player p = Bukkit.getPlayer(uuid);
                if(p == null) return;

                if(!p.hasPotionEffect(effectType)) {
                    this.removePotionEffectFromCache(p, effectType);
                } else {
                    this.addPotionEffectToCache(p, NmsUtils.getInstance().getPotionEffect(p, effectType));
                }
            });
        }
    }

    @Getter
    @AllArgsConstructor
    static class EffectRestore {

        private final PotionEffect effect;
        private final Predicate<Player> condition;
    }

    @ToString
    @Getter @Setter
    @AllArgsConstructor
    static class EffectCache {

        private PotionEffect effect;
        private long cacheTimestamp;
    }
}
