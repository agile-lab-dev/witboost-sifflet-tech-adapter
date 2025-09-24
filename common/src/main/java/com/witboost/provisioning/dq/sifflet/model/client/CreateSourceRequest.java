package com.witboost.provisioning.dq.sifflet.model.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSourceRequest {

    private String name;
    private String description;

    @JsonProperty("parameters")
    private Params params;

    private List<String> tags;

    @JsonProperty("schedule")
    private String cronExpression;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Params {
        private String type;
        private String datasource;
        private String region;
        private String s3OutputLocation;
        private String workgroup;
        private String database;
        private String roleArn;
        private String vpcUrl;
    }
}
