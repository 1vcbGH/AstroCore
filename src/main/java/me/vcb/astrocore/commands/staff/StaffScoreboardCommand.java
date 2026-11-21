package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.scoreboard.ScoreboardManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class StaffScoreboardCommand extends BaseCommand {

	public StaffScoreboardCommand() {
		super("staffscoreboard", Collections.singletonList("staffsb"), "astrocore.staffscoreboard", true);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		ScoreboardManager.getInstance().toggleStaffScoreboard(player);
	}
}
