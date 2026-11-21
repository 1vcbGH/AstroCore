package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LastKillsCommand extends BaseCommand {

	public LastKillsCommand() {
		super("lastkills", "astrocore.lastkills");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length == 0) {
            sender.sendMessage(Language.PREFIX + Language.LAST_KILLS_COMMAND_USAGE);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(target);
				
        if(data.getLastKills().isEmpty()) {
            sender.sendMessage(Language.PREFIX + Language.LAST_KILLS_COMMAND_NO_KILLS_YET
                .replace("<player>", target.getName()));
            return;
        }
				
        sender.sendMessage("");

        sender.sendMessage(Language.LAST_KILLS_COMMAND_HEADER_FORMAT.replace("<player>", target.getName()));

        data.getLastKills().forEach(death -> sender.sendMessage(Language.LAST_KILLS_COMMAND_KILL_MESSAGE_FORMAT
            .replace("<killMessage>", Color.translate(death))));

        sender.sendMessage("");
	}
}
