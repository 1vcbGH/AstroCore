package me.vcb.astrocore.factions.commands.admin;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.claim.Claim;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveClaimCommand extends SubCommand {

    public RemoveClaimCommand() {
        super("removeclaim", "astrocore.factions.removeclaim", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Claim claim = ClaimManager.getInstance().getClaimAt(player.getLocation());

        if(claim == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_REMOVE_CLAIM_NO_CLAIM);
            return;
        }

        if(!ClaimManager.getInstance().removeClaim(claim)) return;

        // Remove the AFK hologram if this was an AFKPool zone.
        if(claim.getOwner().getName().equalsIgnoreCase(me.vcb.astrocore.afk.AfkManager.FACTION_NAME)
            && me.vcb.astrocore.afk.AfkManager.getInstance() != null) {
            me.vcb.astrocore.afk.AfkManager.getInstance().clearHolograms(
                claim.getCenter().getWorld(), claim.getMinX(), claim.getMaxX(), claim.getMinZ(), claim.getMaxZ());
        }

        player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_REMOVE_CLAIM_REMOVED_SENDER
            .replace("<faction>", claim.getOwner().getDisplayName(sender))
            .replace("<location>", StringUtils.getLocationNameWithoutY(claim.getCenter())));

        if(!(claim.getOwner() instanceof PlayerFaction)) return;

        PlayerFaction faction = (PlayerFaction) claim.getOwner();

        faction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_REMOVE_CLAIM_REMOVED_FACTION
            .replace("<player>", player.getName())
            .replace("<location>", StringUtils.getLocationNameWithoutY(claim.getCenter())));

        if(faction.getHome() != null && claim.contains(faction.getHome())) {
            faction.setHome(null);
            FactionsManager.getInstance().checkHomeTeleports(faction, Language.FACTIONS_UNCLAIM_HOME_TELEPORT_CANCELLED);
        }
    }
}
