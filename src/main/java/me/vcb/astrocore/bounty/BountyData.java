package me.vcb.astrocore.bounty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class BountyData {

    private final UUID targetUuid;
    private final String targetName;
    private final int amount;
    private final String setterName; // "AUTO" if auto-placed

    public boolean isAuto() {
        return "AUTO".equals(this.setterName);
    }
}
