package com.witboost.provisioning.dq.sifflet.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.witboost.provisioning.dq.sifflet.filesystem.FileManager;
import com.witboost.provisioning.dq.sifflet.model.YamlMapper;
import com.witboost.provisioning.dq.sifflet.model.cli.*;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import io.vavr.control.Either;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceManager {

    private final Logger logger = LoggerFactory.getLogger(WorkspaceManager.class);

    private final SiffletCLI cli;
    private final FileManager fileManager;

    public WorkspaceManager(SiffletCLI cli, FileManager fileManager) {
        this.cli = cli;
        this.fileManager = fileManager;
    }

    /**
     * Returns a Workspace based on its name. This method does not retrieve monitor information
     * as Sifflet doesn't provide the command for this.
     * @param name Workspace name
     * @return Optional with the found Workspace if existent on Sifflet
     */
    public Either<FailedOperation, Optional<Workspace>> get(String name) {
        logger.info("Querying for workspace with name '{}' existence", name);
        try {
            return Either.right(cli.getWorkspaceId(name).map(id -> new Workspace(id, name, List.of())));
        } catch (Exception exception) {
            String error = String.format(
                    "An unexpected error occurred while retrieving workspace named '%s'. Details: %s",
                    name, exception.getMessage());
            logger.error(error, exception);
            return Either.left(new FailedOperation(error, List.of(new Problem(error, exception))));
        }
    }

    /**
     * Upserts a workspace based on a workspace name and a list of monitors
     * @param name Workspace name <db_name>_<table_name>
     * @param monitors List of monitors to be created. Monitors not present on this list won't be modified
     * @return Workspace with assigned id and list of PersistentMonitors
     */
    public Either<FailedOperation, Workspace> createOrUpdate(String name, List<Monitor> monitors) {
        try {
            Path workspaceDir = fileManager.initTempDirectory(name);
            var eitherWorkspace = get(name);
            if (eitherWorkspace.isLeft()) return Either.left(eitherWorkspace.getLeft());
            var optionalWorkspace = eitherWorkspace.get();

            File workspaceFile;
            Workspace workspace;
            if (optionalWorkspace.isPresent()) {
                logger.info("Workspace '{}' is present on sifflet, recreating workspace file", name);
                // recreate workspace file
                workspace = optionalWorkspace.get();
                workspaceFile =
                        fileManager.createAndStoreTempFile(workspaceDir, name, ".yaml", YamlMapper.toYaml(workspace));
                logger.debug("Workspace file will be located in {}", workspaceFile);
            } else {
                logger.info("Workspace '{}' is not present on sifflet, initializing workspace file", name);
                workspaceFile = new File(Path.of(workspaceDir.toString(), String.format("%s.yaml", name))
                        .toString());
                logger.debug("Workspace file will be located in {}", workspaceFile);
                workspace = cli.initWorkspace(name, workspaceFile.getAbsolutePath());
            }

            // create monitor files
            logger.info("Creating {} monitors", monitors.size());
            List<PersistentMonitor> persistentMonitors = monitors.stream()
                    .map(monitor -> {
                        String friendlyId = String.format("%s_%s", name, monitor.getName());
                        PersistentMonitor persistentMonitor = new PersistentMonitor(friendlyId, monitor);
                        try {
                            logger.info("Creating monitor {} yaml file", monitor.getName());
                            fileManager.createAndStoreTempFile(
                                    workspaceDir, "monitor-", ".yaml", YamlMapper.toYaml(persistentMonitor));
                            return persistentMonitor;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

            // apply
            logger.info("Applying workspace '{}' changes, including {} monitors", name, monitors.size());
            cli.applyWorkspace(workspaceFile.getAbsolutePath());

            return Either.right(new Workspace(workspace, persistentMonitors));
        } catch (IOException | InterruptedException exception) {
            String error = String.format(
                    "An unexpected error occurred while creating workspace named '%s'. Details: %s",
                    name, exception.getMessage());
            logger.error(error, exception);
            return Either.left(new FailedOperation(error, List.of(new Problem(error, exception))));
        } catch (ProcessFailedException exception) {
            return Either.left(parseProcessFailedException(exception));
        }
    }

    private FailedOperation parseProcessFailedException(ProcessFailedException exception) {
        String yaml = exception.getStandardOutput().stream()
                .map(line -> {
                    // Sifflet response on stdout is almost a valid YAML,
                    // with only one line having an invalid key format.
                    // To be able to easily parse the response, we find this line and reformat it
                    if (line.contains("Change summary"))
                        return line.replace(" ", "_").toLowerCase();
                    else return line;
                })
                .collect(Collectors.joining("\n"));
        try {
            WorkspaceApplyOutput workspaceApplyOutput = YamlMapper.fromYaml(yaml, WorkspaceApplyOutput.class);
            logger.error(
                    "Workspace creation failed. Summary: {}", String.join(",", workspaceApplyOutput.change_summary()));
            logger.error("Detailed errors: {}", workspaceApplyOutput.changes());
            Optional<String> errorsOccurred = workspaceApplyOutput.change_summary().stream()
                    .filter(s -> s.contains("errors occurred"))
                    .findFirst();
            return new FailedOperation(
                    String.format(
                            "%s while upserting workspace and monitors on Sifflet",
                            errorsOccurred.orElse("Errors occurred")),
                    Stream.concat(
                                    workspaceApplyOutput.changes().stream()
                                            .flatMap(workspaceApplyChanges -> workspaceApplyChanges.logs().stream()
                                                    .map(WorkspaceApplyChanges.Log::message)),
                                    Stream.of(
                                            String.format("Raw output:\n%s", YamlMapper.toYaml(workspaceApplyOutput))))
                            .map(Problem::new)
                            .collect(Collectors.toList()));

        } catch (JsonProcessingException e) {
            String error = String.format(
                    "An unexpected error occurred while parsing Sifflet response: \n%s. \n\nDetails: %s",
                    yaml, e.getMessage());
            logger.error(error, e);
            return new FailedOperation(error, List.of(new Problem(error, e)));
        }
    }

    /**
     * Deletes a workspace and all monitors associated with it. This method does not retrieve monitor information
     * as Sifflet doesn't provide the command for this.
     * @param name WOrkspace name
     * @return Optional workspace if the workspace existed on Sifflet, empty otherwise
     */
    public Either<FailedOperation, Optional<Workspace>> delete(String name) {
        try {
            var eitherWorkspace = get(name);
            if (eitherWorkspace.isLeft()) return Either.left(eitherWorkspace.getLeft());
            var optionalWorkspace = eitherWorkspace.get();

            if (optionalWorkspace.isPresent()) {
                logger.info("Workspace with name '{}' exists. Removing", name);
                cli.deleteWorkspace(optionalWorkspace.get().getId());
                logger.info("Workspace '{}' successfully removed", name);
            }
            return Either.right(optionalWorkspace);
        } catch (IOException | InterruptedException exception) {
            String error = String.format(
                    "An unexpected error occurred while deleting Sifflet workspace: %s", exception.getMessage());
            logger.error(error, exception);
            return Either.left(new FailedOperation(error, List.of(new Problem(error, exception))));
        }
    }
}
