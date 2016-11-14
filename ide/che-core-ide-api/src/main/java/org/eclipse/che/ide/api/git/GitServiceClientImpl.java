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
package org.eclipse.che.ide.api.git;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchCreateRequest;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.CloneRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.Commiters;
import org.eclipse.che.api.git.shared.DiffType;
import org.eclipse.che.api.git.shared.FetchRequest;
import org.eclipse.che.api.git.shared.GitUrlVendorInfo;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.MergeRequest;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.PullRequest;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.PushRequest;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.RemoteAddRequest;
import org.eclipse.che.api.git.shared.RepoInfo;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.gwt.http.client.RequestBuilder.DELETE;
import static com.google.gwt.http.client.RequestBuilder.POST;
import static org.eclipse.che.api.git.shared.StatusFormat.PORCELAIN;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.MimeType.TEXT_PLAIN;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENTTYPE;

/**
 * Implementation of the {@link GitServiceClient}.
 *
 * @author Ann Zhuleva
 * @author Valeriy Svydenko
 */
@Singleton
public class GitServiceClientImpl implements GitServiceClient {
    private static final String ADD         = "/git/add";
    private static final String BRANCH      = "/git/branch";
    private static final String CHECKOUT    = "/git/checkout";
    private static final String CLONE       = "/git/clone";
    private static final String COMMIT      = "/git/commit";
    private static final String CONFIG      = "/git/config";
    private static final String DIFF        = "/git/diff";
    private static final String FETCH       = "/git/fetch";
    private static final String INIT        = "/git/init";
    private static final String LOG         = "/git/log";
    private static final String SHOW        = "/git/show";
    private static final String MERGE       = "/git/merge";
    private static final String STATUS      = "/git/status";
    private static final String PUSH        = "/git/push";
    private static final String PULL        = "/git/pull";
    private static final String REMOTE      = "/git/remote";
    private static final String REMOVE      = "/git/remove";
    private static final String RESET       = "/git/reset";
    private static final String COMMITERS   = "/git/commiters";
    private static final String REPOSITORY  = "/git/repository";

    /** Loader to be displayed. */
    private final AsyncRequestLoader     loader;
    private final DtoFactory             dtoFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final WsAgentStateController wsAgentStateController;
    private final AppContext             appContext;

