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
package org.eclipse.che.plugin.github.server.rest;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.ssh.server.SshServiceClient;
import org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
import org.eclipse.che.api.ssh.shared.model.SshPair;
import org.eclipse.che.plugin.github.server.GitHubDTOFactory;
import org.eclipse.che.plugin.github.server.GitHubFactory;
import org.eclipse.che.plugin.github.server.GitHubKeyUploader;
import org.eclipse.che.plugin.github.shared.Collaborators;
import org.eclipse.che.plugin.github.shared.GitHubIssueCommentInput;
import org.eclipse.che.plugin.github.shared.GitHubPullRequest;
import org.eclipse.che.plugin.github.shared.GitHubPullRequestCreationInput;
import org.eclipse.che.plugin.github.shared.GitHubPullRequestList;
import org.eclipse.che.plugin.github.shared.GitHubRepository;
import org.eclipse.che.plugin.github.shared.GitHubRepositoryList;
import org.eclipse.che.plugin.github.shared.GitHubUser;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * REST service to get the list of repositories from GitHub (where sample projects are located).
 *
 * @author Oksana Vereshchaka
 * @author Stéphane Daviet
 * @author Kevin Pollet
 * @author Igor vinokur
 */
@Path("/github")
public class GitHubService {
    @Inject
    private GitHubFactory gitHubFactory;

    @Inject
    private GitHubDTOFactory gitHubDTOFactory;

    @Inject
    private GitHubKeyUploader githubKeyUploader;

    @Inject
    private SshServiceClient sshServiceClient;

    private static final Logger LOG = LoggerFactory.getLogger(GitHubService.class);

    @GET
    @Path("repositories/{user}/{repository}")
    @Produces(MediaType.APPLICATION_JSON)
    public GitHubRepository getUserRepository(@PathParam("user") String user, @PathParam("repository") String repository)
            throws ApiException {
        try {
            return gitHubDTOFactory.createRepository(gitHubFactory.connect().getUser(user).getRepository(repository));
        } catch (IOException e) {
            LOG.error("Get user info error", e);
            throw new ServerException(e.getMessage());
        }
    }

    @GET
    @Path("list/user")
    @Produces(MediaType.APPLICATION_JSON)
    public GitHubRepositoryList listRepositoriesByUser(@QueryParam("username") String userName) throws ApiException {
        try {
            return gitHubDTOFactory.createRepositoriesList(gitHubFactory.connect().getUser(userName).listRepositories());
        } catch (IOException e) {
            LOG.error("Get list repositories by user fail", e);
            throw new ServerException(e.getMessage());
        }
    }

    @GET
    @Path("list/org")
    @Produces(MediaType.APPLICATION_JSON)
    public GitHubRepositoryList listRepositoriesByOrganization(@QueryParam("organization") String organization) throws ApiException {
        try {
            return gitHubDTOFactory.createRepositoriesList(gitHubFactory.connect().getOrganization(organization).listRepositories());
        } catch (IOException e) {
            LOG.error("Get list repositories by organization fail", e);
            throw new ServerException(e.getMessage());
        }
    }

