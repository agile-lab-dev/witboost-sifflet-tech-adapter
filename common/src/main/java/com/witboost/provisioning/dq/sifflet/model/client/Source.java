package com.witboost.provisioning.dq.sifflet.model.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class Source {

    private String id;
    private String name;

    @JsonProperty("lastRun")
    private LastRun lastrun;

    @JsonProperty("params")
    private Map<String, Object> parameters;
}
