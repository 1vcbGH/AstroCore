package me.vcb.astrocore.games.conquest.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.Placeholder;
import me.vcb.astrocore.games.conquest.ConquestData;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public class ConquestInfoCommand extends SubCommand {

    ConquestInfoCommand() {
        super("info");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ConquestData conquest = AstroCore.getInstance().getConquestManager().getConquest();

        if(conquest.getCuboids().values().stream().anyMatch(Objects::isNull)) {
            sender.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_INFO_NOT_SETUP);
            return;
        }

        Language.CONQUEST_INFO_MESSAGE.forEach(line -> sender.sendMessage(Placeholder
        .ConquestReplacer.parse(conquest, line)));
    }
}
