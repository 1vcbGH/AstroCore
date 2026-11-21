package me.vcb.astrocore.punishments;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.sanctions.SanctionManager;
import me.vcb.astrocore.sanctions.SanctionType;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.FileUtils;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Active punishment storage + enforcement.
 * Active bans / mutes / blacklists are persisted per-player and checked on
 * login (ban/blacklist) and chat (mute). History is mirrored to
 * {@link SanctionManager} for the /historial command.
 */
public class PunishmentManager {

    private static PunishmentManager instance;
    private final File dir;

    public PunishmentManager() {
        instance = this;
        this.dir = new File(AstroCore.getInstance().getDataFolder(), "punishments");
        if (!this.dir.exists()) this.dir.mkdirs();
    }

    public static PunishmentManager getInstance() { return instance; }

    // ── Apply ──────────────────────────────────────────────────────────────

    /** Apply a ban (duration <=0 = permanent). Kicks if online. */
    public void ban(UUID uuid, String name, String staff, String reason, long durationMillis) {
        ban(uuid, name, staff, reason, durationMillis, null);
    }

    public void ban(UUID uuid, String name, String staff, String reason, long durationMillis, String categoryId) {
        long now = System.currentTimeMillis();
        long expires = durationMillis <= 0 ? 0 : now + durationMillis;
        SanctionType type = expires == 0 ? SanctionType.BAN : SanctionType.TEMPBAN;
        Punishment p = new Punishment(type, staff, reason, now, expires);
        p.setCategory(categoryId);
        record(uuid, p, name);
        Tasks.sync(() -> {
            Player online = Bukkit.getPlayer(uuid);
            if (online != null) online.kickPlayer(buildBanScreen(type, reason, staff, expires));
        });
    }

    /**
     * Applies a categorised punishment, auto-escalating to the next offense tier
     * based on how many prior offenses of this category the player has.
     * @return the 1-based offense number that was applied.
     */
    public int applyCategory(UUID uuid, String name, String staff, PunishmentCategory cat) {
        int prior = countOffenses(uuid, cat.id());
        int tierIndex = Math.min(prior, cat.tierCount() - 1);
        long dur = cat.durationForTier(tierIndex);
        int offenseNumber = tierIndex + 1;
        String reason = cat.getDisplayName() + " (Ofensa #" + offenseNumber + ")";

        if (cat.getType() == SanctionType.MUTE) {
            mute(uuid, name, staff, reason, dur, cat.id());
        } else {
            ban(uuid, name, staff, reason, dur, cat.id());
        }
        return offenseNumber;
    }

    /** Counts how many times this category was applied to the player (active or not). */
    public int countOffenses(UUID uuid, String categoryId) {
        int n = 0;
        for (Punishment p : load(uuid))
            if (categoryId.equals(p.getCategory())) n++;
        return n;
    }

