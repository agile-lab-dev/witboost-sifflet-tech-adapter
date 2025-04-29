package com.witboost.provisioning.dq.sifflet.model.athena;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AthenaTableTest {

    @Test
    void testGettersAndSetters() {
        AthenaTable table = new AthenaTable();

        table.setCatalog("testCatalog");
        table.setDatabase("testDatabase");
        table.setName("testName");
        table.setTableFormat(TableFormat.ICEBERG);

        assertEquals("testCatalog", table.getCatalog());
        assertEquals("testDatabase", table.getDatabase());
        assertEquals("testName", table.getName());
        assertEquals(TableFormat.ICEBERG, table.getTableFormat());
    }
}
