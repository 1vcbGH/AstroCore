package me.vcb.astrocore.hologram.command;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HologramCreateCommand extends SubCommand {

    public HologramCreateCommand() {
        super("create", "astrocore.holograms", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 1) {
            sender.sendMessage(Language.HOLOGRAMS_PREFIX + Language.HOLOGRAMS_CREATE_USAGE);
            return;
        }

        Player player = (Player) sender;
        AstroCore.getInstance().getHologramManager().createHologram(player, args[0]);
    }
}
