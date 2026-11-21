package me.vcb.astrocore.punishments;

/** Duration parsing/formatting helpers for punishments. */
public final class PunishmentUtil {

    private PunishmentUtil() {}

    /**
     * Parses a duration string like "30m", "2h", "7d", "perm".
     * @return millis to add to now, or 0 for permanent, or -1 if invalid.
     */
    public static long parseDuration(String input) {
        if (input == null) return -1;
        input = input.trim().toLowerCase();
        if (input.isEmpty()) return -1;
        if (input.equals("perm") || input.equals("permanent") || input.equals("permanente")
            || input.equals("0")) return 0;

        char unit = input.charAt(input.length() - 1);
        String numPart = input.substring(0, input.length() - 1);
        long value;
        try { value = Long.parseLong(numPart); } catch (NumberFormatException e) { return -1; }
        if (value <= 0) return -1;

        return switch (unit) {
            case 's' -> value * 1000L;
            case 'm' -> value * 60_000L;
            case 'h' -> value * 3_600_000L;
            case 'd' -> value * 86_400_000L;
            case 'w' -> value * 604_800_000L;
            case 'y' -> value * 31_536_000_000L;
            default  -> -1;
        };
    }

    /** Human readable remaining time, e.g. "6d 3h 12m". */
    public static String formatRemaining(long millis) {
        if (millis < 0) return "Permanente";
        if (millis == 0) return "Expirado";

        long seconds = millis / 1000;
        long days  = seconds / 86400; seconds %= 86400;
        long hours = seconds / 3600;  seconds %= 3600;
        long mins  = seconds / 60;    seconds %= 60;

        StringBuilder sb = new StringBuilder();
        if (days  > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (mins  > 0) sb.append(mins).append("m ");
        if (sb.length() == 0) sb.append(seconds).append("s");
        return sb.toString().trim();
    }

    /** Label for storing in history (e.g. "Permanente" or "7d"). */
    public static String durationLabel(long expiresAt, long issuedAt) {
        if (expiresAt <= 0) return "Permanente";
        return formatRemaining(expiresAt - issuedAt);
    }
}
