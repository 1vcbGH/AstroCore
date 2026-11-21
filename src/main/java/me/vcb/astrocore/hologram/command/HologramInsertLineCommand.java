package me.vcb.astrocore.hologram.command;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;

public class HologramInsertLineCommand extends SubCommand {

    public HologramInsertLineCommand() {
        super("insertline", "astrocore.holograms");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 3) {
            sender.sendMessage(Language.HOLOGRAMS_PREFIX + Language.HOLOGRAMS_INSERT_LINE_USAGE);
            return;
        }

        if(!this.checkNumber(sender, args[0]) || !this.checkNumber(sender, args[1])) return;

        int hologramId = Math.abs(Integer.parseInt(args[0]));
        int lineNumber = Math.abs(Integer.parseInt(args[1]));
        String text = StringUtils.joinArray(args, " ", 3);

        AstroCore.getInstance().getHologramManager().insertHologramLine(sender, hologramId, lineNumber, text);
    }
}
