package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetDomainsResponseTest {

    @Test
    void testGettersAndSetters() {
        GetDomainsResponse response = new GetDomainsResponse();
        response.setTotalElements(1);
        Domain domain = new Domain();
        response.setData(List.of(domain));

        assertEquals(1, response.getTotalElements());
        assertEquals(List.of(domain), response.getData());
    }
}
