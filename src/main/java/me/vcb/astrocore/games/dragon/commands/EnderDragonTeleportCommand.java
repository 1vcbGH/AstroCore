package me.vcb.astrocore.games.dragon.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class EnderDragonTeleportCommand extends SubCommand {

    EnderDragonTeleportCommand() {
        super("teleport", Collections.singletonList("tp"), "astrocore.enderdragon.tp", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!AstroCore.getInstance().getEnderDragonManager().isActive()) {
            sender.sendMessage(Language.ENDER_DRAGON_PREFIX + Language.ENDER_DRAGON_EXCEPTION_NOT_RUNNING);
            return;
        }

        Player player = (Player) sender;
        AstroCore.getInstance().getEnderDragonManager().teleportToDragon(player);
    }
}
