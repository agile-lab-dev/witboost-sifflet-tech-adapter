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
@RequiredArgsConstructor
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
    private final String kind;

    @JsonIgnore
    private final Map<String, JsonNode> additionalProperties = new HashMap<>();

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @NoArgsConstructor(force = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FieldNulls extends Parameters {
        private final String field;
        private final String nullValues;
        private final Threshold threshold;

        public FieldNulls(String field, String nullValues, Threshold threshold) {
            super("FieldNulls");
            this.field = field;
            this.nullValues = nullValues;
            this.threshold = threshold;
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @NoArgsConstructor(force = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FieldDuplicates extends Parameters {
        /**
         * field can be either a single string or a string list, so as long as we don't need to validate this value
         * we leave it as json
         */
        private final JsonNode field;

        public FieldDuplicates(JsonNode field) {
            super("FieldDuplicates");
            this.field = field;
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @NoArgsConstructor(force = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RowDuplicates extends Parameters {
        private final Threshold threshold;

        public RowDuplicates(Threshold threshold) {
            super("RowDuplicates");
            this.threshold = threshold;
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SchemaChange extends Parameters {
        public SchemaChange() {
            super("SchemaChange");
        }
    }

    // TODO This can be further improved to include more threshold types,
    //  but for now we leave it as-is
    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Threshold {
        private final String kind;
        private final String valueMode;
        private final String max;
    }

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
