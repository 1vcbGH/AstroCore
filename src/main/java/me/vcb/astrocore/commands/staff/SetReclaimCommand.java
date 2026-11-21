package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetReclaimCommand extends BaseCommand {

    public SetReclaimCommand() {
        super("setreclaim", "astrocore.setreclaim");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage(Language.PREFIX + Language.SET_RECLAIM_USAGE);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        if(!StringUtils.isBoolean(args[1])) {
            sender.sendMessage(Language.PREFIX + Language.SET_RECLAIM_INVALID_BOOLEAN);
            return;
        }

        Userdata userdata = AstroCore.getInstance().getUserdataManager().getUserdata(target);
        userdata.setReclaimUsed(Boolean.parseBoolean(args[1]));

        sender.sendMessage(Language.PREFIX + (userdata.isReclaimUsed()
            ? Language.SET_RECLAIM_SET_USED_MESSAGE
            : Language.SET_RECLAIM_SET_NOT_USED_MESSAGE).replace("<player>", target.getName()));
    }
}
