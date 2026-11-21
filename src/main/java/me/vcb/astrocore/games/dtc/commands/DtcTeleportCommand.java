package me.vcb.astrocore.games.dtc.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.dtc.DtcData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class DtcTeleportCommand extends SubCommand {

    DtcTeleportCommand() {
        super("teleport", Collections.singletonList("tp"), "astrocore.dtc.teleport", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        DtcData dtcData = AstroCore.getInstance().getDtcManager().getDtcData();

        if(dtcData.getLocation() == null) {
            sender.sendMessage(Language.DTC_PREFIX + Language.DTC_TELEPORT_CORE_NOT_SET);
            return;
        }

        Player player = (Player) sender;
        player.teleport(dtcData.getLocation().clone().add(0.5, 2, 0.5));
    }
}
