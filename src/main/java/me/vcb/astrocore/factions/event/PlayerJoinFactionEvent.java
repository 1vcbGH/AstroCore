package me.vcb.astrocore.factions.event;

import lombok.Getter;
import lombok.Setter;
import me.vcb.astrocore.factions.FactionPlayer;
import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;

@Getter
public class PlayerJoinFactionEvent extends FactionEvent implements Cancellable {

    private final FactionPlayer factionPlayer;
    private final PlayerFaction faction;
    @Setter private boolean cancelled;

    public PlayerJoinFactionEvent(FactionPlayer factionPlayer, PlayerFaction faction) {
        this.factionPlayer = factionPlayer;
        this.faction = faction;

        Bukkit.getPluginManager().callEvent(this);
    }
}
