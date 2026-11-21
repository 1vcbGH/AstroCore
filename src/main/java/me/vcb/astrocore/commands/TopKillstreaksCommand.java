package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.leaderboard.LeaderboardHandler;
import me.vcb.astrocore.handlers.leaderboard.type.PlayerLeaderboardType;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class TopKillstreaksCommand extends BaseCommand {

    public TopKillstreaksCommand() {
        super("topkillstreak", Collections.singletonList("topkillstreaks"), "astrocore.topkillstreak");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!Config.KITMAP_MODE_ENABLED) {
            sender.sendMessage(Language.PREFIX + Language.LEADERBOARDS_KITMAP_MODE_ONLY);
            return;
        }

        LeaderboardHandler handler = AstroCore.getInstance().getLeaderboardHandler();
        handler.sendLeaderboardMessage(sender, PlayerLeaderboardType.HIGHEST_KILLSTREAK);
    }
}
