package com.witboost.provisioning.dq.sifflet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.witboost.provisioning.dq.sifflet.model.cli.Monitor;
import com.witboost.provisioning.model.DataContract;
import java.util.List;
import java.util.stream.Stream;
import lombok.*;

@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SiffletDataContract extends DataContract {
    private final List<QualityRule<?>> quality;

    @NoArgsConstructor(force = true)
    @Getter
    @EqualsAndHashCode
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            defaultImpl = QualityRule.OtherQualityRule.class,
            property = "engine",
            visible = true)
    @JsonSubTypes({@JsonSubTypes.Type(value = QualityRule.SiffletQualityRule.class, name = "sifflet")})
    public static class QualityRule<T> {
        private final String type;
        private final String engine;
        private final T implementation;

        public static class SiffletQualityRule extends QualityRule<Monitor> {}

        public static class OtherQualityRule extends QualityRule<JsonNode> {}
    }

    @JsonIgnore
    public List<Monitor> getSiffletMonitors() {
        return this.getQuality().stream()
                .flatMap(rule -> {
                    if (rule instanceof SiffletDataContract.QualityRule.SiffletQualityRule siffletQualityRule) {
                        return Stream.of(siffletQualityRule.getImplementation());
                    } else return Stream.empty();
                })
                .toList();
    }
}
