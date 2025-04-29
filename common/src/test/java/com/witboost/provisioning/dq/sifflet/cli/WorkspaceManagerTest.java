package com.witboost.provisioning.dq.sifflet.cli;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.witboost.provisioning.dq.sifflet.filesystem.FileManager;
import com.witboost.provisioning.dq.sifflet.model.YamlMapper;
import com.witboost.provisioning.dq.sifflet.model.cli.*;
import com.witboost.provisioning.model.common.Problem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkspaceManagerTest {

    @Mock
    FileManager fileManager;

    @Mock
    SiffletCLI siffletCLI;

    @Test
    void monitorManagerShouldCreateNewWorkspaceWithTwoMonitors() throws IOException, InterruptedException {
        WorkspaceManager workspaceManager = new WorkspaceManager(siffletCLI, fileManager);

        List<String> monitorYamls = List.of(
                """
                        kind: Monitor
                        version: 2
                        name: ID field unique v2
                        description: "<p>This monitor checks the 'id' field within 'table0', focusing exclusively on ensuring its uniqueness across the dataset. The 'id' field, of type VARCHAR, is crucial for identifying records distinctly. No additional fields or datasets are targeted in this monitoring setup.</p>"
                        schedule: '@daily'
                        scheduleTimezone: UTC
                        incident:
                          severity: Low
                          createOnFailure: false
                        datasets:
                        - uri: awsathena://athena.eu-west-1.amazonaws.com/AwsDataCatalog.sifflet_demo.table0
                        parameters:
                          kind: FieldNulls
                          field: id
                          nullValues: NullEmptyAndWhitespaces
                        """,
                """
                        kind: Monitor
                        version: 2
                        name: Unique Monitor v2
                        description: "<p>This monitor checks the uniqueness of the 'id' field within 'table0'. It ensures that all entries in the 'id' field are unique, supporting data integrity and consistency in the dataset. This is crucial for datasets where 'id' serves as a primary key or a unique identifier for records.</p>"
                        schedule: '@daily'
                        scheduleTimezone: UTC
                        incident:
                          severity: High
                          createOnFailure: false
                        notifications:
                        - kind: Email
                          name: sergio.mejia@agilelab.it
                        datasets:
                        - uri: awsathena://athena.eu-west-1.amazonaws.com/AwsDataCatalog.sifflet_demo.table0
                        parameters:
                          kind: FieldDuplicates
                          field: id
                        """);

        List<Monitor> monitors = monitorYamls.stream()
                .map(yaml -> {
                    try {
                        return YamlMapper.fromYaml(yaml, Monitor.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        String workspaceName = "java_test";
        Path dir = Path.of("/tmp/java_test_123abc");
        String fileLocation = new File("/tmp/java_test_123abc/java_test.yaml").getAbsolutePath();

        when(fileManager.initTempDirectory("java_test")).thenReturn(dir);
        when(siffletCLI.getWorkspaceId("java_test")).thenReturn(Optional.empty());
        when(siffletCLI.initWorkspace("java_test", fileLocation))
                .thenReturn(new Workspace("abcdef-123456-abcdef-123456", "java_test", List.of()));
        lenient()
                .when(fileManager.createAndStoreTempFile(any(Path.class), anyString(), anyString(), anyString()))
                .thenReturn(new File(fileLocation));
        doNothing().when(siffletCLI).applyWorkspace(fileLocation);

        var output = workspaceManager.createOrUpdate(workspaceName, monitors);

        assertTrue(output.isRight());
        assertEquals("abcdef-123456-abcdef-123456", output.get().getId());
        assertEquals("java_test", output.get().getName());
        assertEquals(
                output.get().getMonitors().stream().map(Monitor::getName).toList(),
                monitors.stream().map(Monitor::getName).toList());
        output.get().getMonitors().forEach(monitor -> assertInstanceOf(PersistentMonitor.class, monitor));
    }

    @Test
    void getWorkspaceShouldReturnExistingWorkspace() throws IOException, InterruptedException {
        WorkspaceManager workspaceManager = new WorkspaceManager(siffletCLI, fileManager);

        when(siffletCLI.getWorkspaceId("java_test")).thenReturn(Optional.of("abcdef-123456-abcdef-123456"));

        var output = workspaceManager.get("java_test");
        assertTrue(output.isRight());
        assertTrue(output.get().isPresent());
        assertEquals("abcdef-123456-abcdef-123456", output.get().get().getId());
    }

    @Test
    void getWorkspaceShouldReturnNone() throws IOException, InterruptedException {
        WorkspaceManager workspaceManager = new WorkspaceManager(siffletCLI, fileManager);

        when(siffletCLI.getWorkspaceId("java_test")).thenReturn(Optional.empty());

        var output = workspaceManager.get("java_test");
        assertTrue(output.isRight());
        assertTrue(output.get().isEmpty());
    }

    @Test
    void deleteWorkspace() throws IOException, InterruptedException {
        WorkspaceManager workspaceManager = new WorkspaceManager(siffletCLI, fileManager);

        when(siffletCLI.getWorkspaceId("java_test")).thenReturn(Optional.of("abcdef-123456-abcdef-123456"));
        doNothing().when(siffletCLI).deleteWorkspace("abcdef-123456-abcdef-123456");

        var output = workspaceManager.delete("java_test");
        assertTrue(output.isRight());
        assertTrue(output.get().isPresent());
        assertEquals("abcdef-123456-abcdef-123456", output.get().get().getId());
    }

    @Test
    void createOrUpdateShouldHandleProcessFailedException() throws IOException, InterruptedException {
        WorkspaceManager workspaceManager = new WorkspaceManager(siffletCLI, fileManager);
        List<Monitor> monitors = List.of(new Monitor(
                "ID field unique",
                new Incident(Incident.Severity.Low, false),
                List.of(),
                List.of(),
                new Parameters.FieldNulls(),
                "@daily",
                "UTC"));

        String workspaceName = "java_test";
        Path dir = Path.of("/tmp/java_test_123abc");
        String fileLocation = new File("/tmp/java_test_123abc/java_test.yaml").getAbsolutePath();

        when(fileManager.initTempDirectory("java_test")).thenReturn(dir);
        when(siffletCLI.getWorkspaceId("java_test")).thenReturn(Optional.empty());
        when(siffletCLI.initWorkspace("java_test", fileLocation))
                .thenReturn(new Workspace("abcdef-123456-abcdef-123456", "java_test", List.of()));
        lenient()
                .when(fileManager.createAndStoreTempFile(any(Path.class), anyString(), anyString(), anyString()))
                .thenThrow(new IOException("Error creating monitor file"));

        var output = workspaceManager.createOrUpdate(workspaceName, monitors);

        assertTrue(output.isLeft());
        assertEquals(
                "An unexpected error occurred while creating workspace named 'java_test'. Details: java.io.IOException: Error creating monitor file",
                output.getLeft().message());
    }

    @Test
    void createOrUpdateShouldHandleWorkspaceNotFound() throws IOException, InterruptedException {
        WorkspaceManager workspaceManager = new WorkspaceManager(siffletCLI, fileManager);

        List<Monitor> monitors = List.of(new Monitor(
                "ID field unique",
                new Incident(Incident.Severity.Low, false),
                List.of(),
                List.of(),
                new Parameters.FieldNulls(),
                "@daily",
                "UTC"));

        String workspaceName = "java_test";
        Path dir = Path.of("/tmp/java_test_123abc");
        String fileLocation = new File("/tmp/java_test_123abc/java_test.yaml").getAbsolutePath();

        when(fileManager.initTempDirectory("java_test")).thenReturn(dir);
        when(siffletCLI.getWorkspaceId("java_test")).thenReturn(Optional.empty());
        when(siffletCLI.initWorkspace("java_test", fileLocation))
                .thenReturn(new Workspace("abcdef-123456-abcdef-123456", "java_test", List.of()));
        lenient()
                .when(fileManager.createAndStoreTempFile(any(Path.class), anyString(), anyString(), anyString()))
                .thenReturn(new File(fileLocation));
        doNothing().when(siffletCLI).applyWorkspace(fileLocation);

        var output = workspaceManager.createOrUpdate(workspaceName, monitors);

        assertTrue(output.isRight());
        assertEquals("abcdef-123456-abcdef-123456", output.get().getId());
    }

    @Test
    void createOrUpdateShouldHandleYamlParsingException() throws IOException, InterruptedException {
        WorkspaceManager workspaceManager = new WorkspaceManager(siffletCLI, fileManager);

        List<Monitor> monitors = List.of(new Monitor(
                "ID field unique",
                new Incident(Incident.Severity.Low, false),
                List.of(),
                List.of(),
                new Parameters.FieldNulls(),
                "@daily",
                "UTC"));

        String workspaceName = "java_test";
        Path dir = Path.of("/tmp/java_test_123abc");
        String fileLocation = new File("/tmp/java_test_123abc/java_test.yaml").getAbsolutePath();

        when(fileManager.initTempDirectory("java_test")).thenReturn(dir);
        when(siffletCLI.getWorkspaceId("java_test")).thenReturn(Optional.empty());
        when(siffletCLI.initWorkspace("java_test", fileLocation))
                .thenReturn(new Workspace("abcdef-123456-abcdef-123456", "java_test", List.of()));

        try (MockedStatic<YamlMapper> mockedStatic = mockStatic(YamlMapper.class)) {
            mockedStatic
                    .when(() -> YamlMapper.toYaml(any(PersistentMonitor.class)))
                    .thenThrow(new JsonMappingException("Error serializing monitor"));

            var output = workspaceManager.createOrUpdate(workspaceName, monitors);

            assertTrue(output.isLeft());
            assertEquals(
                    "An unexpected error occurred while creating workspace named 'java_test'. Details: com.fasterxml.jackson.databind.JsonMappingException: Error serializing monitor",
                    output.getLeft().message());
        }
    }

    @Test
    void deleteWorkspaceShouldHandleWorkspaceNotFound() throws IOException, InterruptedException {
        WorkspaceManager workspaceManager = new WorkspaceManager(siffletCLI, fileManager);

        when(siffletCLI.getWorkspaceId("java_test")).thenReturn(Optional.empty());

        var output = workspaceManager.delete("java_test");

        assertTrue(output.isRight());
        assertTrue(output.get().isEmpty());
    }

    @Test
    void deleteWorkspaceShouldHandleIOException() throws IOException, InterruptedException {
        WorkspaceManager workspaceManager = new WorkspaceManager(siffletCLI, fileManager);

        when(siffletCLI.getWorkspaceId("java_test")).thenReturn(Optional.of("abcdef-123456-abcdef-123456"));
        doThrow(new IOException("Error deleting workspace"))
                .when(siffletCLI)
                .deleteWorkspace("abcdef-123456-abcdef-123456");

        var output = workspaceManager.delete("java_test");

        assertTrue(output.isLeft());
        assertEquals(
                "An unexpected error occurred while deleting Sifflet workspace: Error deleting workspace",
                output.getLeft().message());
    }

    @Test
    void getShouldHandleUnexpectedException() throws IOException, InterruptedException {
        WorkspaceManager workspaceManager = new WorkspaceManager(siffletCLI, fileManager);

        String workspaceName = "java_test";

        when(siffletCLI.getWorkspaceId(workspaceName)).thenThrow(new RuntimeException("Database connection failed"));

        var output = workspaceManager.get(workspaceName);

        assertTrue(output.isLeft());
        String expectedMessage =
                "An unexpected error occurred while retrieving workspace named 'java_test'. Details: Database connection failed";
        assertEquals(expectedMessage, output.getLeft().message());

        List<Problem> problems = output.getLeft().problems();
        assertFalse(problems.isEmpty());
    }
}
