package com.witboost.provisioning.dq.sifflet.model.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSourceResponse {

    private String id;
    private String name;
    private String description;
    private String timezone;
    private List<String> tags;
    private String credentials;
    private String schedule;
    private Map<String, Object> lastRun;

    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    @Override
    public String toString() {
        return "CreateSourceResponse{" + "id='"
                + id + '\'' + ", name='"
                + name + '\'' + ", description='"
                + description + '\'' + ", timezone='"
                + timezone + '\'' + ", tags='"
                + tags + '\'' + ", credentials='"
                + credentials + '\'' + ", schedule="
                + schedule + '\'' + ", parameters="
                + parameters + '}';
    }
}
