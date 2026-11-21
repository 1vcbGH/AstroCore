package me.vcb.astrocore.handlers.logger.nms;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.games.conquest.ConquestManager;
import me.vcb.astrocore.handlers.death.DeathMessageHandler;
import me.vcb.astrocore.handlers.logger.CombatLogger;
import me.vcb.astrocore.handlers.logger.CombatLoggerType;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Tasks;
import me.vcb.astrocore.utils.nms.NmsUtils;
import me.vcb.astrocore.utils.nms.NmsUtils_1_21;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * Combat logger using entity composition pattern for Paper 1.21.
 * Spawns a real Skeleton or Villager entity and drives its behavior via event listeners.
 */
public class CombatLogger_1_21 implements CombatLogger, Listener {

    @Getter private Player player;
    private final LivingEntity entity;
    private final int deathban;
    private final ItemStack[] contents;
    private final ItemStack[] armor;
    private final ItemStack offHand;
    private final BukkitTask removeTask;
    private boolean wasKilled = false;
    private Location killLocation = null;

    public CombatLogger_1_21(World world, Player player, CombatLoggerType loggerType) {
        this.player = player;
        this.deathban = AstroCore.getInstance().getDeathbanManager().getBanTime(player);
        this.contents = player.getInventory().getContents();
        this.armor = player.getInventory().getArmorContents();
        this.offHand = player.getInventory().getItemInOffHand();

        Location loc = player.getLocation();
        double spawnY = loc.getBlock().getType() == Material.AIR ? loc.getY() : Math.floor(loc.getY()) + 1;
        Location spawnLoc = new Location(world, loc.getX(), spawnY, loc.getZ(), loc.getYaw(), loc.getPitch());

        // Spawn the entity
        LivingEntity spawned;
        if (loggerType == CombatLoggerType.SKELETON) {
            spawned = (Skeleton) world.spawnEntity(spawnLoc, EntityType.SKELETON);
        } else {
            Villager villager = (Villager) world.spawnEntity(spawnLoc, EntityType.VILLAGER);
            villager.setProfession(Villager.Profession.NONE);
            spawned = villager;
        }

        this.entity = spawned;

        // Tag as combat logger in PersistentDataContainer
        this.entity.getPersistentDataContainer().set(
            NmsUtils_1_21.COMBAT_LOGGER_KEY,
            PersistentDataType.STRING,
            player.getUniqueId().toString()
        );

        // Configure entity
        this.entity.setHealth(player.getHealth());
        this.entity.setFireTicks(0);
        this.entity.setRemoveWhenFarAway(false);
        this.entity.setCustomName(Config.COMBAT_LOGGER_NAME_FORMAT.replace("<player>", player.getName()));
        this.entity.setCustomNameVisible(true);

        // Set max health attribute
        if (this.entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            this.entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        }

        // Disable AI
        if (this.entity instanceof Mob mob) {
            mob.setAI(false);
        }

        // Equipment
        EntityEquipment equipment = this.entity.getEquipment();
        if (equipment != null) {
            equipment.setItemInHand(player.getItemInHand());
            equipment.setItemInOffHand(this.offHand);
            equipment.setItemInOffHandDropChance(0);
            equipment.setHelmet(this.armor[3]);
            equipment.setChestplate(this.armor[2]);
            equipment.setLeggings(this.armor[1]);
            equipment.setBoots(this.armor[0]);
            equipment.setHelmetDropChance(0);
            equipment.setChestplateDropChance(0);
            equipment.setLeggingsDropChance(0);
            equipment.setBootsDropChance(0);
            equipment.setItemInHandDropChance(0);
        }

        // Apply active potion effects
        player.getActivePotionEffects().forEach(effect -> {
            if (effect.getDuration() > 12000) return;
            this.entity.addPotionEffect(new PotionEffect(
                effect.getType(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient()));
        });

        // Register event listener
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());

        // Auto-remove task
        this.removeTask = Tasks.syncLater(() -> {
            AstroCore.getInstance().getCombatLoggerHandler().removeCombatLogger(this.player.getUniqueId());
            this.entity.remove();
            this.player = null;
            HandlerList.unregisterAll(this);
        }, Config.COMBAT_LOGGER_TIME * 20L);
    }

    @Override
    public boolean wasKilled() { return wasKilled; }

    @Override
    public Location getKillLocation() { return killLocation; }

    @Override
    public void removeCombatLogger() {
        this.removeTask.cancel();
        if (!this.entity.isDead()) this.entity.remove();
        this.player = null;
        HandlerList.unregisterAll(this);
    }

    @Override
    public float getCombatLoggerHealth() {
        return (float) this.entity.getHealth();
    }

    @Override
    public Location getCombatLoggerLocation() {
        return this.entity.getLocation();
    }

