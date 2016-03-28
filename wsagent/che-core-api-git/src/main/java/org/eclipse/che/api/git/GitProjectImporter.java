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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.core.util.LineConsumerFactory;
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
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.eclipse.che.api.core.ErrorCodes.FAILED_CHECKOUT;
import static org.eclipse.che.api.core.ErrorCodes.FAILED_CHECKOUT_WITH_START_POINT;
import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_ALL;

/**
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class GitProjectImporter implements ProjectImporter {

    private final GitConnectionFactory gitConnectionFactory;
    private static final Logger LOG = LoggerFactory.getLogger(GitProjectImporter.class);
    private final EventService         eventService;

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
        try {
            // For factory: checkout particular commit after clone
            String commitId = null;
            // For factory: github pull request feature
            String remoteOriginFetch = null;
            String branch = null;
            String startPoint = null;
            // For factory or probably for our projects templates:
            // If git repository contains more than one project need clone all repository but after cloning keep just
            // sub-project that is specified in parameter "keepDirectory".
            String keepDirectory = null;
            // For factory and for our projects templates:
            // Keep all info related to the vcs. In case of Git: ".git" directory and ".gitignore" file.
            // Delete vcs info if false.
            String branchMerge = null;
            boolean keepVcs = true;

            Map<String, String> parameters = storage.getParameters();
            if (parameters != null) {
                commitId = parameters.get("commitId");
                branch = parameters.get("branch");
                startPoint = parameters.get("startPoint");
                remoteOriginFetch = parameters.get("remoteOriginFetch");
                keepDirectory = parameters.get("keepDirectory");
                if (parameters.containsKey("keepVcs")) {
                    keepVcs = Boolean.parseBoolean(parameters.get("keepVcs"));
                }
                branchMerge = parameters.get("branchMerge");
            }
            // Get path to local file. Git works with local filesystem only.
            final String localPath = baseFolder.getVirtualFile().toIoFile().getAbsolutePath();
            final DtoFactory dtoFactory = DtoFactory.getInstance();
            final String location = storage.getLocation();
            final String projectName = baseFolder.getName();
            if (keepDirectory != null) {
                final File temp = Files.createTempDirectory(null).toFile();
                try {
                    git = gitConnectionFactory.getConnection(temp, consumerFactory);
                    sparsecheckout(git, projectName, location, branch == null ? "master" : branch, startPoint, keepDirectory, dtoFactory);
                    // Copy content of directory to the project folder.
                    final File projectDir = new File(localPath);
                    IoUtil.copy(temp, projectDir, IoUtil.ANY_FILTER);
                } finally {
                    FileCleaner.addFile(temp);
                }
            } else {

                git = gitConnectionFactory.getConnection(localPath, consumerFactory);
                if (baseFolder.getChildren().size() == 0) {
                    cloneRepository(git, "origin", location, dtoFactory);
                    if (commitId != null) {
                        checkoutCommit(git, commitId, dtoFactory);
                    } else if (remoteOriginFetch != null) {
                        git.getConfig().add("remote.origin.fetch", remoteOriginFetch);
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
                    } else if (remoteOriginFetch != null) {
                        git.getConfig().add("remote.origin.fetch", remoteOriginFetch);
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
                if (!keepVcs) {
                    cleanGit(git.getWorkingDir());
                }
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
        }
    }

    private void cloneRepository(GitConnection git, String remoteName, String url, DtoFactory dtoFactory)
            throws ServerException, UnauthorizedException, URISyntaxException {
        final CloneRequest request = dtoFactory.createDto(CloneRequest.class).withRemoteName(remoteName).withRemoteUri(url);
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
                                       .anyMatch(branch -> branch.getName().equals(branchName));
        final GitCheckoutEvent checkout = dtoFactory.createDto(GitCheckoutEvent.class)
                                                    .withWorkspaceId(System.getenv("CHE_WORKSPACE_ID"))
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

    private void sparsecheckout(GitConnection git,
                                String projectName,
                                String url,
                                String branch,
                                String startPoint,
                                String directory,
                                DtoFactory dtoFactory)
            throws GitException, UnauthorizedException {
        /*
        Does following sequence of Git commands:
        $ git init
        $ git remote add origin <URL>
        $ git config core.sparsecheckout true
        $ echo keepDirectory >> .git/info/sparse-checkout
        $ git pull origin master
        */
        initRepository(git, dtoFactory);
        addRemote(git, "origin", url, dtoFactory);
        git.getConfig().add("core.sparsecheckout", "true");
        final File workingDir = git.getWorkingDir();
        final File sparseCheckout = new File(workingDir, ".git" + File.separator + "info" + File.separator + "sparse-checkout");
        try (BufferedWriter writer = Files.newBufferedWriter(sparseCheckout.toPath(), Charset.forName("UTF-8"))) {
            writer.write(directory.startsWith(File.separator) ? directory : File.separator + directory);
        } catch (IOException e) {
            throw new GitException(e);
        }
        fetchBranch(git, "origin", branch, dtoFactory);
        checkoutBranch(git, projectName, branch, startPoint, dtoFactory);
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
