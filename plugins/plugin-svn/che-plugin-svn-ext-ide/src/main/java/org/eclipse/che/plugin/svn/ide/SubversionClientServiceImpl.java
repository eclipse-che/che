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
package org.eclipse.che.plugin.svn.ide;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.plugin.svn.shared.AddRequest;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponseList;
import org.eclipse.che.plugin.svn.shared.CLIOutputWithRevisionResponse;
import org.eclipse.che.plugin.svn.shared.CleanupRequest;
import org.eclipse.che.plugin.svn.shared.CommitRequest;
import org.eclipse.che.plugin.svn.shared.CopyRequest;
import org.eclipse.che.plugin.svn.shared.Depth;
import org.eclipse.che.plugin.svn.shared.GetRevisionsRequest;
import org.eclipse.che.plugin.svn.shared.GetRevisionsResponse;
import org.eclipse.che.plugin.svn.shared.InfoRequest;
import org.eclipse.che.plugin.svn.shared.InfoResponse;
import org.eclipse.che.plugin.svn.shared.ListRequest;
import org.eclipse.che.plugin.svn.shared.ListResponse;
import org.eclipse.che.plugin.svn.shared.LockRequest;
import org.eclipse.che.plugin.svn.shared.MergeRequest;
import org.eclipse.che.plugin.svn.shared.MoveRequest;
import org.eclipse.che.plugin.svn.shared.PropertyDeleteRequest;
import org.eclipse.che.plugin.svn.shared.PropertyGetRequest;
import org.eclipse.che.plugin.svn.shared.PropertyListRequest;
import org.eclipse.che.plugin.svn.shared.PropertyRequest;
import org.eclipse.che.plugin.svn.shared.PropertySetRequest;
import org.eclipse.che.plugin.svn.shared.RemoveRequest;
import org.eclipse.che.plugin.svn.shared.ResolveRequest;
import org.eclipse.che.plugin.svn.shared.RevertRequest;
import org.eclipse.che.plugin.svn.shared.SaveCredentialsRequest;
import org.eclipse.che.plugin.svn.shared.ShowDiffRequest;
import org.eclipse.che.plugin.svn.shared.ShowLogRequest;
import org.eclipse.che.plugin.svn.shared.StatusRequest;
import org.eclipse.che.plugin.svn.shared.UpdateRequest;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link SubversionClientService}.
 *
 * @author Jeremy Whitlock
 */
@Singleton
public class SubversionClientServiceImpl implements SubversionClientService {

