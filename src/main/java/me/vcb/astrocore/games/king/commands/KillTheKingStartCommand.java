package me.vcb.astrocore.games.king.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KillTheKingStartCommand extends SubCommand {

    KillTheKingStartCommand() {
        super("start", "astrocore.killtheking.start");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.KING_PREFIX + Language.KING_START_USAGE);
            return;
        }

        if(AstroCore.getInstance().getKillTheKingManager().isActive()) {
            sender.sendMessage(Language.KING_PREFIX + Language.KING_EXCEPTION_ALREADY_RUNNING);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        AstroCore.getInstance().getKillTheKingManager().startKillTheKing(target);
    }
}
