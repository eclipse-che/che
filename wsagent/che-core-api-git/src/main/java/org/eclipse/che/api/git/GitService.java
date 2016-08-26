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
package org.eclipse.che.api.git;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CheckoutParams;
import org.eclipse.che.api.git.params.CloneParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.params.DiffParams;
import org.eclipse.che.api.git.params.FetchParams;
import org.eclipse.che.api.git.params.LogParams;
import org.eclipse.che.api.git.params.PullParams;
import org.eclipse.che.api.git.params.PushParams;
import org.eclipse.che.api.git.params.RemoteAddParams;
import org.eclipse.che.api.git.params.RemoteUpdateParams;
import org.eclipse.che.api.git.params.ResetParams;
import org.eclipse.che.api.git.params.RmParams;
import org.eclipse.che.api.git.params.TagCreateParams;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchCreateRequest;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.CloneRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.Commiters;
import org.eclipse.che.api.git.shared.ConfigRequest;
import org.eclipse.che.api.git.shared.DiffType;
import org.eclipse.che.api.git.shared.FetchRequest;
import org.eclipse.che.api.git.shared.MergeRequest;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.MoveRequest;
import org.eclipse.che.api.git.shared.PullRequest;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.PushRequest;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.RebaseRequest;
import org.eclipse.che.api.git.shared.RebaseResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.RemoteAddRequest;
import org.eclipse.che.api.git.shared.RemoteUpdateRequest;
import org.eclipse.che.api.git.shared.RepoInfo;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.RmRequest;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.api.git.shared.Tag;
import org.eclipse.che.api.git.shared.TagCreateRequest;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/** @author andrew00x */
@Path("git")
public class GitService {

    private static final Logger LOG = LoggerFactory.getLogger(GitService.class);

    @Inject
    private GitConnectionFactory gitConnectionFactory;

    @Inject
    private ProjectRegistry projectRegistry;

    @QueryParam("projectPath")
    private String projectPath;

