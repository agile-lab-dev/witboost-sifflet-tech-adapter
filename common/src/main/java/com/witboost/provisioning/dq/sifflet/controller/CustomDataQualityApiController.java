package com.witboost.provisioning.dq.sifflet.controller;

import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.controller.V1CustomDataQualityApi;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.controller.V1CustomDataQualityApiDelegate;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.DataQualityResult;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.OutputPortRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomDataQualityApiController implements V1CustomDataQualityApi {

    private final V1CustomDataQualityApiDelegate delegate;

    public CustomDataQualityApiController(@Autowired V1CustomDataQualityApiDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public ResponseEntity<List<DataQualityResult>> getDataQualityResult(@RequestBody OutputPortRequest request)
            throws Exception {
        return delegate.getDataQualityResult(request);
    }
}
