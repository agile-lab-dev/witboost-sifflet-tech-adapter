package com.witboost.provisioning.dq.sifflet.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileManager {

    public Path initTempDirectory(String prefix) throws IOException {
        return Files.createTempDirectory(prefix);
    }

    public File createAndStoreTempFile(Path directory, String prefix, String suffix, String content)
            throws IOException {
        File tempFile = createTempFile(directory, prefix, suffix);
        PrintWriter writer = new PrintWriter(tempFile);
        writer.write(content);
        writer.close();
        return tempFile;
    }

    public File createTempFile(Path directory, String prefix, String suffix) throws IOException {
        return File.createTempFile(prefix, suffix, directory.toFile());
    }

    public String loadFile(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }
}
