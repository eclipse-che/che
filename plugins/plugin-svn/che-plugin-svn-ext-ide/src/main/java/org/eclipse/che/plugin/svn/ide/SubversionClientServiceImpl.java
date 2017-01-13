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
package org.eclipse.che.plugin.svn.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.subversion.Credentials;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
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
import org.eclipse.che.plugin.svn.shared.ShowDiffRequest;
import org.eclipse.che.plugin.svn.shared.ShowLogRequest;
import org.eclipse.che.plugin.svn.shared.StatusRequest;
import org.eclipse.che.plugin.svn.shared.SwitchRequest;
import org.eclipse.che.plugin.svn.shared.UpdateRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.resource.Path.toList;

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
        return appContext.getDevMachine().getWsAgentBaseUrl() + "/svn";
    }

    @Override
    public Promise<CLIOutputResponse> add(Path project, Path[] path, String depth, boolean addIgnored, boolean addParents,
                                          boolean autoProps, boolean noAutoProps) {
        final AddRequest request = dtoFactory.createDto(AddRequest.class)
                                             .withAddIgnored(addIgnored)
                                             .withAddParents(addParents)
                                             .withDepth(depth)
                                             .withPaths(toList(path))
                                             .withProjectPath(project.toString())
                                             .withAutoProps(autoProps)
                                             .withNotAutoProps(noAutoProps);

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/add", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> revert(Path project, Path[] paths, String depth) {
        final RevertRequest request = dtoFactory.createDto(RevertRequest.class)
                                                .withProjectPath(project.toString())
                                                .withPaths(toList(paths))
                                                .withDepth(depth);
        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/revert", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> copy(Path project, Path source, Path destination, String comment, Credentials credentials) {
        final CopyRequest request = dtoFactory.createDto(CopyRequest.class)
                                              .withProjectPath(project.toString())
                                              .withSource(source.toString())
                                              .withDestination(destination.toString())
                                              .withComment(comment);
        if (credentials != null) {
            request.setUsername(credentials.getUsername());
            request.setPassword(credentials.getPassword());
        }

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/copy", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> remove(Path project, Path[] paths) {
        final RemoveRequest request =
                dtoFactory.createDto(RemoveRequest.class)
                          .withPaths(toList(paths))
                          .withProjectPath(project.toString());

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/remove", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> merge(Path project, Path target, Path sourceUrl) {
        final MergeRequest request = dtoFactory.createDto(MergeRequest.class)
                                               .withProjectPath(project.toString())
                                               .withTarget(target.toString())
                                               .withSourceURL(sourceUrl.toString());
        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/merge", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<InfoResponse> info(Path project, String target, String revision, boolean children) {
        return info(project, target, revision, children, dtoFactory.createDto(Credentials.class));
    }

    @Override
    public Promise<InfoResponse> info(Path project, String target, String revision, boolean children, Credentials credentials) {
        final InfoRequest request = dtoFactory.createDto(InfoRequest.class)
                                              .withProjectPath(project.toString())
                                              .withTarget(target)
                                              .withRevision(revision)
                                              .withChildren(children);
        if (credentials != null) {
            request.setUsername(credentials.getUsername());
            request.setPassword(credentials.getPassword());
        }

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/info", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(InfoResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> status(Path project, Path[] paths, String depth, boolean ignoreExternals, boolean showIgnored,
                                             boolean showUpdates, boolean showUnversioned, boolean verbose, List<String> changeLists) {
        final StatusRequest request = dtoFactory.createDto(StatusRequest.class)
                                                .withVerbose(verbose)
                                                .withChangeLists(changeLists)
                                                .withDepth(depth)
                                                .withIgnoreExternals(ignoreExternals)
                                                .withPaths(toList(paths))
                                                .withProjectPath(project.toString())
                                                .withShowIgnored(showIgnored)
                                                .withShowUnversioned(showUnversioned)
                                                .withShowUpdates(showUpdates);

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/status", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputWithRevisionResponse> update(Path project,
                                                         Path[] paths,
                                                         String revision,
                                                         String depth,
                                                         boolean ignoreExternals,
                                                         String accept,
                                                         Credentials credentials) {
        final UpdateRequest request =
                dtoFactory.createDto(UpdateRequest.class)
                          .withProjectPath(project.toString())
                          .withPaths(toList(paths))
                          .withRevision(revision)
                          .withDepth(depth)
                          .withIgnoreExternals(ignoreExternals)
                          .withAccept(accept);
        if (credentials != null) {
            request.setUsername(credentials.getUsername());
            request.setPassword(credentials.getPassword());
        }

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/update", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputWithRevisionResponse.class));
    }

    @Override
    public Promise<CLIOutputWithRevisionResponse> doSwitch(String location,
                                                           Path project,
                                                           String revision,
                                                           String depth,
                                                           String setDepth,
                                                           String accept,
                                                           boolean ignoreExternals,
                                                           boolean ignoreAncestry,
                                                           boolean relocate,
                                                           boolean force,
                                                           @Nullable Credentials credentials) {
        SwitchRequest request = dtoFactory.createDto(SwitchRequest.class)
                                          .withLocation(location)
                                          .withProjectPath(project.toString())
                                          .withDepth(depth)
                                          .withSetDepth(setDepth)
                                          .withRelocate(relocate)
                                          .withIgnoreExternals(ignoreExternals)
                                          .withIgnoreAncestry(ignoreAncestry)
                                          .withRevision(revision)
                                          .withAccept(accept)
                                          .withForce(force);

        if (credentials != null) {
            request.setUsername(credentials.getUsername());
            request.setPassword(credentials.getPassword());
        }

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/switch", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputWithRevisionResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> showLog(Path project, Path[] paths, String revision) {
        final String url = getBaseUrl() + "/showlog";
        final ShowLogRequest request = dtoFactory.createDto(ShowLogRequest.class)
                                                 .withProjectPath(project.toString())
                                                 .withPaths(toList(paths))
                                                 .withRevision(revision);
        return asyncRequestFactory.createPostRequest(url, request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> lock(Path project, Path[] paths, boolean force, Credentials credentials) {
        final String url = getBaseUrl() + "/lock";
        final LockRequest request = dtoFactory.createDto(LockRequest.class)
                                              .withProjectPath(project.toString())
                                              .withTargets(toList(paths))
                                              .withForce(force);
        if (credentials != null) {
            request.setUsername(credentials.getUsername());
            request.setPassword(credentials.getPassword());
        }

        return asyncRequestFactory.createPostRequest(url, request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> unlock(Path project, Path[] paths, boolean force, Credentials credentials) {
        final String url = getBaseUrl() + "/unlock";
        final LockRequest request = dtoFactory.createDto(LockRequest.class)
                                              .withProjectPath(project.toString())
                                              .withTargets(toList(paths))
                                              .withForce(force);
        if (credentials != null) {
            request.setUsername(credentials.getUsername());
            request.setPassword(credentials.getPassword());
        }

        return asyncRequestFactory.createPostRequest(url, request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> showDiff(Path project, Path[] paths, String revision, Credentials credentials) {
        final String url = getBaseUrl() + "/showdiff";
        final ShowDiffRequest request = dtoFactory.createDto(ShowDiffRequest.class)
                                                  .withProjectPath(project.toString())
                                                  .withPaths(toList(paths))
                                                  .withRevision(revision);
        if (credentials != null) {
            request.setUsername(credentials.getUsername());
            request.setPassword(credentials.getPassword());
        }

        return asyncRequestFactory.createPostRequest(url, request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputWithRevisionResponse> commit(Path project, Path[] paths, String message, boolean keepChangeLists,
                                                         boolean keepLocks) {
        final String url = getBaseUrl() + "/commit";
        final CommitRequest request = dtoFactory.createDto(CommitRequest.class)
                                                .withPaths(toList(paths))
                                                .withMessage(message)
                                                .withProjectPath(project.toString())
                                                .withKeepChangeLists(keepChangeLists)
                                                .withKeepLocks(keepLocks);
        return asyncRequestFactory.createPostRequest(url, request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputWithRevisionResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> cleanup(Path project, Path[] paths) {
        final String url = getBaseUrl() + "/cleanup";
        final CleanupRequest request = dtoFactory.createDto(CleanupRequest.class)
                                                 .withPaths(toList(paths))
                                                 .withProjectPath(project.toString());
        return asyncRequestFactory.createPostRequest(url, request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> showConflicts(Path project, Path[] paths) {
        final StatusRequest request =
                dtoFactory.createDto(StatusRequest.class)
                          .withVerbose(false)
                          .withChangeLists(Collections.<String>emptyList())
                          .withDepth(Depth.FULLY_RECURSIVE.getValue())
                          .withIgnoreExternals(false)
                          .withPaths(toList(paths))
                          .withProjectPath(project.toString())
                          .withShowIgnored(false)
                          .withShowUnversioned(false)
                          .withShowUpdates(false);

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/status", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponseList> resolve(Path project, Map<String, String> resolutions, String depth) {
        final String url = getBaseUrl() + "/resolve";
        final ResolveRequest request = dtoFactory.createDto(ResolveRequest.class)
                                                 .withProjectPath(project.toString())
                                                 .withConflictResolutions(resolutions)
                                                 .withDepth(depth);
        return asyncRequestFactory.createPostRequest(url, request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponseList.class));
    }

    @Override
    public Promise<CLIOutputResponse> move(Path project, Path source, Path destination, String comment, Credentials credentials) {
        final MoveRequest request =
                dtoFactory.createDto(MoveRequest.class)
                          .withProjectPath(project.toString())
                          .withSource(Collections.singletonList(source.toString()))
                          .withDestination(destination.toString())
                          .withComment(comment);
        if (credentials != null) {
            request.setUsername(credentials.getUsername());
            request.setPassword(credentials.getPassword());
        }

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/move", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> propertySet(Path project, String propertyName, String propertyValues, Depth depth, boolean force,
                                                  Path path) {
        final PropertyRequest request =
                dtoFactory.createDto(PropertySetRequest.class)
                          .withValue(propertyValues)
                          .withProjectPath(project.toString())
                          .withName(propertyName)
                          .withDepth(depth)
                          .withForce(force)
                          .withPath(path.toString());

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/propset", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> propertyDelete(Path project, String propertyName, Depth depth, boolean force, Path path) {
        final PropertyRequest request = dtoFactory.createDto(PropertyDeleteRequest.class)
                                                  .withProjectPath(project.toString())
                                                  .withName(propertyName)
                                                  .withDepth(depth)
                                                  .withForce(force)
                                                  .withPath(path.toString());

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/propdel", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<GetRevisionsResponse> getRevisions(Path project, Path path, String revisionRange) {
        final GetRevisionsRequest request = dtoFactory.createDto(GetRevisionsRequest.class)
                                                      .withProjectPath(project.toString())
                                                      .withPath(path.toString())
                                                      .withRevisionRange(revisionRange);
        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/revisions", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(GetRevisionsResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> propertyGet(Path project, String propertyName, Path path) {
        final PropertyRequest request = dtoFactory.createDto(PropertyGetRequest.class)
                                                  .withProjectPath(project.toString())
                                                  .withName(propertyName)
                                                  .withPath(path.toString());

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/propget", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> propertyList(Path project, Path path) {
        final PropertyRequest request = dtoFactory.createDto(PropertyListRequest.class)
                                                  .withProjectPath(project.toString())
                                                  .withPath(path.toString());

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/proplist", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> list(Path project, String target, @Nullable Credentials credentials) {
        ListRequest request = dtoFactory.createDto(ListRequest.class)
                                        .withProjectPath(project.toString())
                                        .withTargetPath(target);
        if (credentials != null) {
            request.setUsername(credentials.getUsername());
            request.setPassword(credentials.getPassword());
        }

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/list", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> listBranches(Path project, @Nullable Credentials credentials) {
        ListRequest request = dtoFactory.createDto(ListRequest.class)
                                        .withProjectPath(project.toString());
        if (credentials != null) {
            request.setUsername(credentials.getUsername());
            request.setPassword(credentials.getPassword());
        }

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/branches", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }

    @Override
    public Promise<CLIOutputResponse> listTags(Path project, @Nullable Credentials credentials) {
        ListRequest request = dtoFactory.createDto(ListRequest.class)
                                        .withProjectPath(project.toString());
        if (credentials != null) {
            request.setUsername(credentials.getUsername());
            request.setPassword(credentials.getPassword());
        }

        return asyncRequestFactory.createPostRequest(getBaseUrl() + "/tags", request)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class));
    }
}
