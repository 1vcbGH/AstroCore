package me.vcb.astrocore.killeffects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@Getter
@RequiredArgsConstructor
public enum KillEffect {

    NINGUNO     ("ninguno",      "Sin efecto",       "Nada ocurre al matar.",                   Material.BARRIER,           0,    true),
    RAYO        ("rayo",         "⚡ Rayo",           "Cae un rayo en el lugar de la muerte.",   Material.LIGHTNING_ROD,     500,  false),
    FUEGOS      ("fuegos",       "🎆 Fuegos Artificiales","Fuegos artificiales al matar.",       Material.FIREWORK_ROCKET,   750,  false),
    EXPLOSION   ("explosion",    "💥 Explosión",     "Efecto de explosión (sin daño).",          Material.TNT,               1000, false),
    LLAMAS      ("llamas",       "🔥 Llamas",        "Columna de fuego al matar.",               Material.BLAZE_POWDER,      600,  false),
    DRAGON      ("dragon",       "🐉 Dragon",        "Partículas de dragón al matar.",           Material.DRAGON_BREATH,     1500, false),
    CORAZON     ("corazon",      "❤ Corazones",     "Lluvia de corazones al matar.",            Material.POPPY,             400,  false);

    private final String   id;
    private final String   displayName;
    private final String   description;
    private final Material icon;
    private final int      price;
    private final boolean  free;

    public static KillEffect fromId(String id) {
        for (KillEffect e : values()) if (e.id.equalsIgnoreCase(id)) return e;
        return NINGUNO;
    }
}
