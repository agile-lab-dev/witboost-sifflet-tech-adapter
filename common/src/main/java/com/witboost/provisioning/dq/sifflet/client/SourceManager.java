package com.witboost.provisioning.dq.sifflet.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.witboost.provisioning.dq.sifflet.model.*;
import com.witboost.provisioning.dq.sifflet.model.athena.AthenaEntity;
import com.witboost.provisioning.dq.sifflet.model.client.*;
import com.witboost.provisioning.dq.sifflet.utils.RestClientHelper;
import com.witboost.provisioning.dq.sifflet.utils.Utils;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import io.vavr.control.Either;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SourceManager {

    private final Logger logger = LoggerFactory.getLogger(SourceManager.class);

    @Value("${sifflet.token}")
    private String token;

    @Value("${sifflet.basePath}")
    private String basePath;

    @Value("${sifflet.sourceUpdateTimeoutSeconds:30}")
    private int sourceUpdateTimeoutSeconds;

    private final ObjectMapper objectMapper;
    private final RestClientHelper restClientHelper;

    private static final String API_V1_SOURCES = "/api/v1/sources/";
    private static final String API_UI_V1_DATASOURCES = "/api/ui/v1/datasources";
    private static final String API_UI_V1_DOMAINS = "/api/ui/v1/domains";
    private static final String API_V1_ASSETS_SEARCH = "/api/v1/assets/search";

    public SourceManager(ObjectMapper objectMapper, RestClientHelper restClientHelper) {
        this.objectMapper = objectMapper;
        this.restClientHelper = restClientHelper;
    }

    @PostConstruct
    public void validateTimeout() {
        if (sourceUpdateTimeoutSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Source update timeout must be greater than zero. "
                            + "Please check the 'SOURCE_UPDATE_TIMEOUT_SECONDS' environment variable or application configuration.");
        }
    }

    public Either<FailedOperation, String> provisionSource(
            @NotNull AthenaEntity athenaEntity, @NotNull SiffletSpecific siffletSpecific, @NotBlank String roleArn) {
        try {

            String sourceName = computeSourceName(athenaEntity.getDatabase());
            Either<FailedOperation, Optional<Source>> sourceFromName = getSourceFromName(sourceName);

            if (sourceFromName.isLeft()) return Either.left(sourceFromName.getLeft());

            if (sourceFromName.get().isEmpty()) {
                Either<FailedOperation, CreateSourceResponse> createSource =
                        createSource(athenaEntity, siffletSpecific, roleArn, sourceName);
                if (createSource.isLeft()) return Either.left(createSource.getLeft());

                return waitForSourceToBeUpdated(createSource.get().getId(), sourceName)
                        .map(v -> createSource.get().getId());

            } else {
                Source source = sourceFromName.get().get();
                logger.info("Refreshing source: {}", sourceName);
                Either<FailedOperation, Void> sourceRefresh = triggerSourceRefresh(source.getId());
                if (sourceRefresh.isLeft()) return Either.left(sourceRefresh.getLeft());

                Either<FailedOperation, Source> updatedSource = getSourceFromID(source.getId());
                if (updatedSource.isLeft()) return Either.left(updatedSource.getLeft());

                return waitForSourceToBeUpdated(updatedSource.get().getId(), sourceName)
                        .map(v -> updatedSource.get().getId());
            }

        } catch (Exception e) {
            String error = String.format(
                    "Unexpected error while provisioning source '%s': %s", athenaEntity.getDatabase(), e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(
                    String.format(
                            "Unexpected error while provisioning source '%s'. Check details for more information",
                            athenaEntity.getDatabase()),
                    List.of(new Problem(error, e))));
        }
    }

    protected Either<FailedOperation, Source> getSourceFromID(String sourceID) {
        try {

            String url = basePath + API_V1_SOURCES + sourceID;
            return Either.right(restClientHelper.performGetRequest(url, token, Source.class, true));

        } catch (Exception e) {
            String error = String.format(
                    "Unexpected error while getting source information for source with ID: %s. Details: %s",
                    sourceID, e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(
                    String.format(
                            "Unexpected error while getting source information for source with ID '%s'. Check details for more information",
                            sourceID),
                    List.of(new Problem(error, e))));
        }
    }

    @NotNull
    protected Either<FailedOperation, CreateSourceResponse> createSource(
            AthenaEntity athenaEntity, SiffletSpecific siffletSpecific, String roleArn, String sourceName) {
        try {
            CreateSourceRequest createSourceRequest = createSourceRequest(athenaEntity, siffletSpecific, roleArn);
            String jsonRequest = objectMapper.writeValueAsString(createSourceRequest);
            String url = basePath + API_UI_V1_DATASOURCES;
            logger.info("Creating new source: {}", sourceName);

            CreateSourceResponse createSourceResponse =
                    restClientHelper.performPostRequest(url, token, jsonRequest, CreateSourceResponse.class, true);

            logger.info("Source created successfully: {}", sourceName);

            return Either.right(createSourceResponse);
        } catch (Exception e) {
            String error = String.format(
                    "Unexpected error while creating source '%s': %s", athenaEntity.getDatabase(), e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(
                    String.format(
                            "Unexpected error while creating source '%s'. Check details for more information",
                            athenaEntity.getDatabase()),
                    List.of(new Problem(error, e))));
        }
    }

    public Either<FailedOperation, Optional<Source>> getSourceFromName(String sourceName) {
        try {
            logger.info("Checking existence of source: {}", sourceName);

            String url = basePath + API_V1_SOURCES + "search";
            String jsonBody = String.format(
                    "{\"filter\":{\"textSearch\":\"%s\"},\"pagination\":{\"itemsPerPage\":-1,\"page\":0}}", sourceName);

            GetSourcesResponse getSourcesResponse =
                    restClientHelper.performPostRequest(url, token, jsonBody, GetSourcesResponse.class, true);
            Optional<Source> foundSource = getSourcesResponse.getData().stream()
                    .filter(source -> source.getName().equals(sourceName))
                    .findFirst();

            if (foundSource.isPresent()) {
                logger.info(
                        "Source '{}' found with ID: {}",
                        sourceName,
                        foundSource.get().getId());
            } else {
                logger.info("Source '{}' not found.", sourceName);
            }

            return Either.right(foundSource);

        } catch (Exception e) {
            String error = String.format(
                    "An unexpected error occurred while looking for source named '%s'. Details: %s",
                    sourceName, e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(
                    String.format(
                            "An unexpected error occurred while looking for source named '%s'. Check details for more information",
                            sourceName),
                    List.of(new Problem(error, e))));
        }
    }

    protected Either<FailedOperation, Void> triggerSourceRefresh(String sourceID) {
        try {
            String url = String.format("%s%s/%s/run", basePath, API_V1_SOURCES, sourceID);
            Void response = restClientHelper.performPostRequest(url, token, "{}", Void.class, false);

            return Either.right(response);

        } catch (Exception e) {
            String error = String.format(
                    "An unexpected error occurred while trying to update source with ID '%s'. Details: %s",
                    sourceID, e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(
                    String.format(
                            "An unexpected error occurred while trying to update source with ID '%s'. Check details for more information",
                            sourceID),
                    List.of(new Problem(error, e))));
        }
    }

    @NotNull
    protected static CreateSourceRequest createSourceRequest(
            AthenaEntity athenaEntity, SiffletSpecific siffletSpecific, String roleArn) {

        String description = "Source for database: " + athenaEntity.getDatabase();

        CreateSourceRequest.Params params = new CreateSourceRequest.Params(
                SourceType.ATHENA.getValue(),
                athenaEntity.getCatalog(),
                athenaEntity.getRegion().id(),
                athenaEntity.getS3Bucket() + "/sifflet/",
                athenaEntity.getWorkGroup(),
                athenaEntity.getDatabase(),
                roleArn,
                "");

        return new CreateSourceRequest(
                computeSourceName(athenaEntity.getDatabase()),
                description,
                SourceType.ATHENA.getValue(),
                params,
                Collections.emptyList(),
                siffletSpecific.getDataSourceRefreshCron());
    }

    protected static String computeSourceName(String databaseName) {
        String hash = Utils.sha256(databaseName);
        return databaseName + "_" + hash.substring(0, 5);
    }

    protected Either<FailedOperation, Void> waitForSourceToBeUpdated(String sourceID, String sourceName) {
        try {
            long startTime = System.currentTimeMillis();
            long timeout = sourceUpdateTimeoutSeconds * 1000L;

            logger.info(
                    "Waiting for source '{}' to be updated. Timeout: {} seconds",
                    sourceName,
                    sourceUpdateTimeoutSeconds);

            while (true) {
                Either<FailedOperation, Source> source = getSourceFromID(sourceID);
                if (source.isLeft()) return Either.left(source.getLeft());

                String status = source.get().getLastrun().getStatus();
                logger.info("Current status of source '{}': {}", sourceName, status);

                switch (status) {
                    case "SUCCESS":
                        logger.info("Source '{}' successfully updated.", sourceName);
                        return Either.right(null);
                    case "FAILURE":
                        String errorFailedUpdate = String.format("Update of source '%s' failed.", sourceName);
                        logger.error(errorFailedUpdate);
                        return Either.left(
                                new FailedOperation(errorFailedUpdate, List.of(new Problem(errorFailedUpdate))));
                    case "SKIPPED_DATASOURCE_ALREADY_RUNNING":
                        String errorSkippedUpdate = String.format(
                                "Update of source '%s' skipped (datasource already running).", sourceName);
                        logger.error(errorSkippedUpdate);
                        return Either.left(
                                new FailedOperation(errorSkippedUpdate, List.of(new Problem(errorSkippedUpdate))));
                    default:
                        if (System.currentTimeMillis() - startTime > timeout) {
                            String errorTimeOut = String.format(
                                    "Update of source '%s' timeout: execution time exceeded %d seconds",
                                    sourceName, sourceUpdateTimeoutSeconds);
                            logger.error(errorTimeOut);
                            return Either.left(new FailedOperation(errorTimeOut, List.of(new Problem(errorTimeOut))));
                        }

                        Thread.sleep(3000);
                }
            }

        } catch (Exception e) {
            String error = String.format(
                    "An unexpected error waiting for the completion of the update of the source %s. Details: %s",
                    sourceID, e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(
                    String.format(
                            "An unexpected error waiting for the completion of the update of the source %s. Check details for more information",
                            sourceID),
                    List.of(new Problem(error, e))));
        }
    }

    public Either<FailedOperation, Void> attachDomainToSourceDatasets(String domainName, String sourceID) {

        logger.info("Attaching domain {} to datasets with source {}", domainName, sourceID);

        return getDomainFromName(domainName).flatMap(domain -> {
            if (domain.isEmpty()) {
                String error = String.format(
                        "Error while attaching domain '%s' to datasets with source '%s'. Details: Domain not found.",
                        domainName, sourceID);

                return Either.left(new FailedOperation(
                        String.format(
                                "Error while attaching domain '%s' to datasets with source '%s'.",
                                domainName, sourceID),
                        List.of(new Problem(error))));
            }
            return getDatasetsForSource(sourceID)
                    .flatMap(datasets -> assignDomainToDataSources(domain.get().getId(), domainName, datasets))
                    .peek(result -> logger.info(
                            "Successfully attached domain '{}' to datasets with source '{}'", domainName, sourceID));
        });
    }

    protected Either<FailedOperation, Optional<Domain>> getDomainFromName(String domainName) {
        try {
            String url = String.format("%s%s", basePath, API_UI_V1_DOMAINS);

            GetDomainsResponse getDomainsResponses =
                    restClientHelper.performGetRequest(url, token, GetDomainsResponse.class, true);
            Optional<Domain> foundDomain = getDomainsResponses.getData().stream()
                    .filter(domain -> domain.getName().equalsIgnoreCase(domainName))
                    .findFirst();

            if (foundDomain.isPresent()) {
                logger.info(
                        "Domain '{}' found with ID: {}",
                        domainName,
                        foundDomain.get().getId());
            } else {
                logger.info("Domain '{}' not found.", domainName);
            }

            return Either.right(foundDomain);

        } catch (Exception e) {
            String error = String.format(
                    "An unexpected error occurred while looking for domain named '%s'. Details: %s",
                    domainName, e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(
                    String.format(
                            "An unexpected error occurred while looking for domain named '%s'. Check details for more information",
                            domainName),
                    List.of(new Problem(error, e))));
        }
    }

    protected Either<FailedOperation, List<Dataset>> getDatasetsForSource(String sourceID) {
        try {
            logger.info("Getting datasets for source: {}", sourceID);

            String url = basePath + API_V1_ASSETS_SEARCH;
            String jsonBody = "{ \"filter\": { \"sourceId\": [ \"" + sourceID + "\" ] } }";

            GetAssetsResponse getAssetsResponses =
                    restClientHelper.performPostRequest(url, token, jsonBody, GetAssetsResponse.class, true);

            return Either.right(getAssetsResponses.getData());

        } catch (Exception e) {
            String error = String.format(
                    "An unexpected error occurred while getting the list of datasets for source with ID '%s'. Details: %s",
                    sourceID, e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(
                    String.format(
                            "An unexpected error occurred while getting the list of datasets for source with ID '%s'. Check details for more information",
                            sourceID),
                    List.of(new Problem(error, e))));
        }
    }

    protected Either<FailedOperation, Void> assignDomainToDataSources(
            String domainID, String domainName, List<Dataset> datasets) {
        try {
            logger.info("Assigning datasets to domain with ID {}", domainID);

            String url = String.format("%s%s/%s", basePath, API_UI_V1_DOMAINS, domainID);

            List<String> assetUrns =
                    (datasets != null) ? datasets.stream().map(Dataset::getUrn).toList() : List.of();

            DomainAssignmentRequest domainAssignmentRequest =
                    new DomainAssignmentRequest("STATIC", false, assetUrns, "", domainName);

            String jsonBody = objectMapper.writeValueAsString(domainAssignmentRequest);

            Void response = restClientHelper.performPutRequest(url, token, jsonBody, Void.class, false);

            return Either.right(response);

        } catch (Exception e) {
            String error = String.format(
                    "An unexpected error occurred while assigning datasets to domain named '%s'. Details: %s",
                    domainName, e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(
                    String.format(
                            "An unexpected error occurred while assigning datasets to domain with ID '%s'. Check details for more information",
                            domainID),
                    List.of(new Problem(error, e))));
        }
    }
}
