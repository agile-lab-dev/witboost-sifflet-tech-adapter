package com.witboost.provisioning.dq.sifflet.model;

import com.witboost.provisioning.model.common.FailedOperation;
import lombok.Getter;

@Getter
public class DataQualityProvisioningException extends RuntimeException {

    private final FailedOperation failedOperation;

    public DataQualityProvisioningException(String message, FailedOperation failedOperation) {
        super(message);
        this.failedOperation = failedOperation;
    }

    public DataQualityProvisioningException(String message, FailedOperation failedOperation, Throwable cause) {
        super(message, cause);
        this.failedOperation = failedOperation;
    }
}
