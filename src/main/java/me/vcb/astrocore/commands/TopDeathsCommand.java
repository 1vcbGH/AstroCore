package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.handlers.leaderboard.LeaderboardHandler;
import me.vcb.astrocore.handlers.leaderboard.type.PlayerLeaderboardType;
import org.bukkit.command.CommandSender;

public class TopDeathsCommand extends BaseCommand {

    public TopDeathsCommand() {
        super("topdeaths", "astrocore.topdeaths");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        LeaderboardHandler handler = AstroCore.getInstance().getLeaderboardHandler();
        handler.sendLeaderboardMessage(sender, PlayerLeaderboardType.DEATHS);
    }
}
