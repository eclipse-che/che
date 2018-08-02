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
package org.eclipse.che.api.git;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.core.ErrorCodes.FAILED_CHECKOUT;
import static org.eclipse.che.api.core.ErrorCodes.FAILED_CHECKOUT_WITH_START_POINT;
import static org.eclipse.che.api.fs.server.WsPathUtils.nameOf;
import static org.eclipse.che.api.git.GitBasicAuthenticationCredentialsProvider.clearCredentials;
import static org.eclipse.che.api.git.GitBasicAuthenticationCredentialsProvider.setCurrentCredentials;
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_ALL;
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_REMOTE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.eclipse.che.WorkspaceIdProvider;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.CheckoutParams;
import org.eclipse.che.api.git.params.CloneParams;
import org.eclipse.che.api.git.params.FetchParams;
import org.eclipse.che.api.git.params.RemoteAddParams;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.event.GitCheckoutEvent;
import org.eclipse.che.api.project.server.ProjectImporter;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Vladyslav Zhukovskii */
@Singleton
public class GitProjectImporter implements ProjectImporter {

  private static final Logger LOG = LoggerFactory.getLogger(GitProjectImporter.class);

  private final GitConnectionFactory gitConnectionFactory;
  private final EventService eventService;
  private final FsManager fsManager;
  private final PathTransformer pathTransformer;

