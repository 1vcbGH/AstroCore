package me.vcb.astrocore.kits.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.kits.kit.KitData;
import me.vcb.astrocore.kits.kit.KitType;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCreateCommand extends SubCommand {

	KitCreateCommand() {
		super("create", "astrocore.kits.create", true);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		
		if(args.length < 3) {
            player.sendMessage(Language.KIT_PREFIX + Language.KITS_CREATE_USAGE);
		    return;
		}
			
		KitData kit = AstroCore.getInstance().getKitsManager().getKit(args[1]);

		if(kit != null) {
            player.sendMessage(Language.KIT_PREFIX + Language.KITS_EXCEPTION_ALREADY_EXISTS.replace("<kit>", kit.getName()));
            return;
		}

		KitType kitType = KitType.fromName(args[1].toUpperCase());

		if(kitType == null || kitType == KitType.SPECIAL) {
			player.sendMessage(Language.KIT_PREFIX + Language.KITS_CREATE_INVALID_KIT_TYPE.replace("<type>", args[1]));
			return;
		}

		Integer delay = StringUtils.tryParseInteger(args[2]);

		if(delay == null || delay != -1) {
			delay = StringUtils.parseSeconds(args[2]);

			if(delay == -1) {
				sender.sendMessage(Language.KIT_PREFIX + Language.COMMANDS_INVALID_DURATION);
				return;
			}
		}

		AstroCore.getInstance().getKitsManager().createKit(args[0], kitType, delay);
		player.sendMessage(Language.KIT_PREFIX + Language.KITS_CREATE_CREATED.replace("<kit>", args[0]));
	}
}
