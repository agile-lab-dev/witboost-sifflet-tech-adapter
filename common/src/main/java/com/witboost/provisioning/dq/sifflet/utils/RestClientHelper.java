package com.witboost.provisioning.dq.sifflet.utils;

import java.net.URI;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RestClientHelper {

    private final RestClient restClient;

    @Autowired
    public RestClientHelper(RestClient restClient) {
        this.restClient = restClient;
    }

    public <T> T performGetRequest(
            String url, String token, Class<T> responseType, boolean throwErrorIfResponseBodyIsNull) {
        try {
            ResponseEntity<T> responseEntity = restClient
                    .get()
                    .uri(URI.create(url))
                    .header("accept", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpStatusCodeException(
                                "HTTP error. Response status code: " + res.getStatusCode() + ". Response: "
                                        + res.getStatusText(),
                                res.getStatusCode());
                    })
                    .toEntity(responseType);

            if (responseEntity.getBody() == null) {
                if (throwErrorIfResponseBodyIsNull) {
                    throw new EmptyResponseException("No HTTP response body received.");
                } else {
                    return null;
                }
            }
            return responseEntity.getBody();
        } catch (EmptyResponseException | HttpStatusCodeException e) {
            throw e;

        } catch (RestClientException e) {
            throw new RestClientRequestException("Error during HTTP request", e);
        } catch (Exception e) {
            throw new GenericRestClientException("Unexpected error", e);
        }
    }

    public <T> T performPostRequest(
            String url, String token, String body, Class<T> responseType, boolean throwErrorIfResponseBodyIsNull) {
        try {
            ResponseEntity<T> responseEntity = restClient
                    .post()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpStatusCodeException(
                                "HTTP error. Response status code: " + res.getStatusCode() + ". Response: "
                                        + res.getStatusText(),
                                res.getStatusCode());
                    })
                    .toEntity(responseType);

            if (responseEntity.getBody() == null) {
                if (throwErrorIfResponseBodyIsNull) {
                    throw new EmptyResponseException("No HTTP response body received.");
                } else {
                    return null;
                }
            }
            return responseEntity.getBody();
        } catch (EmptyResponseException | HttpStatusCodeException e) {
            throw e;
        } catch (RestClientException e) {
            throw new RestClientRequestException("Error during HTTP request", e);
        } catch (Exception e) {
            throw new GenericRestClientException("Unexpected error", e);
        }
    }

    public <T> T performPutRequest(
            String url, String token, String body, Class<T> responseType, boolean throwErrorIfResponseBodyIsNull) {
        try {
            ResponseEntity<T> responseEntity = restClient
                    .put()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpStatusCodeException(
                                "HTTP error. Response status code: " + res.getStatusCode() + ". Response: "
                                        + res.getStatusText(),
                                res.getStatusCode());
                    })
                    .toEntity(responseType);

            if (responseEntity.getBody() == null) {
                if (throwErrorIfResponseBodyIsNull) {
                    throw new EmptyResponseException("No HTTP response body received.");
                } else {
                    return null;
                }
            }
            return responseEntity.getBody();
        } catch (EmptyResponseException | HttpStatusCodeException e) {
            throw e;

        } catch (RestClientException e) {
            throw new RestClientRequestException("Error during HTTP request", e);
        } catch (Exception e) {
            throw new GenericRestClientException("Unexpected error", e);
        }
    }

    @Getter
    public static class HttpStatusCodeException extends RestClientException {
        private final HttpStatusCode statusCode;

        public HttpStatusCodeException(String message, HttpStatusCode statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
    }

    public static class EmptyResponseException extends RestClientException {
        public EmptyResponseException(String message) {
            super(message);
        }
    }

    public static class RestClientRequestException extends RestClientException {
        public RestClientRequestException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class GenericRestClientException extends RuntimeException {
        public GenericRestClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
