package com.witboost.provisioning.dq.sifflet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AthenaInfo {
    private final Map<String, Info> privateInfo;

    @Getter
    @NoArgsConstructor(force = true)
    @RequiredArgsConstructor
    public static class Info {
        private final String label;
        private final String value;
        private final String type;
    }

    @JsonIgnore
    public Optional<String> getInfoValue(String name) {
        return Optional.ofNullable(privateInfo)
                .flatMap(map -> Optional.ofNullable(map.get(name)))
                .map(Info::getValue);
    }
}
