package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWarpCommand extends BaseCommand {

    public SetWarpCommand() {
        super("setwarp", "astrocore.setwarp", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.PREFIX + Language.SET_WARP_USAGE);
            return;
        }

        Player player = (Player) sender;

        if(!AstroCore.getInstance().getWarpsHandler().addWarp(args[0], player.getLocation())) {
            player.sendMessage(Language.PREFIX + Language.SET_WARP_ALREADY_EXIST.replace("<name>", args[0]));
            return;
        }

        String worldName = StringUtils.getWorldName(player.getWorld());
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        player.sendMessage(Language.PREFIX + Language.SET_WARP_CREATED
            .replace("<name>", args[0])
            .replace("<world>", worldName)
            .replace("<x>", String.valueOf(x))
            .replace("<y>", String.valueOf(y))
            .replace("<z>", String.valueOf(z)));
    }
}
