package me.vcb.astrocore.commands;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.timer.scoreboard.LogoutTimer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LogoutCommand extends BaseCommand {

	public LogoutCommand() {
		super("logout", "astrocore.logout", true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		LogoutTimer logoutTimer = TimerManager.getInstance().getLogoutTimer();

		if(logoutTimer.isActive(player)) {
			player.sendMessage(Language.PREFIX + Language.LOGOUT_ALREADY_RUNNING);
			return;
		}

		logoutTimer.activate(player);

		player.sendMessage(Language.PREFIX + Language.LOGOUT_START_MESSAGE
			.replace("<seconds>", String.valueOf(Config.LOGOUT_DELAY)));
	}
}
