package me.vcb.astrocore.games.dragon.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;

public class EnderDragonSetHealthCommand extends SubCommand {

    EnderDragonSetHealthCommand() {
        super("sethealth", "astrocore.enderdragon.sethealth");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.ENDER_DRAGON_PREFIX + Language.ENDER_DRAGON_SET_HEALTH_USAGE);
            return;
        }

        if(!AstroCore.getInstance().getEnderDragonManager().isActive()) {
            sender.sendMessage(Language.ENDER_DRAGON_PREFIX + Language.ENDER_DRAGON_EXCEPTION_NOT_RUNNING);
            return;
        }

        if(!this.checkNumber(sender, args[0])) return;

        int amount = Math.abs(Integer.parseInt(args[0]));
        AstroCore.getInstance().getEnderDragonManager().changeDragonHealth(amount);

        Messages.sendMessage(Language.ENDER_DRAGON_PREFIX + Language.ENDER_DRAGON_SET_HEALTH_CHANGED
        .replace("<amount>", String.valueOf(amount)));
    }
}
