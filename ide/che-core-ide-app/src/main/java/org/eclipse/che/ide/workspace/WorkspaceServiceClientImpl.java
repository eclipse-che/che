/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.workspace;

import static com.google.gwt.http.client.RequestBuilder.PUT;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

/**
 * Implementation for {@link WorkspaceServiceClient}.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 * @author Igor Vinokur
 */
public class WorkspaceServiceClientImpl implements WorkspaceServiceClient {

  private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
  private final AsyncRequestFactory asyncRequestFactory;
  private final LoaderFactory loaderFactory;
  private final String baseHttpUrl;

  @Inject
  private WorkspaceServiceClientImpl(
      AppContext appContext,
      DtoUnmarshallerFactory dtoUnmarshallerFactory,
      AsyncRequestFactory asyncRequestFactory,
      LoaderFactory loaderFactory) {
    this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    this.asyncRequestFactory = asyncRequestFactory;
    this.loaderFactory = loaderFactory;
    this.baseHttpUrl = appContext.getMasterEndpoint() + "/workspace";
  }

  @Override
  public Promise<WorkspaceDto> create(
      final WorkspaceConfigDto newWorkspace, final String accountId) {
    String url = baseHttpUrl;
    if (accountId != null) {
      url += "?account=" + accountId;
    }
    return asyncRequestFactory
        .createPostRequest(url, newWorkspace)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Creating workspace..."))
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
  }

