package com.witboost.provisioning.dq.sifflet.model.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetSourcesResponse {

    private List<Source> data;
    private int totalElements;
}
