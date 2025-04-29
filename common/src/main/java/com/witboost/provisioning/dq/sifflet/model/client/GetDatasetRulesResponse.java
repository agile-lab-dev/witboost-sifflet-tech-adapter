package com.witboost.provisioning.dq.sifflet.model.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetDatasetRulesResponse {
    private List<CatalogFilter> catalogFilters;
    private SearchRules searchRules;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CatalogFilter {
        private String name;
        private String query;
        private String id;
        private List<CatalogChild> children;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CatalogChild {
        private String name;
        private int results;
        private String id;
        private List<CatalogChild> children;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRules {
        private List<RuleData> data;
        private int totalElements;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleData {
        private String id;
        private String name;
        private String sourcePlatform;
        private Provider provider;
        private CreatedBy createdBy;
        private String lastRunId;
        private RunStatus lastRunStatus;
        private RuleStatus ruleStatus;
        private List<RunStatus> lastWeekStatuses;
        private boolean readOnly;
        private boolean canManuallyRun;
        private boolean supportAsCodeYAMLConversion;
        private String schedule;
        private String ruleLabel;
        private boolean selectable;
        private boolean multiDimensional;
        private int criticality;
        private String ruleType;
        private List<Dataset> datasets;
        private List<String> datasetFieldNames;
        private List<String> tags;
        private List<String> terms;
        private List<String> slackChannels;
        private List<Mail> mails;
        private List<String> msTeams;
        private boolean hasAiRecommendations;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Provider {
        private String createdBy;
        private String type;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatedBy {
        private String name;
        private String login;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RunStatus {
        private long timestamp;
        private String status;
        private String result;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleStatus {
        private String ruleStatus;
        private long latestRunDate;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dataset {
        private String name;
        private String id;
        private String urn;
        private String datasourceName;
        private String datasourceType;
        private String uri;
        private String qualifiedName;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Mail {
        private String id;
        private long createdDate;
        private long lastModifiedDate;
        private String createdBy;
        private String modifiedBy;
        private String name;
        private String externalHook;
        private String type;
    }
}
