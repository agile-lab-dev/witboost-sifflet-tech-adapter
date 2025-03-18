package com.witboost.provisioning.dq.sifflet.cli;

import java.util.List;
import lombok.Getter;

@Getter
public class ProcessFailedException extends RuntimeException {
    private final List<String> standardOutput;
    private final List<String> standardErrorOutput;

    public ProcessFailedException(List<String> standardOutput, List<String> standardErrorOutput) {
        super(String.join("\n", standardErrorOutput));
        this.standardOutput = standardOutput;
        this.standardErrorOutput = standardErrorOutput;
    }
}