  @Inject
  public GitProjectImporter(
      GitConnectionFactory gitConnectionFactory,
      EventService eventService,
      FsManager fsManager,
      PathTransformer pathTransformer) {
    this.gitConnectionFactory = gitConnectionFactory;
    this.eventService = eventService;
    this.fsManager = fsManager;
    this.pathTransformer = pathTransformer;
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
  public SourceCategory getSourceCategory() {
    return SourceCategory.VCS;
  }

  @Override
  public void doImport(SourceStorage src, String dst)
      throws ForbiddenException, ConflictException, UnauthorizedException, IOException,
          ServerException, NotFoundException {
    doImport(src, dst, null);
  }

  @Override
  public void doImport(SourceStorage src, String dst, Supplier<LineConsumer> supplier)
      throws ForbiddenException, ConflictException, UnauthorizedException, IOException,
          ServerException, NotFoundException {
    if (supplier == null) {
      supplier = () -> LineConsumer.DEV_NULL;
    }

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
      // If git repository contains more than one project need clone all repository but after
      // cloning keep just
      // sub-project that is specified in parameter "keepDir".
      String keepDir = null;
      // For factory and for our projects templates:
      // Keep all info related to the vcs. In case of Git: ".git" directory and ".gitignore" file.
      // Delete vcs info if false.
      String branchMerge = null;
      boolean keepVcs = true;
      boolean recursiveEnabled = false;
      boolean convertToTopLevelProject = false;

      Map<String, String> parameters = src.getParameters();
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
        // convertToTopLevelProject feature is working only if we don't need any git information
        // and when we are working in git sparse checkout mode.
        if (!keepVcs
            && !isNullOrEmpty(keepDir)
            && parameters.containsKey("convertToTopLevelProject")) {
          convertToTopLevelProject =
              Boolean.parseBoolean(parameters.get("convertToTopLevelProject"));
        }
        branchMerge = parameters.get("branchMerge");
        final String user = src.getParameters().remove("username");
        final String pass = src.getParameters().remove("password");
        if (user != null && pass != null) {
          credentialsHaveBeenSet = true;
          setCurrentCredentials(user, pass);
        }
      }
      // Get path to local file. Git works with local filesystem only.
      final String localPath = pathTransformer.transform(dst).toString();
      final String location = src.getLocation();
      final String projectName = nameOf(dst);

      // Converting steps
      // 1. Clone to temporary folder on same device with /projects
      // 2. Remove git information
      // 3. Move to path requested by user.
      // Very important to have initial clone folder on the same drive with /project
      // otherwise we will have to replace atomic move with copy-delete operation.
      if (convertToTopLevelProject) {
        File tempDir = new File(new File(localPath).getParent(), NameGenerator.generate(".che", 6));
        git = gitConnectionFactory.getConnection(tempDir, supplier::get);
      } else {
        git = gitConnectionFactory.getConnection(localPath, supplier::get);
      }

      if (keepDir != null) {
        git.cloneWithSparseCheckout(keepDir, location);
        if (branch != null) {
          git.checkout(CheckoutParams.create(branch));
        }
      } else {
        if (fsManager.getAllChildrenNames(dst).isEmpty()) {
          cloneRepository(git, "origin", location, recursiveEnabled);
          if (commitId != null) {
            checkoutCommit(git, commitId);
          } else if (fetch != null) {
            git.getConfig().add("remote.origin.fetch", fetch);
            fetch(git, "origin");
            if (branch != null) {
              checkoutBranch(git, projectName, branch, startPoint);
            }
          } else if (branch != null) {
            checkoutBranch(git, projectName, branch, startPoint);
          }
        } else {
          git.init(false);
          addRemote(git, "origin", location);
          if (commitId != null) {
            fetchBranch(git, "origin", branch == null ? "*" : branch);
            checkoutCommit(git, commitId);
          } else if (fetch != null) {
            git.getConfig().add("remote.origin.fetch", fetch);
            fetch(git, "origin");
            if (branch != null) {
              checkoutBranch(git, projectName, branch, startPoint);
            }
          } else {
            fetchBranch(git, "origin", branch == null ? "*" : branch);

            List<Branch> branchList = git.branchList(LIST_REMOTE);
            if (!branchList.isEmpty()) {
              checkoutBranch(git, projectName, branch == null ? "master" : branch, startPoint);
            }
          }
        }
        if (branchMerge != null) {
          git.getConfig()
              .set("branch." + (branch == null ? "master" : branch) + ".merge", branchMerge);
        }
      }
      if (!keepVcs) {
        cleanGit(git.getWorkingDir());
      }
      if (convertToTopLevelProject) {
        Files.move(
            new File(git.getWorkingDir(), keepDir).toPath(),
            new File(localPath).toPath(),
            StandardCopyOption.ATOMIC_MOVE);
        IoUtil.deleteRecursive(git.getWorkingDir());
      }

    } catch (URISyntaxException e) {
      throw new ServerException(
          "Your project cannot be imported. The issue is either from git configuration, a malformed URL, "
              + "or file system corruption. Please contact support for assistance.",
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

  private void cloneRepository(
      GitConnection git, String remoteName, String url, boolean recursiveEnabled)
      throws ServerException, UnauthorizedException, URISyntaxException {
    final CloneParams params =
        CloneParams.create(url).withRemoteName(remoteName).withRecursive(recursiveEnabled);
    git.clone(params);
  }

  private void addRemote(GitConnection git, String name, String url) throws GitException {
    git.remoteAdd(RemoteAddParams.create(name, url));
  }

  private void fetch(GitConnection git, String remote) throws UnauthorizedException, GitException {
    final FetchParams params = FetchParams.create(remote);
    git.fetch(params);
  }

  private void fetchBranch(GitConnection gitConnection, String remote, String branch)
      throws UnauthorizedException, GitException {

    final List<String> refSpecs =
        Collections.singletonList(
            String.format("refs/heads/%1$s:refs/remotes/origin/%1$s", branch));
    try {
      fetchRefSpecs(gitConnection, remote, refSpecs);
    } catch (GitException e) {
      LOG.warn("Git exception on branch fetch", e);
      throw new GitException(
          String.format(
              "Unable to fetch remote branch %s. Make sure it exists and can be accessed.", branch),
          e);
    }
  }

  private void fetchRefSpecs(GitConnection git, String remote, List<String> refSpecs)
      throws UnauthorizedException, GitException {
    final FetchParams params = FetchParams.create(remote).withRefSpec(refSpecs);
    git.fetch(params);
  }

  private void checkoutCommit(GitConnection git, String commit) throws GitException {
    final CheckoutParams params =
        CheckoutParams.create("temp").withCreateNew(true).withStartPoint(commit);
    try {
      git.checkout(params);
    } catch (GitException e) {
      LOG.warn("Git exception on commit checkout", e);
      throw new GitException(
          String.format(
              "Unable to checkout commit %s. Make sure it exists and can be accessed.", commit),
          e);
    }
  }

  private void checkoutBranch(
      GitConnection git, String projectName, String branchName, String startPoint)
      throws GitException {
    final CheckoutParams params = CheckoutParams.create(branchName);
    final boolean branchExist =
        git.branchList(LIST_ALL)
            .stream()
            .anyMatch(branch -> branch.getDisplayName().equals("origin/" + branchName));
    final GitCheckoutEvent checkout =
        newDto(GitCheckoutEvent.class)
            .withWorkspaceId(WorkspaceIdProvider.getWorkspaceId())
            .withProjectName(projectName);
    if (startPoint != null) {
      if (branchExist) {
        git.checkout(params);
        eventService.publish(
            checkout.withCheckoutOnly(true).withBranchRef(getRemoteBranch(git, branchName)));
      } else {
        checkoutAndRethrow(
            git,
            params.withCreateNew(true).withStartPoint(startPoint).withNoTrack(true),
            FAILED_CHECKOUT_WITH_START_POINT);
        eventService.publish(checkout.withCheckoutOnly(false));
      }
    } else {
      checkoutAndRethrow(git, params, FAILED_CHECKOUT);
      eventService.publish(
          checkout.withCheckoutOnly(true).withBranchRef(getRemoteBranch(git, branchName)));
    }
  }

  private void checkoutAndRethrow(GitConnection git, CheckoutParams params, int errorCode)
      throws GitException {
    try {
      git.checkout(params);
    } catch (GitException ex) {
      throw new GitException(ex.getMessage(), errorCode);
    }
  }

  private void cleanGit(File project) {
    IoUtil.deleteRecursive(new File(project, ".git"));
    new File(project, ".gitignore").delete();
  }

  private String getRemoteBranch(GitConnection git, String branchName) throws GitException {
    final List<Branch> remotes = git.branchList(LIST_REMOTE);
    final Optional<Branch> first =
        remotes
            .stream()
            .filter(
                br -> branchName.equals(br.getName().substring(br.getName().lastIndexOf("/") + 1)))
            .findFirst();
    if (!first.isPresent()) {
      throw new GitException("Failed to get remote branch name", FAILED_CHECKOUT);
    }
    return first.get().getName();
  }
}
