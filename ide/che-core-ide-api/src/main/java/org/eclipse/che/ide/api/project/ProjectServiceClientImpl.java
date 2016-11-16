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
package org.eclipse.che.ide.api.project;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.app.AppContext;
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
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.rest.UrlBuilder;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.gwt.http.client.RequestBuilder.DELETE;
import static com.google.gwt.http.client.RequestBuilder.POST;
import static com.google.gwt.http.client.RequestBuilder.PUT;
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
 * @see ProjectServiceClient
 */
public class ProjectServiceClientImpl implements ProjectServiceClient {

    private static final String PROJECT = "/project";

    private static final String ITEM     = "/item";
    private static final String TREE     = "/tree";
    private static final String MOVE     = "/move";
    private static final String COPY     = "/copy";
    private static final String FOLDER   = "/folder";
    private static final String FILE     = "/file";
    private static final String SEARCH   = "/search";
    private static final String IMPORT   = "/import";
    private static final String RESOLVE  = "/resolve";
    private static final String ESTIMATE = "/estimate";

    private final WsAgentStateController wsAgentStateController;
    private final LoaderFactory          loaderFactory;
    private final AsyncRequestFactory    reqFactory;
    private final DtoFactory             dtoFactory;
    private final DtoUnmarshallerFactory unmarshaller;
    private final AppContext             appContext;

    @Inject
    protected ProjectServiceClientImpl(WsAgentStateController wsAgentStateController,
                                       LoaderFactory loaderFactory,
                                       AsyncRequestFactory reqFactory,
                                       DtoFactory dtoFactory,
                                       DtoUnmarshallerFactory unmarshaller,
                                       AppContext appContext) {
        this.wsAgentStateController = wsAgentStateController;
        this.loaderFactory = loaderFactory;
        this.reqFactory = reqFactory;
        this.dtoFactory = dtoFactory;
        this.unmarshaller = unmarshaller;
        this.appContext = appContext;
    }

