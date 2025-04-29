package com.witboost.provisioning.dq.sifflet.model.athena;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AthenaSpecificTest {

    @Test
    void testGettersAndSetters() {
        AthenaSpecific specific = new AthenaSpecific();

        AthenaTable table = new AthenaTable();
        AthenaView view = new AthenaView();

        specific.setStorageAreaId("storageArea1");
        specific.setSourceTable(table);
        specific.setView(view);

        assertEquals("storageArea1", specific.getStorageAreaId());
        assertEquals(table, specific.getSourceTable());
        assertEquals(view, specific.getView());
    }
}
