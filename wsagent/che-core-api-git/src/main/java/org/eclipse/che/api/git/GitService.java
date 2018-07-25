/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.git;

import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.PathTransformer;
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
import org.eclipse.che.api.git.shared.Constants;
import org.eclipse.che.api.git.shared.DiffType;
import org.eclipse.che.api.git.shared.EditedRegion;
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
import org.eclipse.che.api.git.shared.RevertRequest;
import org.eclipse.che.api.git.shared.RevertResult;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.Tag;
import org.eclipse.che.api.git.shared.TagCreateRequest;
import org.eclipse.che.api.git.shared.event.GitRepositoryDeletedEvent;
import org.eclipse.che.api.project.server.ProjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines Git REST API.
 *
 * @author andrew00x
 * @author Igor Vinokur
 */
@Path("git")
public class GitService {

  private static final Logger LOG = LoggerFactory.getLogger(GitService.class);

  @Inject private GitConnectionFactory gitConnectionFactory;

  @Inject private ProjectManager projectManager;

  @Inject private EventService eventService;

  @Inject private FsManager fsManager;

  @Inject private PathTransformer pathTransformer;

  @QueryParam("projectPath")
  private String projectPath;

  @POST
  @Path("add")
  @Consumes(MediaType.APPLICATION_JSON)
  public void add(AddRequest request) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      gitConnection.add(AddParams.create(request.getFilePattern()).withUpdate(request.isUpdate()));
    }
  }

  @POST
  @Path("checkout")
  @Consumes(MediaType.APPLICATION_JSON)
  public void checkout(CheckoutRequest request) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      gitConnection.checkout(
          CheckoutParams.create(request.getName())
              .withFiles(request.getFiles())
              .withCreateNew(request.isCreateNew())
              .withNoTrack(request.isNoTrack())
              .withTrackBranch(request.getTrackBranch())
              .withStartPoint(request.getStartPoint()));
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
  public void branchDelete(@QueryParam("name") String name, @QueryParam("force") boolean force)
      throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      gitConnection.branchDelete(name, force);
    }
  }

  @POST
  @Path("branch")
  public void branchRename(
      @QueryParam("oldName") String oldName, @QueryParam("newName") String newName)
      throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      gitConnection.branchRename(oldName, newName);
    }
  }

  @GET
  @Path("branch")
  @Produces(MediaType.APPLICATION_JSON)
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
    LOG.info(
        "Repository clone from '"
            + request.getRemoteUri()
            + "' to '"
            + request.getWorkingDir()
            + "' started");
    GitConnection gitConnection = getGitConnection();
    try {
      gitConnection.clone(
          CloneParams.create(request.getRemoteUri())
              // On-the-fly resolving of repository's working directory.
              .withWorkingDir(getAbsoluteProjectPath(request.getWorkingDir()))
              .withBranchesToFetch(request.getBranchesToFetch())
              .withRemoteName(request.getRemoteName())
              .withTimeout(request.getTimeout())
              .withUsername(request.getUsername())
              .withPassword(request.getPassword()));
      return newDto(RepoInfo.class).withRemoteUri(request.getRemoteUri());
    } finally {
      long end = System.currentTimeMillis();
      long seconds = (end - start) / 1000;
      LOG.info(
          "Repository clone from '"
              + request.getRemoteUri()
              + "' to '"
              + request.getWorkingDir()
              + "' finished. Process took "
              + seconds
              + " seconds ("
              + seconds / 60
              + " minutes)");
      gitConnection.close();
    }
  }

  @POST
  @Path("commit")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  public Revision commit(CommitRequest request) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      return gitConnection.commit(
          CommitParams.create(request.getMessage())
              .withFiles(request.getFiles())
              .withAll(request.isAll())
              .withAmend(request.isAmend()));
    }
  }

  @GET
  @Path("diff")
  @Produces(MediaType.TEXT_PLAIN)
  public InfoPage diff(
      @QueryParam("fileFilter") List<String> fileFilter,
      @QueryParam("diffType") String diffType,
      @QueryParam("noRenames") boolean noRenames,
      @QueryParam("renameLimit") int renameLimit,
      @QueryParam("commitA") String commitA,
      @QueryParam("commitB") String commitB,
      @QueryParam("cached") boolean cached)
      throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      return gitConnection.diff(
          DiffParams.create()
              .withFileFilter(fileFilter)
              .withType(diffType == null ? null : DiffType.valueOf(diffType))
              .withNoRenames(noRenames)
              .withRenameLimit(renameLimit)
              .withCommitA(commitA)
              .withCommitB(commitB)
              .withCached(cached));
    }
  }

  @GET
  @Path("edits")
  @Produces(MediaType.APPLICATION_JSON)
  public List<EditedRegion> getEditedRegions(@Required @QueryParam("filePath") String file)
      throws ApiException {
    requiredNotNull(file, "File path");
    try (GitConnection gitConnection = getGitConnection()) {
      return gitConnection.getEditedRegions(file);
    }
  }

  @GET
  @Path("show")
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  public ShowFileContentResponse showFileContent(
      @QueryParam("file") String file, @QueryParam("version") String version) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      return gitConnection.showFileContent(file, version);
    }
  }

  @POST
  @Path("fetch")
  @Consumes(MediaType.APPLICATION_JSON)
  public void fetch(FetchRequest request) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      gitConnection.fetch(
          FetchParams.create(request.getRemote())
              .withRefSpec(request.getRefSpec())
              .withTimeout(request.getTimeout())
              .withRemoveDeletedRefs(request.isRemoveDeletedRefs())
              .withUsername(request.getUsername())
              .withPassword(request.getPassword()));
    }
  }

  @POST
  @Path("init")
  public void init(@QueryParam("bare") boolean bare) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      gitConnection.init(bare);
    }
    projectManager.setType(projectPath, GitProjectType.TYPE_ID, true);
  }

  @DELETE
  @Path("repository")
  public void deleteRepository(@Context UriInfo uriInfo) throws ApiException {
    ProjectConfig project =
        projectManager
            .get(projectPath)
            .orElseThrow(() -> new NotFoundException("Can't find project"));

    String dotGitWsPath = resolve(absolutize(projectPath), ".git");
    fsManager.delete(dotGitWsPath);
    eventService.publish(
        newDto(GitRepositoryDeletedEvent.class)
            .withProjectName(project.getName())
            .withProjectPath(projectPath));
  }

  @GET
  @Path("log")
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  public LogPage log(
      @QueryParam("fileFilter") List<String> fileFilter,
      @QueryParam("since") String revisionRangeSince,
      @QueryParam("until") String revisionRangeUntil,
      @QueryParam("skip") @DefaultValue("0") int skip,
      @QueryParam("maxCount") @DefaultValue(Constants.DEFAULT_PAGE_SIZE_QUERY_PARAM) int maxCount)
      throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      return gitConnection.log(
          LogParams.create()
              .withFileFilter(fileFilter)
              .withRevisionRangeSince(revisionRangeSince)
              .withRevisionRangeUntil(revisionRangeUntil)
              .withMaxCount(maxCount)
              .withSkip(skip));
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
  public void move(MoveRequest request) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      gitConnection.mv(request.getSource(), request.getTarget());
    }
  }

  @DELETE
  @Path("remove")
  public void remove(@QueryParam("items") List<String> items, @QueryParam("cached") boolean cached)
      throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      gitConnection.rm(RmParams.create(items).withCached(cached));
    }
  }

  @POST
  @Path("pull")
  @Consumes(MediaType.APPLICATION_JSON)
  public PullResponse pull(PullRequest request) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      return gitConnection.pull(
          PullParams.create(request.getRemote())
              .withRefSpec(request.getRefSpec())
              .withRebase(request.getRebase())
              .withTimeout(request.getTimeout())
              .withUsername(request.getUsername())
              .withPassword(request.getPassword()));
    }
  }

  @POST
  @Path("push")
  @Consumes(MediaType.APPLICATION_JSON)
  public PushResponse push(PushRequest request) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      return gitConnection.push(
          PushParams.create(request.getRemote())
              .withRefSpec(request.getRefSpec())
              .withForce(request.isForce())
              .withTimeout(request.getTimeout())
              .withUsername(request.getUsername())
              .withPassword(request.getPassword()));
    }
  }

  @PUT
  @Path("remote")
  @Consumes(MediaType.APPLICATION_JSON)
  public void remoteAdd(RemoteAddRequest request) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      gitConnection.remoteAdd(
          RemoteAddParams.create(request.getName(), request.getUrl())
              .withBranches(request.getBranches()));
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
  @Produces(MediaType.APPLICATION_JSON)
  public List<Remote> remoteList(
      @QueryParam("remoteName") String remoteName, @QueryParam("verbose") boolean verbose)
      throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      return gitConnection.remoteList(remoteName, verbose);
    }
  }

  @POST
  @Path("remote")
  @Consumes(MediaType.APPLICATION_JSON)
  public void remoteUpdate(RemoteUpdateRequest request) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      gitConnection.remoteUpdate(
          RemoteUpdateParams.create(request.getName())
              .withRemoveUrl(request.getRemoveUrl())
              .withRemovePushUrl(request.getRemovePushUrl())
              .withAddUrl(request.getAddUrl())
              .withAddPushUrl(request.getAddPushUrl())
              .withBranches(request.getBranches())
              .withAddBranches(request.isAddBranches()));
    }
  }

  @POST
  @Path("reset")
  @Consumes(MediaType.APPLICATION_JSON)
  public void reset(ResetRequest request) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      gitConnection.reset(
          ResetParams.create(request.getCommit(), request.getType())
              .withFilePattern(request.getFilePattern()));
    }
  }

  @POST
  @Path("revert")
  @Consumes(MediaType.APPLICATION_JSON)
  public RevertResult revert(RevertRequest request) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      return gitConnection.revert(request.getCommit());
    }
  }

  @GET
  @Path("status")
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  public Status status(@QueryParam("filter") List<String> filter) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      return gitConnection.status(filter);
    }
  }

  @POST
  @Path("tag")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Tag tagCreate(TagCreateRequest request) throws ApiException {
    GitConnection gitConnection = getGitConnection();
    try {
      return gitConnection.tagCreate(
          TagCreateParams.create(request.getName())
              .withCommit(request.getCommit())
              .withMessage(request.getMessage())
              .withForce(request.isForce()));
    } finally {
      gitConnection.close();
    }
  }

  @DELETE
  @Path("tag/{name}")
  public void tagDelete(@PathParam("name") String name) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      gitConnection.tagDelete(name);
    }
  }

  @GET
  @Path("tag")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Tag> tagList(@QueryParam("pattern") String pattern) throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      return gitConnection.tagList(pattern);
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
            // value for this config property non found. Do nothing
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
  public void unsetConfig(@QueryParam("requestedConfig") List<String> requestedConfig)
      throws ApiException {
    try (GitConnection gitConnection = getGitConnection()) {
      Config config = gitConnection.getConfig();
      if (requestedConfig != null && !requestedConfig.isEmpty()) {
        for (String entry : requestedConfig) {
          try {
            config.unset(entry);
          } catch (GitException exception) {
            // value for this config property non found. Do nothing
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
    return pathTransformer.transform(wsRelatedProjectPath).toString();
  }

  private GitConnection getGitConnection() throws ApiException {
    return gitConnectionFactory.getConnection(getAbsoluteProjectPath(projectPath));
  }

  /**
   * Checks object reference is not {@code null}
   *
   * @param object object reference to check
   * @param subject used as subject of exception message "{subject} required"
   * @throws BadRequestException when object reference is {@code null}
   */
  private void requiredNotNull(Object object, String subject) throws BadRequestException {
    if (object == null) {
      throw new BadRequestException(subject + " required");
    }
  }
}
