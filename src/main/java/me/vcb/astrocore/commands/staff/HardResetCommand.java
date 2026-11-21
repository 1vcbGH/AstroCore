package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.timer.TimerManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.ArrayList;
import java.util.List;

public class HardResetCommand extends BaseCommand {

    private final String[] TAB_COMPLETIONS = new String[] {
        "playerfactions", "factionpoints", "userdata", "timers", "deathbans", "leaderboards", "all"
    };

    public HardResetCommand() {
        super("hardreset", "astrocore.hardreset");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(Language.PREFIX + Language.COMMANDS_FOR_CONSOLE_ONLY);
            return;
        }

        if(args.length == 0) {
            Language.HARD_RESET_COMMAND_USAGE.forEach(sender::sendMessage);
            return;
        }

        switch(args[0].toLowerCase()) {
            case "playerfactions": {
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                ClaimManager.getInstance().deleteAllPlayerFactionClaims();
                FactionsManager.getInstance().deleteAllPlayerFactions();
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                return;
            }
            case "factionpoints": {
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                FactionsManager.getInstance().resetPlayerFactionPoints();
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                return;
            }
            case "userdata": {
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                AstroCore.getInstance().getUserdataManager().deleteAllUserdata();
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                return;
            }
            case "timers": {
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                TimerManager.getInstance().deleteAllTimers();
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                return;
            }
            case "deathbans": {
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                AstroCore.getInstance().getDeathbanManager().deleteAllDeathbans(true);
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                return;
            }
            case "leaderboards": {
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                AstroCore.getInstance().getLeaderboardHandler().deleteAllLeaderboards();
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                return;
            }
            case "all": {
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                ClaimManager.getInstance().deleteAllPlayerFactionClaims();
                FactionsManager.getInstance().deleteAllPlayerFactions();
                AstroCore.getInstance().getUserdataManager().deleteAllUserdata();
                AstroCore.getInstance().getDeathbanManager().deleteAllDeathbans(true);
                AstroCore.getInstance().getLeaderboardHandler().deleteAllLeaderboards();
                TimerManager.getInstance().deleteAllTimers();
                AstroCore.getInstance().log("&4===&c=============================================&4===");
                return;
            }
            default: {
                Language.HARD_RESET_COMMAND_USAGE.forEach(sender::sendMessage);
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        if(args.length != 1 || !(sender instanceof ConsoleCommandSender)) return null;

        List<String> completions = new ArrayList<>();

        for(String resetType : TAB_COMPLETIONS) {
            if(!resetType.startsWith(args[0].toLowerCase())) continue;

            completions.add(resetType);
        }

        return completions;
    }
}
