package com.witboost.provisioning.dq.sifflet.model.client;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class GetRuleRunsResponse {
    private List<DataItem> data;
    private int totalElements;

    @Data
    public static class DebugSql {
        private String query;
        private List<String> positionalParameters;
    }

    @Data
    public static class DataItem {
        private String id;
        private long createdDate;
        private String createdBy;
        private long startDate;
        private long endDate;
        private String result;
        private Status status;
        private String type;
        private DebugSql debugSql;
        private String ruleId;
        private boolean debuggable;
        private boolean hasGroupBy;
        private boolean hasGraph;
        private boolean canShowFailingRows;
    }

    public enum Status {
        PENDING,
        RUNNING,
        SUCCESS,
        REQUIRES_YOUR_ATTENTION,
        TECHNICAL_ERROR,
        FAILED
    }

    private static double calculatePercentage(List<DataItem> dataItems, Status status) {
        if (dataItems == null || dataItems.isEmpty()) return 0.0;

        long count =
                dataItems.stream().filter(item -> item.getStatus() == status).count();

        return (count * 100.0) / dataItems.size();
    }

    public static Map<Status, Double> calculateStatusPercentages(List<DataItem> dataItems) {
        if (dataItems == null || dataItems.isEmpty()) {
            return Map.of(
                    Status.SUCCESS, 0.0,
                    Status.FAILED, 0.0,
                    Status.REQUIRES_YOUR_ATTENTION, 0.0,
                    Status.TECHNICAL_ERROR, 0.0);
        }

        return Map.of(
                Status.SUCCESS, calculatePercentage(dataItems, Status.SUCCESS),
                Status.FAILED, calculatePercentage(dataItems, Status.FAILED),
                Status.REQUIRES_YOUR_ATTENTION, calculatePercentage(dataItems, Status.REQUIRES_YOUR_ATTENTION),
                Status.TECHNICAL_ERROR, calculatePercentage(dataItems, Status.TECHNICAL_ERROR));
    }
}
