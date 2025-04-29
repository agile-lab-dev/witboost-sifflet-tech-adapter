package com.witboost.provisioning.dq.sifflet.model;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.witboost.provisioning.dq.sifflet.model.cli.Monitor;
import com.witboost.provisioning.parser.Parser;
import org.junit.jupiter.api.Test;

class SiffletDataContractTest {

    @Test
    void testParseDataContract() throws JsonProcessingException {
        String yaml =
                """
                schema: []
                termsAndConditions: null
                endpoint: null
                SLA:
                  intervalOfChange: null
                  timeliness: null
                  upTime: null
                quality:
                - type: "custom"
                  engine: "other"
                  implementation: {}
                - type: "custom"
                  engine: "sifflet"
                  implementation:
                    name: Name unique among all monitors for this OP
                    description: string
                    schedule: "@daily" # Defined as @hourly/@daily/@weekly/@monthly/@yearly (default to midnight UTC) or CRON expression.
                    scheduleTimezone: "UTC" # or (optional - default null) Schedule Time Zone, i.e. Europe/Paris
                    incident:
                      severity: Low # (REQUIRED) Severity of the incident "Low" | "Moderate" | "High" | "Critical"
                      createOnFailure: true
                    parameters:
                      kind: FieldNulls # Defines the monitor type
                      # Fields below depend on the kind selected above
                      field: id
                      nullValues: NullEmptyAndWhitespaces""";
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        JsonNode node = om.readTree(yaml);

        var dataContract = Parser.parseObject(node, SiffletDataContract.class);
        assertTrue(dataContract.isRight());
    }

    @Test
    void testParseMultipleQualityRules() throws Exception {
        String yaml =
                """
                quality:
                  - type: "custom"
                    engine: "sifflet"
                    implementation:
                      name: "Rule 1"
                      description: "Description for Rule 1"
                  - type: "custom"
                    engine: "other"
                    implementation: { "exampleKey": "exampleValue" }
                """;

        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        JsonNode node = objectMapper.readTree(yaml);

        var parsedResult = Parser.parseObject(node, SiffletDataContract.class);
        assertTrue(parsedResult.isRight());

        SiffletDataContract dataContract = parsedResult.get();
        assertNotNull(dataContract.getQuality());
        assertEquals(2, dataContract.getQuality().size());

        var rule1 = dataContract.getQuality().get(0);
        assertEquals("sifflet", rule1.getEngine());
        assertTrue(rule1 instanceof SiffletDataContract.QualityRule.SiffletQualityRule);

        var rule2 = dataContract.getQuality().get(1);
        assertEquals("other", rule2.getEngine());
        assertTrue(rule2 instanceof SiffletDataContract.QualityRule.OtherQualityRule);
    }

    @Test
    void testGetSiffletMonitors() throws Exception {
        String yaml =
                """
                quality:
                  - type: "custom"
                    engine: "sifflet"
                    implementation:
                      name: "Monitor 1"
                      description: "Description 1"
                  - type: "custom"
                    engine: "sifflet"
                    implementation:
                      name: "Monitor 2"
                      description: "Description 2"
                  - type: "custom"
                    engine: "other"
                    implementation: { }
                """;

        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        JsonNode node = objectMapper.readTree(yaml);

        var parsedResult = Parser.parseObject(node, SiffletDataContract.class);
        assertTrue(parsedResult.isRight());

        SiffletDataContract dataContract = parsedResult.get();
        var monitors = dataContract.getSiffletMonitors();

        assertNotNull(monitors);
        assertEquals(2, monitors.size());
        assertTrue(monitors.stream().allMatch(monitor -> monitor.getName().startsWith("Monitor")));
    }

    @Test
    void testParseWithoutQuality() throws Exception {
        String yaml =
                """
        schema:
          - name: "id"
            dataType: "INT"
        termsAndConditions: null
        """;

        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        JsonNode node = objectMapper.readTree(yaml);

        var parsedResult = Parser.parseObject(node, SiffletDataContract.class);
        assertTrue(parsedResult.isRight());

        SiffletDataContract dataContract = parsedResult.get();
        assertNotNull(dataContract.getSchema());
        assertEquals(1, dataContract.getSchema().size());
        assertNull(dataContract.getQuality());
    }

    @Test
    void testParseInvalidYaml() {
        String invalidYaml = "invalid: [ ";

        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        JsonNode node;

        try {
            node = objectMapper.readTree(invalidYaml);
            Parser.parseObject(node, SiffletDataContract.class);
            fail("Expected an exception to be thrown for invalid YAML");
        } catch (Exception e) {
            assertTrue(e instanceof JsonProcessingException, "Exception should be related to parsing");
        }
    }

    @Test
    void testCanEqualWithValidMonitorJSON() throws Exception {
        String json =
                """
    {
      "type": "custom",
      "engine": "sifflet",
      "implementation": {
        "name": "Test Monitor",
        "incident": null,
        "notifications": [],
        "datasets": [],
        "parameters": null,
        "schedule": null,
        "scheduleTimezone": null
      }
    }
    """;

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        SiffletDataContract.QualityRule<Monitor> qualityRule1 =
                objectMapper.readValue(json, SiffletDataContract.QualityRule.class);
        SiffletDataContract.QualityRule<Monitor> qualityRule2 =
                objectMapper.readValue(json, SiffletDataContract.QualityRule.class);

        assertTrue(
                qualityRule1.canEqual(qualityRule2), "QualityRule should be able to compare with another QualityRule");
        assertFalse(qualityRule1.canEqual("string"), "QualityRule should not be equal to a string or unrelated type");
    }

    @Test
    void testEqualsHashCodeGetTypeAndToStringForQualityRule() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonRule1 =
                """
        {
            "type": "type1",
            "engine": "sifflet",
            "implementation": null
        }
        """;

        String jsonRule2 =
                """
        {
            "type": "type1",
            "engine": "sifflet",
            "implementation": null
        }
        """;

        String jsonRule3 =
                """
        {
            "type": "type2",
            "engine": "other",
            "implementation": null
        }
        """;

        SiffletDataContract.QualityRule.SiffletQualityRule rule1 =
                objectMapper.readValue(jsonRule1, SiffletDataContract.QualityRule.SiffletQualityRule.class);

        SiffletDataContract.QualityRule.SiffletQualityRule rule2 =
                objectMapper.readValue(jsonRule2, SiffletDataContract.QualityRule.SiffletQualityRule.class);

        SiffletDataContract.QualityRule.OtherQualityRule rule3 =
                objectMapper.readValue(jsonRule3, SiffletDataContract.QualityRule.OtherQualityRule.class);

        assertEquals(rule1, rule2);
        assertNotEquals(rule1, rule3);
        assertEquals(rule1.hashCode(), rule2.hashCode());
        assertNotEquals(rule1.hashCode(), rule3.hashCode());

        assertEquals("type1", rule1.getType());
        assertEquals("type1", rule2.getType());
        assertEquals("type2", rule3.getType());

        String rule1ToString = rule1.toString();
        assertTrue(rule1ToString.contains("type1"));
        assertTrue(rule1ToString.contains("sifflet"));
        assertTrue(rule1ToString.contains("null"));

        String rule3ToString = rule3.toString();
        assertTrue(rule3ToString.contains("type2"));
        assertTrue(rule3ToString.contains("other"));
    }
}
