/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.workspace;

import com.google.gwt.http.client.URL;
import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.CommandDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import static com.google.gwt.http.client.RequestBuilder.PUT;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

public class WorkspaceServiceClient {

    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final LoaderFactory          loaderFactory;
    private final String                 baseHttpUrl;

    @Inject
    private WorkspaceServiceClient(AppContext appContext,
                                   DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                   AsyncRequestFactory asyncRequestFactory,
                                   LoaderFactory loaderFactory) {
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.loaderFactory = loaderFactory;
        this.baseHttpUrl = appContext.getMasterEndpoint() + "/workspace";
    }

    /**
     * Creates new workspace.
     *
     * @param newWorkspace
     *         the configuration to create the new workspace
     * @param account
     *         the account id related to this operation
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#create(WorkspaceConfigDto, List, Boolean, String)
     */
    public Promise<WorkspaceDto> create(final WorkspaceConfigDto newWorkspace, final String accountId) {
        String url = baseHttpUrl;
        if (accountId != null) {
            url += "?account=" + accountId;
        }
        return asyncRequestFactory.createPostRequest(url, newWorkspace)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Creating workspace..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
    }

    /**
     * Gets users workspace by key.
     *
     * @param key
     *         composite key can be just workspace ID or in the namespace/workspace_name form
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#getByKey(String)
     */
    public Promise<WorkspaceDto> getWorkspace(final String key) {
        final String url = baseHttpUrl + '/' + key;
        return asyncRequestFactory.createGetRequest(url)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting info about workspace..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
    }

    /**
     * Gets workspace by namespace and name
     *
     * @param namespace
     *         namespace
     * @param workspaceName
     *         workspace name
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#getByKey(String)
     */
    public Promise<WorkspaceDto> getWorkspace(@NotNull final String namespace, @NotNull final String workspaceName) {
        final String url = baseHttpUrl + '/' + namespace + "/" + workspaceName;
        return asyncRequestFactory.createGetRequest(url)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting info about workspace..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
    }

    /**
     * Starts workspace based on workspace id and environment.
     *
     * @param id
     *         workspace ID
     * @param envName
     *         the name of the workspace environment that should be used for start
     * @param restore
     *         if <code>true</code> workspace will be restored from snapshot if snapshot exists,
     *         if <code>false</code> workspace will not be restored from snapshot
     *         even if auto-restore is enabled and snapshot exists
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     */
    public Promise<WorkspaceDto> startById(@NotNull final String id, final String envName, final Boolean restore) {
        String url = baseHttpUrl + "/" + id + "/runtime";
        if (restore != null) {
            url += "?restore=" + restore;
        }
        if (envName != null) {
            url += (url.contains("?") ? '&' : '?') + "environment=" + envName;
        }
        return asyncRequestFactory.createPostRequest(url, null)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Starting workspace..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
    }

    /**
     * Stops running workspace.
     *
     * @param wsId
     *         workspace ID
     * @return a promise that will resolve when the workspace has been stopped, or rejects with an error
     * @see WorkspaceService#stop(String, Boolean)
     */
    public Promise<Void> stop(String wsId) {
        final String url = baseHttpUrl + "/" + wsId + "/runtime";
        return asyncRequestFactory.createDeleteRequest(url)
                                  .loader(loaderFactory.newLoader("Stopping workspace..."))
                                  .send();
    }

    /**
     * Stops currently run runtime with ability to create snapshot.
     *
     * @param wsId
     *         workspace ID
     * @param createSnapshot
     *         create snapshot during the stop operation
     * @return a promise that will resolve when the workspace has been stopped, or rejects with an error
     */
    public Promise<Void> stop(String wsId, boolean createSnapshot) {
        final String url = baseHttpUrl + "/" + wsId + "/runtime?create-snapshot=" + createSnapshot;

        return asyncRequestFactory.createDeleteRequest(url)
                                  .loader(loaderFactory.newLoader("Stopping workspace..."))
                                  .send();
    }

    /**
     * Get all commands from the specified workspace.
     *
     * @param wsId
     *         workspace ID
     * @return a promise that will provide a list of {@link CommandDto}s, or rejects with an error
     */
    public Promise<List<CommandDto>> getCommands(String wsId) {
        return getWorkspace(wsId).then((Function<WorkspaceDto, List<CommandDto>>)workspaceDto -> workspaceDto.getConfig().getCommands());
    }

    /**
     * Adds command to workspace
     *
     * @param wsId
     *         workspace ID
     * @param newCommand
     *         the new workspace command
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#addCommand(String, CommandDto)
     */
    public Promise<WorkspaceDto> addCommand(final String wsId, final CommandDto newCommand) {
        final String url = baseHttpUrl + '/' + wsId + "/command";
        return asyncRequestFactory.createPostRequest(url, newCommand)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Adding command..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
    }

    /**
     * Updates command.
     *
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#updateCommand(String, String, CommandDto)
     */
    public Promise<WorkspaceDto> updateCommand(final String wsId, final String commandName, final CommandDto commandUpdate) {
        final String url = baseHttpUrl + '/' + wsId + "/command/" + URL.encodePathSegment(commandName);
        return asyncRequestFactory.createRequest(PUT, url, commandUpdate, false)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Updating command..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
    }

    /**
     * Removes command from workspace.
     *
     * @param wsId
     *         workspace ID
     * @param commandName
     *         the name of the command to remove
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#deleteCommand(String, String)
     */
    public Promise<WorkspaceDto> deleteCommand(final String wsId, final String commandName) {
        final String url = baseHttpUrl + '/' + wsId + "/command/" + URL.encodePathSegment(commandName);
        return asyncRequestFactory.createDeleteRequest(url)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Deleting command..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
    }

    /**
     * Get workspace related server configuration values defined in che.properties
     *
     * @see WorkspaceService#getSettings()
     */
    public Promise<Map<String, String>> getSettings() {
        return asyncRequestFactory.createGetRequest(baseHttpUrl + "/settings")
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .send(new StringMapUnmarshaller());
    }
}
