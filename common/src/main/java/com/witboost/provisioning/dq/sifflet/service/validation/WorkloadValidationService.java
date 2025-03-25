package com.witboost.provisioning.dq.sifflet.service.validation;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import com.witboost.provisioning.dq.sifflet.model.SiffletSpecific;
import com.witboost.provisioning.dq.sifflet.utils.ErrorUtils;
import com.witboost.provisioning.framework.service.validation.ComponentValidationService;
import com.witboost.provisioning.model.Component;
import com.witboost.provisioning.model.OperationType;
import com.witboost.provisioning.model.OutputPort;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.Workload;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import com.witboost.provisioning.model.request.OperationRequest;
import com.witboost.provisioning.parser.Parser;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;

@org.springframework.stereotype.Component
@Validated
public class WorkloadValidationService implements ComponentValidationService {

    private final Logger logger = LoggerFactory.getLogger(WorkloadValidationService.class);

    // TODO As a future improvement, we could --dry-run to sifflet to validate the monitor creation
    @Override
    public Either<FailedOperation, Void> validate(
            @Valid OperationRequest<?, ? extends Specific> operationRequest, OperationType operationType) {
        logger.info(
                "Validating operation request {}",
                operationRequest
                        .getComponent()
                        .map(Component::getId)
                        .orElse(operationRequest.getDataProduct().getId()));
        var dataProduct = operationRequest.getDataProduct();

        var maybeComponent = operationRequest.getComponent();
        if (maybeComponent.isEmpty()) {
            return left(
                    new FailedOperation(
                            "No component to provision on input descriptor",
                            Collections.singletonList(
                                    new Problem(
                                            "Operation request didn't contain a component to operate with. Expected a component descriptor"))));
        }
        var component = maybeComponent.get();

        if (component instanceof Workload<? extends Specific>) {
            if (component.getSpecific() instanceof SiffletSpecific siffletSpecific) {
                var specificValidation = SiffletValidator.validateSiffletComponent(siffletSpecific);
                if (specificValidation.isLeft()) {
                    logger.error("Error while validating Sifflet specific section: {}", specificValidation.getLeft());
                    return left(specificValidation.getLeft());
                }
                // For each dependency:
                var response = siffletSpecific.getAthenaOutputPorts().stream()
                        .<Either<FailedOperation, Boolean>>map(dependencyId -> {
                            var maybeDependentComponent = dataProduct.getComponentToProvision(dependencyId);
                            if (maybeDependentComponent.isEmpty())
                                return left(new FailedOperation(
                                        String.format(
                                                "Dependency %s is not present on the input descriptor", dependencyId),
                                        Collections.singletonList(new Problem(String.format(
                                                "Cannot find Sifflet dependency %s component on the input descriptor",
                                                dependencyId)))));

                            var dependencyComponent = maybeDependentComponent.get();
                            var eitherOutputPort =
                                    Parser.parseComponent(dependencyComponent, OutputPort.class, Specific.class);
                            if (eitherOutputPort.isLeft()) return left(eitherOutputPort.getLeft());

                            return right(true);
                        })
                        .toList();
                return ErrorUtils.mergeSequence(response, "occurred while validating Sifflet workload and dependencies")
                        .map(l -> null);

            } else {
                String errorMessage = String.format(
                        "The specific section of the component %s doesn't have the expected schema", component.getId());
                logger.error(errorMessage);
                return left(new FailedOperation(errorMessage, Collections.singletonList(new Problem(errorMessage))));
            }
        }
        // If we arrive here, provisioner errors, so we call the super implementation
        return ComponentValidationService.super.validate(operationRequest, operationType);
    }
}
