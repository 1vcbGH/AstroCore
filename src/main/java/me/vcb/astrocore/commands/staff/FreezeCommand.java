package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class FreezeCommand extends BaseCommand {

    public FreezeCommand() {
        super("freeze", Arrays.asList("fz", "ss"), "astrocore.freeze");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.PREFIX + Language.FREEZE_USAGE);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        if(sender != target && target.hasPermission("astrocore.freeze.bypass")) {
            sender.sendMessage(Language.PREFIX + Language.FREEZE_CAN_NOT_FREEZE_PLAYER
                .replace("<player>", target.getName()));
            return;
        }

        AstroCore.getInstance().getFreezeHandler().toggleFreeze(sender, target);
    }
}
