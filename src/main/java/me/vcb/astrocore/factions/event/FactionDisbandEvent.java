package me.vcb.astrocore.factions.event;

import lombok.Getter;
import lombok.Setter;
import me.vcb.astrocore.factions.Faction;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

@Getter
public class FactionDisbandEvent extends FactionEvent implements Cancellable {

    private final Faction faction;
    private final CommandSender sender;
    @Setter private boolean cancelled;

    public FactionDisbandEvent(Faction faction, CommandSender sender) {
        this.faction = faction;
        this.sender = sender;

        Bukkit.getPluginManager().callEvent(this);
    }
}
