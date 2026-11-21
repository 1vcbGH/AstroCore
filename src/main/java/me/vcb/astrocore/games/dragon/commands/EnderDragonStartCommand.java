package me.vcb.astrocore.games.dragon.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;

public class EnderDragonStartCommand extends SubCommand {

    EnderDragonStartCommand() {
        super("start", "astrocore.enderdragon.start");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(AstroCore.getInstance().getEnderDragonManager().isActive()) {
            sender.sendMessage(Language.ENDER_DRAGON_PREFIX + Language.ENDER_DRAGON_START_ALREADY_RUNNING);
            return;
        }

        if(AstroCore.getInstance().getEnderDragonManager() == null) {

            return;
        }

        AstroCore.getInstance().getEnderDragonManager().startEnderDragon(sender);
    }
}
