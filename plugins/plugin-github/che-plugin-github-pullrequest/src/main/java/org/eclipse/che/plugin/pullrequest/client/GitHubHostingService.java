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
package org.eclipse.che.plugin.pullrequest.client;

import static org.eclipse.che.ide.util.StringUtils.containsIgnoreCase;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.plugin.github.ide.GitHubClientService;
import org.eclipse.che.plugin.github.shared.GitHubPullRequest;
import org.eclipse.che.plugin.github.shared.GitHubPullRequestCreationInput;
import org.eclipse.che.plugin.github.shared.GitHubPullRequestList;
import org.eclipse.che.plugin.github.shared.GitHubRepository;
import org.eclipse.che.plugin.github.shared.GitHubRepositoryList;
import org.eclipse.che.plugin.github.shared.GitHubUser;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.HostingServiceTemplates;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.NoCommitsInPullRequestException;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.NoHistoryInCommonException;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.NoPullRequestException;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.NoUserForkException;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.PullRequestAlreadyExistsException;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.ServiceUtil;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import org.eclipse.che.plugin.pullrequest.shared.dto.HostUser;
import org.eclipse.che.plugin.pullrequest.shared.dto.PullRequest;
import org.eclipse.che.plugin.pullrequest.shared.dto.Repository;
import org.eclipse.che.security.oauth.SecurityTokenProvider;

/**
 * {@link VcsHostingService} implementation for GitHub.
 *
 * @author Kevin Pollet
 */
public class GitHubHostingService implements VcsHostingService {

  public static final String SERVICE_NAME = "GitHub";

  private static final String SSH_URL_PREFIX = "git@github.com:";
  private static final String HTTPS_URL_PREFIX = "https://github.com/";
  private static final String API_URL_PREFIX = "https://api.github.com/repos/";
  private static final RegExp REPOSITORY_NAME_OWNER_PATTERN =
      RegExp.compile("([^\\/]+)\\/([^\\/]+)(?:\\.git)?");
  private static final String REPOSITORY_GIT_EXTENSION = ".git";
  private static final String NO_COMMITS_IN_PULL_REQUEST_ERROR_MESSAGE = "No commits between";
  private static final String PULL_REQUEST_ALREADY_EXISTS_ERROR_MESSAGE =
      "A pull request already exists for ";
  private static final String NO_HISTORYIN_COMMON_ERROR_MESSAGE = "has no history in common with";

  private final AppContext appContext;
  private final DtoFactory dtoFactory;
  private final GitHubClientService gitHubClientService;
  private final HostingServiceTemplates templates;
  private final String baseUrl;
  private final SecurityTokenProvider securityTokenProvider;

  @Inject
  public GitHubHostingService(
      @NotNull @RestContext final String baseUrl,
      @NotNull final AppContext appContext,
      @NotNull final DtoFactory dtoFactory,
      @NotNull final GitHubClientService gitHubClientService,
      @NotNull final GitHubTemplates templates,
      SecurityTokenProvider securityTokenProvider) {
    this.appContext = appContext;
    this.dtoFactory = dtoFactory;
    this.gitHubClientService = gitHubClientService;
    this.templates = templates;
    this.baseUrl = baseUrl;
    this.securityTokenProvider = securityTokenProvider;
  }

  @Override
  public Promise<HostUser> getUserInfo() {
    return gitHubClientService
        .getUserInfo()
        .then(
            new Function<GitHubUser, HostUser>() {
              @Override
              public HostUser apply(GitHubUser gitHubUser) throws FunctionException {
                return dtoFactory
                    .createDto(HostUser.class)
                    .withId(gitHubUser.getId())
                    .withLogin(gitHubUser.getLogin())
                    .withName(gitHubUser.getName())
                    .withUrl(gitHubUser.getUrl());
              }
            });
  }

  @Override
  public Promise<Repository> getRepository(String owner, String repositoryName) {
    return gitHubClientService
        .getRepository(owner, repositoryName)
        .then(
            new Function<GitHubRepository, Repository>() {
              @Override
              public Repository apply(GitHubRepository ghRepo) throws FunctionException {
                return valueOf(ghRepo);
              }
            });
  }

  @NotNull
  @Override
  public String getRepositoryNameFromUrl(@NotNull final String url) {
    final String urlWithoutGitHubPrefix = removeGithubPrefix(url);

    final String namePart = REPOSITORY_NAME_OWNER_PATTERN.exec(urlWithoutGitHubPrefix).getGroup(2);
    if (namePart != null && namePart.endsWith(REPOSITORY_GIT_EXTENSION)) {
      return namePart.substring(0, namePart.length() - REPOSITORY_GIT_EXTENSION.length());
    } else {
      return namePart;
    }
  }

