package com.witboost.provisioning.dq.sifflet.model.athena;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AthenaInfoTest {

    @Test
    void testGetInfoValue() {
        AthenaInfo.Info info = new AthenaInfo.Info("label1", "value1", "type1");
        AthenaInfo athenaInfo = new AthenaInfo(Map.of("key1", info));

        assertEquals("value1", athenaInfo.getInfoValue("key1").orElse(null));
        assertEquals(Optional.empty(), athenaInfo.getInfoValue("key2"));
    }

    @Test
    void testInfoClassGetters() {
        AthenaInfo.Info info = new AthenaInfo.Info("label1", "value1", "type1");

        assertEquals("label1", info.getLabel());
        assertEquals("value1", info.getValue());
        assertEquals("type1", info.getType());
    }
}
