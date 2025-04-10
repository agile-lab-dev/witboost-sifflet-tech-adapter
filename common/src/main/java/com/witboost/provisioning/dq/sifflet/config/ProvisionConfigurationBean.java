package com.witboost.provisioning.dq.sifflet.config;

import com.witboost.provisioning.dq.sifflet.service.provision.WorkloadProvisionService;
import com.witboost.provisioning.framework.service.ProvisionConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ProvisionConfigurationBean {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    @Bean
    ProvisionConfiguration provisionConfiguration(WorkloadProvisionService workloadProvisionService) {
        return ProvisionConfiguration.builder()
                .workloadProvisionService(workloadProvisionService)
                .build();
    }
}
