package com.witboost.provisioning.dq.sifflet.service.dataquality;

import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.DataQualityResult;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.OutputPortRequest;
import java.util.List;

public interface DataQualityService {

    public List<DataQualityResult> getDataQualityResult(OutputPortRequest outputPortRequest);
}
