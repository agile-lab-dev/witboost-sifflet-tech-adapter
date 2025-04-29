package com.witboost.provisioning.dq.sifflet.model.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.witboost.provisioning.dq.sifflet.model.YamlMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkspaceTest {

    @Test
    void workspaceShouldConvertCorrectlyToYaml() {
        Workspace workspace = new Workspace("abcdef-123456", "Name", List.of());

        String yaml = assertDoesNotThrow(() -> YamlMapper.toYaml(workspace));

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
        assertEquals("Name", workspace.getName());
        assertEquals("abcdef-123456", workspace.getId());
    }

    @Test
    void testWorkspaceCreationWithIdAndName() {
        Workspace workspace = new Workspace("WS-ID-123", "MyWorkspace", List.of());

        assertNotNull(workspace);
        assertEquals("WS-ID-123", workspace.getId());
        assertEquals("MyWorkspace", workspace.getName());
        assertEquals("Workspace", workspace.getKind());
        assertEquals(1, workspace.getVersion());
        assertEquals(List.of("*.yaml"), workspace.getInclude());
        assertEquals(List.of(), workspace.getExclude());
    }

    @Test
    void testWorkspaceCreationFromExisting() {
        Workspace original = new Workspace("WS-ID-123", "Original", List.of());
        Workspace updated = new Workspace(
                original, List.of(Monitor.builder().name("Monitor1").build()));

        assertEquals(original.getId(), updated.getId());
        assertEquals(original.getName(), updated.getName());
        assertEquals(List.of(Monitor.builder().name("Monitor1").build()), updated.getMonitors());
        assertEquals(original.getInclude(), updated.getInclude());
    }

    @Test
    void testWorkspaceToStringAndEquality() {
        Workspace workspace1 = new Workspace("WS-ID-123", "Workspace1", List.of());
        Workspace workspace2 = new Workspace("WS-ID-123", "Workspace1", List.of());
        Workspace workspace3 = new Workspace("WS-ID-124", "Workspace2", List.of());

        assertEquals(workspace1, workspace2);
        assertNotEquals(workspace1, workspace3);
        assertTrue(workspace1.toString().contains("Workspace1"));
    }

    @Test
    void testHashCodeConsistency() {
        Workspace workspace1 = new Workspace("123", "TestWorkspace", List.of());
        Workspace workspace2 = new Workspace("123", "TestWorkspace", List.of());
        Workspace workspace3 = new Workspace("456", "AnotherWorkspace", List.of());

        assertEquals(workspace1.hashCode(), workspace2.hashCode());
        assertNotEquals(workspace1.hashCode(), workspace3.hashCode());

        Workspace workspaceWithNullFields = new Workspace(null, null, null);
        assertNotNull(workspaceWithNullFields.hashCode());
    }

    @Test
    void testEquals() {
        Workspace workspace1 = new Workspace("123", "TestWorkspace", List.of());
        Workspace workspace2 = new Workspace("123", "TestWorkspace", List.of());
        Workspace workspace3 = new Workspace("456", "AnotherWorkspace", List.of());
        Workspace workspaceWithNullFields = new Workspace(null, null, null);

        assertEquals(workspace1, workspace1);
        assertEquals(workspace1, workspace2);
        assertNotEquals(workspace1, workspace3);
        assertNotEquals(null, workspace1);
        assertNotEquals(new Object(), workspace1);
        assertNotEquals(workspace1, workspaceWithNullFields);
        assertEquals(new Workspace(null, null, null), workspaceWithNullFields);
    }
}
