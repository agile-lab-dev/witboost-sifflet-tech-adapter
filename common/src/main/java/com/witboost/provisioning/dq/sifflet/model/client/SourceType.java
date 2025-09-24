package com.witboost.provisioning.dq.sifflet.model.client;

import lombok.Getter;

@Getter
public enum SourceType {
    ATHENA("ATHENA");

    private final String value;

    SourceType(String value) {
        this.value = value;
    }
}
