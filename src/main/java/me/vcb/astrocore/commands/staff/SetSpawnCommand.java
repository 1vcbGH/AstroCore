package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.event.SpawnSetEvent;
import me.vcb.astrocore.utils.LocationUtils;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand extends BaseCommand {

    public SetSpawnCommand() {
        super("setspawn", "astrocore.setspawn", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 0) {
            Language.SET_SPAWN_USAGE.forEach(player::sendMessage);
            return;
        }

        switch(args[0].toLowerCase()) {
            case "world": {
                this.setSpawn(player, Environment.NORMAL, "WORLD");
                return;
            }
            case "nether": {
                this.setSpawn(player, Environment.NETHER, "NETHER");
                return;
            }
            case "end": {
                this.setSpawn(player, Environment.THE_END, "END");
                return;
            }
            default: Language.SET_SPAWN_USAGE.forEach(player::sendMessage);
        }
    }

    private void setSpawn(Player player, Environment environment, String world) {
        if(player.getWorld().getEnvironment() != environment) {
            player.sendMessage(Language.PREFIX + Language.SET_SPAWN_NOT_IN_WORLD
                .replace("<world>", StringUtils.capitalize(world.toLowerCase()))
                .replace("<world>", world.toLowerCase()));
            return;
        }

        Location location = player.getLocation();

        Config.WORLD_SPAWNS.put(environment, location);

        new SpawnSetEvent(player, environment, location);

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        player.sendMessage(Language.PREFIX + Language.SET_SPAWN_SPAWN_SET
            .replace("<world>", StringUtils.capitalize(world.toLowerCase()))
            .replace("<x>", String.valueOf(x))
            .replace("<y>", String.valueOf(y))
            .replace("<z>", String.valueOf(z)));

        AstroCore.getInstance().getUtilitiesFile().set(world + "_SPAWN", LocationUtils.locationToString(location));
        AstroCore.getInstance().getUtilitiesFile().save();
    }
}
