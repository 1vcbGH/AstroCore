package me.vcb.astrocore.handlers.limiter;

import lombok.Getter;
import lombok.Setter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class PotionLimiterHandler extends Handler implements Listener {

    private final List<PotionLimit> potionLimits;

    public PotionLimiterHandler() {
        this.potionLimits = new ArrayList<>();

        this.loadPotionLimits();
    }

    @Override
    public void disable() {
        this.potionLimits.clear();
    }

    private void loadPotionLimits() {
        this.potionLimits.clear();

        ConfigurationSection section = AstroCore.getInstance().getLimitersFile()
            .getSection("POTION_LIMITER");

        section.getKeys(false).forEach(type -> {
            if(section.getInt(type + ".LEVEL") == -1) return;

            PotionLimit potionLimit = new PotionLimit();
            potionLimit.setType(PotionEffectType.getByName(type));
            potionLimit.setLevel(section.getInt(type + ".LEVEL"));
            potionLimit.setExtended(section.getBoolean(type + ".EXTENDED"));

            this.potionLimits.add(potionLimit);
        });
    }

    private PotionLimit getPotionLimit(PotionEffectType type) {
        return this.potionLimits.stream().filter(limit -> limit.getType() == type).findFirst().orElse(null);
    }

    public boolean shouldNotCancelEffect(ItemStack item) {
        if(item == null || !(item.getItemMeta() instanceof PotionMeta meta)) return true;

        List<PotionEffect> effects = new ArrayList<>();
        boolean extended = false;

        PotionType base = meta.getBasePotionType();
        if(base != null) {
            effects.addAll(base.getPotionEffects());
            extended = base.name().contains("LONG");
        }
        effects.addAll(meta.getCustomEffects());

        if(effects.isEmpty()) return true;

        for(PotionEffect effect : effects) {
            PotionLimit limit = this.getPotionLimit(effect.getType());
            if(limit == null) return true;

            if(limit.getLevel() == 0 || (effect.getAmplifier() + 1) > limit.getLevel()) return false;
            if(extended && !limit.isExtended()) return false;
        }

        return true;
    }

    public boolean shouldNotCancelEffect(PotionEffect effect) {
        PotionLimit limit = this.getPotionLimit(effect.getType());
        if(limit == null) return true;

        return limit.getLevel() != 0 && (effect.getAmplifier() + 1) <= limit.getLevel();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionBrew(BrewEvent event) {
        BrewerInventory brewer = event.getContents();
        ItemStack ingredient = brewer.getIngredient().clone();

        ItemStack[] potions = new ItemStack[3];

        for(int i = 0; i < 3; i++) {
            if(event.getContents().getItem(i) == null) continue;

            potions[i] = brewer.getItem(i).clone();
        }

        Tasks.sync(() -> {
            for(int i = 0; i < 3; i++) {
                if(brewer.getItem(i) == null) continue;
                if(this.shouldNotCancelEffect(brewer.getItem(i))) continue;

                brewer.setItem(i, potions[i]);
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if(item.getType() != Material.POTION) return;
        if(this.shouldNotCancelEffect(item)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        player.setItemInHand(new ItemStack(Material.AIR));
        player.sendMessage(Language.PREFIX + Language.POTION_LIMITER_DENY_MESSAGE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion thrownPotion = event.getPotion();
        if(this.shouldNotCancelEffect(thrownPotion.getItem())) return;

        event.setCancelled(true);

        if(!(thrownPotion.getShooter() instanceof Player)) return;
        Player player = (Player) thrownPotion.getShooter();

        player.setItemInHand(new ItemStack(Material.AIR));
        player.sendMessage(Language.PREFIX + Language.POTION_LIMITER_DENY_MESSAGE);
    }

    @Getter @Setter
    private static class PotionLimit {

        private PotionEffectType type;
        private int level;
        private boolean extended;
    }
}
