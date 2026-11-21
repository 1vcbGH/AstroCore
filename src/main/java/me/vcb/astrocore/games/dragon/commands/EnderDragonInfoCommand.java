package me.vcb.astrocore.games.dragon.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class EnderDragonInfoCommand extends SubCommand {

    EnderDragonInfoCommand() {
        super("info");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Location dragonSpawn = AstroCore.getInstance().getEnderDragonManager().getSpawnLocation();

        Language.ENDER_DRAGON_INFO_MESSAGE.forEach(line -> sender.sendMessage(line
        .replace("<health>", String.valueOf(Config.ENDER_DRAGON_HEALTH))
        .replace("<location>", dragonSpawn == null ? Language.ENDER_DRAGON_SPAWN_NOT_SET
        : StringUtils.getLocationNameWithWorld(dragonSpawn))));
    }
}
