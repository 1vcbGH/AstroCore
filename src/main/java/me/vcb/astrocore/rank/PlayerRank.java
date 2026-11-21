package me.vcb.astrocore.rank;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlayerRank {

    MORTAL   ("Mortal",    "§7",  0),
    GUERRERO ("Guerrero",  "§f",  1200),
    VALQUIRIA("Valquiria", "§b",  1500),
    TITAN    ("Titán",     "§a",  1800),
    FENIX    ("Fénix",    "§6",  2200),
    ANUBIS   ("Anubis",    "§5",  2700),
    ODIN     ("Odín",     "§9",  3300),
    OLYMPUS  ("Olympus",   "§c",  4000);

    private final String displayName;
    private final String color;
    private final int eloRequired;

    /** Base ELO for new players. */
    public static final int BASE_ELO = 1000;
    /** ELO gained per kill. */
    public static final int ELO_PER_KILL = 10;
    /** ELO lost per death (minimum 0). */
    public static final int ELO_PER_DEATH = 15;
    /** ELO gained per KOTH cap. */
    public static final int ELO_PER_KOTH = 150;

    /** Returns the highest rank the player qualifies for based on ELO. */
    public static PlayerRank fromElo(int elo) {
        PlayerRank result = MORTAL;
        for (PlayerRank rank : values()) {
            if (elo >= rank.eloRequired) {
                result = rank;
            }
        }
        return result;
    }

    /** Returns the next rank above this one, or null if already max. */
    public PlayerRank next() {
        PlayerRank[] vals = values();
        int idx = this.ordinal() + 1;
        return idx < vals.length ? vals[idx] : null;
    }

    public String getColoredName() {
        return this.color + this.displayName;
    }
}
