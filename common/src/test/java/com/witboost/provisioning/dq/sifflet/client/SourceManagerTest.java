package com.witboost.provisioning.dq.sifflet.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.witboost.provisioning.dq.sifflet.model.SiffletSpecific;
import com.witboost.provisioning.dq.sifflet.model.athena.AthenaEntity;
import com.witboost.provisioning.dq.sifflet.model.cli.Notification;
import com.witboost.provisioning.dq.sifflet.model.client.*;
import com.witboost.provisioning.dq.sifflet.utils.RestClientHelper;
import com.witboost.provisioning.model.common.FailedOperation;
import io.vavr.control.Either;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import okhttp3.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.regions.Region;

@ExtendWith(MockitoExtension.class)
class SourceManagerTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SourceManager sourceManager;

    @Mock
    RestClientHelper restClientHelper;

    private SourceManager sourceManagerSpy;

    @BeforeEach
    void setUp() {
        sourceManagerSpy = spy(sourceManager);
    }

    @BeforeEach
    void init() {
        String basePath = "https://base/path";
        ReflectionTestUtils.setField(sourceManager, "basePath", basePath);
        ReflectionTestUtils.setField(sourceManager, "token", "test-token");
    }

    @Test
    void getSourceFromName_shouldReturnSource_whenExists() {
        GetSourcesResponse getSourcesResponse = new GetSourcesResponse();
        Source existingSource = new Source();
        existingSource.setId("1234");
        existingSource.setName("existing-source");
        getSourcesResponse.setData(Collections.singletonList(existingSource));

        when(restClientHelper.performPostRequest(
                        anyString(), anyString(), anyString(), eq(GetSourcesResponse.class), anyBoolean()))
                .thenReturn(getSourcesResponse);

        Either<FailedOperation, Optional<Source>> result = sourceManager.getSourceFromName("existing-source");

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).contains(existingSource);
    }

    @Test
    void getSourceFromName_shouldReturnEmpty_whenNotExists() {
        String sourceName = "non-existent-source";
        GetSourcesResponse mockResponse = new GetSourcesResponse();
        mockResponse.setData(Collections.emptyList());

        when(restClientHelper.performPostRequest(
                        anyString(), anyString(), anyString(), eq(GetSourcesResponse.class), anyBoolean()))
                .thenReturn(mockResponse);

        Either<FailedOperation, Optional<Source>> result = sourceManager.getSourceFromName(sourceName);

        assert (result.isRight());
        assertThat(result.get().isEmpty()).isTrue();
    }

    @Test
    void provisionSource_shouldCreateNewSource_whenNotExists() {
        doReturn(Either.right(Optional.empty())).when(sourceManagerSpy).getSourceFromName(anyString());
        CreateSourceResponse createSourceResponse = mock(CreateSourceResponse.class);
        when(createSourceResponse.getId()).thenReturn("id");
        doReturn(Either.right(createSourceResponse))
                .when(sourceManagerSpy)
                .createSource(any(AthenaEntity.class), any(SiffletSpecific.class), anyString(), anyString());
        Source source = new Source();
        source.setLastrun(new LastRun("SUCCESS", "0123"));
        doReturn(Either.right(source)).when(sourceManagerSpy).getSourceFromID(anyString());

        Either<FailedOperation, String> result = sourceManagerSpy.provisionSource(
                new AthenaEntity(
                        "AwsDataCatalog", "database", "source_table", Region.EU_WEST_1, "primary", "s3://testbucket"),
                new SiffletSpecific("cron", new Notification.Email("john.doe@witboost.com"), List.of()),
                "roleArn");

        assert (result.isRight());
    }

    @Test
    void provisionSource_shouldRefreshExistingSource_whenExists() {
        String sourceId = "existing-source-id";
        Source existingSource = new Source();
        existingSource.setId(sourceId);
        existingSource.setName("existing-source");
        Source source = new Source();
        source.setId("1234");
        source.setName("existing-source");
        source.setLastrun(new LastRun("SUCCESS", "0123"));

        doReturn(Either.right(Optional.of(source))).when(sourceManagerSpy).getSourceFromName(anyString());
        doReturn(Either.right(null)).when(sourceManagerSpy).triggerSourceRefresh(anyString());
        doReturn(Either.right(source)).when(sourceManagerSpy).getSourceFromID(anyString());

        Either<FailedOperation, String> result = sourceManagerSpy.provisionSource(
                new AthenaEntity(
                        "AwsDataCatalog", "database", "source_table", Region.EU_WEST_1, "primary", "s3://testbucket"),
                new SiffletSpecific("cron", new Notification.Email("john.doe@witboost.com"), List.of()),
                "roleArn");

        assert (result.isRight());
    }

    @Test
    void triggerSourceRefresh_shouldSucceed_whenValidSourceId() {
        String sourceId = "12345";

        when(restClientHelper.performPostRequest(anyString(), anyString(), anyString(), eq(Void.class), anyBoolean()))
                .thenReturn(null);

        Either<FailedOperation, Void> result = sourceManager.triggerSourceRefresh(sourceId);

        assert (result.isRight());
        assert (result.get() == null);
    }

    @Test
    void triggerSourceRefresh_shouldFail_whenApiReturnsError() {
        String sourceId = "12345";

        String errorMessage = "errorMessage";
        when(restClientHelper.performPostRequest(anyString(), anyString(), anyString(), eq(Void.class), anyBoolean()))
                .thenThrow(new RuntimeException(errorMessage));

        Either<FailedOperation, Void> result = sourceManagerSpy.triggerSourceRefresh(sourceId);

        assertThat(result.isLeft()).isTrue();
        assertThatCollection(result.getLeft().problems())
                .anyMatch(problem -> problem.getMessage().contains(errorMessage));
    }

    @Test
    void waitForSourceToBeUpdated_shouldReturnTimeoutError_whenTimeoutExceeded() {
        String sourceId = "source-id";
        String sourceName = "source-name";
        Source source = new Source();
        source.setId(sourceId);
        source.setLastrun(new LastRun("RUNNING", "0123"));

        when(restClientHelper.performGetRequest(anyString(), anyString(), eq(Source.class), anyBoolean()))
                .thenReturn(source);

        ReflectionTestUtils.setField(sourceManager, "sourceUpdateTimeoutSeconds", 1);

        Either<FailedOperation, Void> result = sourceManager.waitForSourceToBeUpdated(sourceId, sourceName);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("timeout");
    }

    @Test
    void validateTimeout_shouldThrowException_whenTimeoutIsZero() {
        ReflectionTestUtils.setField(sourceManager, "sourceUpdateTimeoutSeconds", 0);

        assertThatThrownBy(() -> sourceManager.validateTimeout())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void getSourceFromName_shouldReturnError_whenApiFails() {
        String errorMessage = "Bad request";

        when(restClientHelper.performPostRequest(
                        anyString(), anyString(), anyString(), eq(GetSourcesResponse.class), anyBoolean()))
                .thenThrow(new RuntimeException(errorMessage));

        Either<FailedOperation, Optional<Source>> result = sourceManager.getSourceFromName("source-name");

        assertThat(result.isLeft()).isTrue();
        assertThatCollection(result.getLeft().problems())
                .anyMatch(problem -> problem.getMessage().contains(errorMessage));
    }

    @Test
    void createSource_shouldReturnError_whenApiFails() throws JsonProcessingException {
        String errorMessage = "Bad request";

        when(objectMapper.writeValueAsString(any(CreateSourceRequest.class))).thenReturn("jsonBody");

        when(restClientHelper.performPostRequest(
                        anyString(), anyString(), anyString(), eq(CreateSourceResponse.class), anyBoolean()))
                .thenThrow(new RuntimeException(errorMessage));

        Either<FailedOperation, CreateSourceResponse> result = sourceManager.createSource(
                new AthenaEntity("AwsDataCatalog", "database", "table", Region.EU_WEST_1, "primary", "s3://test"),
                new SiffletSpecific("cron", new Notification.Email("john.doe@witboost.com"), List.of()),
                "roleArn",
                "some-token");

        assertThat(result.isLeft()).isTrue();
        assert (result.getLeft().problems().get(0).getMessage().contains(errorMessage));
    }

    @Test
    void triggerSourceRefresh_shouldReturnError_whenUnexpectedResponse() {
        when(restClientHelper.performPostRequest(anyString(), anyString(), anyString(), eq(Void.class), anyBoolean()))
                .thenThrow(new RuntimeException("Unexpected"));

        Either<FailedOperation, Void> result = sourceManager.triggerSourceRefresh("1234");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().problems().get(0).getMessage())
                .contains("An unexpected error occurred while trying to update source with ID '1234'");
    }

    @Test
    void waitForSourceToBeUpdated_shouldSucceed_whenSourceUpdated() {
        String sourceId = "source-id";
        Source source = new Source();
        source.setId(sourceId);
        source.setLastrun(new LastRun("SUCCESS", "0123"));

        doReturn(Either.right(source)).when(sourceManagerSpy).getSourceFromID(anyString());

        Either<FailedOperation, Void> result = sourceManagerSpy.waitForSourceToBeUpdated(sourceId, "source-name");

        assertThat(result.isRight()).isTrue();
    }

    @Test
    void provisionSource_shouldReturnError_whenSourceCreationFails() {
        doReturn(Either.right(Optional.empty())).when(sourceManagerSpy).getSourceFromName(anyString());
        doReturn(Either.left(new FailedOperation("Source creation failed", Collections.emptyList())))
                .when(sourceManagerSpy)
                .createSource(any(AthenaEntity.class), any(SiffletSpecific.class), anyString(), anyString());

        Either<FailedOperation, String> result = sourceManagerSpy.provisionSource(
                new AthenaEntity(
                        "AwsDataCatalog", "database", "source_table", Region.EU_WEST_1, "primary", "s3://testbucket"),
                new SiffletSpecific("cron", new Notification.Email("john.doe@witboost.com"), List.of()),
                "roleArn");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("Source creation failed");
    }

    @Test
    void getSourceFromName_shouldReturnError_whenTimeoutOccurs() {
        String errorMessage = "Request Timeout";
        when(restClientHelper.performPostRequest(
                        anyString(), anyString(), anyString(), eq(GetSourcesResponse.class), anyBoolean()))
                .then(invocation -> {
                    throw new RuntimeException(errorMessage);
                });

        Either<FailedOperation, Optional<Source>> result = sourceManager.getSourceFromName("source-name");

        assertThat(result.isLeft()).isTrue();
        assertThatCollection(result.getLeft().problems())
                .anyMatch(problem -> problem.getMessage().contains(errorMessage));
    }

    @Test
    void provisionSource_shouldReturnError_whenRefreshFails() {
        String sourceId = "existing-source-id";
        Source existingSource = new Source();
        existingSource.setId(sourceId);
        existingSource.setName("existing-source");
        Source source = new Source();
        source.setId("1234");
        source.setName("existing-source");
        source.setLastrun(new LastRun("SUCCESS", "0123"));

        doReturn(Either.right(Optional.of(source))).when(sourceManagerSpy).getSourceFromName(anyString());
        doReturn(Either.left(new FailedOperation("Failed to trigger source refresh", Collections.emptyList())))
                .when(sourceManagerSpy)
                .triggerSourceRefresh(anyString());

        Either<FailedOperation, String> result = sourceManagerSpy.provisionSource(
                new AthenaEntity(
                        "AwsDataCatalog", "database", "source_table", Region.EU_WEST_1, "primary", "s3://testbucket"),
                new SiffletSpecific("cron", new Notification.Email("john.doe@witboost.com"), List.of()),
                "roleArn");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("Failed to trigger source refresh");
    }

    @Test
    void triggerSourceRefresh_shouldReturnError_whenInvalidSourceId() {
        String invalidSourceId = "invalid-source-id";

        String errorMessage = "Bad Request";

        when(restClientHelper.performPostRequest(anyString(), anyString(), anyString(), eq(Void.class), anyBoolean()))
                .then(invocation -> {
                    throw new RuntimeException(errorMessage);
                });

        Either<FailedOperation, Void> result = sourceManager.triggerSourceRefresh(invalidSourceId);

        assertThat(result.isLeft()).isTrue();
        assertThatCollection(result.getLeft().problems())
                .anyMatch(problem -> problem.getMessage().contains(errorMessage));
    }

    @Test
    void waitForSourceToBeUpdated_shouldReturnError_whenSourceUpdateFails() {
        String sourceId = "source-id";
        Source source = new Source();
        source.setId(sourceId);
        source.setName("source-name");
        source.setLastrun(new LastRun("FAILURE", "0123"));

        doReturn(Either.right(source)).when(sourceManagerSpy).getSourceFromID(anyString());

        Either<FailedOperation, Void> result = sourceManagerSpy.waitForSourceToBeUpdated(sourceId, "source-name");

        assertThat(result.isLeft()).isTrue();
        assertThatCollection(result.getLeft().problems())
                .anyMatch(problem -> problem.getMessage().contains("Update of source 'source-name' failed."));
    }

    @Test
    void waitForSourceToBeUpdated_shouldReturnError_whenSourceUpdateIsSkipped() {
        String sourceId = "source-id";
        Source source = new Source();
        source.setId(sourceId);
        source.setName("source-name");

        source.setLastrun(new LastRun("SKIPPED_DATASOURCE_ALREADY_RUNNING", "0123"));

        doReturn(Either.right(source)).when(sourceManagerSpy).getSourceFromID(anyString());

        Either<FailedOperation, Void> result = sourceManagerSpy.waitForSourceToBeUpdated(sourceId, "source-name");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("datasource already running");
    }

    @Test
    void waitForSourceToBeUpdated_shouldReturnError_whenUnexpectedExceptionOccurs() {
        String sourceId = "source-id";
        Source source = new Source();
        source.setId(sourceId);
        source.setLastrun(new LastRun("RUNNING", "0123"));

        doThrow(new RuntimeException("Unexpected error")).when(sourceManagerSpy).getSourceFromID(anyString());

        Either<FailedOperation, Void> result = sourceManagerSpy.waitForSourceToBeUpdated(sourceId, "source-name");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message())
                .contains("An unexpected error waiting for the completion of the update of the source source-id.");
    }

    @Test
    void waitForSourceToBeUpdated_shouldReturnError_whenUnexpectedErrorOccurs() {
        String sourceId = "source-id";

        doThrow(new RuntimeException("Unexpected error")).when(sourceManagerSpy).getSourceFromID(anyString());

        Either<FailedOperation, Void> result = sourceManagerSpy.waitForSourceToBeUpdated(sourceId, "source-name");

        assertThat(result.isLeft()).isTrue();
        assertThatCollection(result.getLeft().problems())
                .anyMatch(problem -> problem.getMessage().contains("Details: Unexpected error"));
    }

    @Test
    void getSourceFromName_shouldReturnError_whenSourceNotFound() {
        String sourceName = "non-existent-source";
        String errorMessage = "Simulated IO Exception";

        when(restClientHelper.performPostRequest(
                        anyString(), anyString(), anyString(), eq(GetSourcesResponse.class), anyBoolean()))
                .then(invocation -> {
                    throw new IOException(errorMessage);
                });

        Either<FailedOperation, Optional<Source>> result = sourceManager.getSourceFromName(sourceName);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("An unexpected error occurred while looking for source named");
        assertThat(result.getLeft().problems())
                .anyMatch(problem -> problem.getMessage().contains(errorMessage));
    }

    @Test
    void createSource_shouldReturnCreateSourceResponse_whenSuccessful() throws JsonProcessingException {
        CreateSourceResponse createSourceResponse = new CreateSourceResponse();
        createSourceResponse.setId("1234");

        when(objectMapper.writeValueAsString(any(CreateSourceRequest.class))).thenReturn("jsonBody");

        when(restClientHelper.performPostRequest(
                        anyString(), anyString(), anyString(), eq(CreateSourceResponse.class), anyBoolean()))
                .thenReturn(createSourceResponse);

        Either<FailedOperation, CreateSourceResponse> result = sourceManager.createSource(
                new AthenaEntity("AwsDataCatalog", "database", "table", Region.EU_WEST_1, "primary", "s3://test"),
                new SiffletSpecific("cron", new Notification.Email("john.doe@witboost.com"), List.of()),
                "roleArn",
                "some-token");

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(createSourceResponse);
        assertThat(result.get().getId()).isEqualTo("1234");
    }

    @Test
    void shouldReturnError_whenUnexpectedExceptionOccursWhileCreatingSource() throws JsonProcessingException {
        AthenaEntity athenaEntity =
                new AthenaEntity("AwsDataCatalog", "database", "table", Region.EU_WEST_1, "primary", "s3://test");

        String errorMessage = "Internal Server Error";

        when(objectMapper.writeValueAsString(any(CreateSourceRequest.class))).thenReturn("jsonBody");

        when(restClientHelper.performPostRequest(anyString(), anyString(), anyString(), any(), anyBoolean()))
                .then(invocation -> {
                    throw new RuntimeException(errorMessage);
                });

        Either<FailedOperation, CreateSourceResponse> result = sourceManager.createSource(
                athenaEntity,
                new SiffletSpecific("cron", new Notification.Email("john.doe@witboost.com"), List.of()),
                "roleArn",
                "source-name");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().problems().get(0).getMessage()).contains(errorMessage);
    }

    @Test
    void attachDomainToSourceDatasets_shouldSucceed_whenDomainAndDatasetsExist() {
        String domainName = "TestDomain";
        String sourceID = "source-123";
        Domain domain = new Domain();
        domain.setId("domain-123");
        domain.setName(domainName);

        Dataset dataset1 = new Dataset();
        dataset1.setUrn("urn:dataset:1");
        Dataset dataset2 = new Dataset();
        dataset2.setUrn("urn:dataset:2");

        doReturn(Either.right(Optional.of(domain))).when(sourceManagerSpy).getDomainFromName(domainName);
        doReturn(Either.right(List.of(dataset1, dataset2)))
                .when(sourceManagerSpy)
                .getDatasetsForSource(sourceID);
        doReturn(Either.right(null))
                .when(sourceManagerSpy)
                .assignDomainToDataSources(anyString(), anyString(), anyList());

        Either<FailedOperation, Void> result = sourceManagerSpy.attachDomainToSourceDatasets(domainName, sourceID);

        assertThat(result.isRight()).isTrue();
    }

    @Test
    void attachDomainToSourceDatasets_domainNotFound() {
        String domainName = "NonExistentDomain";
        String sourceID = "source-123";

        doReturn(Either.right(Optional.empty())).when(sourceManagerSpy).getDomainFromName(domainName);

        Either<FailedOperation, Void> result = sourceManagerSpy.attachDomainToSourceDatasets(domainName, sourceID);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().problems().get(0).getMessage()).contains("Domain not found");
    }

    @Test
    void attachDomainToSourceDatasets_shouldFail_whenFetchingDatasetsFails() {
        String domainName = "TestDomain";
        String sourceID = "source-123";
        Domain domain = new Domain();
        domain.setId("domain-123");

        doReturn(Either.right(Optional.of(domain))).when(sourceManagerSpy).getDomainFromName(domainName);
        doReturn(Either.left(new FailedOperation("Error fetching datasets", Collections.emptyList())))
                .when(sourceManagerSpy)
                .getDatasetsForSource(sourceID);

        Either<FailedOperation, Void> result = sourceManagerSpy.attachDomainToSourceDatasets(domainName, sourceID);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("Error fetching datasets");
    }

    @Test
    void attachDomainToSourceDatasets_shouldFail_whenAssigningDomainFails() {
        String domainName = "TestDomain";
        String sourceID = "source-123";
        Domain domain = new Domain();
        domain.setId("domain-123");

        Dataset dataset = new Dataset();
        dataset.setUrn("urn:dataset:1");

        doReturn(Either.right(Optional.of(domain))).when(sourceManagerSpy).getDomainFromName(domainName);
        doReturn(Either.right(List.of(dataset))).when(sourceManagerSpy).getDatasetsForSource(sourceID);
        doReturn(Either.left(new FailedOperation("Failed to assign domain", Collections.emptyList())))
                .when(sourceManagerSpy)
                .assignDomainToDataSources(anyString(), anyString(), anyList());

        Either<FailedOperation, Void> result = sourceManagerSpy.attachDomainToSourceDatasets(domainName, sourceID);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("Failed to assign domain");
    }

    @Test
    void getDomainFromName_shouldReturnDomain_whenExists() {
        String domainName = "TestDomain";
        Domain domain = new Domain();
        domain.setId("domain-123");
        domain.setName(domainName);

        GetDomainsResponse response = new GetDomainsResponse();
        response.setData(List.of(domain));

        when(restClientHelper.performGetRequest(anyString(), anyString(), eq(GetDomainsResponse.class), anyBoolean()))
                .thenReturn(response);

        Either<FailedOperation, Optional<Domain>> result = sourceManager.getDomainFromName(domainName);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).contains(domain);
    }

    @Test
    void getDomainFromName_shouldReturnEmpty_whenDomainNotFound() {
        String domainName = "NonExistentDomain";
        GetDomainsResponse response = new GetDomainsResponse();
        response.setData(Collections.emptyList());

        when(restClientHelper.performGetRequest(anyString(), anyString(), eq(GetDomainsResponse.class), anyBoolean()))
                .thenReturn(response);

        Either<FailedOperation, Optional<Domain>> result = sourceManager.getDomainFromName(domainName);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEmpty();
    }

    @Test
    void getDomainFromName_shouldReturnError_whenApiFails() {

        String errorMessage = "Internal Server Error";

        when(restClientHelper.performGetRequest(anyString(), anyString(), eq(GetDomainsResponse.class), anyBoolean()))
                .thenThrow(new RuntimeException(errorMessage));

        Either<FailedOperation, Optional<Domain>> result = sourceManager.getDomainFromName("TestDomain");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().problems().get(0).getMessage()).contains(errorMessage);
    }

    @Test
    void getDatasetsForSource_shouldReturnDatasets_whenExists() {
        String sourceID = "source-123";
        Dataset dataset1 = new Dataset();
        dataset1.setUrn("urn:dataset:1");
        Dataset dataset2 = new Dataset();
        dataset2.setUrn("urn:dataset:2");

        GetAssetsResponse response = new GetAssetsResponse();
        response.setData(List.of(dataset1, dataset2));

        when(restClientHelper.performPostRequest(
                        anyString(), anyString(), anyString(), eq(GetAssetsResponse.class), anyBoolean()))
                .thenReturn(response);

        Either<FailedOperation, List<Dataset>> result = sourceManager.getDatasetsForSource(sourceID);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).containsExactly(dataset1, dataset2);
    }

    @Test
    void getDatasetsForSource_shouldReturnEmpty_whenNoDatasetsFound() {
        String sourceID = "source-123";
        GetAssetsResponse response = new GetAssetsResponse();
        response.setData(Collections.emptyList());

        when(restClientHelper.performPostRequest(
                        anyString(), anyString(), anyString(), eq(GetAssetsResponse.class), anyBoolean()))
                .thenReturn(response);

        Either<FailedOperation, List<Dataset>> result = sourceManager.getDatasetsForSource(sourceID);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEmpty();
    }

    @Test
    void getDatasetsForSource_shouldReturnError_whenApiFails() {
        String sourceID = "source-123";
        String errorMessage = "Internal Server Error";

        when(restClientHelper.performPostRequest(
                        anyString(), anyString(), anyString(), eq(GetAssetsResponse.class), anyBoolean()))
                .thenThrow(new RuntimeException(errorMessage));

        Either<FailedOperation, List<Dataset>> result = sourceManager.getDatasetsForSource(sourceID);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().problems().get(0).getMessage()).contains(errorMessage);
    }

    @Test
    void assignDomainToDataSources_shouldSucceed_whenApiCallIsSuccessful() throws JsonProcessingException {
        String domainID = "domain-123";
        String domainName = "TestDomain";

        Dataset dataset1 = new Dataset();
        dataset1.setUrn("urn:dataset:1");
        Dataset dataset2 = new Dataset();
        dataset2.setUrn("urn:dataset:2");
        List<Dataset> datasets = (List.of(dataset1, dataset2));

        when(objectMapper.writeValueAsString(any(DomainAssignmentRequest.class)))
                .thenReturn("jsonBody");

        when(restClientHelper.performPutRequest(anyString(), anyString(), anyString(), eq(Void.class), anyBoolean()))
                .thenReturn(null);

        Either<FailedOperation, Void> result = sourceManager.assignDomainToDataSources(domainID, domainName, datasets);

        assertThat(result.isRight()).isTrue();
    }

    @Test
    void assignDomainToDataSources_shouldReturnError_whenUnexpectedExceptionOccurs() throws JsonProcessingException {
        String domainID = "domain-123";
        String domainName = "TestDomain";
        Dataset dataset1 = new Dataset();
        dataset1.setUrn("urn:dataset:1");
        List<Dataset> datasets = (List.of(dataset1));

        String errorMessage = "Unexpected error";

        when(objectMapper.writeValueAsString(any(DomainAssignmentRequest.class)))
                .thenReturn("jsonBody");

        when(restClientHelper.performPutRequest(anyString(), anyString(), anyString(), eq(Void.class), anyBoolean()))
                .thenThrow(new RuntimeException(errorMessage));

        Either<FailedOperation, Void> result = sourceManager.assignDomainToDataSources(domainID, domainName, datasets);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().problems().get(0).getMessage()).contains("Unexpected error");
    }

    @Test
    void getSourceFromID_shouldReturnError_whenApiFails() {
        String sourceId = "source-123";
        String errorMessage = "Simulated API failure";

        when(restClientHelper.performGetRequest(anyString(), anyString(), eq(Source.class), anyBoolean()))
                .thenThrow(new RuntimeException(errorMessage));

        Either<FailedOperation, Source> result = sourceManager.getSourceFromID(sourceId);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("Unexpected error while getting source information");
        assertThat(result.getLeft().problems().get(0).getMessage()).contains(errorMessage);
    }

    @Test
    void provisionSource_shouldReturnError_whenUnexpectedExceptionOccurs() {
        AthenaEntity athenaEntity = new AthenaEntity(
                "AwsDataCatalog", "database", "source_table", Region.EU_WEST_1, "primary", "s3://testbucket");
        SiffletSpecific siffletSpecific =
                new SiffletSpecific("cron", new Notification.Email("john.doe@witboost.com"), List.of());

        doThrow(new RuntimeException("TestError")).when(sourceManagerSpy).getSourceFromName(anyString());

        Either<FailedOperation, String> result =
                sourceManagerSpy.provisionSource(athenaEntity, siffletSpecific, "roleArn");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("Unexpected error while provisioning source 'database'");
        assertThat(result.getLeft().problems().get(0).getMessage()).contains("TestError");
    }
}
