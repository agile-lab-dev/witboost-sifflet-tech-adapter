package com.witboost.provisioning.dq.sifflet.model;

import com.witboost.provisioning.model.Specific;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SiffletSpecific extends Specific {

    String dataSourceRefreshCron;
}
