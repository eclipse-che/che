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
public class CheTestDockerWorkspaceLogsReader extends TestWorkspaceLogsReader {

  @Inject private DockerUtil dockerUtil;

  private final List<WorkspaceLogProvider> workspaceLogProviders =
      ImmutableList.of(
          new WorkspaceLogProvider("ws-agent", Paths.get("/home/user/che/ws-agent/logs")),
          new WorkspaceLogProvider("exec-agent", Paths.get("/home/user/che/exec-agent/logs")),
          new WorkspaceLogProvider("tomcat", Paths.get("/home/user/tomcat8/logs")),
          new WorkspaceLogProvider("apache", Paths.get("/var/log/apache2")),
          new WorkspaceLogProvider("traefik", Paths.get("/home/user/che/traefik/log.txt")));

  @Override
  String getReadLogsCommand(
      String workspaceId, Path testLogsDirectory, Path logLocationInsideWorkspace) {
    return format(
        "[[ $(docker exec -i $(docker ps -q -f name=%1$s) bash -c '[ -d %2$s ] && echo true || echo false') == true ]] "
            + "&& docker cp $(docker ps -q -f name=%1$s):%2$s %3$s",
        workspaceId, logLocationInsideWorkspace, testLogsDirectory);
  }

  @Override
  List<WorkspaceLogProvider> getLogProviders() {
    return workspaceLogProviders;
  }

  @Override
  boolean canWorkspaceLogsBeRead() {
    return dockerUtil.isCheRunLocally();
  }
}
