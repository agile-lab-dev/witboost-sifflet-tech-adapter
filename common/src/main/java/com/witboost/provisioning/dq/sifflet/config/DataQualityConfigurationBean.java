package com.witboost.provisioning.dq.sifflet.config;

import com.witboost.provisioning.dq.sifflet.service.dataquality.CustomDataQualityProvisionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataQualityConfigurationBean {

    @Bean
    CustomDataQualityProvisionService customDataQualityProvisionService() {
        return new CustomDataQualityProvisionService();
    }
}
