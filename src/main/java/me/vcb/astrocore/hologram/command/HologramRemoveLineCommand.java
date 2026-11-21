package me.vcb.astrocore.hologram.command;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;

public class HologramRemoveLineCommand extends SubCommand {

    public HologramRemoveLineCommand() {
        super("removeline", "astrocore.holograms");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage(Language.HOLOGRAMS_PREFIX + Language.HOLOGRAMS_REMOVE_LINE_USAGE);
            return;
        }

        if(!this.checkNumber(sender, args[0]) || !this.checkNumber(sender, args[1])) return;

        int hologramId = Math.abs(Integer.parseInt(args[0]));
        int lineNumber = Math.abs(Integer.parseInt(args[1]));

        AstroCore.getInstance().getHologramManager().removeHologramLine(sender, hologramId, lineNumber);
    }
}
