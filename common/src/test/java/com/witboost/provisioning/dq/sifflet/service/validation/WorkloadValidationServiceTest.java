package com.witboost.provisioning.dq.sifflet.service.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.witboost.provisioning.model.DataProduct;
import com.witboost.provisioning.model.OperationType;
import com.witboost.provisioning.model.Workload;
import com.witboost.provisioning.model.request.ProvisionOperationRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/*
 * TODO Review these tests after you have implemented the tech adapter logic
 */
class WorkloadValidationServiceTest {

    @Test
    void validate() {
        var validationService = new WorkloadValidationService();

        var actual = validationService.validate(
                new ProvisionOperationRequest<>(new DataProduct<>(), new Workload<>(), false, Optional.empty()),
                OperationType.VALIDATE);
        assertTrue(actual.isLeft());
    }
}
