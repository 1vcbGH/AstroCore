package me.vcb.astrocore.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.handlers.leaderboard.cache.FactionCacheHolder;
import me.vcb.astrocore.handlers.leaderboard.cache.PlayerCacheHolder;
import me.vcb.astrocore.handlers.leaderboard.entry.UuidCacheEntry;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.Executors;

/**
 * Lightweight embedded HTTP API exposing the leaderboards as JSON, so a website
 * can fetch them. No external dependencies (uses com.sun.net.httpserver).
 *
 * Endpoints (GET):
 *   /api/status
 *   /api/leaderboards                 → everything (players + factions)
 *   /api/leaderboards/players         → all player boards
 *   /api/leaderboards/players/{type}  → kills|deaths|balance|killstreak|elo|koths|bounties|kd
 *   /api/leaderboards/factions        → all faction boards
 *   /api/leaderboards/factions/{type} → kills|points|balance|koths
 *
 * Optional ?limit=N (default = max-entries). Optional API key via ?key= or the
 * X-API-Key header (when configured in api.yml).
 */
public class WebApiManager {

    private static WebApiManager instance;

    private HttpServer server;
    private org.bukkit.scheduler.BukkitTask exportTask;
    private boolean enabled;
    private String mode = "file";      // "file" | "http" | "push"
    private int port = 24466;
    private String apiKey = "";
    private int maxEntries = 100;
    private int exportSeconds = 30;
    private File exportDir;
    private String pushUrl = "";
    private java.net.http.HttpClient httpClient;

    public WebApiManager() {
        instance = this;
        loadConfig();
        if (!this.enabled) return;
        switch (this.mode.toLowerCase()) {
            case "http" -> start();
            case "push" -> startPush();
            default -> startFileExport();
        }
    }

    public static WebApiManager getInstance() { return instance; }

    private void loadConfig() {
        File file = new File(AstroCore.getInstance().getDataFolder(), "api.yml");
        if (!file.exists()) {
            YamlConfiguration y = new YamlConfiguration();
            y.set("enabled", false);
            y.set("mode", "push");
            y.set("push-url", "http://TU_VPS_IP:PUERTO/ingest");
            y.set("export-seconds", 30);
            y.set("port", 24466);
            y.set("api-key", "cambiame");
            y.set("max-entries", 100);
            try { y.save(file); } catch (IOException ignored) {}
        }
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        this.enabled = y.getBoolean("enabled", false);
        this.mode = y.getString("mode", "push");
        this.pushUrl = y.getString("push-url", "");
        this.exportSeconds = Math.max(5, y.getInt("export-seconds", 30));
        this.port = y.getInt("port", 24466);
        this.apiKey = y.getString("api-key", "");
        this.maxEntries = y.getInt("max-entries", 100);
        this.exportDir = new File(AstroCore.getInstance().getDataFolder(), "api");
    }

    // ── Push mode  ──────────────────────────

