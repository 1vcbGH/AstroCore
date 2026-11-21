package me.vcb.astrocore.sanctions;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Guarda el historial de sanciones en plugins/AstroCore/sanctions/<uuid>.json
 */
public class SanctionManager {

    private static SanctionManager instance;
    private final File sanctionsDir;

    public SanctionManager() {
        instance = this;
        this.sanctionsDir = new File(AstroCore.getInstance().getDataFolder(), "sanctions");
        if (!this.sanctionsDir.exists()) this.sanctionsDir.mkdirs();
    }

    public static SanctionManager getInstance() { return instance; }

    // ── Guardar ───────────────────────────────────────────────────────────

    public void addSanction(UUID uuid, SanctionEntry entry) {
        List<SanctionEntry> list = load(uuid);
        list.add(0, entry); // más reciente primero
        if (list.size() > 50) list = list.subList(0, 50); // máximo 50 por jugador
        save(uuid, list);
    }

    public void addSanction(UUID uuid, SanctionType type, String staff, String reason, String duration) {
        addSanction(uuid, new SanctionEntry(type, staff, reason, duration, System.currentTimeMillis()));
    }

    // ── Leer ─────────────────────────────────────────────────────────────

    public List<SanctionEntry> getSanctions(UUID uuid) {
        return load(uuid);
    }

    // ── IO ────────────────────────────────────────────────────────────────

    private List<SanctionEntry> load(UUID uuid) {
        File file = new File(sanctionsDir, uuid + ".json");
        if (!file.exists()) return new ArrayList<>();
        String content = FileUtils.readWholeFile(file);
        if (content == null) return new ArrayList<>();
        try {
            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<SanctionEntry>>(){}.getType();
            List<SanctionEntry> list = AstroCore.getInstance().getGson().fromJson(content, listType);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) { return new ArrayList<>(); }
    }

    private void save(UUID uuid, List<SanctionEntry> list) {
        File file = FileUtils.getOrCreateFile(sanctionsDir, uuid + ".json");
        FileUtils.writeString(file, AstroCore.getInstance().getGson().toJson(list));
    }
}
