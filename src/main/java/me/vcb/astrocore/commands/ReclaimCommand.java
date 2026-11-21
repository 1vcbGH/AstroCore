package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReclaimCommand extends BaseCommand {

    public ReclaimCommand() {
        super("reclaim", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        AstroCore.getInstance().getReclaimHandler().performCommand(player);
    }
}
