/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.pullrequest.client.vcs.hosting;

import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.plugin.pullrequest.shared.dto.HostUser;
import org.eclipse.che.plugin.pullrequest.shared.dto.PullRequest;
import org.eclipse.che.plugin.pullrequest.shared.dto.Repository;

/**
 * Represents a repository host
 *
 * @author Kevin Pollet
 */
public interface VcsHostingService {

  /**
   * Initializes new implementation if additional data from remote url is required
   *
   * @param remoteUrl
   * @return
   */
  VcsHostingService init(String remoteUrl);

  /**
   * Returns the VCS Host name.
   *
   * @return the VCS Host name never {@code null}.
   */
  @NotNull
  String getName();

  /**
   * Returns the VCS Host.
   *
   * @return the VCS Host never {@code null}.
   */
  @NotNull
  String getHost();

  /**
   * Checks if the given remote URL is hosted by this service.
   *
   * @param remoteUrl the remote url to check.
   * @return {@code true} if the given remote url is hosted by this service, {@code false}
   *     otherwise.
   */
  boolean isHostRemoteUrl(@NotNull String remoteUrl);

  /**
   * Get a pull request by qualified name.
   *
   * @param owner the repository owner.
   * @param repository the repository name.
   * @param username the user name.
   * @param branchName pull request branch name.
   */
  Promise<PullRequest> getPullRequest(
      @NotNull String owner,
      @NotNull String repository,
      @NotNull String username,
      @NotNull String branchName);

  /**
   * Creates a pull request.
   *
   * @param owner the repository owner.
   * @param repository the repository name.
   * @param username the user name.
   * @param headBranchName the head branch name.
   * @param baseBranchName the base branch name.
   * @param title the pull request title.
   * @param body the pull request body.
   */
  Promise<PullRequest> createPullRequest(
      String owner,
      String repository,
      String username,
      String headBranchName,
      String baseBranchName,
      String title,
      String body);

  /**
   * Forks the given repository for the current user.
   *
   * @param owner the repository owner.
   * @param repository the repository name.
   */
  Promise<Repository> fork(String owner, String repository);

  /**
   * Returns the promise which either resolves repository or rejects with an error.
   *
   * @param owner the owner of the repositoryName
   * @param repositoryName the name of the repository
   */
  Promise<Repository> getRepository(String owner, String repositoryName);

  /**
   * Returns the repository name from the given url.
   *
   * @param url the url.
   * @return the repository name, never {@code null}.
   */
  @NotNull
  String getRepositoryNameFromUrl(@NotNull String url);

  /**
   * Returns the repository owner from the given url.
   *
   * @param url the url.
   * @return the repository owner, never {@code null}.
   */
  @NotNull
  String getRepositoryOwnerFromUrl(@NotNull String url);

  /**
   * Returns the repository fork of the given user.
   *
   * @param user the user.
   * @param owner the repository owner.
   * @param repository the repository name.
   */
  Promise<Repository> getUserFork(String user, String owner, String repository);

  /** Returns the user information on the repository host. */
  Promise<HostUser> getUserInfo();

  /**
   * Makes the remote SSH url for the given username and repository.
   *
   * @param username the user name.
   * @param repository the repository name.
   * @return the remote url.
   */
  String makeSSHRemoteUrl(@NotNull String username, @NotNull String repository);

  /**
   * Makes the remote HTTP url for the given username and repository.
   *
   * @param username the user name.
   * @param repository the repository name.
   * @return the remote url.
   */
  String makeHttpRemoteUrl(@NotNull String username, @NotNull String repository);

  /**
   * Makes the pull request url for the given username, repository and pull request number.
   *
   * @param username the user name.
   * @param repository the repository name.
   * @param pullRequestNumber the pull request number.
   * @return the remote url.
   */
  String makePullRequestUrl(
      @NotNull String username, @NotNull String repository, @NotNull String pullRequestNumber);

  /**
   * Use the VCS hosting comment markup language to format the review factory URL.
   *
   * @param reviewFactoryUrl the review factory URL to format.
   * @return the formatted review factory URL.
   */
  @NotNull
  String formatReviewFactoryUrl(@NotNull String reviewFactoryUrl);

  /**
   * Authenticates the current user on the hosting service.
   *
   * @param user the user to authenticate
   * @return the promise which resolves host user or rejects with an error
   */
  Promise<HostUser> authenticate(CurrentUser user);

  /**
   * Update pull request information e.g. title, description
   *
   * @param owner repository owner
   * @param repository name of repository
   * @param pullRequest pull request for update
   * @return updated pull request
   */
  Promise<PullRequest> updatePullRequest(String owner, String repository, PullRequest pullRequest);
}
