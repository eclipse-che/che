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
package org.eclipse.che.api.workspace.gwt.client;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RuntimeWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
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
    public Promise<UsersWorkspaceDto> create(final WorkspaceConfigDto newWorkspace, final String accountId) {
        return newPromise(new RequestCall<UsersWorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<UsersWorkspaceDto> callback) {
                create(newWorkspace, accountId, callback);
            }
        });
    }

    private void create(@NotNull WorkspaceConfigDto newWorkspace,
                        String accountId,
                        @NotNull AsyncCallback<UsersWorkspaceDto> callback) {
        String url = baseHttpUrl;
        if (accountId != null) {
            url += "?account=" + accountId;
        }
        asyncRequestFactory.createPostRequest(url, newWorkspace)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Creating workspace..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(UsersWorkspaceDto.class)));
    }

    @Override
    public Promise<UsersWorkspaceDto> getUsersWorkspace(final String wsId) {
        return newPromise(new RequestCall<UsersWorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<UsersWorkspaceDto> callback) {
                getUsersWorkspace(wsId, callback);
            }
        });
    }

    private void getUsersWorkspace(@NotNull String wsId, @NotNull AsyncCallback<UsersWorkspaceDto> callback) {
        final String url = baseHttpUrl + '/' + wsId;
        asyncRequestFactory.createGetRequest(url)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting info about workspace..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(UsersWorkspaceDto.class)));
    }

    @Override
    public Promise<RuntimeWorkspaceDto> getRuntimeWorkspace(String wsId) {
        return null;
    }

    @Override
    public Promise<List<UsersWorkspaceDto>> getWorkspaces(int skip, int limit) {
        return newPromise(new RequestCall<List<UsersWorkspaceDto>>() {
            @Override
            public void makeCall(AsyncCallback<List<UsersWorkspaceDto>> callback) {
                getWorkspaces(callback);
            }
        }).then(new Function<List<UsersWorkspaceDto>, List<UsersWorkspaceDto>>() {
            @Override
            public List<UsersWorkspaceDto> apply(List<UsersWorkspaceDto> arg) throws FunctionException {
                final List<UsersWorkspaceDto> descriptors = new ArrayList<>();
                for (UsersWorkspaceDto descriptor : arg) {
                    descriptors.add(descriptor);
                }
                return descriptors;
            }
        });
    }

    private void getWorkspaces(@NotNull AsyncCallback<List<UsersWorkspaceDto>> callback) {
        final String url = baseHttpUrl;
        asyncRequestFactory.createGetRequest(url)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting info about workspaces..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newListUnmarshaller(UsersWorkspaceDto.class)));
    }

    @Override
    public Promise<List<RuntimeWorkspaceDto>> getRuntimeWorkspaces(int skip, int limit) {
        return null;
    }

    @Override
    public Promise<UsersWorkspaceDto> update(String wsId, WorkspaceConfigDto newCfg) {
        final String url = baseHttpUrl + '/' + wsId;
        return asyncRequestFactory.createRequest(RequestBuilder.PUT, url, newCfg, true)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(UsersWorkspaceDto.class));
    }

    @Override
    public Promise<Void> delete(String wsId) {
        final String url = baseHttpUrl + '/' + wsId;
        return asyncRequestFactory.createDeleteRequest(url)
                                  .send();
    }

    @Override
    public Promise<RuntimeWorkspaceDto> startTemporary(final WorkspaceConfigDto cfg, final String accountId) {
        return newPromise(new RequestCall<RuntimeWorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<RuntimeWorkspaceDto> callback) {
                startTemporary(cfg, accountId, callback);
            }
        });
    }

    private void startTemporary(@NotNull WorkspaceConfigDto cfg,
                                @NotNull String accountId,
                                @NotNull AsyncCallback<RuntimeWorkspaceDto> callback) {
        asyncRequestFactory.createPostRequest(baseHttpUrl + "/runtime", cfg)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Creating machine from recipe..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(RuntimeWorkspaceDto.class)));
    }

    @Override
    public Promise<UsersWorkspaceDto> startById(@NotNull final String id, final String envName) {
        return newPromise(new RequestCall<UsersWorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<UsersWorkspaceDto> callback) {
                startById(id, envName, callback);
            }
        });
    }

    private void startById(@NotNull String workspaceId,
                           @Nullable String envName,
                           @NotNull AsyncCallback<UsersWorkspaceDto> callback) {
        String url = baseHttpUrl + "/" + workspaceId + "/runtime";
        if (envName != null) {
            url += "?environment=" + envName;
        }
        asyncRequestFactory.createPostRequest(url, null)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Starting workspace..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(UsersWorkspaceDto.class)));
    }

    @Override
    public Promise<UsersWorkspaceDto> startByName(String name, String envName) {
        return null;
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
        return getUsersWorkspace(wsId).then(new Function<UsersWorkspaceDto, List<CommandDto>>() {
            @Override
            public List<CommandDto> apply(UsersWorkspaceDto arg) throws FunctionException {
                return arg.getConfig().getCommands();
            }
        });
    }

    @Override
    public Promise<UsersWorkspaceDto> addCommand(final String wsId, final CommandDto newCommand) {
        return newPromise(new RequestCall<UsersWorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<UsersWorkspaceDto> callback) {
                addCommand(wsId, newCommand, callback);
            }
        });
    }

    private void addCommand(@NotNull final String wsId,
                            @NotNull final CommandDto newCommand,
                            @NotNull AsyncCallback<UsersWorkspaceDto> callback) {
        final String url = baseHttpUrl + '/' + wsId + "/command";
        asyncRequestFactory.createPostRequest(url, newCommand)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Adding command..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(UsersWorkspaceDto.class)));
    }

    @Override
    public Promise<UsersWorkspaceDto> updateCommand(final String wsId, final CommandDto commandUpdate) {
        return newPromise(new RequestCall<UsersWorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<UsersWorkspaceDto> callback) {
                updateCommand(wsId, commandUpdate, callback);
            }
        });
    }

    private void updateCommand(@NotNull final String wsId,
                               @NotNull final CommandDto commandUpdate,
                               @NotNull AsyncCallback<UsersWorkspaceDto> callback) {
        final String url = baseHttpUrl + '/' + wsId + "/command";
        asyncRequestFactory.createRequest(PUT, url, commandUpdate, false)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Updating command..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(UsersWorkspaceDto.class)));
    }

    @Override
    public Promise<UsersWorkspaceDto> deleteCommand(final String wsId, final String commandName) {
        return newPromise(new RequestCall<UsersWorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<UsersWorkspaceDto> callback) {
                deleteCommand(wsId, commandName, callback);
            }
        });
    }

    private void deleteCommand(@NotNull final String wsId,
                               @NotNull final String commandName,
                               @NotNull AsyncCallback<UsersWorkspaceDto> callback) {
        final String url = baseHttpUrl + '/' + wsId + "/command/" + commandName;
        asyncRequestFactory.createDeleteRequest(url)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Deleting command..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(UsersWorkspaceDto.class)));
    }

    @Override
    public Promise<UsersWorkspaceDto> addEnvironment(String wsId, EnvironmentDto newEnv) {
        return null;
    }

    @Override
    public Promise<UsersWorkspaceDto> updateEnvironment(String wsId, EnvironmentDto environmentUpdate) {
        return null;
    }

    @Override
    public Promise<UsersWorkspaceDto> deleteEnvironment(String wsId, String envName) {
        return null;
    }

    @Override
    public Promise<UsersWorkspaceDto> addProject(String wsId, ProjectConfigDto newProject) {
        return null;
    }

    @Override
    public Promise<UsersWorkspaceDto> updateProject(String wsId, ProjectConfigDto newEnv) {
        return null;
    }

    @Override
    public Promise<UsersWorkspaceDto> deleteProject(String wsId, String projectName) {
        return null;
    }

    @Override
    public Promise<MachineDto> createMachine(final String wsId, final MachineConfigDto machineConfig) {
        return newPromise(new RequestCall<MachineDto>() {
            @Override
            public void makeCall(AsyncCallback<MachineDto> callback) {
                createMachine(wsId, machineConfig, callback);
            }
        });
    }

    private void createMachine(@NotNull String wsId,
                               @NotNull MachineConfigDto newMachine,
                               @NotNull AsyncCallback<MachineDto> callback) {
        String url = baseHttpUrl + '/' + wsId + "/machine";
        asyncRequestFactory.createPostRequest(url, newMachine)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Creating machine..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(MachineDto.class)));
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
    public Promise<UsersWorkspaceDto> recoverWorkspace(final String workspaceId, final String envName, final String accountId) {
        return newPromise(new RequestCall<UsersWorkspaceDto>() {
            @Override
            public void makeCall(AsyncCallback<UsersWorkspaceDto> callback) {
                final String url = baseHttpUrl + '/' + workspaceId + "/runtime/snapshot?environment=" + envName;
                asyncRequestFactory.createPostRequest(url, null)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .loader(loaderFactory.newLoader("Recovering workspace from snapshot"))
                                   .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(UsersWorkspaceDto.class)));
            }
        });
    }
}
