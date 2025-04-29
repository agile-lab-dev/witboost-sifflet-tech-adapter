package com.witboost.provisioning.dq.sifflet.model.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IncidentTest {

    @Test
    void testIncidentCreation() {
        Incident incident = new Incident(Incident.Severity.High, true);

        assertNotNull(incident);
        assertEquals(Incident.Severity.High, incident.getSeverity());
        assertTrue(incident.getCreateOnFailure());
    }

    @Test
    void testSeverityFromString() {
        assertEquals(Incident.Severity.Low, Incident.Severity.fromString("Low"));
        assertEquals(Incident.Severity.Critical, Incident.Severity.fromString("critical"));

        Exception exception =
                assertThrows(IllegalArgumentException.class, () -> Incident.Severity.fromString("Unknown"));
        assertTrue(exception.getMessage().contains("Unknown severity"));
    }

    @Test
    void testIncidentEquality() {
        Incident incident1 = new Incident(Incident.Severity.High, true);
        Incident incident2 = new Incident(Incident.Severity.High, true);
        Incident incident3 = new Incident(Incident.Severity.Low, false);

        assertEquals(incident1, incident2);
        assertNotEquals(incident1, incident3);
    }

    @Test
    void testIncidentToString() {
        Incident incident = new Incident(Incident.Severity.Low, false);
        assertTrue(incident.toString().contains("Low"));
    }
}
