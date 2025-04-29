package com.witboost.provisioning.dq.sifflet.model.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PersistentMonitorTest {

    @Test
    void testPersistentMonitorCreation() {
        Monitor monitor = Monitor.builder()
                .name("TestMonitor")
                .schedule("daily")
                .scheduleTimezone("UTC")
                .build();

        PersistentMonitor persistentMonitor = new PersistentMonitor("Friendly-ID-123", monitor);

        assertNotNull(persistentMonitor);
        assertEquals("Friendly-ID-123", persistentMonitor.getFriendlyId());
        assertEquals("TestMonitor", persistentMonitor.getName());
        assertEquals("daily", persistentMonitor.getSchedule());
        assertEquals("UTC", persistentMonitor.getScheduleTimezone());
    }

    @Test
    void testPersistentMonitorToStringAndEquality() {
        Monitor monitor1 = Monitor.builder().name("Monitor1").build();
        PersistentMonitor persistentMonitor1 = new PersistentMonitor("ID-001", monitor1);
        PersistentMonitor persistentMonitor2 = new PersistentMonitor("ID-001", monitor1);
        PersistentMonitor persistentMonitor3 = new PersistentMonitor("ID-002", monitor1);

        assertEquals(persistentMonitor1, persistentMonitor2);
        assertNotEquals(persistentMonitor1, persistentMonitor3);
        assertTrue(persistentMonitor1.toString().contains("ID-001"));
    }
}
