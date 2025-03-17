package com.witboost.provisioning.dq.sifflet.model.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String type;
    private String status;
    private String createdAt;
    private String updatedAt;

    @JsonProperty("params")
    private Map<String, Object> parameters;

    @Override
    public String toString() {
        return "CreateSourceResponse{" + "id='"
                + id + '\'' + ", name='"
                + name + '\'' + ", type='"
                + type + '\'' + ", status='"
                + status + '\'' + ", createdAt='"
                + createdAt + '\'' + ", updatedAt='"
                + updatedAt + '\'' + ", parameters="
                + parameters + '}';
    }
}
