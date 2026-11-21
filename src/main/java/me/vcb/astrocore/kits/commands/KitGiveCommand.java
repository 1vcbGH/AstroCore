package me.vcb.astrocore.kits.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.kits.kit.KitData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitGiveCommand extends SubCommand {

	KitGiveCommand() {
		super("give", "astrocore.kits.give");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length < 2) {
            sender.sendMessage(Language.KIT_PREFIX + Language.KITS_GIVE_USAGE);
            return;
		}

		Player target = Bukkit.getPlayer(args[0]);
		if(!this.checkPlayer(sender, target, args[0])) return;
				
		KitData kit = AstroCore.getInstance().getKitsManager().getKit(args[1]);
				
		if(kit == null) {
		    sender.sendMessage(Language.KIT_PREFIX + Language.KITS_EXCEPTION_DOESNT_EXISTS.replace("<kit>", args[1]));
		    return;
		}

		AstroCore.getInstance().getKitsManager().giveKitWithCommand(sender, target, kit);
	}
}
