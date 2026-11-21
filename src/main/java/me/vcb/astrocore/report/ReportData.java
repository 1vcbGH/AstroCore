package me.vcb.astrocore.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ReportData {

    private final UUID   reporterUuid;
    private final String reporterName;
    private final UUID   suspectUuid;
    private final String suspectName;
    private final String reason;
    private final long   timestamp;
}
