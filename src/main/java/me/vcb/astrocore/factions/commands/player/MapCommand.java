package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.factions.claim.ClaimManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapCommand extends SubCommand {

    public MapCommand() {
        super("map", true);

        this.setExecuteAsync(true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ClaimManager.getInstance().getFactionMap().showFactionMap(player);
    }
}
