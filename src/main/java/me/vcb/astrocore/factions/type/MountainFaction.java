package me.vcb.astrocore.factions.type;

import lombok.NoArgsConstructor;
import me.vcb.astrocore.utils.Color;

@NoArgsConstructor
public class MountainFaction extends SystemFaction {

    public MountainFaction(String name) {
        super(name);

        this.setColor(name.equals("Ore") ? Color.translate("&b") : Color.translate("&6"));
    }
}
