package com.witboost.provisioning.dq.sifflet.model.cli;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
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
}
