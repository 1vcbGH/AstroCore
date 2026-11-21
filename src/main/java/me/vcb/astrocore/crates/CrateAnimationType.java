package me.vcb.astrocore.crates;

import org.bukkit.Material;

/** Selectable opening-animation styles for a crate. */
public enum CrateAnimationType {

    SPIN("Ruleta horizontal", "Items deslizan a los lados y paran en el centro.", Material.HOPPER),
    ROULETTE("Ruleta vertical", "Columna central gira hacia abajo y desacelera.", Material.CHAIN),
    REVEAL("Revelado", "Se muestran varios items y se descartan hasta el ganador.", Material.SCULK_SENSOR),
    FLASH("Parpadeo", "Items random parpadean rápido y se congelan en el premio.", Material.FIREWORK_ROCKET),
    INSTANT("Instantáneo", "Sin animación, premio directo.", Material.COMMAND_BLOCK);

    private final String displayName;
    private final String description;
    private final Material icon;

    CrateAnimationType(String displayName, String description, Material icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Material getIcon() { return icon; }

    public CrateAnimationType next() {
        CrateAnimationType[] v = values();
        return v[(ordinal() + 1) % v.length];
    }

    public static CrateAnimationType fromName(String name) {
        if (name == null) return SPIN;
        try { return valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return SPIN; }
    }
}
