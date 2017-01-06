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
package org.eclipse.che.plugin.github.ide;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.plugin.github.shared.Collaborators;
import org.eclipse.che.plugin.github.shared.GitHubIssueComment;
import org.eclipse.che.plugin.github.shared.GitHubIssueCommentInput;
import org.eclipse.che.plugin.github.shared.GitHubPullRequest;
import org.eclipse.che.plugin.github.shared.GitHubPullRequestCreationInput;
import org.eclipse.che.plugin.github.shared.GitHubPullRequestList;
import org.eclipse.che.plugin.github.shared.GitHubRepository;
import org.eclipse.che.plugin.github.shared.GitHubRepositoryList;
import org.eclipse.che.plugin.github.shared.GitHubUser;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Client service for Samples.
 *
 * @author Oksana Vereshchaka
 * @author Kevin Pollet
 */
public interface GitHubClientService {
    /**
     * Get given repository information.
     *
     * @param user
     *         the owner of the repository.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     * @deprecated use {@link #getRepository(String, String)}
     */
    @Deprecated
    void getRepository(@NotNull String user, @NotNull String repository, @NotNull AsyncRequestCallback<GitHubRepository> callback);

    /**
     * Get given repository information.
     *
     * @param user
     *         the owner of the repository.
     * @param repository
     *         the repository name.
     */
    Promise<GitHubRepository> getRepository(String user, String repository);

    /**
     * Get list of available public and private repositories of the authorized user.
     */
    Promise<List<GitHubRepository>> getRepositoriesList();

    /**
     * Get list of forks for given repository
     *
     * @param user
     *         the owner of the repository.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     * @deprecated use {@link #getForks(String user, String repository)}
     */
    @Deprecated
    void getForks(@NotNull String user, @NotNull String repository, @NotNull AsyncRequestCallback<GitHubRepositoryList> callback);

    /**
     * Get list of forks for given repository
     *
     * @param user
     *         the owner of the repository.
     * @param repository
     *         the repository name.
     */
    Promise<GitHubRepositoryList> getForks(String user, String repository);

    /**
     * Fork the given repository for the authorized user.
     *
     * @param user
     *         the owner of the repository to fork.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     * @deprecated use {@link #fork(String, String)}
     */
    @Deprecated
    void fork(@NotNull String user, @NotNull String repository, @NotNull AsyncRequestCallback<GitHubRepository> callback);

    /**
     * Fork the given repository for the authorized user.
     *
     * @param user
     *         the owner of the repository to fork.
     * @param repository
     *         the repository name.
     */
    Promise<GitHubRepository> fork(String user, String repository);

    /**
     * Add a comment to the issue on the given repository.
     *
     * @param user
     *         the owner of the repository.
     * @param repository
     *         the repository name.
     * @param issue
     *         the issue number.
     * @param input
     *         the comment.
     * @param callback
     *         callback called when operation is done.
     */
    void commentIssue(@NotNull String user, @NotNull String repository, @NotNull String issue, @NotNull GitHubIssueCommentInput input,
                      @NotNull AsyncRequestCallback<GitHubIssueComment> callback);

    /**
     * Get pull requests for given repository.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     * @deprecated use {@link #getPullRequests(String, String)}
     */
    @Deprecated
    void getPullRequests(@NotNull String owner, @NotNull String repository, @NotNull AsyncRequestCallback<GitHubPullRequestList> callback);

    /**
     * Get pull requests for given repository.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     */
    Promise<GitHubPullRequestList> getPullRequests(@NotNull String owner, @NotNull String repository);

    /**
     * Get pull requests for given repository.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param head
     *         user and branch name in the format of user:ref-name
     */
    Promise<GitHubPullRequestList> getPullRequests(String owner, String repository, String head);

    /**
     * Get a pull request by id for a given repository.
     *
     * @param owner
     *         the owner of the target repository
     * @param repository
     *         the target repository
     * @param pullRequestId
     *         the Id of the pull request
     * @param callback
     *         the callback with either the pull request as argument or null if it doesn't exist
     */
    void getPullRequest(@NotNull String owner,
                        @NotNull String repository,
                        @NotNull String pullRequestId,
                        @NotNull AsyncRequestCallback<GitHubPullRequest> callback);

    /**
     * Create a pull request on origin repository
     *
     * @param user
     *         the owner of the repository.
     * @param repository
     *         the repository name.
     * @param input
     *         the pull request information.
     * @param callback
     *         callback called when operation is done.
     * @deprecated use {@link #createPullRequest(String, String, GitHubPullRequestCreationInput)}
     */
    @Deprecated
    void createPullRequest(@NotNull String user,
                           @NotNull String repository,
                           @NotNull GitHubPullRequestCreationInput input,
                           @NotNull AsyncRequestCallback<GitHubPullRequest> callback);

    /**
     * Create a pull request on origin repository
     *
     * @param user
     *         the owner of the repository.
     * @param repository
     *         the repository name.
     * @param input
     *         the pull request information.
     */
    Promise<GitHubPullRequest> createPullRequest(@NotNull String user,
                                                 @NotNull String repository,
                                                 @NotNull GitHubPullRequestCreationInput input);

    /**
     * Get the list of available public repositories for a GitHub user.
     *
     * @param userName
     *         the name of GitHub User
     * @param callback
     *         callback called when operation is done.
     */
    void getRepositoriesByUser(String userName, @NotNull AsyncRequestCallback<GitHubRepositoryList> callback);

    /**
     * Get the list of available repositories by GitHub organization.
     *
     * @param organization
     *         the name of GitHub organization.
     * @param callback
     *         callback called when operation is done.
     */
    void getRepositoriesByOrganization(String organization, @NotNull AsyncRequestCallback<GitHubRepositoryList> callback);

    /**
     * Get list of available public repositories for GitHub account.
     *
     * @param account
     *         the GitHub account.
     * @param callback
     *         callback called when operation is done.
     */
    void getRepositoriesByAccount(String account, @NotNull AsyncRequestCallback<GitHubRepositoryList> callback);

    /**
     * Get list of collaborators of GitHub repository. For detail see GitHub REST API http://developer.github.com/v3/repos/collaborators/.
     *
     * @param user
     *         the owner of the repository.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     */
    void getCollaborators(@NotNull String user, @NotNull String repository, @NotNull AsyncRequestCallback<Collaborators> callback);

    /**
     * Get the list of the organizations, where authorized user is a member.
     *
     * Use {@link #getOrganizations()}.
     */
    @Deprecated
    void getOrganizations(@NotNull AsyncRequestCallback<List<String>> callback);

    /**
     * Get the list of the organizations, where authorized user is a member.
     */
    Promise<List<GitHubUser>> getOrganizations();

    /**
     * Get authorized user information.
     *
     * Use {@link #getUserInfo()}.
     */
    @Deprecated
    void getUserInfo(@NotNull AsyncRequestCallback<GitHubUser> callback);

    /**
     * Get authorized user information.
     */
    Promise<GitHubUser> getUserInfo();

    /**
     * Generate and upload new public key if not exist on github.com.
     *
     * @param callback
     *         callback called when operation is done.
     */
    void updatePublicKey(@NotNull AsyncRequestCallback<Void> callback);

    /**
     * Updates github pull request
     *
     * @param user
     *         repository owner
     * @param repository
     *         name of repository
     * @param pullRequestId
     *         pull request identifier
     * @param pullRequest
     *         update body
     * @return updated pull request
     */
    Promise<GitHubPullRequest> updatePullRequest(String user,
                                                 String repository,
                                                 String pullRequestId,
                                                 GitHubPullRequest pullRequest);
}
