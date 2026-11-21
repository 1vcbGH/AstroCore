package me.vcb.astrocore.games.dtc.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.dtc.DtcData;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;

public class DtcInfoCommand extends SubCommand {

    DtcInfoCommand() {
        super("info");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        DtcData dtcData = AstroCore.getInstance().getDtcManager().getDtcData();

        if(dtcData.getLocation() == null) {
            sender.sendMessage(Language.DTC_PREFIX + Language.DTC_INFO_NOT_SETUP);
            return;
        }

        Language.DTC_INFO_MESSAGE.forEach(line -> sender.sendMessage(line
        .replace("<location>", StringUtils.getLocationNameWithWorld(dtcData.getLocation()))
        .replace("<amount>", String.valueOf(Config.DTC_CORE_BREAKS))));
    }
}
