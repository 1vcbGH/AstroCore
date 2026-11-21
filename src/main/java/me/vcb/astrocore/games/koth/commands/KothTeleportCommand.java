package me.vcb.astrocore.games.koth.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.koth.KothData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class KothTeleportCommand extends SubCommand {

    KothTeleportCommand() {
        super("teleport", Collections.singletonList("tp"), "astrocore.koth.teleport", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_TELEPORT_USAGE);
            return;
        }

        KothData koth = AstroCore.getInstance().getKothManager().getKoth(args[0]);

        if(koth == null) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_EXCEPTION_DOESNT_EXIST.replace("<koth>", args[0]));
            return;
        }

        Player player = (Player) sender;
        player.teleport(koth.getCuboid().getCenter());
    }
}
