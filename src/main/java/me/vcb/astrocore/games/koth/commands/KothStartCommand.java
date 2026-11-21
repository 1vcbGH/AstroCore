package me.vcb.astrocore.games.koth.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.koth.KothData;
import me.vcb.astrocore.games.koth.RunningKoth;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;

public class KothStartCommand extends SubCommand {

    KothStartCommand() {
        super("start", "astrocore.koth.start");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_START_USAGE);
            return;
        }

        if(AstroCore.getInstance().getKothManager().isMaxRunningKothsReached()) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_EXCEPTION_MAX_RUNNING_KOTHS_AMOUNT_REACHED);
            return;
        }

        KothData koth = AstroCore.getInstance().getKothManager().getKoth(args[0]);

        if(koth == null) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_EXCEPTION_DOESNT_EXIST.replace("<koth>", args[0]));
            return;
        }

        RunningKoth runningKoth = AstroCore.getInstance().getKothManager().getRunningKoth(args[0]);

        if(runningKoth != null) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_START_ALREADY_RUNNING.replace("<koth>", koth.getName()));
            return;
        }

        int time = args.length == 1 ? koth.getCaptime() : StringUtils.parseSeconds(args[1]);

        if(time == -1) {
            sender.sendMessage(Language.PREFIX + Language.COMMANDS_INVALID_DURATION);
            return;
        }

        AstroCore.getInstance().getKothManager().startKoth(koth, time);
    }
}
