package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.timer.scoreboard.SotwTimer;
import me.vcb.astrocore.utils.StringUtils;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SotwCommand extends BaseCommand {

	public SotwCommand() {
		super("sotw");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length == 0) {
			(sender.hasPermission("astrocore.sotw")
				? Language.SOTW_ADMIN_COMMAND_USAGE
				: Language.SOTW_PLAYER_COMMAND_USAGE).forEach(sender::sendMessage);
			return;
		}

		if(args.length == 1) {
			switch(args[0].toLowerCase()) {
				case "start": {
					if(!this.checkPermission(sender, "astrocore.sotw")) return;
					AstroCore.getInstance().getSotwHandler().startSotwTimer(sender, Config.SOTW_DEFAULT_TIME * 60);
					return;
				}
				case "stop": {
					if(!this.checkPermission(sender, "astrocore.sotw")) return;
					AstroCore.getInstance().getSotwHandler().stopSotwTimer(sender);
					return;
				}
				case "hideplayers": {
					if(!this.checkPermission(sender, "astrocore.sotw")) return;
					Tasks.async(() -> AstroCore.getInstance().getSotwHandler().toggleSotwInvisibility(sender, null));
					return;
				}
				case "enable": {
					if(!this.checkConsoleSender(sender) || !this.checkPermission(sender, "astrocore.sotw.enable")) return;
					AstroCore.getInstance().getSotwHandler().enableSotwForPlayer((Player) sender);
					return;
				}
				case "time": {
					SotwTimer sotwTimer = TimerManager.getInstance().getSotwTimer();

					if(!sotwTimer.isActive()) {
						sender.sendMessage(Language.PREFIX + Language.SOTW_NOT_RUNNING);
						return;
					}

					sender.sendMessage(Language.PREFIX + Language.SOTW_TIME_STATUS
						.replace("<time>", sotwTimer.getDynamicTimeLeft()));
					return;
				}
				default: {
					(sender.hasPermission("astrocore.sotw")
						? Language.SOTW_ADMIN_COMMAND_USAGE
						: Language.SOTW_PLAYER_COMMAND_USAGE).forEach(sender::sendMessage);
				}
			}
		} else {
			if(!this.checkPermission(sender, "astrocore.sotw")) return;

			if(args[0].equalsIgnoreCase("start")) {
				int time = StringUtils.parseSeconds(args[1]);

				if(time == -1) {
					sender.sendMessage(Language.KIT_PREFIX + Language.COMMANDS_INVALID_DURATION);
					return;
				}

				AstroCore.getInstance().getSotwHandler().startSotwTimer(sender, time);
			} else {
				Language.SOTW_ADMIN_COMMAND_USAGE.forEach(sender::sendMessage);
			}
		}
	}
}
