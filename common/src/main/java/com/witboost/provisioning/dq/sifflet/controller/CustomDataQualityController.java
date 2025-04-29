package com.witboost.provisioning.dq.sifflet.controller;

import com.witboost.provisioning.dq.sifflet.service.dataquality.CustomDataQualityProvisionService;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.controller.V1CustomDataQualityApiDelegate;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.DataQualityResult;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.OutputPortRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class CustomDataQualityController implements V1CustomDataQualityApiDelegate {

    private final CustomDataQualityProvisionService provisionService;

    public CustomDataQualityController(CustomDataQualityProvisionService provisionService) {
        this.provisionService = provisionService;
    }

    @Override
    public ResponseEntity<List<DataQualityResult>> getDataQualityResult(
            @RequestBody OutputPortRequest outputPortRequest) {

        return ResponseEntity.ok(provisionService.getDataQualityResult(outputPortRequest));
    }
}
