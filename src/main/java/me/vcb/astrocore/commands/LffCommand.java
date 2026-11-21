package me.vcb.astrocore.commands;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.handlers.chat.ChatHandler;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.timer.cooldown.CooldownTimer;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LffCommand extends BaseCommand {

    public LffCommand() {
        super("lff", "astrocore.lff", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(FactionsManager.getInstance().getPlayerFaction(player) != null) {
            player.sendMessage(Language.PREFIX + Language.LFF_COMMAND_ALREADY_IN_FACTION);
            return;
        }

        CooldownTimer timer = TimerManager.getInstance().getCooldownTimer();

        if(timer.isActive(player, "LFF")) {
            player.sendMessage(Language.PREFIX + Language.LFF_COMMAND_COOLDOWN
                .replace("<seconds>", timer.getTimeLeft(player, "LFF")));
            return;
        }

        String rankPrefix = ChatHandler.getInstance().getPrefix(player);

        Language.LFF_COMMAND_MESSAGE.forEach(line -> Messages.sendMessage(line
            .replace("<player>", player.getName())
            .replace("<rankPrefix>", rankPrefix)));

        timer.activate(player, "LFF", Config.LFF_COMMAND_COOLDOWN,
            Language.PREFIX + Language.LFF_COMMAND_COOLDOWN_EXPIRED);
    }
}
