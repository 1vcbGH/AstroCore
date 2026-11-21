package me.vcb.astrocore.games.dtc.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.dtc.DtcData;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;

public class DtcSetHealthCommand extends SubCommand {

    public DtcSetHealthCommand() {
        super("sethealth", "astrocore.dtc.sethealth");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.DTC_PREFIX + Language.DTC_SET_HEALTH_USAGE);
            return;
        }

        if(!AstroCore.getInstance().getDtcManager().isActive()) {
            sender.sendMessage(Language.DTC_PREFIX + Language.DTC_EXCEPTION_NOT_RUNNING);
            return;
        }

        if(!this.checkNumber(sender, args[0])) return;

        int newHealth = Math.max(1, Math.abs(Integer.parseInt(args[0])));

        DtcData dtcData = AstroCore.getInstance().getDtcManager().getDtcData();
        dtcData.setBreaksLeft(newHealth);

        Messages.sendMessage(Language.DTC_PREFIX + Language.DTC_SET_HEALTH_CHANGED
            .replace("<health>", String.valueOf(newHealth)));
    }
}
