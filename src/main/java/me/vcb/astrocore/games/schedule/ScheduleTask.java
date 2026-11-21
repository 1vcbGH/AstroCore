package me.vcb.astrocore.games.schedule;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.koth.KothData;
import me.vcb.astrocore.games.koth.KothManager;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Getter
class ScheduleTask extends BukkitRunnable {

    private final ScheduleManager manager;
    private DayOfWeek day;
    private List<ScheduleData> daySchedules;

    ScheduleTask(ScheduleManager manager) {
        this.manager = manager;

        this.day = LocalDateTime.now(Config.TIMEZONE.toZoneId()).getDayOfWeek();
        this.daySchedules = manager.getSchedulesByDay(this.day);

        this.runTaskTimerAsynchronously(AstroCore.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        LocalDateTime date = LocalDateTime.now(Config.TIMEZONE.toZoneId());
        if(date.getSecond() != 0) return;

        DayOfWeek day = date.getDayOfWeek();

        if(this.day != day) {
            this.day = day;
            this.daySchedules = AstroCore.getInstance().getScheduleManager().getSchedulesByDay(day);
        }

        this.daySchedules.forEach(schedule -> {
            LocalDateTime time = schedule.getTime();
            if(time.getHour() != date.getHour() || time.getMinute() != date.getMinute()) return;

            Tasks.sync(() -> this.startScheduledEvent(schedule.getName()));
        });
    }

    private void startScheduledEvent(String scheduleName) {
        AstroCore.getInstance().getScheduleManager().cacheNextEventSchedule();

        if(scheduleName.equalsIgnoreCase("Conquest")) {
            AstroCore.getInstance().getConquestManager().startConquest(Bukkit.getConsoleSender());
            return;
        }

        if(scheduleName.equalsIgnoreCase("DTC")) {
            AstroCore.getInstance().getDtcManager().startDtc(Bukkit.getConsoleSender(), Config.DTC_CORE_BREAKS);
            return;
        }

        if(scheduleName.equalsIgnoreCase("EnderDragon")) {
            AstroCore.getInstance().getEnderDragonManager().startEnderDragon(Bukkit.getConsoleSender());
            return;
        }

        this.startKoth(scheduleName);
    }

    private void startKoth(String scheduleName) {
        KothManager manager = AstroCore.getInstance().getKothManager();

        if(manager.isMaxRunningKothsReached()) {
            AstroCore.getInstance().log(Language.KOTH_PREFIX + Language.KOTH_EXCEPTION_MAX_RUNNING_KOTHS_AMOUNT_REACHED);
            return;
        }

        KothData koth = manager.getKoth(scheduleName);

        if(koth != null) {
            manager.startKoth(koth);
        } else {
            this.manager.removeScheduleByName(scheduleName);
        }
    }
}
