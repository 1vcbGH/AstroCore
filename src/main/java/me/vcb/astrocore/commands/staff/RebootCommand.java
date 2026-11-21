package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;

public class RebootCommand extends BaseCommand {

    public RebootCommand() {
        super("reboot", "astrocore.reboot");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            Language.REBOOT_USAGE.forEach(sender::sendMessage);
            return;
        }

        switch(args[0].toLowerCase()) {
            case "stop":
            case "cancel": {
                if(!AstroCore.getInstance().getRebootHandler().isRebooting()) {
                    sender.sendMessage(Language.PREFIX + Language.REBOOT_NOT_RUNNING);
                    return;
                }

                AstroCore.getInstance().getRebootHandler().cancelReboot();
                return;
            } case "start": {
                if(args.length < 2) {
                    Language.REBOOT_USAGE.forEach(sender::sendMessage);
                    return;
                }

                if(AstroCore.getInstance().getRebootHandler().isRebooting()) {
                    sender.sendMessage(Language.PREFIX + Language.REBOOT_ALREADY_RUNNING);
                    return;
                }

                int duration = StringUtils.parseSeconds(args[1]);

                if(duration == -1) {
                    sender.sendMessage(Language.PREFIX + Language.COMMANDS_INVALID_DURATION);
                    return;
                }

                AstroCore.getInstance().getRebootHandler().startReboot(duration);
                return;
            } default: {
                Language.REBOOT_USAGE.forEach(sender::sendMessage);
            }
        }
    }
}
