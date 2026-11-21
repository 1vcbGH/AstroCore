package me.vcb.astrocore.games.dtc.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;

public class DtcStartCommand extends SubCommand {

    DtcStartCommand() {
        super("start", "astrocore.dtc.start");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(AstroCore.getInstance().getDtcManager().isActive()) {
            sender.sendMessage(Language.DTC_PREFIX + Language.DTC_EXCEPTION_ALREADY_RUNNING);
            return;
        }

        if(args.length == 0) {
            AstroCore.getInstance().getDtcManager().startDtc(sender, Config.DTC_CORE_BREAKS);
            return;
        }

        if(!this.checkNumber(sender, args[0])) return;

        AstroCore.getInstance().getDtcManager().startDtc(sender, Integer.parseInt(args[0]));
    }
}
