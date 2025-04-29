package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LastRunStatusTest {

    @Test
    void testEnumValues() {
        assertEquals("pending", LastRunStatus.PENDING.getStatus());
        assertEquals("running", LastRunStatus.RUNNING.getStatus());
        assertEquals("success", LastRunStatus.SUCCESS.getStatus());
        assertEquals("failure", LastRunStatus.FAILURE.getStatus());
        assertEquals(
                "skipped_datasource_already_running", LastRunStatus.SKIPPED_DATASOURCE_ALREADY_RUNNING.getStatus());
    }

    @Test
    void testFromStringValid() {
        assertEquals(LastRunStatus.PENDING, LastRunStatus.fromString("pending"));
        assertEquals(LastRunStatus.RUNNING, LastRunStatus.fromString("RUNNING"));
        assertEquals(LastRunStatus.SUCCESS, LastRunStatus.fromString("Success"));
        assertEquals(LastRunStatus.FAILURE, LastRunStatus.fromString("FAILURE"));
        assertEquals(
                LastRunStatus.SKIPPED_DATASOURCE_ALREADY_RUNNING,
                LastRunStatus.fromString("skipped_datasource_already_running"));
    }

    @Test
    void testFromStringInvalid() {
        Exception exception =
                assertThrows(IllegalArgumentException.class, () -> LastRunStatus.fromString("invalid_status"));
        assertEquals("Unknown status: invalid_status", exception.getMessage());
    }
}