    private void startPush() {
        if (this.pushUrl == null || this.pushUrl.isEmpty() || this.pushUrl.contains("TU_VPS_IP")) {
            AstroCore.getInstance().log("&cWeb API push: configurá &fpush-url &cen api.yml.");
            return;
        }
        this.httpClient = java.net.http.HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10)).build();
        long ticks = (long) this.exportSeconds * 20L;
        this.exportTask = me.vcb.astrocore.utils.Tasks.asyncTimer(this::pushData, 100L, ticks);
        AstroCore.getInstance().log("- &7Web API push every &a" + this.exportSeconds + "s &7→ &f" + this.pushUrl);
    }

    private void pushData() {
        try {
            String json = AstroCore.getInstance().getGson().toJson(leaderboards("", this.maxEntries));
            java.net.http.HttpRequest.Builder req = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(this.pushUrl))
                .timeout(java.time.Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
            if (this.apiKey != null && !this.apiKey.isEmpty()) req.header("X-API-Key", this.apiKey);
            java.net.http.HttpResponse<Void> resp = this.httpClient.send(
                req.build(), java.net.http.HttpResponse.BodyHandlers.discarding());
            if (resp.statusCode() / 100 != 2)
                AstroCore.getInstance().log("&cWeb API push: la VPS respondió HTTP " + resp.statusCode()
                    + " (¿api-key distinta?).");
        } catch (Throwable t) {
            AstroCore.getInstance().log("&cWeb API push falló: " + t.getClass().getSimpleName()
                + ": " + t.getMessage());
        }
    }

    // ── File export mode ─────────────────────────────────────

    private void startFileExport() {
        if (!this.exportDir.exists()) this.exportDir.mkdirs();
        long ticks = (long) this.exportSeconds * 20L;
        this.exportTask = me.vcb.astrocore.utils.Tasks.asyncTimer(this::writeFiles, 100L, ticks);
        AstroCore.getInstance().log("- &7Web API export every &a" + this.exportSeconds + "s &7→ &fplugins/AstroCore/api/");
    }

    private void writeFiles() {
        try {
            write("leaderboards.json", leaderboards("", this.maxEntries));
            write("players.json", leaderboards("/players", this.maxEntries));
            write("factions.json", leaderboards("/factions", this.maxEntries));
        } catch (Throwable ignored) {}
    }

    private void write(String name, Object body) {
        try {
            String json = AstroCore.getInstance().getGson().toJson(body);
            java.nio.file.Files.writeString(new File(this.exportDir, name).toPath(), json,
                StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    public void start() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(this.port), 0);
            this.server.createContext("/api", this::handle);
            this.server.setExecutor(Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "AstroCore-WebAPI");
                t.setDaemon(true);
                return t;
            }));
            this.server.start();
            AstroCore.getInstance().log("- &7Web API on port &a" + this.port + "&7.");
        } catch (IOException e) {
            AstroCore.getInstance().log("&cFailed to start Web API: " + e.getMessage());
        }
    }

    public void disable() {
        if (this.server != null) this.server.stop(0);
        if (this.exportTask != null) this.exportTask.cancel();
    }

    // ── Request handling ──────────────────────────────────────────────────────

    private void handle(HttpExchange ex) throws IOException {
        try {
            // CORS
            ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");

            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) { send(ex, 405, err("Method not allowed")); return; }

            if (!checkKey(ex)) { send(ex, 401, err("Invalid API key")); return; }

            String path = ex.getRequestURI().getPath();
            int limit = parseLimit(ex.getRequestURI().getQuery());

            Object body;
            if (path.equals("/api") || path.equals("/api/") || path.equals("/api/status")) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("status", "ok");
                m.put("plugin", "AstroCore");
                m.put("online", org.bukkit.Bukkit.getOnlinePlayers().size());
                m.put("maxPlayers", org.bukkit.Bukkit.getMaxPlayers());
                body = m;
            } else if (path.startsWith("/api/leaderboards")) {
                body = leaderboards(path.substring("/api/leaderboards".length()), limit);
                if (body == null) { send(ex, 404, err("Unknown leaderboard")); return; }
            } else {
                send(ex, 404, err("Not found"));
                return;
            }

            send(ex, 200, AstroCore.getInstance().getGson().toJson(body));
        } catch (Throwable t) {
            send(ex, 500, err("Internal error"));
        }
    }

    private boolean checkKey(HttpExchange ex) {
        if (this.apiKey == null || this.apiKey.isEmpty()) return true;
        String header = ex.getRequestHeaders().getFirst("X-API-Key");
        String query = queryParam(ex.getRequestURI().getQuery(), "key");
        return this.apiKey.equals(header) || this.apiKey.equals(query);
    }

    private Object leaderboards(String sub, int limit) {
        PlayerCacheHolder p = AstroCore.getInstance().getLeaderboardHandler().getPlayerCacheHolder();
        FactionCacheHolder f = AstroCore.getInstance().getLeaderboardHandler().getFactionCacheHolder();

        if (sub.isEmpty() || sub.equals("/")) {
            Map<String, Object> all = new LinkedHashMap<>();
            all.put("players", allPlayer(p, limit));
            all.put("factions", allFaction(f, limit));
            return all;
        }
        if (sub.equals("/players")) return allPlayer(p, limit);
        if (sub.equals("/factions")) return allFaction(f, limit);
        if (sub.startsWith("/players/")) {
            NavigableSet<UuidCacheEntry<Integer>> set = playerSet(p, sub.substring("/players/".length()));
            return set == null ? null : toList(set, limit);
        }
        if (sub.startsWith("/factions/")) {
            NavigableSet<UuidCacheEntry<Integer>> set = factionSet(f, sub.substring("/factions/".length()));
            return set == null ? null : toList(set, limit);
        }
        return null;
    }

    private Map<String, Object> allPlayer(PlayerCacheHolder p, int limit) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("kills", toList(p.getTopKills(), limit));
        m.put("deaths", toList(p.getTopDeaths(), limit));
        m.put("balance", toList(p.getTopBalance(), limit));
        m.put("killstreak", toList(p.getTopHighestKillstreak(), limit));
        m.put("elo", toList(p.getTopElo(), limit));
        m.put("koths", toList(p.getTopKothsCapped(), limit));
        m.put("bounties", toList(p.getTopBountiesCollected(), limit));
        m.put("kd", toList(p.getTopKD(), limit));
        return m;
    }

    private Map<String, Object> allFaction(FactionCacheHolder f, int limit) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("kills", toList(f.getTopKills(), limit));
        m.put("points", toList(f.getTopPoints(), limit));
        m.put("balance", toList(f.getTopBalance(), limit));
        m.put("koths", toList(f.getTopKothsCapped(), limit));
        return m;
    }

    private NavigableSet<UuidCacheEntry<Integer>> playerSet(PlayerCacheHolder p, String type) {
        return switch (type.toLowerCase()) {
            case "kills" -> p.getTopKills();
            case "deaths" -> p.getTopDeaths();
            case "balance" -> p.getTopBalance();
            case "killstreak", "highestkillstreak" -> p.getTopHighestKillstreak();
            case "elo" -> p.getTopElo();
            case "koths" -> p.getTopKothsCapped();
            case "bounties" -> p.getTopBountiesCollected();
            case "kd" -> p.getTopKD();
            default -> null;
        };
    }

    private NavigableSet<UuidCacheEntry<Integer>> factionSet(FactionCacheHolder f, String type) {
        return switch (type.toLowerCase()) {
            case "kills" -> f.getTopKills();
            case "points" -> f.getTopPoints();
            case "balance" -> f.getTopBalance();
            case "koths" -> f.getTopKothsCapped();
            default -> null;
        };
    }

    private List<Map<String, Object>> toList(NavigableSet<UuidCacheEntry<Integer>> set, int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (set == null) return list;
        int rank = 1;
        for (UuidCacheEntry<Integer> e : set) {
            if (rank > limit) break;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rank", rank++);
            m.put("uuid", String.valueOf(e.getKey()));
            m.put("name", e.getName());
            m.put("value", e.getValue());
            list.add(m);
        }
        return list;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private int parseLimit(String query) {
        String v = queryParam(query, "limit");
        if (v == null) return this.maxEntries;
        try { return Math.max(1, Math.min(this.maxEntries, Integer.parseInt(v))); }
        catch (NumberFormatException e) { return this.maxEntries; }
    }

    private String queryParam(String query, String key) {
        if (query == null) return null;
        for (String part : query.split("&")) {
            int eq = part.indexOf('=');
            if (eq > 0 && part.substring(0, eq).equals(key)) return part.substring(eq + 1);
        }
        return null;
    }

    private String err(String msg) {
        return "{\"error\":\"" + msg.replace("\"", "'") + "\"}";
    }

    private void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
}
