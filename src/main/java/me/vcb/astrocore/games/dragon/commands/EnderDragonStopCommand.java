package me.vcb.astrocore.games.dragon.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;

public class EnderDragonStopCommand extends SubCommand {

    EnderDragonStopCommand() {
        super("stop", "astrocore.enderdragon.stop");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!AstroCore.getInstance().getEnderDragonManager().isActive()) {
            sender.sendMessage(Language.ENDER_DRAGON_PREFIX + Language.ENDER_DRAGON_EXCEPTION_NOT_RUNNING);
            return;
        }

        AstroCore.getInstance().getEnderDragonManager().stopEnderDragon(true);
        Messages.sendMessage(Language.ENDER_DRAGON_PREFIX + Language.ENDER_DRAGON_STOP_STOPPED);
    }
}
