package me.vcb.astrocore.punishments;

import me.vcb.astrocore.sanctions.SanctionType;
import org.bukkit.Material;

import java.util.List;

/**
 * Catalog of offense categories with escalating per-offense durations.
 *
 * Each category has a list of tier durations (millis, 0 = permanent). The first
 * offense uses tier[0], the second tier[1], and so on; once the last tier is
 * reached it stays there.
 */
public enum PunishmentCategory {

    // ── Mutes ──────────────────────────────────────────────────────────────
    CHAT_MISUSE("Mal uso del Chat", "Insultos, spam, flood, comportamiento inapropiado",
        SanctionType.MUTE, true, Material.PAPER,
        new long[]{ h(1), h(2), h(12), d(1) }),

    STAFF_INSULTS("Insultos al Staff", "Insultos dirigidos al equipo de staff",
        SanctionType.MUTE, false, Material.WRITABLE_BOOK,
        new long[]{ d(1), d(3), d(7), d(14) }),

    // ── Bans ───────────────────────────────────────────────────────────────
    BAD_NICK("Nick Inapropiado", "Multicuenta permitida",
        SanctionType.BAN, false, Material.NAME_TAG,
        new long[]{ 0 }),

    BAD_SKIN("Skin Inapropiada", "Skin ofensiva o inapropiada",
        SanctionType.BAN, false, Material.LEATHER_CHESTPLATE,
        new long[]{ d(1), d(3), d(5), d(7) }),

    PROMOTE_DISOBEDIENCE("Promover la Desobediencia", "Incitar a romper las reglas",
        SanctionType.BAN, false, Material.TNT,
        new long[]{ d(1), d(3), d(7), 0 }),

    SERVER_INSULTS("Insultos al Servidor", "Insultos contra el servidor",
        SanctionType.BAN, false, Material.REDSTONE,
        new long[]{ d(1), d(3), 0 }),

    FAKE_STAFF("Fake Staff", "Hacerse pasar por staff",
        SanctionType.BAN, false, Material.IRON_HELMET,
        new long[]{ d(3), d(7), 0 }),

    GRIEF_NEAR_CLAIM("Grifeos alrededor de un Claim", "Griefeo cerca de un claim",
        SanctionType.BAN, true, Material.FLINT_AND_STEEL,
        new long[]{ d(1), d(3), d(7) }),

    CLAIM_TROLL("Claim Troll", "Trolleo con claims",
        SanctionType.BAN, true, Material.OAK_FENCE,
        new long[]{ d(1), d(3), d(7) }),

    ANTI_AFK("Máquina AntiAFK", "Uso de máquinas anti-AFK",
        SanctionType.BAN, true, Material.REDSTONE_TORCH,
        new long[]{ d(1), d(3), d(7) }),

    TPA_KILL("TPAKill", "Matar usando TPA",
        SanctionType.BAN, false, Material.ENDER_PEARL,
        new long[]{ d(7), d(14), d(30), 0 }),

    STAFF_DEFAMATION("Difamación al Staff/Directivo", "Difamación al equipo staff y/o directivo",
        SanctionType.BAN, false, Material.BOOK,
        new long[]{ d(3), d(7), 0 }),

    SCAM("Estafas / Robos", "Estafas o robos a usuarios",
        SanctionType.BAN, true, Material.GOLD_INGOT,
        new long[]{ d(15), d(30), 0 }),

    BAN_EVASION("Multicuenta para Evadir Sanción", "Evadir una sanción con otra cuenta",
        SanctionType.BAN, true, Material.SKELETON_SKULL,
        new long[]{ 0 }),

    NSFW_MINORS("Material no Apto para Menores", "Notificación a autoridades y tutores legales",
        SanctionType.BAN, false, Material.RED_BANNER,
        new long[]{ 0 }),

    ILLEGAL_CLIENTS("Clientes Ilegales / Abuso de Errores", "Uso de clientes ilegales y abuso de errores",
        SanctionType.BAN, false, Material.NETHERITE_SWORD,
        new long[]{ 0 }),

    SERVER_GRIEF("Grifeos al Servidor", "Griefeo al servidor",
        SanctionType.BAN, false, Material.LAVA_BUCKET,
        new long[]{ 0 }),

    EXPLOITS_BUGS("Uso de Exploits / Bugs", "Uso de exploits o bugs",
        SanctionType.BAN, false, Material.COMMAND_BLOCK,
        new long[]{ 0 }),

    FREE_KILLS("Free Kills", "Free kills",
        SanctionType.BAN, true, Material.DIAMOND_SWORD,
        new long[]{ d(1), d(3), d(7), 0 }),

    CONFIG_ABUSE("Aprovechamiento de Errores de Config.", "Aprovechar errores de configuración",
        SanctionType.BAN, true, Material.COMPARATOR,
        new long[]{ d(15), d(30), 0 }),

    HACKS_EVIDENT("Uso de Hacks (Evidentes)", "Hacks encontrados o evidentes",
        SanctionType.BAN, false, Material.IRON_SWORD,
        new long[]{ d(30), d(60), d(90) }),

    HACKS_ADMITTED("Uso de Hacks (Admitidos)", "Hacks admitidos por el usuario",
        SanctionType.BAN, false, Material.STONE_SWORD,
        new long[]{ d(10), d(20), d(30) }),

    REFUSE_SS("Refuse SS", "Negarse a un screenshare",
        SanctionType.BAN, true, Material.SPYGLASS,
        new long[]{ 0 }),

    ADVERTISING_INTENTIONAL("Publicidad Intencional", "Publicidad intencional",
        SanctionType.BAN, false, Material.OAK_SIGN,
        new long[]{ 0 }),

    ADVERTISING_UNINTENTIONAL("Publicidad no Intencional", "Publicidad no intencional",
        SanctionType.BAN, true, Material.PAPER,
        new long[]{ d(1), d(3), d(7) }),

    USER_EXPROPRIATION("Expropiación de Usuarios", "Expropiación de usuarios",
        SanctionType.BAN, false, Material.CHEST,
        new long[]{ 0 });

    private final String       displayName;
    private final String       description;
    private final SanctionType type;       // BAN or MUTE base type
    private final boolean      appealable;
    private final Material     icon;
    private final long[]       tiers;       // millis per offense, 0 = permanent

    PunishmentCategory(String displayName, String description, SanctionType type,
                       boolean appealable, Material icon, long[] tiers) {
        this.displayName = displayName;
        this.description = description;
        this.type        = type;
        this.appealable  = appealable;
        this.icon        = icon;
        this.tiers       = tiers;
    }

    public String id() { return name(); }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public SanctionType getType()  { return type; }
    public boolean isAppealable()  { return appealable; }
    public Material getIcon()      { return icon; }
    public int tierCount()         { return tiers.length; }

    /** Duration (millis, 0 = permanent) for a 0-based tier index, clamped to last. */
    public long durationForTier(int tierIndex) {
        if (tierIndex < 0) tierIndex = 0;
        if (tierIndex >= tiers.length) tierIndex = tiers.length - 1;
        return tiers[tierIndex];
    }

    private static long h(long n) { return n * 3_600_000L; }
    private static long d(long n) { return n * 86_400_000L; }

    public static PunishmentCategory byId(String id) {
        if (id == null) return null;
        try { return valueOf(id); } catch (IllegalArgumentException e) { return null; }
    }

    public static List<PunishmentCategory> all() { return List.of(values()); }
}
