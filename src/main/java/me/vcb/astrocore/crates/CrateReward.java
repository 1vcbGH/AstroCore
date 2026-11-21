package me.vcb.astrocore.crates;

import lombok.Getter;
import lombok.Setter;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Getter
@Setter
public class CrateReward {

    private int          slot;       // 0-53 in the 54-slot grid
    private String       itemBase64; // serialized ItemStack
    private boolean      winnable;   // false = display only
    private int          chance;     // weight for rolling
    private List<String> commands;

    public CrateReward() {
        this.slot     = 0;
        this.winnable = false;
        this.chance   = 10;
        this.commands = new ArrayList<>();
    }

    public CrateReward(int slot, ItemStack item) {
        this();
        this.slot = slot;
        setItem(item);
    }

    // ── Item helpers ──────────────────────────────────────────────────────────

    public ItemStack getItem() {
        if (itemBase64 == null) return null;
        try { return ItemStack.deserializeBytes(Base64.getDecoder().decode(itemBase64)); }
        catch (Exception e) { return null; }
    }

    public void setItem(ItemStack item) {
        if (item == null || item.getType().isAir()) { this.itemBase64 = null; return; }
        this.itemBase64 = Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    // ── Build display item for the grid GUI ───────────────────────────────────

    /**
     * Winnable items get a glow override so admins can distinguish them.
     * No name or lore changes — item looks exactly as placed.
     */
    public ItemStack buildGridItem() {
        ItemStack base = getItem();
        if (base == null) return null;
        ItemStack display = base.clone();
        var meta = display.getItemMeta();
        if (meta != null) {
            meta.setEnchantmentGlintOverride(winnable);
            if (!winnable) meta.setDisplayName("§r"); // hide name for display-only items
            display.setItemMeta(meta);
        }
        return display;
    }

    /** Clean item for the spin animation (no editor lore). */
    public ItemStack buildSpinItem() {
        ItemStack base = getItem();
        if (base == null) return new ItemBuilder(Material.CHEST).setName("§fPremio").build();
        return base.clone();
    }

    /** Item shown in preview GUI (read-only, shows chance %). */
    public ItemStack buildPreviewItem(int totalWeight) {
        ItemStack base = getItem();
        if (base == null) return null;

        ItemStack preview = base.clone();
        ItemMeta  meta    = preview.getItemMeta();
        if (meta == null) return preview;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(" ");
        if (totalWeight > 0) {
            double pct = (double) chance / totalWeight * 100;
            lore.add(Color.translate("§7Probabilidad: §e" + String.format("%.1f", pct) + "%"));
        }
        if (!commands.isEmpty())
            lore.add(Color.translate("§7Comandos: §f" + commands.size()));

        meta.setLore(lore);
        meta.setEnchantmentGlintOverride(true);
        preview.setItemMeta(meta);
        return preview;
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void save(ConfigurationSection section) {
        section.set("slot",     slot);
        section.set("item",     itemBase64);
        section.set("winnable", winnable);
        section.set("chance",   chance);
        section.set("commands", commands);
    }

    public static CrateReward load(ConfigurationSection section) {
        CrateReward r = new CrateReward();
        r.slot     = section.getInt("slot", 0);
        r.itemBase64 = section.getString("item");
        r.winnable = section.getBoolean("winnable", false);
        r.chance   = section.getInt("chance", 10);
        r.commands = section.getStringList("commands");
        return r;
    }
}
