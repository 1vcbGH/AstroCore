package me.vcb.astrocore.tags;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum ChatTag {

    // Free
    RANGO        ("rango",        "§7[Rango]",            0,      Material.NETHER_STAR,        "§7Muestra tu rango de ELO o posición top en el chat."),

    // Baratos — 100–600 coins
    NOOB         ("noob",         "§7[Noob]",             100,    Material.WOODEN_SWORD,       "§7Para los que recién empiezan... o no."),
    GRIND        ("grind",        "§a[Grind]",            150,    Material.CLOCK,              "§7Siempre en el servidor."),
    AFK          ("afk",          "§8[AFK]",              150,    Material.GRAY_BED,           "§7¿Está o no está?"),
    RATA         ("rata",         "§8[Rata]",             200,    Material.RABBIT_FOOT,        "§7Huye cuando más lo necesitan."),
    TOXIC        ("toxic",        "§2[Toxic]",            200,    Material.SLIME_BALL,         "§7Mejor no cruzarte."),
    TRYHARD      ("tryhard",      "§e[Tryhard]",          200,    Material.GOLDEN_SWORD,       "§7Juega como si su vida dependiera de ello."),
    VIP          ("vip",          "§b[VIP]",              250,    Material.DIAMOND,            "§7El clásico de los clásicos."),
    SWEAT        ("sweat",        "§3[Sweat]",            300,    Material.WATER_BUCKET,       "§7Demasiado serio para un juego."),
    GUERRILLA    ("guerrilla",    "§2[Guerrilla]",        300,    Material.CROSSBOW,           "§7Golpea y desaparece."),
    REBELDE      ("rebelde",      "§c[Rebelde]",          350,    Material.TNT,                "§7No sigue las reglas."),
    MVP          ("mvp",          "§6[MVP]",              350,    Material.GOLD_NUGGET,        "§7El más valioso del servidor."),
    LOCO         ("loco",         "§5[Loco]",             400,    Material.FERMENTED_SPIDER_EYE,"§7Impredecible y peligroso."),
    CLUTCH       ("clutch",       "§c[Clutch]",           400,    Material.TOTEM_OF_UNDYING,   "§7Gana cuando más importa."),
    PRO          ("pro",          "§a[Pro]",              400,    Material.EMERALD,            "§7Un profesional del PvP."),
    ELITE        ("elite",        "§d[Elite]",            500,    Material.AMETHYST_SHARD,     "§7Por encima del promedio."),
    STREAMER     ("streamer",     "§c[Streamer]",         500,    Material.REDSTONE,           "§7Todo el mundo mirando."),
    CAMPER       ("camper",       "§8[Camper]",           600,    Material.OAK_LOG,            "§7No se mueve de un lugar."),

    // Tier 1 — 700–2000 coins
    NOVATO       ("novato",       "§8[Novato]",           700,    Material.STONE_SWORD,        "§7Los inicios de una leyenda."),
    BANDIDO      ("bandido",      "§6[Bandido]",          800,    Material.CROSSBOW,           "§7Nadie está a salvo de él."),
    CAZADOR      ("cazador",      "§a[Cazador]",          1000,   Material.BOW,                "§7Siempre en busca de su presa."),
    PIRATA       ("pirata",       "§e[Pirata]",           1200,   Material.NAUTILUS_SHELL,     "§7Saquea sin remordimientos."),
    LOBO         ("lobo",         "§7[Lobo]",             1500,   Material.BONE,               "§7Caza en manada, muere solo."),
    MERCENARIO   ("mercenario",   "§7[Mercenario]",       1800,   Material.CROSSBOW,           "§7Lucha por el que más paga."),
    VIKINGO      ("vikingo",      "§b[Vikingo]",          2000,   Material.IRON_AXE,           "§7El horror de los mares del norte."),

    // Tier 2 — 3000–6000 coins
    ASESINO      ("asesino",      "§c[Asesino]",          3000,   Material.IRON_SWORD,         "§7Para los que no perdonan."),
    NINJA        ("ninja",        "§8[Ninja]",            3200,   Material.FEATHER,            "§7Rápido, silencioso, letal."),
    FANTASMA     ("fantasma",     "§f[Fantasma]",         3500,   Material.PHANTOM_MEMBRANE,   "§7Te ven pero no te pueden atrapar."),
    SOMBRA       ("sombra",       "§8[Sombra]",           4000,   Material.INK_SAC,            "§7Silencioso y letal."),
    GLADIADOR    ("gladiador",    "§6[Gladiador]",        4200,   Material.SHIELD,             "§7Combate en la arena, gana siempre."),
    BARBARO      ("barbaro",      "§6[Bárbaro]",          4500,   Material.GOLDEN_AXE,         "§7Fuerza bruta sin límites."),
    GUERRERO     ("guerrero",     "§f[Guerrero]",         5000,   Material.IRON_CHESTPLATE,    "§7Nacido para la batalla."),
    CORSARIO     ("corsario",     "§3[Corsario]",         5500,   Material.TRIDENT,            "§7Pirata de los servidores."),
    DRAGON       ("dragon",       "§4[Dragón]",           6000,   Material.DRAGON_BREATH,      "§7Fuego en sus venas."),

    // Tier 3 — 7000–12000 coins
    TITAN        ("titan",        "§a[Titán]",            7000,   Material.DIAMOND_CHESTPLATE, "§7Imparable."),
    SAMURAI      ("samurai",      "§e[Samurai]",          7500,   Material.GOLDEN_SWORD,       "§7Honor y filo en cada golpe."),
    VERDUGO      ("verdugo",      "§c[Verdugo]",          8000,   Material.IRON_AXE,           "§7Ejecutor sin piedad."),
    CONQUISTADOR ("conquistador", "§5[Conquistador]",     8500,   Material.NETHERITE_SWORD,    "§7Conquista todo a su paso."),
    DEMONIO      ("demonio",      "§4[Demonio]",          9000,   Material.BLAZE_POWDER,       "§7Directo del infierno."),
    EXTERMINADOR ("exterminador", "§c[Exterminador]",     9500,   Material.TNT,                "§7Sin supervivientes."),
    REY          ("rey",          "§e[Rey]",              10000,  Material.GOLDEN_HELMET,      "§7El que manda en el servidor."),
    MAESTRO      ("maestro",      "§a[Maestro]",          10500,  Material.ENCHANTED_BOOK,     "§7Domina cada aspecto del juego."),
    CAMPEON      ("campeon",      "§6[Campeón]",          11000,  Material.GOLDEN_APPLE,       "§7El número uno."),
    INMORTAL     ("inmortal",     "§d[Inmortal]",         12000,  Material.TOTEM_OF_UNDYING,   "§7No puede ser derrotado."),

    // Tier 4 — 15000–40000 coins
    LEYENDA      ("leyenda",      "§6[Leyenda]",          15000,  Material.GOLD_INGOT,         "§7Solo los mejores lo llevan."),
    IMBATIBLE    ("imbatible",    "§d[Imbatible]",        16000,  Material.NETHERITE_CHESTPLATE,"§7Nadie lo ha vencido aún."),
    DIOS         ("dios",         "§5[Dios]",             18000,  Material.END_CRYSTAL,        "§7Por encima de todos."),
    ETERNO       ("eterno",       "§b[Eterno]",           20000,  Material.NETHER_STAR,        "§7Existe desde siempre, existirá para siempre."),
    CAOS         ("caos",         "§c§l[Caos]",           22000,  Material.FIRE_CHARGE,        "§7El desorden hecho persona."),
    OLIMPICO     ("olimpico",     "§e§l[Olímpico]",       25000,  Material.BEACON,             "§7Nivel de los dioses."),
    ABSOLUTO     ("absoluto",     "§c§l[Absoluto]",       28000,  Material.CONDUIT,            "§7Control total. Sin excepciones."),
    SUPREMO      ("supremo",      "§d§l[Supremo]",        35000,  Material.HEART_OF_THE_SEA,   "§7Nadie por encima."),
    INVENCIBLE   ("invencible",   "§4§l[Invencible]",     40000,  Material.ENCHANTED_GOLDEN_APPLE, "§7Imposible de detener."),

    // Emoji / símbolo — baratos
    TAG_RAYO     ("tag_rayo",     "§e[⚡]",               500,    Material.LIGHTNING_ROD,         "§7El poder del rayo."),
    TAG_STAR     ("tag_star",     "§6[★]",                800,    Material.GOLD_INGOT,            "§7Una estrella entre todos."),
    TAG_HIELO    ("tag_hielo",    "§b[❄]",                1200,   Material.PACKED_ICE,            "§7Frío como el hielo."),
    TAG_SKULL    ("tag_skull",    "§8[☠]",                2000,   Material.WITHER_SKELETON_SKULL, "§7La muerte te sigue."),
    TAG_MUSIC    ("tag_music",    "§d[♪]",                300,    Material.JUKEBOX,               "§7Siempre con la música."),
    TAG_PEACE    ("tag_peace",    "§a[☮]",                150,    Material.WHITE_WOOL,            "§7Paz... hasta cierto punto."),
    TAG_CORAZON  ("tag_corazon",  "§c[♥]",                250,    Material.RED_DYE,               "§7Para los románticos del PvP."),
    TAG_INFINITO ("tag_infinito", "§7[∞]",                600,    Material.ENDER_EYE,             "§7Sin límites."),
    TAG_DELTA    ("tag_delta",    "§e[△]",                400,    Material.PRISMARINE_SHARD,      "§7Siempre en movimiento."),
    TAG_CRUZ     ("tag_cruz",     "§c[✚]",                500,    Material.RED_WOOL,              "§7El médico del campo de batalla."),

    // Emoji / símbolo — medios
    TAG_ESPADAS  ("tag_espadas",  "§c[⚔]",               3000,   Material.IRON_SWORD,            "§7Nacido para el combate."),
    TAG_CORONA   ("tag_corona",   "§e[♛]",               5000,   Material.GOLDEN_HELMET,         "§7La corona es tuya."),
    TAG_KNIGHT   ("tag_knight",   "§7[♞]",               6500,   Material.IRON_CHESTPLATE,       "§7El caballero del servidor."),
    TAG_FLEUR    ("tag_fleur",    "§d[⚜]",               9000,   Material.PURPLE_DYE,            "§7Elegancia y poder."),
    TAG_OMEGA    ("tag_omega",    "§5[Ω]",                4000,   Material.PURPLE_DYE,            "§7El último en quedar en pie."),
    TAG_SIGMA    ("tag_sigma",    "§7[Σ]",                2500,   Material.GRAY_DYE,              "§7Estratega nato."),
    TAG_ALPHA    ("tag_alpha",    "§6[α]",                3500,   Material.ORANGE_DYE,            "§7El líder nato."),
    TAG_BETA     ("tag_beta",     "§3[β]",                1500,   Material.CYAN_DYE,              "§7El segundo, pero no por mucho."),
    TAG_PI       ("tag_pi",       "§b[π]",                2000,   Material.LIGHT_BLUE_DYE,        "§7Matemáticamente OP."),
    TAG_RUNE     ("tag_rune",     "§5[ᚱ]",               5500,   Material.AMETHYST_SHARD,        "§7Poder ancestral."),

    // Emoji / símbolo — caros
    TAG_FIRE     ("tag_fire",     "§c§l[🔥]",            12000,  Material.BLAZE_ROD,             "§7Fuego puro."),
    TAG_DIAMOND  ("tag_diamond",  "§b§l[💎]",            20000,  Material.DIAMOND,               "§7Tan valioso como el diamante."),
    TAG_CROWN    ("tag_crown",    "§6§l[👑]",            30000,  Material.GOLDEN_HELMET,         "§7El rey absoluto."),
    TAG_SKULL2   ("tag_skull2",   "§4§l[💀]",            35000,  Material.NETHERITE_SWORD,       "§7Sembró el terror."),
    TAG_VOID     ("tag_void",     "§0§l[▓]",             15000,  Material.BLACK_CONCRETE,        "§7El vacío te consume."),
    TAG_CAOS2    ("tag_caos2",    "§4§l[⁂]",            18000,  Material.NETHER_BRICK,          "§7Caos absoluto."),
    TAG_ECLIPSE  ("tag_eclipse",  "§8§l[◐]",             22000,  Material.OBSIDIAN,              "§7Entre la luz y la oscuridad."),
    TAG_NEXUS    ("tag_nexus",    "§d§l[⊕]",             28000,  Material.CONDUIT,               "§7Conectado a todo."),
    TAG_VOID2    ("tag_void2",    "§5§l[✦]",             40000,  Material.END_CRYSTAL,           "§7Brilla en la oscuridad."),

    // Tags texto variados — baratos
    RATA2        ("rata2",        "§8[Espia]",            200,    Material.RABBIT_HIDE,           "§7Información es poder."),
    FLANKER      ("flanker",      "§e[Flanker]",          250,    Material.SUGAR_CANE,            "§7Siempre por los costados."),
    RUSHER       ("rusher",       "§c[Rusher]",           300,    Material.SPRUCE_BOAT,           "§7Va al frente sin pensar."),
    SNIPER       ("sniper",       "§7[Sniper]",           400,    Material.BOW,                   "§7Un disparo, una baja."),
    ROGUE        ("rogue",        "§5[Rogue]",            500,    Material.POISONOUS_POTATO,      "§7Impredecible y esquivo."),
    TANQUE       ("tanque",       "§8[Tanque]",           450,    Material.IRON_BLOCK,            "§7No se mueve, no cae."),
    SPEEDRUN     ("speedrun",     "§a[Speedrun]",         350,    Material.FEATHER,               "§7Termina el juego en tiempo récord."),
    CARRY        ("carry",        "§6[Carry]",            600,    Material.GOLDEN_APPLE,          "§7Carga al equipo solo."),
    SUICIDA      ("suicida",      "§c[Suicida]",          200,    Material.TNT,                   "§7Muere rápido, muere seguido."),
    VETERANO     ("veterano",     "§7[Veterano]",         700,    Material.IRON_HELMET,           "§7Vio cosas que no se pueden olvidar."),

    // Tags texto variados — medios
    INFILTRADO   ("infiltrado",   "§8[Infiltrado]",       1500,   Material.LEATHER_CHESTPLATE,    "§7Adentro del enemigo sin que lo sepan."),
    ESTRATEGA    ("estratega",    "§b[Estratega]",        2000,   Material.MAP,                   "§7Cada movimiento, calculado."),
    PREDADOR     ("predador",     "§4[Predador]",         2500,   Material.NETHERITE_SWORD,       "§7En lo más alto de la cadena."),
    GUARDIAN     ("guardian",     "§3[Guardian]",         3000,   Material.SHIELD,                "§7Protege lo que importa."),
    BERSERKER    ("berserker",    "§c[Berserker]",        3500,   Material.GOLDEN_AXE,            "§7Cuando se activa, no hay quien lo pare."),
    TEMPLAR      ("templar",      "§f[Templar]",          4000,   Material.DIAMOND_SWORD,         "§7Fe y acero."),
    ESPECTRO     ("espectro",     "§7[Espectro]",         5000,   Material.PHANTOM_MEMBRANE,      "§7Ni vivo ni muerto."),
    WARLORD      ("warlord",      "§c[Warlord]",          6000,   Material.NETHERITE_AXE,         "§7Señor de la guerra."),
    CAZARECOM    ("cazarecomp",   "§6[Cazarrecomp.]",     7000,   Material.CROSSBOW,              "§7Siempre en busca del objetivo."),
    GOAT         ("goat",         "§a[GOAT]",             8000,   Material.GOAT_HORN,             "§7Greatest Of All Time."),

    // Tags texto variados — caros
    REAPER       ("reaper",       "§8[Reaper]",           10000,  Material.WITHER_SKELETON_SKULL, "§7La muerte con nombre propio."),
    PHANTOM2     ("phantom2",     "§f[Phantom]",          11000,  Material.PHANTOM_MEMBRANE,      "§7Aparece cuando menos lo esperás."),
    OVERLORD     ("overlord",     "§4[Overlord]",         14000,  Material.NETHERITE_CHESTPLATE,  "§7Dominio total."),
    ASCENDIDO    ("ascendido",    "§e§l[Ascendido]",      17000,  Material.NETHER_STAR,           "§7Superó todos los límites."),
    PRIMORDIAL   ("primordial",   "§5§l[Primordial]",     23000,  Material.ANCIENT_DEBRIS,        "§7Existía antes que todo."),
    TRASCENDENTE ("trascendente", "§d§l[Trascendente]",   32000,  Material.END_CRYSTAL,           "§7Más allá de lo comprensible."),
    NEBULA       ("nebula",       "§9§l[Nebula]",         38000,  Material.LAPIS_LAZULI,          "§7Nacido entre estrellas.");

    private final String id;
    private final String display;
    private final int price;
    private final Material icon;
    private final String description;

    ChatTag(String id, String display, int price, Material icon, String description) {
        this.id = id;
        this.display = display;
        this.price = price;
        this.icon = icon;
        this.description = description;
    }

    public boolean isFree() { return this.price == 0; }

    public static ChatTag fromId(String id) {
        for (ChatTag tag : values()) {
            if (tag.id.equalsIgnoreCase(id)) return tag;
        }
        return null;
    }
}
