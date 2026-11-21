package me.vcb.astrocore.abilities.type;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.abilities.AbilityItem;
import me.vcb.astrocore.abilities.AbilityType;
import me.vcb.astrocore.config.ConfigFile;
import me.vcb.astrocore.config.Language;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AntiMazo — protects from mace (mazo) hits for a configurable duration.
 * Right-click to activate; any hit dealt by a player holding a mace is cancelled while active.
 */
public class AntiMazoAbility extends AbilityItem implements Listener {

    private int shieldDuration; // seconds

    /** players who are currently shielded — maps UUID to expiry time (ms) */
    private final Map<UUID, Long> shielded = new HashMap<>();

    public AntiMazoAbility(ConfigFile config) {
        super(AbilityType.ANTI_MAZO, "ANTI_MAZO", config);
    }

    @Override
    protected void loadAdditionalData(ConfigurationSection abilitySection) {
        this.shieldDuration = abilitySection.getInt("SHIELD_DURATION", 5);
    }

    @Override
    protected void disable() {
        this.shielded.clear();
    }

    @Override
    protected boolean onItemClick(Player player, PlayerInteractEvent event) {
        long expiry = System.currentTimeMillis() + (this.shieldDuration * 1000L);
        this.shielded.put(player.getUniqueId(), expiry);

        // Auto-remove shield after shieldDuration seconds
        UUID uuid = player.getUniqueId();
        AstroCore.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(
            AstroCore.getInstance(), () -> this.shielded.remove(uuid), this.shieldDuration * 20L);

        event.setCancelled(true);
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        ItemStack weapon = damager.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType() != Material.MACE) return;

        Player victim = (Player) event.getEntity();
        Long expiry = this.shielded.get(victim.getUniqueId());
        if (expiry == null) return;

        if (System.currentTimeMillis() < expiry) {
            event.setCancelled(true);
            victim.sendMessage(Language.ABILITIES_PREFIX + Language.ABILITIES_ANTI_MAZO_BLOCKED);
        } else {
            this.shielded.remove(victim.getUniqueId());
        }
    }
}
