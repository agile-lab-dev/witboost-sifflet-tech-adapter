package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CriticalityTest {

    @Test
    void testGetNameById() {
        assertEquals("CRITICAL", Criticality.getNameById(0));
        assertEquals("HIGH", Criticality.getNameById(1));
        assertEquals("MODERATE", Criticality.getNameById(2));
        assertEquals("LOW", Criticality.getNameById(3));
        assertThrows(IllegalArgumentException.class, () -> Criticality.getNameById(99));
    }
}
