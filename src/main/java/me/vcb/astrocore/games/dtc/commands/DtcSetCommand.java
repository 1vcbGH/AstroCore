package me.vcb.astrocore.games.dtc.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.dtc.DtcManager;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class DtcSetCommand extends SubCommand {

    DtcSetCommand() {
        super("set", "astrocore.dtc.set", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Block targetBlock = player.getTargetBlock(Set.of(), 5);

        if(targetBlock == null || targetBlock.getType() != Material.OBSIDIAN) {
            player.sendMessage(Language.DTC_PREFIX + Language.DTC_SET_MUST_BE_OBSIDIAN);
            return;
        }

        DtcManager dtcManager = AstroCore.getInstance().getDtcManager();

        dtcManager.getDtcData().setLocation(targetBlock.getLocation());
        if(!dtcManager.isActive()) targetBlock.setType(Material.AIR);

        player.sendMessage(Language.DTC_PREFIX + Language.DTC_SET_CORE_SET.replace("<location>",
        StringUtils.getLocationNameWithWorld(targetBlock.getLocation())));
    }
}
