package me.vcb.astrocore.hologram.command;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;

public class HologramUpdateLineCommand extends SubCommand {

    public HologramUpdateLineCommand() {
        super("updateline", "astrocore.holograms");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 3) {
            sender.sendMessage(Language.HOLOGRAMS_PREFIX + Language.HOLOGRAMS_UPDATE_LINE_USAGE);
            return;
        }

        if(!this.checkNumber(sender, args[0]) || !this.checkNumber(sender, args[1])) return;

        int hologramId = Math.abs(Integer.parseInt(args[0]));
        int lineNumber = Math.abs(Integer.parseInt(args[1]));
        String text = StringUtils.joinArray(args, " ", 3);

        AstroCore.getInstance().getHologramManager().updateHologramLine(sender, hologramId, lineNumber, text);
    }
}
