package com.witboost.provisioning.dq.sifflet.model.cli;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WorkspaceApplyChanges(
        String kind, String id, WorkspaceChange change, String status, String subStatus, List<Log> logs) {
    // field 'to' stores the target object
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WorkspaceChange(String type, JsonNode from, JsonNode to) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Log(String level, String message) {}
}
