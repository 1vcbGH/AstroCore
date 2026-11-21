package me.vcb.astrocore.hologram.command;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class HologramDeleteCommand extends SubCommand {

    public HologramDeleteCommand() {
        super("delete", Collections.singletonList("remove"), "astrocore.holograms");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(this.prefix + Language.HOLOGRAMS_DELETE_USAGE);
            return;
        }

        if(!this.checkNumber(sender, args[0])) return;

        int hologramId = Integer.parseInt(args[0]);
        AstroCore.getInstance().getHologramManager().deleteHologram(sender, hologramId);
    }
}
