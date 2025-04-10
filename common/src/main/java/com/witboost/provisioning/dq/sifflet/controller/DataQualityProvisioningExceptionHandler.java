package com.witboost.provisioning.dq.sifflet.controller;

import com.witboost.provisioning.dq.sifflet.model.DataQualityProvisioningException;
import com.witboost.provisioning.framework.common.ErrorBuilder;
import com.witboost.provisioning.framework.openapi.model.RequestValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DataQualityProvisioningExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(DataQualityProvisioningExceptionHandler.class);

    @ExceptionHandler({DataQualityProvisioningException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected RequestValidationError handleConflict(DataQualityProvisioningException ex) {
        logger.error("DataQualityProvisioningException error:", ex);
        return ErrorBuilder.buildRequestValidationError(ex.getFailedOperation());
    }
}
