package me.vcb.astrocore.coinshop;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;

@Getter
public enum TrailType {

    NINGUNO("ninguno", "§7[Ningún Trail]", "Sin trail equipado.", 0, Material.BARRIER, null, 0),
    LLAMA("llama", "§c[Llama]", "Deja llamas a tu paso.", 500, Material.BLAZE_ROD, Particle.FLAME, 3),
    ENCANTAMIENTO("encantamiento", "§d[Encantamiento]", "Partículas de encantamiento.", 600, Material.ENCHANTED_BOOK, Particle.ENCHANT, 5),
    CORAZONES("corazones", "§c[Corazones]", "Deja corazones flotantes.", 800, Material.RED_DYE, Particle.HEART, 2),
    NOTAS("notas", "§6[Notas]", "Notas musicales a tu paso.", 700, Material.JUKEBOX, Particle.NOTE, 3),
    AGUA("agua", "§b[Agua]", "Salpicaduras de agua.", 600, Material.WATER_BUCKET, Particle.SPLASH, 8),
    HUMO("humo", "§8[Humo]", "Estela de humo oscuro.", 500, Material.COAL, Particle.SMOKE, 5),
    CRITICO("critico", "§e[Crítico]", "Partículas de golpe crítico.", 1000, Material.GOLDEN_SWORD, Particle.CRIT, 6),
    MAGIA("magia", "§5[Magia]", "Magia de bruja a tu paso.", 1200, Material.BREWING_STAND, Particle.WITCH, 4),
    NIEVE("nieve", "§f[Nieve]", "Copos de nieve cayendo.", 1500, Material.SNOWBALL, Particle.SNOWFLAKE, 6),
    DRAGON("dragon", "§4[Dragón]", "Aliento de dragón.", 2500, Material.DRAGON_BREATH, Particle.DRAGON_BREATH, 5),
    PORTAL("portal", "§5[Portal]", "Partículas de portal.", 3000, Material.ENDER_PEARL, Particle.PORTAL, 10),
    AURORA("aurora", "§b§l[Aurora]", "Estela de aurora boreal.", 5000, Material.END_ROD, Particle.END_ROD, 4),
    TOTEM("totem", "§e§l[Tótem]", "El poder del tótem te acompaña.", 8000, Material.TOTEM_OF_UNDYING, Particle.TOTEM_OF_UNDYING, 3);

    private final String id;
    private final String displayName;
    private final String description;
    private final int price;
    private final Material icon;
    private final Particle particle;
    private final int particleCount;

    TrailType(String id, String displayName, String description, int price, Material icon, Particle particle, int particleCount) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.price = price;
        this.icon = icon;
        this.particle = particle;
        this.particleCount = particleCount;
    }

    public boolean isFree() {
        return this.price == 0;
    }

    public static TrailType fromId(String id) {
        if (id == null) return null;
        for (TrailType t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        return null;
    }
}
