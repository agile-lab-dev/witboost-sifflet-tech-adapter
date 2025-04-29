package com.witboost.provisioning.dq.sifflet.cli;

import com.witboost.provisioning.dq.sifflet.filesystem.FileManager;
import com.witboost.provisioning.dq.sifflet.model.YamlMapper;
import com.witboost.provisioning.dq.sifflet.model.cli.Workspace;
import io.vavr.Tuple2;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SiffletCLI {

    private final Logger logger = LoggerFactory.getLogger(SiffletCLI.class);

    private final FileManager fileManager;

    public SiffletCLI(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    private List<String> buildCommand(String... strings) {
        return System.getProperty("os.name").toLowerCase().startsWith("win")
                ? Stream.concat(Stream.of("cmd.exe", "/c"), Stream.of(strings)).toList()
                : List.of("sh", "-c", String.join(" ", strings));
    }

    /**
     * Launches a process and blocks the main thread until it finishes, while gathering stdout and stderr outputs.
     * @param builder Builder ready to be executed
     * @return List of string representing the output
     * @throws IOException
     * @throws InterruptedException
     */
    protected List<String> launchProcess(ProcessBuilder builder) throws IOException, InterruptedException {
        logger.debug("Executing {}...", builder.command());
        Process process = builder.start();

        List<String> processInputStreamOutput = new Vector<>();
        // Create a thread to read the process's input stream
        new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            processInputStreamOutput.add(line.stripTrailing());
                        }
                    } catch (IOException e) {
                        logger.error("Error reading process stream", e);
                    }
                })
                .start();

        List<String> processErrorStreamOutput = new Vector<>();
        // Create a thread to read the process's error stream
        new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            processErrorStreamOutput.add(line.stripTrailing());
                        }
                    } catch (IOException e) {
                        logger.error("Error reading process stream", e);
                    }
                })
                .start();

        process.waitFor(); // this blocks the main thread
        logger.debug("Execution finished");
        if (process.exitValue() != 0) {
            throw new ProcessFailedException(processInputStreamOutput, processErrorStreamOutput);
        }

        return processInputStreamOutput;
    }

    public Workspace initWorkspace(String name, String fileLocation) throws IOException, InterruptedException {
        logger.info("Executing sifflet workspace init on CLI");
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(fileLocation).getParentFile())
                .command(buildCommand("sifflet code workspace init", "--name", name, "--file", fileLocation));

        launchProcess(builder);

        // Loading new workspace after creation
        String content = fileManager.loadFile(new File(fileLocation));
        Workspace workspace = YamlMapper.fromYaml(content, Workspace.class);
        logger.debug("New workspace: {}", workspace);

        return workspace;
    }

    public void applyWorkspace(String fileLocation) throws IOException, InterruptedException {
        logger.info("Executing sifflet workspace apply on CLI");
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(fileLocation).getParentFile())
                .command(buildCommand(
                        "sifflet code workspace apply", "--file", fileLocation, "--fail-on-error", "--force-delete"))
                .environment()
                .put("COLUMNS", "200"); // Avoids output truncation on some consoles

        launchProcess(builder);
    }

    public Optional<String> getWorkspaceId(String name) throws IOException, InterruptedException {
        logger.info("Executing sifflet workspace list on CLI");
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(buildCommand("sifflet code workspace list"))
                .environment()
                .put("COLUMNS", "200"); // Avoids output truncation on some consoles;

        List<String> outputs = launchProcess(builder);

        logger.info("Sifflet returned the existence of {} workspaces", outputs.size());
        logger.debug("CLI output:\n{}", outputs);
        // List of workspaces is returned with pattern:
        //  - <UUID> - <Workspace name>
        Pattern p = Pattern.compile("^ - ([a-f0-9\\-]+) - (\\w+)$");
        return outputs.stream()
                .map(p::matcher)
                .filter(Matcher::matches)
                .map(matcher -> {
                    String id = matcher.group(1);
                    String workspaceName = matcher.group(2);
                    return new Tuple2<>(id, workspaceName);
                })
                .filter(workspace -> {
                    if (workspace._2.equalsIgnoreCase(name)) {
                        logger.info("Found matching workspace with id equal to {}", workspace._1);
                        return true;
                    }
                    return false;
                })
                .findFirst()
                .map(Tuple2::_1);
    }

    public void deleteWorkspace(String id) throws IOException, InterruptedException {
        logger.info("Executing sifflet workspace delete on CLI");
        if (!Pattern.matches("^[a-fA-F0-9\\-]+$", id)) {
            throw new InvalidParameterException("Workspace ID is not an UUID");
        }
        ProcessBuilder builder = new ProcessBuilder();
        // nosemgrep
        builder.command(buildCommand("sifflet code workspace delete", "--id", id));
        launchProcess(builder);
    }
}