  @Override
  public Promise<WorkspaceDto> getWorkspace(final String key) {
    final String url = baseHttpUrl + '/' + key;
    return asyncRequestFactory
        .createGetRequest(url)
        .header(ACCEPT, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Getting info about workspace..."))
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
  }

  @Override
  public Promise<WorkspaceDto> getWorkspace(
      @NotNull final String namespace, @NotNull final String workspaceName) {
    final String url = baseHttpUrl + '/' + namespace + "/" + workspaceName;
    return asyncRequestFactory
        .createGetRequest(url)
        .header(ACCEPT, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Getting info about workspace..."))
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
  }

  @Override
  public Promise<List<WorkspaceDto>> getWorkspaces(final int skip, final int limit) {
    return fetchWorkspaces()
        .then(
            (Function<List<WorkspaceDto>, List<WorkspaceDto>>)
                workspaceDtoList -> workspaceDtoList.stream().collect(toList()));
  }

  private Promise<List<WorkspaceDto>> fetchWorkspaces() {
    return asyncRequestFactory
        .createGetRequest(baseHttpUrl)
        .header(ACCEPT, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Getting info about workspaces..."))
        .send(dtoUnmarshallerFactory.newListUnmarshaller(WorkspaceDto.class));
  }

  @Override
  public Promise<WorkspaceDto> update(String wsId, WorkspaceDto workspaceDto) {
    final String url = baseHttpUrl + '/' + wsId;
    return asyncRequestFactory
        .createPutRequest(url, workspaceDto)
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
  }

  @Override
  public Promise<Void> delete(String wsId) {
    final String url = baseHttpUrl + '/' + wsId;
    return asyncRequestFactory.createDeleteRequest(url).send();
  }

  @Override
  public Promise<WorkspaceDto> startFromConfig(
      final WorkspaceConfigDto cfg, final boolean isTemporary, final String accountId) {
    return asyncRequestFactory
        .createPostRequest(baseHttpUrl + "/runtime", cfg)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Creating machine from recipe..."))
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
  }

  @Override
  public Promise<WorkspaceDto> startById(
      @NotNull final String id, final String envName, final Boolean restore) {
    String url = baseHttpUrl + "/" + id + "/runtime";
    if (restore != null) {
      url += "?restore=" + restore;
    }
    if (envName != null) {
      url += (url.contains("?") ? '&' : '?') + "environment=" + envName;
    }
    return asyncRequestFactory
        .createPostRequest(url, null)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Starting workspace..."))
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
  }

  @Override
  public Promise<Void> stop(String wsId) {
    final String url = baseHttpUrl + "/" + wsId + "/runtime";
    return asyncRequestFactory
        .createDeleteRequest(url)
        .loader(loaderFactory.newLoader("Stopping workspace..."))
        .send();
  }

  @Override
  public Promise<Void> stop(String wsId, boolean createSnapshot) {
    final String url = baseHttpUrl + "/" + wsId + "/runtime?create-snapshot=" + createSnapshot;

    return asyncRequestFactory
        .createDeleteRequest(url)
        .loader(loaderFactory.newLoader("Stopping workspace..."))
        .send();
  }

  @Override
  public Promise<List<CommandDto>> getCommands(String wsId) {
    return getWorkspace(wsId)
        .then(
            (Function<WorkspaceDto, List<CommandDto>>)
                workspaceDto -> workspaceDto.getConfig().getCommands());
  }

  @Override
  public Promise<WorkspaceDto> addCommand(final String wsId, final CommandDto newCommand) {
    final String url = baseHttpUrl + '/' + wsId + "/command";
    return asyncRequestFactory
        .createPostRequest(url, newCommand)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Adding command..."))
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
  }

  @Override
  public Promise<WorkspaceDto> updateCommand(
      final String wsId, final String commandName, final CommandDto commandUpdate) {
    final String url = baseHttpUrl + '/' + wsId + "/command/" + URL.encodePathSegment(commandName);
    return asyncRequestFactory
        .createRequest(PUT, url, commandUpdate, false)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Updating command..."))
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
  }

  @Override
  public Promise<WorkspaceDto> deleteCommand(final String wsId, final String commandName) {
    final String url = baseHttpUrl + '/' + wsId + "/command/" + URL.encodePathSegment(commandName);
    return asyncRequestFactory
        .createDeleteRequest(url)
        .header(ACCEPT, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Deleting command..."))
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
  }

  @Override
  public Promise<WorkspaceDto> addEnvironment(String wsId, String envName, EnvironmentDto newEnv) {
    return asyncRequestFactory
        .createPostRequest(baseHttpUrl + '/' + wsId + "/environment?name=" + envName, newEnv)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Adding environment..."))
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
  }

  @Override
  public Promise<WorkspaceDto> updateEnvironment(
      String wsId, String envName, EnvironmentDto environmentUpdate) {
    return null;
  }

  @Override
  public Promise<WorkspaceDto> deleteEnvironment(String wsId, String envName) {
    return null;
  }

  @Override
  public Promise<WorkspaceDto> addProject(String wsId, ProjectConfigDto newProject) {
    return null;
  }

  @Override
  public Promise<WorkspaceDto> updateProject(String wsId, String path, ProjectConfigDto newEnv) {
    return null;
  }

  @Override
  public Promise<WorkspaceDto> deleteProject(String wsId, String projectName) {
    return null;
  }

  @Override
  public Promise<Void> createMachine(final String wsId, final MachineConfigDto machineConfig) {
    String url = baseHttpUrl + '/' + wsId + "/machine";
    return asyncRequestFactory
        .createPostRequest(url, machineConfig)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Creating machine..."))
        .send();
  }

  @Override
  public Promise<List<SnapshotDto>> getSnapshot(final String workspaceId) {
    final String url = baseHttpUrl + '/' + workspaceId + "/snapshot";
    return asyncRequestFactory
        .createGetRequest(url)
        .header(ACCEPT, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Getting workspace's snapshot"))
        .send(dtoUnmarshallerFactory.newListUnmarshaller(SnapshotDto.class));
  }

  @Override
  public Promise<Void> createSnapshot(final String workspaceId) {
    final String url = baseHttpUrl + '/' + workspaceId + "/snapshot";
    return asyncRequestFactory
        .createPostRequest(url, null)
        .header(ACCEPT, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Creating workspace's snapshot"))
        .send();
  }

  @Override
  public Promise<WsAgentHealthStateDto> getWsAgentState(String workspaceId) {
    return asyncRequestFactory
        .createGetRequest(baseHttpUrl + '/' + workspaceId + "/check")
        .header(ACCEPT, APPLICATION_JSON)
        .send(dtoUnmarshallerFactory.newUnmarshaller(WsAgentHealthStateDto.class));
  }

  @Override
  public Promise<Map<String, String>> getSettings() {
    return asyncRequestFactory
        .createGetRequest(baseHttpUrl + "/settings") //
        .header(ACCEPT, APPLICATION_JSON) //
        .header(CONTENT_TYPE, APPLICATION_JSON) //
        .send(new StringMapUnmarshaller());
  }
}
