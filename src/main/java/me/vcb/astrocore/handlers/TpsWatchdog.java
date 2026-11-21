package me.vcb.astrocore.handlers;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Monitorea el TPS cada 30 segundos.
 * Si cae de TPS_THRESHOLD → notifica al staff en juego.
 * Si hay webhook configurado → envía mensaje a Discord.
 * Cooldown de 2 min para no spamear.
 */
public class TpsWatchdog {

    private static final double TPS_THRESHOLD   = 15.0;
    private static final long   CHECK_INTERVAL  = 600L;  // 30 s (20 ticks/s)
    private static final long   NOTIFY_COOLDOWN = 120_000L;

    private long lastNotify = 0;
    private BukkitTask task;

    public TpsWatchdog() {
        start();
    }

    public void start() {
        task = Tasks.syncTimer(() -> {
            double tps = getTps();
            if (tps >= TPS_THRESHOLD) return;

            long now = System.currentTimeMillis();
            if (now - lastNotify < NOTIFY_COOLDOWN) return;
            lastNotify = now;

            String tpsFormatted = String.format("%.1f", tps);
            String msg = Color.translate("&c&l[TPS] &cEl servidor está laggeando: &e" + tpsFormatted + " TPS &c(umbral: &e" + TPS_THRESHOLD + "&c)");

            // Notificar a staff en juego
            Messages.sendMessage(msg, "astrocore.staff");

            // Enviar a Discord webhook (async)
            String webhookUrl = AstroCore.getInstance().getConfig().getString("TPS_WATCHDOG_WEBHOOK", "");
            if (!webhookUrl.isEmpty()) {
                Tasks.async(() -> sendDiscordWebhook(webhookUrl, tpsFormatted));
            }

        }, 600L, CHECK_INTERVAL);
    }

    public void cancel() {
        if (task != null) task.cancel();
    }

    private double getTps() {
        try {
            double[] tps = Bukkit.getServer().getTPS();
            return tps.length > 0 ? tps[0] : 20.0;
        } catch (Exception e) {
            return 20.0;
        }
    }

    private void sendDiscordWebhook(String webhookUrl, String tps) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int online = Bukkit.getOnlinePlayers().size();
            String serverName = Bukkit.getServer().getName();
            String json = "{\"content\": null, \"embeds\": [{\"title\": \"⚠️ TPS Bajo\", "
                + "\"description\": \"El servidor **" + serverName + "** está laggeando.\\n"
                + "**TPS:** `" + tps + "` (umbral: `" + TPS_THRESHOLD + "`)\\n"
                + "**Jugadores online:** `" + online + "`\", "
                + "\"color\": 16711680}]}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            conn.getResponseCode(); // trigger send
            conn.disconnect();
        } catch (Exception e) {
            AstroCore.getInstance().log("&c[TpsWatchdog] Error enviando webhook: " + e.getMessage());
        }
    }
}
