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
package org.eclipse.che.ide.api.git;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchCreateRequest;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.DiffType;
import org.eclipse.che.api.git.shared.FetchRequest;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.MergeRequest;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.PullRequest;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.PushRequest;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.RemoteAddRequest;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusFormat;
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
    public Promise<Void> init(final Path project, final boolean bare) {
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
    public Promise<String> statusText(Path project, StatusFormat format) {
        String params = "?projectPath=" + project.toString() + "&format=" + format;
        String url = appContext.getDevAgentEndpoint() + STATUS + params;

        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .header(CONTENTTYPE, APPLICATION_JSON)
                                  .header(ACCEPT, TEXT_PLAIN)
                                  .send(new StringUnmarshaller());
    }

    @Override
    public void add(ProjectConfig project,
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
    public Promise<Void> add(final Path project, final boolean update, final Path[] paths) {
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
    public void commit(ProjectConfig project,
                       String message,
                       boolean all,
                       boolean amend,
                       AsyncRequestCallback<Revision> callback) {
        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(all);
        String url = appContext.getDevAgentEndpoint() + COMMIT + "?projectPath=" + project.getPath();

        asyncRequestFactory.createPostRequest(url, commitRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Revision> commit(Path project, String message, boolean all, boolean amend) {
        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(all);
        String url = appContext.getDevAgentEndpoint() + COMMIT + "?projectPath=" + project;

        return asyncRequestFactory.createPostRequest(url, commitRequest)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Revision.class));
    }

    @Override
    public Promise<Revision> commit(Path project, String message, Path[] files, boolean amend) {
        return commit(project, message, false, files, amend);
    }

    @Override
    public Promise<Revision> commit(Path project, String message, boolean all, Path[] files, boolean amend) {
        List<String> paths = new ArrayList<>(files.length);

        for (Path file : files) {
            if (!file.isEmpty()) {
                paths.add(file.toString());
            }
        }

        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(all)
                                                .withFiles(paths);
        String url = appContext.getDevAgentEndpoint() + COMMIT + "?projectPath=" + project;

        return asyncRequestFactory.createPostRequest(url, commitRequest)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Revision.class));
    }

    @Override
    public Promise<Map<String, String>> config(Path project, List<String> requestedConfig) {
        String params = "?projectPath=" + project.toString();
        if (requestedConfig != null) {
            for (String entry : requestedConfig) {
                params += "&requestedConfig=" + entry;
            }
        }
        String url = appContext.getDevAgentEndpoint() + CONFIG + params;
        return asyncRequestFactory.createGetRequest(url).loader(loader).send(new StringMapUnmarshaller());
    }

    @Override
    public Promise<PushResponse> push(ProjectConfig project, List<String> refSpec, String remote, boolean force) {
        PushRequest pushRequest = dtoFactory.createDto(PushRequest.class)
                                            .withRemote(remote)
                                            .withRefSpec(refSpec)
                                            .withForce(force);
        return asyncRequestFactory.createPostRequest(appContext.getDevAgentEndpoint() + PUSH +
                                                     "?projectPath=" + project.getPath(), pushRequest)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(PushResponse.class));
    }

    @Override
    public Promise<PushResponse> push(Path project, List<String> refSpec, String remote, boolean force) {
        PushRequest pushRequest = dtoFactory.createDto(PushRequest.class)
                                            .withRemote(remote)
                                            .withRefSpec(refSpec)
                                            .withForce(force);
        return asyncRequestFactory.createPostRequest(appContext.getDevAgentEndpoint() + PUSH + "?projectPath=" + project, pushRequest)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(PushResponse.class));
    }

    @Override
    public Promise<List<Remote>> remoteList(ProjectConfig project, @Nullable String remoteName, boolean verbose) {
        String params = "?projectPath=" + project.getPath() + "&verbose=" + String.valueOf(verbose);
        if (remoteName != null) {
            params += "&remoteName=" + remoteName;
        }
        String url = appContext.getDevAgentEndpoint() + REMOTE + params;
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(Remote.class));
    }

    @Override
    public Promise<List<Remote>> remoteList(Path project, String remoteName, boolean verbose) {
        String params = "?projectPath=" + project.toString() + (remoteName != null ? "&remoteName=" + remoteName : "") +
                        "&verbose=" + String.valueOf(verbose);
        String url = appContext.getDevAgentEndpoint() + REMOTE + params;
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(Remote.class));
    }

    @Override
    @Deprecated
    public void branchList(ProjectConfig project,
                           BranchListMode listMode,
                           AsyncRequestCallback<List<Branch>> callback) {
        String url = appContext.getDevAgentEndpoint() + BRANCH + "?projectPath=" + project.getPath() +
                     (listMode == null ? "" : "&listMode=" + listMode);
        asyncRequestFactory.createGetRequest(url).send(callback);
    }

    @Override
    public Promise<List<Branch>> branchList(Path project, BranchListMode listMode) {
        String url = appContext.getDevAgentEndpoint() + BRANCH + "?projectPath=" + project.toString() +
                     (listMode == null ? "" : "&listMode=" + listMode);
        return asyncRequestFactory.createGetRequest(url).send(dtoUnmarshallerFactory.newListUnmarshaller(Branch.class));
    }

    @Override
    public Promise<Status> getStatus(Path project) {
        final String params = "?projectPath=" + project.toString() + "&format=" + PORCELAIN;
        final String url = appContext.getDevAgentEndpoint() + STATUS + params;
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .header(CONTENTTYPE, APPLICATION_JSON)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Status.class));
    }

    @Override
    public Promise<Void> branchDelete(Path project, String name, boolean force) {
        String url = appContext.getDevAgentEndpoint() + BRANCH + "?projectPath=" + project.toString()
                     + "&name=" + name + "&force=" + force;
        return asyncRequestFactory.createDeleteRequest(url).loader(loader).send();
    }

    @Override
    public Promise<Void> branchRename(Path project, String oldName, String newName) {
        String params = "?projectPath=" + project.toString() + "&oldName=" + oldName + "&newName=" + newName;
        String url = appContext.getDevAgentEndpoint() + BRANCH + params;
        return asyncRequestFactory.createPostRequest(url, null).loader(loader)
                                  .header(CONTENTTYPE, MimeType.APPLICATION_FORM_URLENCODED)
                                  .send();
    }

    @Override
    public Promise<Branch> branchCreate(Path project, String name, String startPoint) {
        BranchCreateRequest branchCreateRequest = dtoFactory.createDto(BranchCreateRequest.class).withName(name).withStartPoint(startPoint);
        String url = appContext.getDevAgentEndpoint() + BRANCH + "?projectPath=" + project.toString();
        return asyncRequestFactory.createPostRequest(url, branchCreateRequest)
                                  .loader(loader)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Branch.class));
    }

    @Override
    public void checkout(ProjectConfig project,
                         CheckoutRequest checkoutRequest,
                         AsyncRequestCallback<String> callback) {
        String url = appContext.getDevAgentEndpoint() + CHECKOUT + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, checkoutRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> checkout(Path project,
                                  CheckoutRequest request) {

        final String url = appContext.getDevAgentEndpoint() + CHECKOUT + "?projectPath=" + project.toString();
        return asyncRequestFactory.createPostRequest(url, request).loader(loader).send();
    }

    @Override
    public Promise<Void> remove(Path project, Path[] items, boolean cached) {
        String params = "?projectPath=" + project.toString();
        if (items != null) {
            for (Path item : items) {
                params += "&items=" + item.toString();
            }
        }
        params += "&cached=" + String.valueOf(cached);
        String url = appContext.getDevAgentEndpoint() + REMOVE + params;
        return asyncRequestFactory.createDeleteRequest(url).loader(loader).send();
    }

    @Override
    public Promise<Void> reset(Path project, String commit, ResetRequest.ResetType resetType, Path[] files) {
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
        String url = appContext.getDevAgentEndpoint() + RESET + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, resetRequest).loader(loader).send();
    }

    @Override
    public Promise<LogResponse> log(Path project, Path[] fileFilter, boolean plainText) {
        return log(project, fileFilter, -1, -1, plainText);
    }

    @Override
    public Promise<LogResponse> log(Path project, Path[] fileFilter, int skip, int maxCount, boolean plainText) {
        StringBuilder params = new StringBuilder().append("?projectPath=").append(project.toString());
        if (fileFilter != null) {
            for (Path file : fileFilter) {
                params.append("&fileFilter=").append(file.toString());
            }
        }
        params.append("&skip=").append(skip);
        params.append("&maxCount=").append(maxCount);
        String url = appContext.getDevAgentEndpoint() + LOG + params;
        if (plainText) {
            return asyncRequestFactory.createGetRequest(url)
                                      .send(dtoUnmarshallerFactory.newUnmarshaller(LogResponse.class));
        } else {
            return asyncRequestFactory.createGetRequest(url)
                                      .header(ACCEPT, APPLICATION_JSON)
                                      .send(dtoUnmarshallerFactory.newUnmarshaller(LogResponse.class));
        }
    }

    @Override
    public void remoteAdd(ProjectConfig project,
                          String name,
                          String repositoryURL,
                          AsyncRequestCallback<String> callback) {
        RemoteAddRequest remoteAddRequest = dtoFactory.createDto(RemoteAddRequest.class).withName(name).withUrl(repositoryURL);
        String url = appContext.getDevAgentEndpoint() + REMOTE + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPutRequest(url, remoteAddRequest).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> remoteAdd(Path project, String name, String url) {
        RemoteAddRequest remoteAddRequest = dtoFactory.createDto(RemoteAddRequest.class).withName(name).withUrl(url);
        String requestUrl = appContext.getDevAgentEndpoint() + REMOTE + "?projectPath=" + project.toString();
        return asyncRequestFactory.createPutRequest(requestUrl, remoteAddRequest).loader(loader).send();
    }

    @Override
    public void remoteDelete(ProjectConfig project,
                             String name,
                             AsyncRequestCallback<String> callback) {
        String url = appContext.getDevAgentEndpoint() + REMOTE + '/' + name + "?projectPath=" + project.getPath();
        asyncRequestFactory.createDeleteRequest(url).loader(loader).send(callback);
    }

    @Override
    public Promise<Void> remoteDelete(Path project, String name) {
        String url = appContext.getDevAgentEndpoint() + REMOTE + '/' + name + "?projectPath=" + project.toString();
        return asyncRequestFactory.createDeleteRequest(url).loader(loader).send();
    }

    @Override
    public Promise<Void> fetch(Path project, String remote, List<String> refspec, boolean removeDeletedRefs) {
        FetchRequest fetchRequest = dtoFactory.createDto(FetchRequest.class)
                                              .withRefSpec(refspec)
                                              .withRemote(remote)
                                              .withRemoveDeletedRefs(removeDeletedRefs);
        String url = appContext.getDevAgentEndpoint() + FETCH + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, fetchRequest).send();
    }

    @Override
    public Promise<PullResponse> pull(Path project, String refSpec, String remote) {
        PullRequest pullRequest = dtoFactory.createDto(PullRequest.class).withRemote(remote).withRefSpec(refSpec);
        String url = appContext.getDevAgentEndpoint() + PULL + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, pullRequest).send(dtoUnmarshallerFactory.newUnmarshaller(PullResponse.class));
    }

    @Override
    public Promise<String> diff(Path project,
                                List<String> fileFilter,
                                DiffType type,
                                boolean noRenames,
                                int renameLimit,
                                String commitA,
                                String commitB) {
        return diff(project, fileFilter, type, noRenames, renameLimit, commitA, commitB, false).send(new StringUnmarshaller());
    }

    @Override
    public Promise<String> diff(Path project,
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
        if (commitA != null) {
            params.append("&commitA=").append(commitA);
        }
        if (commitB != null) {
            params.append("&commitB=").append(commitB);
        }
        params.append("&cached=").append(cached);

        String url = appContext.getDevAgentEndpoint() + DIFF + params;
        return asyncRequestFactory.createGetRequest(url).loader(loader);
    }

    @Override
    public Promise<ShowFileContentResponse> showFileContent(Path project, Path file, String version) {
        String params = "?projectPath=" + project.toString() + "&file=" + file + "&version=" + version ;
        String url = appContext.getDevAgentEndpoint() + SHOW + params;
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(ShowFileContentResponse.class));
    }

    @Override
    public Promise<MergeResult> merge(Path project, String commit) {
        MergeRequest mergeRequest = dtoFactory.createDto(MergeRequest.class).withCommit(commit);
        String url = appContext.getDevAgentEndpoint() + MERGE + "?projectPath=" + project;
        return asyncRequestFactory.createPostRequest(url, mergeRequest)
                                  .loader(loader)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MergeResult.class));
    }

    @Override
    public Promise<Void> deleteRepository(final Path project) {
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
}