    public void blacklist(UUID uuid, String name, String staff, String reason) {
        long now = System.currentTimeMillis();
        record(uuid, new Punishment(SanctionType.BLACKLIST, staff, reason, now, 0), name);
        Tasks.sync(() -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.kickPlayer(buildBanScreen(SanctionType.BLACKLIST, reason, staff, 0));
        });
    }

    /** Apply a mute (duration <=0 = permanent). */
    public void mute(UUID uuid, String name, String staff, String reason, long durationMillis) {
        mute(uuid, name, staff, reason, durationMillis, null);
    }

    public void mute(UUID uuid, String name, String staff, String reason, long durationMillis, String categoryId) {
        long now = System.currentTimeMillis();
        long expires = durationMillis <= 0 ? 0 : now + durationMillis;
        SanctionType type = expires == 0 ? SanctionType.MUTE : SanctionType.TEMPMUTE;
        Punishment punishment = new Punishment(type, staff, reason, now, expires);
        punishment.setCategory(categoryId);
        record(uuid, punishment, name);
        Tasks.sync(() -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(Color.translate("&cHas sido silenciado. &7Razón: &f" + reason
                + (expires == 0 ? " &8(&cPermanente&8)" : " &8(&7" + PunishmentUtil.formatRemaining(expires - now) + "&8)")));
        });
    }

    public void warn(UUID uuid, String name, String staff, String reason) {
        long now = System.currentTimeMillis();
        // warns are not "active" enforcement, but we keep them in history + count
        Punishment p = new Punishment(SanctionType.WARN, staff, reason, now, 0);
        record(uuid, p, name);
        Tasks.sync(() -> {
            Player online = Bukkit.getPlayer(uuid);
            if (online != null) {
                online.sendMessage(Color.translate("&e&l⚠ &cHas recibido una advertencia. &7Razón: &f" + reason));
                online.sendTitle(Color.translate("&e&l⚠ ADVERTENCIA"), Color.translate("&f" + reason), 10, 60, 10);
            }
        });
    }

    public void kick(UUID uuid, String name, String staff, String reason) {
        // kicks only go to history, no persistent state
        SanctionManager.getInstance().addSanction(uuid, SanctionType.KICK, staff, reason, "");
        Tasks.sync(() -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.kickPlayer(Color.translate("&c&lExpulsado del servidor\n\n&7Razón: &f" + reason
                + "\n&7Staff: &f" + staff));
        });
    }

    // ── Pardon ─────────────────────────────────────────────────────────────

    /** Deactivates active bans/blacklists. Returns true if any was active. */
    public boolean unban(UUID uuid, String staff) {
        boolean changed = deactivate(uuid, p ->
            p.getType() == SanctionType.BAN || p.getType() == SanctionType.TEMPBAN
            || p.getType() == SanctionType.BLACKLIST, staff);
        if (changed) SanctionManager.getInstance().addSanction(uuid, SanctionType.UNBAN, staff, "", "");
        return changed;
    }

    public boolean unmute(UUID uuid, String staff) {
        boolean changed = deactivate(uuid, p ->
            p.getType() == SanctionType.MUTE || p.getType() == SanctionType.TEMPMUTE, staff);
        if (changed) SanctionManager.getInstance().addSanction(uuid, SanctionType.UNMUTE, staff, "", "");
        return changed;
    }

    public boolean unblacklist(UUID uuid, String staff) {
        boolean changed = deactivate(uuid, p -> p.getType() == SanctionType.BLACKLIST, staff);
        if (changed) SanctionManager.getInstance().addSanction(uuid, SanctionType.UNBLACKLIST, staff, "", "");
        return changed;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public Punishment getActiveBan(UUID uuid) {
        for (Punishment p : load(uuid)) {
            if (!p.isCurrentlyActive()) continue;
            if (p.getType() == SanctionType.BAN || p.getType() == SanctionType.TEMPBAN
                || p.getType() == SanctionType.BLACKLIST) return p;
        }
        return null;
    }

    public Punishment getActiveMute(UUID uuid) {
        for (Punishment p : load(uuid)) {
            if (!p.isCurrentlyActive()) continue;
            if (p.getType() == SanctionType.MUTE || p.getType() == SanctionType.TEMPMUTE) return p;
        }
        return null;
    }

    public boolean isBanned(UUID uuid)  { return getActiveBan(uuid)  != null; }
    public boolean isMuted(UUID uuid)   { return getActiveMute(uuid) != null; }

    public List<Punishment> getPunishments(UUID uuid) { return load(uuid); }

    // ── Internal ───────────────────────────────────────────────────────────

    private void record(UUID uuid, Punishment punishment, String name) {
        List<Punishment> list = load(uuid);
        list.add(0, punishment);
        save(uuid, list);
        SanctionManager.getInstance().addSanction(uuid, punishment.getType(), punishment.getStaff(),
            punishment.getReason(), PunishmentUtil.durationLabel(punishment.getExpiresAt(), punishment.getIssuedAt()));
    }

    private interface Filter { boolean test(Punishment p); }

    private boolean deactivate(UUID uuid, Filter filter, String staff) {
        List<Punishment> list = load(uuid);
        boolean changed = false;
        for (Punishment p : list) {
            if (p.isActive() && filter.test(p)) {
                p.setActive(false);
                p.setRemovedBy(staff);
                changed = true;
            }
        }
        if (changed) save(uuid, list);
        return changed;
    }

    public String buildBanScreen(SanctionType type, String reason, String staff, long expiresAt) {
        String header = switch (type) {
            case BLACKLIST -> "&4&lESTÁS EN LA BLACKLIST";
            default        -> "&c&lHas sido baneado";
        };
        String dur = expiresAt <= 0 ? "&cPermanente"
            : "&f" + PunishmentUtil.formatRemaining(expiresAt - System.currentTimeMillis());
        return Color.translate(header
            + "\n\n&7Razón: &f" + reason
            + "\n&7Duración: " + dur
            + "\n&7Staff: &f" + staff
            + "\n\n&7Apela en &fdiscord.gg/tuserver");
    }

    // ── IO ───────────────────────────────────────────────────────────────────

    private List<Punishment> load(UUID uuid) {
        File file = new File(dir, uuid + ".json");
        if (!file.exists()) return new ArrayList<>();
        String content = FileUtils.readWholeFile(file);
        if (content == null) return new ArrayList<>();
        try {
            java.lang.reflect.Type t = new com.google.gson.reflect.TypeToken<List<Punishment>>(){}.getType();
            List<Punishment> list = AstroCore.getInstance().getGson().fromJson(content, t);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) { return new ArrayList<>(); }
    }

    private void save(UUID uuid, List<Punishment> list) {
        File file = FileUtils.getOrCreateFile(dir, uuid + ".json");
        FileUtils.writeString(file, AstroCore.getInstance().getGson().toJson(list));
    }
}
