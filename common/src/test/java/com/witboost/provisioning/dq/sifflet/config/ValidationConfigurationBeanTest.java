package com.witboost.provisioning.dq.sifflet.config;

import static org.junit.jupiter.api.Assertions.*;

import com.witboost.provisioning.dq.sifflet.service.validation.OutputPortValidationService;
import com.witboost.provisioning.dq.sifflet.service.validation.StorageAreaValidationService;
import com.witboost.provisioning.dq.sifflet.service.validation.WorkloadValidationService;
import org.junit.jupiter.api.Test;

class ValidationConfigurationBeanTest {

    @Test
    void beanCreation() {
        var outputPort = new OutputPortValidationService();
        var storageArea = new StorageAreaValidationService();
        var workload = new WorkloadValidationService();
        var bean = new ValidationConfigurationBean().validationConfiguration(outputPort, storageArea, workload);

        assertEquals(outputPort, bean.getOutputPortValidationService());
        assertEquals(storageArea, bean.getStorageValidationService());
        assertEquals(workload, bean.getWorkloadValidationService());
    }
}
