package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapkitCommand extends BaseCommand {

	public MapkitCommand() {
		super("mapkit", "astrocore.mapkit", true);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;

		if(args.length == 1 && args[0].equalsIgnoreCase("edit")) {
			if(!player.hasPermission("astrocore.mapkit.edit")) {
				player.sendMessage(Language.PREFIX + Language.COMMANDS_NO_PERMISSION);
				return;
			}

			AstroCore.getInstance().getMapkitHandler().setEditingMapkit(player);
		}

        player.openInventory(AstroCore.getInstance().getMapkitHandler().getMapkitInv());
	}
}
