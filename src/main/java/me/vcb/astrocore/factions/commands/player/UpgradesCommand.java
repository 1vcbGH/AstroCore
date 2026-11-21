package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.factions.upgrades.FactionUpgradesGui;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /f upgrades — menú de mejoras de facción. */
public class UpgradesCommand extends SubCommand {

    public UpgradesCommand() {
        super("upgrades", java.util.List.of("mejoras"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        FactionUpgradesGui.getInstance().open((Player) sender);
    }
}
