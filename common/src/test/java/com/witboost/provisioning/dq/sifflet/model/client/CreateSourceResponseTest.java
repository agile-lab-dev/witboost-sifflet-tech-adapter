package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CreateSourceResponseTest {

    @Test
    void testCreateSourceResponse() {
        CreateSourceResponse response = new CreateSourceResponse();

        response.setId("source1");
        response.setName("testSource");
        response.setType("testType");
        response.setStatus("created");
        response.setCreatedAt("2023-01-01T12:00:00Z");
        response.setUpdatedAt("2023-01-02T12:00:00Z");
        response.setParameters(Map.of("key1", "value1", "key2", 123));

        assertEquals("source1", response.getId());
        assertEquals("testSource", response.getName());
        assertEquals("testType", response.getType());
        assertEquals("created", response.getStatus());
        assertEquals("2023-01-01T12:00:00Z", response.getCreatedAt());
        assertEquals("2023-01-02T12:00:00Z", response.getUpdatedAt());
        assertEquals(Map.of("key1", "value1", "key2", 123), response.getParameters());
    }

    @Test
    void testToString() {
        CreateSourceResponse response = new CreateSourceResponse();
        response.setId("source1");
        response.setName("testSource");

        String expected = "CreateSourceResponse{" + "id='source1', name='testSource', type='null', "
                + "status='null', createdAt='null', updatedAt='null', parameters=null}";
        assertTrue(response.toString().contains("CreateSourceResponse{"));
        assertTrue(response.toString().contains("id='source1'"));
        assertTrue(response.toString().contains("name='testSource'"));
    }
}
