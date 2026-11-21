package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class TeleportCommand extends BaseCommand {

    public TeleportCommand() {
        super("teleport", Collections.singletonList("tp"), "astrocore.teleport", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 0) {
            player.sendMessage(Language.PREFIX + Language.TELEPORT_USAGE);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        if(player.teleport(target)) {
            player.sendMessage(Language.PREFIX + Language.TELEPORT_MESSAGE.replace("<player>", target.getName()));
        }
    }
}
