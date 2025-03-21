package com.witboost.provisioning.dq.sifflet.config;

import com.witboost.provisioning.dq.sifflet.service.provision.WorkloadProvisionService;
import com.witboost.provisioning.framework.service.ProvisionConfiguration;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProvisionConfigurationBean {

    @Bean
    ProvisionConfiguration provisionConfiguration(WorkloadProvisionService workloadProvisionService) {
        return ProvisionConfiguration.builder()
                .workloadProvisionService(workloadProvisionService)
                .build();
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }
}
