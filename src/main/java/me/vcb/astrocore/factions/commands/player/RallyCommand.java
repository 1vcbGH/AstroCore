package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.event.FactionRallyEvent;
import me.vcb.astrocore.factions.event.FactionRallyEvent.RallyEventType;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class RallyCommand extends SubCommand {

    public RallyCommand() {
        super("rally", Collections.singletonList("setrally"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        Location playerLocation = player.getLocation();

        faction.setRallyLocation(playerLocation);

        String location = Config.FACTION_RALLY_INCLUDE_Y_COORDINATE
            ? StringUtils.getLocationNameWithWorld(playerLocation)
            : StringUtils.getLocationNameWithWorldWithoutY(playerLocation);

        faction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_RALLY_SET
            .replace("<player>", player.getName())
            .replace("<location>", location));

        new FactionRallyEvent(player.getUniqueId(), playerLocation, faction, RallyEventType.ADD);
    }
}
