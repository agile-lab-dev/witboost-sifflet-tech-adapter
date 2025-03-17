package com.witboost.provisioning.dq.sifflet.service.provision;

import com.witboost.provisioning.dq.sifflet.client.SourceManager;
import com.witboost.provisioning.framework.service.ProvisionService;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.request.ProvisionOperationRequest;
import com.witboost.provisioning.model.request.ReverseProvisionOperationRequest;
import com.witboost.provisioning.model.status.ProvisionInfo;
import com.witboost.provisioning.model.status.ReverseProvisionInfo;
import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkloadProvisionService implements ProvisionService {

    @Autowired
    SourceManager sourceManager;

    @Override
    public Either<FailedOperation, ProvisionInfo> provision(
            ProvisionOperationRequest<?, ? extends Specific> operationRequest) {

        return ProvisionService.super.provision(operationRequest);
    }

    @Override
    public Either<FailedOperation, ProvisionInfo> unprovision(
            ProvisionOperationRequest<?, ? extends Specific> operationRequest) {
        // TODO Remember to remove the super call and implement the unprovision for the workload.
        return ProvisionService.super.unprovision(operationRequest);
    }

    @Override
    public Either<FailedOperation, ReverseProvisionInfo> reverseProvision(
            ReverseProvisionOperationRequest<? extends Specific> operationRequest) {
        // TODO Remember to remove the super call and implement the reverse provision for the workload.
        //  If your tech adapter doesn't support reverse provision, simply remove this override
        return ProvisionService.super.reverseProvision(operationRequest);
    }
}
