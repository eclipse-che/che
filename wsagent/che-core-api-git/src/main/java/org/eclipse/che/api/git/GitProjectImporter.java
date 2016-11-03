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

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.WorkspaceIdProvider;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchListRequest;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.CloneRequest;
import org.eclipse.che.api.git.shared.FetchRequest;
import org.eclipse.che.api.git.shared.GitCheckoutEvent;
import org.eclipse.che.api.git.shared.InitRequest;
import org.eclipse.che.api.git.shared.RemoteAddRequest;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.eclipse.che.api.core.ErrorCodes.FAILED_CHECKOUT;
import static org.eclipse.che.api.core.ErrorCodes.FAILED_CHECKOUT_WITH_START_POINT;
import static org.eclipse.che.api.git.GitBasicAuthenticationCredentialsProvider.clearCredentials;
import static org.eclipse.che.api.git.GitBasicAuthenticationCredentialsProvider.setCurrentCredentials;
import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_ALL;

/**
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class GitProjectImporter implements ProjectImporter {

    private final GitConnectionFactory gitConnectionFactory;
    private static final Logger LOG = LoggerFactory.getLogger(GitProjectImporter.class);
    private final EventService eventService;

    @Inject
    public GitProjectImporter(GitConnectionFactory gitConnectionFactory,
                              EventService eventService) {
        this.gitConnectionFactory = gitConnectionFactory;
        this.eventService = eventService;
    }

    @Override
    public String getId() {
        return "git";
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getDescription() {
        return "Import project from hosted GIT repository URL.";
    }

    /** {@inheritDoc} */
    @Override
    public ImporterCategory getCategory() {
        return ImporterCategory.SOURCE_CONTROL;
    }

    @Override
    public void importSources(FolderEntry baseFolder, SourceStorage storage) throws ForbiddenException,
                                                                                    ConflictException,
                                                                                    UnauthorizedException,
                                                                                    IOException,
                                                                                    ServerException {
        importSources(baseFolder, storage, LineConsumerFactory.NULL);
    }

    @Override
    public void importSources(FolderEntry baseFolder,
                              SourceStorage storage,
                              LineConsumerFactory consumerFactory) throws ForbiddenException,
                                                                          ConflictException,
                                                                          UnauthorizedException,
                                                                          IOException,
                                                                          ServerException {
        GitConnection git = null;
        boolean credentialsHaveBeenSet = false;
        try {
            // For factory: checkout particular commit after clone
            String commitId = null;
            // For factory: github pull request feature
            String fetch = null;
            String branch = null;
            String startPoint = null;
            // For factory or probably for our projects templates:
            // If git repository contains more than one project need clone all repository but after cloning keep just
            // sub-project that is specified in parameter "keepDir".
            String keepDir = null;
            // For factory and for our projects templates:
            // Keep all info related to the vcs. In case of Git: ".git" directory and ".gitignore" file.
            // Delete vcs info if false.
            String branchMerge = null;
            boolean keepVcs = true;
            boolean recursiveEnabled = false;
            boolean convertToTopLevelProject = false;

            Map<String, String> parameters = storage.getParameters();
            if (parameters != null) {
                commitId = parameters.get("commitId");
                branch = parameters.get("branch");
                startPoint = parameters.get("startPoint");
                fetch = parameters.get("fetch");
                keepDir = parameters.get("keepDir");
                if (parameters.containsKey("keepVcs")) {
                    keepVcs = Boolean.parseBoolean(parameters.get("keepVcs"));
                }
                if (parameters.containsKey("recursive")) {
                    recursiveEnabled = true;
                }
                //convertToTopLevelProject feature is working only if we don't need any git information
                //and when we are working in git sparse checkout mode.
                if (!keepVcs && !Strings.isNullOrEmpty(keepDir) && parameters.containsKey("convertToTopLevelProject")) {
                    convertToTopLevelProject = Boolean.parseBoolean(parameters.get("convertToTopLevelProject"));
                }
                branchMerge = parameters.get("branchMerge");
                final String user = storage.getParameters().remove("username");
                final String pass = storage.getParameters().remove("password");
                if (user != null && pass != null) {
                    credentialsHaveBeenSet = true;
                    setCurrentCredentials(user, pass);
                }
            }
            // Get path to local file. Git works with local filesystem only.
            final String localPath = baseFolder.getVirtualFile().toIoFile().getAbsolutePath();
            final DtoFactory dtoFactory = DtoFactory.getInstance();
            final String location = storage.getLocation();
            final String projectName = baseFolder.getName();

            // Converting steps
            // 1. Clone to temporary folder on same device with /projects
            // 2. Remove git information
            // 3. Move to path requested by user.
            // Very important to have initial clone folder on the same drive with /project
            // otherwise we will have to replace atomic move with copy-delete operation.
            if (convertToTopLevelProject) {
                File tempDir = new File(new File(localPath).getParent(), NameGenerator.generate(".che", 6));
                git = gitConnectionFactory.getConnection(tempDir, consumerFactory);
            } else {
                git = gitConnectionFactory.getConnection(localPath, consumerFactory);
            }

            if (keepDir != null) {
                git.cloneWithSparseCheckout(keepDir, location, branch == null ? "master" : branch);
            } else {
                if (baseFolder.getChildren().size() == 0) {
                    cloneRepository(git, "origin", location, dtoFactory, recursiveEnabled);
                    if (commitId != null) {
                        checkoutCommit(git, commitId, dtoFactory);
                    } else if (fetch != null) {
                        git.getConfig().add("remote.origin.fetch", fetch);
                        fetch(git, "origin", dtoFactory);
                        if (branch != null) {
                            checkoutBranch(git, projectName, branch, startPoint, dtoFactory);
                        }
                    } else if (branch != null) {
                        checkoutBranch(git, projectName, branch, startPoint, dtoFactory);
                    }
                } else {
                    initRepository(git, dtoFactory);
                    addRemote(git, "origin", location, dtoFactory);
                    if (commitId != null) {
                        fetchBranch(git, "origin", branch == null ? "*" : branch, dtoFactory);
                        checkoutCommit(git, commitId, dtoFactory);
                    } else if (fetch != null) {
                        git.getConfig().add("remote.origin.fetch", fetch);
                        fetch(git, "origin", dtoFactory);
                        if (branch != null) {
                            checkoutBranch(git, projectName, branch, startPoint, dtoFactory);
                        }
                    } else {
                        fetchBranch(git, "origin", branch == null ? "*" : branch, dtoFactory);

                        List<Branch> branchList = git.branchList(dtoFactory.createDto(BranchListRequest.class).withListMode("r"));
                        if (!branchList.isEmpty()) {
                            checkoutBranch(git, projectName, branch == null ? "master" : branch, startPoint, dtoFactory);
                        }
                    }
                }
                if (branchMerge != null) {
                    git.getConfig().set("branch." + (branch == null ? "master" : branch) + ".merge", branchMerge);
                }
            }
            if (!keepVcs) {
                cleanGit(git.getWorkingDir());
            }
            if (convertToTopLevelProject) {
                Files.move(new File(git.getWorkingDir(), keepDir).toPath(),
                           new File(localPath).toPath(),
                           StandardCopyOption.ATOMIC_MOVE);
                IoUtil.deleteRecursive(git.getWorkingDir());
            }

        } catch (URISyntaxException e) {
            throw new ServerException(
                    "Your project cannot be imported. The issue is either from git configuration, a malformed URL, " +
                    "or file system corruption. Please contact support for assistance.",
                    e);
        } finally {
            if (git != null) {
                git.close();
            }
            if (credentialsHaveBeenSet) {
                clearCredentials();
            }
        }
    }

    private void cloneRepository(GitConnection git, String remoteName, String url, DtoFactory dtoFactory, boolean recursiveEnabled)
            throws ServerException, UnauthorizedException, URISyntaxException {
        final CloneRequest request = dtoFactory.createDto(CloneRequest.class)
                                               .withRemoteName(remoteName)
                                               .withRemoteUri(url)
                                               .withRecursive(recursiveEnabled);
        git.clone(request);
    }

    private void initRepository(GitConnection git, DtoFactory dtoFactory) throws GitException {
        final InitRequest request = dtoFactory.createDto(InitRequest.class).withBare(false);
        git.init(request);
    }

    private void addRemote(GitConnection git, String name, String url, DtoFactory dtoFactory) throws GitException {
        final RemoteAddRequest request = dtoFactory.createDto(RemoteAddRequest.class).withName(name).withUrl(url);
        git.remoteAdd(request);
    }

    private void fetch(GitConnection git, String remote, DtoFactory dtoFactory) throws UnauthorizedException, GitException {
        final FetchRequest request = dtoFactory.createDto(FetchRequest.class).withRemote(remote);
        git.fetch(request);
    }

    private void fetchBranch(GitConnection gitConnection, String remote, String branch, DtoFactory dtoFactory)
            throws UnauthorizedException, GitException {

        final List<String> refSpecs = Collections.singletonList(String.format("refs/heads/%1$s:refs/remotes/origin/%1$s", branch));
        try {
            fetchRefSpecs(gitConnection, remote, refSpecs, dtoFactory);
        } catch (GitException e) {
            LOG.warn("Git exception on branch fetch", e);
            throw new GitException(
                    String.format("Unable to fetch remote branch %s. Make sure it exists and can be accessed.", branch),
                    e);
        }
    }

    private void fetchRefSpecs(GitConnection git, String remote, List<String> refSpecs, DtoFactory dtoFactory)
            throws UnauthorizedException, GitException {
        final FetchRequest request = dtoFactory.createDto(FetchRequest.class).withRemote(remote).withRefSpec(refSpecs);
        git.fetch(request);
    }

    private void checkoutCommit(GitConnection git, String commit, DtoFactory dtoFactory) throws GitException {
        final CheckoutRequest request = dtoFactory.createDto(CheckoutRequest.class).withName("temp")
                                                  .withCreateNew(true)
                                                  .withStartPoint(commit);
        try {
            git.checkout(request);
        } catch (GitException e) {
            LOG.warn("Git exception on commit checkout", e);
            throw new GitException(
                    String.format("Unable to checkout commit %s. Make sure it exists and can be accessed.", commit), e);
        }
    }

    private void checkoutBranch(GitConnection git,
                                String projectName,
                                String branchName,
                                String startPoint,
                                DtoFactory dtoFactory) throws GitException {
        final CheckoutRequest request = dtoFactory.createDto(CheckoutRequest.class).withName(branchName);
        final boolean branchExist = git.branchList(dtoFactory.createDto(BranchListRequest.class).withListMode(LIST_ALL))
                                       .stream()
                                       .anyMatch(branch -> branch.getDisplayName().equals("origin/" + branchName));
        final GitCheckoutEvent checkout = dtoFactory.createDto(GitCheckoutEvent.class)
                                                    .withWorkspaceId(WorkspaceIdProvider.getWorkspaceId())
                                                    .withProjectName(projectName);
        if (startPoint != null) {
            if (branchExist) {
                git.checkout(request);
                eventService.publish(checkout.withCheckoutOnly(true)
                                             .withBranchRef(getRemoteBranch(dtoFactory, git, branchName)));
            } else {
                checkoutAndRethrow(git, request.withCreateNew(true).withStartPoint(startPoint).withNoTrack(true),
                                   FAILED_CHECKOUT_WITH_START_POINT);
                eventService.publish(checkout.withCheckoutOnly(false));
            }
        } else {
            checkoutAndRethrow(git, request, FAILED_CHECKOUT);
            eventService.publish(checkout.withCheckoutOnly(true)
                                         .withBranchRef(getRemoteBranch(dtoFactory, git, branchName)));
        }
    }

    private void checkoutAndRethrow(GitConnection git, CheckoutRequest request, int errorCode) throws GitException {
        try {
            git.checkout(request);
        } catch (GitException ex) {
            throw new GitException(ex.getMessage(), errorCode);
        }
    }

    private void cleanGit(File project) {
        IoUtil.deleteRecursive(new File(project, ".git"));
        new File(project, ".gitignore").delete();
    }

    private String getRemoteBranch(DtoFactory dtoFactory, GitConnection git, String branchName) throws GitException {
        final List<Branch> remotes = git.branchList(dtoFactory.createDto(BranchListRequest.class)
                                                              .withListMode(BranchListRequest.LIST_REMOTE));
        final Optional<Branch> first = remotes.stream()
                                              .filter(br -> branchName.equals(br.getName().substring(br.getName().lastIndexOf("/") + 1)))
                                              .findFirst();
        if (!first.isPresent()) {
            throw new GitException("Failed to get remote branch name", FAILED_CHECKOUT);
        }
        return first.get().getName();
    }
}
