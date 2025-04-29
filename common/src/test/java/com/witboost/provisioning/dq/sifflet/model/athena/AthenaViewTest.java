package com.witboost.provisioning.dq.sifflet.model.athena;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AthenaViewTest {

    @Test
    void testGettersAndSetters() {
        AthenaView view = new AthenaView();

        view.setCatalog("testCatalog");
        view.setDatabase("testDatabase");
        view.setName("testName");

        assertEquals("testCatalog", view.getCatalog());
        assertEquals("testDatabase", view.getDatabase());
        assertEquals("testName", view.getName());
    }
}
