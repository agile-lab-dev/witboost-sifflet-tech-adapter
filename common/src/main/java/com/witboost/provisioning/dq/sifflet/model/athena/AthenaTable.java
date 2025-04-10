package com.witboost.provisioning.dq.sifflet.model.athena;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AthenaTable extends AbstractAthenaEntity {

    @NotNull
    private TableFormat tableFormat;
}
