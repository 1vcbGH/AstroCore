package me.vcb.astrocore.games.koth.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;

public class KothStopAllCommand extends SubCommand {

    KothStopAllCommand() {
        super("stopall", "astrocore.koth.stopall");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(AstroCore.getInstance().getKothManager().getRunningKoths().isEmpty()) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_STOP_ALL_NO_RUNNING_KOTHS);
            return;
        }

        AstroCore.getInstance().getKothManager().stopAllKoths();

        Messages.sendMessage(Language.KOTH_PREFIX + Language.KOTH_STOP_ALL_STOPPED_ALL
        .replace("<player>", sender.getName()));
    }
}
