package me.vcb.astrocore.abilities.type;

import me.vcb.astrocore.abilities.AbilityItem;
import me.vcb.astrocore.abilities.AbilityType;
import me.vcb.astrocore.config.ConfigFile;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Language;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiElytraAbility extends AbilityItem implements Listener {

    private int blockDuration;

    private final Map<UUID, UUID> lastAttacker = new HashMap<>();
    private final Map<UUID, Long> elytraBlocked = new HashMap<>();

    public AntiElytraAbility(ConfigFile config) {
        super(AbilityType.ANTI_ELYTRAS, "ANTI_ELYTRAS", config);
        this.overrideActivationMessage();
    }

    @Override
    protected void loadAdditionalData(ConfigurationSection abilitySection) {
        this.blockDuration = abilitySection.getInt("BLOCK_DURATION", 10);
    }

    @Override
    protected void disable() {
        this.lastAttacker.clear();
        this.elytraBlocked.clear();
    }

    @Override
    protected boolean onItemClick(Player player, PlayerInteractEvent event) {
        UUID attackerUUID = this.lastAttacker.get(player.getUniqueId());
        if(attackerUUID == null) {
            player.sendMessage(Language.ABILITIES_PREFIX + Language.ABILITIES_ANTI_ELYTRAS_NO_ATTACKER);
            return false;
        }

        Player attacker = Bukkit.getPlayer(attackerUUID);
        if(attacker == null || !attacker.isOnline()) {
            player.sendMessage(Language.ABILITIES_PREFIX + Language.ABILITIES_ANTI_ELYTRAS_NOT_ONLINE);
            return false;
        }

        long expiry = System.currentTimeMillis() + (this.blockDuration * 1000L);
        this.elytraBlocked.put(attackerUUID, expiry);

        if(attacker.isGliding()) {
            attacker.setGliding(false);
        }

        attacker.sendMessage(Language.ABILITIES_PREFIX + Language.ABILITIES_ANTI_ELYTRAS_BLOCKED
            .replace("<duration>", String.valueOf(this.blockDuration)));

        // Schedule expiry message to attacker
        AstroCore.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(
            AstroCore.getInstance(), () -> {
                this.elytraBlocked.remove(attackerUUID);
                Player target = Bukkit.getPlayer(attackerUUID);
                if(target != null && target.isOnline()) {
                    target.sendMessage(Language.ABILITIES_PREFIX + Language.ABILITIES_ANTI_ELYTRAS_EXPIRED);
                }
            }, this.blockDuration * 20L);

        // Send activation message with correct block duration
        String durationStr = String.valueOf(this.blockDuration);
        this.activationMessage.forEach(line -> player.sendMessage(line
            .replace("<abilityName>", this.displayName)
            .replace("<duration>", durationStr)));

        event.setCancelled(true);
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
        this.lastAttacker.put(event.getEntity().getUniqueId(), event.getDamager().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        if(!event.isGliding() || !(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Long expiry = this.elytraBlocked.get(player.getUniqueId());
        if(expiry == null) return;

        if(System.currentTimeMillis() < expiry) {
            event.setCancelled(true);
            player.sendMessage(Language.ABILITIES_PREFIX + Language.ABILITIES_ANTI_ELYTRAS_CANNOT_GLIDE);
        } else {
            this.elytraBlocked.remove(player.getUniqueId());
        }
    }
}
