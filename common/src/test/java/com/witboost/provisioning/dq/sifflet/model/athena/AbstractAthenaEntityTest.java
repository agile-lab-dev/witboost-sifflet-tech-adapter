package com.witboost.provisioning.dq.sifflet.model.athena;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AbstractAthenaEntityTest {

    static class TestAthenaEntity extends AbstractAthenaEntity {}

    @Test
    void testGettersAndSetters() {
        TestAthenaEntity entity = new TestAthenaEntity();
        entity.setCatalog("testCatalog");
        entity.setDatabase("testDatabase");
        entity.setName("testName");

        assertEquals("testCatalog", entity.getCatalog());
        assertEquals("testDatabase", entity.getDatabase());
        assertEquals("testName", entity.getName());
    }
}
