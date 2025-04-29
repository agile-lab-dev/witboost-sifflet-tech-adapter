package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SourceTypeTest {

    @Test
    void testSourceTypeValue() {
        assertEquals("athena", SourceType.ATHENA.getValue());
    }
}
