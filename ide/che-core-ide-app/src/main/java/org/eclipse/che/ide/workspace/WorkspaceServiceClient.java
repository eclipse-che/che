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
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.CommandDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

/** Client for Workspace API. */
public class WorkspaceServiceClient {

  private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
  private final DtoFactory dtoFactory;
  private final AsyncRequestFactory asyncRequestFactory;
  private final LoaderFactory loaderFactory;
  private final String baseHttpUrl;

  @Inject
  private WorkspaceServiceClient(
      AppContext appContext,
      DtoUnmarshallerFactory dtoUnmarshallerFactory,
      DtoFactory dtoFactory,
      AsyncRequestFactory asyncRequestFactory,
      LoaderFactory loaderFactory) {
    this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    this.dtoFactory = dtoFactory;
    this.asyncRequestFactory = asyncRequestFactory;
    this.loaderFactory = loaderFactory;
    this.baseHttpUrl = appContext.getMasterApiEndpoint() + "/workspace";
  }

  /**
   * Creates new workspace.
   *
   * @param newWorkspace the configuration to create the new workspace
   * @param account the account id related to this operation
   * @return a promise that resolves to the {@link WorkspaceImpl}, or rejects with an error
   */
  @Deprecated
  public Promise<WorkspaceImpl> create(
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
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class))
        .then((Function<WorkspaceDto, WorkspaceImpl>) WorkspaceImpl::new);
  }

  /**
   * Gets users workspace by key.
   *
   * @param key composite key can be just workspace ID or in the namespace/workspace_name form
   * @return a promise that resolves to the {@link WorkspaceImpl}, or rejects with an error
   */
  public Promise<WorkspaceImpl> getWorkspace(final String key) {
    final String url = baseHttpUrl + '/' + key;
    return asyncRequestFactory
        .createGetRequest(url)
        .header(ACCEPT, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Getting info about workspace..."))
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class))
        .then((Function<WorkspaceDto, WorkspaceImpl>) WorkspaceImpl::new);
  }

  /**
   * Gets workspace by namespace and name
   *
   * @param namespace namespace
   * @param workspaceName workspace name
   * @return a promise that resolves to the {@link WorkspaceImpl}, or rejects with an error
   */
  public Promise<WorkspaceImpl> getWorkspace(
      @NotNull final String namespace, @NotNull final String workspaceName) {
    final String url = baseHttpUrl + '/' + namespace + "/" + workspaceName;
    return asyncRequestFactory
        .createGetRequest(url)
        .header(ACCEPT, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Getting info about workspace..."))
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class))
        .then((Function<WorkspaceDto, WorkspaceImpl>) WorkspaceImpl::new);
  }

  /**
   * Starts workspace based on workspace id and environment.
   *
   * @param id workspace ID
   * @param envName the name of the workspace environment that should be used for start
   * @param restore if <code>true</code> workspace will be restored from snapshot if snapshot
   *     exists, if <code>false</code> workspace will not be restored from snapshot even if
   *     auto-restore is enabled and snapshot exists
   * @return a promise that resolves to the {@link WorkspaceImpl}, or rejects with an error
   */
  public Promise<WorkspaceImpl> startById(
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
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class))
        .then((Function<WorkspaceDto, WorkspaceImpl>) WorkspaceImpl::new);
  }

  /**
   * Stops running workspace.
   *
   * @param wsId workspace ID
   * @return a promise that will resolve when the workspace has been stopped, or rejects with an
   *     error
   */
  public Promise<Void> stop(String wsId) {
    final String url = baseHttpUrl + "/" + wsId + "/runtime";
    return asyncRequestFactory
        .createDeleteRequest(url)
        .loader(loaderFactory.newLoader("Stopping workspace..."))
        .send();
  }

  /**
   * Stops currently run runtime with ability to create snapshot.
   *
   * @param wsId workspace ID
   * @param createSnapshot create snapshot during the stop operation
   * @return a promise that will resolve when the workspace has been stopped, or rejects with an
   *     error
   */
  public Promise<Void> stop(String wsId, boolean createSnapshot) {
    final String url = baseHttpUrl + "/" + wsId + "/runtime?create-snapshot=" + createSnapshot;

    return asyncRequestFactory
        .createDeleteRequest(url)
        .loader(loaderFactory.newLoader("Stopping workspace..."))
        .send();
  }

  /**
   * Adds command to workspace
   *
   * @param wsId workspace ID
   * @param newCommand the new workspace command
   * @return a promise that resolves to the {@link WorkspaceImpl}, or rejects with an error
   */
  public Promise<WorkspaceImpl> addCommand(final String wsId, final CommandImpl newCommand) {
    final String url = baseHttpUrl + '/' + wsId + "/command";

    final CommandDto commandDto =
        dtoFactory
            .createDto(CommandDto.class)
            .withName(newCommand.getName())
            .withCommandLine(newCommand.getCommandLine())
            .withType(newCommand.getType())
            .withAttributes(newCommand.getAttributes());

    return asyncRequestFactory
        .createPostRequest(url, commandDto)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Adding command..."))
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class))
        .then((Function<WorkspaceDto, WorkspaceImpl>) WorkspaceImpl::new);
  }

  /**
   * Updates command.
   *
   * @return a promise that resolves to the {@link WorkspaceImpl}, or rejects with an error
   */
  public Promise<WorkspaceImpl> updateCommand(
      final String wsId, final String commandName, final CommandImpl commandUpdate) {
    final String url = baseHttpUrl + '/' + wsId + "/command/" + URL.encodePathSegment(commandName);

    final CommandDto commandDto =
        dtoFactory
            .createDto(CommandDto.class)
            .withName(commandUpdate.getName())
            .withCommandLine(commandUpdate.getCommandLine())
            .withType(commandUpdate.getType())
            .withAttributes(commandUpdate.getAttributes());

    return asyncRequestFactory
        .createRequest(PUT, url, commandDto, false)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Updating command..."))
        .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class))
        .then((Function<WorkspaceDto, WorkspaceImpl>) WorkspaceImpl::new);
  }

  /**
   * Removes command from workspace.
   *
   * @param wsId workspace ID
   * @param commandName the name of the command to remove
   * @return a promise that will resolve when the command has been stopped, or rejects with an error
   */
  public Promise<Void> deleteCommand(final String wsId, final String commandName) {
    final String url = baseHttpUrl + '/' + wsId + "/command/" + URL.encodePathSegment(commandName);
    return asyncRequestFactory
        .createDeleteRequest(url)
        .loader(loaderFactory.newLoader("Deleting command..."))
        .send();
  }

  /** Get workspace related server configuration values defined in che.properties */
  public Promise<Map<String, String>> getSettings() {
    return asyncRequestFactory
        .createGetRequest(baseHttpUrl + "/settings")
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .send(new StringMapUnmarshaller());
  }
}
