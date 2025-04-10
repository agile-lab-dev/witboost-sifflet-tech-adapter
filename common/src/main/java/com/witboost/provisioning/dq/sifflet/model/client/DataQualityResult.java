package com.witboost.provisioning.dq.sifflet.model.client;

import java.util.List;
import lombok.Data;

@Data
public class DataQualityResult {
    private String id; // monitor ID
    private String name; // monitor Name
    private String ruleLabel;
    private String criticality;
    private String lastRunId;
    private long lastRunTimestamp;
    private String lastRunStatus;
    private String lastRunResult;
    private int lastRunsMonitorRuns;
    private double lastRunsMonitorSuccess;
    private double lastRunsMonitorFailed;
    private double lastRunsMonitorAttentionRequired;
    private double lastRunsMonitorAttentionTechicalError;
    private List<RunStatus> lastRunsStatuses;

    @Data
    public static class RunStatus {
        private long timestamp;
        private String status;
        private String result;
    }
}
