package com.witboost.provisioning.dq.sifflet.model.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;

class ParametersTest {

    @Test
    void testSchemaChangeCreation() {
        Parameters.SchemaChange schemaChange = new Parameters.SchemaChange();
        assertNotNull(schemaChange);
        assertEquals("SchemaChange", schemaChange.getKind());
    }

    @Test
    void testFieldNullsCreation() {
        Parameters.FieldNulls fieldNulls =
                new Parameters.FieldNulls("myField", "NULL_VALUES", new Parameters.Threshold("type", "value", "100"));

        assertNotNull(fieldNulls);
        assertEquals("FieldNulls", fieldNulls.getKind());
        assertEquals("myField", fieldNulls.getField());
        assertEquals("NULL_VALUES", fieldNulls.getNullValues());
        assertNotNull(fieldNulls.getThreshold());
        assertEquals("type", fieldNulls.getThreshold().getKind());
        assertEquals("100", fieldNulls.getThreshold().getMax());
    }

    @Test
    void testFieldDuplicatesCreation() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode fieldNode = mapper.createObjectNode().put("field", "value");

        Parameters.FieldDuplicates fieldDuplicates = new Parameters.FieldDuplicates(fieldNode);

        assertNotNull(fieldDuplicates);
        assertEquals("FieldDuplicates", fieldDuplicates.getKind());
        assertEquals(fieldNode, fieldDuplicates.getField());
        assertTrue(fieldNode.has("field"));
    }

    @Test
    void testRowDuplicatesCreation() {
        Parameters.RowDuplicates rowDuplicates =
                new Parameters.RowDuplicates(new Parameters.Threshold("type", "mode", "200"));

        assertNotNull(rowDuplicates);
        assertEquals("RowDuplicates", rowDuplicates.getKind());
        assertNotNull(rowDuplicates.getThreshold());
        assertEquals("type", rowDuplicates.getThreshold().getKind());
    }

    @Test
    void testThresholdEquality() {
        Parameters.Threshold threshold1 = new Parameters.Threshold("KindA", "ModeA", "50");
        Parameters.Threshold threshold2 = new Parameters.Threshold("KindA", "ModeA", "50");
        Parameters.Threshold threshold3 = new Parameters.Threshold("KindB", "ModeB", "100");

        assertEquals(threshold1, threshold2);
        assertNotEquals(threshold1, threshold3);
    }

    @Test
    void testParameterEquality() {
        Parameters.SchemaChange schemaChange1 = new Parameters.SchemaChange();
        Parameters.SchemaChange schemaChange2 = new Parameters.SchemaChange();

        assertEquals(schemaChange1, schemaChange2);
    }

    @Test
    void testParameterToString() {
        Parameters.SchemaChange schemaChange = new Parameters.SchemaChange();
        String toStringValue = schemaChange.toString();

        assertTrue(toStringValue.contains("kind=SchemaChange"));
    }

    @Test
    void testCanEqual() {
        Parameters.SchemaChange schemaChange1 = new Parameters.SchemaChange();
        Parameters.FieldDuplicates fieldDuplicates = new Parameters.FieldDuplicates(null);

        assertTrue(schemaChange1.canEqual(new Parameters.SchemaChange()));
        assertFalse(schemaChange1.canEqual(fieldDuplicates));
    }

    @Test
    void testSetOtherField() {
        Parameters parameters = new Parameters("CustomKind");

        parameters.setOtherField("testKey", JsonNodeFactory.instance.textNode("testValue"));
        assertNotNull(parameters.otherFields());
        assertEquals("testValue", parameters.otherFields().get("testKey").asText());

        parameters.setOtherField("nullKey", null);
        assertTrue(parameters.otherFields().containsKey("nullKey"));
        assertNull(parameters.otherFields().get("nullKey"));
    }

    @Test
    void testHashCodeConsistency() {
        Parameters.FieldNulls fieldNulls1 = new Parameters.FieldNulls("field1", "nullValue", null);
        Parameters.FieldNulls fieldNulls2 = new Parameters.FieldNulls("field1", "nullValue", null);

        assertEquals(fieldNulls1.hashCode(), fieldNulls2.hashCode());

        Parameters.FieldNulls fieldNullsWithNull = new Parameters.FieldNulls(null, null, null);
        assertNotNull(fieldNullsWithNull.hashCode());
    }

    @Test
    void testEquals() {
        Parameters.FieldNulls fieldNulls1 = new Parameters.FieldNulls("field1", "nullValue", null);
        Parameters.FieldNulls fieldNulls2 = new Parameters.FieldNulls("field1", "nullValue", null);
        Parameters.FieldDuplicates fieldDuplicates = new Parameters.FieldDuplicates(null);

        assertEquals(fieldNulls1, fieldNulls2);
        assertNotEquals(fieldNulls1, fieldDuplicates);

        Parameters.FieldNulls differentField = new Parameters.FieldNulls("differentField", "nullValue", null);
        assertNotEquals(fieldNulls1, differentField);
    }
}
