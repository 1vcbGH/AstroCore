package me.vcb.astrocore.sanctions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SanctionEntry {

    private final SanctionType type;
    private final String       staff;      // nombre del staff que aplicó
    private final String       reason;
    private final String       duration;   // "Permanente" o "30m", "7d", etc. Vacío si no aplica
    private final long         timestamp;  // System.currentTimeMillis()

    /** Retorna una línea formateada para mostrar en chat */
    public String toDisplayLine(int index) {
        String color = typeColor();
        String time = formatTimestamp();
        String dur  = (duration != null && !duration.isEmpty()) ? " &8[&7" + duration + "&8]" : "";
        String rsn  = (reason  != null && !reason.isEmpty())   ? " &7— &f" + reason            : "";
        return me.vcb.astrocore.utils.Color.translate(
            "  &7#" + index + " " + color + type.name() + dur + rsn
            + " &8| &7Por &e" + staff + " &8| &7" + time);
    }

    private String typeColor() {
        switch (type) {
            case BAN: case TEMPBAN: return "&c";
            case UNBAN:             return "&a";
            case MUTE: case TEMPMUTE: return "&e";
            case UNMUTE:            return "&a";
            case KICK:              return "&6";
            case WARN:              return "&e";
            default:                return "&7";
        }
    }

    private String formatTimestamp() {
        java.time.LocalDateTime dt = java.time.Instant.ofEpochMilli(timestamp)
            .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        return dt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
