package me.vcb.astrocore.games.mountain.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;

public class MountainListCommand extends SubCommand {

    MountainListCommand() {
        super("list");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        AstroCore.getInstance().getMountainManager().listMountains(sender);
    }
}
