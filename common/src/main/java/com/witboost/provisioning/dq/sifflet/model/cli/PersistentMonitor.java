package com.witboost.provisioning.dq.sifflet.model.cli;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistentMonitor extends Monitor {
    protected String friendlyId;

    public PersistentMonitor(String friendlyId, Monitor monitor) {
        super(
                monitor.getName(),
                monitor.getIncident(),
                monitor.getNotifications(),
                monitor.getDatasets(),
                monitor.getParameters(),
                monitor.getSchedule(),
                monitor.getScheduleTimezone());
        this.friendlyId = friendlyId;
    }

    public PersistentMonitor(
            String friendlyId,
            String name,
            Incident incident,
            List<Notification> notifications,
            List<Dataset> datasets,
            Parameters parameters,
            String schedule,
            String scheduleTimezone) {
        super(name, incident, notifications, datasets, parameters, schedule, scheduleTimezone);
        this.friendlyId = friendlyId;
    }

    public static PersistentMonitor loadFromYaml(String yaml) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        return objectMapper.readValue(yaml, PersistentMonitor.class);
    }
}
