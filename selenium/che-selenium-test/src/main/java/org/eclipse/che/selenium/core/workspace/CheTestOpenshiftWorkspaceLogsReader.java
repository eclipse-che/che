/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** @author Dmytro Nochevnov */
public class CheTestOpenshiftWorkspaceLogsReader extends TestWorkspaceLogsReader {

  private final List<LogInfo> logInfos =
      ImmutableList.of(
          create("bootstrapper", Paths.get("/workspace_logs/bootstrapper")),
          create("exec-agent", Paths.get("/workspace_logs/exec-agent")),
          create("ws-agent", Paths.get("/workspace_logs/ws-agent")));

  @Override
  String getReadLogsCommand(
      String workspaceId, Path testLogsDirectory, Path logLocationInsideWorkspace) {
    return format(
        "docker cp $(docker ps -q -f name=k8s_container_%s | head -1):%s %s",
        workspaceId, logLocationInsideWorkspace, testLogsDirectory);
  }

  @Override
  List<LogInfo> getLogInfos() {
    return logInfos;
  }

  @Override
  boolean canWorkspaceLogsBeRead() {
    return true;
  }
}
