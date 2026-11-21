package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.event.ExitSetEvent;
import me.vcb.astrocore.utils.LocationUtils;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetExitCommand extends BaseCommand {

    public SetExitCommand() {
        super("setexit", "astrocore.setexit", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 0) {
            Language.SET_EXIT_USAGE.forEach(player::sendMessage);
            return;
        }

        switch(args[0].toLowerCase()) {
            case "nether":
                this.setExit(player, Environment.NETHER, "NETHER");
                return;
            case "end": {
                this.setExit(player, Environment.THE_END, "END");
                return;
            }
            default: Language.SET_EXIT_USAGE.forEach(player::sendMessage);
        }
    }

    private void setExit(Player player, Environment environment, String world) {
        Location location = player.getLocation();
        Config.WORLD_EXITS.put(environment, location);

        new ExitSetEvent(player, environment, location);

        String worldName = StringUtils.getWorldName(player.getWorld());
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        player.sendMessage(Language.PREFIX + Language.SET_EXIT_EXIT_SET
            .replace("<worldName>", StringUtils.capitalize(world.toLowerCase()))
            .replace("<world>", worldName)
            .replace("<x>", String.valueOf(x))
            .replace("<y>", String.valueOf(y))
            .replace("<z>", String.valueOf(z)));

        AstroCore.getInstance().getUtilitiesFile().set(world + "_EXIT", LocationUtils.locationToString(location));
        AstroCore.getInstance().getUtilitiesFile().save();
    }
}
