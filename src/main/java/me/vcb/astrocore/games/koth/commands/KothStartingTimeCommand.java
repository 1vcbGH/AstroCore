package me.vcb.astrocore.games.koth.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.koth.KothData;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;

public class KothStartingTimeCommand extends SubCommand {

    KothStartingTimeCommand() {
        super("startingtime", "astrocore.koth.startingtime");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_STARTING_TIME_USAGE);
            return;
        }

        KothData koth = AstroCore.getInstance().getKothManager().getKoth(args[0]);

        if(koth == null) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_EXCEPTION_DOESNT_EXIST.replace("<koth>", args[0]));
            return;
        }

        int duration = StringUtils.parseSeconds(args[1]);

        if(duration == -1) {
            sender.sendMessage(Language.PREFIX + Language.COMMANDS_INVALID_DURATION);
            return;
        }

        koth.setCaptime(duration);

        sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_STARTING_TIME_CHANGED
            .replace("<koth>", koth.getName())
            .replace("<time>", StringUtils.formatTime(duration, StringUtils.FormatType.SECONDS_TO_MINUTES)));
    }
}