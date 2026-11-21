package me.vcb.astrocore.games.schedule.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;

public class ScheduleClearCommand extends SubCommand {

    ScheduleClearCommand() {
        super("clear", "astrocore.schedule.clear");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        AstroCore.getInstance().getScheduleManager().clearSchedule(sender);
    }
}
