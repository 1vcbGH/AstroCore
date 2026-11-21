package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class EotwCommand extends BaseCommand {

    public EotwCommand() {
        super("eotw", "astrocore.eotw");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 1 && "stop".equalsIgnoreCase(args[0])) {
            AstroCore.getInstance().getEotwHandler().stopEotw(sender);
            return;
        }

        if(args.length == 2 && "start".equalsIgnoreCase(args[0])) {
            if(!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(Language.PREFIX + Language.COMMANDS_FOR_CONSOLE_ONLY);
                return;
            }

            int time = StringUtils.parseSeconds(args[1]);

            if(time == -1) {
                sender.sendMessage(Language.KIT_PREFIX + Language.COMMANDS_INVALID_DURATION);
                return;
            }

            AstroCore.getInstance().getEotwHandler().startPreEotwTask(sender, time);
            return;
        }

        Language.EOTW_COMMAND_USAGE.forEach(sender::sendMessage);
    }
}
