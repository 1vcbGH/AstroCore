package me.vcb.astrocore.commands;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;

public class SubclaimCommand extends BaseCommand {

    public SubclaimCommand() {
        super("subclaim", "astrocore.subclaim");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Language.SUBCLAIM_MESSAGE.forEach(sender::sendMessage);
    }
}
