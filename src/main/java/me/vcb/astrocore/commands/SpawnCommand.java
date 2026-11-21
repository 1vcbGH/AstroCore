package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.SpawnTeleportHandler;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends BaseCommand {

    public SpawnCommand() {
        super("spawn", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(player.hasPermission("astrocore.spawn")) {
            this.teleportStaffToSpawn(player, args);
            return;
        }

        SpawnTeleportHandler handler = AstroCore.getInstance().getSpawnTeleportHandler();

        if(Config.KITMAP_MODE_ENABLED) {
            handler.startKitmapSpawnTeleport(player);
        } else {
            handler.startSpawnCreditsTeleport(player);
        }
    }

    private void teleportStaffToSpawn(Player player, String[] args) {
        Environment environment = this.getWorldEnvironment(args);
        String worldName = StringUtils.getWorldName(environment);
        Location spawn = Config.WORLD_SPAWNS.get(environment);

        if(spawn == null) {
            player.sendMessage(Language.PREFIX + Language.SPAWN_DOESNT_EXIST.replace("<world>", worldName));
            return;
        }

        if(player.teleport(spawn)) {
            player.sendMessage(Language.PREFIX + Language.SPAWN_TELEPORTED.replace("<world>", worldName));
        }
    }

    private Environment getWorldEnvironment(String[] args) {
        if(args.length == 0) {
            return Environment.NORMAL;
        }

        switch(args[0].toLowerCase()) {
            case "nether": return Environment.NETHER;
            case "end": return Environment.THE_END;
            default: return Environment.NORMAL;
        }
    }
}
