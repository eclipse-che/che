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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.inject.Singleton;
import org.eclipse.che.selenium.core.utils.DockerUtil;

/** @author Dmytro Nochevnov */
@Singleton
public class CheTestDockerWorkspaceLogsGrabber extends TestWorkspaceLogsGrabber {

  @Inject private DockerUtil dockerUtil;

  private final List<WorkspaceLog> workspaceLogs =
      ImmutableList.of(
          new WorkspaceLog("ws-agent", Paths.get("/home/user/che/ws-agent/logs")),
          new WorkspaceLog("exec-agent", Paths.get("/home/user/che/exec-agent/logs")),
          new WorkspaceLog("tomcat", Paths.get("/home/user/che/tomcat8/logs")),
          new WorkspaceLog("apache", Paths.get("/var/log/apache2")),
          new WorkspaceLog("traefik", Paths.get("/home/user/che/traefik/log.txt")));

  @Override
  String getGrabLogsCommand(
      String workspaceId, Path testLogsDirectory, Path logDestinationInsideWorkspace) {
    return format(
        "docker cp $(docker ps -q -f name=%s):%s %s",
        workspaceId, logDestinationInsideWorkspace, testLogsDirectory);
  }

  @Override
  List<WorkspaceLog> getLogs() {
    return workspaceLogs;
  }

  @Override
  boolean canLogsBeGrabbed() {
    return dockerUtil.isCheRunLocally();
  }
}
