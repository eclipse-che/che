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

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.utils.FileUtil.removeEmptyDirectory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.utils.DockerUtil;
import org.eclipse.che.selenium.core.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Dmytro Nochevnov */
@Singleton
public class CheTestDockerWorkspaceLogsGrabber implements TestWorkspaceLogsGrabber {

  private static final Logger LOG =
      LoggerFactory.getLogger(CheTestDockerWorkspaceLogsGrabber.class);

  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private DockerUtil dockerUtil;

  @Inject
  @Named("che.infrastructure")
  private String infrastructure;

  private enum WorkspaceLog {
    WS_AGENT("/home/user/che/ws-agent/logs/"),
    EXEC_AGENT("/home/user/che/exec-agent/logs/"),
    TOMCAT("/home/user/che/tomcat8/logs/"),
    APACHE("/var/log/apache2"),
    TRAEFIK("/home/user/che/traefik/log.txt");

    private final String workspacePath;

    WorkspaceLog(String workspacePathStr) {
      this.workspacePath = workspacePathStr;
    }

    public Path getWorkspacePath() {
      return Paths.get(workspacePath);
    }
  }

  @Override
  public void grabLogs(TestWorkspace workspace, Path pathToStore) {
    switch (infrastructure) {
      case "docker":
        grabLogsFromDocker(workspace, pathToStore);
        return;

      default:
        break;
    }
  }

  private void grabLogsFromDocker(TestWorkspace workspace, Path pathToStore) {
    if (!dockerUtil.isCheRunLocally()) {
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
            "It's impossible to get logs of workspace with workspaceId={} because of improper status {}",
            workspaceId,
            status);
        return;
      }
    } catch (Exception e) {
      LOG.warn("It's impossible to get status of workspace with id={}", workspaceId, e);
      return;
    }

    asList(WorkspaceLog.values())
        .stream()
        .forEach(log -> grabLogFromDocker(log, workspaceId, pathToStore));
  }

  private void grabLogFromDocker(WorkspaceLog log, String workspaceId, Path pathToStore) {
    ListLineConsumer outputConsumer = new ListLineConsumer();
    Path testLogsDirectory = pathToStore.resolve(workspaceId).resolve(log.name().toLowerCase());

    try {
      Files.createDirectories(testLogsDirectory);

      // execute command to copy logs from workspace container to the workspaceLogsDir
      String[] commandLine = {
        "bash",
        "-c",
        format(
            "docker cp $(docker ps -q -f name=%s):%s %s",
            workspaceId, log.getWorkspacePath(), testLogsDirectory.toString())
      };

      ProcessUtil.executeAndWait(commandLine, PREPARING_WS_TIMEOUT_SEC, SECONDS, outputConsumer);
    } catch (Exception e) {
      LOG.warn(
          "Can't obtain {} logs from workspace with id={} from directory {}. Error: {}",
          log.name(),
          workspaceId,
          log.getWorkspacePath(),
          outputConsumer.getText(),
          e);
    } finally {
      try {
        removeEmptyDirectory(testLogsDirectory);
      } catch (IOException e) {
        CheTestDockerWorkspaceLogsGrabber.LOG.warn(
            "Error of removal of empty log directory {}.", testLogsDirectory, e);
      }
    }
  }
}
