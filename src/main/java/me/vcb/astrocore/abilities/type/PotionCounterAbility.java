package me.vcb.astrocore.abilities.type;

import me.vcb.astrocore.abilities.AbilityItem;
import me.vcb.astrocore.abilities.AbilityType;
import me.vcb.astrocore.config.ConfigFile;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class PotionCounterAbility extends AbilityItem {

    public PotionCounterAbility(ConfigFile config) {
        super(AbilityType.POTION_COUNTER, "POTION_COUNTER", config);

        this.overrideActivationMessage();
    }

    // Count splash potions of Healing (Instant Health I/II) in target inventory.
    private int countSplashHealing(Player target) {
        int amount = 0;
        for(ItemStack item : target.getInventory().getContents()) {
            if(item == null || item.getType() != Material.SPLASH_POTION) continue;
            if(!(item.getItemMeta() instanceof PotionMeta meta)) continue;

            PotionType base = meta.getBasePotionType();
            if(base == PotionType.HEALING || base == PotionType.STRONG_HEALING) {
                amount += item.getAmount();
            }
        }
        return amount;
    }

    public void sendActivationMessage(Player player, Player target, int potionAmount) {
        this.activationMessage.forEach(line -> player.sendMessage(line
            .replace("<abilityName>", this.displayName)
            .replace("<target>", target.getName())
            .replace("<amount>", String.valueOf(potionAmount))
            .replace("<cooldown>", StringUtils.formatDurationWords(this.cooldown * 1000L))));
    }

    @Override
    protected boolean onPlayerItemHit(Player damager, Player target, EntityDamageByEntityEvent event) {
        int potionAmount = countSplashHealing(target);
        this.sendActivationMessage(damager, target, potionAmount);
        return true;
    }
}
