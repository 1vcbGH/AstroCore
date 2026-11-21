package me.vcb.astrocore.games.conquest.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.conquest.ConquestData;
import me.vcb.astrocore.games.conquest.ZoneType;
import me.vcb.astrocore.selection.Selection;
import me.vcb.astrocore.selection.SelectionType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConquestSetCapzoneCommand extends SubCommand {

    ConquestSetCapzoneCommand() {
        super("setcapzone", "astrocore.conquest.setcapzone", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_SET_CAPZONE_USAGE);
            return;
        }

        ZoneType type;

        try {
            type = ZoneType.valueOf(args[0].toUpperCase());
        } catch(IllegalArgumentException e) {
            sender.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_EXCEPTION_INVALID_ZONE);
            return;
        }

        Player player = (Player) sender;
        Selection selection = AstroCore.getInstance().getSelectionManager().getSelection(player);

        if(selection == null || selection.getType() != SelectionType.EVENT_CLAIM) {
            AstroCore.getInstance().getSelectionManager().toggleSelectionProcess(player, SelectionType.EVENT_CLAIM, null);
            player.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_SET_CAPZONE_MAKE_A_SELECTION);
            return;
        }

        if(!selection.areBothPositionsSet()) {
            player.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_SET_CAPZONE_SET_BOTH_POSITIONS);
            return;
        }

        ConquestData conquest = AstroCore.getInstance().getConquestManager().getConquest();
        conquest.setCuboid(type, selection.toCuboid());

        AstroCore.getInstance().getSelectionManager().removeSelectionProcess(player);

        player.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_SET_CAPZONE_CREATED
        .replace("<zone>", type.getName()));
    }
}
