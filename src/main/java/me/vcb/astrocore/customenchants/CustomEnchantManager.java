package me.vcb.astrocore.customenchants;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CustomEnchantManager {

    @Getter private static CustomEnchantManager instance;

    public CustomEnchantManager() {
        instance = this;
    }

    // ── PDC helpers ──────────────────────────────────────────────────────────

    private NamespacedKey key(CustomEnchant ce) {
        return new NamespacedKey(AstroCore.getInstance(), "ce_" + ce.getId());
    }

    /** Returns level of the enchant on the item, or 0 if not present. */
    public int getLevel(ItemStack item, CustomEnchant ce) {
        if (item == null || !item.hasItemMeta()) return 0;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        Integer level = pdc.get(key(ce), PersistentDataType.INTEGER);
        return level == null ? 0 : level;
    }

    /** Returns all custom enchants on the item and their levels. */
    public Map<CustomEnchant, Integer> getEnchants(ItemStack item) {
        Map<CustomEnchant, Integer> result = new EnumMap<>(CustomEnchant.class);
        if (item == null || !item.hasItemMeta()) return result;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        for (CustomEnchant ce : CustomEnchant.values()) {
            Integer lvl = pdc.get(key(ce), PersistentDataType.INTEGER);
            if (lvl != null && lvl > 0) result.put(ce, lvl);
        }
        return result;
    }

    /**
     * Applies (or updates) a custom enchant on the item.
     * Returns false if the item is incompatible or level is out of range.
     */
    public boolean apply(ItemStack item, CustomEnchant ce, int level) {
        if (item == null) return false;
        if (!ce.appliesTo(item.getType())) return false;
        if (level < 1 || level > ce.getMaxLevel()) return false;

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key(ce), PersistentDataType.INTEGER, level);
        rebuildLore(meta, item);
        item.setItemMeta(meta);
        return true;
    }

    /** Removes a custom enchant from the item. */
    public boolean remove(ItemStack item, CustomEnchant ce) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(key(ce), PersistentDataType.INTEGER)) return false;
        meta.getPersistentDataContainer().remove(key(ce));
        rebuildLore(meta, item);
        item.setItemMeta(meta);
        return true;
    }

    // ── Lore management ──────────────────────────────────────────────────────

    private static final String LORE_SEPARATOR = "§8§m──────────────────";

    private void rebuildLore(ItemMeta meta, ItemStack item) {
        List<String> existing = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        // Strip old CE lines (separator + enchant lines)
        List<String> cleaned = new ArrayList<>();
        boolean inCeBlock = false;
        for (String line : existing) {
            if (line.equals(LORE_SEPARATOR)) { inCeBlock = true; continue; }
            if (inCeBlock && isCeLoreLine(line)) continue;
            inCeBlock = false;
            cleaned.add(line);
        }

        // Build new CE lore lines from PDC
        Map<CustomEnchant, Integer> enchants = getEnchantsFromMeta(meta);
        if (!enchants.isEmpty()) {
            List<String> ceLines = new ArrayList<>();
            ceLines.add(LORE_SEPARATOR);
            enchants.forEach((ce, lvl) ->
                ceLines.add(ce.getLorePrefix() + toRoman(lvl))
            );
            // Prepend CE block
            cleaned.addAll(0, ceLines);
        }

        meta.setLore(cleaned);
    }

    private Map<CustomEnchant, Integer> getEnchantsFromMeta(ItemMeta meta) {
        Map<CustomEnchant, Integer> result = new EnumMap<>(CustomEnchant.class);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        for (CustomEnchant ce : CustomEnchant.values()) {
            Integer lvl = pdc.get(key(ce), PersistentDataType.INTEGER);
            if (lvl != null && lvl > 0) result.put(ce, lvl);
        }
        return result;
    }

    private boolean isCeLoreLine(String line) {
        for (CustomEnchant ce : CustomEnchant.values())
            if (line.startsWith(ce.getLorePrefix())) return true;
        return false;
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(n);
        };
    }
}
