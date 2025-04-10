package com.witboost.provisioning.dq.sifflet.service.provision;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import com.witboost.provisioning.dq.sifflet.cli.WorkspaceManager;
import com.witboost.provisioning.dq.sifflet.client.SourceManager;
import com.witboost.provisioning.dq.sifflet.model.*;
import com.witboost.provisioning.dq.sifflet.model.athena.AthenaEntity;
import com.witboost.provisioning.dq.sifflet.model.cli.Dataset;
import com.witboost.provisioning.dq.sifflet.model.cli.Monitor;
import com.witboost.provisioning.dq.sifflet.model.cli.Workspace;
import com.witboost.provisioning.dq.sifflet.service.validation.SiffletValidator;
import com.witboost.provisioning.dq.sifflet.utils.ErrorUtils;
import com.witboost.provisioning.framework.service.ProvisionService;
import com.witboost.provisioning.model.DataProduct;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.Workload;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import com.witboost.provisioning.model.request.ProvisionOperationRequest;
import com.witboost.provisioning.model.status.ProvisionInfo;
import io.vavr.control.Either;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class WorkloadProvisionService implements ProvisionService {

    private final Logger logger = LoggerFactory.getLogger(WorkloadProvisionService.class);

    @Autowired
    SourceManager sourceManager;

    @Autowired
    WorkspaceManager workspaceManager;

    @Value("${sifflet.athena.iamRole}")
    private String iamRole;

    @Override
    public Either<FailedOperation, ProvisionInfo> provision(
            ProvisionOperationRequest<?, ? extends Specific> operationRequest) {

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
                        .map(id -> this.provisionResources(operationRequest.getDataProduct(), siffletSpecific, id))
                        .toList();

                return ErrorUtils.mergeSequence(response, "occurred while provisioning Sifflet workload")
                        .map(output -> ProvisionInfo.builder()
                                .privateInfo(output.isEmpty() ? Optional.empty() : Optional.of(output.get(0)))
                                .build());

            } else {
                String errorMessage = String.format(
                        "The specific section of the component %s doesn't have the expected schema", component.getId());
                logger.error(errorMessage);
                return left(new FailedOperation(errorMessage, Collections.singletonList(new Problem(errorMessage))));
            }
        }
        // If we arrive here, provisioner errors, so we call the super implementation
        return ProvisionService.super.provision(operationRequest);
    }

    @Override
    public Either<FailedOperation, ProvisionInfo> unprovision(
            ProvisionOperationRequest<?, ? extends Specific> operationRequest) {
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
                var response = siffletSpecific.getAthenaOutputPorts().stream()
                        .map(id -> this.unprovisionResources(operationRequest.getDataProduct(), id))
                        .toList();

                return ErrorUtils.mergeSequence(response, "occurred while unprovisioning Sifflet workload")
                        .map(output -> ProvisionInfo.builder()
                                .privateInfo(Optional.of(output))
                                .build());

            } else {
                String errorMessage = String.format(
                        "The specific section of the component %s doesn't have the expected schema", component.getId());
                logger.error(errorMessage);
                return left(new FailedOperation(errorMessage, Collections.singletonList(new Problem(errorMessage))));
            }
        }
        // If we arrive here, provisioner errors, so we call the super implementation
        return ProvisionService.super.unprovision(operationRequest);
    }

    private Either<FailedOperation, SiffletProvisionOutput> provisionResources(
            DataProduct<?> dataProduct, SiffletSpecific siffletSpecific, String dependencyId) {

        var maybeDependentComponent = dataProduct.getComponentToProvision(dependencyId);
        if (maybeDependentComponent.isEmpty())
            return left(new FailedOperation(
                    String.format("Dependency %s is not present on the input descriptor", dependencyId),
                    Collections.singletonList(new Problem(String.format(
                            "Cannot find Sifflet dependency %s component on the input descriptor", dependencyId)))));

        var dependencyComponent = maybeDependentComponent.get();

        var dependencyOutputs = SiffletValidator.extractAndValidateSiffletOutputPortDependency(dependencyComponent);
        if (dependencyOutputs.isLeft()) {
            logger.error(
                    "Error while extracting information from Output Port marked as dependency: {}",
                    dependencyOutputs.getLeft());
            return left(dependencyOutputs.getLeft());
        }
        AthenaEntity athenaEntity = dependencyOutputs.get()._1();
        SiffletDataContract siffletDataContract = dependencyOutputs.get()._2();

        logger.debug("Athena Data Contract with Sifflet monitors parsed successfully: {}", siffletDataContract);
        // Call source manager
        logger.debug(
                "Upserting source from source data {} and sifflet specific data {}", athenaEntity, siffletSpecific);
        var provisionSourceOutput = sourceManager.provisionSource(athenaEntity, siffletSpecific, iamRole);
        if (provisionSourceOutput.isLeft()) {
            logger.error(
                    "Error while upserting source from data {}. Details: {}",
                    athenaEntity,
                    provisionSourceOutput.getLeft());
            return left(provisionSourceOutput.getLeft());
        }
        logger.info("Source with ID provisioned '{}' successfully", provisionSourceOutput.get());

        var attachDomainToSourceDatasets =
                sourceManager.attachDomainToSourceDatasets(dataProduct.getDomain(), provisionSourceOutput.get());
        if (attachDomainToSourceDatasets.isLeft()) return Either.left(attachDomainToSourceDatasets.getLeft());

        // Create workspace
        // Define workspace name (<dbName>_<tableName>)
        String workspaceName = getWorkspaceName(athenaEntity);

        /* Get monitors and:
           - Define URI for monitors to be stored on datasets[].uri
           - Define notifications to be stored on notifications[]
        */
        List<Monitor> monitors = siffletDataContract.getSiffletMonitors().stream()
                .map(monitor -> monitor.toBuilder()
                        .dataset(new Dataset(buildDatasetUri(athenaEntity)))
                        .notification(siffletSpecific.getNotification())
                        .build())
                .toList();

        logger.debug("Upserting workspace {} with monitor list {}", workspaceName, monitors);

        // Call workspace manager
        var provisionWorkspaceOutput = workspaceManager.createOrUpdate(workspaceName, monitors);
        if (provisionWorkspaceOutput.isLeft()) return left(provisionWorkspaceOutput.getLeft());

        return right(new SiffletProvisionOutput(provisionSourceOutput.get(), provisionWorkspaceOutput.get()));
    }

    private Either<FailedOperation, Optional<Workspace>> unprovisionResources(
            DataProduct<?> dataProduct, String dependencyId) {
        logger.info("Executing unprovision of monitors for output port '{}'", dependencyId);
        var maybeDependentComponent = dataProduct.getComponentToProvision(dependencyId);
        if (maybeDependentComponent.isEmpty())
            return left(new FailedOperation(
                    String.format("Dependency %s is not present on the input descriptor", dependencyId),
                    Collections.singletonList(new Problem(String.format(
                            "Cannot find Sifflet dependency %s component on the input descriptor", dependencyId)))));

        var dependencyComponent = maybeDependentComponent.get();
        var dependencyOutputs = SiffletValidator.extractAndValidateSiffletOutputPortDependency(dependencyComponent);
        if (dependencyOutputs.isLeft()) {
            logger.error(
                    "Error while extracting information from Output Port marked as dependency: {}",
                    dependencyOutputs.getLeft());
            return left(dependencyOutputs.getLeft());
        }
        AthenaEntity athenaEntity = dependencyOutputs.get()._1();

        // Define workspace name (<dbName>_<tableName>)
        String workspaceName = getWorkspaceName(athenaEntity);
        var output = this.workspaceManager.delete(workspaceName);
        if (output.isRight()) {
            logger.info("Unprovision of monitors for Output Port '{}' completed successfully", dependencyId);
        }
        return output;
    }

    private String getWorkspaceName(AthenaEntity athenaEntity) {
        return String.format("%s_%s", athenaEntity.getDatabase(), athenaEntity.getName())
                .replaceAll("[^\\w_\\-.]", "_");
    }

    private String buildDatasetUri(AthenaEntity athenaEntity) {
        // Format: awsathena://athena.<REGION>.amazonaws.com/<CATALOG>.<DB>.<TABLE>
        return String.format(
                "awsathena://athena.%s.amazonaws.com/%s.%s.%s",
                athenaEntity.getRegion(),
                athenaEntity.getCatalog(),
                athenaEntity.getDatabase(),
                athenaEntity.getName());
    }
}
