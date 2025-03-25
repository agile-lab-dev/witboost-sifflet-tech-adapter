package com.witboost.provisioning.dq.sifflet.service.provision;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.witboost.provisioning.dq.sifflet.cli.WorkspaceManager;
import com.witboost.provisioning.dq.sifflet.client.SourceManager;
import com.witboost.provisioning.dq.sifflet.model.AthenaEntity;
import com.witboost.provisioning.dq.sifflet.model.SiffletProvisionOutput;
import com.witboost.provisioning.dq.sifflet.model.SiffletSpecific;
import com.witboost.provisioning.dq.sifflet.model.cli.*;
import com.witboost.provisioning.dq.sifflet.util.ResourceUtils;
import com.witboost.provisioning.model.DataProduct;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.Workload;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import com.witboost.provisioning.model.request.AccessControlOperationRequest;
import com.witboost.provisioning.model.request.ProvisionOperationRequest;
import com.witboost.provisioning.model.request.ReverseProvisionOperationRequest;
import com.witboost.provisioning.parser.Parser;
import io.vavr.control.Either;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.regions.Region;

@ExtendWith(MockitoExtension.class)
class WorkloadProvisionServiceTest {

    @Mock
    SourceManager sourceManager;

    @Mock
    WorkspaceManager workspaceManager;

