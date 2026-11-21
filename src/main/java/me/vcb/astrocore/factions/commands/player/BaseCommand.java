package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /f base — abre el menú para elegir color de lana y construir una base sencilla. */
public class BaseCommand extends SubCommand {

    public BaseCommand() {
        super("base", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        AstroCore.getInstance().getBaseManager().openMenu(player);
    }
}
