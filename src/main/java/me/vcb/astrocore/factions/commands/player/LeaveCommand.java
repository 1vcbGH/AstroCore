package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.claim.Claim;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand extends SubCommand {

    public LeaveCommand() {
        super("leave", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        if(faction.getMember(player).getRole() == Role.LEADER) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_LEADER_LEAVE);
            return;
        }

        if(!Config.FACTION_LEAVE_WHILE_FROZEN && faction.isFrozen()) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CANNOT_LEAVE_WHILE_REGENERATING);
            return;
        }

        Claim claim = ClaimManager.getInstance().getClaimAt(player);

        if(claim != null && !Config.FACTION_LEAVE_WHILE_IN_OWN_CLAIM && claim.getOwner() == faction) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CANNOT_LEAVE_WHILE_IN_OWN_CLAIM);
            return;
        }

        if(!FactionsManager.getInstance().leaveFaction(player, faction)) return;

        player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_LEFT_SELF.replace("<name>", faction.getName()));
        faction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_LEFT_OTHERS.replace("<player>", player.getName()));
    }
}
