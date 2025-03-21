package com.witboost.provisioning.dq.sifflet.model;

import com.witboost.provisioning.dq.sifflet.model.cli.Notification;
import com.witboost.provisioning.model.Specific;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class SiffletSpecific extends Specific {

    private final String dataSourceRefreshCron;
    private final Notification.Email notification;
    private final List<String> athenaOutputPorts;
}
