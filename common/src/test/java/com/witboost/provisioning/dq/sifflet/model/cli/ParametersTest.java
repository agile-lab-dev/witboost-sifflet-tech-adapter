package com.witboost.provisioning.dq.sifflet.model.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

class ParametersTest {

    @Test
    void parsingDuplicatesParameters() {
        String yaml =
                """
                kind: FieldDuplicates
                field: myField
                other: "other!"
                other:
                  ignored: additional
                """;

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        Parameters params = assertDoesNotThrow(() -> om.readValue(yaml, Parameters.class));
        assertInstanceOf(Parameters.FieldDuplicates.FieldDuplicates.class, params);
        assertEquals(((Parameters.FieldDuplicates) params).getField().textValue(), "myField");
    }

    @Test
    void parsingFieldNullsParameters() {
        String yaml =
                """
                kind: FieldNulls
                field: myField
                threshold:
                  kind: Static
                  valueMode: Percentage
                  max: 10%""";

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        Parameters params = assertDoesNotThrow(() -> om.readValue(yaml, Parameters.class));
        assertInstanceOf(Parameters.FieldDuplicates.FieldNulls.class, params);
        assertEquals(((Parameters.FieldNulls) params).getField(), "myField");
    }

    @Test
    void parsingSchemaChangeParameters() {
        String yaml = """
                kind: SchemaChange
                """;

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        Parameters params = assertDoesNotThrow(() -> om.readValue(yaml, Parameters.class));
        assertInstanceOf(Parameters.FieldDuplicates.SchemaChange.class, params);
    }

    @Test
    void parsingRowDuplicatesParameters() {
        String yaml = """
                kind: RowDuplicates
                """;

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        Parameters params = assertDoesNotThrow(() -> om.readValue(yaml, Parameters.class));
        assertInstanceOf(Parameters.FieldDuplicates.RowDuplicates.class, params);
    }
}
