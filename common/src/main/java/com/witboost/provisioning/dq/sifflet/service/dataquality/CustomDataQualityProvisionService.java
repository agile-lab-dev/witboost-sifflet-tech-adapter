package com.witboost.provisioning.dq.sifflet.service.dataquality;

import com.witboost.provisioning.dq.sifflet.client.RulesManager;
import com.witboost.provisioning.dq.sifflet.model.DataQualityProvisioningException;
import com.witboost.provisioning.dq.sifflet.model.client.GetDatasetRulesResponse;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.DataQualityResult;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.OutputPortRequest;
import io.vavr.control.Either;
import java.util.Collections;
import java.util.List;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@NoArgsConstructor
@Service
public class CustomDataQualityProvisionService implements DataQualityService {

    @Autowired
    RulesManager rulesManager;

    private final Logger logger = LoggerFactory.getLogger(CustomDataQualityProvisionService.class);

    @Override
    public List<DataQualityResult> getDataQualityResult(OutputPortRequest outputPortRequest) {

        try {
            var outputPort = outputPortRequest.getOutputport();

            if (outputPort == null) {

                String error = "The provided input is invalid";
                logger.error(error);
                throw new DataQualityProvisioningException(
                        "Error while getting data quality results. The provided input is invalid.",
                        new FailedOperation(
                                error,
                                Collections.singletonList(
                                        new Problem("Ensure that the 'outputport' field is provided"))));
            }

            var view = outputPort.getSpecific().getView();

            Either<FailedOperation, List<GetDatasetRulesResponse.RuleData>> rules =
                    rulesManager.getDatasetRules(view.getDatabase(), view.getName());

            if (rules.isLeft())
                throw new DataQualityProvisioningException(
                        rules.getLeft().message(),
                        new FailedOperation(
                                rules.getLeft().message(), rules.getLeft().problems()));

            Either<FailedOperation, List<DataQualityResult>> lastRules =
                    rulesManager.createResultForFrontend(rules.get(), 10);
            if (lastRules.isLeft())
                throw new DataQualityProvisioningException(
                        lastRules.getLeft().message(),
                        new FailedOperation(
                                lastRules.getLeft().message(),
                                lastRules.getLeft().problems()));

            return lastRules.get();

        } catch (Exception ex) {
            String error = "Unexpected error while getting data quality results. Details: " + ex.getMessage();
            logger.error(error, ex);
            throw new DataQualityProvisioningException(
                    error,
                    new FailedOperation(
                            error,
                            Collections.singletonList(new Problem("Error while getting data quality results", ex))),
                    ex);
        }
    }
}
