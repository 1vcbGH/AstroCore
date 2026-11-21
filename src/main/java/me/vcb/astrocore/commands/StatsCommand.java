package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class StatsCommand extends BaseCommand {

    public StatsCommand() {
        super("stats", Collections.singletonList("ores"), "astrocore.stats", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 0) {
            AstroCore.getInstance().getStatsHandler().openStatsInventory(player, player);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        AstroCore.getInstance().getStatsHandler().openStatsInventory(player, target);
    }
}
