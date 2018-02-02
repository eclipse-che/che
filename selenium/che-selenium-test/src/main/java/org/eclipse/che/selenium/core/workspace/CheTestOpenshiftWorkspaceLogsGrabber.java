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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** @author Dmytro Nochevnov */
public class CheTestOpenshiftWorkspaceLogsGrabber extends TestWorkspaceLogsGrabber {

  private final List<WorkspaceLog> workspaceLogs =
      ImmutableList.of(
          new WorkspaceLog("bootstrapper", Paths.get("/workspace_logs/bootstrapper")),
          new WorkspaceLog("exec-agent", Paths.get("/workspace_logs/exec-agent")),
          new WorkspaceLog("ws-agent", Paths.get("/workspace_logs/ws-agent")));

  @Override
  String getGrabLogsCommand(
      String workspaceId, Path testLogsDirectory, Path logDestinationInsideWorkspace) {
    return format(
        "docker cp $(docker ps -q -f name=k8s_container_%s):%s %s",
        workspaceId, logDestinationInsideWorkspace, testLogsDirectory);
  }

  @Override
  List<WorkspaceLog> getLogs() {
    return workspaceLogs;
  }

  @Override
  boolean canLogsBeGrabbed() {
    return true;
  }
}
