package me.vcb.astrocore.games.schedule.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.schedule.ScheduleData;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class ScheduleDeleteCommand extends SubCommand {

    ScheduleDeleteCommand() {
        super("delete", Collections.singletonList("remove"), "astrocore.schedule.delete");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.SCHEDULE_PREFIX + Language.SCHEDULE_DELETE_USAGE);
            return;
        }

        if(!this.checkNumber(sender, args[0])) return;

        int scheduleId = Integer.parseInt(args[0]);
        ScheduleData schedule = AstroCore.getInstance().getScheduleManager().getScheduleById(scheduleId);

        if(schedule == null) {
            sender.sendMessage(Language.SCHEDULE_PREFIX + Language.SCHEDULE_DELETE_DOESNT_EXIST.replace("<id>", args[0]));
            return;
        }

        AstroCore.getInstance().getScheduleManager().removeSchedule(schedule, true);

        sender.sendMessage(Language.SCHEDULE_PREFIX + Language.SCHEDULE_DELETE_DELETED
            .replace("<id>", String.valueOf(schedule.getId())));
    }
}
