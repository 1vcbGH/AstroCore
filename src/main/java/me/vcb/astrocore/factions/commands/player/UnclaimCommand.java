package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.claim.Claim;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnclaimCommand extends SubCommand {

    public UnclaimCommand() {
        super("unclaim", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        if(faction.getMember(player).getRole() != Role.LEADER) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NO_PERMISSION.replace("<role>", Role.LEADER.getName()));
            return;
        }

        if(faction.getClaims().size() == 0) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_UNCLAIM_NO_CLAIMS);
            return;
        }

        Claim claim = ClaimManager.getInstance().getClaimAt(player);

        if(claim == null || claim.getOwner() != faction) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_UNCLAIM_NOT_OWNER);
            return;
        }

        if(!Config.FACTION_UNCLAIM_WHILE_FROZEN && faction.isFrozen()) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CANNOT_UNCLAIM_WHILE_REGENERATING);
            return;
        }

        if(!ClaimManager.getInstance().removeClaim(claim)) return;

        if(faction.getHome() != null && claim.contains(faction.getHome())) {
            faction.setHome(null);
            FactionsManager.getInstance().checkHomeTeleports(faction, Language.FACTIONS_UNCLAIM_HOME_TELEPORT_CANCELLED);
        }

        faction.addLog("&c- &f" + player.getName() + " &7desclaimeó &f" + StringUtils.getLocationNameWithoutY(claim.getCenter()));
        faction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_UNCLAIMED.replace("<player>",
        player.getName()).replace("<location>", StringUtils.getLocationNameWithoutY(claim.getCenter())));
    }
}
