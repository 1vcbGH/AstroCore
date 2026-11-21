package me.vcb.astrocore.abilities.type;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.abilities.AbilityItem;
import me.vcb.astrocore.abilities.AbilityType;
import me.vcb.astrocore.abilities.event.ProjectileAbilityActivatedEvent;
import me.vcb.astrocore.abilities.utils.AbilityUtils;
import me.vcb.astrocore.config.ConfigFile;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.PlayerUtils;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public class AggressivePearlAbility extends AbilityItem implements Listener {

    private final String metadataName;
    private List<PotionEffect> effects;

    public AggressivePearlAbility(ConfigFile config) {
        super(AbilityType.AGGRESSIVE_PEARL, "AGGRESSIVE_PEARL", config);

        this.metadataName = "aggressivePearl";
        this.removeOneItem = false;
        this.projectileAbility = true;

        this.overrideActivationMessage();
    }

    @Override
    protected void disable() {
        this.effects.clear();
    }

    @Override
    protected void loadAdditionalData(ConfigurationSection abilitySection) {
        this.effects = AbilityUtils.loadEffects(abilitySection);
    }

    public void sendActivationMessage(Player player, List<PotionEffect> effects) {
        this.activationMessage.forEach(line -> player.sendMessage(line
            .replace("<abilityName>", this.displayName)
            .replace("<effects>", AbilityUtils.getEffectList(effects, Language.ABILITIES_AGGRESSIVE_PEARL_EFFECT_FORMAT))
            .replace("<cooldown>", StringUtils.formatDurationWords(this.cooldown * 1000L))));
    }

    @Override
    protected boolean onItemClick(Player player, PlayerInteractEvent event) {
        if(player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }

        player.setMetadata(this.metadataName, PlayerUtils.TRUE_METADATA_VALUE);
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if(!(event.getEntity().getShooter() instanceof Player)) return;

        Projectile projectile = event.getEntity();

        Player player = (Player) projectile.getShooter();
        if(!player.hasMetadata(this.metadataName)) return;

        projectile.setMetadata(this.metadataName, PlayerUtils.TRUE_METADATA_VALUE);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if(player.hasMetadata(this.metadataName)) {
            player.removeMetadata(this.metadataName, AstroCore.getInstance());

            ProjectileAbilityActivatedEvent abilityEvent = new ProjectileAbilityActivatedEvent(player, event.getTo(), this);

            if(abilityEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            this.addEffects(player, this.effects);
            this.sendActivationMessage(player, this.effects);
        }
    }
}
