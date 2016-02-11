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
package org.eclipse.che.ide.ext.github.server;

import com.google.inject.Inject;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.github.shared.Collaborators;
import org.eclipse.che.ide.ext.github.shared.GitHubPullRequest;
import org.eclipse.che.ide.ext.github.shared.GitHubPullRequestHead;
import org.eclipse.che.ide.ext.github.shared.GitHubPullRequestList;
import org.eclipse.che.ide.ext.github.shared.GitHubRepository;
import org.eclipse.che.ide.ext.github.shared.GitHubRepositoryList;
import org.eclipse.che.ide.ext.github.shared.GitHubUser;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHPersonSet;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.PagedIterable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class used for converting kohsuke GitHub instances to DTO objects
 *
 * @author Igor Vinokur
 */
public class GitHubDTOFactory {

    @Inject
    GitHubFactory gitHub;

    /**
     * Create DTO object of GitHub repositories collection from given repositories list
     * @param ghRepositoriesList collection of repositories from kohsuke GitHub library
     * @return DTO object
     * @throws IOException
     */
    public GitHubRepositoryList createRepositoriesList(PagedIterable<GHRepository> ghRepositoriesList) throws ApiException, IOException {
        GitHubRepositoryList dtoRepositoriesList = DtoFactory.getInstance().createDto(GitHubRepositoryList.class);

        List<GitHubRepository> dtoRepositories = new ArrayList<>();

        for (GHRepository ghRepository : ghRepositoriesList) {
            dtoRepositories.add(createRepository(ghRepository));
        }

        dtoRepositoriesList.setRepositories(dtoRepositories);

        return dtoRepositoriesList;
    }

    /**
     * Create DTO object of GitHub repositories collection from given repository
     * @param ghRepository repository from kohsuke GitHub library
     * @return DTO object
     * @throws IOException
     */
    public GitHubRepositoryList createRepositoriesList(GHRepository ghRepository) throws ApiException, IOException {
        GitHubRepositoryList dtoRepositoriesList = DtoFactory.getInstance().createDto(GitHubRepositoryList.class);

        List<GitHubRepository> dtoRepositories = new ArrayList<>();
        dtoRepositories.add(createRepository(ghRepository));

        dtoRepositoriesList.setRepositories(dtoRepositories);

        return dtoRepositoriesList;
    }

    /**
     * Create DTO object of GitHub repositories without repositories
     * @return DTO object
     * @throws IOException
     */
    public GitHubRepositoryList createRepositoriesList() throws IOException {
        return DtoFactory.getInstance().createDto(GitHubRepositoryList.class);
    }

    /**
     * Create DTO object of GitHub repository from given repository
     * @param ghRepository repository from kohsuke GitHub library
     * @return DTO object
     * @throws IOException
     */
    public GitHubRepository createRepository(GHRepository ghRepository) throws ApiException, IOException {
        GitHubRepository dtoRepository = DtoFactory.getInstance().createDto(GitHubRepository.class);

        dtoRepository.setName(ghRepository.getName());
        dtoRepository.setUrl(String.valueOf(ghRepository.getUrl()));
        dtoRepository.setHomepage(ghRepository.getHomepage());
        dtoRepository.setForks(ghRepository.getForks());
        dtoRepository.setLanguage(ghRepository.getLanguage());
        dtoRepository.setFork(ghRepository.isFork());
        dtoRepository.setWatchers(ghRepository.getWatchers());
        dtoRepository.setPrivateRepo(ghRepository.isPrivate());
        dtoRepository.setSize(ghRepository.getSize());
        dtoRepository.setDescription(ghRepository.getDescription());
        dtoRepository.setSshUrl(ghRepository.getSshUrl());
        dtoRepository.setHtmlUrl(ghRepository.gitHttpTransportUrl());
        dtoRepository.setUpdatedAt(String.valueOf(ghRepository.getUpdatedAt()));
        dtoRepository.setGitUrl(ghRepository.getGitTransportUrl());
        dtoRepository.setHasWiki(ghRepository.hasWiki());
        dtoRepository.setCloneUrl(String.valueOf(ghRepository.getUrl()));
        dtoRepository.setSvnUrl(ghRepository.getSvnUrl());
        dtoRepository.setOpenedIssues(ghRepository.getOpenIssueCount());
        dtoRepository.setCreatedAt(String.valueOf(ghRepository.getCreatedAt()));
        dtoRepository.setPushedAt(String.valueOf(ghRepository.getPushedAt()));
        dtoRepository.setHasDownloads(ghRepository.hasDownloads());
        dtoRepository.setHasIssues(ghRepository.hasIssues());

        if (ghRepository.isFork() && ghRepository.getParent() != null) {
            dtoRepository.setParent(createRepository(ghRepository.getParent()));
        }
        //if a repository is received from list, parent in repo is null, so we have to get repo separately to get his parent
        if (ghRepository.isFork() && ghRepository.getParent() == null) {
            dtoRepository.setParent(createRepository(gitHub.connect().getUser(ghRepository.getOwner().getLogin())
                                                           .getRepository(ghRepository.getName()).getParent()));
        }

        return dtoRepository;
    }

