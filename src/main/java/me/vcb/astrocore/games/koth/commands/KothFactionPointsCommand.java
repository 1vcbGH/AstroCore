package me.vcb.astrocore.games.koth.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.koth.KothData;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class KothFactionPointsCommand extends SubCommand {

    KothFactionPointsCommand() {
        super("factionpoints", Arrays.asList("setfactionpoints", "setpoints"), "astrocore.koth.factionpoints");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_FACTION_POINTS_USAGE);
            return;
        }

        KothData koth = AstroCore.getInstance().getKothManager().getKoth(args[0]);

        if(koth == null) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_EXCEPTION_DOESNT_EXIST.replace("<koth>", args[0]));
            return;
        }

        if(!this.checkNumber(sender, args[1])) return;

        int factionPoints = Math.abs(Integer.parseInt(args[1]));
        koth.setFactionPoints(factionPoints);

        Messages.sendMessage(Language.KOTH_PREFIX + Language.KOTH_FACTION_POINTS_CHANGED
            .replace("<koth>", koth.getName())
            .replace("<points>", String.valueOf(factionPoints)));
    }
}
