package me.vcb.astrocore.coinshop;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum KillStreakMessageType {

    DEFAULT("default", "§6§l[Default]", "§6§l{medal} §r§e<player> §7lleva §e§l<streak> kills §7seguidas y recibió §e<coins> coins§7.", 0, Material.PAPER, "El mensaje de racha por defecto."),
    BRUTAL("brutal", "§c[Brutal]", "§c☠ §e<player> §ccon §c§l<streak> KILLS §cen racha! §e+<coins> coins §c☠", 500, Material.BONE, "Mensaje agresivo y directo."),
    ANIME("anime", "§d[Anime]", "§d§l¡<player> ACTIVÓ SU MODO BESTIA! §r§7<streak> kills — §e+<coins> coins", 800, Material.BLAZE_ROD, "Estilo épico de anime."),
    ROYALE("royale", "§6[Royale]", "§6§l[KILL FEED] §r§e<player> §7— §c<streak> eliminaciones §7— §e+<coins>¢", 1000, Material.FIREWORK_ROCKET, "Estilo Battle Royale."),
    OLIMPICO("olimpico", "§c[Olímpico]", "§c§l[OLYMPUS] §r§e<player> §7está imparable. §c§l<streak> KILLS §7— §e+<coins> coins!", 1500, Material.NETHER_STAR, "Mensaje épico olímpico."),
    SISTEMA("sistema", "§a[Sistema]", "§a[SYS] §7<player>.killstreak = §e<streak>§7; reward += §e<coins>§7;", 2000, Material.COMMAND_BLOCK, "Estilo de línea de código."),
    LEYENDA("leyenda", "§6§l[Leyenda]", "§6§l★ LEYENDA ★ §r§e<player> §7alcanzó §e§l<streak> kills §7consecutivas. §e+<coins> coins.", 3000, Material.GOLD_INGOT, "Solo para los más grandes.");

    private final String id;
    private final String displayName;
    private final String format;
    private final int price;
    private final Material icon;
    private final String description;

    KillStreakMessageType(String id, String displayName, String format, int price, Material icon, String description) {
        this.id = id;
        this.displayName = displayName;
        this.format = format;
        this.price = price;
        this.icon = icon;
        this.description = description;
    }

    public boolean isFree() {
        return this.price == 0;
    }

    public String build(String player, int streak, int coins) {
        String msg = this.format
            .replace("<player>", player)
            .replace("<streak>", String.valueOf(streak))
            .replace("<coins>", String.valueOf(coins));
        if (msg.contains("{medal}")) {
            msg = msg.replace("{medal}", getMedal(streak));
        }
        return msg;
    }

    private String getMedal(int streak) {
        if (streak >= 30) return "★★★";
        if (streak >= 20) return "★★";
        if (streak >= 10) return "★";
        return "•";
    }

    public static KillStreakMessageType fromId(String id) {
        if (id == null) return null;
        for (KillStreakMessageType k : values()) {
            if (k.id.equalsIgnoreCase(id)) return k;
        }
        return null;
    }
}
