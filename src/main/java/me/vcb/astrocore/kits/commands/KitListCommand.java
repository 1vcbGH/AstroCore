package me.vcb.astrocore.kits.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;

public class KitListCommand extends SubCommand {

	KitListCommand() {
		super("list", "astrocore.kits.list");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		AstroCore.getInstance().getKitsManager().listKits(sender);
	}
}
