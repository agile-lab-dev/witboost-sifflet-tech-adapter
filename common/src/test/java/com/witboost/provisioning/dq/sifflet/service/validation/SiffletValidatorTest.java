package com.witboost.provisioning.dq.sifflet.service.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.witboost.provisioning.dq.sifflet.model.SiffletSpecific;
import com.witboost.provisioning.dq.sifflet.model.athena.AthenaInfo;
import com.witboost.provisioning.dq.sifflet.model.cli.Notification;
import com.witboost.provisioning.model.common.FailedOperation;
import java.util.List;
import org.junit.jupiter.api.Test;

class SiffletValidatorTest {

    @Test
    void validateSiffletComponent_success() {
        var siffletSpecific =
                new SiffletSpecific("@daily", new Notification.Email("email@email.com"), List.of("urn:output:port"));

        var result = SiffletValidator.validateSiffletComponent(siffletSpecific);

        assertTrue(result.isRight(), "Expected validation to succeed");
    }

    @Test
    void validateSiffletComponent_failure() {
        var siffletSpecific = new SiffletSpecific(null, null, List.of()); // Invalid data

        var result = SiffletValidator.validateSiffletComponent(siffletSpecific);

        assertTrue(result.isLeft(), "Expected validation to fail");
        FailedOperation failedOp = result.getLeft();
        assertNotNull(failedOp, "Expected a failure message");
        assertFalse(failedOp.problems().isEmpty(), "Expected a list of problem details");
    }

    @Test
    void validateSiffletComponent_nullCronExpression() {
        var siffletSpecific =
                new SiffletSpecific(null, new Notification.Email("email@email.com"), List.of("urn:output:port"));

        var result = SiffletValidator.validateSiffletComponent(siffletSpecific);

        assertTrue(result.isLeft(), "Expected validation to fail due to null cron expression");
        FailedOperation failedOp = result.getLeft();
        assertNotNull(failedOp, "Expected error for null cron expression");
    }

    @Test
    void validateSiffletComponent_invalidEmailNotification() {
        var siffletSpecific = new SiffletSpecific("@daily", null, List.of("urn:output:port"));

        var result = SiffletValidator.validateSiffletComponent(siffletSpecific);

        assertTrue(result.isLeft(), "Expected validation to fail due to invalid email in Notification");
        FailedOperation failedOp = result.getLeft();
        assertNotNull(failedOp, "Expected a failure message for invalid email");
    }

    @Test
    void validateSiffletComponent_emptyAthenaOutputPorts() {
        var siffletSpecific = new SiffletSpecific("@daily", new Notification.Email("email@email.com"), List.of());

        var result = SiffletValidator.validateSiffletComponent(siffletSpecific);

        assertTrue(result.isLeft(), "Expected validation to fail due to empty list of output ports");
        FailedOperation failedOp = result.getLeft();
        assertNotNull(failedOp, "Expected a failure message for empty output ports");
    }

    @Test
    void extractAndValidateSiffletOutputPortDependency_missingFields() throws Exception {
        String json = "{ \"catalog\": \"AWSDataCatalog\" }"; // Missing required fields
        JsonNode outputPortDependency = new ObjectMapper().readTree(json);

        var result = SiffletValidator.extractAndValidateSiffletOutputPortDependency(outputPortDependency);

        assertTrue(result.isLeft(), "Expected dependency validation to fail due to missing fields");
        FailedOperation failedOp = result.getLeft();
        assertNotNull(failedOp, "Expected a failure message");
        assertFalse(failedOp.problems().isEmpty(), "Expected errors for missing fields");
    }

    @Test
    void extractAndValidateSiffletOutputPortDependency_invalidJson() {
        var result = SiffletValidator.extractAndValidateSiffletOutputPortDependency(null);

        assertTrue(result.isLeft(), "Expected dependency validation to fail for invalid JSON");
        FailedOperation failedOp = result.getLeft();
        assertNotNull(failedOp, "Expected a failure message");
    }

    @Test
    void getAthenaEntityFromInfo_missingFields() {
        var athenaInfo = new AthenaInfo(); // Invalid AthenaInfo

        var result = SiffletValidator.getAthenaEntityFromInfo(athenaInfo);

        assertTrue(result.isLeft(), "Expected AthenaEntity extraction to fail due to missing fields");
        FailedOperation failedOp = result.getLeft();
        assertNotNull(failedOp, "Expected a failure message");
        assertFalse(failedOp.problems().isEmpty(), "Expected specific problem details");
    }
}
