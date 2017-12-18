/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.git.shared.AddRequest.DEFAULT_PATTERN;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.MimeType.TEXT_PLAIN;
import static org.eclipse.che.ide.resource.Path.valueOf;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENTTYPE;
import static org.eclipse.che.ide.util.PathEncoder.encodePath;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchCreateRequest;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.CloneRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.DiffType;
import org.eclipse.che.api.git.shared.EditedRegion;
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
import org.eclipse.che.api.git.shared.RevertRequest;
import org.eclipse.che.api.git.shared.RevertResult;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

/**
 * Implementation of the {@link GitServiceClient}.
 *
 * @author Ann Zhuleva
 * @author Valeriy Svydenko
 */
@Singleton
public class GitServiceClientImpl implements GitServiceClient {
  private static final String ADD = "/git/add";
  private static final String BRANCH = "/git/branch";
  private static final String CHECKOUT = "/git/checkout";
  private static final String CLONE = "/git/clone";
  private static final String COMMIT = "/git/commit";
  private static final String CONFIG = "/git/config";
  private static final String DIFF = "/git/diff";
  private static final String EDITS = "/git/edits";
  private static final String FETCH = "/git/fetch";
  private static final String INIT = "/git/init";
  private static final String LOG = "/git/log";
  private static final String SHOW = "/git/show";
  private static final String MERGE = "/git/merge";
  private static final String STATUS = "/git/status";
  private static final String PUSH = "/git/push";
  private static final String PULL = "/git/pull";
  private static final String REMOTE = "/git/remote";
  private static final String REMOVE = "/git/remove";
  private static final String RESET = "/git/reset";
  private static final String REPOSITORY = "/git/repository";
  private static final String REVERT = "/git/revert";

  /** Loader to be displayed. */
  private final AsyncRequestLoader loader;

  private final DtoFactory dtoFactory;
  private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
  private final AsyncRequestFactory asyncRequestFactory;
  private final AppContext appContext;

  @Inject
  protected GitServiceClientImpl(
      LoaderFactory loaderFactory,
      DtoFactory dtoFactory,
      AsyncRequestFactory asyncRequestFactory,
      DtoUnmarshallerFactory dtoUnmarshallerFactory,
      AppContext appContext) {
    this.appContext = appContext;
    this.loader = loaderFactory.newLoader();
    this.dtoFactory = dtoFactory;
    this.asyncRequestFactory = asyncRequestFactory;
    this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
  }

  @Override
  public Promise<Void> init(Path project, boolean bare) {
    String url =
        getWsAgentBaseUrl() + INIT + "?projectPath=" + encodePath(project) + "&bare=" + bare;
    return asyncRequestFactory.createPostRequest(url, null).loader(loader).send();
  }

  @Override
  public Promise<Void> clone(Path project, String remoteUri, String remoteName) {
    CloneRequest cloneRequest =
        dtoFactory
            .createDto(CloneRequest.class)
            .withRemoteName(remoteName)
            .withRemoteUri(remoteUri)
            .withWorkingDir(project.toString());

    String params = "?projectPath=" + encodePath(project);
    String url = CLONE + params;

    return asyncRequestFactory.createPostRequest(url, cloneRequest).loader(loader).send();
  }

  @Override
  public Promise<String> statusText(Path project) {
    String params = "?projectPath=" + encodePath(project);
    String url = getWsAgentBaseUrl() + STATUS + params;

    return asyncRequestFactory
        .createGetRequest(url)
        .loader(loader)
        .header(CONTENTTYPE, APPLICATION_JSON)
        .header(ACCEPT, TEXT_PLAIN)
        .send(new StringUnmarshaller());
  }

  @Override
  public Promise<Void> add(Path project, boolean update, Path[] paths) {
    AddRequest addRequest = dtoFactory.createDto(AddRequest.class).withUpdate(update);
    addRequest.setFilePattern(
        paths == null
            ? DEFAULT_PATTERN
            : stream(paths).map(path -> path.isEmpty() ? "." : path.toString()).collect(toList()));
    String url = getWsAgentBaseUrl() + ADD + "?projectPath=" + encodePath(project);
    return asyncRequestFactory.createPostRequest(url, addRequest).loader(loader).send();
  }

