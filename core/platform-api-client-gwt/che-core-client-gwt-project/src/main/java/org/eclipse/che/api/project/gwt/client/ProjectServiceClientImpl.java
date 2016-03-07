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
package org.eclipse.che.api.project.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.api.machine.gwt.client.WsAgentStateController;
import org.eclipse.che.api.project.shared.dto.CopyOptions;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.MoveOptions;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.promises.client.callback.PromiseHelper;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.google.gwt.http.client.RequestBuilder.DELETE;
import static com.google.gwt.http.client.RequestBuilder.POST;
import static com.google.gwt.http.client.RequestBuilder.PUT;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newCallback;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newPromise;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENTTYPE;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * Implementation of {@link ProjectServiceClient}.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 */
public class ProjectServiceClientImpl implements ProjectServiceClient {
    private final WsAgentStateController wsAgentStateController;
    private final LoaderFactory          loaderFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final DtoFactory             dtoFactory;
    private final DtoUnmarshallerFactory dtoUnmarshaller;

    private final String extPath;

    @Inject
    protected ProjectServiceClientImpl(WsAgentStateController wsAgentStateController,
                                       LoaderFactory loaderFactory,
                                       AsyncRequestFactory asyncRequestFactory,
                                       DtoFactory dtoFactory,
                                       DtoUnmarshallerFactory dtoUnmarshaller,
                                       @Named("cheExtensionPath") String extPath) {
        this.extPath = extPath;
        this.wsAgentStateController = wsAgentStateController;
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoFactory = dtoFactory;
        this.dtoUnmarshaller = dtoUnmarshaller;
    }

    @Override
    public void getProjects(String workspaceId, boolean includeAttributes, AsyncRequestCallback<List<ProjectConfigDto>> callback) {
        asyncRequestFactory.createGetRequest(extPath + "/project/" + workspaceId + "?includeAttributes=" + includeAttributes)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting projects..."))
                           .send(callback);
    }

    @Override
    public Promise<List<ProjectConfigDto>> getProjects(final String workspaceId, boolean includeAttributes) {
        return newPromise(new AsyncPromiseHelper.RequestCall<List<ProjectConfigDto>>() {
            @Override
            public void makeCall(AsyncCallback<List<ProjectConfigDto>> callback) {
                getProjects(workspaceId, false, newCallback(callback, dtoUnmarshaller.newListUnmarshaller(ProjectConfigDto.class)));
            }
        });
    }

    @Override
    public void getProjectsInSpecificWorkspace(String wsId, AsyncRequestCallback<List<ProjectConfigDto>> callback) {
        final String requestUrl = extPath + "/project/" + wsId;
        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting projects..."))
                           .send(callback);
    }

    @Override
    public void cloneProjectToCurrentWorkspace(String workspaceId,
                                               String srcProjectPath,
                                               String newNameForProject,
                                               AsyncRequestCallback<String> callback) {
        final String requestUrl = extPath + "/vfs/" + workspaceId + "/v2/clone" + "?srcVfsId=" + workspaceId +
                                  "&srcPath=" + srcProjectPath +
                                  "&parentPath=/" +
                                  "&name=" + newNameForProject;

        asyncRequestFactory.createPostRequest(requestUrl, null)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Copying project..."))
                           .send(callback);
    }

    @Override
    public void getProject(String workspaceId, String path, AsyncRequestCallback<ProjectConfigDto> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + normalizePath(path);
        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting project..."))
                           .send(callback);
    }