    @POST
    @Path("add")
    @Consumes(MediaType.APPLICATION_JSON)
    public void add(AddRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            AddParams params = AddParams.create(request.getFilePattern())
                                        .withAttributes(request.getAttributes())
                                        .withUpdate(request.isUpdate());
            gitConnection.add(params);
        }
    }

    @POST
    @Path("checkout")
    @Consumes(MediaType.APPLICATION_JSON)
    public void checkout(CheckoutRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            CheckoutParams params = CheckoutParams.create(request.getName())
                                                  .withFiles(request.getFiles())
                                                  .withCreateNew(request.isCreateNew())
                                                  .withNoTrack(request.isNoTrack())
                                                  .withTrackBranch(request.getTrackBranch())
                                                  .withStartPoint(request.getStartPoint());
            gitConnection.checkout(params);
        }
    }

    @POST
    @Path("branch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Branch branchCreate(BranchCreateRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.branchCreate(request.getName(), request.getStartPoint());
        }
    }

    @DELETE
    @Path("branch")
    public void branchDelete(@QueryParam("name") String name, @QueryParam("force") boolean force) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.branchDelete(name, force);
        }
    }

    @POST
    @Path("branch")
    public void branchRename(@QueryParam("oldName") String oldName,
                             @QueryParam("newName") String newName) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.branchRename(oldName, newName);
        }
    }

    @GET
    @Path("branch")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Branch> branchList(@QueryParam("listMode") String listMode) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.branchList(listMode == null ? null : BranchListMode.valueOf(listMode));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }

    @POST
    @Path("clone")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RepoInfo clone(final CloneRequest request) throws URISyntaxException, ApiException {
        long start = System.currentTimeMillis();
        // On-the-fly resolving of repository's working directory.
        CloneParams params = CloneParams.create(request.getRemoteUri())
                                        .withWorkingDir(getAbsoluteProjectPath(request.getWorkingDir()))
                                        .withBranchesToFetch(request.getBranchesToFetch())
                                        .withRemoteName(request.getRemoteName())
                                        .withTimeout(request.getTimeout());

        LOG.info("Repository clone from '" + request.getRemoteUri() + "' to '" + request.getWorkingDir() + "' started");
        GitConnection gitConnection = getGitConnection();
        try {
            gitConnection.clone(params);
            return newDto(RepoInfo.class).withRemoteUri(request.getRemoteUri());
        } finally {
            long end = System.currentTimeMillis();
            long seconds = (end - start) / 1000;
            LOG.info("Repository clone from '" + request.getRemoteUri() + "' to '" + request.getWorkingDir()
                     + "' finished. Process took " + seconds + " seconds (" + seconds / 60 + " minutes)");
            gitConnection.close();
        }
    }

    @POST
    @Path("commit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Revision commit(CommitRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            CommitParams params = CommitParams.create(request.getMessage())
                                              .withFiles(request.getFiles())
                                              .withAll(request.isAll())
                                              .withAmend(request.isAmend());
            return gitConnection.commit(params);
        }
    }

    @GET
    @Path("diff")
    @Produces(MediaType.TEXT_PLAIN)
    public InfoPage diff(@QueryParam("fileFilter") List<String> fileFilter,
                         @QueryParam("diffType") String diffType,
                         @QueryParam("noRenames") boolean noRenames,
                         @QueryParam("renameLimit") int renameLimit,
                         @QueryParam("commitA") String commitA,
                         @QueryParam("commitB") String commitB,
                         @QueryParam("cached") boolean cached) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            DiffParams params = DiffParams.create()
                                          .withFileFilter(fileFilter)
                                          .withType(diffType == null ? null : DiffType.valueOf(diffType))
                                          .withNoRenames(noRenames)
                                          .withRenameLimit(renameLimit)
                                          .withCommitA(commitA)
                                          .withCommitB(commitB)
                                          .withCached(cached);
            return gitConnection.diff(params);
        }
    }

    @GET
    @Path("show")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public ShowFileContentResponse showFileContent(@QueryParam("file") String file, @QueryParam("version") String version)
            throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.showFileContent(file, version);
        }
    }

    @POST
    @Path("fetch")
    @Consumes(MediaType.APPLICATION_JSON)
    public void fetch(FetchRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            FetchParams params = FetchParams.create(request.getRemote())
                                            .withRefSpec(request.getRefSpec())
                                            .withTimeout(request.getTimeout())
                                            .withRemoveDeletedRefs(request.isRemoveDeletedRefs());
            gitConnection.fetch(params);
        }
    }

    @POST
    @Path("init")
    public void init(@QueryParam("bare") boolean bare) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.init(bare);
        }
        projectRegistry.setProjectType(projectPath, GitProjectType.TYPE_ID, true);
    }

    @DELETE
    @Path("repository")
    public void deleteRepository(@Context UriInfo uriInfo) throws ApiException {
        final RegisteredProject project = projectRegistry.getProject(projectPath);
        final FolderEntry gitFolder = project.getBaseFolder().getChildFolder(".git");
        gitFolder.getVirtualFile().delete();
        projectRegistry.removeProjectType(projectPath, GitProjectType.TYPE_ID);
    }

    @GET
    @Path("log")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public LogPage log(@QueryParam("fileFilter") List<String> fileFilter,
                       @QueryParam("since") String revisionRangeSince,
                       @QueryParam("until") String revisionRangeUntil) throws ApiException {
        LogParams params = LogParams.create()
                                    .withFileFilter(fileFilter)
                                    .withRevisionRangeSince(revisionRangeSince)
                                    .withRevisionRangeUntil(revisionRangeUntil);
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.log(params);
        }
    }

    @POST
    @Path("merge")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public MergeResult merge(MergeRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.merge(request.getCommit());
        }
    }

    @POST
    @Path("rebase")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RebaseResponse rebase(RebaseRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.rebase(request.getOperation(), request.getBranch());
        }
    }

    @POST
    @Path("move")
    @Consumes(MediaType.APPLICATION_JSON)
    public void mv(MoveRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.mv(request.getSource(), request.getTarget());
        }
    }

    @POST
    @Path("remove")
    @Consumes(MediaType.APPLICATION_JSON)
    public void rm(RmRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            RmParams params = RmParams.create(request.getItems())
                                      .withRecursively(request.isRecursively())
                                      .withCached(request.isCached());
            gitConnection.rm(params);
        }
    }

    @POST
    @Path("pull")
    @Consumes(MediaType.APPLICATION_JSON)
    public PullResponse pull(PullRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            PullParams params = PullParams.create(request.getRemote())
                                          .withRefSpec(request.getRefSpec())
                                          .withTimeout(request.getTimeout());
            return gitConnection.pull(params);
        }
    }

    @POST
    @Path("push")
    @Consumes(MediaType.APPLICATION_JSON)
    public PushResponse push(PushRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            PushParams params = PushParams.create(request.getRemote())
                                          .withRefSpec(request.getRefSpec())
                                          .withForce(request.isForce())
                                          .withTimeout(request.getTimeout());
            return gitConnection.push(params);
        }
    }

    @PUT
    @Path("remote")
    @Consumes(MediaType.APPLICATION_JSON)
    public void remoteAdd(RemoteAddRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            RemoteAddParams params = RemoteAddParams.create(request.getName(), request.getUrl()).withBranches(request.getBranches());
            gitConnection.remoteAdd(params);
        }
    }

    @DELETE
    @Path("remote/{name}")
    public void remoteDelete(@PathParam("name") String name) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.remoteDelete(name);
        }
    }

    @GET
    @Path("remote")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public GenericEntity<List<Remote>> remoteList(@QueryParam("remoteName") String remoteName, @QueryParam("verbose") boolean verbose)
            throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return new GenericEntity<List<Remote>>(gitConnection.remoteList(remoteName, verbose)) {
            };
        }
    }

    @POST
    @Path("remote")
    @Consumes(MediaType.APPLICATION_JSON)
    public void remoteUpdate(RemoteUpdateRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            RemoteUpdateParams params = RemoteUpdateParams.create(request.getName())
                                                          .withRemoveUrl(request.getRemoveUrl())
                                                          .withRemovePushUrl(request.getRemovePushUrl())
                                                          .withAddUrl(request.getAddUrl())
                                                          .withAddPushUrl(request.getAddPushUrl())
                                                          .withBranches(request.getBranches())
                                                          .withAddBranches(request.isAddBranches());
            gitConnection.remoteUpdate(params);
        }
    }

    @POST
    @Path("reset")
    @Consumes(MediaType.APPLICATION_JSON)
    public void reset(ResetRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            ResetParams params = ResetParams.create(request.getCommit(), request.getType()).withFilePattern(request.getFilePattern());
            gitConnection.reset(params);
        }
    }

    @GET
    @Path("status")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Status status(@QueryParam("format") StatusFormat format) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return gitConnection.status(format);
        }
    }

    @POST
    @Path("tag")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Tag tagCreate(TagCreateRequest request) throws ApiException {
        GitConnection gitConnection = getGitConnection();
        try {
            TagCreateParams params = TagCreateParams.create(request.getName())
                                                    .withCommit(request.getCommit())
                                                    .withMessage(request.getMessage())
                                                    .withForce(request.isForce());
            return gitConnection.tagCreate(params);
        } finally {
            gitConnection.close();
        }
    }

    @DELETE
    @Path("tag/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void tagDelete(@PathParam("name") String name) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            gitConnection.tagDelete(name);
        }
    }


    @GET
    @Path("tag")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public GenericEntity<List<Tag>> tagList(@QueryParam("pattern") String pattern) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return new GenericEntity<List<Tag>>(gitConnection.tagList(pattern)) {
            };
        }
    }

    @GET
    @Path("config")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getConfig(@QueryParam("requestedConfig") List<String> requestedConfig)
            throws ApiException {
        Map<String, String> result = new HashMap<>();
        try (GitConnection gitConnection = getGitConnection()) {
            Config config = gitConnection.getConfig();
            if (requestedConfig == null || requestedConfig.isEmpty()) {
                for (String row : config.getList()) {
                    String[] keyValues = row.split("=", 2);
                    result.put(keyValues[0], keyValues[1]);
                }
            } else {
                for (String entry : requestedConfig) {
                    try {
                        String value = config.get(entry);
                        result.put(entry, value);
                    } catch (GitException exception) {
                        //value for this config property non found. Do nothing
                    }
                }
            }
        }
        return result;
    }

    @PUT
    @Path("config")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setConfig(ConfigRequest request) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            Config config = gitConnection.getConfig();
            for (Map.Entry<String, String> configData : request.getConfigEntries().entrySet()) {
                try {
                    config.set(configData.getKey(), configData.getValue());
                } catch (GitException exception) {
                    final String msg = "Cannot write to config file";
                    LOG.error(msg, exception);
                    throw new GitException(msg);
                }
            }
        }
    }

    @DELETE
    @Path("config")
    public void unsetConfig(@QueryParam("requestedConfig") List<String> requestedConfig) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            Config config = gitConnection.getConfig();
            if (requestedConfig != null && !requestedConfig.isEmpty()) {
                for (String entry : requestedConfig) {
                    try {
                        config.unset(entry);
                    } catch (GitException exception) {
                        //value for this config property non found. Do nothing
                    }
                }
            }
        }
    }

    @GET
    @Path("commiters")
    public Commiters getCommiters(@Context UriInfo uriInfo) throws ApiException {
        try (GitConnection gitConnection = getGitConnection()) {
            return newDto(Commiters.class).withCommiters(gitConnection.getCommiters());
        }
    }

    private String getAbsoluteProjectPath(String wsRelatedProjectPath) throws ApiException {
        final RegisteredProject project = projectRegistry.getProject(wsRelatedProjectPath);
        return project.getBaseFolder().getVirtualFile().toIoFile().getAbsolutePath();
    }

    private GitConnection getGitConnection() throws ApiException {
        return gitConnectionFactory.getConnection(getAbsoluteProjectPath(projectPath));
    }
}