  @NotNull
  @Override
  public String getRepositoryOwnerFromUrl(@NotNull final String url) {
    final String urlWithoutGitHubPrefix = removeGithubPrefix(url);

    return REPOSITORY_NAME_OWNER_PATTERN.exec(urlWithoutGitHubPrefix).getGroup(1);
  }

  private String removeGithubPrefix(final String url) {
    int start;
    if (url.startsWith(SSH_URL_PREFIX)) {
      start = SSH_URL_PREFIX.length();
    } else if (url.startsWith(HTTPS_URL_PREFIX)) {
      start = HTTPS_URL_PREFIX.length();
    } else if (url.startsWith(API_URL_PREFIX)) {
      start = API_URL_PREFIX.length();
    } else {
      throw new IllegalArgumentException("Unknown github repo URL pattern");
    }
    return url.substring(start);
  }

  @Override
  public Promise<Repository> fork(final String owner, final String repository) {
    return gitHubClientService
        .fork(owner, repository)
        .thenPromise(
            new Function<GitHubRepository, Promise<Repository>>() {
              @Override
              public Promise<Repository> apply(GitHubRepository repository)
                  throws FunctionException {
                if (repository != null) {
                  return Promises.resolve(valueOf(repository));

                } else {
                  return Promises.reject(JsPromiseError.create(new Exception("No repository.")));
                }
              }
            });
  }

  @NotNull
  @Override
  public String makeSSHRemoteUrl(@NotNull final String username, @NotNull final String repository) {
    return templates.sshUrlTemplate(username, repository);
  }

  @NotNull
  @Override
  public String makeHttpRemoteUrl(
      @NotNull final String username, @NotNull final String repository) {
    return templates.httpUrlTemplate(username, repository);
  }

  @NotNull
  @Override
  public String makePullRequestUrl(
      @NotNull final String username,
      @NotNull final String repository,
      @NotNull final String pullRequestNumber) {
    return templates.pullRequestUrlTemplate(username, repository, pullRequestNumber);
  }

  @NotNull
  @Override
  public String formatReviewFactoryUrl(@NotNull final String reviewFactoryUrl) {
    final String protocol = Window.Location.getProtocol();
    final String host = Window.Location.getHost();

    return templates.formattedReviewFactoryUrlTemplate(protocol, host, reviewFactoryUrl);
  }

  @Override
  public VcsHostingService init(String remoteUrl) {
    return this;
  }

  @NotNull
  @Override
  public String getName() {
    return SERVICE_NAME;
  }

  @NotNull
  @Override
  public String getHost() {
    return "github.com";
  }

  @Override
  public boolean isHostRemoteUrl(@NotNull final String remoteUrl) {
    return remoteUrl.startsWith(SSH_URL_PREFIX) || remoteUrl.startsWith(HTTPS_URL_PREFIX);
  }

  @Override
  public Promise<PullRequest> getPullRequest(
      String owner, String repository, String username, final String branchName) {
    return gitHubClientService
        .getPullRequests(owner, repository, username + ':' + branchName)
        .thenPromise(
            new Function<GitHubPullRequestList, Promise<PullRequest>>() {
              @Override
              public Promise<PullRequest> apply(GitHubPullRequestList prsList)
                  throws FunctionException {
                if (prsList.getPullRequests().isEmpty()) {
                  return Promises.reject(
                      JsPromiseError.create(new NoPullRequestException(branchName)));
                }
                return Promises.resolve(valueOf(prsList.getPullRequests().get(0)));
              }
            });
  }

  /**
   * Get all pull requests for given owner:repository
   *
   * @param owner the username of the owner.
   * @param repository the repository name.
   * @param callback callback called when operation is done.
   */
  private void getPullRequests(
      @NotNull final String owner,
      @NotNull final String repository,
      @NotNull final AsyncCallback<List<PullRequest>> callback) {

    gitHubClientService
        .getPullRequests(owner, repository)
        .then(
            result -> {
              final List<PullRequest> pullRequests = new ArrayList<>();
              for (final GitHubPullRequest oneGitHubPullRequest : result.getPullRequests()) {
                pullRequests.add(valueOf(oneGitHubPullRequest));
              }
              callback.onSuccess(pullRequests);
            })
        .catchError(
            error -> {
              callback.onFailure(error.getCause());
            });
  }

