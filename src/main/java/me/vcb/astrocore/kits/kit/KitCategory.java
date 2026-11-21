package me.vcb.astrocore.kits.kit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public enum KitCategory {

    GRATUITO("§a§lKits Gratuitos", "§7Disponibles para todos.",          Material.CHEST,      "§a"),
    GKIT    ("§6§lGKits",          "§7God Kits y kits exclusivos premium.", Material.GOLD_BLOCK, "§6");

    private final String displayName;
    private final String description;
    private final Material icon;
    private final String color;

    public static KitCategory fromName(String name) {
        if(name == null) return GRATUITO;
        // Map old PREMIUM value to GKIT for backwards compatibility
        if(name.equalsIgnoreCase("PREMIUM")) return GKIT;
        try {
            return KitCategory.valueOf(name.toUpperCase());
        } catch(IllegalArgumentException e) {
            return GRATUITO;
        }
    }
}