    @GET
    @Path("list/account")
    @Produces(MediaType.APPLICATION_JSON)
    public GitHubRepositoryList listRepositoriesByAccount(@QueryParam("account") String account) throws ApiException {
        GitHub gitHub = gitHubFactory.connect();
        try {
            //First, try to retrieve organization repositories:
            return gitHubDTOFactory.createRepositoriesList(gitHub.getOrganization(account).listRepositories());
        } catch (IOException ioException) {
            LOG.error("Get list repositories by account fail", ioException);
            //If account is not organization, then try by user name:
            try {
                return gitHubDTOFactory.createRepositoriesList(gitHub.getUser(account).listRepositories());
            } catch (IOException exception) {
                LOG.error("Get list repositories by account fail", exception);
                throw new ServerException(exception.getMessage());
            }
        }
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<GitHubRepository> listRepositories() throws ApiException {
        try {
            return gitHubDTOFactory.createRepositoriesList(gitHubFactory.connect().getMyself().listRepositories()).getRepositories();
        } catch (IOException e) {
            LOG.error("Get list repositories fail", e);
            throw new ServerException(e.getMessage());
        }
    }

    @GET
    @Path("forks/{user}/{repository}")
    @Produces(MediaType.APPLICATION_JSON)
    public GitHubRepositoryList forks(@PathParam("user") String user, @PathParam("repository") String repository) throws ApiException {
        GitHubRepositoryList gitHubRepositoryList;
        try {
            gitHubRepositoryList = gitHubDTOFactory.createRepositoriesList();

            for (GHRepository ghRepository : gitHubFactory.connect().getMyself().listRepositories()) {
                if (ghRepository.isFork() && ghRepository.getName().equals(repository)) {
                    gitHubRepositoryList = gitHubDTOFactory.createRepositoriesList(ghRepository);
                    break;
                }
            }
        } catch (IOException e) {
            LOG.error("Get forks fail", e);
            throw new ServerException(e.getMessage());
        }
        return gitHubRepositoryList;
    }

    @GET
    @Path("createfork/{user}/{repository}")
    @Produces(MediaType.APPLICATION_JSON)
    public GitHubRepository fork(@PathParam("user") String user, @PathParam("repository") String repository) throws ApiException {
        try {
            return gitHubDTOFactory.createRepository(gitHubFactory.connect().getUser(user).getRepository(repository).fork());
        } catch (IOException e) {
            LOG.error("Fork fail", e);
            throw new ServerException(e.getMessage());
        }
    }

    @POST
    @Path("issuecomments/{user}/{repository}/{issue}")
    @Produces(MediaType.APPLICATION_JSON)
    public void commentIssue(@PathParam("user") String user,
                             @PathParam("repository") String repository,
                             @PathParam("issue") String issue,
                             GitHubIssueCommentInput input) throws ApiException {
        try {
            gitHubFactory.connect().getUser(user).getRepository(repository).getIssue(Integer.getInteger(issue)).comment(input.getBody());
        } catch (IOException e) {
            LOG.error("Comment issue fail", e);
            throw new ServerException(e.getMessage());
        }
    }

    @GET
    @Path("pullrequests/{user}/{repository}")
    @Produces(MediaType.APPLICATION_JSON)
    public GitHubPullRequestList listPullRequestsByRepository(@PathParam("user") String user,
                                                              @PathParam("repository") String repository,
                                                              @QueryParam("head") String head)
            throws ApiException {
        try {
            return gitHubDTOFactory.createPullRequestsList(gitHubFactory.connect()
                                                                        .getUser(user)
                                                                        .getRepository(repository)
                                                                        .queryPullRequests()
                                                                        .head(head)
                                                                        .state(GHIssueState.OPEN)
                                                                        .list());
        } catch (IOException e) {
            LOG.error("Getting list of pull request by repositories", e);
            throw new ServerException(e.getMessage());
        }
    }

    @GET
    @Path("pullrequests/{user}/{repository}/{pullRequestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public GitHubPullRequestList getPullRequestsById(@PathParam("user") String user, @PathParam("repository") String repository, @PathParam("pullRequestId") String pullRequestId)
            throws ApiException {
        try {
            return gitHubDTOFactory.createPullRequestsList(gitHubFactory.connect().getUser(user).getRepository(repository)
                                                                        .getPullRequest(Integer.valueOf(pullRequestId)));
        } catch (IOException e) {
            LOG.error("Getting list of pull request by id", e);
            throw new ServerException(e.getMessage());
        }

    }

    @POST
    @Path("pullrequest/{user}/{repository}")
    @Produces(MediaType.APPLICATION_JSON)
    public GitHubPullRequest createPullRequest(@PathParam("user") String user, @PathParam("repository") String repository, GitHubPullRequestCreationInput input)
            throws ApiException {
        try {
            GHPullRequest pullRequest = gitHubFactory.connect().getUser(user).getRepository(repository).createPullRequest(
                    input.getTitle(), input.getHead(), input.getBase(), input.getBody());
            return gitHubDTOFactory.createPullRequest(pullRequest);
        } catch (Exception e) {
            if (!e.getMessage().contains("No commits between master and master")) {
                LOG.error("Creating  pull request fail", e);
            }
            throw new ServerException(e.getMessage());
        }
    }

    @PUT
    @Path("pullrequest/{user}/{repository}/{pullRequestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public GitHubPullRequest updatePullRequest(@PathParam("user")
                                               String user,
                                               @PathParam("repository")
                                               String repository,
                                               @PathParam("pullRequestId")
                                               String pullRequestId,
                                               GitHubPullRequest pullRequest) throws ServerException,
                                                                                     UnauthorizedException {
        try {
            final GHPullRequest ghPullRequest = gitHubFactory.connect()
                                                             .getUser(user)
                                                             .getRepository(repository)
                                                             .getPullRequest(Integer.valueOf(pullRequestId));
            final String body = pullRequest.getBody();
            if (body != null && !body.equals(ghPullRequest.getBody())) {
                ghPullRequest.setBody(body);
            }
            final String title = pullRequest.getTitle();
            if (title != null && !title.equals(ghPullRequest.getTitle())) {
                ghPullRequest.setTitle(title);
            }
            return gitHubDTOFactory.createPullRequest(ghPullRequest);
        } catch (IOException ioEx) {
            throw new ServerException(ioEx.getMessage());
        }
    }

    @GET
    @Path("orgs")
    @Produces(MediaType.APPLICATION_JSON)
    public List<GitHubUser> listOrganizations() throws ApiException {
        try {
            return gitHubDTOFactory.createCollaborators(gitHubFactory.connect().getMyself().getAllOrganizations()).getCollaborators();
        } catch (IOException e) {
            LOG.error("Getting list of available organizations fail", e);
            throw new ServerException(e.getMessage());
        }
    }

    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    public GitHubUser getUserInfo() throws ApiException {
        try {
            return gitHubDTOFactory.createUser(gitHubFactory.connect().getMyself());
        } catch (IOException e) {
            LOG.error("Getting user info fail", e);
            throw new ServerException(e.getMessage());
        }
    }

    @GET
    @Path("collaborators/{user}/{repository}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collaborators collaborators(@PathParam("user") String user, @PathParam("repository") String repository) throws ApiException {
        try {
            return gitHubDTOFactory.createCollaborators(gitHubFactory.connect().getUser(user).getRepository(repository).getCollaborators());
        } catch (IOException e) {
            LOG.error("Get collaborators fail", e);
            throw new ServerException(e.getMessage());
        }
    }

    @POST
    @Path("ssh/generate")
    public void updateSSHKey() throws ApiException {
        final String host = "github.com";
        SshPair sshPair = null;
        try {
            sshPair = sshServiceClient.getPair("vcs", host);
        } catch (NotFoundException ignored) {
        }

        if (sshPair != null) {
            if (sshPair.getPublicKey() == null) {
                sshServiceClient.removePair("vcs", host);
                sshPair = sshServiceClient.generatePair(newDto(GenerateSshPairRequest.class).withService("vcs")
                                                                                            .withName(host));
            }
        } else {
            sshPair = sshServiceClient.generatePair(newDto(GenerateSshPairRequest.class).withService("vcs")
                                                                                        .withName(host));
        }

        // update public key
        try {
            githubKeyUploader.uploadKey(sshPair.getPublicKey());
        } catch (IOException e) {
            LOG.error("Upload github ssh key fail", e);
            throw new GitException(e.getMessage(), e);
        }
    }
}
