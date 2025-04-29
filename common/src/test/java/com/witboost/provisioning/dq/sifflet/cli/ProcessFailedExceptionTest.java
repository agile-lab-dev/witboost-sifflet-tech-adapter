package com.witboost.provisioning.dq.sifflet.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class ProcessFailedExceptionTest {

    @Test
    void shouldStoreStandardAndErrorOutput() {
        List<String> stdOutput = List.of("output line 1", "output line 2");
        List<String> stdError = List.of("error line 1", "error line 2");

        ProcessFailedException exception = new ProcessFailedException(stdOutput, stdError);

        assertThat(exception.getMessage()).isEqualTo(String.join("\n", stdError));

        assertThat(exception.getStandardOutput()).isEqualTo(stdOutput);
        assertThat(exception.getStandardErrorOutput()).isEqualTo(stdError);
    }

    @Test
    void shouldThrowProcessFailedException() {
        List<String> out = List.of("OK");
        List<String> err = List.of("Something went wrong");

        ProcessFailedException thrown = assertThrows(ProcessFailedException.class, () -> {
            throw new ProcessFailedException(out, err);
        });

        assertThat(thrown.getStandardOutput()).isEqualTo(out);
        assertThat(thrown.getStandardErrorOutput()).isEqualTo(err);
        assertThat(thrown.getMessage()).isEqualTo("Something went wrong");
    }
}