    /**
     * Create DTO object of GitHub pull-requests collection from given pull-requests
     * @param ghPullRequestsList collection of pull-requests from kohsuke GitHub library
     * @return DTO object
     * @throws IOException
     */
    public GitHubPullRequestList createPullRequestsList(PagedIterable<GHPullRequest> ghPullRequestsList) throws IOException {
        GitHubPullRequestList gitHubPullRequestList = DtoFactory.getInstance().createDto(GitHubPullRequestList.class);

        List<GitHubPullRequest> dtoPullRequestsList = new ArrayList<>();

        for (GHPullRequest ghPullRequest : ghPullRequestsList) {
            dtoPullRequestsList.add(createPullRequest(ghPullRequest));
        }

        gitHubPullRequestList.setPullRequests(dtoPullRequestsList);

        return gitHubPullRequestList;
    }

    /**
     * Create DTO object of GitHub pull-requests collection from given pull-request
     * @param ghPullRequest pull-request from kohsuke GitHub library
     * @return DTO object
     * @throws IOException
     */
    public GitHubPullRequestList createPullRequestsList(GHPullRequest ghPullRequest) throws IOException {
        GitHubPullRequestList gitHubPullRequestList = DtoFactory.getInstance().createDto(GitHubPullRequestList.class);

        List<GitHubPullRequest> dtoPullRequestsList = new ArrayList<>();
        dtoPullRequestsList.add(createPullRequest(ghPullRequest));


        gitHubPullRequestList.setPullRequests(dtoPullRequestsList);

        return gitHubPullRequestList;
    }

    /**
     * Create DTO object of GitHub pull-request from given pull-request
     * @param ghPullRequest pull-request from kohsuke GitHub library
     * @return DTO object
     * @throws IOException
     */
    public GitHubPullRequest createPullRequest(GHPullRequest ghPullRequest) throws IOException {
        GitHubPullRequest dtoPullRequest = DtoFactory.getInstance().createDto(GitHubPullRequest.class);

        dtoPullRequest.setId(String.valueOf(ghPullRequest.getId()));
        dtoPullRequest.setUrl(String.valueOf(ghPullRequest.getUrl()));
        dtoPullRequest.setHtmlUrl(String.valueOf(ghPullRequest.getHtmlUrl()));
        dtoPullRequest.setNumber(String.valueOf(ghPullRequest.getNumber()));
        dtoPullRequest.setState(ghPullRequest.getState().toString());
        dtoPullRequest.setHead(createPullRequestHead(ghPullRequest.getHead()));
        dtoPullRequest.setMerged(ghPullRequest.isMerged());
        if (ghPullRequest.getMergedBy() != null) {
            dtoPullRequest.setMergedBy(createUser(ghPullRequest.getMergedBy()));
        }
        if (ghPullRequest.getMergeable() !=null) {
            dtoPullRequest.setMergeable(ghPullRequest.getMergeable());
        }

        return dtoPullRequest;
    }

    /**
     * Create DTO object of GitHub collaborators collection from given users
     * @param ghCollaborators collection of users from kohsuke GitHub library
     * @return DTO object
     * @throws IOException
     */
    public Collaborators createCollaborators(GHPersonSet<GHUser> ghCollaborators) throws IOException {
        Collaborators collaborators = DtoFactory.getInstance().createDto(Collaborators.class);

        for (GHUser collaborator : ghCollaborators) {
            collaborators.getCollaborators().add(createUser(collaborator));
        }

        return collaborators;
    }

    /**
     * Create DTO object of GitHub user from given user
     * @param ghUser user from kohsuke GitHub library
     * @return DTO object
     * @throws IOException
     */
    public GitHubUser createUser(GHUser ghUser) throws IOException {
        GitHubUser dtoUser = DtoFactory.getInstance().createDto(GitHubUser.class);

        dtoUser.setId(String.valueOf(ghUser.getId()));
        dtoUser.setHtmlUrl(ghUser.getHtmlUrl().toString());
        dtoUser.setAvatarUrl(ghUser.getAvatarUrl());
        dtoUser.setBio(ghUser.getBlog());
        dtoUser.setCompany(ghUser.getCompany());
        dtoUser.setEmail(ghUser.getEmail());
        dtoUser.setFollowers(ghUser.getFollowersCount());
        dtoUser.setFollowing(ghUser.getFollowingCount());
        dtoUser.setLocation(ghUser.getLocation());
        dtoUser.setLogin(ghUser.getLogin());
        dtoUser.setName(ghUser.getName());
        dtoUser.setPublicGists(ghUser.getPublicGistCount());
        dtoUser.setPublicRepos(ghUser.getPublicRepoCount());
        dtoUser.setUrl(String.valueOf(ghUser.getUrl()));
        dtoUser.setGravatarId(ghUser.getGravatarId());

        return dtoUser;
    }

    /**
     * Create DTO object of GitHub pull-request head from given pull-request head
     * @param ghPullRequestHead pull-request head from kohsuke GitHub library
     * @return DTO object
     */
    public GitHubPullRequestHead createPullRequestHead(GHCommitPointer ghPullRequestHead){
        GitHubPullRequestHead dtoPullRequestHead = DtoFactory.getInstance().createDto(GitHubPullRequestHead.class);

        dtoPullRequestHead.setLabel(ghPullRequestHead.getLabel());
        dtoPullRequestHead.setRef(ghPullRequestHead.getRef());
        dtoPullRequestHead.setSha(ghPullRequestHead.getSha());

        return dtoPullRequestHead;
    }

}
