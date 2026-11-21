package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IgnoreCommand extends BaseCommand {

    public IgnoreCommand() {
        super("ignore", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.PREFIX + Language.IGNORE_USAGE);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        Player player = (Player) sender;

        if(player == target) {
            player.sendMessage(Language.PREFIX + Language.IGNORE_CANNOT_IGNORE_SELF);
            return;
        }

        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(player);

        if(data.isIgnoring(target)) {
            data.removeIgnoring(target);
            player.sendMessage(Language.PREFIX + Language.IGNORE_DISABLED.replace("<player>", target.getName()));
        } else {
            data.addIgnoring(target);
            player.sendMessage(Language.PREFIX + Language.IGNORE_ENABLED.replace("<player>", target.getName()));
        }
    }
}
