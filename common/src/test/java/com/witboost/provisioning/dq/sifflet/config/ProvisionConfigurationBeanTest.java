package com.witboost.provisioning.dq.sifflet.config;

import static org.junit.jupiter.api.Assertions.*;

import com.witboost.provisioning.dq.sifflet.service.provision.WorkloadProvisionService;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class ProvisionConfigurationBeanTest {

    ProvisionConfigurationBean beanConfig = new ProvisionConfigurationBean();

    @Test
    void beanCreation() {
        var workload = new WorkloadProvisionService();
        var bean = beanConfig.provisionConfiguration(workload);

        assertEquals(workload, bean.getWorkloadProvisionService());
    }

    @Test
    void restClientBeanCreation() {
        RestClient client = beanConfig.restClient();

        assertNotNull(client);
    }
}