    /** {@inheritDoc} */
    @Override
    public Promise<List<ProjectConfigDto>> getProjects() {
        final String url = getBaseUrl();

        return reqFactory.createGetRequest(url)
                         .header(ACCEPT, MimeType.APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Getting projects..."))
                         .send(unmarshaller.newListUnmarshaller(ProjectConfigDto.class));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<SourceEstimation> estimate(Path path, String pType) {
        final String url = getBaseUrl() + ESTIMATE + path(path.toString()) + "?type=" + pType;

        return reqFactory.createGetRequest(url)
                         .header(ACCEPT, MimeType.APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Estimating project..."))
                         .send(unmarshaller.newUnmarshaller(SourceEstimation.class));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<List<SourceEstimation>> resolveSources(Path path) {
        final String url = getBaseUrl() + RESOLVE + path(path.toString());

        return reqFactory.createGetRequest(url)
                         .header(ACCEPT, MimeType.APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Resolving sources..."))
                         .send(unmarshaller.newListUnmarshaller(SourceEstimation.class));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> importProject(final Path path,
                                       final SourceStorageDto source) {
        return PromiseHelper.newPromise(new AsyncPromiseHelper.RequestCall<Void>() {
            @Override
            public void makeCall(final AsyncCallback<Void> callback) {
                final String url = PROJECT + IMPORT + path(path.toString());

                final Message message = new MessageBuilder(POST, url).data(dtoFactory.toJson(source))
                                                                     .header(CONTENTTYPE, APPLICATION_JSON)
                                                                     .build();

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

    /** {@inheritDoc} */
    @Override
    public Promise<List<ItemReference>> search(QueryExpression expression) {
        final String url = getBaseUrl() + SEARCH + (isNullOrEmpty(expression.getPath()) ? Path.ROOT : path(expression.getPath()));

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

        return reqFactory.createGetRequest(url + queryParameters.toString().replaceFirst("&", "?"))
                         .header(ACCEPT, MimeType.APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Searching..."))
                         .send(unmarshaller.newListUnmarshaller(ItemReference.class));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<ProjectConfigDto> createProject(ProjectConfigDto configuration, Map<String, String> options) {
        UrlBuilder urlBuilder = new UrlBuilder(getBaseUrl());
        for(String key : options.keySet()) {
            urlBuilder.setParameter(key, options.get(key));
        }
        return reqFactory.createPostRequest(urlBuilder.buildString(), configuration)
                         .header(ACCEPT, MimeType.APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Creating project..."))
                         .send(unmarshaller.newUnmarshaller(ProjectConfigDto.class));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<ItemReference> createFile(Path path, String content) {
        final String url = getBaseUrl() + FILE + path(path.parent().toString()) + "?name=" + URL.encodeQueryString(path.lastSegment());

        return reqFactory.createPostRequest(url, null)
                         .data(content)
                         .loader(loaderFactory.newLoader("Creating file..."))
                         .send(unmarshaller.newUnmarshaller(ItemReference.class));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<String> getFileContent(Path path) {
        final String url = getBaseUrl() + FILE + path(path.toString());

        return reqFactory.createGetRequest(url)
                         .loader(loaderFactory.newLoader("Loading file content..."))
                         .send(new StringUnmarshaller());
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> setFileContent(Path path, String content) {
        final String url = getBaseUrl() + FILE + path(path.toString());

        return reqFactory.createRequest(PUT, url, null, false)
                         .data(content)
                         .loader(loaderFactory.newLoader("Updating file..."))
                         .send();
    }

    /** {@inheritDoc} */
    @Override
    public Promise<ItemReference> createFolder(Path path) {
        final String url = getBaseUrl() + FOLDER + path(path.toString());

        return reqFactory.createPostRequest(url, null)
                         .loader(loaderFactory.newLoader("Creating folder..."))
                         .send(unmarshaller.newUnmarshaller(ItemReference.class));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> deleteItem(Path path) {
        final String url = getBaseUrl() + path(path.toString());

        return reqFactory.createRequest(DELETE, url, null, false)
                         .loader(loaderFactory.newLoader("Deleting project..."))
                         .send();
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> copy(Path source, Path target, String newName, boolean overwrite) {
        final String url = getBaseUrl() + COPY + path(source.toString()) + "?to=" + URL.encodeQueryString(target.toString());

        final CopyOptions copyOptions = dtoFactory.createDto(CopyOptions.class);
        copyOptions.setName(newName);
        copyOptions.setOverWrite(overwrite);

        return reqFactory.createPostRequest(url, copyOptions)
                         .loader(loaderFactory.newLoader("Copying..."))
                         .send();
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> move(Path source, Path target, String newName, boolean overwrite) {
        final String url = getBaseUrl() + MOVE + path(source.toString()) + "?to=" + URL.encodeQueryString(target.toString());

        final MoveOptions moveOptions = dtoFactory.createDto(MoveOptions.class);
        moveOptions.setName(newName);
        moveOptions.setOverWrite(overwrite);

        return reqFactory.createPostRequest(url, moveOptions)
                         .loader(loaderFactory.newLoader("Moving..."))
                         .send();
    }

    /** {@inheritDoc} */
    @Override
    public Promise<TreeElement> getTree(Path path, int depth, boolean includeFiles) {
        final String url = getBaseUrl() + TREE + path(path.toString()) + "?depth=" + depth + "&includeFiles=" + includeFiles;

        return reqFactory.createGetRequest(url)
                         .header(ACCEPT, MimeType.APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Reading project structure..."))
                         .send(unmarshaller.newUnmarshaller(TreeElement.class));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<ItemReference> getItem(Path path) {
        final String url = getBaseUrl() + ITEM + path(path.toString());

        return reqFactory.createGetRequest(url)
                         .header(ACCEPT, MimeType.APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Getting item..."))
                         .send(unmarshaller.newUnmarshaller(ItemReference.class));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<ProjectConfigDto> getProject(Path path) {
        final String url = getBaseUrl() + path(path.toString());

        return reqFactory.createGetRequest(url)
                         .header(ACCEPT, MimeType.APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Getting project..."))
                         .send(unmarshaller.newUnmarshaller(ProjectConfigDto.class));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<ProjectConfigDto> updateProject(ProjectConfigDto configuration) {
        final String url = getBaseUrl() + path(configuration.getPath());

        return reqFactory.createRequest(PUT, url, configuration, false)
                         .header(CONTENT_TYPE, MimeType.APPLICATION_JSON)
                         .header(ACCEPT, MimeType.APPLICATION_JSON)
                         .loader(loaderFactory.newLoader("Updating project..."))
                         .send(unmarshaller.newUnmarshaller(ProjectConfigDto.class));
    }

    /**
     * Returns the base url for the project service. It consists of workspace agent base url plus project prefix.
     *
     * @return base url for project service
     * @since 4.4.0
     */
    private String getBaseUrl() {
        return appContext.getDevMachine().getWsAgentBaseUrl() + PROJECT;
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
    private String path(String path) {
        while (path.indexOf('+') >= 0) {
            path = path.replace("+", "%2B");
        }

        return path.startsWith("/") ? path : '/' + path;
    }
}
