package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SourceTest {

    @Test
    void testGettersAndSetters() {
        Source source = new Source();
        source.setId("123");
        source.setName("TestSource");
        LastRun lastRun = new LastRun();
        source.setLastrun(lastRun);
        Map<String, Object> params = Map.of("key1", "value1", "key2", 42);
        source.setParameters(params);

        assertEquals("123", source.getId());
        assertEquals("TestSource", source.getName());
        assertEquals(lastRun, source.getLastrun());
        assertEquals(params, source.getParameters());
    }

    @Test
    void testNoArgsConstructor() {
        Source source = new Source();

        assertNull(source.getId());
        assertNull(source.getName());
        assertNull(source.getLastrun());
        assertNull(source.getParameters());
    }

    @Test
    void testSettingParameters() {
        Source source = new Source();
        Map<String, Object> params = Map.of("param1", "valueA", "param2", 100);
        source.setParameters(params);
        assertEquals(params, source.getParameters());
        assertTrue(source.getParameters().containsKey("param1"));
        assertTrue(source.getParameters().containsKey("param2"));
        assertEquals("valueA", source.getParameters().get("param1"));
        assertEquals(100, source.getParameters().get("param2"));
    }
}