    @Override
    public Promise<ProjectConfigDto> getProject(String workspaceId, String path) {
        final String requestUrl = extPath + "/project/" + workspaceId + normalizePath(path);
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .header(ACCEPT, MimeType.APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting project..."))
                                  .send(dtoUnmarshaller.newUnmarshaller(ProjectConfigDto.class));
    }

    @Override
    public void getItem(String workspaceId, String path, AsyncRequestCallback<ItemReference> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/item" + normalizePath(path);
        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting item..."))
                           .send(callback);
    }

    @Override
    public void createProject(String workspaceId,
                              String name,
                              ProjectConfigDto projectConfig,
                              AsyncRequestCallback<ProjectConfigDto> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "?name=" + name;
        asyncRequestFactory.createPostRequest(requestUrl, projectConfig)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Creating project..."))
                           .send(callback);
    }

    @Override
    public void estimateProject(String workspaceId,
                                String path,
                                String projectType,
                                AsyncRequestCallback<SourceEstimation> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/estimate" + normalizePath(path) + "?type=" + projectType;
        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Estimating project..."))
                           .send(callback);
    }

    @Override
    public void resolveSources(String workspaceId, String path, AsyncRequestCallback<List<SourceEstimation>> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/resolve" + normalizePath(path);
        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Resolving sources..."))
                           .send(callback);
    }

    @Override
    public Promise<List<SourceEstimation>> resolveSources(String workspaceId, String path) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/resolve" + normalizePath(path);
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .header(ACCEPT, MimeType.APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Resolving sources..."))
                                  .send(dtoUnmarshaller.newListUnmarshaller(SourceEstimation.class));
    }


    @Override
    public void getModules(String workspaceId, String path, AsyncRequestCallback<List<ProjectConfigDto>> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/modules" + normalizePath(path);
        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting modules..."))
                           .send(callback);
    }

    @Override
    public void createModule(String workspaceId,
                             String parentProjectPath,
                             ProjectConfigDto projectConfig,
                             AsyncRequestCallback<ProjectConfigDto> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + normalizePath(parentProjectPath);
        asyncRequestFactory.createPostRequest(requestUrl, projectConfig)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Creating module..."))
                           .send(callback);
    }

    @Override
    public void updateProject(String workspaceId,
                              String path,
                              ProjectConfigDto projectConfig,
                              AsyncRequestCallback<ProjectConfigDto> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + normalizePath(path);
        asyncRequestFactory.createRequest(PUT, requestUrl, projectConfig, false)
                           .header(CONTENT_TYPE, MimeType.APPLICATION_JSON)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Updating project..."))
                           .send(callback);
    }

    @Override
    public Promise<ProjectConfigDto> updateProject(String workspaceId, String path, ProjectConfigDto projectConfig) {
        final String requestUrl = extPath + "/project/" + workspaceId + normalizePath(path);
        return asyncRequestFactory.createRequest(PUT, requestUrl, projectConfig, false)
                           .header(CONTENT_TYPE, MimeType.APPLICATION_JSON)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Updating project..."))
                           .send(dtoUnmarshaller.newUnmarshaller(ProjectConfigDto.class));
    }

    @Override
    public void createFile(String workspaceId,
                           String parentPath,
                           String name,
                           String content,
                           AsyncRequestCallback<ItemReference> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/file" + normalizePath(parentPath) + "?name=" + name;
        asyncRequestFactory.createPostRequest(requestUrl, null)
                           .data(content)
                           .loader(loaderFactory.newLoader("Creating file..."))
                           .send(callback);
    }

    @Override
    public void getFileContent(String workspaceId, String path, AsyncRequestCallback<String> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/file" + normalizePath(path);
        asyncRequestFactory.createGetRequest(requestUrl)
                           .loader(loaderFactory.newLoader("Loading file content..."))
                           .send(callback);
    }

    @Override
    public void updateFile(String workspaceId, String path, String content, AsyncRequestCallback<Void> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/file" + normalizePath(path);
        asyncRequestFactory.createRequest(PUT, requestUrl, null, false)
                           .data(content)
                           .loader(loaderFactory.newLoader("Updating file..."))
                           .send(callback);
    }

    @Override
    public void createFolder(String workspaceId, String path, AsyncRequestCallback<ItemReference> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/folder" + normalizePath(path);
        asyncRequestFactory.createPostRequest(requestUrl, null)
                           .loader(loaderFactory.newLoader("Creating folder..."))
                           .send(callback);
    }

    @Override
    public void delete(String workspaceId, String path, AsyncRequestCallback<Void> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + normalizePath(path);
        asyncRequestFactory.createRequest(DELETE, requestUrl, null, false)
                           .loader(loaderFactory.newLoader("Deleting project..."))
                           .send(callback);
    }

    @Override
    public void deleteModule(String workspaceId, String pathToParent, String modulePath, AsyncRequestCallback<Void> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/module" + normalizePath(pathToParent) + "?module=" + modulePath;
        asyncRequestFactory.createRequest(DELETE, requestUrl, null, false)
                           .loader(loaderFactory.newLoader("Deleting module..."))
                           .send(callback);
    }

    @Override
    public void copy(String workspaceId, String path, String newParentPath, String newName, AsyncRequestCallback<Void> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/copy" + normalizePath(path) + "?to=" + newParentPath;

        final CopyOptions copyOptions = dtoFactory.createDto(CopyOptions.class);
        copyOptions.setName(newName);
        copyOptions.setOverWrite(false);

        asyncRequestFactory.createPostRequest(requestUrl, copyOptions)
                           .loader(loaderFactory.newLoader("Copying..."))
                           .send(callback);
    }

    @Override
    public void move(String workspaceId, String path, String newParentPath, String newName, AsyncRequestCallback<Void> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/move" + normalizePath(path) + "?to=" + newParentPath;

        final MoveOptions moveOptions = dtoFactory.createDto(MoveOptions.class);
        moveOptions.setName(newName);
        moveOptions.setOverWrite(false);

        asyncRequestFactory.createPostRequest(requestUrl, moveOptions)
                           .loader(loaderFactory.newLoader("Moving..."))
                           .send(callback);
    }

    @Override
    public void rename(String workspaceId, String path, String newName, String newMediaType, AsyncRequestCallback<Void> callback) {
        final Path source = Path.valueOf(path);
        final Path sourceParent = source.removeLastSegments(1);
        move(workspaceId, source.toString(), sourceParent.toString(), newName, callback);
    }

    @Override
    public void importProject(String workspaceId,
                              String path,
                              boolean force,
                              SourceStorageDto sourceStorage,
                              RequestCallback<Void> callback) {
        final StringBuilder requestUrl = new StringBuilder("/project/" + workspaceId);
        requestUrl.append("/import").append(normalizePath(path));
        if (force) {
            requestUrl.append("?force=true");
        }

        MessageBuilder builder = new MessageBuilder(POST, requestUrl.toString());
        builder.data(dtoFactory.toJson(sourceStorage)).header(CONTENTTYPE, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    /**
     * Imports sources project.
     */
    @Override
    public Promise<Void> importProject(final String workspaceId,
                                       final String path,
                                       final boolean force,
                                       final SourceStorageDto sourceStorage) {
        return PromiseHelper.newPromise(new AsyncPromiseHelper.RequestCall<Void>() {
            @Override
            public void makeCall(final AsyncCallback<Void> callback) {
                final StringBuilder requestUrl = new StringBuilder("/project/" + workspaceId);
                requestUrl.append("/import").append(normalizePath(path));
                if (force) {
                    requestUrl.append("?force=true");
                }

                MessageBuilder builder = new MessageBuilder(POST, requestUrl.toString());
                builder.data(dtoFactory.toJson(sourceStorage)).header(CONTENTTYPE, APPLICATION_JSON);
                final Message message = builder.build();
                wsAgentStateController.getMessageBus().then(new Operation<MessageBus>() {
                    @Override
                    public void apply(MessageBus messageBus) throws OperationException {
                        try {
                            messageBus.send(message, new RequestCallback<Void>() {
                                @Override
                                protected void onSuccess(Void result) {
                                    callback.onSuccess(result);
                                }

                                @Override
                                protected void onFailure(Throwable exception) {
                                    callback.onFailure(exception);
                                }
                            });
                        } catch (WebSocketException e) {
                            callback.onFailure(e);
                        }
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError arg) throws OperationException {
                        callback.onFailure(arg.getCause());
                    }
                });
            }
        });
    }

    private void sendMessageToWS(final @NotNull Message message, final @NotNull RequestCallback<?> callback) {
        wsAgentStateController.getMessageBus().then(new Operation<MessageBus>() {
            @Override
            public void apply(MessageBus arg) throws OperationException {
                try {
                    arg.send(message, callback);
                } catch (WebSocketException e) {
                    throw new OperationException(e.getMessage(), e);
                }
            }
        });
    }

    @Override
    public void getChildren(String workspaceId, String path, AsyncRequestCallback<List<ItemReference>> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/children" + normalizePath(path);
        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .send(callback);
    }

    @Override
    public void getTree(String workspaceId, String path, int depth, AsyncRequestCallback<TreeElement> callback) {
        final String requestUrl = extPath + "/project/" + workspaceId + "/tree" + normalizePath(path) + "?depth=" + depth;
        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(ACCEPT, MimeType.APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Reading project..."))
                           .send(callback);
    }

    @Override
    public Promise<List<ItemReference>> search(String workspaceId, QueryExpression expression) {
        StringBuilder requestUrl = new StringBuilder(extPath + "/project/" + workspaceId + "/search");
        if (expression.getPath() != null) {
            requestUrl.append(normalizePath(expression.getPath()));
        } else {
            requestUrl.append('/');
        }

        StringBuilder queryParameters = new StringBuilder();
        if (expression.getName() != null && !expression.getName().isEmpty()) {
            queryParameters.append("&name=").append(expression.getName());
        }
        if (expression.getText() != null && !expression.getText().isEmpty()) {
            queryParameters.append("&text=").append(expression.getText());
        }
        if (expression.getMaxItems() != 0) {
            queryParameters.append("&maxItems=").append(expression.getMaxItems());
        }
        if (expression.getSkipCount() != 0) {
            queryParameters.append("&skipCount=").append(expression.getSkipCount());
        }

        return asyncRequestFactory.createGetRequest(requestUrl.toString() + queryParameters.toString().replaceFirst("&", "?"))
                                  .header(ACCEPT, MimeType.APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Searching..."))
                                  .send(dtoUnmarshaller.newListUnmarshaller(ItemReference.class));
    }

    /**
     * Normalizes the path by adding a leading '/' if it doesn't exist.
     * Also escapes some special characters.
     * <p/>
     * See following javascript functions for details:
     * escape() will not encode: @ * / +
     * encodeURI() will not encode: ~ ! @ # $ & * ( ) = : / , ; ? + '
     * encodeURIComponent() will not encode: ~ ! * ( ) '
     *
     * @param path
     *         path to normalize
     * @return normalized path
     */
    private String normalizePath(String path) {
        while (path.indexOf('+') >= 0) {
            path = path.replace("+", "%2B");
        }

        return path.startsWith("/") ? path : '/' + path;
    }
}
