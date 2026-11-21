package me.vcb.astrocore.games.conquest.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;

public class ConquestStartCommand extends SubCommand {

    ConquestStartCommand() {
        super("start", "astrocore.conquest.start");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(AstroCore.getInstance().getConquestManager().isActive()) {
            sender.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_START_ALREADY_RUNNING);
            return;
        }

        AstroCore.getInstance().getConquestManager().startConquest(sender);
    }
}
