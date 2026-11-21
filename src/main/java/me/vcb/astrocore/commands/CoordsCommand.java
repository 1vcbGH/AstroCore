package me.vcb.astrocore.commands;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;

public class CoordsCommand extends BaseCommand {

    public CoordsCommand() {
        super("coords", "astrocore.coords");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Language.COORDS_MESSAGE.forEach(sender::sendMessage);
    }
}
