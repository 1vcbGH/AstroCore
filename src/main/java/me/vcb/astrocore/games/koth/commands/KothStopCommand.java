package me.vcb.astrocore.games.koth.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.koth.KothData;
import me.vcb.astrocore.games.koth.RunningKoth;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;

public class KothStopCommand extends SubCommand {

    KothStopCommand() {
        super("stop", "astrocore.koth.stop");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_STOP_USAGE);
            return;
        }

        KothData koth = AstroCore.getInstance().getKothManager().getKoth(args[0]);

        if(koth == null) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_EXCEPTION_DOESNT_EXIST.replace("<koth>", args[0]));
            return;
        }

        RunningKoth runningKoth = AstroCore.getInstance().getKothManager().getRunningKoth(args[0]);

        if(runningKoth == null) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_EXCEPTION_NOT_RUNNING.replace("<koth>", koth.getName()));
            return;
        }

        AstroCore.getInstance().getKothManager().stopKoth(runningKoth);

        Messages.sendMessage(Language.KOTH_PREFIX + Language.KOTH_STOP_STOPPED
            .replace("<player>", sender.getName()).replace("<koth>", koth.getName()));
    }
}
