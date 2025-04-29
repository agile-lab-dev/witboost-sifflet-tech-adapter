package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class DataQualityResultTest {

    @Test
    void testDataQualityResult() {
        DataQualityResult result = new DataQualityResult();
        result.setId("monitor1");
        result.setName("monitorName");
        result.setRuleLabel("ruleLabel1");
        result.setCriticality("high");
        result.setLastRunId("lastRun123");
        result.setLastRunTimestamp(1672531200000L);
        result.setLastRunStatus("success");
        result.setLastRunResult("result123");
        result.setLastRunsMonitorRuns(10);
        result.setLastRunsMonitorSuccess(80.5);
        result.setLastRunsMonitorFailed(19.5);
        result.setLastRunsMonitorAttentionRequired(2.2);
        result.setLastRunsMonitorAttentionTechicalError(1.1);

        DataQualityResult.RunStatus status = new DataQualityResult.RunStatus();
        status.setTimestamp(1672531200000L);
        status.setStatus("FAILED");
        status.setResult("errorDetails");

        result.setLastRunsStatuses(List.of(status));

        assertEquals("monitor1", result.getId());
        assertEquals("monitorName", result.getName());
        assertEquals("ruleLabel1", result.getRuleLabel());
        assertEquals("high", result.getCriticality());
        assertEquals("lastRun123", result.getLastRunId());
        assertEquals(1672531200000L, result.getLastRunTimestamp());
        assertEquals("success", result.getLastRunStatus());
        assertEquals("result123", result.getLastRunResult());
        assertEquals(10, result.getLastRunsMonitorRuns());
        assertEquals(80.5, result.getLastRunsMonitorSuccess());
        assertEquals(19.5, result.getLastRunsMonitorFailed());
        assertEquals(2.2, result.getLastRunsMonitorAttentionRequired());
        assertEquals(1.1, result.getLastRunsMonitorAttentionTechicalError());
        assertEquals(List.of(status), result.getLastRunsStatuses());

        assertEquals(1672531200000L, status.getTimestamp());
        assertEquals("FAILED", status.getStatus());
        assertEquals("errorDetails", status.getResult());
    }
}
