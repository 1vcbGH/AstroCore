package me.vcb.astrocore.kits.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.kits.kit.KitData;
import me.vcb.astrocore.kits.kit.KitType;
import org.bukkit.command.CommandSender;

public class KitRemoveCommand extends SubCommand {

	KitRemoveCommand() {
		super("remove", "astrocore.kits.remove");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length == 0) {
            sender.sendMessage(Language.KIT_PREFIX + Language.KITS_REMOVE_USAGE);
			return;
		}
			
		KitData kit = AstroCore.getInstance().getKitsManager().getKit(args[0]);
			
		if(kit == null) {
		    sender.sendMessage(Language.KIT_PREFIX + Language.KITS_EXCEPTION_DOESNT_EXISTS.replace("<kit>", args[0]));
		    return;
		}

		if(kit.getType() == KitType.SPECIAL) {
			sender.sendMessage(Language.KIT_PREFIX + Language.KITS_REMOVE_CANNOT_REMOVE_SPECIAL_EVENT_KIT);
			return;
		}

		AstroCore.getInstance().getKitsManager().removeKit(kit);
		sender.sendMessage(Language.KIT_PREFIX + Language.KITS_REMOVE_REMOVED.replace("<kit>", kit.getName()));
	}
}
