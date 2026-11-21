package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class FriendlyFireCommand extends SubCommand {

    public FriendlyFireCommand() {
        super("friendlyfire", Collections.singletonList("ff"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        if(!faction.getMember(player).getRole().isAtLeast(Role.CO_LEADER)) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NO_PERMISSION.replace("<role>", Role.CO_LEADER.getName()));
            return;
        }

        faction.setFriendlyFire(!faction.isFriendlyFire());

        faction.sendMessage(Language.FACTION_PREFIX + (faction.isFriendlyFire()
            ? Language.FACTIONS_FRIENDLY_FIRE_TOGGLED_ON
            : Language.FACTIONS_FRIENDLY_FIRE_TOGGLED_OFF));
    }
}