    @InjectMocks
    WorkloadProvisionService workloadProvisionService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(workloadProvisionService, "iamRole", "arn:aws:iam::myRole");
    }

    @Test
    void provisionCompleted() throws IOException {
        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_athena_sifflet.yml");
        var componentDescriptor =
                Parser.parseComponentDescriptor(ymlDescriptor, Specific.class).get();
        var component = componentDescriptor
                .getDataProduct()
                .getComponentToProvision(componentDescriptor.getComponentIdToProvision())
                .get();
        var workload = Parser.parseComponent(component, Workload.class, SiffletSpecific.class)
                .get();
        var provisionRequest = new ProvisionOperationRequest<>(
                componentDescriptor.getDataProduct(), workload, false, Optional.empty());
        var notification = new Notification.Email("john.doe@witboost.com");
        var expectedMonitors = List.of(
                Monitor.builder()
                        .name("monitoring id")
                        .schedule("@daily")
                        .scheduleTimezone("UTC")
                        .incident(new Incident(Incident.Severity.Low, false))
                        .notifications(List.of(notification))
                        .datasets(
                                List.of(
                                        new Dataset(
                                                "awsathena://athena.eu-west-1.amazonaws.com/AWSDataCatalog.finance_development_exchange_v0_consumable.outputport")))
                        .parameters(new Parameters.FieldDuplicates(JsonNodeFactory.instance.textNode("id")))
                        .build(),
                Monitor.builder()
                        .name("monitoring name")
                        .schedule("@daily")
                        .scheduleTimezone("UTC")
                        .incident(new Incident(Incident.Severity.High, false))
                        .notifications(List.of(notification))
                        .datasets(
                                List.of(
                                        new Dataset(
                                                "awsathena://athena.eu-west-1.amazonaws.com/AWSDataCatalog.finance_development_exchange_v0_consumable.outputport")))
                        .parameters(new Parameters.FieldNulls(
                                "name",
                                "NullEmptyAndWhitespaces",
                                new Parameters.Threshold("Static", "Percentage", "10%")))
                        .build());

        when(sourceManager.provisionSource(
                        new AthenaEntity(
                                "AWSDataCatalog",
                                "finance_development_exchange_v0_consumable",
                                "outputport",
                                Region.EU_WEST_1,
                                "primary",
                                "s3://myoutputs3bucket"),
                        new SiffletSpecific(
                                "@daily", notification, List.of("urn:dmb:cmp:finance:exchange:0:output-port")),
                        "arn:aws:iam::myRole"))
                .thenReturn(right("sourceId"));

        when(sourceManager.attachDomainToSourceDatasets(
                        componentDescriptor.getDataProduct().getDomain(), "sourceId"))
                .thenReturn(Either.right(null));

        Workspace workspace =
                new Workspace("workspaceId", "finance_development_exchange_v0_consumable_outputport", expectedMonitors);
        when(workspaceManager.createOrUpdate(
                        eq("finance_development_exchange_v0_consumable_outputport"), eq(expectedMonitors)))
                .thenReturn(right(workspace));

        var output = workloadProvisionService.provision(provisionRequest);
        assertTrue(output.isRight());
        assertTrue(output.get().getPrivateInfo().isPresent());
        assertEquals(output.get().getPrivateInfo().get(), List.of(new SiffletProvisionOutput("sourceId", workspace)));
    }

    @Test
    void provisionErrorsOnMonitorsCreation() throws IOException {
        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_athena_sifflet.yml");
        var componentDescriptor =
                Parser.parseComponentDescriptor(ymlDescriptor, Specific.class).get();
        var component = componentDescriptor
                .getDataProduct()
                .getComponentToProvision(componentDescriptor.getComponentIdToProvision())
                .get();
        var workload = Parser.parseComponent(component, Workload.class, SiffletSpecific.class)
                .get();
        var provisionRequest = new ProvisionOperationRequest<>(
                componentDescriptor.getDataProduct(), workload, false, Optional.empty());

        var notification = new Notification.Email("john.doe@witboost.com");

        var expectedMonitors = List.of(
                Monitor.builder()
                        .name("monitoring id")
                        .schedule("@daily")
                        .scheduleTimezone("UTC")
                        .incident(new Incident(Incident.Severity.Low, false))
                        .notifications(List.of(notification))
                        .datasets(
                                List.of(
                                        new Dataset(
                                                "awsathena://athena.eu-west-1.amazonaws.com/AWSDataCatalog.finance_development_exchange_v0_consumable.outputport")))
                        .parameters(new Parameters.FieldDuplicates(JsonNodeFactory.instance.textNode("id")))
                        .build(),
                Monitor.builder()
                        .name("monitoring name")
                        .schedule("@daily")
                        .scheduleTimezone("UTC")
                        .incident(new Incident(Incident.Severity.High, false))
                        .notifications(List.of(notification))
                        .datasets(
                                List.of(
                                        new Dataset(
                                                "awsathena://athena.eu-west-1.amazonaws.com/AWSDataCatalog.finance_development_exchange_v0_consumable.outputport")))
                        .parameters(new Parameters.FieldNulls(
                                "name",
                                "NullEmptyAndWhitespaces",
                                new Parameters.Threshold("Static", "Percentage", "10%")))
                        .build());

        when(sourceManager.provisionSource(
                        new AthenaEntity(
                                "AWSDataCatalog",
                                "finance_development_exchange_v0_consumable",
                                "outputport",
                                Region.EU_WEST_1,
                                "primary",
                                "s3://myoutputs3bucket"),
                        new SiffletSpecific(
                                "@daily", notification, List.of("urn:dmb:cmp:finance:exchange:0:output-port")),
                        "arn:aws:iam::myRole"))
                .thenReturn(right("sourceId"));

        when(sourceManager.attachDomainToSourceDatasets(
                        componentDescriptor.getDataProduct().getDomain(), "sourceId"))
                .thenReturn(Either.right(null));

        when(workspaceManager.createOrUpdate(
                        eq("finance_development_exchange_v0_consumable_outputport"), eq(expectedMonitors)))
                .thenReturn(left(new FailedOperation("Error!", List.of())));

        var output = workloadProvisionService.provision(provisionRequest);
        assertTrue(output.isLeft());
        assertEquals(output.getLeft().message(), "Error!");
    }

    @Test
    void provisionErrorsOnSourceCreation() throws IOException {
        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_athena_sifflet.yml");
        var componentDescriptor =
                Parser.parseComponentDescriptor(ymlDescriptor, Specific.class).get();
        var component = componentDescriptor
                .getDataProduct()
                .getComponentToProvision(componentDescriptor.getComponentIdToProvision())
                .get();
        var workload = Parser.parseComponent(component, Workload.class, SiffletSpecific.class)
                .get();
        var provisionRequest = new ProvisionOperationRequest<>(
                componentDescriptor.getDataProduct(), workload, false, Optional.empty());
        var notification = new Notification.Email("john.doe@witboost.com");
        when(sourceManager.provisionSource(
                        new AthenaEntity(
                                "AWSDataCatalog",
                                "finance_development_exchange_v0_consumable",
                                "outputport",
                                Region.EU_WEST_1,
                                "primary",
                                "s3://myoutputs3bucket"),
                        new SiffletSpecific(
                                "@daily", notification, List.of("urn:dmb:cmp:finance:exchange:0:output-port")),
                        "arn:aws:iam::myRole"))
                .thenReturn(left(new FailedOperation("Error!", List.of())));

        var output = workloadProvisionService.provision(provisionRequest);
        assertTrue(output.isLeft());
        assertEquals(output.getLeft().message(), "Error!");
    }

    @Test
    void provisionErrorAttachingDomain() throws IOException {
        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_athena_sifflet.yml");
        var componentDescriptor =
                Parser.parseComponentDescriptor(ymlDescriptor, Specific.class).get();
        var component = componentDescriptor
                .getDataProduct()
                .getComponentToProvision(componentDescriptor.getComponentIdToProvision())
                .get();
        var workload = Parser.parseComponent(component, Workload.class, SiffletSpecific.class)
                .get();
        var provisionRequest = new ProvisionOperationRequest<>(
                componentDescriptor.getDataProduct(), workload, false, Optional.empty());

        var notification = new Notification.Email("john.doe@witboost.com");

        var expectedMonitors = List.of(
                Monitor.builder()
                        .name("monitoring id")
                        .schedule("@daily")
                        .scheduleTimezone("UTC")
                        .incident(new Incident(Incident.Severity.Low, false))
                        .notifications(List.of(notification))
                        .datasets(
                                List.of(
                                        new Dataset(
                                                "awsathena://athena.eu-west-1.amazonaws.com/AWSDataCatalog.finance_development_exchange_v0_consumable.outputport")))
                        .parameters(new Parameters.FieldDuplicates(JsonNodeFactory.instance.textNode("id")))
                        .build(),
                Monitor.builder()
                        .name("monitoring name")
                        .schedule("@daily")
                        .scheduleTimezone("UTC")
                        .incident(new Incident(Incident.Severity.High, false))
                        .notifications(List.of(notification))
                        .datasets(
                                List.of(
                                        new Dataset(
                                                "awsathena://athena.eu-west-1.amazonaws.com/AWSDataCatalog.finance_development_exchange_v0_consumable.outputport")))
                        .parameters(new Parameters.FieldNulls(
                                "name",
                                "NullEmptyAndWhitespaces",
                                new Parameters.Threshold("Static", "Percentage", "10%")))
                        .build());

        when(sourceManager.provisionSource(
                        new AthenaEntity(
                                "AWSDataCatalog",
                                "finance_development_exchange_v0_consumable",
                                "outputport",
                                Region.EU_WEST_1,
                                "primary",
                                "s3://myoutputs3bucket"),
                        new SiffletSpecific(
                                "@daily", notification, List.of("urn:dmb:cmp:finance:exchange:0:output-port")),
                        "arn:aws:iam::myRole"))
                .thenReturn(right("sourceId"));

        when(sourceManager.attachDomainToSourceDatasets(
                        componentDescriptor.getDataProduct().getDomain(), "sourceId"))
                .thenReturn(Either.left(new FailedOperation("Error!", List.of())));

        var output = workloadProvisionService.provision(provisionRequest);
        assertTrue(output.isLeft());
        assertEquals(output.getLeft().message(), "Error!");
    }

    @Test
    void unprovisionCompleted() throws IOException {
        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_athena_sifflet.yml");
        var componentDescriptor =
                Parser.parseComponentDescriptor(ymlDescriptor, Specific.class).get();
        var component = componentDescriptor
                .getDataProduct()
                .getComponentToProvision(componentDescriptor.getComponentIdToProvision())
                .get();
        var workload = Parser.parseComponent(component, Workload.class, SiffletSpecific.class)
                .get();
        var provisionRequest = new ProvisionOperationRequest<>(
                componentDescriptor.getDataProduct(), workload, false, Optional.empty());

        Workspace workspace =
                new Workspace("workspaceId", "finance_development_exchange_v0_consumable_outputport", List.of());
        when(workspaceManager.delete("finance_development_exchange_v0_consumable_outputport"))
                .thenReturn(right(Optional.of(workspace)));

        var output = workloadProvisionService.unprovision(provisionRequest);
        assertTrue(output.isRight());
        assertTrue(output.get().getPrivateInfo().isPresent());
        assertEquals(
                List.of(Optional.of(workspace)), output.get().getPrivateInfo().get());
    }

    @Test
    void unprovisionErrorsOnWorkspaceDeletion() throws IOException {
        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_athena_sifflet.yml");
        var componentDescriptor =
                Parser.parseComponentDescriptor(ymlDescriptor, Specific.class).get();
        var component = componentDescriptor
                .getDataProduct()
                .getComponentToProvision(componentDescriptor.getComponentIdToProvision())
                .get();
        var workload = Parser.parseComponent(component, Workload.class, SiffletSpecific.class)
                .get();
        var provisionRequest = new ProvisionOperationRequest<>(
                componentDescriptor.getDataProduct(), workload, false, Optional.empty());

        when(workspaceManager.delete("finance_development_exchange_v0_consumable_outputport"))
                .thenReturn(left(new FailedOperation("Error!", List.of())));

        var output = workloadProvisionService.unprovision(provisionRequest);
        assertTrue(output.isLeft());
        assertEquals(output.getLeft().message(), "Error!");
    }

    @Test
    void updateAclUnimplemented() {
        var provisionService = new WorkloadProvisionService();
        var expectedError = new FailedOperation(
                "Access control lists update for the operation request is not supported",
                Collections.singletonList(new Problem(
                        "This adapter doesn't support updating access control lists for the received request",
                        Set.of(
                                "Ensure that the adapter is registered correctly for this type of request and that the ProvisionConfiguration is set up to support the requested component",
                                "Please try again. If the problem persists, contact the platform team."))));
        var actual = provisionService.updateAcl(
                new AccessControlOperationRequest<>(new DataProduct<>(), Optional.of(new Workload<>()), Set.of()));
        assertTrue(actual.isLeft());
        assertEquals(expectedError, actual.getLeft());
    }

    @Test
    void reverseProvisionUnimplemented() {
        var provisionService = new WorkloadProvisionService();
        var expectedError = new FailedOperation(
                "Reverse provisioning for the operation request is not supported",
                Collections.singletonList(new Problem(
                        "This adapter doesn't support reverse provisioning for the received request",
                        Set.of(
                                "Ensure that the adapter is registered correctly for this type of request and that the ProvisionConfiguration is set up to support the requested component",
                                "Please try again. If the problem persists, contact the platform team."))));
        var actual = provisionService.reverseProvision(
                new ReverseProvisionOperationRequest<>("useCaseTemplateId", "environment", new Specific(), null));
        assertTrue(actual.isLeft());
        assertEquals(expectedError, actual.getLeft());
    }
}
