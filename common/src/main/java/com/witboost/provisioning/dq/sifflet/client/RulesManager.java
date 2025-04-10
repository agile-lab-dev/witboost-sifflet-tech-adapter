package com.witboost.provisioning.dq.sifflet.client;

import static io.vavr.control.Either.left;

import com.witboost.provisioning.dq.sifflet.model.client.*;
import com.witboost.provisioning.dq.sifflet.utils.RestClientHelper;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.DataQualityResult;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.RunStatus;
import io.vavr.control.Either;
import jakarta.validation.constraints.Positive;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RulesManager {

    private final Logger logger = LoggerFactory.getLogger(RulesManager.class);

    private final SourceManager sourceManager;

    private final RestClientHelper restClientHelper;

    @Value("${sifflet.token}")
    private String token;

    @Value("${sifflet.basePath}")
    private String basePath;

    private static final String API_V1_RULES = "/api/v1/rules";

    public RulesManager(SourceManager sourceManager, RestClientHelper restClientHelper) {
        this.sourceManager = sourceManager;
        this.restClientHelper = restClientHelper;
    }

    public Either<FailedOperation, List<DataQualityResult>> createResultForFrontend(
            List<GetDatasetRulesResponse.RuleData> datasetRules, int lastRunsMonitorRuns) {
        try {

            List<DataQualityResult> dataQualityResults = new ArrayList<>();

            for (GetDatasetRulesResponse.RuleData datasetRule : datasetRules) {
                DataQualityResult monitor = new DataQualityResult();
                monitor.setId(datasetRule.getId());
                monitor.setName(datasetRule.getName());
                monitor.setRuleLabel(datasetRule.getRuleLabel());
                monitor.setCriticality(Criticality.getNameById(datasetRule.getCriticality()));
                monitor.setLastRunId(datasetRule.getLastRunId());

                monitor.setLastRunTimestamp(Optional.ofNullable(datasetRule.getLastRunStatus())
                        .map(GetDatasetRulesResponse.RunStatus::getTimestamp)
                        .orElse(null));

                monitor.setLastRunStatus(Optional.ofNullable(datasetRule.getLastRunStatus())
                        .map(GetDatasetRulesResponse.RunStatus::getStatus)
                        .orElse(null));

                monitor.setLastRunResult(Optional.ofNullable(datasetRule.getLastRunStatus())
                        .map(GetDatasetRulesResponse.RunStatus::getResult)
                        .orElse(null));

                Either<FailedOperation, List<GetRuleRunsResponse.DataItem>> lastNRuns =
                        getLastRuns(datasetRule.getId(), lastRunsMonitorRuns);
                if (lastNRuns.isLeft()) return left(lastNRuns.getLeft());

                Map<GetRuleRunsResponse.Status, Double> percentages =
                        GetRuleRunsResponse.calculateStatusPercentages(lastNRuns.get());

                monitor.setLastRunsMonitorRuns(lastNRuns.get().size());
                monitor.setLastRunsMonitorSuccess(percentages.get(GetRuleRunsResponse.Status.SUCCESS));
                monitor.setLastRunsMonitorFailed(percentages.get(GetRuleRunsResponse.Status.FAILED));
                monitor.setLastRunsMonitorAttentionRequired(
                        percentages.get(GetRuleRunsResponse.Status.REQUIRES_YOUR_ATTENTION));
                monitor.setLastRunsMonitorAttentionTechicalError(
                        percentages.get(GetRuleRunsResponse.Status.TECHNICAL_ERROR));

                List<RunStatus> lastRunsStatuses = new ArrayList<>();
                for (GetRuleRunsResponse.DataItem runResult : lastNRuns.get()) {
                    RunStatus runStatus = new RunStatus();
                    runStatus.setTimestamp(runResult.getCreatedDate());
                    runStatus.setStatus(runResult.getStatus().name());
                    runStatus.setResult(runResult.getResult());
                    lastRunsStatuses.add(runStatus);
                }

                monitor.setLastRunsStatuses(lastRunsStatuses);
                dataQualityResults.add(monitor);
            }

            return Either.right(dataQualityResults);
        } catch (Exception e) {
            String error = "Unexpected error while creating frontend result: " + e.getMessage();
            logger.error(error, e);
            return left(new FailedOperation(error, List.of(new Problem(error, e))));
        }
    }

    public Either<FailedOperation, List<GetDatasetRulesResponse.RuleData>> getDatasetRules(
            String database, String datasetName) {
        logger.info("Fetching dataset rules for database: {}, name: {}", database, datasetName);

        String sourceName = SourceManager.computeSourceName(database);
        Either<FailedOperation, Optional<Source>> sourceFromName = sourceManager.getSourceFromName(sourceName);

        if (sourceFromName.isLeft()) return left(sourceFromName.getLeft());

        if (sourceFromName.get().isEmpty()) {
            String error = "Source not found for database: " + database;
            logger.error(error);
            return left(new FailedOperation(error, List.of(new Problem(error))));
        }
        Source source = sourceFromName.get().get();

        Either<FailedOperation, List<Dataset>> datasetsForSource = sourceManager.getDatasetsForSource(source.getId());
        if (datasetsForSource.isLeft()) return left(datasetsForSource.getLeft());

        Optional<Dataset> viewDataset = datasetsForSource.get().stream()
                .filter(dataset -> dataset.getName().equals(datasetName))
                .findFirst();

        if (viewDataset.isEmpty()) {
            String error = String.format(
                    "Seems that there is no dataset in Sifflet corresponding to %s.%s", database, datasetName);
            return left(new FailedOperation(error, List.of(new Problem(error))));
        }
        try {
            String url =
                    basePath + API_V1_RULES + "?dataset=" + viewDataset.get().getId();
            logger.info("Requesting dataset '{}' rules", viewDataset.get().getId());

            GetDatasetRulesResponse datasetRules =
                    restClientHelper.performGetRequest(url, token, GetDatasetRulesResponse.class, true);

            logger.info(
                    "Successfully fetched dataset rules for dataset: {}",
                    viewDataset.get().getId());
            return Either.right(datasetRules.getSearchRules().getData());

        } catch (Exception e) {
            String error = String.format(
                    "Unexpected error while getting rules of dataset linked to '%s.%s': %s",
                    database, datasetName, e.getMessage());
            logger.error(error, e);
            return left(new FailedOperation(error, List.of(new Problem(error, e))));
        }
    }

    public Either<FailedOperation, List<GetRuleRunsResponse.DataItem>> getLastRuns(String ruleID, @Positive int n) {
        logger.info("Fetching last {} runs for rule: {}", n, ruleID);

        try {

            String url = basePath + API_V1_RULES + "/" + ruleID + "/runs?status=&page=0&itemsPerPage=" + n
                    + "&sort=createdDate%2CDESC";

            GetRuleRunsResponse ruleRuns =
                    restClientHelper.performGetRequest(url, token, GetRuleRunsResponse.class, true);

            List<GetRuleRunsResponse.DataItem> lastRuns = new ArrayList<>(ruleRuns.getData());
            lastRuns.sort(Comparator.comparing(GetRuleRunsResponse.DataItem::getStartDate)
                    .reversed());

            logger.info("Successfully fetched {} runs for rule: {}", lastRuns.size(), ruleID);
            return Either.right(lastRuns);

        } catch (Exception e) {
            String error = String.format(
                    "Unexpected error while fetching last %d runs for rule '%s': %s", n, ruleID, e.getMessage());
            logger.error(error, e);
            return left(new FailedOperation(error, List.of(new Problem(error, e))));
        }
    }
}
