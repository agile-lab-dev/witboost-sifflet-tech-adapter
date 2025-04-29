package com.witboost.provisioning.dq.sifflet.model.athena;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TableFormatTest {

    @Test
    void testGetValue() {
        TableFormat format = TableFormat.ICEBERG;

        assertEquals("iceberg", format.getValue());
    }
}
