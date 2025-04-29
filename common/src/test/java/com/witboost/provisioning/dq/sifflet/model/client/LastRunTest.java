package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LastRunTest {

    @Test
    void testGettersAndSetters() {
        LastRun lastRun = new LastRun();
        lastRun.setStatus("Success");
        lastRun.setTimestamp("2023-01-01T00:00:00");

        assertEquals("Success", lastRun.getStatus());
        assertEquals("2023-01-01T00:00:00", lastRun.getTimestamp());
    }

    @Test
    void testAllArgsConstructor() {
        LastRun lastRun = new LastRun("Pending", "2023-01-02T00:00:00");
        assertEquals("Pending", lastRun.getStatus());
        assertEquals("2023-01-02T00:00:00", lastRun.getTimestamp());
    }
}
