/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.workspace;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.workspace.TestWorkspaceLogsReader.LogInfo.create;

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

  private final List<LogInfo> logInfos =
      ImmutableList.of(
          create("ws-agent", Paths.get("/home/user/che/ws-agent/logs")),
          create("exec-agent", Paths.get("/home/user/che/exec-agent/logs")),
          create("tomcat", Paths.get("/home/user/tomcat8/logs")),
          create("apache", Paths.get("/var/log/apache2")),
          create("traefik", Paths.get("/home/user/che/traefik/log.txt")));

  @Override
  String getReadLogsCommand(
      String workspaceId, Path testLogsDirectory, Path logLocationInsideWorkspace) {
    return format(
        "if [[ $(docker exec -i $(docker ps -q -f name=%1$s) bash -c '[ -d %2$s ] && echo true || echo false') == true ]]; then "
            + "docker cp $(docker ps -q -f name=%1$s | head -1):%2$s %3$s; fi",
        workspaceId, logLocationInsideWorkspace, testLogsDirectory);
  }

  @Override
  List<LogInfo> getLogInfos() {
    return logInfos;
  }

  @Override
  boolean canWorkspaceLogsBeRead() {
    return dockerUtil.isCheRunLocally();
  }
}
