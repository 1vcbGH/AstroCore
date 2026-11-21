package me.vcb.astrocore.games.conquest.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.Cuboid;
import me.vcb.astrocore.games.conquest.ZoneType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class ConquestTeleportCommand extends SubCommand {

    ConquestTeleportCommand() {
        super("teleport", Collections.singletonList("tp"), "astrocore.conquest.teleport", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_TELEPORT_USAGE);
            return;
        }

        ZoneType type;

        try {
            type = ZoneType.valueOf(args[0].toUpperCase());
        } catch(IllegalArgumentException e) {
            sender.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_EXCEPTION_INVALID_ZONE);
            return;
        }

        Cuboid cuboid = AstroCore.getInstance().getConquestManager().getConquest().getCuboid(type);

        if(cuboid == null) {
            sender.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_TELEPORT_ZONE_NOT_SET
            .replace("<zone>", type.getName()));
            return;
        }

        Player player = (Player) sender;
        player.teleport(cuboid.getCenter());
    }
}
