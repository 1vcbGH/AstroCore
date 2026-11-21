package me.vcb.astrocore.games.dtc.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;

public class DtcStopCommand extends SubCommand {

    DtcStopCommand() {
        super("stop", "astrocore.dtc.stop");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!AstroCore.getInstance().getDtcManager().isActive()) {
            sender.sendMessage(Language.DTC_PREFIX + Language.DTC_EXCEPTION_NOT_RUNNING);
            return;
        }

        AstroCore.getInstance().getDtcManager().stopDtc(null);

        Messages.sendMessage(Language.DTC_PREFIX + Language.DTC_STOP_STOPPED
            .replace("<player>", sender.getName()));
    }
}
