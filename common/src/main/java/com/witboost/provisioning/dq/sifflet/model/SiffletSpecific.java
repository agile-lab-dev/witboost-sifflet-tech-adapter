package com.witboost.provisioning.dq.sifflet.model;

import com.witboost.provisioning.dq.sifflet.model.cli.Notification;
import com.witboost.provisioning.model.Specific;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@ToString
public class SiffletSpecific extends Specific {

    @NotBlank
    private final String dataSourceRefreshCron;

    @NotNull
    private final Notification.Email notification;

    @NotEmpty
    private final List<String> athenaOutputPorts;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SiffletSpecific that = (SiffletSpecific) o;
        return Objects.equals(dataSourceRefreshCron, that.dataSourceRefreshCron)
                && Objects.equals(notification, that.notification)
                && Objects.equals(athenaOutputPorts, that.athenaOutputPorts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataSourceRefreshCron, notification, athenaOutputPorts);
    }
}
