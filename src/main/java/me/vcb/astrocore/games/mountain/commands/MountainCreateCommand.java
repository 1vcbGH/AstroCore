package me.vcb.astrocore.games.mountain.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.games.mountain.MountainType;
import me.vcb.astrocore.selection.Selection;
import me.vcb.astrocore.selection.SelectionType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class MountainCreateCommand extends SubCommand {

    MountainCreateCommand() {
        super("create", Collections.singletonList("new"), "astrocore.mountain.create", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.MOUNTAIN_PREFIX + Language.MOUNTAIN_CREATE_USAGE);
            return;
        }

        MountainType type;
        try {
            type = MountainType.valueOf(args[0].toUpperCase());
        } catch(IllegalArgumentException e) {
            sender.sendMessage(Language.MOUNTAIN_PREFIX + Language.MOUNTAIN_EXCEPTION_INCORRECT_TYPE);
            return;
        }

        Player player = (Player) sender;
        Selection selection = AstroCore.getInstance().getSelectionManager().getSelection(player);

        if(selection == null || selection.getType() != SelectionType.SYSTEM_CLAIM) {
            AstroCore.getInstance().getSelectionManager().toggleSelectionProcess(player, SelectionType.SYSTEM_CLAIM, null);
            player.sendMessage(Language.MOUNTAIN_PREFIX + Language.MOUNTAIN_CREATE_MAKE_A_SELECTION);
            return;
        }

        if(!selection.areBothPositionsSet()) {
            player.sendMessage(Language.MOUNTAIN_PREFIX + Language.MOUNTAIN_CREATE_SET_BOTH_POSITIONS);
            return;
        }

        if(!ClaimManager.getInstance().getClaimsInSelection(selection.getPosOne(), selection.getPosTwo()).isEmpty()) {
            player.sendMessage(Language.MOUNTAIN_PREFIX + Language.MOUNTAIN_CREATE_CLAIM_OVERLAPPING);
            return;
        }

        Faction faction = type == MountainType.GLOWSTONE ? FactionsManager.getInstance()
        .getFactionByName("Glowstone") : FactionsManager.getInstance().getFactionByName("Ore");

        if(!ClaimManager.getInstance().addClaim(selection.toClaim(faction))) return;

        int id = AstroCore.getInstance().getMountainManager().createMountain(type, faction.getId(), selection.toCuboid());

        AstroCore.getInstance().getSelectionManager().removeSelectionProcess(player);

        sender.sendMessage(Language.MOUNTAIN_PREFIX + Language.MOUNTAIN_CREATE_CREATED
            .replace("<id>", String.valueOf(id)));
    }
}