    @Override
    public void handleEffectChanges(Player player) {
        player.getActivePotionEffects().forEach(effect -> {
            PotionEffect loggerEffect = this.entity.getPotionEffect(effect.getType());

            if (loggerEffect == null) {
                AstroCore.getInstance().getPvpClassManager().getPotionEffectRestorer()
                    .removePlayerEffect(player, effect.getType());
                return;
            }

            NmsUtils.getInstance().addPotionEffect(player, new PotionEffect(
                loggerEffect.getType(), loggerEffect.getDuration(),
                loggerEffect.getAmplifier(), loggerEffect.isAmbient()));
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!event.getEntity().equals(this.entity)) return;

        // Block indirect magic damage
        if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.MAGIC) {
            event.setCancelled(true);
            return;
        }

        // Block self-damage
        if (event.getDamager().equals(this.player)) {
            event.setCancelled(true);
            return;
        }

        if (!(event.getDamager() instanceof Player damager)) return;

        if (AstroCore.getInstance().getSotwHandler().isUnderSotwProtection(damager)) {
            event.setCancelled(true);
            return;
        }

        if (TimerManager.getInstance().getPvpProtTimer().isActive(damager)) {
            damager.sendMessage(Language.PREFIX + Language.PVP_PROT_PVP_DENY_VICTIM);
            event.setCancelled(true);
            return;
        }

        if (AstroCore.getInstance().getStaffModeManager().isInStaffMode(damager)) {
            damager.sendMessage(Language.PREFIX + Language.STAFF_MODE_DAMAGE_DENY);
            event.setCancelled(true);
            return;
        }

        if (AstroCore.getInstance().getVanishManager().isVanished(damager)) {
            damager.sendMessage(Language.PREFIX + Language.VANISH_DAMAGE_DENY);
            event.setCancelled(true);
            return;
        }

        PlayerFaction playerFaction = FactionsManager.getInstance().getPlayerFaction(this.player);
        if (playerFaction != null) {
            PlayerFaction damagerFaction = FactionsManager.getInstance().getPlayerFaction(damager);

            if (playerFaction == damagerFaction) {
                event.setCancelled(true);
                return;
            }

            if (!Config.FACTION_ALLY_FRIENDLY_FIRE && playerFaction.isAlly(damagerFaction)) {
                damager.sendMessage(Language.FACTIONS_DENY_DAMAGE_ALLIES
                    .replace("<player>", Config.ALLY_COLOR + this.player.getName()));
                event.setCancelled(true);
                return;
            }
        }

        TimerManager.getInstance().getCombatTagTimer().activate(damager.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!event.getEntity().equals(this.entity)) return;

        event.getDrops().clear();
        event.setDroppedExp(0);

        this.dropPlayerItems(this.contents, this.armor, this.entity.getLocation());
        if (this.offHand != null && this.offHand.getType() != Material.AIR) {
            this.entity.getWorld().dropItemNaturally(this.entity.getLocation(), this.offHand);
        }

        Player killer = this.entity.getKiller();
        String reason;

        if (killer != null) {
            AstroCore.getInstance().getKillstreakHandler().checkKillerKillstreak(killer);

            PlayerFaction killerFaction = FactionsManager.getInstance().getPlayerFaction(killer);
            if (killerFaction != null) {
                killerFaction.incrementPoints(Config.FACTION_TOP_KILL);
            }

            DeathMessageHandler deathMessageHandler = AstroCore.getInstance().getDeathMessageHandler();

            reason = Language.DEATHMESSAGE_REASON_COMBATLOGGER_KILLER
                .replace("<player>", deathMessageHandler.getPlayerName(this.player))
                .replace("<killer>", deathMessageHandler.getKillerName(killer));

            AstroCore.getInstance().getUserdataManager().getUserdata(killer).updateKillStats(reason);
        } else {
            reason = Language.DEATHMESSAGE_REASON_COMBATLOGGER
                .replace("<player>", AstroCore.getInstance().getDeathMessageHandler().getPlayerName(this.player));
        }

        final String finalReason = reason;
        final Player finalKiller = killer;

        Bukkit.getOnlinePlayers().forEach(online -> {
            if (finalKiller != null && !finalKiller.equals(online)) {
                Userdata onlineData = AstroCore.getInstance().getUserdataManager().getUserdata(online);
                if (onlineData == null || !onlineData.getSettings().isDeathMessages()) return;
            }
            online.sendMessage(finalReason);
        });

        UUID playerUUID = this.player.getUniqueId();

        Tasks.async(() -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
            Userdata userdata = AstroCore.getInstance().getUserdataManager().getUserdata(offlinePlayer);
            if (userdata == null) return;
            userdata.updateDeathStats(finalReason);
            userdata.resetKillstreak();
        });

        AstroCore.getInstance().getDeathbanManager().deathbanPlayer(
            this.player, this.entity.getLocation(), this.deathban, reason);

        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(this.player);
        if (faction != null) {
            faction.onDeath(this.player);

            ConquestManager conquestManager = AstroCore.getInstance().getConquestManager();
            if (conquestManager.isActive()) {
                conquestManager.onDeath(this.player, faction);
            }
        }

        // Mark as killed — onPlayerJoin will handle killing the player when they reconnect
        this.wasKilled = true;
        this.killLocation = this.entity.getLocation();

        this.removeTask.cancel();
        // Do NOT remove from map here — CombatLoggerHandler.onPlayerJoin needs to see wasKilled=true
        HandlerList.unregisterAll(this);
    }
}
