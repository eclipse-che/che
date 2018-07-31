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
package org.eclipse.che.selenium.core.client;

import java.util.List;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.MemoryMeasure;

public interface TestWorkspaceServiceClient {

  List<String> getAll() throws Exception;

  void stop(String workspaceName, String userName) throws Exception;

  Workspace getByName(String workspace, String username) throws Exception;

  boolean exists(String workspace, String username) throws Exception;

  void delete(String workspaceName, String userName) throws Exception;

  void waitStatus(String workspaceName, String userName, WorkspaceStatus expectedStatus)
      throws Exception;

  /** Creates a new workspace. */
  Workspace createWorkspace(
      String workspaceName, int memory, MemoryMeasure memoryUnit, WorkspaceConfigDto workspace)
      throws Exception;

  void sendStartRequest(String workspaceId, String workspaceName) throws Exception;

  /** Starts workspace. */
  void start(String workspaceId, String workspaceName, TestUser workspaceOwner) throws Exception;

  WorkspaceDto getById(String workspaceId) throws Exception;

  WorkspaceStatus getStatus(String workspaceId) throws Exception;

  @Deprecated
  @Nullable
  String getServerAddressByPort(String workspaceId, int port) throws Exception;

  @Nullable
  Server getServerFromDevMachineBySymbolicName(String workspaceId, String serverName)
      throws Exception;

  void ensureRunningStatus(Workspace workspace) throws IllegalStateException;

  void deleteFactoryWorkspaces(String originalName, String username) throws Exception;
}
