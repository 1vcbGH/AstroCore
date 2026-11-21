package me.vcb.astrocore.hologram.command;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;

public class HologramListCommand extends SubCommand {

    public HologramListCommand() {
        super("list", "astrocore.holograms");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        AstroCore.getInstance().getHologramManager().listAllHolograms(sender);
    }
}
