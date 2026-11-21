package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LastDeathsCommand extends BaseCommand {

	public LastDeathsCommand() {
		super("lastdeaths", "astrocore.lastdeaths");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length == 0) {
            sender.sendMessage(Language.PREFIX + Language.LAST_DEATHS_USAGE);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(target);
				
        if(data.getLastDeaths().isEmpty()) {
            sender.sendMessage(Language.PREFIX + Language.LAST_DEATHS_NO_DEATHS_YET
                .replace("<player>", target.getName()));
            return;
        }
				
        sender.sendMessage("");

        sender.sendMessage(Language.LAST_DEATHS_HEADER_FORMAT.replace("<player>", target.getName()));

        data.getLastDeaths().forEach(death -> sender.sendMessage(Language.LAST_DEATHS_DEATH_MESSAGE_FORMAT
            .replace("<deathMessage>", Color.translate(death))));

        sender.sendMessage("");
	}
}
