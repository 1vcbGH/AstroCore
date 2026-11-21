package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /f skybase — abre el menú para construir una base aérea a +25 con elevador. */
public class SkyBaseCommand extends SubCommand {

    public SkyBaseCommand() {
        super("skybase", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        AstroCore.getInstance().getSkyBaseManager().openMenu(player);
    }
}
