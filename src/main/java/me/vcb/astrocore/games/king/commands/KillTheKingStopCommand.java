package me.vcb.astrocore.games.king.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;

public class KillTheKingStopCommand extends SubCommand {

    KillTheKingStopCommand() {
        super("stop", "astrocore.killtheking.stop");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!AstroCore.getInstance().getKillTheKingManager().isActive()) {
            sender.sendMessage(Language.KING_PREFIX + Language.KING_EXCEPTION_NOT_RUNNING);
            return;
        }

        AstroCore.getInstance().getKillTheKingManager().stopKillTheKing(false);
        Messages.sendMessage(Language.KING_PREFIX + Language.KING_STOP_STOPPED.replace("<player>", sender.getName()));
    }
}
