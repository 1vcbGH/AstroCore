package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;

public class CustomTimerCommand extends BaseCommand {

    public CustomTimerCommand() {
        super("customtimer", "astrocore.customtimer");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            Language.CUSTOM_TIMER_USAGE.forEach(sender::sendMessage);
            return;
        }

        if(args.length < 4) {

            if(args[0].equalsIgnoreCase("list")) {
                TimerManager.getInstance().getCustomTimer().listActiveTimers(sender);
                return;
            }

            if(args.length >= 2 && (args[0].equalsIgnoreCase("cancel") || args[0].equalsIgnoreCase("stop"))) {
                if(TimerManager.getInstance().getCustomTimer().cancel(sender, args[1])) return;

                sender.sendMessage(Language.PREFIX + Language.CUSTOM_TIMER_NOT_RUNNING.replace("<name>", args[1]));
                return;
            }

            Language.CUSTOM_TIMER_USAGE.forEach(sender::sendMessage);
            return;
        }

        if(args[0].equalsIgnoreCase("start")) {
            int duration = StringUtils.parseSeconds(args[args.length-1]);

            if(duration == -1) {
                sender.sendMessage(Language.PREFIX + Language.COMMANDS_INVALID_DURATION);
                return;
            }

            TimerManager.getInstance().getCustomTimer().activate(sender, args, duration);
            return;
        }

        Language.CUSTOM_TIMER_USAGE.forEach(sender::sendMessage);
    }
}
