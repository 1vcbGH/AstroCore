package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;

public class PurgeCommand extends BaseCommand {

    public PurgeCommand() {
        super("purge", "astrocore.purge");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 1 && "stop".equalsIgnoreCase(args[0])) {
            AstroCore.getInstance().getPurgeHandler().stopPurge(sender);
            return;
        }

        if(args.length == 3 && "start".equalsIgnoreCase(args[0])) {
            int initialDelay = StringUtils.parseSeconds(args[1]);
            int purgeDuration = StringUtils.parseSeconds(args[2]);

            if(initialDelay == -1 || purgeDuration == -1) {
                sender.sendMessage(Language.KIT_PREFIX + Language.COMMANDS_INVALID_DURATION);
                return;
            }

            AstroCore.getInstance().getPurgeHandler().startPrePurgeTask(sender, initialDelay, purgeDuration);
            return;
        }

        Language.PURGE_COMMAND_USAGE.forEach(sender::sendMessage);
    }
}
