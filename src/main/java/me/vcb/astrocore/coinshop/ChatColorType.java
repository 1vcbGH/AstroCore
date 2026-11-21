package me.vcb.astrocore.coinshop;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum ChatColorType {

    DEFAULT("default", "§f[Blanco]", "§f", 0, Material.WHITE_DYE, "Color de chat blanco por defecto."),
    AMARILLO("amarillo", "§e[Amarillo]", "§e", 300, Material.YELLOW_DYE, "Escribe en amarillo."),
    VERDE("verde", "§a[Verde]", "§a", 400, Material.LIME_DYE, "Escribe en verde."),
    AQUA("aqua", "§b[Aqua]", "§b", 500, Material.CYAN_DYE, "Escribe en aqua."),
    ROJO("rojo", "§c[Rojo]", "§c", 500, Material.RED_DYE, "Escribe en rojo."),
    DORADO("dorado", "§6[Dorado]", "§6", 600, Material.GOLD_INGOT, "Escribe en dorado."),
    ROSA("rosa", "§d[Rosa]", "§d", 800, Material.PINK_DYE, "Escribe en rosa."),
    AZUL("azul", "§9[Azul]", "§9", 600, Material.BLUE_DYE, "Escribe en azul."),
    GRIS("gris", "§7[Gris]", "§7", 300, Material.GRAY_DYE, "Escribe en gris."),
    VERDE_OSCURO("verde_oscuro", "§2[Verde Oscuro]", "§2", 500, Material.GREEN_DYE, "Escribe en verde oscuro."),
    CELESTE("celeste", "§3[Celeste]", "§3", 700, Material.LIGHT_BLUE_DYE, "Escribe en celeste."),
    PURPURA("purpura", "§5[Púrpura]", "§5", 1000, Material.PURPLE_DYE, "Escribe en púrpura."),
    NEGRITA("negrita", "§l[Negrita]", "§l", 1500, Material.COAL, "Escribe en negrita."),
    ITALIC("italic", "§o[Cursiva]", "§o", 1200, Material.FEATHER, "Escribe en cursiva.");

    private final String id;
    private final String displayName;
    private final String colorCode;
    private final int price;
    private final Material icon;
    private final String description;

    ChatColorType(String id, String displayName, String colorCode, int price, Material icon, String description) {
        this.id = id;
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.price = price;
        this.icon = icon;
        this.description = description;
    }

    public boolean isFree() {
        return this.price == 0;
    }

    public static ChatColorType fromId(String id) {
        if (id == null) return null;
        for (ChatColorType c : values()) {
            if (c.id.equalsIgnoreCase(id)) return c;
        }
        return null;
    }
}
