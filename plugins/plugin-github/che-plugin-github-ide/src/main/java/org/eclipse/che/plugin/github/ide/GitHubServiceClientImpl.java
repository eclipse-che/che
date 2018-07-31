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
package org.eclipse.che.plugin.github.ide;

import com.google.gwt.http.client.RequestBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.plugin.github.shared.Collaborators;
import org.eclipse.che.plugin.github.shared.GitHubIssueComment;
import org.eclipse.che.plugin.github.shared.GitHubIssueCommentInput;
import org.eclipse.che.plugin.github.shared.GitHubPullRequest;
import org.eclipse.che.plugin.github.shared.GitHubPullRequestCreationInput;
import org.eclipse.che.plugin.github.shared.GitHubPullRequestList;
import org.eclipse.che.plugin.github.shared.GitHubRepository;
import org.eclipse.che.plugin.github.shared.GitHubRepositoryList;
import org.eclipse.che.plugin.github.shared.GitHubUser;

/**
 * Implementation for {@link GitHubServiceClient}.
 *
 * @author Oksana Vereshchaka
 * @author Stéphane Daviet
 */
@Singleton
public class GitHubServiceClientImpl implements GitHubServiceClient {

  private static final String TOKEN_HEADER_NAME = "X-Oauth-Token";
  private static final String LIST = "/list";
  private static final String LIST_ACCOUNT = "/list/account";
  private static final String LIST_ORG = "/list/org";
  private static final String LIST_USER = "/list/user";
  private static final String COLLABORATORS = "/collaborators";
  private static final String ORGANIZATIONS = "/orgs";
  private static final String USER = "/user";
  private static final String SSH_GEN = "/ssh/generate";
  private static final String FORKS = "/forks";
  private static final String CREATE_FORK = "/createfork";
  private static final String PULL_REQUEST = "/pullrequest";
  private static final String PULL_REQUESTS = "/pullrequests";
  private static final String ISSUE_COMMENTS = "/issuecomments";
  private static final String REPOSITORIES = "/repositories";

  /** Loader to be displayed. */
  private final AsyncRequestLoader loader;

  private final AsyncRequestFactory asyncRequestFactory;
  private final AppContext appContext;
  private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

  @Inject
  protected GitHubServiceClientImpl(
      LoaderFactory loaderFactory,
      AsyncRequestFactory asyncRequestFactory,
      AppContext appContext,
      DtoUnmarshallerFactory dtoUnmarshallerFactory) {
    this.appContext = appContext;
    this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    this.loader = loaderFactory.newLoader();
    this.asyncRequestFactory = asyncRequestFactory;
  }

  private String baseUrl() {
    return appContext.getWsAgentServerApiEndpoint() + "/github";
  }

  @Override
  public Promise<GitHubRepository> getRepository(
      @NotNull String oauthToken, String user, String repository) {
    final String url = baseUrl() + REPOSITORIES + "/" + user + "/" + repository;
    return asyncRequestFactory
        .createGetRequest(url)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newUnmarshaller(GitHubRepository.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<List<GitHubRepository>> getRepositoriesList(@NotNull String oauthToken) {
    String url = baseUrl() + LIST;
    return asyncRequestFactory
        .createGetRequest(url)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newListUnmarshaller(GitHubRepository.class));
  }

  @Override
  public Promise<GitHubRepositoryList> getForks(
      @NotNull String oauthToken, String user, String repository) {
    return asyncRequestFactory
        .createGetRequest(baseUrl() + FORKS + '/' + user + '/' + repository)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newUnmarshaller(GitHubRepositoryList.class));
  }