    @Inject
    protected GitServiceClientImpl(LoaderFactory loaderFactory,
                                   WsAgentStateController wsAgentStateController,
                                   DtoFactory dtoFactory,
                                   AsyncRequestFactory asyncRequestFactory,
                                   DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                   AppContext appContext) {
        this.wsAgentStateController = wsAgentStateController;
        this.appContext = appContext;
        this.loader = loaderFactory.newLoader();
        this.dtoFactory = dtoFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    @Override
    public void init(DevMachine devMachine, ProjectConfigDto project, boolean bare, final RequestCallback<Void> callback)
            throws WebSocketException {
        String url = INIT + "?projectPath=" + project.getPath() + "&bare=" + bare;

        Message message = new MessageBuilder(POST, url).header(ACCEPT, TEXT_PLAIN).build();

        sendMessageToWS(message, callback);
    }

    @Override
    public Promise<Void> init(DevMachine devMachine, final Path project, final boolean bare) {
        return createFromAsyncRequest(new RequestCall<Void>() {
            @Override
            public void makeCall(final AsyncCallback<Void> callback) {
                String url = INIT + "?projectPath=" + project.toString() + "&bare=" + bare;

                Message message = new MessageBuilder(POST, url).header(ACCEPT, TEXT_PLAIN).build();

                sendMessageToWS(message, new RequestCallback<Void>() {
                    @Override
                    protected void onSuccess(Void result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }
        });
    }

    @Override
    public void cloneRepository(DevMachine devMachine,
                                ProjectConfigDto project,
                                String remoteUri,
                                String remoteName,
                                RequestCallback<RepoInfo> callback) throws WebSocketException {
        CloneRequest cloneRequest = dtoFactory.createDto(CloneRequest.class)
                                              .withRemoteName(remoteName)
                                              .withRemoteUri(remoteUri)
                                              .withWorkingDir(project.getPath());

        String params = "?projectPath=" + project.getPath();

        String url = CLONE + params;

        MessageBuilder builder = new MessageBuilder(POST, url);
        builder.data(dtoFactory.toJson(cloneRequest))
               .header(CONTENTTYPE, APPLICATION_JSON)
               .header(ACCEPT, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
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
    @Deprecated
    public void statusText(DevMachine devMachine, ProjectConfigDto project, StatusFormat format, AsyncRequestCallback<String> callback) {
        String params = "?projectPath=" + project.getPath() + "&format=" + format;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + STATUS + params;

        asyncRequestFactory.createGetRequest(url)
                           .loader(loader)
                           .header(CONTENTTYPE, APPLICATION_JSON)
                           .header(ACCEPT, TEXT_PLAIN)
                           .send(callback);
    }

    @Override
    public Promise<String> statusText(DevMachine devMachine, Path project, StatusFormat format) {
        String params = "?projectPath=" + project.toString() + "&format=" + format;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + STATUS + params;

        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .header(CONTENTTYPE, APPLICATION_JSON)
                                  .header(ACCEPT, TEXT_PLAIN)
                                  .send(new StringUnmarshaller());
    }

    @Override
    public void add(DevMachine devMachine,
                    ProjectConfig project,
                    boolean update,
                    @Nullable List<String> filePattern,
                    RequestCallback<Void> callback) throws WebSocketException {
        AddRequest addRequest = dtoFactory.createDto(AddRequest.class).withUpdate(update);
        if (filePattern == null) {
            addRequest.setFilePattern(AddRequest.DEFAULT_PATTERN);
        } else {
            addRequest.setFilePattern(filePattern);
        }
        String url = ADD + "?projectPath=" + project.getPath();

        MessageBuilder builder = new MessageBuilder(POST, url);
        builder.data(dtoFactory.toJson(addRequest))
               .header(CONTENTTYPE, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    @Override
    public Promise<Void> add(final DevMachine devMachine, final Path project, final boolean update, final Path[] paths) {
        return createFromAsyncRequest(new RequestCall<Void>() {
            @Override
            public void makeCall(final AsyncCallback<Void> callback) {
                final AddRequest addRequest = dtoFactory.createDto(AddRequest.class).withUpdate(update);

                if (paths == null) {
                    addRequest.setFilePattern(AddRequest.DEFAULT_PATTERN);
                } else {

                    final List<String> patterns = new ArrayList<>(); //need for compatible with server side
                    for (Path path : paths) {
                        patterns.add(path.isEmpty() ? "." : path.toString());
                    }

                    addRequest.setFilePattern(patterns);
                }

                final String url = ADD + "?projectPath=" + project.toString();
                final Message message = new MessageBuilder(POST, url).data(dtoFactory.toJson(addRequest))
                                                                     .header(CONTENTTYPE, APPLICATION_JSON)
                                                                     .build();

                sendMessageToWS(message, new RequestCallback<Void>() {
                    @Override
                    protected void onSuccess(Void result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }
        });
    }

    @Override
    public void commit(DevMachine devMachine,
                       ProjectConfig project,
                       String message,
                       boolean all,
                       boolean amend,
                       AsyncRequestCallback<Revision> callback) {
        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(all);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + COMMIT + "?projectPath=" + project.getPath();

        asyncRequestFactory.createPostRequest(url, commitRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Revision> commit(DevMachine devMachine, Path project, String message, boolean all, boolean amend) {
        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(all);
        String url = devMachine.getWsAgentBaseUrl() + COMMIT + "?projectPath=" + project;

        return asyncRequestFactory.createPostRequest(url, commitRequest)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Revision.class));
    }

    @Override
    public void commit(final DevMachine devMachine,
                       final ProjectConfigDto project,
                       final String message,
                       final List<String> files,
                       final boolean amend,
                       final AsyncRequestCallback<Revision> callback) {
        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(false)
                                                .withFiles(files);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + COMMIT + "?projectPath=" + project.getPath();

        asyncRequestFactory.createPostRequest(url, commitRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Revision> commit(DevMachine devMachine, Path project, String message, Path[] files, boolean amend) {

        List<String> paths = new ArrayList<>(files.length);

        for (Path file : files) {
            paths.add(file.isEmpty() ? "." : file.toString());
        }

        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(false)
                                                .withFiles(paths);
        String url = devMachine.getWsAgentBaseUrl() + COMMIT + "?projectPath=" + project;

        return asyncRequestFactory.createPostRequest(url, commitRequest)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Revision.class));
    }

    /** {@inheritDoc} */
    @Override
    public void config(DevMachine devMachine,
                       ProjectConfigDto project,
                       List<String> requestedConfig,
                       AsyncRequestCallback<Map<String, String>> callback) {
        String params = "?projectPath=" + project.getPath();
        if (requestedConfig != null) {
            for (String entry : requestedConfig) {
                params += "&requestedConfig=" + entry;
            }
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + CONFIG + params;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    @Override
    public Promise<Map<String, String>> config(DevMachine devMachine, Path project, List<String> requestedConfig) {
        String params = "?projectPath=" + project.toString();
        if (requestedConfig != null) {
            for (String entry : requestedConfig) {
                params += "&requestedConfig=" + entry;
            }
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + CONFIG + params;
        return asyncRequestFactory.createGetRequest(url).loader(loader).send(new StringMapUnmarshaller());
    }

    @Override
    public void push(DevMachine devMachine,
                     ProjectConfigDto project,
                     List<String> refSpec,
                     String remote,
                     boolean force,
                     AsyncRequestCallback<PushResponse> callback) {
        PushRequest pushRequest = dtoFactory.createDto(PushRequest.class).withRemote(remote).withRefSpec(refSpec).withForce(force);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + PUSH + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, pushRequest).send(callback);
    }

    @Override
    public Promise<PushResponse> push(DevMachine devMachine, ProjectConfig project, List<String> refSpec, String remote, boolean force) {
        PushRequest pushRequest = dtoFactory.createDto(PushRequest.class)
                                            .withRemote(remote)
                                            .withRefSpec(refSpec)
                                            .withForce(force);
        return asyncRequestFactory.createPostRequest(appContext.getDevMachine().getWsAgentBaseUrl() + PUSH +
                                                     "?projectPath=" + project.getPath(), pushRequest)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(PushResponse.class));
    }

    @Override
    public Promise<PushResponse> push(DevMachine devMachine, Path project, List<String> refSpec, String remote, boolean force) {
        PushRequest pushRequest = dtoFactory.createDto(PushRequest.class)
                                            .withRemote(remote)
                                            .withRefSpec(refSpec)
                                            .withForce(force);
        return asyncRequestFactory.createPostRequest(devMachine.getWsAgentBaseUrl() + PUSH + "?projectPath=" + project, pushRequest)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(PushResponse.class));
    }

    @Override
    public void remoteList(DevMachine devMachine,
                           ProjectConfigDto project,
                           @Nullable String remoteName,
                           boolean verbose,
                           AsyncRequestCallback<List<Remote>> callback) {
        String params = "?projectPath=" + project.getPath() + (remoteName != null ? "&remoteName=" + remoteName : "") +
                        "&verbose=" + String.valueOf(verbose);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE + params;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    @Override
    public Promise<List<Remote>> remoteList(DevMachine devMachine, ProjectConfig project, @Nullable String remoteName, boolean verbose) {
        String params = "?projectPath=" + project.getPath() + "&verbose=" + String.valueOf(verbose);
        if (remoteName != null) {
            params += "&remoteName=" + remoteName;
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE + params;
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(Remote.class));
    }

    @Override
    public Promise<List<Remote>> remoteList(DevMachine devMachine, Path project, String remoteName, boolean verbose) {
        String params = "?projectPath=" + project.toString() + (remoteName != null ? "&remoteName=" + remoteName : "") +
                        "&verbose=" + String.valueOf(verbose);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE + params;
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(Remote.class));
    }

    @Override
    @Deprecated
    public void branchList(DevMachine devMachine,
                           ProjectConfig project,
                           BranchListMode listMode,
                           AsyncRequestCallback<List<Branch>> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH + "?projectPath=" + project.getPath() +
                     (listMode == null ? "" : "&listMode=" + listMode);
        asyncRequestFactory.createGetRequest(url).send(callback);
    }

    @Override
    public Promise<List<Branch>> branchList(DevMachine devMachine, Path project, BranchListMode listMode) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH + "?projectPath=" + project.toString() +
                     (listMode == null ? "" : "&listMode=" + listMode);
        return asyncRequestFactory.createGetRequest(url).send(dtoUnmarshallerFactory.newListUnmarshaller(Branch.class));
    }

    @Override
    public Promise<Status> getStatus(DevMachine devMachine, Path project) {
        final String params = "?projectPath=" + project.toString() + "&format=" + PORCELAIN;
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + STATUS + params;
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .header(CONTENTTYPE, APPLICATION_JSON)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Status.class));
    }

    @Override
    public void status(DevMachine devMachine, ProjectConfigDto project, AsyncRequestCallback<Status> callback) {
        String params = "?projectPath=" + project.getPath() + "&format=" + PORCELAIN;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + STATUS + params;
        asyncRequestFactory.createGetRequest(url).loader(loader)
                           .header(CONTENTTYPE, APPLICATION_JSON)
                           .header(ACCEPT, APPLICATION_JSON)
                           .send(callback);
    }

    @Override
    public void branchDelete(DevMachine devMachine,
                             ProjectConfigDto project,
                             String name,
                             boolean force,
                             AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH + "?projectPath=" + project.getPath()
                     + "&name=" + name + "&force=" + force;
        asyncRequestFactory.createDeleteRequest(url).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> branchDelete(DevMachine devMachine, Path project, String name, boolean force) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH + "?projectPath=" + project.toString()
                     + "&name=" + name + "&force=" + force;
        return asyncRequestFactory.createDeleteRequest(url).loader(loader).send();
    }

    @Override
    public void branchRename(DevMachine devMachine,
                             ProjectConfigDto project,
                             String oldName,
                             String newName,
                             AsyncRequestCallback<String> callback) {
        String params = "?projectPath=" + project.getPath() + "&oldName=" + oldName + "&newName=" + newName;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH + params;
        asyncRequestFactory.createPostRequest(url, null).loader(loader)
                           .header(CONTENTTYPE, MimeType.APPLICATION_FORM_URLENCODED)
                           .send(callback);
    }

    @Override
    public Promise<Void> branchRename(DevMachine devMachine, Path project, String oldName, String newName) {
        String params = "?projectPath=" + project.toString() + "&oldName=" + oldName + "&newName=" + newName;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH + params;
        return asyncRequestFactory.createPostRequest(url, null).loader(loader)
                                  .header(CONTENTTYPE, MimeType.APPLICATION_FORM_URLENCODED)
                                  .send();
    }

    @Override
    public void branchCreate(DevMachine devMachine, ProjectConfigDto project, String name, String startPoint,
                             AsyncRequestCallback<Branch> callback) {
        BranchCreateRequest branchCreateRequest = dtoFactory.createDto(BranchCreateRequest.class).withName(name).withStartPoint(startPoint);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, branchCreateRequest).loader(loader).header(ACCEPT, APPLICATION_JSON).send(callback);
    }

    @Override
    public Promise<Branch> branchCreate(DevMachine devMachine, Path project, String name, String startPoint) {
        BranchCreateRequest branchCreateRequest = dtoFactory.createDto(BranchCreateRequest.class).withName(name).withStartPoint(startPoint);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH + "?projectPath=" + project.toString();
        return asyncRequestFactory.createPostRequest(url, branchCreateRequest)
                                  .loader(loader)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Branch.class));
    }

    @Override
    public void checkout(DevMachine devMachine,
                         ProjectConfig project,
                         CheckoutRequest checkoutRequest,
                         AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + CHECKOUT + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, checkoutRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> checkout(DevMachine devMachine,
                                  Path project,
                                  CheckoutRequest request) {

        final String url = devMachine.getWsAgentBaseUrl() + CHECKOUT + "?projectPath=" + project.toString();
        return asyncRequestFactory.createPostRequest(url, request).loader(loader).send();
    }

    @Override
    public void remove(DevMachine devMachine,
                       ProjectConfigDto project,
                       List<String> items,
                       boolean cached,
                       AsyncRequestCallback<String> callback) {
        String params = "?projectPath=" + project.getPath();
        if (items != null) {
            for (String item : items) {
                params += "&items=" + item;
            }
        }
        params += "&cached=" + String.valueOf(cached);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOVE + params;
        asyncRequestFactory.createDeleteRequest(url).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> remove(DevMachine devMachine, Path project, Path[] items, boolean cached) {
        String params = "?projectPath=" + project.toString();
        if (items != null) {
            for (Path item : items) {
                params += "&items=" + item.toString();
            }
        }
        params += "&cached=" + String.valueOf(cached);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOVE + params;
        return asyncRequestFactory.createDeleteRequest(url).loader(loader).send();
    }

    @Override
    public void reset(DevMachine devMachine,
                      ProjectConfigDto project,
                      String commit,
                      @Nullable ResetRequest.ResetType resetType,
                      @Nullable List<String> filePattern,
                      AsyncRequestCallback<Void> callback) {
        ResetRequest resetRequest = dtoFactory.createDto(ResetRequest.class).withCommit(commit);
        if (resetType != null) {
            resetRequest.setType(resetType);
        }
        if (filePattern != null) {
            resetRequest.setFilePattern(filePattern);
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + RESET + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, resetRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> reset(DevMachine devMachine, Path project, String commit, ResetRequest.ResetType resetType, Path[] files) {
        ResetRequest resetRequest = dtoFactory.createDto(ResetRequest.class).withCommit(commit);
        if (resetType != null) {
            resetRequest.setType(resetType);
        }
        if (files != null) {
            List<String> fileList = new ArrayList<>(files.length);
            for (Path file : files) {
                fileList.add(file.isEmpty() ? "." : file.toString());
            }
            resetRequest.setFilePattern(fileList);
        }
        String url = devMachine.getWsAgentBaseUrl() + RESET + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, resetRequest).loader(loader).send();
    }

    @Override
    public void log(DevMachine devMachine, ProjectConfigDto project, List<String> fileFilter, boolean isTextFormat,
                    @NotNull AsyncRequestCallback<LogResponse> callback) {
        StringBuilder params = new StringBuilder().append("?projectPath=").append(project.getPath());
        if (fileFilter != null) {
            for (String file : fileFilter) {
                params.append("&fileFilter=").append(file);
            }
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + LOG + params;
        if (isTextFormat) {
            asyncRequestFactory.createGetRequest(url).send(callback);
        } else {
            asyncRequestFactory.createGetRequest(url).loader(loader).header(ACCEPT, APPLICATION_JSON).send(callback);
        }
    }

    @Override
    public Promise<LogResponse> log(DevMachine devMachine, Path project, Path[] fileFilter, boolean plainText) {
        StringBuilder params = new StringBuilder().append("?projectPath=").append(project.toString());
        if (fileFilter != null) {
            for (Path file : fileFilter) {
                params.append("&fileFilter=").append(file.toString());
            }
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + LOG + params;
        if (plainText) {
            return asyncRequestFactory.createGetRequest(url)
                                      .send(dtoUnmarshallerFactory.newUnmarshaller(LogResponse.class));
        } else {
            return asyncRequestFactory.createGetRequest(url)
                                      .loader(loader)
                                      .header(ACCEPT, APPLICATION_JSON)
                                      .send(dtoUnmarshallerFactory.newUnmarshaller(LogResponse.class));
        }
    }

    @Override
    public void remoteAdd(DevMachine devMachine,
                          ProjectConfig project,
                          String name,
                          String repositoryURL,
                          AsyncRequestCallback<String> callback) {
        RemoteAddRequest remoteAddRequest = dtoFactory.createDto(RemoteAddRequest.class).withName(name).withUrl(repositoryURL);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPutRequest(url, remoteAddRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> remoteAdd(DevMachine devMachine, Path project, String name, String url) {
        RemoteAddRequest remoteAddRequest = dtoFactory.createDto(RemoteAddRequest.class).withName(name).withUrl(url);
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE + "?projectPath=" + project.toString();
        return asyncRequestFactory.createPutRequest(requestUrl, remoteAddRequest).loader(loader).send();
    }

    @Override
    public void remoteDelete(DevMachine devMachine,
                             ProjectConfig project,
                             String name,
                             AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE + '/' + name + "?projectPath=" + project.getPath();
        asyncRequestFactory.createDeleteRequest(url).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> remoteDelete(DevMachine devMachine, Path project, String name) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE + '/' + name + "?projectPath=" + project.toString();
        return asyncRequestFactory.createDeleteRequest(url).loader(loader).send();
    }

    @Override
    public void fetch(DevMachine devMachine,
                      ProjectConfigDto project,
                      String remote,
                      List<String> refspec,
                      boolean removeDeletedRefs,
                      RequestCallback<String> callback) throws WebSocketException {
        FetchRequest fetchRequest = dtoFactory.createDto(FetchRequest.class)
                                              .withRefSpec(refspec)
                                              .withRemote(remote)
                                              .withRemoveDeletedRefs(removeDeletedRefs);

        String url = FETCH + "?projectPath=" + project.getPath();
        MessageBuilder builder = new MessageBuilder(POST, url);
        builder.data(dtoFactory.toJson(fetchRequest))
               .header(CONTENTTYPE, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    @Override
    public Promise<Void> fetch(DevMachine devMachine, Path project, String remote, List<String> refspec, boolean removeDeletedRefs) {
        FetchRequest fetchRequest = dtoFactory.createDto(FetchRequest.class)
                                              .withRefSpec(refspec)
                                              .withRemote(remote)
                                              .withRemoveDeletedRefs(removeDeletedRefs);
        String url = devMachine.getWsAgentBaseUrl() + FETCH + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, fetchRequest).send();
    }

    @Override
    public void pull(DevMachine devMachine,
                     ProjectConfigDto project,
                     String refSpec,
                     String remote,
                     AsyncRequestCallback<PullResponse> callback) {
        PullRequest pullRequest = dtoFactory.createDto(PullRequest.class).withRemote(remote).withRefSpec(refSpec);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + PULL + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, pullRequest).send(callback);
    }

    @Override
    public Promise<PullResponse> pull(DevMachine devMachine, Path project, String refSpec, String remote) {
        PullRequest pullRequest = dtoFactory.createDto(PullRequest.class).withRemote(remote).withRefSpec(refSpec);
        String url = devMachine.getWsAgentBaseUrl() + PULL + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, pullRequest).send(dtoUnmarshallerFactory.newUnmarshaller(PullResponse.class));
    }

    @Override
    public void diff(DevMachine devMachine,
                     ProjectConfigDto project,
                     List<String> fileFilter,
                     DiffType type,
                     boolean noRenames,
                     int renameLimit,
                     String commitA,
                     String commitB, @NotNull AsyncRequestCallback<String> callback) {
        diff(Path.valueOf(project.getPath()), fileFilter, type, noRenames, renameLimit, commitA, commitB, false).send(callback);
    }

    @Override
    public void diff(DevMachine devMachine,
                     ProjectConfigDto project,
                     List<String> fileFilter,
                     DiffType type,
                     boolean noRenames,
                     int renameLimit,
                     String commitA,
                     boolean cached,
                     AsyncRequestCallback<String> callback) {
        diff(Path.valueOf(project.getPath()), fileFilter, type, noRenames, renameLimit, commitA, null, cached).send(callback);
    }

    @Override
    public Promise<String> diff(DevMachine devMachine,
                                Path project,
                                List<String> fileFilter,
                                DiffType type,
                                boolean noRenames,
                                int renameLimit,
                                String commitA,
                                String commitB) {
        return diff(project, fileFilter, type, noRenames, renameLimit, commitA, commitB, false).send(new StringUnmarshaller());
    }

    @Override
    public Promise<String> diff(DevMachine devMachine,
                                Path project,
                                List<String> files,
                                DiffType type,
                                boolean noRenames,
                                int renameLimit,
                                String commitA,
                                boolean cached) {
        return diff(project, files, type, noRenames, renameLimit, commitA, null, cached).send(new StringUnmarshaller());
    }

    private AsyncRequest diff(Path project,
                              List<String> fileFilter,
                              DiffType type,
                              boolean noRenames,
                              int renameLimit,
                              String commitA,
                              String commitB,
                              boolean cached) {
        StringBuilder params = new StringBuilder().append("?projectPath=").append(project.toString());
        if (fileFilter != null) {
            for (String file : fileFilter) {
                if (file.isEmpty()) {
                    continue;
                }
                params.append("&fileFilter=").append(file);
            }
        }
        if (type != null) {
            params.append("&diffType=").append(type);
        }
        params.append("&noRenames=").append(noRenames);
        params.append("&renameLimit=").append(renameLimit);
        params.append("&commitA=").append(commitA);
        if (commitB != null) {
            params.append("&commitB=").append(commitB);
        }
        params.append("&cached=").append(cached);

        String url = appContext.getDevMachine().getWsAgentBaseUrl() + DIFF + params;
        return asyncRequestFactory.createGetRequest(url).loader(loader);
    }

    @Override
    public void showFileContent(DevMachine devMachine,
                                @NotNull ProjectConfigDto project,
                                String file,
                                String version,
                                @NotNull AsyncRequestCallback<ShowFileContentResponse> callback) {
        String params = "?projectPath=" + project.getPath() + "&file=" + file + "&version=" + version ;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + SHOW + params;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    @Override
    public Promise<ShowFileContentResponse> showFileContent(DevMachine devMachine, Path project, Path file, String version) {
        String params = "?projectPath=" + project.toString() + "&file=" + file + "&version=" + version ;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + SHOW + params;
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(ShowFileContentResponse.class));
    }

    @Override
    public void merge(DevMachine devMachine,
                      ProjectConfigDto project,
                      String commit,
                      AsyncRequestCallback<MergeResult> callback) {
        MergeRequest mergeRequest = dtoFactory.createDto(MergeRequest.class).withCommit(commit);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + MERGE + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, mergeRequest).loader(loader)
                           .header(ACCEPT, APPLICATION_JSON)
                           .send(callback);
    }

    @Override
    public Promise<MergeResult> merge(DevMachine devMachine, Path project, String commit) {
        MergeRequest mergeRequest = dtoFactory.createDto(MergeRequest.class).withCommit(commit);
        String url = devMachine.getWsAgentBaseUrl() + MERGE + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, mergeRequest)
                                  .loader(loader)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MergeResult.class));
    }

    @Override
    public void getCommitters(DevMachine devMachine, ProjectConfigDto project, AsyncRequestCallback<Commiters> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + COMMITERS + "?projectPath=" + project.getPath();
        asyncRequestFactory.createGetRequest(url).header(ACCEPT, APPLICATION_JSON).send(callback);
    }


    @Override
    public void deleteRepository(DevMachine devMachine, ProjectConfigDto project, AsyncRequestCallback<Void> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REPOSITORY + "?projectPath=" + project.getPath();
        asyncRequestFactory.createDeleteRequest(url).loader(loader)
                           .header(ACCEPT, TEXT_PLAIN)
                           .send(callback);
    }

    @Override
    public Promise<Void> deleteRepository(DevMachine devMachine, final Path project) {
        return createFromAsyncRequest(new RequestCall<Void>() {
            @Override
            public void makeCall(final AsyncCallback<Void> callback) {
                String url = REPOSITORY + "?projectPath=" + project.toString();
                final Message message = new MessageBuilder(DELETE, url).header(ACCEPT, TEXT_PLAIN)
                                                                       .build();

                sendMessageToWS(message, new RequestCallback<Void>() {
                    @Override
                    protected void onSuccess(Void result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }
        });
    }

    @Override
    public void getUrlVendorInfo(DevMachine devMachine, @NotNull String vcsUrl, @NotNull AsyncRequestCallback<GitUrlVendorInfo> callback) {
        asyncRequestFactory.createGetRequest(appContext.getDevMachine().getWsAgentBaseUrl() + "/git-service/info?vcsurl=" + vcsUrl)
                           .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(callback);
    }
}
