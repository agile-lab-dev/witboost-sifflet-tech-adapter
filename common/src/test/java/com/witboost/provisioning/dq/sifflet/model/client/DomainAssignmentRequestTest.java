package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DomainAssignmentRequestTest {

    @Test
    void testGettersAndAllArgsConstructor() {
        List<String> assets = List.of("asset1", "asset2");
        DomainAssignmentRequest request =
                new DomainAssignmentRequest("Manual", false, assets, "Test Description", "Test Name");

        assertEquals("Manual", request.getDomainInputMethod());
        assertFalse(request.isAllDomain());
        assertEquals(assets, request.getAssets());
        assertEquals("Test Description", request.getDescription());
        assertEquals("Test Name", request.getName());
    }

    @Test
    void testToString() {
        DomainAssignmentRequest request =
                new DomainAssignmentRequest("Manual", false, List.of("asset1"), "Description", "Name");
        String expected =
                "DomainAssignmentRequest(domainInputMethod=Manual, isAllDomain=false, assets=[asset1], description=Description, name=Name)";
        assertEquals(expected, request.toString());
    }
}
