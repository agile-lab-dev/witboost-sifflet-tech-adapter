package com.witboost.provisioning.dq.sifflet.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@SpringBootTest
public class RestClientHelperTest {

    @MockBean
    private RestClient restClient;

    @Autowired
    private RestClientHelper restClientHelper;

    @Mock
    private ResponseEntity<String> responseEntityMock;

    @Mock
    RestClient.RequestHeadersUriSpec requestHeadersUriSpecMock;

    @Mock
    RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    RestClient.ResponseSpec responseSpec;

    private final String url = "http://example.com";
    private final String token = "dummyToken";

    @Test
    public void testPerformGetRequest_Success() {
        when(restClient.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(URI.create(url))).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.header("accept", "application/json")).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.header("Authorization", "Bearer " + token))
                .thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toEntity(String.class)).thenReturn(responseEntityMock);
        when(responseEntityMock.getBody()).thenReturn("Success");

        String result = restClientHelper.performGetRequest(url, token, String.class, true);

        assertEquals("Success", result);
    }

    @Test
    public void testPerformGetRequest_HttpError() {
        when(restClient.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(URI.create(url))).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.header("accept", "application/json")).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.header("Authorization", "Bearer " + token))
                .thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenAnswer(invocation -> {
            throw new RestClientHelper.HttpStatusCodeException("HTTP error", HttpStatusCode.valueOf(500));
        });

        assertThrows(
                RestClientHelper.HttpStatusCodeException.class,
                () -> restClientHelper.performGetRequest(url, token, String.class, true));
    }

    @Test
    public void testPerformGetRequest_NullResponse_NoError() {
        when(restClient.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(URI.create(url))).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.header("accept", "application/json")).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.header("Authorization", "Bearer " + token))
                .thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toEntity(String.class)).thenReturn(responseEntityMock);
        when(responseEntityMock.getBody()).thenReturn(null);

        String result = restClientHelper.performGetRequest(url, token, String.class, false);
        assertNull(result);
    }

    @Test
    public void testPerformPostRequest_Success() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(URI.create(url))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Accept", "application/json")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Authorization", "Bearer " + token)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Content-Type", "application/json")).thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.body("body content")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toEntity(String.class)).thenReturn(responseEntityMock);
        when(responseEntityMock.getBody()).thenReturn("PostSuccess");

        String result = restClientHelper.performPostRequest(url, token, "body content", String.class, true);
        assertEquals("PostSuccess", result);
    }

    @Test
    public void testPerformPostRequest_NullResponse_NoError() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(URI.create(url))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Accept", "application/json")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Authorization", "Bearer " + token)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Content-Type", "application/json")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body("body content")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toEntity(String.class)).thenReturn(responseEntityMock);
        when(responseEntityMock.getBody()).thenReturn(null);

        String result = restClientHelper.performPostRequest(url, token, "body content", String.class, false);
        assertNull(result);
    }

    @Test
    public void testPerformPostRequest_EmptyResponse() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(URI.create(url))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Accept", "application/json")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Authorization", "Bearer " + token)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Content-Type", "application/json")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body("body content")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toEntity(String.class)).thenReturn(responseEntityMock);
        when(responseEntityMock.getBody()).thenReturn(null);

        assertThrows(
                RestClientHelper.EmptyResponseException.class,
                () -> restClientHelper.performPostRequest(url, token, "body content", String.class, true));
    }

    @Test
    public void testPerformPutRequest_Success() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(URI.create(url))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Accept", "application/json")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Authorization", "Bearer " + token)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Content-Type", "application/json")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body("body content")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toEntity(String.class)).thenReturn(responseEntityMock);
        when(responseEntityMock.getBody()).thenReturn("PutSuccess");

        String result = restClientHelper.performPutRequest(url, token, "body content", String.class, true);
        assertEquals("PutSuccess", result);
    }

    @Test
    public void testPerformPutRequest_RestClientException() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(URI.create(url))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Accept", "application/json")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Authorization", "Bearer " + token)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Content-Type", "application/json")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body("body content")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenThrow(new RestClientException("Connection error"));

        assertThrows(
                RestClientHelper.RestClientRequestException.class,
                () -> restClientHelper.performPutRequest(url, token, "body content", String.class, true));
    }

    @Test
    public void testPerformPutRequest_NullResponse_NoError() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(URI.create(url))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Accept", "application/json")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Authorization", "Bearer " + token)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Content-Type", "application/json")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body("body content")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toEntity(String.class)).thenReturn(responseEntityMock);
        when(responseEntityMock.getBody()).thenReturn(null);

        String result = restClientHelper.performPutRequest(url, token, "body content", String.class, false);
        assertNull(result);
    }

    @Test
    public void testPerformGetRequest_GenericException() {
        when(restClient.get()).thenThrow(new RuntimeException("Unexpected error"));
        assertThrows(
                RestClientHelper.GenericRestClientException.class,
                () -> restClientHelper.performGetRequest(url, token, String.class, true));
    }

    @Test
    public void testPerformPostRequest_GenericException() {
        when(restClient.post()).thenThrow(new RuntimeException("Unexpected error"));
        assertThrows(
                RestClientHelper.GenericRestClientException.class,
                () -> restClientHelper.performPostRequest(url, token, "body content", String.class, true));
    }

    @Test
    public void testPerformPutRequest_GenericException() {
        when(restClient.put()).thenThrow(new RuntimeException("Unexpected error"));
        assertThrows(
                RestClientHelper.GenericRestClientException.class,
                () -> restClientHelper.performPutRequest(url, token, "body content", String.class, true));
    }

    @Test
    public void testPerformPostRequest_HttpError() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(URI.create(url))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Accept", "application/json")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Authorization", "Bearer " + token)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header("Content-Type", "application/json")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body("body content")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenAnswer(inv -> {
            throw new RestClientHelper.HttpStatusCodeException("HTTP error", HttpStatusCode.valueOf(400));
        });

        assertThrows(
                RestClientHelper.HttpStatusCodeException.class,
                () -> restClientHelper.performPostRequest(url, token, "body content", String.class, true));
    }

    @Test
    public void testPerformGetRequest_HttpStatusErrorTriggered() {
        when(restClient.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(URI.create(url))).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.header("accept", "application/json")).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.header("Authorization", "Bearer " + token))
                .thenReturn(requestHeadersUriSpecMock);

        when(requestHeadersUriSpecMock.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).then(invocation -> {
            var status = HttpStatusCode.valueOf(403);
            var responseMock =
                    new org.springframework.mock.http.client.MockClientHttpResponse(new byte[0], status.value());
            invocation.getArgument(1, java.util.function.BiConsumer.class).accept(null, responseMock);
            return responseSpec;
        });

        assertThrows(
                RestClientHelper.GenericRestClientException.class,
                () -> restClientHelper.performGetRequest(url, token, String.class, true));
    }
}
