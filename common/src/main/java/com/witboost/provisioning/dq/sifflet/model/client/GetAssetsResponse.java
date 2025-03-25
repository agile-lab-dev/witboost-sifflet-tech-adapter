package com.witboost.provisioning.dq.sifflet.model.client;

import java.util.List;
import lombok.Data;

@Data
public class GetAssetsResponse {

    private int totalCount;
    private List<Dataset> data;
}
