package com.witboost.provisioning.dq.sifflet.model.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.witboost.provisioning.dq.sifflet.model.YamlMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

public class WorkspaceTest {

    @Test
    void workspaceShouldConvertCorrectlyToYaml() {
        Workspace workspace = new Workspace("abcdef-123456", "Name", List.of());

        String yaml = assertDoesNotThrow(() -> YamlMapper.toYaml(workspace));
        System.out.println(yaml);

        assertEquals(
                yaml,
                """
            exclude: []
            id: "abcdef-123456"
            include:
            - "*.yaml"
            kind: "Workspace"
            name: "Name"
            version: 1
            """);
    }

    @Test
    void workspaceShouldBeLoadedFromYaml() {
        String yaml =
                """
            exclude: []
            id: "abcdef-123456"
            include:
            - "*.yaml"
            kind: "Workspace"
            name: "Name"
            version: 1""";

        Workspace workspace = assertDoesNotThrow(() -> YamlMapper.fromYaml(yaml, Workspace.class));

        System.out.println(workspace);
    }
}
