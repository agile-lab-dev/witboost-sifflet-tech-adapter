package com.witboost.provisioning.dq.sifflet.model.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LastRunStatus {
    PENDING("pending"),
    RUNNING("running"),
    SUCCESS("success"),
    FAILURE("failure"),
    SKIPPED_DATASOURCE_ALREADY_RUNNING("skipped_datasource_already_running");

    private final String status;

    public static LastRunStatus fromString(String status) {
        for (LastRunStatus s : LastRunStatus.values()) {
            if (s.status.equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + status);
    }
}
