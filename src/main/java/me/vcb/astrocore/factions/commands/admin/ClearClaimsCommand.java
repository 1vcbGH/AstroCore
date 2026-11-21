package me.vcb.astrocore.factions.commands.admin;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.factions.event.FactionClaimChangeEvent.ClaimChangeReason;
import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.command.CommandSender;

public class ClearClaimsCommand extends SubCommand {

    public ClearClaimsCommand() {
        super("clearclaims", "astrocore.factions.clearclaims");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLEAR_CLAIMS_USAGE);
            return;
        }

        Faction faction = FactionsManager.getInstance().getAnyFaction(args[0]);

        if(faction == null) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FACTION_DOESNT_EXIST.replace("<argument>", args[0]));
            return;
        }

        if(faction.getName().equalsIgnoreCase(me.vcb.astrocore.afk.AfkManager.FACTION_NAME)
            && me.vcb.astrocore.afk.AfkManager.getInstance() != null) {
            me.vcb.astrocore.afk.AfkManager.getInstance().clearAllHolograms();
        }

        if(!ClaimManager.getInstance().removeAllClaims(faction, ClaimChangeReason.UNCLAIM_ALL)) return;
        ClaimManager.getInstance().rebuildCache(); // restore any overlapped claims (e.g. spawn under AFKPool)

        sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLEAR_CLAIMS_CLEARED_SENDER.replace("<faction>", faction.getDisplayName(sender)));

        if(!(faction instanceof PlayerFaction)) return;

        PlayerFaction playerFaction = (PlayerFaction) faction;
        playerFaction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLEAR_CLAIMS_CLEARED_FACTION.replace("<player>", sender.getName()));

        playerFaction.setHome(null);
        FactionsManager.getInstance().checkHomeTeleports(playerFaction, Language.FACTIONS_CLEAR_CLAIMS_HOME_TELEPORT_CANCELLED);
    }
}
