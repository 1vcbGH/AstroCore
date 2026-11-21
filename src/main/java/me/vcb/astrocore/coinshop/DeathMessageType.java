package me.vcb.astrocore.coinshop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

/**
 * Purchasable death message cosmetics.
 *
 * Placeholders:
 *   <killer>  — name of the killer
 *   <victim>  — name of the victim
 *   <weapon>  — weapon used
 */
@Getter
@RequiredArgsConstructor
public enum DeathMessageType {

    // ── Gratis ────────────────────────────────────────────────────────────
    DEFAULT      ("Estándar",      "§7<victim> §ffue eliminado por §e<killer>§f.",
                                   "§7El clásico.", 0, Material.PAPER),

    // ── 300–600 coins ─────────────────────────────────────────────────────
    COMICO       ("Cómico",        "§e<victim> §fintentó pelear con §e<killer>§f... y falló xD",
                                   "§7Mensaje divertido.", 300, Material.SLIME_BALL),

    SENCILLO     ("Sencillo",      "§7<killer> §f: §c1  §7— §f<victim> §7: §c0",
                                   "§7Marcador simple.", 300, Material.PAPER),

    NOOB         ("Noob",          "§e<victim> §fse fue al lobby a llorar. Gracias §e<killer>§f.",
                                   "§7Para el que siempre pierde.", 350, Material.WOODEN_SWORD),

    COTIDIANO    ("Cotidiano",      "§7<killer> §festaba aburrido así que mató a §e<victim>§f.",
                                   "§7Casual y sin remordimientos.", 400, Material.GRAY_DYE),

    TIKTOK       ("TikTok",        "§d<killer> §fhizo un combo viral con §e<victim>§f. POV: te mataron.",
                                   "§7Para los content creators.", 400, Material.PINK_DYE),

    ESCUELA      ("Escuela",       "§e<killer> §fle dio clases gratis a §e<victim>§f. Reprobó.",
                                   "§7Educativo.", 450, Material.BOOK),

    GUERRERO     ("Guerrero",      "§4⚔ §c<killer> §4derrotó a §c<victim> §4en combate. §4⚔",
                                   "§7Mensaje de guerra.", 500, Material.IRON_SWORD),

    BRUTAL       ("Brutal",        "§c☠ §e<killer> §cdestruyó a §4<victim> §ccon §e<weapon>§c! ☠",
                                   "§7Agresivo con calavera.", 500, Material.BONE),

    // ── 600–1200 coins ────────────────────────────────────────────────────
    ELEGANTE     ("Elegante",      "§6✦ §e<killer> §6eliminó a §e<victim> §6con maestría. §6✦",
                                   "§7Dorado y elegante.", 600, Material.GOLD_INGOT),

    CAZADOR      ("Cazador",       "§a<killer> §fcazó a §e<victim> §fcomo si fuera práctica.",
                                   "§7El depredador y su presa.", 600, Material.BOW),

    MISTERIOSO   ("Misterioso",    "§5✦ §d<victim> §5desapareció... solo §d<killer> §5sabe la verdad. §5✦",
                                   "§7Críptico y oscuro.", 700, Material.ENDER_EYE),

    ROYALE       ("Battle Royale", "§6§l#<killer> §r§7eliminó a §f<victim>§7. Quedan §c? §7jugadores.",
                                   "§7Estilo Battle Royale.", 750, Material.FIREWORK_ROCKET),

    FANTASMA     ("Fantasma",      "§f<victim> §7no supo de dónde vino §f<killer>§7. Ni lo vio.",
                                   "§7Para los sigilosos.", 750, Material.FEATHER),

    RESPETO      ("Respeto",       "§7F en el chat por §e<victim>§7. §e<killer> §7gana el round.",
                                   "§7Dale respeto al caído.", 800, Material.WHITE_TULIP),

    CINEMATIC    ("Cinemático",    "§8[§7En cámara lenta§8] §c<killer> §7acaba con §e<victim>§7...",
                                   "§7Épico efecto cinematográfico.", 900, Material.SPYGLASS),

