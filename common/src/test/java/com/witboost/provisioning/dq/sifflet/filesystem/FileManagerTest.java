package com.witboost.provisioning.dq.sifflet.filesystem;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class FileManagerTest {

    FileManager fileManager = new FileManager();

    @Test
    void initTempDirectoryShouldCreateDirectory() throws IOException {
        Path tempDir = fileManager.initTempDirectory("test-prefix");
        assertTrue(Files.exists(tempDir));
        assertTrue(Files.isDirectory(tempDir));
        assertTrue(tempDir.getFileName().toString().startsWith("test-prefix"));
    }

    @Test
    void createTempFileShouldCreateFileInGivenDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("file-test-dir");
        File tempFile = fileManager.createTempFile(tempDir, "my-prefix-", ".txt");

        assertTrue(tempFile.exists());
        assertTrue(tempFile.getName().startsWith("my-prefix-"));
        assertTrue(tempFile.getName().endsWith(".txt"));
        assertEquals(tempDir.toFile(), tempFile.getParentFile());
    }

    @Test
    void createAndStoreTempFileShouldWriteContent() throws IOException {
        Path tempDir = Files.createTempDirectory("store-test-dir");
        String content = "Hello, world!";

        File storedFile = fileManager.createAndStoreTempFile(tempDir, "data-", ".yaml", content);

        assertTrue(storedFile.exists());
        String fileContent = Files.readString(storedFile.toPath());
        assertEquals(content, fileContent);
    }

    @Test
    void loadFileShouldReadFileContent() throws IOException {
        Path tempDir = Files.createTempDirectory("load-test-dir");
        File file = new File(tempDir.toFile(), "load-test.txt");

        String expectedContent = "Testing loadFile method";
        Files.writeString(file.toPath(), expectedContent);

        String actualContent = fileManager.loadFile(file);
        assertEquals(expectedContent, actualContent);
    }
}
