package com.witboost.provisioning.dq.sifflet.model.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MonitorTest {

    @Test
    void testMonitorCreation() {
        Incident incident = new Incident(Incident.Severity.Moderate, true);
        Dataset dataset1 = new Dataset("s3://data1.csv");
        Dataset dataset2 = new Dataset("s3://data2.csv");
        Notification.Email notification = new Notification.Email("TestNotification");

        Monitor monitor = Monitor.builder()
                .name("TestMonitor")
                .incident(incident)
                .notification(notification)
                .dataset(dataset1)
                .dataset(dataset2)
                .schedule("daily")
                .scheduleTimezone("UTC")
                .build();

        assertNotNull(monitor);
        assertEquals("TestMonitor", monitor.getName());
        assertEquals(incident, monitor.getIncident());
        assertEquals(2, monitor.getDatasets().size());
        assertEquals("daily", monitor.getSchedule());
        assertEquals("UTC", monitor.getScheduleTimezone());
    }

    @Test
    void testDefaultValues() {
        Monitor monitor = new Monitor();

        assertEquals("Monitor", monitor.getKind());
        assertEquals(2, monitor.getVersion());
    }

    @Test
    void testMonitorEquality() {
        Monitor monitor1 = Monitor.builder().name("Monitor1").build();
        Monitor monitor2 = Monitor.builder().name("Monitor1").build();
        Monitor monitor3 = Monitor.builder().name("Monitor3").build();

        assertEquals(monitor1, monitor2);
        assertNotEquals(monitor1, monitor3);
    }

    @Test
    void testMonitorToString() {
        Monitor monitor = Monitor.builder().name("ExampleMonitor").build();
        assertTrue(monitor.toString().contains("ExampleMonitor"));
    }

    @Test
    void testHashCodeConsistency() {
        Monitor monitor1 = Monitor.builder()
                .name("Monitor1")
                .schedule("daily")
                .scheduleTimezone("UTC")
                .build();

        Monitor monitor2 = Monitor.builder()
                .name("Monitor1")
                .schedule("daily")
                .scheduleTimezone("UTC")
                .build();

        Monitor monitor3 = Monitor.builder()
                .name("Monitor2")
                .schedule("hourly")
                .scheduleTimezone("PST")
                .build();

        assertEquals(monitor1.hashCode(), monitor2.hashCode());
        assertNotEquals(monitor1.hashCode(), monitor3.hashCode());
    }

    @Test
    void testHashCodeNullFields() {
        Monitor monitor = Monitor.builder()
                .name(null)
                .schedule(null)
                .scheduleTimezone(null)
                .build();

        assertNotNull(monitor.hashCode());
    }

    @Test
    void testToBuilder() {
        Monitor monitor = Monitor.builder()
                .name("Monitor1")
                .schedule("daily")
                .scheduleTimezone("UTC")
                .build();

        Monitor copy = monitor.toBuilder().build();

        assertEquals(monitor, copy);

        Monitor modifiedCopy = monitor.toBuilder().schedule("hourly").build();

        assertNotEquals(monitor, modifiedCopy);

        assertEquals("daily", monitor.getSchedule());
        assertEquals("hourly", modifiedCopy.getSchedule());
    }

    @Test
    void testEquals() {
        Monitor monitor1 = Monitor.builder()
                .name("Monitor1")
                .schedule("daily")
                .scheduleTimezone("UTC")
                .build();

        Monitor monitor2 = Monitor.builder()
                .name("Monitor1")
                .schedule("daily")
                .scheduleTimezone("UTC")
                .build();

        Monitor monitor3 = Monitor.builder()
                .name("Monitor2")
                .schedule("hourly")
                .scheduleTimezone("PST")
                .build();

        assertEquals(monitor1, monitor2);
        assertNotEquals(monitor1, monitor3);
        assertNotEquals(null, monitor1);
        assertNotEquals(new Object(), monitor1);
        assertEquals(monitor1, monitor1);
    }
}
