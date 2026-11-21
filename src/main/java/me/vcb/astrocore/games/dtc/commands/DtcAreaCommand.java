package me.vcb.astrocore.games.dtc.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.selection.Selection;
import me.vcb.astrocore.selection.SelectionType;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DtcAreaCommand extends SubCommand {

    DtcAreaCommand() {
        super("area", "astrocore.dtc.area", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Selection selection = AstroCore.getInstance().getSelectionManager().getSelection(player);

        if(selection == null || selection.getType() != SelectionType.SYSTEM_CLAIM) {
            AstroCore.getInstance().getSelectionManager().toggleSelectionProcess(player, SelectionType.SYSTEM_CLAIM, null);
            player.sendMessage(Language.DTC_PREFIX + Language.DTC_AREA_MAKE_A_SELECTION);
            return;
        }

        if(!selection.areBothPositionsSet()) {
            player.sendMessage(Language.DTC_PREFIX + Language.DTC_AREA_SET_BOTH_POSITIONS);
            return;
        }

        if(!ClaimManager.getInstance().getClaimsInSelection(selection.getPosOne(), selection.getPosTwo()).isEmpty()) {
            player.sendMessage(Language.DTC_PREFIX + Language.DTC_AREA_CLAIM_OVERLAPPING);
            return;
        }

        Faction faction = FactionsManager.getInstance().getFactionByName("DTC");

        if(!ClaimManager.getInstance().addClaim(selection.toClaim(faction))) return;
        AstroCore.getInstance().getSelectionManager().removeSelectionProcess(player);

        Messages.sendMessage(Language.DTC_PREFIX + Language.DTC_AREA_CREATED
        .replace("<player>", player.getName()), "astrocore.staff");
    }
}
