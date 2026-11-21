package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TopCommand extends BaseCommand {

    public TopCommand() {
        super("top", "astrocore.top", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Location location = player.getWorld().getHighestBlockAt(player.getLocation()).getLocation().add(0.5, 0, 0.5);
        location.setPitch(player.getLocation().getPitch());
        location.setYaw(player.getLocation().getYaw());

        if(player.teleport(location)) {
            player.sendMessage(Language.PREFIX + Language.TOP_COMMAND_MESSAGE);
        }
    }
}
