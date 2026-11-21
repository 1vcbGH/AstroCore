package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.event.AstroCoreKickEvent;
import me.vcb.astrocore.handlers.event.AstroCoreKickEvent.KickType;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class KickallCommand extends BaseCommand {

    public KickallCommand() {
        super("kickall", "astrocore.kickall");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.PREFIX + Language.KICKALL_USAGE);
            return;
        }

        String reason = Color.translate("&c" + StringUtils.joinArray(args, " ", 1));
        AstroCore.getInstance().getCombatLoggerHandler().setKickAll(true);

        Bukkit.getOnlinePlayers().stream().filter(player -> !player.hasPermission("astrocore.staff")).forEach(player -> {
            AstroCoreKickEvent event = new AstroCoreKickEvent(player, KickType.KICKALL, reason);

            if(!event.isCancelled()) {
                player.kickPlayer(reason);
            }
        });

        AstroCore.getInstance().getCombatLoggerHandler().setKickAll(false);

        Messages.sendMessage(Language.PREFIX + Language.KICKALL_STAFF_MESSAGE
            .replace("<player>", sender.getName())
            .replace("<reason>", reason));
    }
}
