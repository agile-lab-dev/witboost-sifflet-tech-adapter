package com.witboost.provisioning.dq.sifflet.model;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.witboost.provisioning.parser.Parser;
import org.junit.jupiter.api.Test;

class SiffletDataContractTest {

    @Test
    void testDataContractParse() throws JsonProcessingException {
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
        System.out.println(dataContract.get());
    }
}
