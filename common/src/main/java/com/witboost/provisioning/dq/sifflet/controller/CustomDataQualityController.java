package com.witboost.provisioning.dq.sifflet.controller;

import com.witboost.provisioning.dq.sifflet.service.dataquality.DataQualityService;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.controller.V1CustomDataQualityApiDelegate;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.DataQualityResult;
import generated.witboost.mesh.provisioning.dataquality.sifflet.openapi.model.OutputPortRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomDataQualityController implements V1CustomDataQualityApiDelegate {

    private final DataQualityService dataQualityService;

    public CustomDataQualityController(DataQualityService dataQualityService) {
        this.dataQualityService = dataQualityService;
    }

    @Override
    @PostMapping("/v1/dataquality")
    public ResponseEntity<List<DataQualityResult>> getDataQualityResult(
            @RequestBody OutputPortRequest outputPortRequest) {

        return ResponseEntity.ok(dataQualityService.getDataQualityResult(outputPortRequest));
    }
}
