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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;

import com.google.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read and store the workspace logs. Uses command line operations to obtain the logs from workspace
 * container.
 *
 * @author Dmytro Nochevnov
 */
public abstract class TestWorkspaceLogsReader {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  /**
   * Read logs from workspace.
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
      LOG.warn("It's impossible to get id of test workspace.", e);
      return;
    }

    // check if workspace is running
    try {
      WorkspaceStatus status = workspaceServiceClient.getStatus(workspaceId);
      if (status != RUNNING) {
        LOG.warn(
            "It's impossible to get logs of workspace with id='{}' because of improper status '{}'",
            workspaceId,
            status);
        return;
      }
    } catch (Exception e) {
      LOG.warn("It's impossible to get status of workspace with id='{}'", workspaceId, e);
      return;
    }

    getLogProviders()
        .forEach(workspaceLogProvider -> workspaceLogProvider.readLog(workspaceId, pathToStore));
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
  abstract List<WorkspaceLogProvider> getLogProviders();

  /**
   * Checks if it is possible to read logs from workspace.
   *
   * @return <b>true</b> if it is possible to read logs from workspace, or <b>false</b> otherwise.
   */
  abstract boolean canWorkspaceLogsBeRead();

  /** Represents provider which is aimed to read logs from certain location inside workspace. */
  class WorkspaceLogProvider {
    private final String logName;
    private final Path logLocationInsideWorkspace;

    WorkspaceLogProvider(String logName, Path logLocationInsideWorkspace) {
      this.logName = logName;
      this.logLocationInsideWorkspace = logLocationInsideWorkspace;
    }

    String getLogName() {
      return logName;
    }

    Path getLogLocationInsideWorkspace() {
      return logLocationInsideWorkspace;
    }

    private void readLog(String workspaceId, Path pathToStore) {
      Path testLogsDirectory = pathToStore.resolve(workspaceId).resolve(getLogName());

      try {
        Files.createDirectories(testLogsDirectory.getParent());

        // execute command to copy logs from workspace container to the workspaceLogsDir
        String[] commandLine = {
          "bash",
          "-c",
          getReadLogsCommand(workspaceId, testLogsDirectory, getLogLocationInsideWorkspace())
        };

        ProcessUtil.executeAndWait(
            commandLine,
            PREPARING_WS_TIMEOUT_SEC,
            SECONDS,
            getListLineConsumer(),
            getListLineConsumer());
      } catch (Exception e) {
        LOG.warn(
            "Can't obtain '{}' logs from workspace with id='{}' from directory '{}'.",
            getLogName(),
            workspaceId,
            getLogLocationInsideWorkspace(),
            e);
      }
    }

    private ListLineConsumer getListLineConsumer() {
      return new ListLineConsumer();
    }
  }
}
