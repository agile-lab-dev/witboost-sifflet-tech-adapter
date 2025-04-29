package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class GetSourcesResponseTest {

    @Test
    void testGetSourcesResponse() {
        GetSourcesResponse response = new GetSourcesResponse();

        response.setTotalElements(10);
        response.setData(List.of());

        assertEquals(10, response.getTotalElements());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());
    }
}
