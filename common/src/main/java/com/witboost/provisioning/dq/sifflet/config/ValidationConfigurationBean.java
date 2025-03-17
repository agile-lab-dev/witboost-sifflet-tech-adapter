package com.witboost.provisioning.dq.sifflet.config;

import com.witboost.provisioning.dq.sifflet.service.validation.WorkloadValidationService;
import com.witboost.provisioning.framework.service.validation.ValidationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfigurationBean {

    @Bean
    ValidationConfiguration validationConfiguration(WorkloadValidationService workloadValidationService) {
        return ValidationConfiguration.builder()
                .workloadValidationService(workloadValidationService)
                .build();
    }
}
