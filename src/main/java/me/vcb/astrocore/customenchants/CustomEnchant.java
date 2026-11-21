package me.vcb.astrocore.customenchants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum CustomEnchant {

    // ── Armor ────────────────────────────────────────────────────────────────

    /** Chestplate — 5%/8%/12% lifesteal */
    VAMPIRISMO(
        "vampirismo", "§c§lVampirismo",
        "§7Roba vida al hacer daño. §8(5%/8%/12%)",
        3, Material.REDSTONE,
        Arrays.asList(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE,
                      Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE,
                      Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE),
        "§8✦ §cVampirismo §7Lv."
    ),

    /** Boots — Speed II pasivo */
    VELOZ(
        "veloz", "§a§lVeloz",
        "§7Speed II pasivo mientras llevás las botas.",
        1, Material.FEATHER,
        Arrays.asList(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS,
                      Material.IRON_BOOTS, Material.GOLDEN_BOOTS,
                      Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS),
        "§8✦ §aVeloz §7Lv."
    ),

    /** Boots — Jump Boost I/II */
    SPRING(
        "spring", "§b§lSpring",
        "§7Jump Boost pasivo en botas. §8(I/II)",
        2, Material.SLIME_BALL,
        Arrays.asList(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS,
                      Material.IRON_BOOTS, Material.GOLDEN_BOOTS,
                      Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS),
        "§8✦ §bSpring §7Lv."
    ),

    /** Helmet — Regeneration I pasivo */
    REGENERATIVO(
        "regenerativo", "§d§lRegenerativo",
        "§7Regeneración I pasiva en casco.",
        1, Material.GLISTERING_MELON_SLICE,
        Arrays.asList(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET,
                      Material.IRON_HELMET, Material.GOLDEN_HELMET,
                      Material.DIAMOND_HELMET, Material.NETHERITE_HELMET),
        "§8✦ §dRegenerativo §7Lv."
    ),

    /** Leggings — Invisibilidad permanente mientras las llevás */
    NINJA(
        "ninja", "§7§lNinja",
        "§7Invisibilidad permanente mientras llevás los pantalones.",
        1, Material.ENDER_EYE,
        Arrays.asList(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS,
                      Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS,
                      Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS),
        "§8✦ §7Ninja §7Lv."
    ),

    /** Chestplate — refleja 4%/8% del daño recibido */
    AURA(
        "aura", "§e§lAura",
        "§7Refleja daño al atacante. §8(4%/8%)",
        2, Material.NETHER_STAR,
        Arrays.asList(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE,
                      Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE,
                      Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE),
        "§8✦ §eAura §7Lv."
    ),

    /** Leggings — Fire Resistance I permanente */
    IGNIFUGO(
        "ignifugo", "§c§lIgnífugo",
        "§7Resistencia al fuego mientras llevás los pantalones.",
        1, Material.MAGMA_CREAM,
        Arrays.asList(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS,
                      Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS,
                      Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS),
        "§8✦ §cIgnífugo §7Lv."
    ),

    /** Chestplate — Absorption I/II hearts passiva */
    IMPLANTES(
        "implantes", "§b§lImplantes",
        "§7Corazones de absorción pasivos. §8(I/II)",
        2, Material.GOLDEN_APPLE,
        Arrays.asList(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE,
                      Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE,
                      Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE),
        "§8✦ §bImplantes §7Lv."
    ),

    /** Leggings — Strength I pasiva mientras las llevás */
    FURIA(
        "furia", "§4§lFuria",
        "§7Fuerza I pasiva mientras llevás los pantalones.",
        1, Material.BLAZE_POWDER,
        Arrays.asList(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS,
                      Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS,
                      Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS),
        "§8✦ §4Furia §7Lv."
    ),

    /** Helmet — Regen II por 3s cuando HP baja de 4 corazones (cooldown 15s) */
    RECUPERAR(
        "recuperar", "§a§lRecuperar",
        "§7Regen II por 3s al caer de 4 corazones. §8(cd: 15s)",
        1, Material.HEART_OF_THE_SEA,
        Arrays.asList(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET,
                      Material.IRON_HELMET, Material.GOLDEN_HELMET,
                      Material.DIAMOND_HELMET, Material.NETHERITE_HELMET),
        "§8✦ §aRecuperar §7Lv."
    ),

    /** Leggings — reducción plana 0.3/0.6/0.9 de daño */
    ENDURECIDO(
        "endurecido", "§6§lEndurecido",
        "§7Reducción plana de daño en piernas. §8(0.3/0.6/0.9♥)",
        3, Material.IRON_INGOT,
        Arrays.asList(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS,
                      Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS,
                      Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS),
        "§8✦ §6Endurecido §7Lv."
    ),

    // ── Weapons ──────────────────────────────────────────────────────────────

    /** Sword — Poison I/II al golpear */
    VENENO_X(
        "veneno_x", "§2§lVeneno+",
        "§7Envenena al objetivo al golpear. §8(I/II)",
        2, Material.SPIDER_EYE,
        Arrays.asList(Material.WOODEN_SWORD, Material.STONE_SWORD,
                      Material.IRON_SWORD, Material.GOLDEN_SWORD,
                      Material.DIAMOND_SWORD, Material.NETHERITE_SWORD),
        "§8✦ §2Veneno+ §7Lv."
    ),

    /** Sword — fuego 1.5s/3s al golpear */
    LLAMAS_X(
        "llamas_x", "§6§lLlamas+",
        "§7Prende fuego al objetivo. §8(1.5s/3s)",
        2, Material.BLAZE_POWDER,
        Arrays.asList(Material.WOODEN_SWORD, Material.STONE_SWORD,
                      Material.IRON_SWORD, Material.GOLDEN_SWORD,
                      Material.DIAMOND_SWORD, Material.NETHERITE_SWORD),
        "§8✦ §6Llamas+ §7Lv."
    ),

    /** Sword — Speed I por 3s tras matar */
    COMBO(
        "combo", "§f§lCombo",
        "§7Speed I por 3s después de matar.",
        1, Material.RABBIT_FOOT,
        Arrays.asList(Material.WOODEN_SWORD, Material.STONE_SWORD,
                      Material.IRON_SWORD, Material.GOLDEN_SWORD,
                      Material.DIAMOND_SWORD, Material.NETHERITE_SWORD),
        "§8✦ §fCombo §7Lv."
    ),

    /** Sword — Slowness I/II al golpear */
    RALENTIZAR(
        "ralentizar", "§9§lRalentizar",
        "§7Slowness al objetivo al golpear. §8(I/II)",
        2, Material.SOUL_SAND,
        Arrays.asList(Material.WOODEN_SWORD, Material.STONE_SWORD,
                      Material.IRON_SWORD, Material.GOLDEN_SWORD,
                      Material.DIAMOND_SWORD, Material.NETHERITE_SWORD),
        "§8✦ §9Ralentizar §7Lv."
    ),

    /** Sword — Weakness I al golpear */
    HOLY(
        "holy", "§f§lHoly",
        "§7Aplica Weakness I al golpear.",
        1, Material.GOLDEN_APPLE,
        Arrays.asList(Material.WOODEN_SWORD, Material.STONE_SWORD,
                      Material.IRON_SWORD, Material.GOLDEN_SWORD,
                      Material.DIAMOND_SWORD, Material.NETHERITE_SWORD),
        "§8✦ §fHoly §7Lv."
    );

    // ── Fields ───────────────────────────────────────────────────────────────
    private final String         id;
    private final String         displayName;
    private final String         description;
    private final int            maxLevel;
    private final Material       icon;
    private final List<Material> applicable;
    private final String         lorePrefix;

    public boolean appliesTo(Material mat) {
        return applicable.contains(mat);
    }

    public static CustomEnchant fromId(String id) {
        if (id == null) return null;
        for (CustomEnchant ce : values())
            if (ce.id.equalsIgnoreCase(id)) return ce;
        return null;
    }
}
