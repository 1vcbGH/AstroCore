package me.vcb.astrocore.handlers.logger;

import me.vcb.astrocore.utils.item.ItemUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Stream;

public interface CombatLogger {

    void removeCombatLogger();

    float getCombatLoggerHealth();

    Location getCombatLoggerLocation();

    void handleEffectChanges(Player player);

    default boolean wasKilled() { return false; }

    default Location getKillLocation() { return null; }

    default void dropPlayerItems(ItemStack[] contents, ItemStack[] armor, Location location) {
        Stream.of(contents).filter(ItemUtils.NOT_NULL_ITEM_FILTER)
            .forEach(item -> location.getWorld().dropItemNaturally(location, item));

        Stream.of(armor).filter(ItemUtils.NOT_NULL_ITEM_FILTER)
            .forEach(item -> location.getWorld().dropItemNaturally(location, item));
    }
}
