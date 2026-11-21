package me.vcb.astrocore.hologram.command;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class HologramTeleportHereCommand extends SubCommand {

    public HologramTeleportHereCommand() {
        super("teleporthere", Collections.singletonList("tphere"), "astrocore.holograms", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(this.prefix + Language.HOLOGRAMS_TELEPORT_HERE_USAGE);
            return;
        }

        if(!this.checkNumber(sender, args[0])) return;

        Player player = (Player) sender;
        int hologramId = Integer.parseInt(args[0]);

        AstroCore.getInstance().getHologramManager().teleportHologram(player, hologramId);
    }
}
