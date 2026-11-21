package me.vcb.astrocore.utils;

import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.potion.PotionEffect;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;

public class ServerUtils {

    public static final Set<SpawnReason> ALLOWED_SPAWN_REASONS = EnumSet.of(SpawnReason.SPAWNER_EGG, SpawnReason.EGG,
        SpawnReason.SPAWNER, SpawnReason.BREEDING, SpawnReason.CUSTOM, SpawnReason.SLIME_SPLIT);

    public static PotionEffect getEffect(EntityPotionEffectEvent event) {
        PotionEffect effect = event.getNewEffect();
        if (effect == null) effect = event.getOldEffect();
        return effect;
    }

    public static Function<String, String> parsePlaceholderFunction(String value, String placeholder) {
        if(value.isEmpty()) {
            return null;
        } else {
            int indexOf = value.indexOf(placeholder);

            if(indexOf == 0) {
                String finalPlaceholder = value.replace(placeholder, "");
                return s -> s + finalPlaceholder;
            } else {
                String[] placeholderParts = value.split(placeholder);

                if(placeholderParts.length == 1) {
                    return s -> placeholderParts[0] + s;
                } else {
                    return s -> placeholderParts[0] + s + placeholderParts[1];
                }
            }
        }
    }
}
