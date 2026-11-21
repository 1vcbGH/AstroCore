package me.vcb.astrocore.utils.item;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.util.Set;

public class FakeGlow extends EnchantmentWrapper {

    private static final NamespacedKey KEY = NamespacedKey.fromString("astrocore:fake_glow");

    public FakeGlow() {
        super();
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @Override
    public String getTranslationKey() {
        return "enchantment.astrocore.fake_glow";
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return true;
    }

    @Override
    public boolean conflictsWith(Enchantment item) {
        return false;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return null;
    }

    @Override
    public String getName() {
        return "FakeGlow";
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public RegistryKeySet<Enchantment> getExclusiveWith() {
        return RegistrySet.keySet(RegistryKey.ENCHANTMENT);
    }

    @Override
    public float getDamageIncrease(int level, EntityType entityType) {
        return 0;
    }

    @Override
    public Set<EquipmentSlotGroup> getActiveSlotGroups() {
        return Set.of();
    }

    @Override
    public Component description() {
        return Component.text("FakeGlow");
    }

    @Override
    public RegistryKeySet<ItemType> getSupportedItems() {
        return RegistrySet.keySet(RegistryKey.ITEM);
    }

    @Override
    public RegistryKeySet<ItemType> getPrimaryItems() {
        return null;
    }

    @Override
    public int getWeight() {
        return 1;
    }

    @Override
    public int getAnvilCost() {
        return 1;
    }
}
