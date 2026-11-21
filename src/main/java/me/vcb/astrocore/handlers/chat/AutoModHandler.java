package me.vcb.astrocore.handlers.chat;

import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class AutoModHandler extends Handler implements Listener {

    // ── Spam ──────────────────────────────────────────────────────────────
    private static final int    SPAM_MAX_MESSAGES = 4;
    private static final long   SPAM_WINDOW_MS    = 4_000L;
    private static final long   SPAM_MUTE_MS      = 30_000L;

    // ── CAPS ──────────────────────────────────────────────────────────────
    /** Mínimo de chars para aplicar el filtro de caps */
    private static final int    CAPS_MIN_LENGTH   = 6;
    /** % de letras mayúsculas requerido para considerar el mensaje como caps */
    private static final double CAPS_THRESHOLD    = 0.70;
    private static final long   CAPS_MUTE_MS      = 15_000L;

    // ── Chars repetidos ───────────────────────────────────────────────────
    /** Cuántas veces seguidas el mismo char se considera repetición abusiva */
    private static final int    REPEAT_THRESHOLD  = 5;
    private static final long   REPEAT_MUTE_MS    = 15_000L;

    /** Regex: 5+ veces el mismo carácter seguido (ignore case) */
    private static final Pattern REPEAT_PATTERN =
        Pattern.compile("(.)\\1{" + (REPEAT_THRESHOLD - 1) + ",}", Pattern.CASE_INSENSITIVE);

    // ── Estado ────────────────────────────────────────────────────────────
    private boolean enabled = true;

    private final Map<UUID, Deque<Long>> messageHistory = new ConcurrentHashMap<>();
    private final Map<UUID, Long>        muted          = new ConcurrentHashMap<>();

    @Override
    public void disable() {
        messageHistory.clear();
        muted.clear();
    }

    public void toggle() { enabled = !enabled; }
    public boolean isEnabled() { return enabled; }

    // ── Listener ──────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!enabled) return;

        Player player = event.getPlayer();
        if (player.hasPermission("astrocore.staff")) return;

        UUID uuid   = player.getUniqueId();
        long now    = System.currentTimeMillis();
        String msg  = event.getMessage();

        // 1) ¿Está muteado?
        Long expiry = muted.get(uuid);
        if (expiry != null) {
            if (now < expiry) {
                event.setCancelled(true);
                long remaining = (expiry - now) / 1000L;
                player.sendMessage(Color.translate(
                    "&c[AutoMod] Estás silenciado. Tiempo restante: &e" + remaining + "s&c."));
                return;
            }
            muted.remove(uuid);
        }

        // 2) Filtro de CAPS
        String capsResult = checkCaps(msg);
        if (capsResult != null) {
            event.setCancelled(true);
            muted.put(uuid, now + CAPS_MUTE_MS);
            player.sendMessage(Color.translate(
                "&c[AutoMod] Evita escribir en mayúsculas. Silenciado &e15 segundos&c."));
            notifyStaff(player, "CAPS", msg);
            return;
        }

        // 3) Filtro de chars repetidos
        if (REPEAT_PATTERN.matcher(msg).find()) {
            event.setCancelled(true);
            muted.put(uuid, now + REPEAT_MUTE_MS);
            player.sendMessage(Color.translate(
                "&c[AutoMod] Evita repetir caracteres. Silenciado &e15 segundos&c."));
            notifyStaff(player, "SPAM DE CARACTERES", msg);
            return;
        }

        // 4) Filtro de spam por velocidad
        Deque<Long> history = messageHistory.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        history.addLast(now);
        while (!history.isEmpty() && now - history.peekFirst() > SPAM_WINDOW_MS) history.pollFirst();

        if (history.size() > SPAM_MAX_MESSAGES) {
            event.setCancelled(true);
            muted.put(uuid, now + SPAM_MUTE_MS);
            history.clear();
            player.sendMessage(Color.translate(
                "&c[AutoMod] Silenciado &e30 segundos &cpor spam."));
            notifyStaff(player, "SPAM", msg);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Retorna mensaje de alerta si el texto supera el umbral de caps, null si está bien.
     * Solo cuenta letras (ignora números y símbolos).
     */
    private String checkCaps(String msg) {
        if (msg.length() < CAPS_MIN_LENGTH) return null;
        long letters = msg.chars().filter(Character::isLetter).count();
        if (letters < CAPS_MIN_LENGTH) return null;
        long uppers  = msg.chars().filter(Character::isUpperCase).count();
        return ((double) uppers / letters) >= CAPS_THRESHOLD ? msg : null;
    }

    /** Notifica a todos los staff online sobre una detección de AutoMod. */
    private void notifyStaff(Player player, String reason, String message) {
        String alert = Color.translate(
            "&8[&cAutoMod&8] &e" + player.getName()
            + " &7silenciado por &c" + reason
            + " &8| &7\"&f" + (message.length() > 40 ? message.substring(0, 40) + "..." : message) + "&7\"");
        Messages.sendMessage(alert, "astrocore.staff");
    }
}
