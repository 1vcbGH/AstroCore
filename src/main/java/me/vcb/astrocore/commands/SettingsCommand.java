package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettingsCommand extends BaseCommand {

    public SettingsCommand() {
        super("settings", "astrocore.settings", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        player.openInventory(AstroCore.getInstance().getSettingsHandler().getSettingsInventory(player));
    }
}
