package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAssetsResponseTest {

    @Test
    void testGettersAndSetters() {
        GetAssetsResponse response = new GetAssetsResponse();
        response.setTotalCount(2);
        Dataset dataset1 = new Dataset();
        Dataset dataset2 = new Dataset();
        response.setData(List.of(dataset1, dataset2));

        assertEquals(2, response.getTotalCount());
        assertEquals(List.of(dataset1, dataset2), response.getData());
    }
}