    private final AsyncRequestFactory    asyncRequestFactory;
    private final AppContext             appContext;
    private final DtoFactory             dtoFactory;
    private final AsyncRequestLoader     loader;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    public SubversionClientServiceImpl(final AsyncRequestFactory asyncRequestFactory,
                                       final AppContext appContext,
                                       final DtoFactory dtoFactory,
                                       final LoaderFactory loaderFactory,
                                       final DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.asyncRequestFactory = asyncRequestFactory;
        this.appContext = appContext;
        this.dtoFactory = dtoFactory;
        this.loader = loaderFactory.newLoader();

        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    private String getBaseUrl() {
        return appContext.getDevMachine().getWsAgentBaseUrl() + "/svn/" + appContext.getWorkspaceId();
    }

    @Override
    public void add(@NotNull final String projectPath, final List<String> paths, final String depth,
                    final boolean addIgnored, final boolean addParents, final boolean autoProps,
                    final boolean noAutoProps,
                    final AsyncRequestCallback<CLIOutputResponse> callback) {
        final AddRequest request =
                dtoFactory.createDto(AddRequest.class)
                          .withAddIgnored(addIgnored)
                          .withAddParents(addParents)
                          .withDepth(depth)
                          .withPaths(paths)
                          .withProjectPath(projectPath)
                          .withAutoProps(autoProps)
                          .withNotAutoProps(noAutoProps);

        asyncRequestFactory.createPostRequest(getBaseUrl() + "/add", request).loader(loader).send(callback);
    }

    @Override
    public void revert(String projectPath, List<String> paths, final String depth, AsyncRequestCallback<CLIOutputResponse> callback) {
        final RevertRequest request = dtoFactory.createDto(RevertRequest.class).withProjectPath(projectPath).withPaths(paths)
                                                .withDepth(depth);
        asyncRequestFactory.createPostRequest(getBaseUrl() + "/revert", request).loader(loader).send(callback);
    }

    @Override
    public void copy(@NotNull String projectPath, String source, String destination, String comment,
                     AsyncRequestCallback<CLIOutputResponse> callback) {
        final CopyRequest request = dtoFactory.createDto(CopyRequest.class)
                                              .withProjectPath(projectPath)
                                              .withSource(source)
                                              .withDestination(destination)
                                              .withComment(comment);

        asyncRequestFactory.createPostRequest(getBaseUrl() + "/copy", request).loader(loader).send(callback);
    }

    @Override
    public void remove(@NotNull final String projectPath, final List<String> paths,
                       final AsyncRequestCallback<CLIOutputResponse> callback) {
        final RemoveRequest request =
                dtoFactory.createDto(RemoveRequest.class)
                          .withPaths(paths)
                          .withProjectPath(projectPath);

        asyncRequestFactory.createPostRequest(getBaseUrl() + "/remove", request).loader(loader).send(callback);
    }

    @Override
    public void merge(final @NotNull String projectPath, final String target, final String sourceURL,
                      final AsyncRequestCallback<CLIOutputResponse> callback) {
        final MergeRequest request = dtoFactory.createDto(MergeRequest.class)
                                               .withProjectPath(projectPath)
                                               .withTarget(target)
                                               .withSourceURL(sourceURL);
        asyncRequestFactory.createPostRequest(getBaseUrl() + "/merge", request).loader(loader).send(callback);
    }

    @Override
    public void info(final @NotNull String projectPath, final String target, final String revision, final boolean children,
                     final AsyncRequestCallback<InfoResponse> callback) {
        final InfoRequest request = dtoFactory.createDto(InfoRequest.class)
                                              .withProjectPath(projectPath)
                                              .withTarget(target)
                                              .withRevision(revision)
                                              .withChildren(children);

        asyncRequestFactory.createPostRequest(getBaseUrl() + "/info", request).loader(loader).send(callback);
    }

    @Override
    public void list(final @NotNull String projectPath, final String target,
                     final AsyncRequestCallback<ListResponse> callback) {
        final ListRequest request = dtoFactory.createDto(ListRequest.class)
                                              .withProjectPath(projectPath)
                                              .withTarget(target);

        asyncRequestFactory.createPostRequest(getBaseUrl() + "/list", request).loader(loader).send(callback);
    }

    @Override
    public void status(@NotNull final String projectPath, final List<String> paths, final String depth,
                       final boolean ignoreExternals, final boolean showIgnored, final boolean showUpdates,
                       final boolean showUnversioned, final boolean verbose, final List<String> changeLists,
                       final AsyncRequestCallback<CLIOutputResponse> callback) {
        final StatusRequest request =
                dtoFactory.createDto(StatusRequest.class)
                          .withVerbose(verbose)
                          .withChangeLists(changeLists)
                          .withDepth(depth)
                          .withIgnoreExternals(ignoreExternals)
                          .withPaths(paths)
                          .withProjectPath(projectPath)
                          .withShowIgnored(showIgnored)
                          .withShowUnversioned(showUnversioned)
                          .withShowUpdates(showUpdates);

        asyncRequestFactory.createPostRequest(getBaseUrl() + "/status", request).loader(loader).send(callback);
    }

    @Override
    public void update(@NotNull final String projectPath,
                       final List<String> paths,
                       final String revision,
                       final String depth,
                       final boolean ignoreExternals,
                       final String accept,
                       final AsyncRequestCallback<CLIOutputWithRevisionResponse> callback) {
        final UpdateRequest request =
                dtoFactory.createDto(UpdateRequest.class)
                          .withProjectPath(projectPath)
                          .withPaths(paths)
                          .withRevision(revision)
                          .withDepth(depth)
                          .withIgnoreExternals(ignoreExternals)
                          .withAccept(accept);

        asyncRequestFactory.createPostRequest(getBaseUrl() + "/update", request).loader(loader).send(callback);
    }

    @Override
    public void showLog(final String projectPath,
                        final List<String> paths,
                        final String revision,
                        final AsyncRequestCallback<CLIOutputResponse> callback) {
        final String url = getBaseUrl() + "/showlog";
        final ShowLogRequest request = dtoFactory.createDto(ShowLogRequest.class)
                                                 .withProjectPath(projectPath)
                                                 .withPaths(paths)
                                                 .withRevision(revision);
        asyncRequestFactory.createPostRequest(url, request).loader(loader).send(callback);
    }

    @Override
    public void lock(final @NotNull String projectPath, final List<String> paths, final boolean force,
                     final AsyncRequestCallback<CLIOutputResponse> callback) {
        final String url = getBaseUrl() + "/lock";
        final LockRequest request = dtoFactory.createDto(LockRequest.class)
                                              .withProjectPath(projectPath)
                                              .withTargets(paths)
                                              .withForce(force);
        asyncRequestFactory.createPostRequest(url, request).loader(loader).send(callback);
    }

    @Override
    public void unlock(final @NotNull String projectPath, final List<String> paths, final boolean force,
                       final AsyncRequestCallback<CLIOutputResponse> callback) {
        final String url = getBaseUrl() + "/unlock";
        final LockRequest request = dtoFactory.createDto(LockRequest.class)
                                              .withProjectPath(projectPath)
                                              .withTargets(paths)
                                              .withForce(force);
        asyncRequestFactory.createPostRequest(url, request).loader(loader).send(callback);
    }

    @Override
    public void showDiff(final String projectPath,
                         final List<String> paths,
                         final String revision,
                         final AsyncRequestCallback<CLIOutputResponse> callback) {
        final String url = getBaseUrl() + "/showdiff";
        final ShowDiffRequest request = dtoFactory.createDto(ShowDiffRequest.class)
                                                  .withProjectPath(projectPath)
                                                  .withPaths(paths)
                                                  .withRevision(revision);
        asyncRequestFactory.createPostRequest(url, request).loader(loader).send(callback);
    }

    @Override
    public void commit(final String projectPath, final List<String> paths, final String message,
                       final boolean keepChangeLists, final boolean keepLocks,
                       final AsyncRequestCallback<CLIOutputWithRevisionResponse> callback) {
        final String url = getBaseUrl() + "/commit";
        final CommitRequest request = dtoFactory.createDto(CommitRequest.class)
                                                .withPaths(paths)
                                                .withMessage(message)
                                                .withProjectPath(projectPath)
                                                .withKeepChangeLists(keepChangeLists)
                                                .withKeepLocks(keepLocks);
        asyncRequestFactory.createPostRequest(url, request).loader(loader).send(callback);
    }

    @Override
    public void cleanup(final String projectPath, final List<String> paths,
                        final AsyncRequestCallback<CLIOutputResponse> callback) {
        final String url = getBaseUrl() + "/cleanup";
        final CleanupRequest request = dtoFactory.createDto(CleanupRequest.class)
                                                 .withPaths(paths)
                                                 .withProjectPath(projectPath);
        asyncRequestFactory.createPostRequest(url, request).loader(loader).send(callback);
    }

    @Override
    public void showConflicts(final String projectPath, final List<String> paths, final AsyncCallback<List<String>> callback) {
        final Unmarshallable<CLIOutputResponse> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class);

        final StatusRequest request =
                dtoFactory.createDto(StatusRequest.class)
                          .withVerbose(false)
                          .withChangeLists(Collections.<String>emptyList())
                          .withDepth(Depth.FULLY_RECURSIVE.getValue())
                          .withIgnoreExternals(false)
                          .withPaths(paths)
                          .withProjectPath(projectPath)
                          .withShowIgnored(false)
                          .withShowUnversioned(false)
                          .withShowUpdates(false);

        //don't add loader to async request factory, this method calls only when menu item updates.
        asyncRequestFactory.createPostRequest(getBaseUrl() + "/status", request)
                           .send(new AsyncRequestCallback<CLIOutputResponse>(unmarshaller) {
                               @Override
                               protected void onSuccess(CLIOutputResponse result) {
                                   if (result != null) {
                                       List<String> conflictsList = parseConflictsList(result.getOutput());
                                       callback.onSuccess(conflictsList);
                                   } else {
                                       callback.onFailure(new Exception("showConflicts : no SvnResponse."));
                                   }
                               }

                               @Override
                               protected void onFailure(Throwable exception) {
                                   callback.onFailure(exception);
                               }
                           });
    }

