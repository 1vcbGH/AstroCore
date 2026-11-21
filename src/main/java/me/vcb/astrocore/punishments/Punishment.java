package me.vcb.astrocore.punishments;

import lombok.Getter;
import lombok.Setter;
import me.vcb.astrocore.sanctions.SanctionType;

import java.util.UUID;

/**
 * An active (or historical) punishment applied to a player.
 * Persisted in plugins/AstroCore/punishments/&lt;uuid&gt;.json
 */
@Getter
@Setter
public class Punishment {

    private SanctionType type;
    private String       staff;       // name of staff who applied it
    private String       reason;
    private long         issuedAt;    // epoch millis
    private long         expiresAt;   // epoch millis, 0 = permanent
    private boolean      active;      // false once pardoned/expired-and-cleared
    private String       removedBy;   // staff who pardoned, null otherwise
    private String       category;    // offense category id (GUI presets), null for manual

    public Punishment() {}

    public Punishment(SanctionType type, String staff, String reason, long issuedAt, long expiresAt) {
        this.type      = type;
        this.staff     = staff;
        this.reason    = reason;
        this.issuedAt  = issuedAt;
        this.expiresAt = expiresAt;
        this.active    = true;
    }

    public boolean isPermanent() { return expiresAt <= 0; }

    /** True if the punishment is currently in force. */
    public boolean isCurrentlyActive() {
        if (!active) return false;
        if (isPermanent()) return true;
        return System.currentTimeMillis() < expiresAt;
    }

    public long remainingMillis() {
        if (isPermanent()) return -1;
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }
}
