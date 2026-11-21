package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class UninviteCommand extends SubCommand {

    public UninviteCommand() {
        super("uninvite", Collections.singletonList("uninv"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_UNINVITE_USAGE);
            return;
        }

        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        if(!faction.getMember(player).getRole().isAtLeast(Role.CAPTAIN)) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NO_PERMISSION.replace("<role>", Role.CAPTAIN.getName()));
            return;
        }

        if(args[0].equalsIgnoreCase("all")) {
            faction.getPlayerInvitations().clear();
            faction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_UNINVITE_ALL.replace("<player>", player.getName()));
            return;
        }

        if(!faction.getPlayerInvitations().contains(args[0])) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_UNINVITE_NOT_INVITED.replace("<player>", args[0]));
            return;
        }

        faction.getPlayerInvitations().remove(args[0]);
        faction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_UNINVITE_UNINVITED.replace("<player>", args[0]));
    }
}
