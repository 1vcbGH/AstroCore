package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GodCommand extends BaseCommand {

    public GodCommand() {
        super("god", "astrocore.god");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            if(!this.checkConsoleSender(sender)) return;

            Player player = (Player) sender;
            NmsUtils.getInstance().toggleInvulnerable(player);

            player.sendMessage(Language.PREFIX + (NmsUtils.getInstance().isInvulnerable(player)
            ? Language.GOD_SELF_ENABLED : Language.GOD_SELF_DISABLED));
            return;
        }

        if(!this.checkPermission(sender, "astrocore.god.others")) return;

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        NmsUtils.getInstance().toggleInvulnerable(target);

        target.sendMessage(Language.PREFIX + (NmsUtils.getInstance().isInvulnerable(target)
            ? Language.GOD_SELF_ENABLED : Language.GOD_SELF_DISABLED));

        sender.sendMessage(Language.PREFIX + (NmsUtils.getInstance().isInvulnerable(target)
            ? Language.GOD_OTHERS_ENABLED : Language.GOD_OTHERS_DISABLED).replace("<player>", target.getName()));
    }
}
