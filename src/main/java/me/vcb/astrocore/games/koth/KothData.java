package me.vcb.astrocore.games.koth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.SystemFaction;
import me.vcb.astrocore.games.Cuboid;
import me.vcb.astrocore.games.loot.LootData;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class KothData {

    private String name;
    private UUID factionId;
    private int captime;
    private int factionPoints;
    private Cuboid cuboid;

    private transient String color;
    private transient LootData loot;

    public KothData(String name) {
        this.name = name;
    }

    public Faction getFaction() {
        return FactionsManager.getInstance().getFactionByUuid(this.factionId);
    }

    public String getColoredName() {
        return this.color + this.name;
    }

    public void setupKothColor() {
        this.color =  ((SystemFaction) this.getFaction()).getColor();
    }
}
