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
package org.eclipse.che.api.git.gwt.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchCreateRequest;
import org.eclipse.che.api.git.shared.BranchDeleteRequest;
import org.eclipse.che.api.git.shared.BranchListRequest;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.CloneRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.Commiters;
import org.eclipse.che.api.git.shared.ConfigRequest;
import org.eclipse.che.api.git.shared.DiffRequest;
import org.eclipse.che.api.git.shared.FetchRequest;
import org.eclipse.che.api.git.shared.GitUrlVendorInfo;
import org.eclipse.che.api.git.shared.InitRequest;
import org.eclipse.che.api.git.shared.LogRequest;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.MergeRequest;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.PullRequest;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.PushRequest;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.RemoteAddRequest;
import org.eclipse.che.api.git.shared.RemoteListRequest;
import org.eclipse.che.api.git.shared.RepoInfo;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.RmRequest;
import org.eclipse.che.api.git.shared.ShowFileContentRequest;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.api.machine.gwt.client.DevMachine;
import org.eclipse.che.api.machine.gwt.client.WsAgentStateController;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import static com.google.gwt.http.client.RequestBuilder.POST;
import static org.eclipse.che.api.git.shared.StatusFormat.PORCELAIN;
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
    public static final String ADD               = "/add";
    public static final String BRANCH_LIST       = "/branch-list";
    public static final String CHECKOUT          = "/checkout";
    public static final String BRANCH_CREATE     = "/branch-create";
    public static final String BRANCH_DELETE     = "/branch-delete";
    public static final String BRANCH_RENAME     = "/branch-rename";
    public static final String CLONE             = "/clone";
    public static final String COMMIT            = "/commit";
    public static final String CONFIG            = "/config";
    public static final String DIFF              = "/diff";
    public static final String FETCH             = "/fetch";
    public static final String INIT              = "/init";
    public static final String LOG               = "/log";
    public static final String SHOW              = "/show";
    public static final String MERGE             = "/merge";
    public static final String STATUS            = "/status";
    public static final String RO_URL            = "/read-only-url";
    public static final String PUSH              = "/push";
    public static final String PULL              = "/pull";
    public static final String REMOTE_LIST       = "/remote-list";
    public static final String REMOTE_ADD        = "/remote-add";
    public static final String REMOTE_DELETE     = "/remote-delete";
    public static final String REMOVE            = "/rm";
    public static final String RESET             = "/reset";
    public static final String COMMITERS         = "/commiters";
    public static final String DELETE_REPOSITORY = "/delete-repository";

    /** Loader to be displayed. */
    private final AsyncRequestLoader     loader;
    private final WsAgentStateController wsAgentStateController;
    private final DtoFactory             dtoFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final AppContext             appContext;

    @Inject
    protected GitServiceClientImpl(LoaderFactory loaderFactory,
                                   WsAgentStateController wsAgentStateController,
                                   DtoFactory dtoFactory,
                                   AsyncRequestFactory asyncRequestFactory,
                                   DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                   AppContext appContext) {
        this.appContext = appContext;
        this.loader = loaderFactory.newLoader();
        this.wsAgentStateController = wsAgentStateController;
        this.dtoFactory = dtoFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void init(DevMachine devMachine, ProjectConfigDto project, boolean bare, final RequestCallback<Void> callback)
            throws WebSocketException {
        InitRequest initRequest = dtoFactory.createDto(InitRequest.class);
        initRequest.setBare(bare);
        initRequest.setWorkingDir(project.getName());

        String url = "/git/" + devMachine.getWorkspace() + INIT + "?projectPath=" + project.getPath();

        MessageBuilder builder = new MessageBuilder(POST, url);
        builder.data(dtoFactory.toJson(initRequest)).header(CONTENTTYPE, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    /** {@inheritDoc} */
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

        String url = "/git/" + devMachine.getWorkspace() + CLONE + params;

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

    /** {@inheritDoc} */
    @Override
    public void statusText(DevMachine devMachine, ProjectConfigDto project, StatusFormat format, AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + STATUS;
        String params = "?projectPath=" + project.getPath() + "&format=" + format;

        asyncRequestFactory.createPostRequest(url + params, null)
                           .loader(loader)
                           .header(CONTENTTYPE, APPLICATION_JSON)
                           .header(ACCEPT, TEXT_PLAIN)
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void add(DevMachine devMachine,
                    ProjectConfigDto project,
                    boolean update,
                    @Nullable List<String> filePattern,
                    RequestCallback<Void> callback) throws WebSocketException {
        AddRequest addRequest = dtoFactory.createDto(AddRequest.class).withUpdate(update);
        if (filePattern == null) {
            addRequest.setFilepattern(AddRequest.DEFAULT_PATTERN);
        } else {
            addRequest.setFilepattern(filePattern);
        }
        String url = "/git/" + devMachine.getWorkspace() + ADD + "?projectPath=" + project.getPath();

        MessageBuilder builder = new MessageBuilder(POST, url);
        builder.data(dtoFactory.toJson(addRequest))
               .header(CONTENTTYPE, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    /** {@inheritDoc} */
    @Override
    public void commit(DevMachine devMachine,
                       ProjectConfigDto project,
                       String message,
                       boolean all,
                       boolean amend,
                       AsyncRequestCallback<Revision> callback) {
        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(all);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + COMMIT + "?projectPath=" + project.getPath();

        asyncRequestFactory.createPostRequest(url, commitRequest).loader(loader).send(callback);
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
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + COMMIT + "?projectPath=" + project.getPath();

        asyncRequestFactory.createPostRequest(url, commitRequest).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void config(DevMachine devMachine,
                       ProjectConfigDto project,
                       @Nullable List<String> entries,
                       boolean all,
                       AsyncRequestCallback<Map<String, String>> callback) {
        ConfigRequest configRequest = dtoFactory.createDto(ConfigRequest.class)
                                                .withGetAll(all)
                                                .withConfigEntry(entries);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + CONFIG + "?projectPath=" + project.getPath();

        asyncRequestFactory.createPostRequest(url, configRequest).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void push(DevMachine devMachine,
                     ProjectConfigDto project,
                     List<String> refSpec,
                     String remote,
                     boolean force,
                     AsyncRequestCallback<PushResponse> callback) {
        PushRequest pushRequest = dtoFactory.createDto(PushRequest.class).withRemote(remote).withRefSpec(refSpec).withForce(force);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + PUSH + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, pushRequest).send(callback);
    }

    @Override
    public Promise<PushResponse> push(DevMachine devMachine, ProjectConfigDto project, List<String> refSpec, String remote, boolean force) {
        PushRequest pushRequest = dtoFactory.createDto(PushRequest.class)
                                            .withRemote(remote)
                                            .withRefSpec(refSpec)
                                            .withForce(force);
        return asyncRequestFactory.createPostRequest(appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + PUSH +
                                                     "?projectPath=" + project.getPath(), pushRequest)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(PushResponse.class));
    }

    /** {@inheritDoc} */
    @Override
    public void remoteList(DevMachine devMachine,
                           ProjectConfigDto project,
                           @Nullable String remoteName,
                           boolean verbose,
                           AsyncRequestCallback<List<Remote>> callback) {
        RemoteListRequest remoteListRequest = dtoFactory.createDto(RemoteListRequest.class).withVerbose(verbose);
        if (remoteName != null) {
            remoteListRequest.setRemote(remoteName);
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + REMOTE_LIST + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, remoteListRequest).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<List<Remote>> remoteList(DevMachine devMachine, ProjectConfigDto project, @Nullable String remoteName, boolean verbose) {
        RemoteListRequest remoteListRequest = dtoFactory.createDto(RemoteListRequest.class).withVerbose(verbose);
        if (remoteName != null) {
            remoteListRequest.setRemote(remoteName);
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + REMOTE_LIST + "?projectPath=" + project.getPath();
        return asyncRequestFactory.createPostRequest(url, remoteListRequest)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(Remote.class));
    }

    /** {@inheritDoc} */
    @Override
    public void branchList(DevMachine devMachine,
                           ProjectConfigDto project,
                           @Nullable String remoteMode,
                           AsyncRequestCallback<List<Branch>> callback) {
        BranchListRequest branchListRequest = dtoFactory.createDto(BranchListRequest.class).withListMode(remoteMode);
        String url =appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + BRANCH_LIST + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, branchListRequest).send(callback);
    }

    @Override
    public Promise<Status> status(DevMachine devMachine, ProjectConfigDto project) {
        final String params = "?projectPath=" + project.getPath() + "&format=" + PORCELAIN;
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + STATUS + params;
        return asyncRequestFactory.createPostRequest(url, null)
                                  .loader(loader)
                                  .header(CONTENTTYPE, APPLICATION_JSON)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Status.class));
    }

    /** {@inheritDoc} */
    @Override
    public void status(DevMachine devMachine, ProjectConfigDto project, AsyncRequestCallback<Status> callback) {
        String params = "?projectPath=" + project.getPath() + "&format=" + PORCELAIN;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + STATUS + params;
        asyncRequestFactory.createPostRequest(url, null).loader(loader)
                           .header(CONTENTTYPE, APPLICATION_JSON)
                           .header(ACCEPT, APPLICATION_JSON)
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void branchDelete(DevMachine devMachine,
                             ProjectConfigDto project,
                             String name,
                             boolean force,
                             AsyncRequestCallback<String> callback) {
        BranchDeleteRequest branchDeleteRequest = dtoFactory.createDto(BranchDeleteRequest.class).withName(name).withForce(force);
        String url =appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + BRANCH_DELETE + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, branchDeleteRequest).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void branchRename(DevMachine devMachine,
                             ProjectConfigDto project,
                             String oldName,
                             String newName,
                             AsyncRequestCallback<String> callback) {
        String params = "?projectPath=" + project.getPath() + "&oldName=" + oldName + "&newName=" + newName;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + BRANCH_RENAME + params;
        asyncRequestFactory.createPostRequest(url, null).loader(loader)
                           .header(CONTENTTYPE, MimeType.APPLICATION_FORM_URLENCODED)
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void branchCreate(DevMachine devMachine, ProjectConfigDto project, String name, String startPoint,
                             AsyncRequestCallback<Branch> callback) {
        BranchCreateRequest branchCreateRequest = dtoFactory.createDto(BranchCreateRequest.class).withName(name).withStartPoint(startPoint);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + BRANCH_CREATE + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, branchCreateRequest).loader(loader).header(ACCEPT, APPLICATION_JSON).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void checkout(DevMachine devMachine,
                         ProjectConfigDto project,
                         CheckoutRequest checkoutRequest,
                         AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + CHECKOUT + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, checkoutRequest).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void remove(DevMachine devMachine,
                       ProjectConfigDto project,
                       List<String> items,
                       boolean cached,
                       AsyncRequestCallback<String> callback) {
        RmRequest rmRequest = dtoFactory.createDto(RmRequest.class).withItems(items).withCached(cached).withRecursively(true);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + REMOVE + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, rmRequest).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
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
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + RESET + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, resetRequest).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void log(DevMachine devMachine, ProjectConfigDto project, List<String> fileFilter, boolean isTextFormat,
                    @NotNull AsyncRequestCallback<LogResponse> callback) {
        LogRequest logRequest = dtoFactory.createDto(LogRequest.class).withFileFilter(fileFilter);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + LOG + "?projectPath=" + project.getPath();
        if (isTextFormat) {
            asyncRequestFactory.createPostRequest(url, logRequest).send(callback);
        } else {
            asyncRequestFactory.createPostRequest(url, logRequest).loader(loader).header(ACCEPT, APPLICATION_JSON).send(callback);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void remoteAdd(DevMachine devMachine,
                          ProjectConfigDto project,
                          String name,
                          String repositoryURL,
                          AsyncRequestCallback<String> callback) {
        RemoteAddRequest remoteAddRequest = dtoFactory.createDto(RemoteAddRequest.class).withName(name).withUrl(repositoryURL);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + REMOTE_ADD + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, remoteAddRequest).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void remoteDelete(DevMachine devMachine,
                             ProjectConfigDto project,
                             String name,
                             AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + REMOTE_DELETE + '/' + name + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, null).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
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

        String url = "/git/" + devMachine.getWorkspace() + FETCH + "?projectPath=" + project.getPath();
        MessageBuilder builder = new MessageBuilder(POST, url);
        builder.data(dtoFactory.toJson(fetchRequest))
               .header(CONTENTTYPE, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    /** {@inheritDoc} */
    @Override
    public void pull(DevMachine devMachine,
                     ProjectConfigDto project,
                     String refSpec,
                     String remote,
                     AsyncRequestCallback<PullResponse> callback) {
        PullRequest pullRequest = dtoFactory.createDto(PullRequest.class).withRemote(remote).withRefSpec(refSpec);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + PULL + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, pullRequest).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void diff(DevMachine devMachine,
                     ProjectConfigDto project,
                     List<String> fileFilter,
                     DiffRequest.DiffType type,
                     boolean noRenames,
                     int renameLimit,
                     String commitA,
                     String commitB, @NotNull AsyncRequestCallback<String> callback) {
        DiffRequest diffRequest = dtoFactory.createDto(DiffRequest.class)
                                            .withFileFilter(fileFilter)
                                            .withType(type)
                                            .withNoRenames(noRenames)
                                            .withCommitA(commitA)
                                            .withCommitB(commitB)
                                            .withRenameLimit(renameLimit);

        diff(devMachine, diffRequest, project.getPath(), callback);
    }

    /** {@inheritDoc} */
    @Override
    public void showFileContent(DevMachine devMachine,
                                @NotNull ProjectConfigDto project,
                                String file,
                                String version,
                                @NotNull AsyncRequestCallback<ShowFileContentResponse> callback) {
        ShowFileContentRequest showRequest = dtoFactory.createDto(ShowFileContentRequest.class).withFile(file).withVersion(version);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + SHOW + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, showRequest).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void diff(DevMachine devMachine,
                     ProjectConfigDto project,
                     List<String> fileFilter,
                     DiffRequest.DiffType type,
                     boolean noRenames,
                     int renameLimit,
                     String commitA,
                     boolean cached,
                     AsyncRequestCallback<String> callback) {
        DiffRequest diffRequest = dtoFactory.createDto(DiffRequest.class)
                                            .withFileFilter(fileFilter).withType(type)
                                            .withNoRenames(noRenames)
                                            .withCommitA(commitA)
                                            .withRenameLimit(renameLimit)
                                            .withCached(cached);

        diff(devMachine, diffRequest, project.getPath(), callback);
    }

    /**
     * Make diff request.
     *
     * @param diffRequest
     *         request for diff
     * @param projectPath
     *         project path
     * @param callback
     *         callback
     */
    private void diff(DevMachine devMachine, DiffRequest diffRequest, @NotNull String projectPath, AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + DIFF + "?projectPath=" + projectPath;
        asyncRequestFactory.createPostRequest(url, diffRequest).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void merge(DevMachine devMachine,
                      ProjectConfigDto project,
                      String commit,
                      AsyncRequestCallback<MergeResult> callback) {
        MergeRequest mergeRequest = dtoFactory.createDto(MergeRequest.class).withCommit(commit);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + MERGE + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, mergeRequest).loader(loader)
                           .header(ACCEPT, APPLICATION_JSON)
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getGitReadOnlyUrl(DevMachine devMachine, ProjectConfigDto project, AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + RO_URL + "?projectPath=" + project.getPath();
        asyncRequestFactory.createGetRequest(url).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getCommitters(DevMachine devMachine, ProjectConfigDto project, AsyncRequestCallback<Commiters> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + COMMITERS + "?projectPath=" + project.getPath();
        asyncRequestFactory.createGetRequest(url).header(ACCEPT, APPLICATION_JSON).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteRepository(DevMachine devMachine, ProjectConfigDto project, AsyncRequestCallback<Void> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + DELETE_REPOSITORY + "?projectPath=" + project.getPath();
        asyncRequestFactory.createGetRequest(url).loader(loader)
                           .header(CONTENTTYPE, APPLICATION_JSON).header(ACCEPT, TEXT_PLAIN)
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getUrlVendorInfo(DevMachine devMachine, @NotNull String vcsUrl, @NotNull AsyncRequestCallback<GitUrlVendorInfo> callback) {
        asyncRequestFactory.createGetRequest(appContext.getDevMachine().getWsAgentBaseUrl() + "/git/" + devMachine.getWorkspace() + "/git-service/info?vcsurl=" + vcsUrl)
                           .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(callback);
    }
}
