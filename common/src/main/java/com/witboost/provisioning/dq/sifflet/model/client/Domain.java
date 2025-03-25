package com.witboost.provisioning.dq.sifflet.model.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Domain {

    private String id;
    private long createdDate;
    private String createdBy;
    private String name;
    private boolean canDelete;
    private String description;
    private String domainInputMethod;
    private boolean isAllDomain;
}
