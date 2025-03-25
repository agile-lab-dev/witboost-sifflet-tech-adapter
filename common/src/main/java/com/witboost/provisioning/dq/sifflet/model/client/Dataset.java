package com.witboost.provisioning.dq.sifflet.model.client;

import java.util.List;
import lombok.Data;

@Data
public class Dataset {

    private String id;
    private String uri;
    private String urn;
    private String name;
    private String technology;
    private String type;
    private String description;
    private List<String> externalDescriptions;
    private String healthStatus;
    private List<String> tags;
    private List<String> terms;
    private List<String> owners;
    private String usage;
    private String ingestionMethod;
    private String transformationRun;
}
