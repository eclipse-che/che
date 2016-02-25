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
package org.eclipse.che.ide.ext.github.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.github.shared.Collaborators;
import org.eclipse.che.ide.ext.github.shared.GitHubIssueComment;
import org.eclipse.che.ide.ext.github.shared.GitHubIssueCommentInput;
import org.eclipse.che.ide.ext.github.shared.GitHubPullRequest;
import org.eclipse.che.ide.ext.github.shared.GitHubPullRequestCreationInput;
import org.eclipse.che.ide.ext.github.shared.GitHubPullRequestList;
import org.eclipse.che.ide.ext.github.shared.GitHubRepository;
import org.eclipse.che.ide.ext.github.shared.GitHubRepositoryList;
import org.eclipse.che.ide.ext.github.shared.GitHubUser;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Implementation for {@link GitHubClientService}.
 *
 * @author Oksana Vereshchaka
 * @author Stéphane Daviet
 */
@Singleton
public class GitHubClientServiceImpl implements GitHubClientService {
    private static final String LIST           = "/list";
    private static final String LIST_ACCOUNT   = "/list/account";
    private static final String LIST_ORG       = "/list/org";
    private static final String LIST_USER      = "/list/user";
    private static final String COLLABORATORS  = "/collaborators";
    private static final String ORGANIZATIONS  = "/orgs";
    private static final String USER           = "/user";
    private static final String SSH_GEN        = "/ssh/generate";
    private static final String FORKS          = "/forks";
    private static final String CREATE_FORK    = "/createfork";
    private static final String PULL_REQUEST   = "/pullrequest";
    private static final String PULL_REQUESTS  = "/pullrequests";
    private static final String ISSUE_COMMENTS = "/issuecomments";
    private static final String REPOSITORIES   = "/repositories";
    /** REST service context. */
    private final String                 baseUrl;
    /** Loader to be displayed. */
    private final AsyncRequestLoader     loader;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    protected GitHubClientServiceImpl(@Named("cheExtensionPath") String extPath,
                                      LoaderFactory loaderFactory,
                                      AsyncRequestFactory asyncRequestFactory,
                                      AppContext appContext,
                                      DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.baseUrl = extPath + "/github/" + appContext.getWorkspace().getId();
        this.loader = loaderFactory.newLoader();
        this.asyncRequestFactory = asyncRequestFactory;
    }

    @Override
    public void getRepository(@NotNull String user, @NotNull String repository, @NotNull AsyncRequestCallback<GitHubRepository> callback) {
        String url = baseUrl + REPOSITORIES + "/" + user + "/" + repository;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<List<GitHubRepository>> getRepositoriesList() {
        String url = baseUrl + LIST;
        return asyncRequestFactory.createGetRequest(url).loader(loader)
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(GitHubRepository.class));
    }

    /** {@inheritDoc} */
    @Override
    public void getForks(@NotNull String user, @NotNull String repository,
                         @NotNull AsyncRequestCallback<GitHubRepositoryList> callback) {
        String url = baseUrl + FORKS + "/" + user + "/" + repository;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void fork(@NotNull String user, @NotNull String repository, @NotNull AsyncRequestCallback<GitHubRepository> callback) {
        String url = baseUrl + CREATE_FORK + "/" + user + "/" + repository;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    @Override
    public void commentIssue(@NotNull String user, @NotNull String repository, @NotNull String issue,
                             @NotNull GitHubIssueCommentInput input, @NotNull AsyncRequestCallback<GitHubIssueComment> callback) {
        String url = baseUrl + ISSUE_COMMENTS + "/" + user + "/" + repository + "/" + issue;
        asyncRequestFactory.createPostRequest(url, input).loader(loader).send(callback);
    }

    @Override
    public void getPullRequests(@NotNull String owner, @NotNull String repository,
                                @NotNull AsyncRequestCallback<GitHubPullRequestList> callback) {
        String url = baseUrl + PULL_REQUESTS + "/" + owner + "/" + repository;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    @Override
    public void getPullRequest(final String owner, final String repository, final String pullRequestId,
                               final AsyncRequestCallback<GitHubPullRequest> callback) {
        String url = baseUrl + PULL_REQUESTS + "/" + owner + "/" + repository + "/" + pullRequestId;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void createPullRequest(@NotNull String user, @NotNull String repository, @NotNull GitHubPullRequestCreationInput input,
                                  @NotNull AsyncRequestCallback<GitHubPullRequest> callback) {
        String url = baseUrl + PULL_REQUEST + "/" + user + "/" + repository;
        asyncRequestFactory.createPostRequest(url, input).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getRepositoriesByUser(String userName, @NotNull AsyncRequestCallback<GitHubRepositoryList> callback) {
        String params = (userName != null) ? "?username=" + userName : "";
        String url = baseUrl + LIST_USER;
        asyncRequestFactory.createGetRequest(url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getCollaborators(@NotNull String user, @NotNull String repository, @NotNull AsyncRequestCallback<Collaborators> callback) {
        String url = baseUrl + COLLABORATORS + "/" + user + "/" + repository;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getOrganizations(@NotNull AsyncRequestCallback<List<String>> callback) {
        String url = baseUrl + ORGANIZATIONS;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<List<GitHubUser>> getOrganizations() {
        String url = baseUrl + ORGANIZATIONS;
        return asyncRequestFactory.createGetRequest(url).loader(loader).send(dtoUnmarshallerFactory.newListUnmarshaller(GitHubUser.class));
    }


    /** {@inheritDoc} */
    @Override
    public void getUserInfo(@NotNull AsyncRequestCallback<GitHubUser> callback) {
        String url = baseUrl + USER;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<GitHubUser> getUserInfo() {
        String url = baseUrl + USER;
        return asyncRequestFactory.createGetRequest(url).loader(loader).send(dtoUnmarshallerFactory.newUnmarshaller(GitHubUser.class));
    }

    /** {@inheritDoc} */
    @Override
    public void getRepositoriesByOrganization(String organization, @NotNull AsyncRequestCallback<GitHubRepositoryList> callback) {
        String params = (organization != null) ? "?organization=" + organization : "";
        String url = baseUrl + LIST_ORG;
        asyncRequestFactory.createGetRequest(url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getRepositoriesByAccount(String account, @NotNull AsyncRequestCallback<GitHubRepositoryList> callback) {
        String params = (account != null) ? "?account=" + account : "";
        String url = baseUrl + LIST_ACCOUNT;
        asyncRequestFactory.createGetRequest(url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updatePublicKey(@NotNull AsyncRequestCallback<Void> callback) {
        String url = baseUrl + SSH_GEN;
        asyncRequestFactory.createPostRequest(url, null).loader(loader).send(callback);
    }
}
