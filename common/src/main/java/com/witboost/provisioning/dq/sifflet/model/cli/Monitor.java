package com.witboost.provisioning.dq.sifflet.model.cli;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.*;

@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Monitor {

    private final String kind = "Monitor";
    private final int version = 2;
    private final String name;
    private final Incident incident;
    private final List<Notification> notifications;
    private final List<Dataset> datasets;
    private final Parameters parameters;
    private final String schedule;
    private final String scheduleTimezone;
}
