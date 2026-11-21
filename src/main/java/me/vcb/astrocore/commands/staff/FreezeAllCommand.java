package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class FreezeAllCommand extends BaseCommand {

    public FreezeAllCommand() {
        super("freezeall", Collections.singletonList("ssall"), "astrocore.freezeall");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        AstroCore.getInstance().getFreezeHandler().toggleFreezeAll(sender);
    }
}
