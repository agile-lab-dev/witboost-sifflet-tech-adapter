package com.witboost.provisioning.dq.sifflet.model.cli;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import lombok.*;

@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(force = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "kind",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Parameters.FieldNulls.class, name = "FieldNulls"),
    @JsonSubTypes.Type(value = Parameters.FieldDuplicates.class, name = "FieldDuplicates"),
    @JsonSubTypes.Type(value = Parameters.RowDuplicates.class, name = "RowDuplicates"),
    @JsonSubTypes.Type(value = Parameters.SchemaChange.class, name = "SchemaChange")
})
public class Parameters {
    private String kind;

    @JsonIgnore
    private Map<String, JsonNode> additionalProperties = new HashMap<>();

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @NoArgsConstructor(force = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FieldNulls extends Parameters {
        private final String field;
        private final String nullValues;
        private final JsonNode threshold;
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @NoArgsConstructor(force = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FieldDuplicates extends Parameters {
        /**
         * field can be either a single string or a string list, so as long as we don't need to validate this value
         * we leave it as json
         */
        private final JsonNode field;
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @NoArgsConstructor(force = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class RowDuplicates extends Parameters {}

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @NoArgsConstructor(force = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SchemaChange extends Parameters {}

    // Capture all other fields that Jackson do not match other members
    @JsonAnyGetter
    public Map<String, JsonNode> otherFields() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setOtherField(String name, JsonNode value) {
        additionalProperties.put(name, value);
    }
}
