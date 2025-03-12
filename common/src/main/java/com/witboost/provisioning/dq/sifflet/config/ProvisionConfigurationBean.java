package com.witboost.provisioning.dq.sifflet.config;

import com.witboost.provisioning.dq.sifflet.service.provision.OutputPortProvisionService;
import com.witboost.provisioning.dq.sifflet.service.provision.StorageAreaProvisionService;
import com.witboost.provisioning.dq.sifflet.service.provision.WorkloadProvisionService;
import com.witboost.provisioning.framework.service.ProvisionConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProvisionConfigurationBean {

    // TODO Remove the components you don't need to support on your Tech Adapter
    @Bean
    ProvisionConfiguration provisionConfiguration(
            OutputPortProvisionService outputPortProvisionService,
            StorageAreaProvisionService storageAreaProvisionService,
            WorkloadProvisionService workloadProvisionService) {
        return ProvisionConfiguration.builder()
                .outputPortProvisionService(outputPortProvisionService)
                .storageProvisionService(storageAreaProvisionService)
                .workloadProvisionService(workloadProvisionService)
                .build();
    }
}
