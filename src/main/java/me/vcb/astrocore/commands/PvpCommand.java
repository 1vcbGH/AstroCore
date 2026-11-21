package me.vcb.astrocore.commands;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.timer.scoreboard.PvpProtTimer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvpCommand extends BaseCommand {

    public PvpCommand() {
        super("pvp", "astrocore.pvp", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            Language.PVP_COMMAND_USAGE.forEach(sender::sendMessage);
            return;
        }

        Player player = (Player) sender;

        switch(args[0].toLowerCase()) {
            case "enable": {
                PvpProtTimer timer = TimerManager.getInstance().getPvpProtTimer();

                if(!timer.isActive(player)) {
                    player.sendMessage(Language.PREFIX + Language.PVP_COMMAND_NOT_ACTIVE);
                    return;
                }

                timer.cancel(player);
                player.sendMessage(Language.PREFIX + Language.PVP_COMMAND_PROTECTION_DISABLED);
                return;
            }
            case "time": {
                PvpProtTimer timer = TimerManager.getInstance().getPvpProtTimer();

                if(!timer.isActive(player)) {
                    player.sendMessage(Language.PREFIX + Language.PVP_COMMAND_NOT_ACTIVE);
                    return;
                }

                player.sendMessage(Language.PREFIX + Language.PVP_COMMAND_TIME_STATUS
                    .replace("<time>", timer.getDynamicTimeLeft(player)));
                return;
            }
            default: Language.PVP_COMMAND_USAGE.forEach(player::sendMessage);
        }
    }
}