    // ── 1000–2000 coins ───────────────────────────────────────────────────
    VIKINGO      ("Vikingo",        "§9⚡ §b<killer> §9mandó a §b<victim> §9al Valhalla! §9⚡",
                                   "§7Nórdico épico.", 1000, Material.TRIDENT),

    ANIME        ("Anime",          "§d<killer>§f: '§d<weapon>§f... ¡FINAL SMASH!' §e<victim> §fha sido eliminado.",
                                   "§7Para los otakus del servidor.", 1000, Material.BLAZE_ROD),

    REPORT       ("Report",         "§c<victim> §facaba de reportar a §e<killer>§f. Le banearon a §e<victim>§f.",
                                   "§7Irónico.", 1000, Material.WRITABLE_BOOK),

    TUTORIAL     ("Tutorial",       "§a§l[TUTORIAL] §r§e<victim>§7: murió. §e<killer> §7completó el objetivo.",
                                   "§7Como si fuera una misión.", 1100, Material.MAP),

    OLIMPICO     ("Olímpico",       "§c§l[OLYMPUS] §r§e<killer> §7envió a §e<victim> §7al inframundo!",
                                   "§7Épico al estilo Olympus.", 1200, Material.NETHER_STAR),

    COMENTARISTA ("Comentarista",   "§e<killer> §f¡INCREÍBLE JUGADA! §e<victim> §fno lo pudo creer. ¡GOL!",
                                   "§7Modo narrador deportivo.", 1200, Material.MUSIC_DISC_CAT),

    // ── 1500–3000 coins ───────────────────────────────────────────────────
    HACKER       ("Hacker",         "§a<killer>§f: [Kill confirmed] §e<victim> §fterminado. Ping: §a2ms",
                                   "§7Para los que parecen hacker.", 1500, Material.COMMAND_BLOCK),

    TEATRO       ("Teatro",         "§5§o<victim> §5§o: '¿Por qué... §e§o<killer>§5§o...?' *cae dramáticamente*",
                                   "§7Drama puro.", 1500, Material.RED_DYE),

    HISTORIA     ("Histórico",      "§6§l⚔ §r§7Año 0 — §e<killer> §7escribió su nombre en la historia eliminando a §e<victim>§7.",
                                   "§7Como una epopeya.", 1800, Material.WRITTEN_BOOK),

    SISTEMA      ("Sistema",        "§c[KERNEL] §7Process §e<victim> §7terminated by §e<killer>§7. Exit code: §c0x1",
                                   "§7Estilo sistema operativo.", 2000, Material.COMMAND_BLOCK),

    BOSS         ("Boss Kill",      "§4§l☠ §c§l<killer> §4§lHA ELIMINADO AL BOSS §c§l<victim>§4§l! §c§l+500 EXP ☠",
                                   "§7Como matar un boss de raid.", 2000, Material.DRAGON_EGG),

    DIOS         ("Divino",         "§5§l✦ §d§l<killer> §5§ldesciende y borra a §d§l<victim> §5§ldel mapa. §5§l✦",
                                   "§7Omnipotente.", 2500, Material.END_CRYSTAL),

    FINAL        ("Final Boss",     "§4§l█▓▒░ §c§l<killer> §4§lUSÓ §c§l<weapon> §4§l— DAÑO CRÍTICO — §c§l<victim> §4§lDELETED §4§l░▒▓█",
                                   "§7Barra de vida llegó a 0.", 3000, Material.NETHERITE_SWORD);

    private final String displayName;
    private final String format;
    private final String description;
    private final int price;
    private final Material icon;

    public String build(String killer, String victim, String weapon) {
        return this.format
            .replace("<killer>", killer)
            .replace("<victim>", victim)
            .replace("<weapon>", weapon.isEmpty() ? "sus manos" : weapon);
    }

    public boolean isFree() { return this.price == 0; }
}
