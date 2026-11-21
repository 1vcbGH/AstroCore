package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class DeathbanCommand extends BaseCommand {

    public DeathbanCommand() {
        super("deathban", "astrocore.deathban");

        this.setExecuteAsync(true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2) {
            Language.DEATHBAN_COMMAND_USAGE.forEach(sender::sendMessage);
            return;
        }

        if(args[0].equalsIgnoreCase("check")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if(!this.checkOfflinePlayer(sender, target, args[1])) return;

            AstroCore.getInstance().getDeathbanManager().checkStatus(sender, target);
            return;
        }

        if(args[0].equalsIgnoreCase("revive")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if(!this.checkOfflinePlayer(sender, target, args[1])) return;

            AstroCore.getInstance().getDeathbanManager().revivePlayer(sender, target);
            return;
        }

        Language.DEATHBAN_COMMAND_USAGE.forEach(sender::sendMessage);
    }
}
