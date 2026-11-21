package me.vcb.astrocore.tab.module.impl;

import lombok.AllArgsConstructor;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.games.koth.RunningKoth;
import me.vcb.astrocore.games.schedule.NextEventSchedule;
import me.vcb.astrocore.tab.PlayerTab;
import me.vcb.astrocore.tab.module.TabModule;
import me.vcb.astrocore.utils.ServerUtils;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class NextKothModule extends TabModule {

    private List<Supplier<String>> noneScheduled;
    private List<Supplier<String>> bloodMoon;
    private List<Supplier<String>> hunterPrey;
    private List<Supplier<String>> eloBoost;
    private List<NextKothApplyFunction<NextEventSchedule>> scheduled;
    private List<NextKothApplyFunction<RunningKoth>> runningKoth;

    private int oldLinesCount;

    public NextKothModule() {
        super("NEXT_KOTH_MODULE");
    }

    @Override
    public void loadAdditionalData(ConfigurationSection section) {
        this.noneScheduled = this.loadNoneScheduledFunctions(section);
        this.bloodMoon = this.loadStaticLines(section, "BLOOD_MOON");
        this.hunterPrey = this.loadStaticLines(section, "HUNTER_PREY");
        this.eloBoost = this.loadStaticLines(section, "ELO_BOOST");
        this.scheduled = this.loadScheduledFunctions(section);
        this.runningKoth = this.loadRunningKothFunctions(section);
    }

    private List<Supplier<String>> loadNoneScheduledFunctions(ConfigurationSection section) {
        return this.loadStaticLines(section, "NONE_SCHEDULED");
    }

    private List<Supplier<String>> loadStaticLines(ConfigurationSection section, String key) {
        List<String> lines = section.getStringList(key);
        List<Supplier<String>> functions = new ArrayList<>();

        for(String line : lines) {
            functions.add(() -> line);
        }

        return functions;
    }

    private List<NextKothApplyFunction<NextEventSchedule>> loadScheduledFunctions(ConfigurationSection section) {
        List<String> lines = section.getStringList("SCHEDULED");
        List<NextKothApplyFunction<NextEventSchedule>> functions = new ArrayList<>();

        for(String line : lines) {
            if(line.contains("<startTime>")) {
                functions.add(new NextKothApplyFunction<>(
                    schedule -> StringUtils.formatMillis(schedule.getStartTime() - System.currentTimeMillis()),
                    ServerUtils.parsePlaceholderFunction(line, "<startTime>"))
                );
            } else if(line.contains("<eventName>")) {
                functions.add(new NextKothApplyFunction<>(
                    schedule -> schedule.getEventName(),
                    ServerUtils.parsePlaceholderFunction(line, "<eventName>"))
                );
            } else {
                functions.add(new NextKothApplyFunction<>(s -> "", s -> line));
            }
        }

        return functions;
    }

    private List<NextKothApplyFunction<RunningKoth>> loadRunningKothFunctions(ConfigurationSection section) {
        List<String> lines = section.getStringList("RUNNING_KOTH");
        List<NextKothApplyFunction<RunningKoth>> functions = new ArrayList<>();

        for(String line : lines) {
            if(line.contains("<capTime>")) {
                functions.add(new NextKothApplyFunction<>(
                    koth -> koth.getCapzone().getTimeLeft(),
                    ServerUtils.parsePlaceholderFunction(line, "<capTime>"))
                );
            } else if(line.contains("<location>")) {
                functions.add(new NextKothApplyFunction<>(
                    koth -> StringUtils.getLocationName(koth.getCapzone().getCuboid().getCenter()),
                    ServerUtils.parsePlaceholderFunction(line, "<location>"))
                );
            } else if(line.contains("<world>")) {
                functions.add(new NextKothApplyFunction<>(
                    koth -> StringUtils.getWorldName(koth.getCapzone().getCuboid().getCenter()),
                    ServerUtils.parsePlaceholderFunction(line, "<world>"))
                );
            } else if(line.contains("<kothName>")) {
                functions.add(new NextKothApplyFunction<>(
                    koth -> koth.getKothData().getName(),
                    ServerUtils.parsePlaceholderFunction(line, "<kothName>"))
                );
            } else {
                functions.add(new NextKothApplyFunction<>(s -> "", s -> line));
            }
        }

        return functions;
    }

    @Override
    public void apply(PlayerTab tab) {
        int linesCount;
        RunningKoth koth = AstroCore.getInstance().getKothManager().getFirstRunningKoth();

        me.vcb.astrocore.events.bloodmoon.BloodMoonManager bm =
            me.vcb.astrocore.events.bloodmoon.BloodMoonManager.getInstance();
        me.vcb.astrocore.events.hunterprey.HunterPreyManager hp =
            me.vcb.astrocore.events.hunterprey.HunterPreyManager.getInstance();

        if(koth != null) {
            linesCount = this.runningKoth.size();

            for(int i = 0; i < linesCount; i++) {
                tab.set(this.startSlot + i, this.runningKoth.get(i).applyLine(koth));
            }
        } else if(bm != null && bm.isActive()) {
            linesCount = this.bloodMoon.size();

            for(int i = 0; i < linesCount; i++) {
                tab.set(this.startSlot + i, this.bloodMoon.get(i).get());
            }
        } else if(hp != null && hp.isActive()) {
            linesCount = this.hunterPrey.size();

            for(int i = 0; i < linesCount; i++) {
                tab.set(this.startSlot + i, this.hunterPrey.get(i).get());
            }
        } else if(me.vcb.astrocore.events.eloboost.EloBoostManager.getInstance() != null
                && me.vcb.astrocore.events.eloboost.EloBoostManager.getInstance().isActive()) {
            linesCount = this.eloBoost.size();

            for(int i = 0; i < linesCount; i++) {
                tab.set(this.startSlot + i, this.eloBoost.get(i).get());
            }
        } else {
            NextEventSchedule nextEvent = AstroCore.getInstance()
                .getScheduleManager().getNextEventSchedule();

            if(nextEvent == null) {
                linesCount = this.noneScheduled.size();

                for(int i = 0; i < linesCount; i++) {
                    tab.set(this.startSlot + i, this.noneScheduled.get(i).get());
                }
            } else {
                linesCount = this.scheduled.size();

                for(int i = 0; i < linesCount; i++) {
                    tab.set(this.startSlot + i, this.scheduled.get(i).applyLine(nextEvent));
                }
            }
        }

        if(linesCount < this.oldLinesCount) {
            for(int i = 0; i < this.oldLinesCount - linesCount; i++) {
                tab.set(this.startSlot + linesCount + i, "");
            }
        }

        this.oldLinesCount = linesCount;
    }

    @AllArgsConstructor
    private static class NextKothApplyFunction<T> {

        private final Function<T, String> parameterSupplier;
        private final Function<String, String> lineFunction;

        public String applyLine(T parameter) {
            return this.lineFunction.apply(this.parameterSupplier.apply(parameter));
        }
    }
}
