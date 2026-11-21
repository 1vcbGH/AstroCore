package me.vcb.astrocore.hologram.command;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;

public class HologramAddLineCommand extends SubCommand {

    public HologramAddLineCommand() {
        super("addline", "astrocore.holograms");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage(Language.HOLOGRAMS_PREFIX + Language.HOLOGRAMS_ADD_LINE_USAGE);
            return;
        }

        if(!this.checkNumber(sender, args[0])) return;

        int hologramId = Math.abs(Integer.parseInt(args[0]));
        String text = StringUtils.joinArray(args, " ", 2);

        AstroCore.getInstance().getHologramManager().addHologramLine(sender, hologramId, text);
    }
}
