package com.witboost.provisioning.dq.sifflet.model.cli;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Workspace {

    private final String id;

    private final String name;

    private final List<String> include;

    private final List<String> exclude;

    private final String kind = "Workspace";
    private final int version = 1;

    @JsonIgnore
    private final List<? extends Monitor> monitors;

    public Workspace(Workspace oldWorkspace, List<? extends Monitor> monitors) {
        this.id = oldWorkspace.id;
        this.name = oldWorkspace.name;
        this.monitors = monitors;
        this.include = oldWorkspace.include;
        this.exclude = oldWorkspace.exclude;
    }

    public Workspace(String id, String name, List<? extends Monitor> monitors) {
        this.id = id;
        this.name = name;
        this.monitors = monitors;
        this.include = List.of("*.yaml");
        this.exclude = List.of();
    }
}
