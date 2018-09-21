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
package org.eclipse.che.selenium.core.client;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.api.workspace.server.WsAgentMachineFinderUtil.containsWsAgentServer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactoryCreator;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.MemoryMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Musienko Maxim
 * @author Dmytro Nochevnov
 */
public abstract class AbstractTestWorkspaceServiceClient implements TestWorkspaceServiceClient {

  private static final Logger LOG =
      LoggerFactory.getLogger(AbstractTestWorkspaceServiceClient.class);

  protected final TestApiEndpointUrlProvider apiEndpointProvider;
  protected final HttpJsonRequestFactory requestFactory;

  public AbstractTestWorkspaceServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider, HttpJsonRequestFactory requestFactory) {
    this.apiEndpointProvider = apiEndpointProvider;
    this.requestFactory = requestFactory;
  }

  public AbstractTestWorkspaceServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider,
      TestUserHttpJsonRequestFactoryCreator userHttpJsonRequestFactoryCreator,
      TestUser testUser) {
    this(apiEndpointProvider, userHttpJsonRequestFactoryCreator.create(testUser));
  }

  protected String getBaseUrl() {
    return apiEndpointProvider.get() + "workspace";
  }

  /** Returns the list of workspaces names that belongs to the user. */
  @Override
  public List<String> getAll() throws Exception {
    List<WorkspaceDto> workspaces =
        requestFactory.fromUrl(getBaseUrl()).request().asList(WorkspaceDto.class);
    return workspaces.stream().map(ws -> ws.getConfig().getName()).collect(Collectors.toList());
  }

  /** Stops workspace. */
  @Override
  public void stop(String workspaceName, String userName) throws Exception {
    sendStopRequest(workspaceName, userName);
    waitStatus(workspaceName, userName, STOPPED);
  }

  /** Returns workspace of default user by its name. */
  @Override
  public Workspace getByName(String workspace, String username) throws Exception {
    return requestFactory
        .fromUrl(getNameBasedUrl(workspace, username))
        .request()
        .asDto(WorkspaceDto.class);
  }

  /** Indicates if workspace exists. */
  @Override
  public boolean exists(String workspace, String username) throws Exception {
    try {
      requestFactory.fromUrl(getNameBasedUrl(workspace, username)).request();
    } catch (NotFoundException e) {
      return false;
    }

    return true;
  }

  /** Deletes workspace of default user. */
  @Override
  public void delete(String workspaceName, String userName) throws Exception {
    if (!exists(workspaceName, userName)) {
      return;
    }

    Workspace workspace = getByName(workspaceName, userName);
    if (workspace.getStatus() == STOPPING) {
      waitStatus(workspaceName, userName, STOPPED);
    } else if (workspace.getStatus() != STOPPED) {
      stop(workspaceName, userName);
    }

    requestFactory.fromUrl(getIdBasedUrl(workspace.getId())).useDeleteMethod().request();

    LOG.info(
        "Workspace name='{}', id='{}', username='{}' removed",
        workspaceName,
        workspace.getId(),
        userName);
  }

  /** Waits workspace is started. */
  @Override
  public void waitWorkspaceStart(String workspaceName, String userName) throws Exception {
    WaitUtils.sleepQuietly(5); // delay 5 secs to obtain starting status for sure
    WaitUtils.waitSuccessCondition(
        () -> {
          WorkspaceStatus status;
          try {
            status = getByName(workspaceName, userName).getStatus();
          } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
          }

          switch (status) {
            case RUNNING:
              return true;

            case STARTING:
              return false;

            default:
              throw new RuntimeException(
                  format("Workspace with name '%s' didn't start", workspaceName));
          }
        },
        600,
        1000,
        TimeUnit.SECONDS);
  }

  /** Waits needed status. */
  @Override
  public void waitStatus(String workspaceName, String userName, WorkspaceStatus expectedStatus)
      throws Exception {
    WaitUtils.waitSuccessCondition(
        () -> {
          try {
            if (getByName(workspaceName, userName).getStatus() == expectedStatus) {
              return true;
            }
          } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
          }

          return false;
        },
        600,
        1000,
        TimeUnit.SECONDS);
  }

  /** Sends start workspace request. */
  @Override
  public void sendStartRequest(String workspaceId, String workspaceName) throws Exception {
    requestFactory
        .fromUrl(getIdBasedUrl(workspaceId) + "/runtime")
        .addQueryParam("environment", workspaceName)
        .usePostMethod()
        .request();
  }

  /** Gets workspace by its id. */
  @Override
  public WorkspaceDto getById(String workspaceId) throws Exception {
    return requestFactory.fromUrl(getIdBasedUrl(workspaceId)).request().asDto(WorkspaceDto.class);
  }

  /** Gets workspace status by id. */
  @Override
  public WorkspaceStatus getStatus(String workspaceId) throws Exception {
    return getById(workspaceId).getStatus();
  }

  /**
   * Return server URL related with defined port
   *
   * @deprecated use {@link #getServerFromDevMachineBySymbolicName(String, String)} to retrieve
   *     server URL from instead
   */
  @Override
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
  @Override
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
  @Override
  public void ensureRunningStatus(Workspace workspace) throws IllegalStateException {
    if (workspace.getStatus() != WorkspaceStatus.RUNNING) {
      throw new IllegalStateException(
          format(
              "Workspace with id='%s' should has '%s' status, but its actual state='%s'",
              workspace.getId(), WorkspaceStatus.RUNNING, workspace.getStatus()));
    }
  }

  /**
   * Delete workspaces which could be created from factory
   *
   * @param originalName name workspace which was used to create factory
   */
  @Override
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

  // ================= //
  //  PRIVATE METHODS  //
  // ================= //

  /** Sends stop workspace request. */
  private void sendStopRequest(String workspaceName, String userName) throws Exception {
    if (!exists(workspaceName, userName)) {
      return;
    }

    Workspace workspace = getByName(workspaceName, userName);
    String apiUrl = getIdBasedUrl(workspace.getId()) + "/runtime/";

    requestFactory.fromUrl(apiUrl).useDeleteMethod().request();
  }

  protected String getNameBasedUrl(String workspaceName, String username) {
    return getBaseUrl() + "/" + username + "/" + workspaceName;
  }

  protected String getIdBasedUrl(String workspaceId) {
    return getBaseUrl() + "/" + workspaceId;
  }

  protected long convertToByte(int numberOfMemValue, MemoryMeasure desiredMeasureMemory) {
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
}
