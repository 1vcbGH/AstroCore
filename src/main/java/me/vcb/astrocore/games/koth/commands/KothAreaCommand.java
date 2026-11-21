package me.vcb.astrocore.games.koth.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.games.koth.KothData;
import me.vcb.astrocore.selection.Selection;
import me.vcb.astrocore.selection.SelectionType;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KothAreaCommand extends SubCommand {

    KothAreaCommand() {
        super("area", "astrocore.koth.area", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_AREA_USAGE);
            return;
        }

        KothData koth = AstroCore.getInstance().getKothManager().getKoth(args[0]);

        if(koth == null) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_EXCEPTION_DOESNT_EXIST.replace("<koth>", args[0]));
            return;
        }

        Player player = (Player) sender;
        Selection selection = AstroCore.getInstance().getSelectionManager().getSelection(player);

        if(selection == null || selection.getType() != SelectionType.SYSTEM_CLAIM) {
            AstroCore.getInstance().getSelectionManager().toggleSelectionProcess(player, SelectionType.SYSTEM_CLAIM, null);
            player.sendMessage(Language.KOTH_PREFIX + Language.KOTH_AREA_MAKE_A_SELECTION);
            return;
        }

        if(!selection.areBothPositionsSet()) {
            player.sendMessage(Language.KOTH_PREFIX + Language.KOTH_AREA_SET_BOTH_POSITIONS);
            return;
        }

        if(!ClaimManager.getInstance().getClaimsInSelection(selection.getPosOne(), selection.getPosTwo()).isEmpty()) {
            player.sendMessage(Language.KOTH_PREFIX + Language.KOTH_AREA_CLAIM_OVERLAPPING);
            return;
        }

        if(!ClaimManager.getInstance().addClaim(selection.toClaim(koth.getFaction()))) return;
        AstroCore.getInstance().getSelectionManager().removeSelectionProcess(player);

        Messages.sendMessage(Language.KOTH_PREFIX + Language.KOTH_AREA_CREATED.replace("<player>",
        player.getName()).replace("<koth>", koth.getName()), "astrocore.staff");
    }
}
