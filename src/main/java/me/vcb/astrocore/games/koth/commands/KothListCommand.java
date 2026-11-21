package me.vcb.astrocore.games.koth.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;

public class KothListCommand extends SubCommand {

    KothListCommand() {
        super("list");

        this.setExecuteAsync(true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        AstroCore.getInstance().getKothManager().listKoths(sender);
    }
}
