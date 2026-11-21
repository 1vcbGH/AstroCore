package me.vcb.astrocore.games.dragon.nms;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.dragon.EnderDragon;
import me.vcb.astrocore.games.loot.LootData;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * EnderDragon implementation for Paper 1.21 using event composition.
 * Spawns a real EnderDragon and tracks damage/death via event listeners.
 */
public class EnderDragon_1_21 implements EnderDragon, Listener {

    private final org.bukkit.entity.EnderDragon dragon;
    private final LootData loot;
    private final Map<String, Float> damagers;

    public EnderDragon_1_21(Location loc, LootData loot) {
        this.loot = loot;
        this.damagers = new HashMap<>();

        // Spawn the dragon
        org.bukkit.entity.EnderDragon spawned = (org.bukkit.entity.EnderDragon)
            loc.getWorld().spawnEntity(loc, EntityType.ENDER_DRAGON);

        this.dragon = spawned;

        // Configure dragon
        if (this.dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            this.dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Config.ENDER_DRAGON_HEALTH);
        }
        this.dragon.setHealth(Config.ENDER_DRAGON_HEALTH);
        this.dragon.setCustomName(Config.ENDER_DRAGON_NAME);
        this.dragon.setCustomNameVisible(true);
        this.dragon.setRemoveWhenFarAway(false);

        // Register events
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    @Override
    public void disable() {
        this.damagers.clear();
        HandlerList.unregisterAll(this);
    }

    @Override
    public Entity getDragonBukkitEntity() {
        return this.dragon;
    }

    @Override
    public float getDragonHealth() {
        return (float) this.dragon.getHealth();
    }

    @Override
    public void setDragonHealth(float value) {
        this.dragon.setHealth(value);
    }

    @Override
    public float getDragonMaxHealth() {
        return (float) this.dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    }

    @Override
    public void setDragonMaxHealth(float value) {
        if (this.dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            this.dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(value);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonDamage(EntityDamageByEntityEvent event) {
        if (!event.getEntity().getUniqueId().equals(this.dragon.getUniqueId())) return;

        Entity damager = event.getDamager();
        if (!(damager instanceof Player player)) return;

        float amount = (float) event.getFinalDamage();
        this.damagers.merge(player.getName(), amount, Float::sum);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDragonDeath(EntityDeathEvent event) {
        if (!event.getEntity().getUniqueId().equals(this.dragon.getUniqueId())) return;

        event.getDrops().clear();
        event.setDroppedExp(0);

        AtomicReference<String> winner = new AtomicReference<>();
        Map<String, String> topDamagers = new HashMap<>();

        AtomicInteger counter = new AtomicInteger(1);
        Comparator<Map.Entry<String, Float>> comparator = Map.Entry.<String, Float>comparingByValue().reversed();

        this.damagers.entrySet().stream().sorted(comparator).limit(3).forEach(entry -> {
            if (winner.get() == null) {
                winner.set(entry.getKey());
            }

            String damagerFormat = Language.ENDER_DRAGON_DAMAGER_FORMAT
                .replace("<player>", entry.getKey())
                .replace("<damage>", String.valueOf(entry.getValue().intValue()));

            topDamagers.put("<damager" + counter.getAndIncrement() + ">", damagerFormat);
        });

        Language.ENDER_DRAGON_ENDED.forEach(line -> Messages.sendMessage(line
            .replace("<killer>", winner.get() != null ? winner.get() : "Nobody")
            .replace("<damager1>", topDamagers.getOrDefault("<damager1>", ""))
            .replace("<damager2>", topDamagers.getOrDefault("<damager2>", ""))
            .replace("<damager3>", topDamagers.getOrDefault("<damager3>", ""))));

        if (winner.get() != null) {
            this.loot.handleRewards(Bukkit.getPlayer(winner.get()));
        }

        AstroCore.getInstance().getEnderDragonManager().stopEnderDragon(false);
        HandlerList.unregisterAll(this);
    }
}
