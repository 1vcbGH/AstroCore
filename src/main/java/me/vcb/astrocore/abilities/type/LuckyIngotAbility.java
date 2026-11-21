package me.vcb.astrocore.abilities.type;

import me.vcb.astrocore.abilities.AbilityItem;
import me.vcb.astrocore.abilities.AbilityType;
import me.vcb.astrocore.abilities.utils.AbilityUtils;
import me.vcb.astrocore.config.ConfigFile;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LuckyIngotAbility extends AbilityItem {

    private int percentage;

    private List<PotionEffect> positiveEffects;
    private List<PotionEffect> negativeEffects;

    public LuckyIngotAbility(ConfigFile config) {
        super(AbilityType.LUCKY_INGOT, "LUCKY_INGOT", config);

        this.overrideActivationMessage();
    }

    @Override
    protected void disable() {
        this.positiveEffects.clear();
        this.negativeEffects.clear();
    }

    @Override
    protected void loadAdditionalData(ConfigurationSection abilitySection) {
        this.percentage = abilitySection.getInt("POSITIVE_EFFECT_PERCENTAGE");

        this.positiveEffects = AbilityUtils.loadEffects(abilitySection, "POSITIVE_EFFECTS");
        this.negativeEffects = AbilityUtils.loadEffects(abilitySection, "NEGATIVE_EFFECTS");
    }

    public void sendActivationMessage(Player player, List<PotionEffect> effects) {
        this.activationMessage.forEach(line -> player.sendMessage(line
            .replace("<abilityName>", this.displayName)
            .replace("<effects>", AbilityUtils.getEffectList(effects, Language.ABILITIES_LUCKY_INGOT_EFFECT_FORMAT))
            .replace("<cooldown>", StringUtils.formatDurationWords(this.cooldown * 1000L))));
    }

    @Override
    protected boolean onItemClick(Player player, PlayerInteractEvent event) {
        List<PotionEffect> effects = this.positiveEffects;

        if(ThreadLocalRandom.current().nextInt() > this.percentage) {
            effects = this.negativeEffects;
        }

        this.addEffects(player, effects);
        this.sendActivationMessage(player, effects);

        event.setCancelled(true);
        return true;
    }
}
