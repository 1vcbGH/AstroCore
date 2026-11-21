package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ToggleScoreboardCommand extends BaseCommand {

    public ToggleScoreboardCommand() {
        super("togglescoreboard", Arrays.asList("togglesb", "tscoreboard"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        data.getSettings().setScoreboard(!data.getSettings().isScoreboard());

        player.sendMessage(Language.PREFIX + (data.getSettings().isScoreboard()
            ? Language.TOGGLE_SCOREBOARD_TOGGLED_ON : Language.TOGGLE_SCOREBOARD_TOGGLED_OFF));
    }
}
