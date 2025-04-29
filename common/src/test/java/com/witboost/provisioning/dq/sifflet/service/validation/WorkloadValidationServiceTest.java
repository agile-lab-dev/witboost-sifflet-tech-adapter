package com.witboost.provisioning.dq.sifflet.service.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.witboost.provisioning.dq.sifflet.model.SiffletSpecific;
import com.witboost.provisioning.dq.sifflet.service.dataquality.CustomDataQualityProvisionService;
import com.witboost.provisioning.dq.sifflet.service.provision.WorkloadProvisionService;
import com.witboost.provisioning.dq.sifflet.utils.ResourceUtils;
import com.witboost.provisioning.framework.openapi.model.DescriptorKind;
import com.witboost.provisioning.framework.openapi.model.ProvisioningRequest;
import com.witboost.provisioning.framework.service.ProvisionConfiguration;
import com.witboost.provisioning.framework.service.validation.ValidationConfiguration;
import com.witboost.provisioning.model.DataProduct;
import com.witboost.provisioning.model.OperationType;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.Workload;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.request.ProvisionOperationRequest;
import io.vavr.control.Either;
import java.io.IOException;
import java.util.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestClient;

@SpringBootTest
@AutoConfigureMockMvc
class WorkloadValidationServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkloadProvisionService workloadProvisionService;

    @MockitoBean
    private CustomDataQualityProvisionService customDataQualityProvisionService;

    @MockitoBean
    private RestClient restClient;

    @Autowired
    private ValidationConfiguration validationConfiguration;

    @Autowired
    private ProvisionConfiguration provisionConfiguration;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private ProvisionOperationRequest<?, ? extends Specific> request;

    private WorkloadValidationService workloadValidationService;

    private final String mockValidateEndpoint = "http://127.0.0.1:8888/v1/validate";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workloadValidationService = new WorkloadValidationService();
    }

    @Test
    void testParsingCorrectDescriptor_ShouldReturnValidResponse() throws Exception {
        ProvisioningRequest request = createProvisioningRequest("/descriptor_workload.yml");

        MvcResult result = mockMvc.perform(post(mockValidateEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assert (result.getResponse().getContentAsString().contains("\"valid\":true"));
    }

    @Test
    void testMissingField_ShouldReturnBadRequest() throws Exception {
        ProvisioningRequest request = createProvisioningRequest("/descriptor_workload_missingField.yml");

        MvcResult result = mockMvc.perform(post(mockValidateEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        assertEquals(400, result.getResponse().getStatus());
        String responseContent = result.getResponse().getContentAsString();
        assert (responseContent.contains("athenaOutputPorts must not be empty"));
    }

    @Test
    void testBlankField_ShouldReturnBadRequest() throws Exception {
        ProvisioningRequest request = createProvisioningRequest("/descriptor_workload_blankField.yml");

        MvcResult result = mockMvc.perform(post(mockValidateEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        assertEquals(400, result.getResponse().getStatus());
        String responseContent = result.getResponse().getContentAsString();
        assert (responseContent.contains("dataSourceRefreshCron must not be blank"));
    }

    @Test
    void testMissingComponent_ShouldReturnFailedOperation() {
        DataProduct dataProduct = new DataProduct();
        dataProduct.setId("id");
        when(request.getDataProduct()).thenReturn(dataProduct);
        when(request.getComponent()).thenReturn(Optional.empty());

        Either<FailedOperation, Void> actualRes = workloadValidationService.validate(request, OperationType.PROVISION);

        assert (actualRes.isLeft());
        String expectedError =
                "Operation request didn't contain a component to operate with. Expected a component descriptor";
        assert (actualRes.getLeft().problems().get(0).getMessage().contains(expectedError));
    }

    @Test
    void testWrongSpecificType_ShouldReturnFailedOperation() {
        prepareRequestDataProductAndWokloadMock();

        Either<FailedOperation, Void> actualRes = workloadValidationService.validate(request, OperationType.PROVISION);

        assert (actualRes.isLeft());
        String expectedError = "The specific section of the component id123 doesn't have the expected schema";
        assert (actualRes.getLeft().problems().get(0).getMessage().contains(expectedError));
    }

    @Test
    void testSpecificWorkloadValidationIsLeft_ShouldReturnLeft() {
        prepareRequestDataProductAndWokloadMock();

        FailedOperation failedOperation = new FailedOperation("Validation failed", Collections.emptyList());
        Either<FailedOperation, Void> specificValidation = Either.left(failedOperation);

        WorkloadValidationService validationService = Mockito.spy(workloadValidationService);
        doReturn(specificValidation).when(validationService).validate(any(), any());

        Either<FailedOperation, Void> result = validationService.validate(request, OperationType.PROVISION);

        assertTrue(result.isLeft());
        assertEquals(failedOperation, result.getLeft());
        verify(validationService).validate(request, OperationType.PROVISION);
    }

    private void prepareRequestDataProductAndWokloadMock() {
        DataProduct dataProduct = new DataProduct();
        dataProduct.setId("id");
        when(request.getDataProduct()).thenReturn(dataProduct);

        Workload workload = new Workload();
        workload.setId("id123");
        workload.setName("workload-name");
        workload.setSpecific(new Specific());

        when(request.getDataProduct()).thenReturn(dataProduct);
        when(request.getComponent()).thenReturn(Optional.of(workload));
    }

    @Test
    void testSpecificSiffletValidationIsLeft_ShouldReturnLeft() throws IOException {
        ProvisioningRequest request = createProvisioningRequest("/descriptor_workload.yml");

        try (MockedStatic<SiffletValidator> mockedSiffletValidator = Mockito.mockStatic(SiffletValidator.class)) {
            Either<FailedOperation, Void> mockValidationResult =
                    Either.left(new FailedOperation("Validation error", Collections.emptyList()));
            mockedSiffletValidator
                    .when(() -> SiffletValidator.validateSiffletComponent(any(SiffletSpecific.class)))
                    .thenReturn(mockValidationResult);

            MvcResult result = mockMvc.perform(post(mockValidateEndpoint)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();

            assert (result.getResponse().getContentAsString().contains("\"valid\":false"));
            assert (result.getResponse().getContentAsString().contains("Validation error"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ProvisioningRequest createProvisioningRequest(String resourcePath) throws IOException {
        String ymlDescriptor = ResourceUtils.getContentFromResource(resourcePath);
        return new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);
    }
}
