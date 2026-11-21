package me.vcb.astrocore.report;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ReportManager {

    private static final int MAX_REPORTS = 100;

    @Getter private static ReportManager instance;

    private final List<ReportData> pending = new ArrayList<>();

    public ReportManager() {
        instance = this;
    }

    public void addReport(UUID reporterUuid, String reporterName,
                          UUID suspectUuid, String suspectName, String reason) {
        if (pending.size() >= MAX_REPORTS) pending.remove(0);
        pending.add(new ReportData(reporterUuid, reporterName,
                                   suspectUuid, suspectName, reason,
                                   System.currentTimeMillis()));
    }

    public List<ReportData> getPending() {
        return Collections.unmodifiableList(pending);
    }

    public void dismiss(int index) {
        if (index >= 0 && index < pending.size()) pending.remove(index);
    }

    public void dismissAll() {
        pending.clear();
    }

    public int size() {
        return pending.size();
    }
}
