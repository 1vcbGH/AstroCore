package me.vcb.astrocore.games.dragon.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnderDragonSetSpawnCommand extends SubCommand {

    EnderDragonSetSpawnCommand() {
        super("setspawn", "astrocore.enderdragon.setspawn", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(player.getWorld().getEnvironment() != World.Environment.THE_END) {
            player.sendMessage(Language.ENDER_DRAGON_PREFIX + Language.ENDER_DRAGON_SET_SPAWN_NOT_IN_END);
            return;
        }

        AstroCore.getInstance().getEnderDragonManager().setSpawnLocation(player);

        player.sendMessage(Language.ENDER_DRAGON_PREFIX + Language.ENDER_DRAGON_SET_SPAWN_SET
        .replace("<location>", StringUtils.getLocationName(player.getLocation())));
    }
}
