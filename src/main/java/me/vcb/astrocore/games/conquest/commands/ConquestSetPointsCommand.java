package me.vcb.astrocore.games.conquest.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.games.conquest.RunningConquest;
import org.bukkit.command.CommandSender;

public class ConquestSetPointsCommand extends SubCommand {

    ConquestSetPointsCommand() {
        super("setpoints", "astrocore.conquest.setpoints");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_SET_POINTS_USAGE);
            return;
        }

        RunningConquest conquest = AstroCore.getInstance().getConquestManager().getRunningConquest();

        if(conquest == null) {
            sender.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_EXCEPTION_NOT_RUNNING);
            return;
        }

        PlayerFaction faction = FactionsManager.getInstance().searchForFaction(args[0]);

        if(faction == null) {
            sender.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_SET_POINTS_INVALID_FACTION
                .replace("<argument>", args[0]));
            return;
        }

        if(!this.checkNumber(sender, args[1])) return;

        int amount = Math.min(Integer.parseInt(args[1]), Config.CONQUEST_POINTS_TO_WIN - 1);
        conquest.setPoints(faction, amount);

        sender.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_SET_POINTS_CHANGED
            .replace("<faction>", faction.getName())
            .replace("<amount>", String.valueOf(amount)));
    }
}
