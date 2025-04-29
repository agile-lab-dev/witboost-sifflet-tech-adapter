package com.witboost.provisioning.dq.sifflet.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.witboost.provisioning.dq.sifflet.model.client.*;
import com.witboost.provisioning.dq.sifflet.utils.RestClientHelper;
import com.witboost.provisioning.model.common.FailedOperation;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.DataQualityResult;
import io.vavr.control.Either;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RulesManagerTest {

    @Mock
    SourceManager sourceManager;

    @InjectMocks
    private RulesManager rulesManager;

    private RulesManager rulesManagerSpy;

    @Mock
    RestClientHelper restClientHelper;

    @BeforeEach
    void setUp() {
        rulesManagerSpy = spy(rulesManager);
    }

    @BeforeEach
    void init() {
        String basePath = "https://base/path";
        ReflectionTestUtils.setField(rulesManager, "basePath", basePath);
        ReflectionTestUtils.setField(rulesManager, "token", "test-token");
    }

    @Test
    void getDatasetRules_shouldReturnRules_whenSuccess() {
        String database = "test-db";
        String datasetName = "test-dataset";

        Source mockSource = new Source();
        mockSource.setId("source-123");

        Dataset mockDataset = new Dataset();
        mockDataset.setId("dataset-456");
        mockDataset.setName(datasetName);

        GetDatasetRulesResponse.RuleData ruleData = new GetDatasetRulesResponse.RuleData();
        ruleData.setId("rule-1");
        ruleData.setName("Rule 1");
        ruleData.setRuleLabel("label-1");
        ruleData.setCriticality(2);
        ruleData.setLastRunId("last-run-id");
        GetDatasetRulesResponse mockRulesResponse = new GetDatasetRulesResponse();
        GetDatasetRulesResponse.SearchRules searchRules = new GetDatasetRulesResponse.SearchRules();
        searchRules.setData(List.of(ruleData));
        mockRulesResponse.setSearchRules(searchRules);

        when(sourceManager.getSourceFromName(any())).thenReturn(Either.right(Optional.of(mockSource)));

        when(sourceManager.getDatasetsForSource(any())).thenReturn(Either.right(List.of(mockDataset)));

        when(restClientHelper.performGetRequest(
                        anyString(), anyString(), eq(GetDatasetRulesResponse.class), anyBoolean()))
                .thenReturn(mockRulesResponse);

        Either<FailedOperation, List<GetDatasetRulesResponse.RuleData>> result =
                rulesManager.getDatasetRules(database, datasetName);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().size()).isEqualTo(1);
        assertThat(result.get().get(0).getName()).isEqualTo("Rule 1");
    }

    @Test
    void getDatasetRules_shouldReturnError_whenSourceNotFound() {
        String database = "nonexistent-db";
        String datasetName = "nonexistent-dataset";

        when(sourceManager.getSourceFromName(any())).thenReturn(Either.right(Optional.empty()));

        Either<FailedOperation, List<GetDatasetRulesResponse.RuleData>> result =
                rulesManager.getDatasetRules(database, datasetName);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("Source not found for database: " + database);
    }

    @Test
    void getDatasetRules_shouldReturnError_whenApiFails() {
        String database = "test-db";
        String datasetName = "test-dataset";
        Source mockSource = new Source();
        mockSource.setId("source-123");

        when(sourceManager.getSourceFromName(any())).thenReturn(Either.right(Optional.of(mockSource)));

        Dataset dataset = new Dataset();
        dataset.setId("dataset-123");
        dataset.setName(datasetName);

        when(sourceManager.getDatasetsForSource(any())).thenReturn(Either.right(List.of(dataset)));

        String errorMessage = "Unexpected exception";

        when(restClientHelper.performGetRequest(
                        anyString(), anyString(), eq(GetDatasetRulesResponse.class), anyBoolean()))
                .thenThrow(new RuntimeException(errorMessage));

        Either<FailedOperation, List<GetDatasetRulesResponse.RuleData>> result =
                rulesManager.getDatasetRules(database, datasetName);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains(errorMessage);
    }

    @Test
    void getLastRuns_shouldReturnError_whenRequestFails() {
        String ruleId = "rule-123";
        String errorMessage = "Internal Server Error";

        when(restClientHelper.performGetRequest(anyString(), anyString(), eq(GetRuleRunsResponse.class), anyBoolean()))
                .thenThrow(new RuntimeException(errorMessage));

        Either<FailedOperation, List<GetRuleRunsResponse.DataItem>> result = rulesManager.getLastRuns(ruleId, 10);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains(errorMessage);
    }

    @Test
    void createResultForFrontend_shouldReturnCorrectData() {
        GetDatasetRulesResponse.RuleData ruleData = new GetDatasetRulesResponse.RuleData();
        ruleData.setId("rule-1");
        ruleData.setName("Rule 1");
        ruleData.setRuleLabel("label-1");
        ruleData.setCriticality(2);
        ruleData.setLastRunId("last-run-id");

        GetRuleRunsResponse.DataItem run1 = new GetRuleRunsResponse.DataItem();
        run1.setId("run-1");
        run1.setStartDate(45896);
        run1.setStatus(GetRuleRunsResponse.Status.SUCCESS);
        run1.setResult("result-1");

        List<GetRuleRunsResponse.DataItem> mockRuns = List.of(run1);

        when(rulesManagerSpy.getLastRuns(any(), anyInt())).thenReturn(Either.right(mockRuns));

        Either<FailedOperation, List<DataQualityResult>> result =
                rulesManagerSpy.createResultForFrontend(List.of(ruleData), 3);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().size()).isEqualTo(1);
        assertThat(result.get().get(0).getName()).isEqualTo("Rule 1");
    }

    @Test
    void getDatasetRules_shouldReturnError_whenDatasetNotFound() {
        String database = "test-db";
        String datasetName = "missing-dataset";

        Source mockSource = new Source();
        mockSource.setId("source-123");

        when(sourceManager.getSourceFromName(any())).thenReturn(Either.right(Optional.of(mockSource)));

        when(sourceManager.getDatasetsForSource(any())).thenReturn(Either.right(List.of()));

        Either<FailedOperation, List<GetDatasetRulesResponse.RuleData>> result =
                rulesManager.getDatasetRules(database, datasetName);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("Seems that there is no dataset in Sifflet");
    }

    @Test
    void createResultForFrontend_shouldReturnError_whenGetLastNRunsFails() {
        GetDatasetRulesResponse.RuleData ruleData = new GetDatasetRulesResponse.RuleData();
        ruleData.setId("rule-1");
        ruleData.setName("Rule 1");
        ruleData.setRuleLabel("label-1");
        ruleData.setCriticality(2);
        ruleData.setLastRunId("last-run-id");

        when(rulesManagerSpy.getLastRuns(any(), anyInt()))
                .thenReturn(Either.left(new FailedOperation("Error fetching last runs", Collections.emptyList())));

        Either<FailedOperation, List<DataQualityResult>> result =
                rulesManagerSpy.createResultForFrontend(List.of(ruleData), 3);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("Error fetching last runs");
    }

    @Test
    void getDatasetRules_shouldReturnError_whenDatasetsForSourceFails() {
        String database = "test-db";
        String datasetName = "test-dataset";

        Source mockSource = new Source();
        mockSource.setId("source-123");

        when(sourceManager.getSourceFromName(any())).thenReturn(Either.right(Optional.of(mockSource)));

        when(sourceManager.getDatasetsForSource(any()))
                .thenReturn(Either.left(new FailedOperation("Error fetching datasets", Collections.emptyList())));

        Either<FailedOperation, List<GetDatasetRulesResponse.RuleData>> result =
                rulesManager.getDatasetRules(database, datasetName);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("Error fetching datasets");
    }

    @Test
    void getDatasetRules_shouldReturnError_whenApiReturnsNotFound() {
        String database = "test-db";
        String datasetName = "test-dataset";

        Source mockSource = new Source();
        mockSource.setId("source-123");

        Dataset mockDataset = new Dataset();
        mockDataset.setId("dataset-456");
        mockDataset.setName(datasetName);

        String errorMessage = "Not found";

        when(sourceManager.getSourceFromName(any())).thenReturn(Either.right(Optional.of(mockSource)));
        when(sourceManager.getDatasetsForSource(any())).thenReturn(Either.right(List.of(mockDataset)));

        when(restClientHelper.performGetRequest(
                        anyString(), anyString(), eq(GetDatasetRulesResponse.class), anyBoolean()))
                .thenThrow(new RuntimeException(errorMessage));

        Either<FailedOperation, List<GetDatasetRulesResponse.RuleData>> result =
                rulesManager.getDatasetRules(database, datasetName);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains(errorMessage);
    }

    @Test
    void getLastRuns_shouldSortRunsByStartDateDescending() {
        String ruleId = "rule-123";

        GetRuleRunsResponse.DataItem run1 = new GetRuleRunsResponse.DataItem();
        run1.setId("run-1");
        run1.setStartDate(100);

        GetRuleRunsResponse.DataItem run2 = new GetRuleRunsResponse.DataItem();
        run2.setId("run-2");
        run2.setStartDate(300);

        GetRuleRunsResponse.DataItem run3 = new GetRuleRunsResponse.DataItem();
        run3.setId("run-3");
        run3.setStartDate(200);

        GetRuleRunsResponse response = new GetRuleRunsResponse();
        response.setData(List.of(run1, run2, run3));

        when(restClientHelper.performGetRequest(anyString(), anyString(), eq(GetRuleRunsResponse.class), anyBoolean()))
                .thenReturn(response);

        Either<FailedOperation, List<GetRuleRunsResponse.DataItem>> result = rulesManager.getLastRuns(ruleId, 3);

        assertThat(result.isRight()).isTrue();
        List<GetRuleRunsResponse.DataItem> runs = result.get();

        assertThat(runs).extracting(GetRuleRunsResponse.DataItem::getId).containsExactly("run-2", "run-3", "run-1");
    }

    @Test
    void createResultForFrontend_shouldCatchUnexpectedException() {
        GetDatasetRulesResponse.RuleData ruleData = new GetDatasetRulesResponse.RuleData();
        ruleData.setId("rule-1");

        when(rulesManagerSpy.getLastRuns(any(), anyInt())).thenThrow(new NullPointerException("TestError!"));

        Either<FailedOperation, List<DataQualityResult>> result =
                rulesManagerSpy.createResultForFrontend(List.of(ruleData), 3);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("Unexpected error while creating frontend result");
        assertThat(result.getLeft().problems().get(0).getMessage()).contains("TestError!");
    }
}
