package me.vcb.astrocore.games.koth.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.koth.KothData;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class KothDeleteCommand extends SubCommand {

    KothDeleteCommand() {
        super("delete", Collections.singletonList("remove"), "astrocore.koth.delete");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_REMOVE_USAGE);
            return;
        }

        KothData koth = AstroCore.getInstance().getKothManager().getKoth(args[0]);

        if(koth == null) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_EXCEPTION_DOESNT_EXIST.replace("<koth>", args[0]));
            return;
        }

        AstroCore.getInstance().getKothManager().deleteKoth(sender, koth);
        sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_REMOVE_REMOVED.replace("<koth>", koth.getName()));
    }
}