  @Override
  public Promise<Revision> commit(Path project, String message, boolean all, boolean amend) {
    CommitRequest commitRequest =
        dtoFactory
            .createDto(CommitRequest.class)
            .withMessage(message)
            .withAmend(amend)
            .withAll(all);
    String url = getWsAgentBaseUrl() + COMMIT + "?projectPath=" + encodePath(project);
    return asyncRequestFactory
        .createPostRequest(url, commitRequest)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newUnmarshaller(Revision.class));
  }

  @Override
  public Promise<Revision> commit(Path project, String message, boolean amend, Path[] files) {
    CommitRequest commitRequest =
        dtoFactory
            .createDto(CommitRequest.class)
            .withMessage(message)
            .withAmend(amend)
            .withFiles(
                stream(files)
                    .filter(file -> !file.isEmpty())
                    .map(Path::toString)
                    .collect(toList()));

    String url = getWsAgentBaseUrl() + COMMIT + "?projectPath=" + encodePath(project);

    return asyncRequestFactory
        .createPostRequest(url, commitRequest)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newUnmarshaller(Revision.class));
  }

  @Override
  public Promise<Map<String, String>> config(Path project, List<String> requestedConfig) {
    String params = "?projectPath=" + encodePath(project);
    if (requestedConfig != null) {
      for (String entry : requestedConfig) {
        params += "&requestedConfig=" + entry;
      }
    }
    String url = getWsAgentBaseUrl() + CONFIG + params;
    return asyncRequestFactory
        .createGetRequest(url)
        .loader(loader)
        .send(new StringMapUnmarshaller());
  }

  @Override
  public Promise<PushResponse> push(
      Path project, List<String> refSpec, String remote, boolean force) {
    PushRequest pushRequest =
        dtoFactory
            .createDto(PushRequest.class)
            .withRemote(remote)
            .withRefSpec(refSpec)
            .withForce(force);
    String url = getWsAgentBaseUrl() + PUSH + "?projectPath=" + encodePath(project);
    return asyncRequestFactory
        .createPostRequest(url, pushRequest)
        .send(dtoUnmarshallerFactory.newUnmarshaller(PushResponse.class));
  }

  @Override
  public Promise<List<Remote>> remoteList(Path project, String remoteName, boolean verbose) {
    String params =
        "?projectPath="
            + encodePath(project)
            + (remoteName != null ? "&remoteName=" + remoteName : "")
            + "&verbose="
            + String.valueOf(verbose);
    String url = getWsAgentBaseUrl() + REMOTE + params;
    return asyncRequestFactory
        .createGetRequest(url)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newListUnmarshaller(Remote.class));
  }

  @Override
  public Promise<List<Branch>> branchList(Path project, BranchListMode listMode) {
    String url =
        getWsAgentBaseUrl()
            + BRANCH
            + "?projectPath="
            + encodePath(project)
            + (listMode == null ? "" : "&listMode=" + listMode);
    return asyncRequestFactory
        .createGetRequest(url)
        .send(dtoUnmarshallerFactory.newListUnmarshaller(Branch.class));
  }

  @Override
  public Promise<Status> getStatus(Path project, List<String> filter) {
    StringBuilder params = new StringBuilder("?projectPath=" + encodePath(project));
    if (filter != null) {
      for (String path : filter) {
        if (!path.isEmpty()) {
          params.append("&filter=").append(path);
        }
      }
    }
    String url = getWsAgentBaseUrl() + STATUS + params;
    return asyncRequestFactory
        .createGetRequest(url)
        .loader(loader)
        .header(CONTENTTYPE, APPLICATION_JSON)
        .header(ACCEPT, APPLICATION_JSON)
        .send(dtoUnmarshallerFactory.newUnmarshaller(Status.class));
  }

  @Override
  public Promise<Void> branchDelete(Path project, String name, boolean force) {
    String url =
        getWsAgentBaseUrl()
            + BRANCH
            + "?projectPath="
            + encodePath(project)
            + "&name="
            + name
            + "&force="
            + force;
    return asyncRequestFactory.createDeleteRequest(url).loader(loader).send();
  }

  @Override
  public Promise<Void> branchRename(Path project, String oldName, String newName) {
    String params =
        "?projectPath=" + encodePath(project) + "&oldName=" + oldName + "&newName=" + newName;
    String url = getWsAgentBaseUrl() + BRANCH + params;
    return asyncRequestFactory
        .createPostRequest(url, null)
        .loader(loader)
        .header(CONTENTTYPE, MimeType.APPLICATION_FORM_URLENCODED)
        .send();
  }

  @Override
  public Promise<Branch> branchCreate(Path project, String name, String startPoint) {
    BranchCreateRequest branchCreateRequest =
        dtoFactory.createDto(BranchCreateRequest.class).withName(name).withStartPoint(startPoint);
    String url = getWsAgentBaseUrl() + BRANCH + "?projectPath=" + encodePath(project);
    return asyncRequestFactory
        .createPostRequest(url, branchCreateRequest)
        .loader(loader)
        .header(ACCEPT, APPLICATION_JSON)
        .send(dtoUnmarshallerFactory.newUnmarshaller(Branch.class));
  }

  @Override
  public Promise<String> checkout(Path project, CheckoutRequest request) {
    String url = getWsAgentBaseUrl() + CHECKOUT + "?projectPath=" + encodePath(project);
    return asyncRequestFactory
        .createPostRequest(url, request)
        .loader(loader)
        .send(new StringUnmarshaller());
  }

  @Override
  public Promise<Void> remove(Path project, Path[] items, boolean cached) {
    String params = "?projectPath=" + encodePath(project);
    if (items != null) {
      for (Path item : items) {
        params += "&items=" + item.toString();
      }
    }
    params += "&cached=" + String.valueOf(cached);
    String url = getWsAgentBaseUrl() + REMOVE + params;
    return asyncRequestFactory.createDeleteRequest(url).loader(loader).send();
  }

  @Override
  public Promise<Void> reset(
      Path project, String commit, ResetRequest.ResetType resetType, Path[] files) {
    ResetRequest resetRequest = dtoFactory.createDto(ResetRequest.class).withCommit(commit);
    if (resetType != null) {
      resetRequest.setType(resetType);
    }
    if (files != null) {
      resetRequest.setFilePattern(
          stream(files).map(file -> file.isEmpty() ? "." : file.toString()).collect(toList()));
    }
    String url = getWsAgentBaseUrl() + RESET + "?projectPath=" + encodePath(project);
    return asyncRequestFactory.createPostRequest(url, resetRequest).loader(loader).send();
  }

  @Override
  public Promise<LogResponse> log(
      Path project, Path[] fileFilter, int skip, int maxCount, boolean plainText) {
    StringBuilder params =
        new StringBuilder()
            .append("?projectPath=")
            .append(encodePath(project))
            .append("&skip=")
            .append(skip)
            .append("&maxCount=")
            .append(maxCount);
    if (fileFilter != null) {
      stream(fileFilter).forEach(file -> params.append("&fileFilter=").append(file));
    }
    String url = getWsAgentBaseUrl() + LOG + params;
    if (plainText) {
      return asyncRequestFactory
          .createGetRequest(url)
          .send(dtoUnmarshallerFactory.newUnmarshaller(LogResponse.class));
    } else {
      return asyncRequestFactory
          .createGetRequest(url)
          .header(ACCEPT, APPLICATION_JSON)
          .send(dtoUnmarshallerFactory.newUnmarshaller(LogResponse.class));
    }
  }

  @Override
  public Promise<Void> remoteAdd(Path project, String name, String url) {
    RemoteAddRequest remoteAddRequest =
        dtoFactory.createDto(RemoteAddRequest.class).withName(name).withUrl(url);
    String requestUrl = getWsAgentBaseUrl() + REMOTE + "?projectPath=" + encodePath(project);
    return asyncRequestFactory.createPutRequest(requestUrl, remoteAddRequest).loader(loader).send();
  }

  @Override
  public Promise<Void> remoteDelete(Path project, String name) {
    String url = getWsAgentBaseUrl() + REMOTE + '/' + name + "?projectPath=" + encodePath(project);
    return asyncRequestFactory.createDeleteRequest(url).loader(loader).send();
  }

  @Override
  public Promise<Void> fetch(
      Path project, String remote, List<String> refspec, boolean removeDeletedRefs) {
    FetchRequest fetchRequest =
        dtoFactory
            .createDto(FetchRequest.class)
            .withRefSpec(refspec)
            .withRemote(remote)
            .withRemoveDeletedRefs(removeDeletedRefs);
    String url = getWsAgentBaseUrl() + FETCH + "?projectPath=" + encodePath(project);
    return asyncRequestFactory.createPostRequest(url, fetchRequest).send();
  }

  @Override
  public Promise<PullResponse> pull(Path project, String refSpec, String remote, boolean rebase) {
    PullRequest pullRequest =
        dtoFactory
            .createDto(PullRequest.class)
            .withRemote(remote)
            .withRefSpec(refSpec)
            .withRebase(rebase);
    String url = getWsAgentBaseUrl() + PULL + "?projectPath=" + encodePath(project);
    return asyncRequestFactory
        .createPostRequest(url, pullRequest)
        .send(dtoUnmarshallerFactory.newUnmarshaller(PullResponse.class));
  }

  @Override
  public Promise<String> diff(
      Path project,
      List<String> fileFilter,
      DiffType type,
      boolean noRenames,
      int renameLimit,
      String commitA,
      String commitB) {
    return diff(project, fileFilter, type, noRenames, renameLimit, commitA, commitB, false)
        .send(new StringUnmarshaller());
  }

  @Override
  public Promise<String> diff(
      Path project,
      List<String> files,
      DiffType type,
      boolean noRenames,
      int renameLimit,
      String commitA,
      boolean cached) {
    return diff(project, files, type, noRenames, renameLimit, commitA, null, cached)
        .send(new StringUnmarshaller());
  }

  @Override
  public Promise<List<EditedRegion>> getEditedRegions(Path project, Path filePath) {
    String url =
        getWsAgentBaseUrl()
            + EDITS
            + "?projectPath="
            + encodePath(project)
            + "&filePath="
            + encodePath(filePath);
    return asyncRequestFactory
        .createGetRequest(url)
        .send(dtoUnmarshallerFactory.newListUnmarshaller(EditedRegion.class));
  }

  private AsyncRequest diff(
      Path project,
      List<String> fileFilter,
      DiffType type,
      boolean noRenames,
      int renameLimit,
      String commitA,
      String commitB,
      boolean cached) {
    StringBuilder params =
        new StringBuilder()
            .append("?projectPath=")
            .append(encodePath(project))
            .append("&noRenames=")
            .append(noRenames)
            .append("&renameLimit=")
            .append(renameLimit)
            .append("&cached=")
            .append(cached);
    if (fileFilter != null) {
      fileFilter
          .stream()
          .filter(file -> !file.isEmpty())
          .forEach(file -> params.append("&fileFilter=").append(encodePath(valueOf(file))));
    }
    if (type != null) {
      params.append("&diffType=").append(type);
    }
    if (commitA != null) {
      params.append("&commitA=").append(commitA);
    }
    if (commitB != null) {
      params.append("&commitB=").append(commitB);
    }
    String url = getWsAgentBaseUrl() + DIFF + params;
    return asyncRequestFactory.createGetRequest(url).loader(loader);
  }

  @Override
  public Promise<ShowFileContentResponse> showFileContent(Path project, Path file, String version) {
    String params =
        "?projectPath=" + encodePath(project) + "&file=" + encodePath(file) + "&version=" + version;
    String url = getWsAgentBaseUrl() + SHOW + params;
    return asyncRequestFactory
        .createGetRequest(url)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newUnmarshaller(ShowFileContentResponse.class));
  }

  @Override
  public Promise<MergeResult> merge(Path project, String commit) {
    MergeRequest mergeRequest = dtoFactory.createDto(MergeRequest.class).withCommit(commit);
    String url = getWsAgentBaseUrl() + MERGE + "?projectPath=" + encodePath(project);
    return asyncRequestFactory
        .createPostRequest(url, mergeRequest)
        .loader(loader)
        .header(ACCEPT, APPLICATION_JSON)
        .send(dtoUnmarshallerFactory.newUnmarshaller(MergeResult.class));
  }

  @Override
  public Promise<Void> deleteRepository(Path project) {
    String url = getWsAgentBaseUrl() + REPOSITORY + "?projectPath=" + encodePath(project);
    return asyncRequestFactory.createDeleteRequest(url).loader(loader).send();
  }

  private String getWsAgentBaseUrl() {
    return appContext.getWsAgentServerApiEndpoint();
  }

  @Override
  public Promise<RevertResult> revert(Path project, String commit) {
    RevertRequest revertRequest = dtoFactory.createDto(RevertRequest.class).withCommit(commit);
    String url = getWsAgentBaseUrl() + REVERT + "?projectPath=" + project;
    return asyncRequestFactory
        .createPostRequest(url, revertRequest)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newUnmarshaller(RevertResult.class));
  }
}
