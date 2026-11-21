package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /f falltrap — abre el menú para elegir tier/pared y construir la falltrap. */
public class FalltrapCommand extends SubCommand {

    public FalltrapCommand() {
        super("falltrap", "astrocore.faction.falltrap", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if ("cancel".equalsIgnoreCase(args.length > 0 ? args[0] : "")) {
            AstroCore.getInstance().getFalltrapManager().cancel(player);
            return;
        }
        AstroCore.getInstance().getFalltrapManager().openMenu(player);
    }
}