    protected List<String> parseConflictsList(List<String> output) {
        List<String> conflictsList = new ArrayList<>();
        for (String line : output) {
            if (line.startsWith("C ")) {
                int lastSpaceIndex = line.lastIndexOf(' ');
                String filePathMatched = line.substring(lastSpaceIndex + 1);
                conflictsList.add(filePathMatched);
            }
        }
        return conflictsList;
    }

    @Override
    public void resolve(final String projectPath,
                        final Map<String, String> resolutions,
                        final String depth,
                        final AsyncCallback<CLIOutputResponseList> callback) {
        final String url = getBaseUrl() + "/resolve";
        final ResolveRequest request = dtoFactory.createDto(ResolveRequest.class)
                                                 .withProjectPath(projectPath)
                                                 .withConflictResolutions(resolutions)
                                                 .withDepth(depth);
        asyncRequestFactory.createPostRequest(url, request).loader(loader)
                           .send(new AsyncRequestCallback<CLIOutputResponseList>(
                                   dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponseList.class)) {

                               @Override
                               protected void onSuccess(CLIOutputResponseList result) {
                                   if (result != null) {
                                       callback.onSuccess(result);
                                   } else {
                                       callback.onFailure(new Exception("resolve : no SvnResponse."));
                                   }
                               }

                               @Override
                               protected void onFailure(Throwable exception) {
                                   callback.onFailure(exception);
                               }
                           });
    }

    @Override
    public void saveCredentials(final String repositoryUrl, final String username, final String password,
                                final AsyncRequestCallback<Void> callback) {
        final String url = getBaseUrl() + "/saveCredentials";
        final SaveCredentialsRequest request = dtoFactory.createDto(SaveCredentialsRequest.class)
                                                         .withUsername(username)
                                                         .withPassword(password)
                                                         .withRepositoryUrl(repositoryUrl);
        asyncRequestFactory.createPostRequest(url, request).loader(loader).send(callback);

    }

    @Override
    public void move(@NotNull String projectPath, List<String> source, String destination, String comment,
                     AsyncRequestCallback<CLIOutputResponse> callback) {
        final MoveRequest request =
                dtoFactory.createDto(MoveRequest.class)
                          .withProjectPath(projectPath)
                          .withSource(source)
                          .withDestination(destination)
                          .withComment(comment);

        asyncRequestFactory.createPostRequest(getBaseUrl() + "/move", request).loader(loader).send(callback);
    }

    @Override
    public void propertySet(String projectPath, String propertyName, String propertyValues, Depth depth, boolean force, String path,
                            AsyncRequestCallback<CLIOutputResponse> callback) {
        final PropertyRequest request =
                dtoFactory.createDto(PropertySetRequest.class)
                          .withValue(propertyValues)
                          .withProjectPath(projectPath)
                          .withName(propertyName)
                          .withDepth(depth)
                          .withForce(force)
                          .withPath(path);

        asyncRequestFactory.createPostRequest(getBaseUrl() + "/propset", request).loader(loader).send(callback);
    }

    @Override
    public void propertyDelete(String projectPath, String propertyName, Depth depth, boolean force, String path,
                               AsyncRequestCallback<CLIOutputResponse> callback) {
        final PropertyRequest request =
                dtoFactory.createDto(PropertyDeleteRequest.class)
                          .withProjectPath(projectPath)
                          .withName(propertyName)
                          .withDepth(depth)
                          .withForce(force)
                          .withPath(path);

        asyncRequestFactory.createPostRequest(getBaseUrl() + "/propdel", request).loader(loader).send(callback);
    }

    /**
     * Get the list of all revisions where a given path was modified
     *
     * @param projectPath
     *         the project path
     * @param path
     *         path to get the revisions for
     * @param revisionRange
     *         the range of revisions to check
     * @param callback
     */
    @Override
    public void getRevisions(@NotNull String projectPath, String path, String revisionRange,
                             AsyncRequestCallback<GetRevisionsResponse> callback) {
        final GetRevisionsRequest request = dtoFactory.createDto(GetRevisionsRequest.class)
                                                      .withProjectPath(projectPath)
                                                      .withPath(path)
                                                      .withRevisionRange(revisionRange);
        asyncRequestFactory.createPostRequest(getBaseUrl() + "/revisions", request).loader(loader).send(callback);
    }

    @Override
    public void propertyGet(String projectPath, String propertyName, String path, AsyncRequestCallback<CLIOutputResponse> callback) {
        final PropertyRequest request = dtoFactory.createDto(PropertyGetRequest.class)
                                                  .withProjectPath(projectPath)
                                                  .withName(propertyName)
                                                  .withPath(path);

        asyncRequestFactory.createPostRequest(getBaseUrl() + "/propget", request).loader(loader).send(callback);
    }

    @Override
    public void propertyList(String projectPath, String path, AsyncRequestCallback<CLIOutputResponse> callback) {
        final PropertyRequest request = dtoFactory.createDto(PropertyListRequest.class)
                                                  .withProjectPath(projectPath)
                                                  .withPath(path);

        asyncRequestFactory.createPostRequest(getBaseUrl() + "/proplist", request).loader(loader).send(callback);
    }
}
