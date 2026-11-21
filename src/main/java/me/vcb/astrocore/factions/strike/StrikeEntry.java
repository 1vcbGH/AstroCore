package me.vcb.astrocore.factions.strike;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StrikeEntry {

    private final String reason;
    private final String staffName;
    private final String date; // "dd/MM/yyyy"
}
