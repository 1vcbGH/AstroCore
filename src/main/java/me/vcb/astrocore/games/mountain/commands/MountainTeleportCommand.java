package me.vcb.astrocore.games.mountain.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.mountain.MountainData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class MountainTeleportCommand extends SubCommand {

    MountainTeleportCommand() {
        super("teleport", Collections.singletonList("tp"), "astrocore.mountain.tp", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.MOUNTAIN_PREFIX + Language.MOUNTAIN_TELEPORT_USAGE);
            return;
        }

        if(!this.checkNumber(sender, args[0])) return;

        MountainData mountain = AstroCore.getInstance().getMountainManager().getMountain(Integer.parseInt(args[0]));

        if(mountain == null) {
            sender.sendMessage(Language.MOUNTAIN_PREFIX + Language.MOUNTAIN_EXCEPTION_DOESNT_EXISTS.replace("<id>", args[0]));
            return;
        }

        Player player = (Player) sender;
        player.teleport(mountain.getCuboid().getCenter());
    }
}