  /**
   * Get all pull requests for given owner:repository
   *
   * @param owner the username of the owner.
   * @param repository the repository name.
   */
  private Promise<List<PullRequest>> getPullRequests(String owner, String repository) {

    return gitHubClientService
        .getPullRequests(owner, repository)
        .then(
            new Function<GitHubPullRequestList, List<PullRequest>>() {
              @Override
              public List<PullRequest> apply(GitHubPullRequestList result)
                  throws FunctionException {
                final List<PullRequest> pullRequests = new ArrayList<>();
                for (final GitHubPullRequest oneGitHubPullRequest : result.getPullRequests()) {
                  pullRequests.add(valueOf(oneGitHubPullRequest));
                }
                return pullRequests;
              }
            });
  }

  protected PullRequest getPullRequestByBranch(
      final String headBranch, final List<PullRequest> pullRequests) {
    for (final PullRequest onePullRequest : pullRequests) {
      if (headBranch.equals(onePullRequest.getHeadRef())) {
        return onePullRequest;
      }
    }
    return null;
  }

  @Override
  public Promise<PullRequest> createPullRequest(
      final String owner,
      final String repository,
      final String username,
      final String headBranchName,
      final String baseBranchName,
      final String title,
      final String body) {
    final String brName = username + ":" + headBranchName;
    final GitHubPullRequestCreationInput input =
        dtoFactory
            .createDto(GitHubPullRequestCreationInput.class)
            .withTitle(title)
            .withHead(brName)
            .withBase(baseBranchName)
            .withBody(body);
    return gitHubClientService
        .createPullRequest(owner, repository, input)
        .then(
            new Function<GitHubPullRequest, PullRequest>() {
              @Override
              public PullRequest apply(GitHubPullRequest arg) throws FunctionException {
                return valueOf(arg);
              }
            })
        .catchErrorPromise(
            new Function<PromiseError, Promise<PullRequest>>() {
              @Override
              public Promise<PullRequest> apply(PromiseError err) throws FunctionException {
                final String msg = err.getMessage();
                if (containsIgnoreCase(msg, NO_COMMITS_IN_PULL_REQUEST_ERROR_MESSAGE)) {
                  return Promises.reject(
                      JsPromiseError.create(
                          new NoCommitsInPullRequestException(brName, baseBranchName)));
                } else if (containsIgnoreCase(msg, PULL_REQUEST_ALREADY_EXISTS_ERROR_MESSAGE)) {
                  return Promises.reject(
                      JsPromiseError.create(new PullRequestAlreadyExistsException(brName)));
                } else if (containsIgnoreCase(msg, NO_HISTORYIN_COMMON_ERROR_MESSAGE)) {
                  return Promises.reject(
                      JsPromiseError.create(
                          new NoHistoryInCommonException(
                              "The "
                                  + brName
                                  + " branch has no history in common with "
                                  + owner
                                  + ':'
                                  + baseBranchName)));
                }

                return Promises.reject(err);
              }
            });
  }

  @Override
  public Promise<Repository> getUserFork(
      final String user, final String owner, final String repository) {
    return getForks(owner, repository)
        .thenPromise(
            new Function<List<Repository>, Promise<Repository>>() {
              @Override
              public Promise<Repository> apply(List<Repository> repositories)
                  throws FunctionException {
                final Repository userFork = getUserFork(user, repositories);
                if (userFork != null) {
                  return Promises.resolve(userFork);
                } else {
                  return Promises.reject(JsPromiseError.create(new NoUserForkException(user)));
                }
              }
            });
  }

  /**
   * Returns the forks of the given repository for the given owner.
   *
   * @param owner the repository owner.
   * @param repository the repository name.
   * @param callback callback called when operation is done.
   */
  private void getForks(
      @NotNull final String owner,
      @NotNull final String repository,
      @NotNull final AsyncCallback<List<Repository>> callback) {
    gitHubClientService
        .getForks(owner, repository)
        .then(
            gitHubRepositoryList -> {
              final List<Repository> repositories = new ArrayList<>();
              for (final GitHubRepository oneGitHubRepository :
                  gitHubRepositoryList.getRepositories()) {
                repositories.add(valueOf(oneGitHubRepository));
              }
              callback.onSuccess(repositories);
            })
        .catchError(
            error -> {
              callback.onFailure(error.getCause());
            });
  }

  private Promise<List<Repository>> getForks(final String owner, final String repository) {
    return gitHubClientService
        .getForks(owner, repository)
        .then(
            new Function<GitHubRepositoryList, List<Repository>>() {
              @Override
              public List<Repository> apply(GitHubRepositoryList gitHubRepositoryList)
                  throws FunctionException {
                final List<Repository> repositories = new ArrayList<>();
                for (final GitHubRepository oneGitHubRepository :
                    gitHubRepositoryList.getRepositories()) {
                  repositories.add(valueOf(oneGitHubRepository));
                }
                return repositories;
              }
            });
  }

