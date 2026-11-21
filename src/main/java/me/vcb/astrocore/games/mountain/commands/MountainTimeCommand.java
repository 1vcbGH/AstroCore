package me.vcb.astrocore.games.mountain.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;

public class MountainTimeCommand extends SubCommand {

    MountainTimeCommand() {
        super("time", "astrocore.mountain.time");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(AstroCore.getInstance().getMountainManager().nextRespawnString());
    }
}
