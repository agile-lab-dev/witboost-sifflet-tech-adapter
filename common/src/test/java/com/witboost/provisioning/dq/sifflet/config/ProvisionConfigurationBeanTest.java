package com.witboost.provisioning.dq.sifflet.config;

import static org.junit.jupiter.api.Assertions.*;

import com.witboost.provisioning.dq.sifflet.service.provision.WorkloadProvisionService;
import org.junit.jupiter.api.Test;

class ProvisionConfigurationBeanTest {

    @Test
    void beanCreation() {
        var workload = new WorkloadProvisionService();
        var bean = new ProvisionConfigurationBean().provisionConfiguration(workload);

        assertEquals(workload, bean.getWorkloadProvisionService());
    }
}
