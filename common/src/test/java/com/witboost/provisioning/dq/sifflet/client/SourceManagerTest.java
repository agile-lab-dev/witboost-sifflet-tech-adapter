package com.witboost.provisioning.dq.sifflet.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.witboost.provisioning.dq.sifflet.model.AthenaEntity;
import com.witboost.provisioning.dq.sifflet.model.SiffletSpecific;
import com.witboost.provisioning.dq.sifflet.model.cli.Notification;
import com.witboost.provisioning.dq.sifflet.model.client.*;
import com.witboost.provisioning.dq.sifflet.utils.OkHttpUtils;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.regions.Region;

@ExtendWith(MockitoExtension.class)
class SourceManagerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OkHttpClient okHttpClient;

    @InjectMocks
    private SourceManager sourceManager;

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

    private void createMockCall(Response mockResponseObj) throws IOException {
        Call mockCall = mock(Call.class);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponseObj);
    }

    @Test
    void getSourceFromName_shouldReturnSource_whenExists() throws Exception {
        GetSourcesResponse getSourcesResponse = new GetSourcesResponse();
        Source existingSource = new Source();
        existingSource.setId("1234");
        existingSource.setName("existing-source");
        getSourcesResponse.setData(Collections.singletonList(existingSource));

        Response mockResponseObj = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("{}", MediaType.get("application/json")))
                .build();

        createMockCall(mockResponseObj);

        when(objectMapper.readValue(anyString(), eq(GetSourcesResponse.class))).thenReturn(getSourcesResponse);

        Either<FailedOperation, Optional<Source>> result = sourceManagerSpy.getSourceFromName("existing-source");

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).contains(existingSource);
    }

    @Test
    void getSourceFromName_shouldReturnEmpty_whenNotExists() throws Exception {
        String sourceName = "non-existent-source";

        Response mockResponseObj = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("{}", MediaType.get("application/json")))
                .build();

        createMockCall(mockResponseObj);

        GetSourcesResponse mockResponse = new GetSourcesResponse();
        mockResponse.setData(Collections.emptyList());

        when(objectMapper.readValue(anyString(), eq(GetSourcesResponse.class))).thenReturn(mockResponse);

        Either<FailedOperation, Optional<Source>> result = sourceManagerSpy.getSourceFromName(sourceName);

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
    void triggerSourceRefresh_shouldSucceed_whenValidSourceId() throws Exception {
        String sourceId = "12345";

        Response mockResponseObj = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("{}", MediaType.get("application/json")))
                .build();

        createMockCall(mockResponseObj);

        Either<FailedOperation, Void> result = sourceManagerSpy.triggerSourceRefresh(sourceId);

        assert (result.isRight());
    }

    @Test
    void triggerSourceRefresh_shouldFail_whenApiReturnsError() throws Exception {
        String sourceId = "12345";

        Response mockResponseObj = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Error message")
                .body(ResponseBody.create("{}", MediaType.get("application/json")))
                .build();

        createMockCall(mockResponseObj);

        Either<FailedOperation, Void> result = sourceManagerSpy.triggerSourceRefresh(sourceId);

        assertThat(result.isLeft()).isTrue();
        assertThatCollection(result.getLeft().problems())
                .anyMatch(problem -> problem.getMessage().contains(mockResponseObj.message()));
    }

    @Test
    void waitForSourceToBeUpdated_shouldReturnTimeoutError_whenTimeoutExceeded() throws Exception {
        String sourceId = "source-id";
        String sourceName = "source-name";
        Source source = new Source();
        source.setId(sourceId);
        source.setLastrun(new LastRun("RUNNING", "0123"));

        Response mockResponseObj = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("{}", MediaType.get("application/json")))
                .build();

        createMockCall(mockResponseObj);

        when(sourceManagerSpy.getSourceFromID(sourceId)).thenReturn(Either.right(source));

        ReflectionTestUtils.setField(sourceManagerSpy, "sourceUpdateTimeoutSeconds", 1);

        Either<FailedOperation, Void> result = sourceManagerSpy.waitForSourceToBeUpdated(sourceId, sourceName);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("timeout");
    }

    @Test
    void validateTimeout_shouldThrowException_whenTimeoutIsZero() {
        ReflectionTestUtils.setField(sourceManagerSpy, "sourceUpdateTimeoutSeconds", 0);

        assertThatThrownBy(() -> sourceManagerSpy.validateTimeout())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void getSourceFromName_shouldReturnError_whenApiFails() throws Exception {
        Response mockErrorResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Internal Server Error")
                .body(ResponseBody.create("{\"error\":\"Internal Server Error\"}", MediaType.get("application/json")))
                .build();

        createMockCall(mockErrorResponse);

        Either<FailedOperation, Optional<Source>> result = sourceManagerSpy.getSourceFromName("source-name");

        assertThat(result.isLeft()).isTrue();
        assertThatCollection(result.getLeft().problems())
                .anyMatch(problem -> problem.getMessage().contains("Internal Server Error"));
    }

    @Test
    void createSource_shouldReturnError_whenApiFails() throws Exception {
        Response mockErrorResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(400)
                .message("Bad Request")
                .body(ResponseBody.create("{\"error\":\"Bad Request\"}", MediaType.get("application/json")))
                .build();

        try (MockedStatic<OkHttpUtils> mockedStatic = mockStatic(OkHttpUtils.class)) {
            mockedStatic
                    .when(() -> OkHttpUtils.buildPostRequest(any(), any(), any()))
                    .thenReturn(mock(Request.class));

            createMockCall(mockErrorResponse);

            Either<FailedOperation, CreateSourceResponse> result = sourceManagerSpy.createSource(
                    new AthenaEntity("AwsDataCatalog", "database", "table", Region.EU_WEST_1, "primary", "s3://test"),
                    new SiffletSpecific("cron", new Notification.Email("john.doe@witboost.com"), List.of()),
                    "roleArn",
                    "some-token");

            assertThat(result.isLeft()).isTrue();
            assertThatCollection(result.getLeft().problems())
                    .anyMatch(problem -> problem.getMessage().contains("Bad Request"));
        }
    }

    @Test
    void triggerSourceRefresh_shouldReturnError_whenUnexpectedResponse() throws Exception {
        Response mockUnexpectedResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(123)
                .message("OK")
                .body(ResponseBody.create("{\"unexpected\":\"value\"}", MediaType.get("application/json")))
                .build();

        createMockCall(mockUnexpectedResponse);

        Either<FailedOperation, Void> result = sourceManagerSpy.triggerSourceRefresh("1234");

        assertThat(result.isLeft()).isTrue();
        assertThatCollection(result.getLeft().problems()).anyMatch(problem -> problem.getMessage()
                .contains("Failed to trigger source metadata ingestion job for source 1234"));
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
    void getSourceFromName_shouldReturnError_whenTimeoutOccurs() throws Exception {

        Response mockResponseObj = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(408)
                .message("Request Timeout")
                .body(ResponseBody.create("{\"error\":\"Request Timeout\"}", MediaType.get("application/json")))
                .build();

        createMockCall(mockResponseObj);

        Either<FailedOperation, Optional<Source>> result = sourceManagerSpy.getSourceFromName("source-name");

        assertThat(result.isLeft()).isTrue();
        assertThatCollection(result.getLeft().problems())
                .anyMatch(problem -> problem.getMessage().contains("Request Timeout"));
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
    void triggerSourceRefresh_shouldReturnError_whenInvalidSourceId() throws Exception {
        String invalidSourceId = "invalid-source-id";

        Response mockResponseObj = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(400)
                .message("Bad Request")
                .body(ResponseBody.create("{\"error\":\"Bad Request\"}", MediaType.get("application/json")))
                .build();

        createMockCall(mockResponseObj);

        Either<FailedOperation, Void> result = sourceManagerSpy.triggerSourceRefresh(invalidSourceId);

        assertThat(result.isLeft()).isTrue();
        assertThatCollection(result.getLeft().problems())
                .anyMatch(problem -> problem.getMessage().contains("Bad Request"));
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
    void getSourceFromName_shouldReturnError_whenSourceNotFound() throws Exception {
        String sourceName = "non-existent-source";

        doThrow(new RuntimeException("runtime exception"))
                .when(sourceManagerSpy)
                .executeRequest(any(Request.class));

        Either<FailedOperation, Optional<Source>> result = sourceManagerSpy.getSourceFromName(sourceName);

        assertThat(result.isLeft()).isTrue();
        assertThatCollection(result.getLeft().problems())
                .anyMatch(problem -> problem.getMessage().contains("runtime exception"));
    }

    @Test
    void createSource_shouldReturnCreateSourceResponse_whenSuccessful() throws Exception {
        CreateSourceResponse createSourceResponse = new CreateSourceResponse();
        createSourceResponse.setId("1234");

        Response mockResponseObj = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("{}", MediaType.get("application/json")))
                .build();

        createMockCall(mockResponseObj);

        try (MockedStatic<OkHttpUtils> mockedStatic = mockStatic(OkHttpUtils.class)) {
            mockedStatic
                    .when(() -> OkHttpUtils.buildPostRequest(any(), any(), any()))
                    .thenReturn(mock(Request.class));

            doReturn(createSourceResponse).when(objectMapper).readValue(Mockito.anyString(), Mockito.any(Class.class));

            Either<FailedOperation, CreateSourceResponse> result = sourceManagerSpy.createSource(
                    new AthenaEntity("AwsDataCatalog", "database", "table", Region.EU_WEST_1, "primary", "s3://test"),
                    new SiffletSpecific("cron", new Notification.Email("john.doe@witboost.com"), List.of()),
                    "roleArn",
                    "some-token");

            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(createSourceResponse);
            assertThat(result.get().getId()).isEqualTo("1234");
        }
    }

    @Test
    void shouldReturnError_whenUnexpectedExceptionOccursWhileCreatingSource() throws Exception {
        AthenaEntity athenaEntity =
                new AthenaEntity("AwsDataCatalog", "database", "table", Region.EU_WEST_1, "primary", "s3://test");

        Response mockResponseObj = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("{}", MediaType.get("application/json")))
                .build();

        createMockCall(mockResponseObj);

        try (MockedStatic<OkHttpUtils> mockedStatic = mockStatic(OkHttpUtils.class)) {
            mockedStatic
                    .when(() -> OkHttpUtils.buildPostRequest(any(), any(), any()))
                    .thenReturn(mock(Request.class));

            when(objectMapper.readValue(anyString(), eq(CreateSourceResponse.class)))
                    .thenThrow(new RuntimeException("Simulated error"));

            Either<FailedOperation, CreateSourceResponse> result = sourceManagerSpy.createSource(
                    athenaEntity,
                    new SiffletSpecific("cron", new Notification.Email("john.doe@witboost.com"), List.of()),
                    "roleArn",
                    "source-name");

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft().message()).contains("Unexpected error while creating source 'database'");
            assertThatCollection(result.getLeft().problems())
                    .anyMatch(problem -> problem.getMessage().contains("Simulated error"));
        }
    }
}
