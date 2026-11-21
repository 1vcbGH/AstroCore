package me.vcb.astrocore.games.schedule.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;

public class ScheduleNextCommand extends SubCommand {

    ScheduleNextCommand() {
        super("next");

        this.setExecuteAsync(true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        AstroCore.getInstance().getScheduleManager().listNextSchedules(sender);
    }
}