  @Override
  public Promise<GitHubRepository> fork(
      @NotNull String oauthToken, String user, String repository) {
    return asyncRequestFactory
        .createGetRequest(baseUrl() + CREATE_FORK + '/' + user + '/' + repository)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newUnmarshaller(GitHubRepository.class));
  }

  @Override
  public void commentIssue(
      @NotNull String oauthToken,
      @NotNull String user,
      @NotNull String repository,
      @NotNull String issue,
      @NotNull GitHubIssueCommentInput input,
      @NotNull AsyncRequestCallback<GitHubIssueComment> callback) {
    String url = baseUrl() + ISSUE_COMMENTS + "/" + user + "/" + repository + "/" + issue;
    asyncRequestFactory
        .createPostRequest(url, input)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(callback);
  }

  @Override
  public Promise<GitHubPullRequestList> getPullRequests(
      @NotNull String oauthToken, @NotNull String owner, @NotNull String repository) {
    final String url = baseUrl() + PULL_REQUESTS + '/' + owner + '/' + repository;
    return asyncRequestFactory
        .createGetRequest(url)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newUnmarshaller(GitHubPullRequestList.class));
  }

  @Override
  public Promise<GitHubPullRequestList> getPullRequests(
      @NotNull String oauthToken, String owner, String repository, String head) {
    final String url = baseUrl() + PULL_REQUESTS + '/' + owner + '/' + repository + "?head=" + head;
    return asyncRequestFactory
        .createGetRequest(url)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newUnmarshaller(GitHubPullRequestList.class));
  }

  @Override
  public void getPullRequest(
      @NotNull String oauthToken,
      final String owner,
      final String repository,
      final String pullRequestId,
      final AsyncRequestCallback<GitHubPullRequest> callback) {
    String url = baseUrl() + PULL_REQUESTS + "/" + owner + "/" + repository + "/" + pullRequestId;
    asyncRequestFactory
        .createGetRequest(url)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(callback);
  }

  @Override
  public Promise<GitHubPullRequest> createPullRequest(
      @NotNull String oauthToken,
      @NotNull String user,
      @NotNull String repository,
      @NotNull GitHubPullRequestCreationInput input) {
    final String url = baseUrl() + PULL_REQUEST + '/' + user + '/' + repository;
    return asyncRequestFactory
        .createPostRequest(url, input)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newUnmarshaller(GitHubPullRequest.class));
  }

  /** {@inheritDoc} */
  @Override
  public void getRepositoriesByUser(
      @NotNull String oauthToken,
      String userName,
      @NotNull AsyncRequestCallback<GitHubRepositoryList> callback) {
    String params = (userName != null) ? "?username=" + userName : "";
    String url = baseUrl() + LIST_USER;
    asyncRequestFactory
        .createGetRequest(url + params)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(callback);
  }

  /** {@inheritDoc} */
  @Override
  public void getCollaborators(
      @NotNull String oauthToken,
      @NotNull String user,
      @NotNull String repository,
      @NotNull AsyncRequestCallback<Collaborators> callback) {
    String url = baseUrl() + COLLABORATORS + "/" + user + "/" + repository;
    asyncRequestFactory
        .createGetRequest(url)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(callback);
  }

  /** {@inheritDoc} */
  @Override
  public Promise<List<GitHubUser>> getOrganizations(@NotNull String oauthToken) {
    String url = baseUrl() + ORGANIZATIONS;
    return asyncRequestFactory
        .createGetRequest(url)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newListUnmarshaller(GitHubUser.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<GitHubUser> getUserInfo(@NotNull String oauthToken) {
    String url = baseUrl() + USER;
    return asyncRequestFactory
        .createGetRequest(url)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newUnmarshaller(GitHubUser.class));
  }

  /** {@inheritDoc} */
  @Override
  public void getRepositoriesByOrganization(
      @NotNull String oauthToken,
      String organization,
      @NotNull AsyncRequestCallback<GitHubRepositoryList> callback) {
    String params = (organization != null) ? "?organization=" + organization : "";
    String url = baseUrl() + LIST_ORG;
    asyncRequestFactory
        .createGetRequest(url + params)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(callback);
  }

  /** {@inheritDoc} */
  @Override
  public void getRepositoriesByAccount(
      @NotNull String oauthToken,
      String account,
      @NotNull AsyncRequestCallback<GitHubRepositoryList> callback) {
    String params = (account != null) ? "?account=" + account : "";
    String url = baseUrl() + LIST_ACCOUNT;
    asyncRequestFactory
        .createGetRequest(url + params)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(callback);
  }

  /** {@inheritDoc} */
  @Override
  public void updatePublicKey(String oauthToken, @NotNull AsyncRequestCallback<Void> callback) {
    String url = baseUrl() + SSH_GEN;
    asyncRequestFactory
        .createPostRequest(url, null)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(callback);
  }

  @Override
  public Promise<GitHubPullRequest> updatePullRequest(
      @NotNull String oauthToken,
      String owner,
      String repository,
      String pullRequestId,
      GitHubPullRequest updateInput) {
    final String url =
        baseUrl() + PULL_REQUEST + '/' + owner + '/' + repository + '/' + pullRequestId;
    return asyncRequestFactory
        .createRequest(RequestBuilder.PUT, url, updateInput, false)
        .header(TOKEN_HEADER_NAME, oauthToken)
        .loader(loader)
        .send(dtoUnmarshallerFactory.newUnmarshaller(GitHubPullRequest.class));
  }
}
