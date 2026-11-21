package me.vcb.astrocore.games.loot.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;

public class LootListCommand extends SubCommand {

    LootListCommand() {
        super("list");

        this.setExecuteAsync(true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        AstroCore.getInstance().getLootManager().listLoots(sender);
    }
}
