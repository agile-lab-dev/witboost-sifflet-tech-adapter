package com.witboost.provisioning.dq.sifflet.model.cli;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Incident {

    private final Severity severity;

    private final Boolean createOnFailure;

    @Getter
    @AllArgsConstructor
    public enum Severity {
        Low("Low"),
        Moderate("Moderate"),
        High("High"),
        Critical("Critical");

        private final String severity;

        public static Severity fromString(String severity) {
            for (Severity s : Severity.values()) {
                if (s.severity.equalsIgnoreCase(severity)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Unknown severity: " + severity);
        }
    }
}
