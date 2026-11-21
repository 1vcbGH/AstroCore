package me.vcb.astrocore.handlers.chat;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MessagingHandler extends Handler implements Listener {

    /** Max mensajes guardados por conversación */
    private static final int MAX_HISTORY = 20;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final Map<UUID, UUID> replies;
    private final Set<UUID> socialSpy;

    /**
     * Historial: clave = UUID menor + "_" + UUID mayor (par ordenado para evitar duplicados)
     * Valor = deque de strings ya formateados para mostrar
     */
    private final Map<String, Deque<String>> msgHistory;

    public MessagingHandler() {
        this.replies    = new HashMap<>();
        this.socialSpy  = new HashSet<>();
        this.msgHistory = new HashMap<>();
    }

    @Override
    public void disable() {
        this.replies.clear();
        this.socialSpy.clear();
        this.msgHistory.clear();
    }

    public void sendMessage(Player sender, Player target, String message) {
        if (!this.isIgnoring(sender, target, message)) {
            this.sendMessageToBoth(sender, target, message);
        }
    }

    public void sendReplyMessage(Player sender, String message) {
        Player target = Bukkit.getPlayer(this.replies.get(sender.getUniqueId()));

        if (target == null) {
            sender.sendMessage(Language.PREFIX + Language.REPLY_NOBODY_TO_REPLY);
            this.replies.remove(sender.getUniqueId());
            return;
        }

        this.sendMessage(sender, target, message);
    }

    private void sendMessageToBoth(Player sender, Player target, String message) {
        this.replies.put(sender.getUniqueId(), target.getUniqueId());
        this.replies.put(target.getUniqueId(), sender.getUniqueId());

        sender.sendMessage(Language.MESSAGE_SEND_FORMAT
            .replace("<rankPrefix>", Color.translate(ChatHandler.getInstance().getPrefix(target)))
            .replace("<player>", target.getName()) + message);

        target.sendMessage(Language.MESSAGE_RECEIVE_FORMAT
            .replace("<rankPrefix>", Color.translate(ChatHandler.getInstance().getPrefix(sender)))
            .replace("<player>", sender.getName()) + message);

        this.socialSpy.stream().map(Bukkit::getPlayer).filter(java.util.Objects::nonNull)
            .forEach(staff -> staff.sendMessage(Language.SOCIAL_SPY_MESSAGE_FORMAT
                .replace("<sender>", sender.getName())
                .replace("<target>", target.getName())
                .replace("<message>", message)));

        // Guardar en historial
        String histKey = histKey(sender.getUniqueId(), target.getUniqueId());
        Deque<String> history = msgHistory.computeIfAbsent(histKey, k -> new ArrayDeque<>());
        String timestamp = TIME_FMT.format(LocalDateTime.now());
        history.addLast(Color.translate("&8[" + timestamp + "] &e" + sender.getName() + " &7→ &e" + target.getName() + ": &f" + message));
        if (history.size() > MAX_HISTORY) history.pollFirst();
    }

    private boolean isIgnoring(Player sender, Player target, String message) {
        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(target);

        if (!data.getSettings().isMessages()) {
            sender.sendMessage(Language.PREFIX + Language.MESSAGE_MESSAGES_DISABLED.replace("<player>", target.getName()));
            return true;
        }

        if (data.isIgnoring(sender)) {
            sender.sendMessage(Language.MESSAGE_SEND_FORMAT
                .replace("<rankPrefix>", Color.translate(ChatHandler.getInstance().getPrefix(target)))
                .replace("<player>", target.getName()) + message);
            return true;
        }

        if (data.getSettings().isSounds()) {
            target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        }

        return false;
    }

    // ── Historial público ─────────────────────────────────────────────────

    /** Devuelve los mensajes de la conversación entre a y b, página 1-based, 10 por página */
    public List<String> getHistory(UUID a, UUID b, int page) {
        String key = histKey(a, b);
        Deque<String> deque = msgHistory.get(key);
        if (deque == null || deque.isEmpty()) return null;

        List<String> all = new ArrayList<>(deque);
        int perPage = 10;
        int totalPages = (int) Math.ceil(all.size() / (double) perPage);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int from = (page - 1) * perPage;
        int to   = Math.min(from + perPage, all.size());
        return all.subList(from, to);
    }

    public int getHistoryPages(UUID a, UUID b) {
        String key = histKey(a, b);
        Deque<String> deque = msgHistory.get(key);
        if (deque == null) return 0;
        return (int) Math.ceil(deque.size() / 10.0);
    }

    private String histKey(UUID a, UUID b) {
        // Orden determinista para que (a,b) y (b,a) usen la misma key
        return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
    }

    // ── SocialSpy ─────────────────────────────────────────────────────────

    public boolean isSocialSpying(Player player) {
        return this.socialSpy.contains(player.getUniqueId());
    }

    public void toggleSocialSpy(Player player) {
        if (this.isSocialSpying(player)) {
            this.socialSpy.remove(player.getUniqueId());
            player.sendMessage(Language.PREFIX + Language.SOCIAL_SPY_DISABLED);
        } else {
            this.socialSpy.add(player.getUniqueId());
            player.sendMessage(Language.PREFIX + Language.SOCIAL_SPY_ENABLED);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.replies.remove(event.getPlayer().getUniqueId());
        this.socialSpy.remove(event.getPlayer().getUniqueId());
    }
}
