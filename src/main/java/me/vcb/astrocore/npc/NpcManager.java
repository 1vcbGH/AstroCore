package me.vcb.astrocore.npc;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NpcManager {

    private static final double RENDER_DISTANCE_SQ = 64 * 64;
    private static final double LOOK_DISTANCE_SQ   = 10 * 10;

    private static NpcManager instance;
    public static NpcManager getInstance() { return instance; }

    private final File file;
    private final Map<String, NpcData>   dataMap    = new HashMap<>();
    private final Map<String, NpcEntity> entityMap  = new HashMap<>();

    private BukkitTask renderTask;
    private BukkitTask lookTask;

    public NpcManager() {
        instance = this;
        this.file = new File(AstroCore.getInstance().getDataFolder(), "npcs.yml");
        NpcEntity.initNms();
        load();
        startTasks();
        Bukkit.getPluginManager().registerEvents(new NpcListener(), AstroCore.getInstance());
        new NpcEditorGui();
    }

    // ── Tasks ─────────────────────────────────────────────────────────────────

    private void startTasks() {
        // Render: show/hide NPC based on distance — every second
        this.renderTask = Tasks.syncTimer(this::tickRender, 20L, 20L);
        // Look: rotate head toward nearest player — every 2 ticks
        this.lookTask   = Tasks.syncTimer(this::tickLook,   2L,  2L);
    }

    private void tickRender() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (NpcEntity npc : entityMap.values()) {
                if (npc.getData().getLocation() == null) continue;
                if (!npc.getData().getLocation().getWorld().equals(p.getWorld())) {
                    if (npc.isViewing(p)) npc.despawnFor(p);
                    continue;
                }
                double dist = npc.getData().getLocation().distanceSquared(p.getLocation());
                if (dist <= RENDER_DISTANCE_SQ) {
                    if (!npc.isViewing(p)) npc.spawnFor(p);
                    // Respawn hologram if dead
                    NpcHologram holo = npc.getHologram();
                    if (holo != null && !holo.isAlive()) {
                        holo.spawn();
                        holo.showTo(p);
                    }
                } else if (npc.isViewing(p)) {
                    npc.despawnFor(p);
                }
            }
        }
    }

    private void tickLook() {
        for (NpcEntity npc : entityMap.values()) {
            if (npc.getData().getLocation() == null) continue;
            // Find closest player within look distance
            Player closest = null;
            double minDist = LOOK_DISTANCE_SQ;
            for (UUID uid : npc.getViewers()) {
                Player p = Bukkit.getPlayer(uid);
                if (p == null) continue;
                double d = npc.getData().getLocation().distanceSquared(p.getLocation());
                if (d < minDist) { minDist = d; closest = p; }
            }
            if (closest == null) continue;
            // Send look packet to all viewers
            for (UUID uid : npc.getViewers()) {
                Player viewer = Bukkit.getPlayer(uid);
                if (viewer != null) npc.lookAt(viewer, closest.getEyeLocation());
            }
        }
    }

    public void disable() {
        save();
        if (renderTask != null) renderTask.cancel();
        if (lookTask   != null) lookTask.cancel();
        entityMap.values().forEach(NpcEntity::despawnForAll);
        entityMap.values().forEach(e -> { if (e.getHologram() != null) e.getHologram().destroy(); });
        entityMap.clear();
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public NpcData createNpc(String id) {
        NpcData data = new NpcData(id);
        dataMap.put(id, data);
        NpcEntity entity = new NpcEntity(data);
        entityMap.put(id, entity);
        save();
        return data;
    }

    public boolean deleteNpc(String id) {
        NpcData data = dataMap.remove(id);
        if (data == null) return false;
        NpcEntity entity = entityMap.remove(id);
        if (entity != null) {
            entity.despawnForAll();
            if (entity.getHologram() != null) entity.getHologram().destroy();
        }
        save();
        return true;
    }

    public NpcData getNpc(String id) { return dataMap.get(id); }
    public NpcEntity getEntity(String id) { return entityMap.get(id); }
    public Collection<NpcData> getAllNpcs() { return Collections.unmodifiableCollection(dataMap.values()); }

    /** Find NPC entity by its packet entity ID. */
    public NpcEntity getByEntityId(int entityId) {
        for (NpcEntity e : entityMap.values())
            if (e.getEntityId() == entityId) return e;
        return null;
    }

    /** Respawn NPC for all nearby players (after location/skin change). */
    public void respawn(String id) {
        NpcEntity entity = entityMap.get(id);
        if (entity == null) return;
        entity.despawnForAll();
        if (entity.getHologram() != null) entity.getHologram().destroy();

        NpcHologram holo = new NpcHologram(entity.getData());
        holo.spawn();
        entity.setHologram(holo);

        // Reshow to all nearby players immediately
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (entity.getData().getLocation() == null) continue;
            if (!entity.getData().getLocation().getWorld().equals(p.getWorld())) continue;
            if (entity.getData().getLocation().distanceSquared(p.getLocation()) <= RENDER_DISTANCE_SQ) {
                entity.spawnFor(p);
            }
        }
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (NpcData d : dataMap.values())
            d.save(yaml.createSection("npcs." + d.getId()));
        try { yaml.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        var section = yaml.getConfigurationSection("npcs");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            var s = section.getConfigurationSection(id);
            if (s == null) continue;
            NpcData data = NpcData.load(id, s);
            dataMap.put(id, data);

            NpcEntity entity = new NpcEntity(data);
            NpcHologram holo = new NpcHologram(data);
            holo.spawn();
            entity.setHologram(holo);
            entityMap.put(id, entity);
        }
    }

    // ── Skin fetching ─────────────────────────────────────────────────────────

    /** Async fetch skin from Mojang and respawn the NPC. */
    public void fetchSkinAsync(String npcId, String playerName) {
        Tasks.async(() -> {
            try {
                // ── Step 1: Resolve UUID from player name ────────────────────────
                java.net.HttpURLConnection uuidConn = (java.net.HttpURLConnection)
                    new java.net.URL("https://api.mojang.com/users/profiles/minecraft/" + playerName).openConnection();
                uuidConn.setConnectTimeout(5000);
                uuidConn.setReadTimeout(5000);
                int uuidCode = uuidConn.getResponseCode();
                if (uuidCode == 204 || uuidCode == 404) {
                    AstroCore.getInstance().log("&c[NPC] Skin fetch: jugador &f" + playerName + " &cno existe (HTTP " + uuidCode + ")");
                    return;
                }
                if (uuidCode != 200) {
                    AstroCore.getInstance().log("&c[NPC] Skin fetch: Mojang UUID API devolvió HTTP " + uuidCode);
                    return;
                }
                String uuidJson;
                try (java.io.InputStream is = uuidConn.getInputStream()) {
                    uuidJson = new String(is.readAllBytes());
                }

                java.util.regex.Matcher uuidM = java.util.regex.Pattern
                    .compile("\"id\"\\s*:\\s*\"([0-9a-fA-F]{32})\"").matcher(uuidJson);
                if (!uuidM.find()) {
                    AstroCore.getInstance().log("&c[NPC] Skin fetch: no se pudo parsear UUID. JSON: " + uuidJson);
                    return;
                }
                String rawUuid = uuidM.group(1);
                String formattedUuid = rawUuid.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");

                // ── Step 2: Fetch skin texture properties ────────────────────────
                java.net.HttpURLConnection profConn = (java.net.HttpURLConnection)
                    new java.net.URL("https://sessionserver.mojang.com/session/minecraft/profile/"
                            + formattedUuid + "?unsigned=false").openConnection();
                profConn.setConnectTimeout(5000);
                profConn.setReadTimeout(5000);
                int profCode = profConn.getResponseCode();
                if (profCode != 200) {
                    AstroCore.getInstance().log("&c[NPC] Skin fetch: sessionserver devolvió HTTP " + profCode);
                    return;
                }
                String profileJson;
                try (java.io.InputStream is = profConn.getInputStream()) {
                    profileJson = new String(is.readAllBytes());
                }

                java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("\"value\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"signature\"\\s*:\\s*\"([^\"]+)\"")
                    .matcher(profileJson);
                if (!m.find()) {
                    AstroCore.getInstance().log("&c[NPC] Skin fetch: textura/firma no encontrada en sessionserver.");
                    return;
                }

                String texture   = m.group(1);
                String signature = m.group(2);

                Tasks.sync(() -> {
                    NpcData data = getNpc(npcId);
                    if (data == null) {
                        AstroCore.getInstance().log("&c[NPC] Skin fetch: NPC &f" + npcId + " &cya no existe.");
                        return;
                    }
                    data.setSkinName(playerName);
                    data.setSkinTexture(texture);
                    data.setSkinSignature(signature);
                    save();
                    respawn(npcId);
                    AstroCore.getInstance().log("- &a[NPC] Skin de &f" + playerName + " &aaplicada a &f" + npcId);
                });
            } catch (Exception e) {
                AstroCore.getInstance().log("&c[NPC] Skin fetch error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
