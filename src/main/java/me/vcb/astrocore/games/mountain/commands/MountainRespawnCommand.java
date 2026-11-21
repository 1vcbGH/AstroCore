package me.vcb.astrocore.games.mountain.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.Placeholder;
import me.vcb.astrocore.games.mountain.MountainData;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;

public class MountainRespawnCommand extends SubCommand {

    MountainRespawnCommand() {
        super("respawn", "astrocore.mountain.respawn");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.MOUNTAIN_PREFIX + Language.MOUNTAIN_RESPAWN_USAGE);
            return;
        }

        if(!this.checkNumber(sender, args[0])) return;

        MountainData mountain = AstroCore.getInstance().getMountainManager().getMountain(Integer.parseInt(args[0]));

        if(mountain == null) {
            sender.sendMessage(Language.MOUNTAIN_PREFIX + Language.MOUNTAIN_EXCEPTION_DOESNT_EXISTS.replace("<id>", args[0]));
            return;
        }

        mountain.respawn();
        Messages.sendMessage(Placeholder.MountainReplacer.parse(mountain, Language.MOUNTAIN_RESPAWN_RESPAWNED));
    }
}
