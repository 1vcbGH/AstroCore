package me.vcb.astrocore.commands;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.timer.scoreboard.AppleTimer;
import me.vcb.astrocore.timer.scoreboard.GAppleTimer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class GoldenAppleCommand extends BaseCommand {

    public GoldenAppleCommand() {
        super("goldenapple", Collections.singletonList("gapple"), "astrocore.goldenapple", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        AppleTimer appleTimer = TimerManager.getInstance().getAppleTimer();
        GAppleTimer gAppleTimer = TimerManager.getInstance().getGAppleTimer();

        if(!appleTimer.isActive(player) && !gAppleTimer.isActive(player)) {
            player.sendMessage(Language.PREFIX + Language.GOLDEN_APPLE_NO_COOLDOWNS);
            return;
        }

        if(appleTimer.isActive(player)) {
            player.sendMessage(Language.PREFIX + Language.NORMAL_APPLE_COMMAND_MESSAGE
                .replace("<time>", appleTimer.getTimeLeft(player)));
        }

        if(gAppleTimer.isActive(player)) {
            player.sendMessage(Language.PREFIX + Language.ENCHANTED_APPLE_COMMAND_MESSAGE
                .replace("<time>", gAppleTimer.getDynamicTimeLeft(player)));
        }
    }
}
