/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.workspace;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.selenium.core.utils.FileUtil.removeDirectoryIfItIsEmpty;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads and stores the workspace logs by using command line operations. It ignores absent or empty
 * logs directory.
 *
 * @author Dmytro Nochevnov
 */
public abstract class TestWorkspaceLogsReader {

  private static final String READ_LOGS_ERROR_MESSAGE_TEMPLATE =
      "Can't obtain '{}' logs from workspace with id='{}' from directory '{}'.";

  @VisibleForTesting Logger log = LoggerFactory.getLogger(this.getClass());

  @Inject @VisibleForTesting TestWorkspaceServiceClient workspaceServiceClient;

  @Inject @VisibleForTesting ProcessAgent processAgent;

  /**
   * Read logs from workspace. It ignores absent or empty logs directory.
   *
   * @param workspace workspace which logs should be read.
   * @param pathToStore location of directory where logs should be stored.
   */
  public void read(TestWorkspace workspace, Path pathToStore) {
    if (!canWorkspaceLogsBeRead()) {
      return;
    }

    final String workspaceId;
    try {
      workspaceId = workspace.getId();
    } catch (ExecutionException | InterruptedException e) {
      log.warn("It's impossible to get id of test workspace.", e);
      return;
    }

    // check if workspace is running
    try {
      WorkspaceStatus status = workspaceServiceClient.getStatus(workspaceId);
      if (status != RUNNING) {
        log.warn(
            "It's impossible to get logs of workspace with id='{}' because of improper status '{}'",
            workspaceId,
            status);
        return;
      }
    } catch (Exception e) {
      log.warn("It's impossible to get status of workspace with id='{}'", workspaceId, e);
      return;
    }

    getLogInfos().forEach(logInfo -> readLog(logInfo, workspaceId, pathToStore));
  }

  private void readLog(LogInfo logInfo, String workspaceId, Path pathToStore) {
    Path testLogsDirectory = pathToStore.resolve(workspaceId).resolve(logInfo.getName());

    try {
      Files.createDirectories(testLogsDirectory.getParent());

      // execute command to copy logs from workspace container to the workspaceLogsDir
      processAgent.execute(
          getReadLogsCommand(workspaceId, testLogsDirectory, logInfo.getLocationInsideWorkspace()));
    } catch (Exception e) {
      log.warn(
          READ_LOGS_ERROR_MESSAGE_TEMPLATE,
          logInfo.getName(),
          workspaceId,
          logInfo.getLocationInsideWorkspace(),
          e);
    } finally {
      try {
        removeDirectoryIfItIsEmpty(testLogsDirectory);
      } catch (IOException e) {
        log.warn("Error of removal of empty log directory {}.", testLogsDirectory, e);
      }
    }
  }

  /**
   * Returns bash command to read logs from workspace by path to them inside workspace.
   *
   * @param workspaceId ID of workspace
   * @param testLogsDirectory location of directory to save the logs
   * @param logLocationInsideWorkspace location of logs inside workspace
   * @return command to read logs from workspace
   */
  abstract String getReadLogsCommand(
      String workspaceId, Path testLogsDirectory, Path logLocationInsideWorkspace);

  /**
   * Gets list of available workspace log providers which are dedicated to read certain logs.
   *
   * @return list of log providers
   */
  abstract List<LogInfo> getLogInfos();

  /**
   * Checks if it is possible to read logs from workspace.
   *
   * @return <b>true</b> if it is possible to read logs from workspace, or <b>false</b> otherwise.
   */
  abstract boolean canWorkspaceLogsBeRead();

  @VisibleForTesting
  LineConsumer getStdoutConsumer() {
    return new AbstractLineConsumer() {};
  }

  @VisibleForTesting
  ListLineConsumer getStderrConsumer() {
    return new ListLineConsumer();
  }

  /** Holds information about log to read. */
  static class LogInfo {
    private final String name;
    private final Path locationInsideWorkspace;

    private LogInfo(String name, Path locationInsideWorkspace) {
      this.name = name;
      this.locationInsideWorkspace = locationInsideWorkspace;
    }

    String getName() {
      return name;
    }

    Path getLocationInsideWorkspace() {
      return locationInsideWorkspace;
    }

    static LogInfo create(String name, Path locationInsideWorkspace) {
      return new LogInfo(name, locationInsideWorkspace);
    }
  }
}