  private Repository getUserFork(final String login, final List<Repository> forks) {
    for (final Repository oneRepository : forks) {
      final String repositoryUrl = oneRepository.getCloneUrl();
      if (repositoryUrl != null && containsIgnoreCase(repositoryUrl, "/" + login + "/")) {
        return oneRepository;
      }
    }
    return null;
  }

  /**
   * Converts an instance of {@link org.eclipse.che.plugin.github.shared.GitHubRepository} into a
   * {@link Repository}.
   *
   * @param gitHubRepository the GitHub repository to convert.
   * @return the corresponding {@link Repository} instance or {@code null} if given gitHubRepository
   *     is {@code null}.
   */
  private Repository valueOf(final GitHubRepository gitHubRepository) {
    if (gitHubRepository == null) {
      return null;
    }

    final GitHubRepository gitHubRepositoryParent = gitHubRepository.getParent();
    final Repository parent =
        gitHubRepositoryParent == null
            ? null
            : dtoFactory
                .createDto(Repository.class)
                .withFork(gitHubRepositoryParent.isFork())
                .withName(gitHubRepositoryParent.getName())
                .withParent(null)
                .withPrivateRepo(gitHubRepositoryParent.isPrivateRepo())
                .withCloneUrl(gitHubRepositoryParent.getCloneUrl());

    return dtoFactory
        .createDto(Repository.class)
        .withFork(gitHubRepository.isFork())
        .withName(gitHubRepository.getName())
        .withParent(parent)
        .withPrivateRepo(gitHubRepository.isPrivateRepo())
        .withCloneUrl(gitHubRepository.getCloneUrl());
  }

  /**
   * Converts an instance of {@link org.eclipse.che.plugin.github.shared.GitHubPullRequest} into a
   * {@link PullRequest}.
   *
   * @param gitHubPullRequest the GitHub pull request to convert.
   * @return the corresponding {@link PullRequest} instance or {@code null} if given
   *     gitHubPullRequest is {@code null}.
   */
  private PullRequest valueOf(final GitHubPullRequest gitHubPullRequest) {
    if (gitHubPullRequest == null) {
      return null;
    }

    return dtoFactory
        .createDto(PullRequest.class)
        .withId(gitHubPullRequest.getId())
        .withUrl(gitHubPullRequest.getUrl())
        .withHtmlUrl(gitHubPullRequest.getHtmlUrl())
        .withNumber(gitHubPullRequest.getNumber())
        .withState(gitHubPullRequest.getState())
        .withHeadRef(gitHubPullRequest.getHead().getLabel())
        .withDescription(gitHubPullRequest.getBody());
  }

  @Override
  public Promise<HostUser> authenticate(final CurrentUser user) {
    final Workspace workspace = this.appContext.getWorkspace();
    if (workspace == null) {
      return Promises.reject(JsPromiseError.create("Error accessing current workspace"));
    }
    final String authUrl =
        baseUrl
            + "/oauth/authenticate?oauth_provider=github&userId="
            + user.getProfile().getUserId()
            + "&scope=user,repo,write:public_key&redirect_after_login="
            + Window.Location.getProtocol()
            + "//"
            + Window.Location.getHost()
            + "/ws/"
            + workspace.getConfig().getName();
    return ServiceUtil.performWindowAuth(this, authUrl, securityTokenProvider);
  }

  @Override
  public Promise<PullRequest> updatePullRequest(
      String owner, String repository, PullRequest pullRequest) {
    return gitHubClientService
        .updatePullRequest(owner, repository, pullRequest.getNumber(), valueOf(pullRequest))
        .then(
            new Function<GitHubPullRequest, PullRequest>() {
              @Override
              public PullRequest apply(GitHubPullRequest arg) throws FunctionException {
                return valueOf(arg);
              }
            });
  }

  private GitHubPullRequest valueOf(PullRequest pullRequest) {
    if (pullRequest == null) {
      return null;
    }

    return dtoFactory
        .createDto(GitHubPullRequest.class)
        .withId(pullRequest.getId())
        .withUrl(pullRequest.getUrl())
        .withHtmlUrl(pullRequest.getHtmlUrl())
        .withNumber(pullRequest.getNumber())
        .withState(pullRequest.getState())
        .withBody(pullRequest.getDescription());
  }

  @Override
  public String toString() {
    return "GitHubHostingService";
  }
}
