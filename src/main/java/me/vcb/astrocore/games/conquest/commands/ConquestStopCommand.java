package me.vcb.astrocore.games.conquest.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;

public class ConquestStopCommand extends SubCommand {

    ConquestStopCommand() {
        super("stop", "astrocore.conquest.stop");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!AstroCore.getInstance().getConquestManager().isActive()) {
            sender.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_EXCEPTION_NOT_RUNNING);
            return;
        }

        AstroCore.getInstance().getConquestManager().stopConquest(false);

        Messages.sendMessage(Language.CONQUEST_PREFIX + Language
        .CONQUEST_STOP_STOPPED.replace("<player>", sender.getName()));
    }
}
