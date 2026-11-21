package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.selection.SelectionType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EndPortalCommand extends BaseCommand {

    public EndPortalCommand() {
        super("endportal", "astrocore.endportal", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        AstroCore.getInstance().getSelectionManager().toggleSelectionProcess(player, SelectionType.SELECTION,
        AstroCore.getInstance().getEndPortalHandler().createSelectionCheck());

        player.sendMessage(Language.PREFIX + Language.ENDPORTAL_SELECTOR_ADDED_TO_INVENTORY);
    }
}
