package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class RandomTeleportCommand extends BaseCommand {

    public RandomTeleportCommand() {
        super("randomteleport", Arrays.asList("randomtp", "rtp"), "astrocore.randomteleport", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        AstroCore.getInstance().getStaffModeManager().randomTeleport(player);
    }
}

