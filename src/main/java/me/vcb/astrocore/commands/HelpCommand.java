package me.vcb.astrocore.commands;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class HelpCommand extends BaseCommand {

    public HelpCommand() {
        super("help", Collections.singletonList("?"), "astrocore.help");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Language.HELP_MESSAGE.forEach(sender::sendMessage);
    }
}
