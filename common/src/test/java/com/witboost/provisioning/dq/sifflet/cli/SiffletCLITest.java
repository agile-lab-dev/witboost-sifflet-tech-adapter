package com.witboost.provisioning.dq.sifflet.cli;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.witboost.provisioning.dq.sifflet.filesystem.FileManager;
import com.witboost.provisioning.dq.sifflet.model.YamlMapper;
import com.witboost.provisioning.dq.sifflet.model.cli.Workspace;
import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class SiffletCLITest {

    @Mock
    FileManager fileManager;

    @Spy
    @InjectMocks
    SiffletCLI siffletCLI;

    @BeforeEach
    void setUp() {}

    @Test
    void initWorkspace_shouldLoadWorkspaceCorrectly() throws Exception {
        String fileLocation = "/tmp/test-ws.yaml";
        String name = "test-ws";
        String yamlContent = "name: test-ws";

        doReturn(Collections.emptyList()).when(siffletCLI).launchProcess(any(ProcessBuilder.class));

        when(fileManager.loadFile(any(File.class))).thenReturn(yamlContent);

        Workspace workspace = new Workspace("test-id", "test-ws", Collections.emptyList());
        mockStatic(YamlMapper.class)
                .when(() -> YamlMapper.fromYaml(yamlContent, Workspace.class))
                .thenReturn(workspace);

        Workspace result = siffletCLI.initWorkspace(name, fileLocation);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("test-ws");
    }

    @Test
    void applyWorkspace_shouldInvokeCLI() throws Exception {
        doReturn(Collections.emptyList()).when(siffletCLI).launchProcess(any(ProcessBuilder.class));

        assertThatCode(() -> siffletCLI.applyWorkspace("/tmp/test-ws.yaml")).doesNotThrowAnyException();
    }

    @Test
    void getWorkspaceId_shouldReturnMatchingId() throws Exception {
        String expectedId = "abc123ab-1234-5678-abcd-abcdef123456";
        List<String> mockOutput = List.of(" - abc123ab-1234-5678-abcd-abcdef123456 - workspace");

        doReturn(mockOutput).when(siffletCLI).launchProcess(any(ProcessBuilder.class));

        Optional<String> result = siffletCLI.getWorkspaceId("workspace");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedId);
    }

    @Test
    void getWorkspaceId_shouldReturnEmpty_whenNoMatchFound() throws Exception {
        List<String> mockOutput = List.of(" - abc123-uuid - anotherworkspace");

        doReturn(mockOutput).when(siffletCLI).launchProcess(any(ProcessBuilder.class));

        Optional<String> result = siffletCLI.getWorkspaceId("nomatch");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteWorkspace_shouldThrowException_whenIdInvalid() {
        assertThatThrownBy(() -> siffletCLI.deleteWorkspace("invalid_id"))
                .isInstanceOf(InvalidParameterException.class)
                .hasMessageContaining("Workspace ID is not an UUID");
    }

    @Test
    void deleteWorkspace_shouldInvokeCLI_whenIdValid() throws Exception {
        doReturn(Collections.emptyList()).when(siffletCLI).launchProcess(any(ProcessBuilder.class));

        String validId = "123e4567-e89b-12d3-a456-426614174000";

        assertThatCode(() -> siffletCLI.deleteWorkspace(validId)).doesNotThrowAnyException();
    }

    @Test
    void launchProcess_shouldCaptureOutputAndReturnList() throws Exception {
        ProcessBuilder mockBuilder = mock(ProcessBuilder.class);
        Process mockProcess = mock(Process.class);

        String processOutput = "output line 1\noutput line 2\n";
        InputStream inputStream = new ByteArrayInputStream(processOutput.getBytes());

        when(mockBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(inputStream);
        when(mockProcess.waitFor()).thenReturn(0);

        List<String> output = siffletCLI.launchProcess(mockBuilder);

        assertThat(output).containsExactly("output line 1", "output line 2");
        verify(mockBuilder).start();
        verify(mockProcess).waitFor();
    }

    @Test
    void launchProcess_shouldLogErrorWhenIOExceptionOccurs() throws Exception {
        System.out.println(LoggerFactory.getILoggerFactory().getClass());
        ProcessBuilder mockBuilder = mock(ProcessBuilder.class);
        Process mockProcess = mock(Process.class);

        InputStream brokenInputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Broken input stream");
            }
        };

        when(mockBuilder.start()).thenReturn(mockProcess);
        lenient().when(mockProcess.getInputStream()).thenReturn(brokenInputStream);

        List<String> output = siffletCLI.launchProcess(mockBuilder);

        assertThat(output).isEmpty();
    }
}
