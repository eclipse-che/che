/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.workspace;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.http.client.RequestBuilder.PUT;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newCallback;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newPromise;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

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
    private final AsyncRequestFactory    asyncRequestFactory;
    private final LoaderFactory          loaderFactory;
    private final String                 baseHttpUrl;

    @Inject
    private WorkspaceServiceClientImpl(@RestContext String restContext,
                                       DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                       AsyncRequestFactory asyncRequestFactory,
                                       LoaderFactory loaderFactory) {
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.loaderFactory = loaderFactory;
        this.baseHttpUrl = restContext + "/workspace";
    }

    @Override
    public Promise<WorkspaceDto> create(final WorkspaceConfigDto newWorkspace, final String accountId) {
        return newPromise(new RequestCall<WorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<WorkspaceDto> callback) {
                create(newWorkspace, accountId, callback);
            }
        });
    }

    private void create(@NotNull WorkspaceConfigDto newWorkspace,
                        String accountId,
                        @NotNull AsyncCallback<WorkspaceDto> callback) {
        String url = baseHttpUrl;
        if (accountId != null) {
            url += "?account=" + accountId;
        }
        asyncRequestFactory.createPostRequest(url, newWorkspace)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Creating workspace..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class)));
    }

    @Override
    public Promise<WorkspaceDto> getWorkspace(final String wsId) {
        return newPromise(new RequestCall<WorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<WorkspaceDto> callback) {
                getUsersWorkspace(wsId, callback);
            }
        });
    }

    private void getUsersWorkspace(@NotNull String wsId, @NotNull AsyncCallback<WorkspaceDto> callback) {
        final String url = baseHttpUrl + '/' + wsId;
        asyncRequestFactory.createGetRequest(url)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting info about workspace..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class)));
    }

    @Override
    public Promise<WorkspaceDto> getWorkspace(@NotNull final String namespace, @NotNull final String workspaceName){
        return newPromise(new RequestCall<WorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<WorkspaceDto> callback) {
                final String url = baseHttpUrl + '/' + namespace + ":" + workspaceName;
                asyncRequestFactory.createGetRequest(url)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .loader(loaderFactory.newLoader("Getting info about workspace..."))
                                   .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class)));
            }
        });
    }

    @Override
    public Promise<List<WorkspaceDto>> getWorkspaces(int skip, int limit) {
        return newPromise(new RequestCall<List<WorkspaceDto>>() {
            @Override
            public void makeCall(AsyncCallback<List<WorkspaceDto>> callback) {
                getWorkspaces(callback);
            }
        }).then(new Function<List<WorkspaceDto>, List<WorkspaceDto>>() {
            @Override
            public List<WorkspaceDto> apply(List<WorkspaceDto> arg) throws FunctionException {
                final List<WorkspaceDto> descriptors = new ArrayList<>();
                for (WorkspaceDto descriptor : arg) {
                    descriptors.add(descriptor);
                }
                return descriptors;
            }
        });
    }

    private void getWorkspaces(@NotNull AsyncCallback<List<WorkspaceDto>> callback) {
        final String url = baseHttpUrl;
        asyncRequestFactory.createGetRequest(url)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting info about workspaces..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newListUnmarshaller(WorkspaceDto.class)));
    }


    @Override
    public Promise<WorkspaceDto> update(String wsId, WorkspaceDto workspaceDto) {
        final String url = baseHttpUrl + '/' + wsId;
        return asyncRequestFactory.createPutRequest(url, workspaceDto)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
    }

    @Override
    public Promise<Void> delete(String wsId) {
        final String url = baseHttpUrl + '/' + wsId;
        return asyncRequestFactory.createDeleteRequest(url)
                                  .send();
    }

    @Override
    public Promise<WorkspaceDto> startFromConfig(final WorkspaceConfigDto cfg, final boolean isTemporary, final String accountId) {
        return newPromise(new RequestCall<WorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<WorkspaceDto> callback) {
                startWorkspace(cfg, accountId, callback);
            }
        });
    }

    private void startWorkspace(@NotNull WorkspaceConfigDto cfg,
                                @NotNull String accountId,
                                @NotNull AsyncCallback<WorkspaceDto> callback) {
        asyncRequestFactory.createPostRequest(baseHttpUrl + "/runtime", cfg)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Creating machine from recipe..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class)));
    }

    @Override
    public Promise<WorkspaceDto> startById(@NotNull final String id, final String envName, final boolean restore) {
        return newPromise(new RequestCall<WorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<WorkspaceDto> callback) {
                startById(id, envName, restore, callback);
            }
        });
    }

    private void startById(@NotNull String workspaceId,
                           @Nullable String envName,
                           boolean restore,
                           @NotNull AsyncCallback<WorkspaceDto> callback) {
        String url = baseHttpUrl + "/" + workspaceId + "/runtime?restore=" + restore;
        if (envName != null) {
            url += "&environment=" + envName;
        }
        asyncRequestFactory.createPostRequest(url, null)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Starting workspace..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class)));
    }

    @Override
    public Promise<Void> stop(String wsId) {
        final String url = baseHttpUrl + "/" + wsId + "/runtime";

        return newPromise(new RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {
                asyncRequestFactory.createDeleteRequest(url)
                                   .loader(loaderFactory.newLoader("Stopping workspace..."))
                                   .send(newCallback(callback));
            }
        });
    }

    @Override
    public Promise<List<CommandDto>> getCommands(String wsId) {
        return getWorkspace(wsId).then(new Function<WorkspaceDto, List<CommandDto>>() {
            @Override
            public List<CommandDto> apply(WorkspaceDto arg) throws FunctionException {
                return arg.getConfig().getCommands();
            }
        });
    }

    @Override
    public Promise<WorkspaceDto> addCommand(final String wsId, final CommandDto newCommand) {
        return newPromise(new RequestCall<WorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<WorkspaceDto> callback) {
                addCommand(wsId, newCommand, callback);
            }
        });
    }

    private void addCommand(@NotNull final String wsId,
                            @NotNull final CommandDto newCommand,
                            @NotNull AsyncCallback<WorkspaceDto> callback) {
        final String url = baseHttpUrl + '/' + wsId + "/command";
        asyncRequestFactory.createPostRequest(url, newCommand)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Adding command..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class)));
    }

    @Override
    public Promise<WorkspaceDto> updateCommand(final String wsId, final String commandName, final CommandDto commandUpdate) {
        return newPromise(new RequestCall<WorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<WorkspaceDto> callback) {
                updateCommand(wsId, commandUpdate, commandName, callback);
            }
        });
    }

    private void updateCommand(@NotNull final String wsId,
                               @NotNull final CommandDto commandUpdate,
                               final String commandName,
                               @NotNull AsyncCallback<WorkspaceDto> callback) {
        final String url = baseHttpUrl + '/' + wsId + "/command/" + URL.encodePathSegment(commandName);
        asyncRequestFactory.createRequest(PUT, url, commandUpdate, false)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Updating command..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class)));
    }

    @Override
    public Promise<WorkspaceDto> deleteCommand(final String wsId, final String commandName) {
        return newPromise(new RequestCall<WorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<WorkspaceDto> callback) {
                deleteCommand(wsId, commandName, callback);
            }
        });
    }

    private void deleteCommand(@NotNull final String wsId,
                               @NotNull final String commandName,
                               @NotNull AsyncCallback<WorkspaceDto> callback) {
        final String url = baseHttpUrl + '/' + wsId + "/command/" + URL.encodePathSegment(commandName);
        asyncRequestFactory.createDeleteRequest(url)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Deleting command..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class)));
    }

    @Override
    public Promise<WorkspaceDto> addEnvironment(String wsId, String envName, EnvironmentDto newEnv) {
        return asyncRequestFactory.createPostRequest(baseHttpUrl + '/' + wsId + "/environment?name=" + envName, newEnv)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Adding environment..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDto.class));
    }

    @Override
    public Promise<WorkspaceDto> updateEnvironment(String wsId, String envName, EnvironmentDto environmentUpdate) {
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
        return newPromise(new RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {
                createMachine(wsId, machineConfig, callback);
            }
        });
    }

    private void createMachine(@NotNull String wsId,
                               @NotNull MachineConfigDto newMachine,
                               @NotNull AsyncCallback<Void> callback) {
        String url = baseHttpUrl + '/' + wsId + "/machine";
        asyncRequestFactory.createPostRequest(url, newMachine)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Creating machine..."))
                           .send(newCallback(callback));
    }

    @Override
    public Promise<List<SnapshotDto>> getSnapshot(final String workspaceId) {
        return newPromise(new RequestCall<List<SnapshotDto>>() {
            @Override
            public void makeCall(AsyncCallback<List<SnapshotDto>> callback) {
                final String url = baseHttpUrl + '/' + workspaceId + "/snapshot";
                asyncRequestFactory.createGetRequest(url)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .loader(loaderFactory.newLoader("Getting workspace's snapshot"))
                                   .send(newCallback(callback, dtoUnmarshallerFactory.newListUnmarshaller(SnapshotDto.class)));
            }
        });
    }

    @Override
    public Promise<Void> createSnapshot(final String workspaceId) {
        return newPromise(new RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {
                final String url = baseHttpUrl + '/' + workspaceId + "/snapshot";
                asyncRequestFactory.createPostRequest(url, null)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .loader(loaderFactory.newLoader("Creating workspace's snapshot"))
                                   .send(newCallback(callback));
            }
        });
    }

    @Override
    public Promise<WsAgentHealthStateDto> getWsAgentState(String workspaceId) {
        return asyncRequestFactory.createGetRequest(baseHttpUrl + '/' + workspaceId + "/check")
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(WsAgentHealthStateDto.class));
    }
}
