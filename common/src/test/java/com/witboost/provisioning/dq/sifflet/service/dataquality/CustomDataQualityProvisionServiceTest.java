package com.witboost.provisioning.dq.sifflet.service.dataquality;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

import com.witboost.provisioning.dq.sifflet.client.RulesManager;
import com.witboost.provisioning.dq.sifflet.model.DataQualityProvisioningException;
import com.witboost.provisioning.dq.sifflet.model.client.GetDatasetRulesResponse;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.*;
import io.vavr.control.Either;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class CustomDataQualityProvisionServiceTest {

    @Mock
    private RulesManager rulesManager;

    @InjectMocks
    private CustomDataQualityProvisionService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getDataQualityResult_shouldReturnResults_whenSuccess() {
        OutputPortRequest request = new OutputPortRequest();
        OutputPortRequestOutputportSpecific outputportSpecific = new OutputPortRequestOutputportSpecific();
        outputportSpecific.setView(new OutputPortRequestOutputportSpecificView("catalog", "name", "view"));
        request.setOutputport(
                new OutputPortRequestOutputport("outputport", "op-id", "op-desc", "op-name", outputportSpecific));

        GetDatasetRulesResponse.RuleData ruleData = new GetDatasetRulesResponse.RuleData();
        ruleData.setName("Rule 1");
        ruleData.setId("rule-1");

        List<GetDatasetRulesResponse.RuleData> rules = List.of(ruleData);
        DataQualityResult dataQualityResult = new DataQualityResult();
        dataQualityResult.setName("Rule 1");
        dataQualityResult.setLastRunStatus("SUCCESS");
        List<DataQualityResult> dataQualityResults = List.of(dataQualityResult);

        when(rulesManager.getDatasetRules(anyString(), anyString())).thenReturn(Either.right(rules));
        when(rulesManager.createResultForFrontend(anyList(), anyInt())).thenReturn(Either.right(dataQualityResults));

        List<DataQualityResult> results = service.getDataQualityResult(request);

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).getName()).isEqualTo("Rule 1");
    }

    @Test
    void getDataQualityResult_shouldThrowException_whenOutputPortIsNull() {
        OutputPortRequest request = new OutputPortRequest();
        request.setOutputport(null);
        DataQualityProvisioningException exception = assertThrows(DataQualityProvisioningException.class, () -> {
            service.getDataQualityResult(request);
        });

        assertThat(exception.getMessage()).contains("The provided input is invalid");
    }

    @Test
    void getDataQualityResult_shouldThrowException_whenDatasetRulesFails() {
        OutputPortRequest request = new OutputPortRequest();
        OutputPortRequestOutputportSpecific outputportSpecific = new OutputPortRequestOutputportSpecific();
        outputportSpecific.setView(new OutputPortRequestOutputportSpecificView("catalog", "name", "view"));
        request.setOutputport(
                new OutputPortRequestOutputport("outputport", "op-id", "op-desc", "op-name", outputportSpecific));

        when(rulesManager.getDatasetRules(anyString(), anyString()))
                .thenReturn(Either.left(new FailedOperation(
                        "Failed to get dataset rules", Collections.singletonList(new Problem("Dataset rules error")))));

        DataQualityProvisioningException exception = assertThrows(DataQualityProvisioningException.class, () -> {
            service.getDataQualityResult(request);
        });

        assertThat(exception.getMessage()).contains("Failed to get dataset rules");
    }

    @Test
    void getDataQualityResult_shouldThrowException_whenCreateResultForFrontendFails() {
        OutputPortRequest request = new OutputPortRequest();
        OutputPortRequestOutputportSpecific outputportSpecific = new OutputPortRequestOutputportSpecific();
        outputportSpecific.setView(new OutputPortRequestOutputportSpecificView("catalog", "name", "view"));
        request.setOutputport(
                new OutputPortRequestOutputport("outputport", "op-id", "op-desc", "op-name", outputportSpecific));

        List<GetDatasetRulesResponse.RuleData> rules = List.of(new GetDatasetRulesResponse.RuleData());
        when(rulesManager.getDatasetRules(anyString(), anyString())).thenReturn(Either.right(rules));
        when(rulesManager.createResultForFrontend(anyList(), anyInt()))
                .thenReturn(Either.left(new FailedOperation(
                        "Failed to create result for frontend",
                        Collections.singletonList(new Problem("Result creation error")))));

        DataQualityProvisioningException exception = assertThrows(DataQualityProvisioningException.class, () -> {
            service.getDataQualityResult(request);
        });

        assertThat(exception.getMessage()).contains("Failed to create result for frontend");
    }

    @Test
    void getDataQualityResult_shouldThrowException_whenUnexpectedErrorOccurs() {
        OutputPortRequest request = new OutputPortRequest();
        when(rulesManager.getDatasetRules(anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        DataQualityProvisioningException exception = assertThrows(DataQualityProvisioningException.class, () -> {
            service.getDataQualityResult(request);
        });

        assertThat(exception.getMessage()).contains("Unexpected error while getting data quality results");
    }
}
