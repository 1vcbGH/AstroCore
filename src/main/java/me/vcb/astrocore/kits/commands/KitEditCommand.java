package me.vcb.astrocore.kits.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.kits.kit.KitData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitEditCommand extends SubCommand {

	KitEditCommand() {
		super("edit", "astrocore.kits.edit", true);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		
		if(args.length == 0) {
            player.sendMessage(Language.KIT_PREFIX + Language.KITS_EDIT_USAGE);
            return;
		}
			
		KitData kit = AstroCore.getInstance().getKitsManager().getKit(args[0]);
			
		if(kit == null) {
		    sender.sendMessage(Language.KIT_PREFIX + Language.KITS_EXCEPTION_DOESNT_EXISTS.replace("<kit>", args[0]));
		    return;
		}
				
		AstroCore.getInstance().getKitsManager().editKit(player, kit);
	}
}
