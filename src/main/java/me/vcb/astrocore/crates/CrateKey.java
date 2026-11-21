package me.vcb.astrocore.crates;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class CrateKey {

    private static final NamespacedKey KEY_ID = new NamespacedKey(AstroCore.getInstance(), "crate_key_id");

    public static ItemStack createKey(Crate crate, int amount) {
        Material mat;
        try { mat = Material.valueOf(crate.getKeyMaterial().toUpperCase()); }
        catch (Exception e) { mat = Material.TRIPWIRE_HOOK; }

        ItemStack item = new ItemStack(mat, amount);
        ItemMeta  meta = item.getItemMeta();

        String displayName = Color.translate(crate.getDisplayName());
        meta.setDisplayName(Color.translate("§6§lLlave §r" + displayName));

        String desc = Color.translate(crate.getKeyDescription()
            .replace("%crate%", displayName));
        meta.setLore(Arrays.asList(
            Color.translate("§8──────────────────"),
            desc,
            Color.translate("§8──────────────────"),
            Color.translate("§7Click derecho en la crate para abrir.")
        ));
        meta.setEnchantmentGlintOverride(true);
        meta.getPersistentDataContainer().set(KEY_ID, PersistentDataType.STRING, crate.getId());

        item.setItemMeta(meta);
        return item;
    }

    /** Returns crate id stored on this key, or null if not a key. */
    public static String getCrateId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.get(KEY_ID, PersistentDataType.STRING);
    }

    public static boolean isKeyFor(ItemStack item, Crate crate) {
        String id = getCrateId(item);
        return id != null && id.equalsIgnoreCase(crate.getId());
    }
}
