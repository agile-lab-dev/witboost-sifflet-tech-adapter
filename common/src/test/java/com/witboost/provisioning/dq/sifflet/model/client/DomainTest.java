package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DomainTest {

    @Test
    void testGettersAndSetters() {
        Domain domain = new Domain();
        domain.setId("456");
        domain.setCreatedDate(1680123456789L);
        domain.setCreatedBy("user");
        domain.setName("TestDomain");
        domain.setCanDelete(true);
        domain.setDescription("Test Domain Description");
        domain.setDomainInputMethod("Manual");

        assertEquals("456", domain.getId());
        assertEquals(1680123456789L, domain.getCreatedDate());
        assertEquals("user", domain.getCreatedBy());
        assertEquals("TestDomain", domain.getName());
        assertTrue(domain.isCanDelete());
        assertEquals("Test Domain Description", domain.getDescription());
        assertEquals("Manual", domain.getDomainInputMethod());
    }

    @Test
    void testNoArgsConstructor() {
        Domain domain = new Domain();
        assertNull(domain.getId());
        assertNull(domain.getName());
        assertFalse(domain.isCanDelete());
    }
}
