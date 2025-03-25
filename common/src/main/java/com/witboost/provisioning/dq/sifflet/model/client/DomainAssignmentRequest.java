package com.witboost.provisioning.dq.sifflet.model.client;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class DomainAssignmentRequest {
    private final String domainInputMethod;
    private final boolean isAllDomain;
    private final List<String> assets;
    private final String description;
    private final String name;
}
