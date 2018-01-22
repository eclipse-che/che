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
package org.eclipse.che.selenium.core.client;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.WsAgentMachineFinderUtil.containsWsAgentServer;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.server.WsAgentMachineFinderUtil;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactoryCreator;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.MemoryMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * @author Musienko Maxim
 * @author Dmytro Nochevnov
 */
public class TestWorkspaceServiceClient {

  private static final Logger LOG = LoggerFactory.getLogger(TestWorkspaceServiceClient.class);

  private final TestApiEndpointUrlProvider apiEndpointProvider;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public TestWorkspaceServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider, HttpJsonRequestFactory requestFactory) {
    this.apiEndpointProvider = apiEndpointProvider;
    this.requestFactory = requestFactory;
  }

  @AssistedInject
  public TestWorkspaceServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider,
      TestUserHttpJsonRequestFactoryCreator userHttpJsonRequestFactoryCreator,
      @Assisted("name") String name,
      @Assisted("password") String password,
      @Assisted("offlineToken") String offlineToken) {
    this(
        apiEndpointProvider,
        userHttpJsonRequestFactoryCreator.create(name, password, offlineToken));
  }

  private String getBaseUrl() {
    return apiEndpointProvider.get() + "workspace";
  }

  /** Returns the list of workspaces names that belongs to the user. */
  public List<String> getAll() throws Exception {
    List<WorkspaceDto> workspaces =
        requestFactory.fromUrl(getBaseUrl()).request().asList(WorkspaceDto.class);
    return workspaces.stream().map(ws -> ws.getConfig().getName()).collect(Collectors.toList());
  }

  /** Sends stop workspace request. */
  private void sendStopRequest(String workspaceName, String userName) throws Exception {
    if (!exists(workspaceName, userName)) {
      return;
    }

    Workspace workspace = getByName(workspaceName, userName);
    String apiUrl = getIdBasedUrl(workspace.getId()) + "/runtime/";

    requestFactory.fromUrl(apiUrl).useDeleteMethod().request();
  }

  /** Stops workspace. */
  public void stop(String workspaceName, String userName) throws Exception {
    sendStopRequest(workspaceName, userName);
    waitStatus(workspaceName, userName, STOPPED);
  }

  /** Returns workspace of default user by its name. */
  public Workspace getByName(String workspace, String username) throws Exception {
    return requestFactory
        .fromUrl(getNameBasedUrl(workspace, username))
        .request()
        .asDto(WorkspaceDto.class);
  }

  /** Returns workspace by its name. */
  public Workspace getByName(String workspace, String username, String authToken) throws Exception {
    return requestFactory
        .fromUrl(getNameBasedUrl(workspace, username))
        .setAuthorizationHeader(authToken)
        .request()
        .asDto(WorkspaceDto.class);
  }

  /** Indicates if workspace exists. */
  public boolean exists(String workspace, String username) throws Exception {
    try {
      requestFactory.fromUrl(getNameBasedUrl(workspace, username)).request();
    } catch (NotFoundException e) {
      return false;
    }

    return true;
  }

  /** Deletes workspace of default user. */
  public void delete(String workspaceName, String userName) throws Exception {
    if (!exists(workspaceName, userName)) {
      return;
    }

    Workspace workspace = getByName(workspaceName, userName);
    if (workspace.getStatus() != STOPPED) {
      stop(workspaceName, userName);
    }

    requestFactory.fromUrl(getIdBasedUrl(workspace.getId())).useDeleteMethod().request();

    LOG.info(
        "Workspace name='{}', id='{}', username='{}' removed",
        workspaceName,
        workspace.getId(),
        userName);
  }

  /** Waits needed status. */
  public void waitStatus(String workspaceName, String userName, WorkspaceStatus expectedStatus)
      throws Exception {

    WorkspaceStatus status = null;
    for (int i = 0; i < 120; i++) {
      status = getByName(workspaceName, userName).getStatus();
      if (status == expectedStatus) {
        return;
      } else {
        WaitUtils.sleepQuietly(5);
      }
    }

    throw new IllegalStateException(
        format(
            "Workspace %s, status=%s, expected status=%s", workspaceName, status, expectedStatus));
  }

  /** Creates a new workspace. */
  public Workspace createWorkspace(
      String workspaceName, int memory, MemoryMeasure memoryUnit, WorkspaceConfigDto workspace)
      throws Exception {
    EnvironmentDto environment = workspace.getEnvironments().get("replaced_name");
    environment
        .getMachines()
        .values()
        .stream()
        .filter(WsAgentMachineFinderUtil::containsWsAgentServerOrInstaller)
        .forEach(
            m ->
                m.getAttributes()
                    .put(MEMORY_LIMIT_ATTRIBUTE, Long.toString(convertToByte(memory, memoryUnit))));
    workspace.getEnvironments().remove("replaced_name");
    workspace.getEnvironments().put(workspaceName, environment);
    workspace.setName(workspaceName);
    workspace.setDefaultEnv(workspaceName);

    WorkspaceDto workspaceDto =
        requestFactory
            .fromUrl(getBaseUrl())
            .usePostMethod()
            .setBody(workspace)
            .request()
            .asDto(WorkspaceDto.class);

    LOG.info("Workspace name='{}' and id='{}' created", workspaceName, workspaceDto.getId());

    return workspaceDto;
  }

  /** Sends start workspace request. */
  public void sendStartRequest(String workspaceId, String workspaceName) throws Exception {
    requestFactory
        .fromUrl(getIdBasedUrl(workspaceId) + "/runtime")
        .addQueryParam("environment", workspaceName)
        .usePostMethod()
        .request();
  }

  /** Starts workspace. */
  public void start(String workspaceId, String workspaceName, TestUser workspaceOwner)
      throws Exception {
    sendStartRequest(workspaceId, workspaceName);

    try {
      waitStatus(workspaceName, workspaceOwner.getName(), RUNNING);
    } catch (IllegalStateException ex) {
      // Remove try-catch block after issue has been resolved
      Assert.fail("Known issue https://github.com/eclipse/che/issues/8031");
    }
  }

  /** Gets workspace by its id. */
  public WorkspaceDto getById(String workspaceId) throws Exception {
    return requestFactory.fromUrl(getIdBasedUrl(workspaceId)).request().asDto(WorkspaceDto.class);
  }

  /**
   * Return server URL related with defined port
   *
   * @deprecated use {@link #getServerFromDevMachineBySymbolicName(String, String)} to retrieve
   *     server URL from instead
   */
  @Deprecated
  @Nullable
  public String getServerAddressByPort(String workspaceId, int port) throws Exception {
    Workspace workspace = getById(workspaceId);
    ensureRunningStatus(workspace);

    Map<String, ? extends Machine> machines = workspace.getRuntime().getMachines();
    for (Machine machine : machines.values()) {
      if (containsWsAgentServer(machine)) {
        return machine.getServers().get(valueOf(port) + "/tcp").getUrl();
      }
    }
    return null;
  }

  /**
   * Return ServerDto object from runtime by it's symbolic name
   *
   * @param workspaceId workspace id of current user
   * @param serverName server name
   * @return ServerDto object
   */
  @Nullable
  public Server getServerFromDevMachineBySymbolicName(String workspaceId, String serverName)
      throws Exception {
    Workspace workspace =
        requestFactory.fromUrl(getIdBasedUrl(workspaceId)).request().asDto(WorkspaceDto.class);

    ensureRunningStatus(workspace);

    Map<String, ? extends Machine> machines = workspace.getRuntime().getMachines();
    for (Machine machine : machines.values()) {
      if (containsWsAgentServer(machine)) {
        return machine.getServers().get(serverName);
      }
    }
    return null;
  }

  /**
   * Ensure workspace has running status, or throw IllegalStateException.
   *
   * @param workspace workspace description to get status and id.
   * @throws IllegalStateException if workspace with certain workspaceId doesn't have RUNNING
   *     status.
   */
  public void ensureRunningStatus(Workspace workspace) throws IllegalStateException {
    if (workspace.getStatus() != WorkspaceStatus.RUNNING) {
      throw new IllegalStateException(
          format(
              "Workspace with id='%s' should has '%s' status, but its actual state='%s'",
              workspace.getId(), WorkspaceStatus.RUNNING, workspace.getStatus()));
    }
  }

  private String getNameBasedUrl(String workspaceName, String username) {
    return getBaseUrl() + "/" + username + "/" + workspaceName;
  }

  private String getIdBasedUrl(String workspaceId) {
    return getBaseUrl() + "/" + workspaceId;
  }

  private long convertToByte(int numberOfMemValue, MemoryMeasure desiredMeasureMemory) {
    long calculatedValue = 0;
    // represents values of bytes in 1 megabyte (2x20)
    final long MEGABYTES_CONST = 1048576;

    // represents values of bytes in 1 gygabyte (2x30)
    final long GYGABYTES_CONST = 1073741824;

    switch (desiredMeasureMemory) {
      case MB:
        calculatedValue = numberOfMemValue * MEGABYTES_CONST;
        break;
      case GB:
        calculatedValue = numberOfMemValue * GYGABYTES_CONST;
        break;
    }
    return calculatedValue;
  }

  /**
   * Delete workspaces which could be created from factory
   *
   * @param originalName name workspace which was used to create factory
   */
  public void deleteFactoryWorkspaces(String originalName, String username) throws Exception {
    String workspace2delete = originalName;
    for (int i = 1; ; i++) {
      if (!exists(workspace2delete, username)) {
        break;
      }

      delete(workspace2delete, username);
      workspace2delete = originalName + "_" + i;
    }
  }
}
