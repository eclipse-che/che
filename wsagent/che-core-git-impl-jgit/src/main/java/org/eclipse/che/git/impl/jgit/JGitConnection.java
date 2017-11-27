/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *   SAP           - implementation
 */
package org.eclipse.che.git.impl.jgit;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_ALL;
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_LOCAL;
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_REMOTE;
import static org.eclipse.che.api.git.shared.EditedRegionType.DELETION;
import static org.eclipse.che.api.git.shared.EditedRegionType.INSERTION;
import static org.eclipse.che.api.git.shared.EditedRegionType.MODIFICATION;
import static org.eclipse.che.api.git.shared.ProviderInfo.AUTHENTICATE_URL;
import static org.eclipse.che.api.git.shared.ProviderInfo.PROVIDER_NAME;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.jgit.api.RebaseResult.Status.STOPPED;
import static org.eclipse.jgit.api.RebaseResult.Status.UNCOMMITTED_CHANGES;
import static org.eclipse.jgit.api.RebaseResult.Status.UP_TO_DATE;
import static org.eclipse.jgit.diff.Edit.Type.DELETE;
import static org.eclipse.jgit.lib.Constants.DEFAULT_REMOTE_NAME;
import static org.eclipse.jgit.lib.Constants.FETCH_HEAD;
import static org.eclipse.jgit.lib.Constants.HEAD;
import static org.eclipse.jgit.lib.Constants.R_HEADS;
import static org.eclipse.jgit.lib.Constants.R_REMOTES;
import static org.eclipse.jgit.lib.Constants.R_TAGS;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.net.ssl.SSLHandshakeException;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.git.Config;
import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.api.git.DiffPage;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitUrlUtils;
import org.eclipse.che.api.git.GitUserResolver;
import org.eclipse.che.api.git.LogPage;
import org.eclipse.che.api.git.UserCredential;
import org.eclipse.che.api.git.exception.GitConflictException;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.exception.GitInvalidRefNameException;
import org.eclipse.che.api.git.exception.GitRefAlreadyExistsException;
import org.eclipse.che.api.git.exception.GitRefNotFoundException;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CheckoutParams;
import org.eclipse.che.api.git.params.CloneParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.params.DiffParams;
import org.eclipse.che.api.git.params.FetchParams;
import org.eclipse.che.api.git.params.LogParams;
import org.eclipse.che.api.git.params.LsFilesParams;
import org.eclipse.che.api.git.params.PullParams;
import org.eclipse.che.api.git.params.PushParams;
import org.eclipse.che.api.git.params.RemoteAddParams;
import org.eclipse.che.api.git.params.RemoteUpdateParams;
import org.eclipse.che.api.git.params.ResetParams;
import org.eclipse.che.api.git.params.RmParams;
import org.eclipse.che.api.git.params.TagCreateParams;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.DiffCommitFile;
import org.eclipse.che.api.git.shared.EditedRegion;
import org.eclipse.che.api.git.shared.EditedRegionType;
import org.eclipse.che.api.git.shared.GitUser;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.ProviderInfo;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.RebaseResponse;
import org.eclipse.che.api.git.shared.RebaseResponse.RebaseStatus;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.RemoteReference;
import org.eclipse.che.api.git.shared.RevertResult;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusChangedEventDto;
import org.eclipse.che.api.git.shared.Tag;
import org.eclipse.che.api.git.shared.event.GitRepositoryInitializedEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.proxy.ProxyAuthenticator;
import org.eclipse.che.plugin.ssh.key.script.SshKeyProvider;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.RevertCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RefUpdate.Result;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.merge.ResolveMerger.MergeFailureReason;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.eclipse.jgit.util.io.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrey Parfonov
 * @author Igor Vinokur
 * @author Mykola Morhun
 */
class JGitConnection implements GitConnection {
  private static final String REBASE_OPERATION_SKIP = "SKIP";
  private static final String REBASE_OPERATION_CONTINUE = "CONTINUE";
  private static final String REBASE_OPERATION_ABORT = "ABORT";

  // Push Response Constants
  private static final String BRANCH_REFSPEC_SEPERATOR = " -> ";
  private static final String REFSPEC_COLON = ":";
  private static final String KEY_COMMIT_MESSAGE = "Message";
  private static final String KEY_RESULT = "Result";
  private static final String KEY_REMOTENAME = "RemoteName";
  private static final String KEY_LOCALNAME = "LocalName";

  private static final String ERROR_UPDATE_REMOTE_NAME_MISSING =
      "Update operation failed, remote name is required.";
  private static final String ERROR_UPDATE_REMOTE_REMOVE_INVALID_URL =
      "remoteUpdate: Ignore this error. Cannot remove invalid URL.";

  private static final String ERROR_ADD_REMOTE_NAME_ALREADY_EXISTS =
      "Add Remote operation failed, remote name %s already exists.";
  private static final String ERROR_ADD_REMOTE_NAME_MISSING =
      "Add operation failed, remote name is required.";
  private static final String ERROR_ADD_REMOTE_URL_MISSING =
      "Add Remote operation failed, Remote url required.";

  private static final String ERROR_PULL_MERGING =
      "Could not pull because the repository state is 'MERGING'.";
  private static final String ERROR_PULL_HEAD_DETACHED = "Could not pull because HEAD is detached.";
  private static final String ERROR_PULL_REF_MISSING =
      "Could not pull because remote ref is missing for branch %s.";
  private static final String ERROR_PULL_UNCOMMITED_CHANGES =
      "Could not pull with rebase because uncommited changes are present.";
  private static final String ERROR_PULL_AUTO_MERGE_FAILED =
      "Automatic merge failed; fix conflicts and then commit the result.";
  private static final String ERROR_PULL_AUTO_REBASE_FAILED =
      "Pull with automatic rebase failed due to a conflict.";
  private static final String ERROR_PULL_MERGE_CONFLICT_IN_FILES =
      "Could not pull because a merge conflict is detected in the files:";
  private static final String ERROR_PULL_COMMIT_BEFORE_MERGE =
      "Could not pull. Commit your changes before merging.";

  private static final String ERROR_CHECKOUT_BRANCH_NAME_EXISTS =
      "A branch named '%s' already exists.";
  private static final String ERROR_CHECKOUT_BRANCH_NAME_EXISTS_IN_SEVERAL_REMOTES =
      "A branch named '%s' exists in more than one remote repos";
  private static final String ERROR_CHECKOUT_CONFLICT =
      "Checkout operation failed, the following files would be " + "overwritten by merge:";

  private static final String ERROR_PUSH_CONFLICTS_PRESENT =
      "failed to push '%s' to '%s'. Try to merge "
          + "remote changes using pull, and then push again.";
  private static final String INFO_PUSH_IGNORED_UP_TO_DATE = "Everything up-to-date";

  private static final String ERROR_AUTHENTICATION_REQUIRED =
      "Authentication is required but no CredentialsProvider has been registered";
  private static final String ERROR_AUTHENTICATION_FAILED =
      "fatal: Authentication failed for '%s/'" + lineSeparator();

  private static final String ERROR_TAG_DELETE =
      "Could not delete the tag %1$s. An error occurred: %2$s.";
  private static final String ERROR_LOG_NO_HEAD_EXISTS =
      "No HEAD exists and no explicit starting revision was specified";
  private static final String ERROR_INIT_FOLDER_MISSING = "The working folder %s does not exist.";
  private static final String ERROR_NO_REMOTE_REPOSITORY =
      "No remote repository specified.  Please, specify either a "
          + "URL or a remote name from which new revisions should be "
          + "fetched in request.";

  private static final String MESSAGE_COMMIT_NOT_POSSIBLE =
      "Commit is not possible because repository state is '%s'";
  private static final String MESSAGE_COMMIT_AMEND_NOT_POSSIBLE =
      "Amend is not possible because repository state is '%s'";

  private static final String FILE_NAME_TOO_LONG_ERROR_PREFIX = "File name too long";

  private static final Pattern GIT_URL_WITH_CREDENTIALS_PATTERN =
      Pattern.compile("https?://[^:]+:[^@]+@.*");

  private static final Logger LOG = LoggerFactory.getLogger(JGitConnection.class);

  private Git git;
  private JGitConfigImpl config;
  private LineConsumerFactory lineConsumerFactory;

  private final CredentialsLoader credentialsLoader;
  private final SshKeyProvider sshKeyProvider;
  private final EventService eventService;
  private final GitUserResolver userResolver;
  private final Repository repository;

  @Inject
  JGitConnection(
      Repository repository,
      CredentialsLoader credentialsLoader,
      SshKeyProvider sshKeyProvider,
      EventService eventService,
      GitUserResolver userResolver) {
    this.repository = repository;
    this.credentialsLoader = credentialsLoader;
    this.sshKeyProvider = sshKeyProvider;
    this.eventService = eventService;
    this.userResolver = userResolver;
  }

  @Override
  public void add(AddParams params) throws GitException {
    AddCommand addCommand = getGit().add().setUpdate(params.isUpdate());

    List<String> filePatterns = params.getFilePattern();
    if (filePatterns.isEmpty()) {
      filePatterns = AddRequest.DEFAULT_PATTERN;
    }
    filePatterns.forEach(addCommand::addFilepattern);

    try {
      addCommand.call();

      addDeletedFilesToIndex(filePatterns);
    } catch (GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  /** To add deleted files in index it is required to perform git rm on them */
  private void addDeletedFilesToIndex(List<String> filePatterns) throws GitAPIException {
    Set<String> deletedFiles = getGit().status().call().getMissing();
    if (!deletedFiles.isEmpty()) {
      RmCommand rmCommand = getGit().rm();
      if (filePatterns.contains(".")) {
        deletedFiles.forEach(rmCommand::addFilepattern);
      } else {
        filePatterns.forEach(
            filePattern ->
                deletedFiles
                    .stream()
                    .filter(deletedFile -> deletedFile.startsWith(filePattern))
                    .forEach(rmCommand::addFilepattern));
      }
      rmCommand.call();
    }
  }

  @Override
  public void checkout(CheckoutParams params) throws GitException {
    CheckoutCommand checkoutCommand = getGit().checkout();
    String startPoint = params.getStartPoint();
    String name = params.getName();
    String trackBranch = params.getTrackBranch();

    // checkout files?
    List<String> files = params.getFiles();
    boolean shouldCheckoutToFile = name != null && new File(getWorkingDir(), name).exists();
    if (shouldCheckoutToFile || !files.isEmpty()) {
      if (shouldCheckoutToFile) {
        checkoutCommand.addPath(params.getName());
      } else {
        files.forEach(checkoutCommand::addPath);
      }
    } else {
      // checkout branch
      if (startPoint != null && trackBranch != null) {
        throw new GitException("Start point and track branch can not be used together.");
      }
      if (params.isCreateNew() && name == null) {
        throw new GitException("Branch name must be set when createNew equals to true.");
      }
      if (startPoint != null) {
        checkoutCommand.setStartPoint(startPoint);
      }
      if (params.isCreateNew()) {
        checkoutCommand.setCreateBranch(true);
        checkoutCommand.setName(name);
      } else if (name != null) {
        checkoutCommand.setName(name);
        List<String> localBranches =
            branchList(LIST_LOCAL)
                .stream()
                .map(Branch::getDisplayName)
                .collect(Collectors.toList());
        if (!localBranches.contains(name)) {
          List<Branch> remoteBranchesWithGivenName =
              branchList(LIST_REMOTE)
                  .stream()
                  .filter(
                      branch -> {
                        String branchName = branch.getName();
                        return name.equals(branchName.substring(branchName.lastIndexOf("/") + 1));
                      })
                  .collect(Collectors.toList());
          if (remoteBranchesWithGivenName.size() > 1) {
            throw new GitException(
                String.format(ERROR_CHECKOUT_BRANCH_NAME_EXISTS_IN_SEVERAL_REMOTES, name));
          } else if (remoteBranchesWithGivenName.size() == 1) {
            checkoutCommand.setCreateBranch(true);
            checkoutCommand.setStartPoint(remoteBranchesWithGivenName.get(0).getName());
          }
        }
      }
      if (trackBranch != null) {
        if (name == null) {
          checkoutCommand.setName(cleanRemoteName(trackBranch));
        }
        checkoutCommand.setCreateBranch(true);
        checkoutCommand.setStartPoint(trackBranch);
      }
      checkoutCommand.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM);
    }
    try {
      checkoutCommand.call();
    } catch (CheckoutConflictException exception) {
      throw new GitConflictException(exception.getMessage(), exception.getConflictingPaths());
    } catch (RefAlreadyExistsException exception) {
      throw new GitRefAlreadyExistsException(exception.getMessage());
    } catch (RefNotFoundException exception) {
      throw new GitRefNotFoundException(exception.getMessage());
    } catch (InvalidRefNameException exception) {
      throw new GitInvalidRefNameException(exception.getMessage());
    } catch (GitAPIException exception) {
      if (exception.getMessage().endsWith("already exists")) {
        throw new GitException(
            format(
                ERROR_CHECKOUT_BRANCH_NAME_EXISTS,
                name != null ? name : cleanRemoteName(trackBranch)));
      }
      throw new GitException(exception.getMessage(), exception);
    }
  }

  @Override
  public Branch branchCreate(String name, String startPoint) throws GitException {
    CreateBranchCommand createBranchCommand = getGit().branchCreate().setName(name);
    if (startPoint != null) {
      createBranchCommand.setStartPoint(startPoint);
    }
    try {
      Ref brRef = createBranchCommand.call();
      String refName = brRef.getName();
      String displayName = Repository.shortenRefName(refName);
      return newDto(Branch.class)
          .withName(refName)
          .withDisplayName(displayName)
          .withActive(false)
          .withRemote(false);
    } catch (GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  @Override
  public void branchDelete(String name, boolean force) throws GitException {
    try {
      getGit().branchDelete().setBranchNames(name).setForce(force).call();
    } catch (GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  @Override
  public void branchRename(String oldName, String newName) throws GitException {
    try {
      getGit().branchRename().setOldName(oldName).setNewName(newName).call();
    } catch (GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  @Override
  public List<Branch> branchList(BranchListMode listMode) throws GitException {
    ListBranchCommand listBranchCommand = getGit().branchList();
    if (LIST_ALL == listMode || listMode == null) {
      listBranchCommand.setListMode(ListMode.ALL);
    } else if (LIST_REMOTE == listMode) {
      listBranchCommand.setListMode(ListMode.REMOTE);
    }
    List<Ref> refs;
    String currentRef;
    try {
      refs = listBranchCommand.call();
      String headBranch = getRepository().getBranch();
      Optional<Ref> currentTag =
          getGit()
              .tagList()
              .call()
              .stream()
              .filter(tag -> tag.getObjectId().getName().equals(headBranch))
              .findFirst();
      if (currentTag.isPresent()) {
        currentRef = currentTag.get().getName();
      } else {
        currentRef = "refs/heads/" + headBranch;
      }

    } catch (GitAPIException | IOException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
    List<Branch> branches = new ArrayList<>();
    for (Ref ref : refs) {
      String refName = ref.getName();
      boolean isCommitOrTag = HEAD.equals(refName);
      String branchName = isCommitOrTag ? currentRef : refName;
      String branchDisplayName;
      if (isCommitOrTag) {
        branchDisplayName = "(detached from " + Repository.shortenRefName(currentRef) + ")";
      } else {
        branchDisplayName = Repository.shortenRefName(refName);
      }
      Branch branch =
          newDto(Branch.class)
              .withName(branchName)
              .withActive(isCommitOrTag || refName.equals(currentRef))
              .withDisplayName(branchDisplayName)
              .withRemote(refName.startsWith("refs/remotes"));
      branches.add(branch);
    }
    return branches;
  }

  public void clone(CloneParams params) throws GitException, UnauthorizedException {
    String remoteUri = params.getRemoteUrl();
    boolean removeIfFailed = false;
    try {
      if (params.getRemoteName() == null) {
        params.setRemoteName(DEFAULT_REMOTE_NAME);
      }
      if (params.getWorkingDir() == null) {
        params.setWorkingDir(repository.getWorkTree().getCanonicalPath());
      }

      // If clone fails and the .git folder didn't exist we want to remove it.
      // We have to do this here because the clone command doesn't revert its own changes in case of
      // failure.
      removeIfFailed = !repository.getDirectory().exists();

      CloneCommand cloneCommand =
          Git.cloneRepository()
              .setDirectory(new File(params.getWorkingDir()))
              .setRemote(params.getRemoteName())
              .setCloneSubmodules(params.isRecursive())
              .setURI(remoteUri);
      if (params.getBranchesToFetch().isEmpty()) {
        cloneCommand.setCloneAllBranches(true);
      } else {
        cloneCommand.setBranchesToClone(params.getBranchesToFetch());
      }

      LineConsumer lineConsumer = lineConsumerFactory.newLineConsumer();
      cloneCommand.setProgressMonitor(
          new BatchingProgressMonitor() {
            @Override
            protected void onUpdate(String taskName, int workCurr) {
              try {
                lineConsumer.writeLine(taskName + ": " + workCurr + " completed");
              } catch (IOException exception) {
                LOG.error(exception.getMessage(), exception);
              }
            }

            @Override
            protected void onEndTask(String taskName, int workCurr) {}

            @Override
            protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
              try {
                lineConsumer.writeLine(
                    taskName
                        + ": "
                        + workCurr
                        + " of "
                        + workTotal
                        + " completed, "
                        + percentDone
                        + "% done");
              } catch (IOException exception) {
                LOG.error(exception.getMessage(), exception);
              }
            }

            @Override
            protected void onEndTask(
                String taskName, int workCurr, int workTotal, int percentDone) {}
          });

      ((Git)
              executeRemoteCommand(
                  remoteUri, cloneCommand, params.getUsername(), params.getPassword()))
          .close();

      StoredConfig repositoryConfig = getRepository().getConfig();
      GitUser gitUser = getUser();
      if (gitUser != null) {
        repositoryConfig.setString(
            ConfigConstants.CONFIG_USER_SECTION,
            null,
            ConfigConstants.CONFIG_KEY_NAME,
            gitUser.getName());
        repositoryConfig.setString(
            ConfigConstants.CONFIG_USER_SECTION,
            null,
            ConfigConstants.CONFIG_KEY_EMAIL,
            gitUser.getEmail());
      }
      repositoryConfig.save();
    } catch (IOException | GitAPIException exception) {
      // Delete .git directory in case it was created
      if (removeIfFailed) {
        deleteRepositoryFolder();
      }
      // TODO remove this when JGit will support HTTP 301 redirects,
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=465167
      // try to clone repository by replacing http to https in the url if HTTP 301 redirect happened
      if (exception.getMessage().contains(": 301 Moved Permanently")) {
        remoteUri = "https" + remoteUri.substring(4);
        try {
          clone(params.withRemoteUrl(remoteUri));
        } catch (UnauthorizedException | GitException e) {
          throw new GitException("Failed to clone the repository", e);
        }
        return;
      }
      String message = generateExceptionMessage(exception);
      throw new GitException(message, exception);
    }
  }

  @Override
  public Revision commit(CommitParams params) throws GitException {
    try {
      // Check repository state
      RepositoryState repositoryState = repository.getRepositoryState();
      if (!repositoryState.canCommit()) {
        throw new GitException(
            format(MESSAGE_COMMIT_NOT_POSSIBLE, repositoryState.getDescription()));
      }
      if (params.isAmend() && !repositoryState.canAmend()) {
        throw new GitException(
            format(MESSAGE_COMMIT_AMEND_NOT_POSSIBLE, repositoryState.getDescription()));
      }

      // Check committer
      GitUser committer = getUser();
      if (committer == null) {
        throw new GitException("Committer can't be null");
      }
      String committerName = committer.getName();
      String committerEmail = committer.getEmail();
      if (committerName == null || committerEmail == null) {
        throw new GitException(
            "Git user name and (or) email wasn't set",
            ErrorCodes.NO_COMMITTER_NAME_OR_EMAIL_DEFINED);
      }

      // Check commit message
      String message = params.getMessage();
      if (message == null) {
        throw new GitException("Message wasn't set");
      }

      List<String> files = params.getFiles();
      Status status = status(files);
      List<String> specified = files;

      List<String> staged = new ArrayList<>();
      staged.addAll(status.getAdded());
      staged.addAll(status.getChanged());
      staged.addAll(status.getRemoved());

      List<String> changed = new ArrayList<>(staged);
      changed.addAll(status.getModified());
      changed.addAll(status.getMissing());

      List<String> specifiedStaged =
          specified
              .stream()
              .filter(path -> staged.stream().anyMatch(s -> s.startsWith(path)))
              .collect(toList());

      List<String> specifiedChanged =
          specified
              .stream()
              .filter(path -> changed.stream().anyMatch(c -> c.startsWith(path)))
              .collect(toList());

      // Check that there are changes present for commit, if 'isAmend' is disabled
      if (!params.isAmend()) {
        // Check that there are staged changes present for commit, or any changes if 'isAll' is
        // enabled
        if (status.isClean()) {
          throw new GitException("Nothing to commit, working directory clean");
        } else if (!params.isAll()
            && (specified.isEmpty() ? staged.isEmpty() : specifiedStaged.isEmpty())) {
          throw new GitException("No changes added to commit");
        }
      } else {
        /*
        By default Jgit doesn't allow to commit not changed specified paths. According to setAllowEmpty method documentation,
        setting this flag to true must allow such commit, but it won't because Jgit has a bug:
        https://bugs.eclipse.org/bugs/show_bug.cgi?id=510685. As a workaround, specified paths of the commit command will contain
        only changed and specified paths. If other changes are present, but the list of changed and specified paths is empty,
        throw exception to prevent committing other paths. TODO Remove this check when the bug will be fixed.
        */
        if (!specified.isEmpty()
            && !(params.isAll() ? changed.isEmpty() : staged.isEmpty())
            && specifiedChanged.isEmpty()) {
          throw new GitException(
              format(
                  "Changes are present but not changed path%s specified for commit.",
                  specified.size() > 1 ? "s were" : " was"));
        }
      }

      // TODO add 'setAllowEmpty(params.isAmend())' when
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=510685 will be fixed
      CommitCommand commitCommand =
          getGit()
              .commit()
              .setCommitter(committerName, committerEmail)
              .setAuthor(committerName, committerEmail)
              .setMessage(message)
              .setAll(params.isAll())
              .setAmend(params.isAmend());

      if (!params.isAll()) {
        // TODO change to 'specified.forEach(commitCommand::setOnly)' when
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=510685 will be fixed. See description
        // above.
        specifiedChanged.forEach(commitCommand::setOnly);
      }

      // Check if repository is configured with Gerrit Support
      String gerritSupportConfigValue =
          repository
              .getConfig()
              .getString(
                  ConfigConstants.CONFIG_GERRIT_SECTION,
                  null,
                  ConfigConstants.CONFIG_KEY_CREATECHANGEID);
      boolean isGerritSupportConfigured =
          gerritSupportConfigValue != null ? Boolean.valueOf(gerritSupportConfigValue) : false;
      commitCommand.setInsertChangeId(isGerritSupportConfigured);
      RevCommit result = commitCommand.call();

      Map<String, List<EditedRegion>> modifiedFiles = new HashMap<>();
      for (String file : status.getChanged()) {
        modifiedFiles.put(file, getEditedRegions(file));
      }
      // Need to fire this event because with the help of the file watchers we can not detect JGit's
      // commit operation completion.
      eventService.publish(
          newDto(StatusChangedEventDto.class)
              .withStatus(status(emptyList()))
              .withModifiedFiles(modifiedFiles)
              .withProjectName(repository.getWorkTree().getName()));

      GitUser gitUser = newDto(GitUser.class).withName(committerName).withEmail(committerEmail);

      return newDto(Revision.class)
          .withBranch(getCurrentBranch())
          .withId(result.getId().getName())
          .withMessage(result.getFullMessage())
          .withCommitTime(MILLISECONDS.convert(result.getCommitTime(), SECONDS))
          .withCommitter(gitUser);
    } catch (GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  @Override
  public DiffPage diff(DiffParams params) throws GitException {
    return new JGitDiffPage(params, repository);
  }

  @Override
  public List<EditedRegion> getEditedRegions(String filePath) throws GitException {
    try (ObjectReader reader = repository.newObjectReader();
        RevWalk revWalk = new RevWalk(repository);
        DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
      formatter.setRepository(repository);
      formatter.setPathFilter(PathFilter.create(filePath));

      Ref headRef = repository.exactRef(HEAD);
      RevCommit commit = revWalk.parseCommit(headRef.getObjectId());
      RevTree tree = revWalk.parseTree(commit.getTree().getId());
      CanonicalTreeParser treeParser = new CanonicalTreeParser();
      treeParser.reset(reader, tree);

      Optional<DiffEntry> optional =
          formatter.scan(treeParser, new FileTreeIterator(repository)).stream().findAny();
      if (optional.isPresent()) {
        EditList edits = formatter.toFileHeader(optional.get()).getHunks().get(0).toEditList();
        return edits
            .stream()
            .map(
                edit -> {
                  EditedRegionType type = null;
                  switch (edit.getType()) {
                    case INSERT:
                      {
                        type = INSERTION;
                        break;
                      }
                    case REPLACE:
                      {
                        type = MODIFICATION;
                        break;
                      }
                    case DELETE:
                      {
                        type = DELETION;
                        break;
                      }
                    case EMPTY:
                      {
                        break;
                      }
                  }
                  return newDto(EditedRegion.class)
                      .withBeginLine(
                          edit.getType() == DELETE ? edit.getBeginB() : edit.getBeginB() + 1)
                      .withEndLine(edit.getEndB())
                      .withType(type);
                })
            .collect(toList());
      }
    } catch (Exception e) {
      throw new GitException(e.getMessage());
    }
    return Collections.emptyList();
  }

  @Override
  public boolean isInsideWorkTree() throws GitException {
    return RepositoryCache.FileKey.isGitRepository(getRepository().getDirectory(), FS.DETECTED);
  }

  @Override
  public ShowFileContentResponse showFileContent(String file, String version) throws GitException {
    String content;
    ObjectId revision;
    try {
      revision = getRepository().resolve(version);
      try (RevWalk revWalk = new RevWalk(getRepository())) {
        RevCommit revCommit = revWalk.parseCommit(revision);
        RevTree tree = revCommit.getTree();

        try (TreeWalk treeWalk = new TreeWalk(getRepository())) {
          treeWalk.addTree(tree);
          treeWalk.setRecursive(true);
          treeWalk.setFilter(PathFilter.create(file));
          if (!treeWalk.next()) {
            throw new GitException(
                "fatal: Path '" + file + "' does not exist in '" + version + "'" + lineSeparator());
          }
          ObjectId objectId = treeWalk.getObjectId(0);
          ObjectLoader loader = repository.open(objectId);
          content = new String(loader.getBytes());
        }
      }
    } catch (IOException exception) {
      throw new GitException(exception.getMessage());
    }
    return newDto(ShowFileContentResponse.class).withContent(content);
  }

  @Override
  public void fetch(FetchParams params) throws GitException, UnauthorizedException {
    String remoteName = params.getRemote();
    String remoteUri;
    try {
      List<RefSpec> fetchRefSpecs;
      List<String> refSpec = params.getRefSpec();
      if (!refSpec.isEmpty()) {
        fetchRefSpecs = new ArrayList<>(refSpec.size());
        for (String refSpecItem : refSpec) {
          RefSpec fetchRefSpec =
              (refSpecItem.indexOf(':') < 0) //
                  ? new RefSpec(R_HEADS + refSpecItem + ":") //
                  : new RefSpec(refSpecItem);
          fetchRefSpecs.add(fetchRefSpec);
        }
      } else {
        fetchRefSpecs = emptyList();
      }

      FetchCommand fetchCommand = getGit().fetch();

      // If this an unknown remote with no refspecs given, put HEAD
      // (otherwise JGit fails)
      if (remoteName != null && refSpec.isEmpty()) {
        boolean found = false;
        List<Remote> configRemotes = remoteList(null, false);
        for (Remote configRemote : configRemotes) {
          if (remoteName.equals(configRemote.getName())) {
            found = true;
            break;
          }
        }
        if (!found) {
          fetchRefSpecs = singletonList(new RefSpec(HEAD + ":" + FETCH_HEAD));
        }
      }

      if (remoteName == null) {
        remoteName = DEFAULT_REMOTE_NAME;
      }
      fetchCommand.setRemote(remoteName);
      remoteUri =
          getRepository()
              .getConfig()
              .getString(
                  ConfigConstants.CONFIG_REMOTE_SECTION,
                  remoteName,
                  ConfigConstants.CONFIG_KEY_URL);
      fetchCommand.setRefSpecs(fetchRefSpecs);

      int timeout = params.getTimeout();
      if (timeout > 0) {
        fetchCommand.setTimeout(timeout);
      }
      fetchCommand.setRemoveDeletedRefs(params.isRemoveDeletedRefs());

      executeRemoteCommand(remoteUri, fetchCommand, params.getUsername(), params.getPassword());
    } catch (GitException | GitAPIException exception) {
      String errorMessage;
      if (exception.getMessage().contains("Invalid remote: ")) {
        errorMessage = ERROR_NO_REMOTE_REPOSITORY;
      } else if ("Nothing to fetch.".equals(exception.getMessage())) {
        return;
      } else {
        errorMessage = generateExceptionMessage(exception);
      }
      throw new GitException(errorMessage, exception);
    }
  }

  @Override
  public void init(boolean isBare) throws GitException {
    File workDir = repository.getWorkTree();
    if (!workDir.exists()) {
      throw new GitException(format(ERROR_INIT_FOLDER_MISSING, workDir));
    }
    // If create fails and the .git folder didn't exist we want to remove it.
    // We have to do this here because the create command doesn't revert its own changes in case of
    // failure.
    boolean removeIfFailed = !repository.getDirectory().exists();

    try {
      repository.create(isBare);
      eventService.publish(
          newDto(GitRepositoryInitializedEvent.class)
              .withProjectName(repository.getWorkTree().getName()));
    } catch (IOException exception) {
      if (removeIfFailed) {
        deleteRepositoryFolder();
      }
      throw new GitException(exception.getMessage(), exception);
    }
  }

  /** @see org.eclipse.che.api.git.GitConnection#log(LogParams) */
  @Override
  public LogPage log(LogParams params) throws GitException {
    LogCommand logCommand = getGit().log();
    try {
      setRevisionRange(logCommand, params);
      logCommand.setSkip(params.getSkip());
      logCommand.setMaxCount(params.getMaxCount());
      List<String> fileFilter = params.getFileFilter();
      if (fileFilter != null) {
        fileFilter.forEach(logCommand::addPath);
      }
      String filePath = params.getFilePath();
      if (!isNullOrEmpty(filePath)) {
        logCommand.addPath(filePath);
      }
      Iterator<RevCommit> revIterator = logCommand.call().iterator();
      List<Revision> commits = new ArrayList<>();
      while (revIterator.hasNext()) {
        RevCommit commit = revIterator.next();
        Revision revision = getRevision(commit, filePath);
        commits.add(revision);
      }
      return new LogPage(commits);
    } catch (GitAPIException | IOException exception) {
      String errorMessage = exception.getMessage();
      if (ERROR_LOG_NO_HEAD_EXISTS.equals(errorMessage)) {
        throw new GitException(errorMessage, ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED);
      } else {
        LOG.error("Failed to retrieve log. ", exception);
        throw new GitException(exception);
      }
    }
  }

  private Revision getRevision(RevCommit commit, String filePath)
      throws GitAPIException, IOException {
    List<String> commitParentsList =
        Stream.of(commit.getParents()).map(RevCommit::getName).collect(Collectors.toList());

    return newDto(Revision.class)
        .withId(commit.getId().getName())
        .withMessage(commit.getFullMessage())
        .withCommitTime((long) commit.getCommitTime() * 1000)
        .withCommitter(getCommitCommitter(commit))
        .withAuthor(getCommitAuthor(commit))
        .withBranches(getBranchesOfCommit(commit, ListMode.ALL))
        .withCommitParent(commitParentsList)
        .withDiffCommitFile(getCommitDiffFiles(commit, filePath));
  }

  private GitUser getCommitCommitter(RevCommit commit) {
    PersonIdent committerIdentity = commit.getCommitterIdent();
    return newDto(GitUser.class)
        .withName(committerIdentity.getName())
        .withEmail(committerIdentity.getEmailAddress());
  }

  private GitUser getCommitAuthor(RevCommit commit) {
    PersonIdent authorIdentity = commit.getAuthorIdent();
    return newDto(GitUser.class)
        .withName(authorIdentity.getName())
        .withEmail(authorIdentity.getEmailAddress());
  }

  private List<Branch> getBranchesOfCommit(RevCommit commit, ListMode mode) throws GitAPIException {
    List<Ref> branches =
        getGit().branchList().setListMode(mode).setContains(commit.getName()).call();
    return branches
        .stream()
        .map(branch -> newDto(Branch.class).withName(branch.getName()))
        .collect(toList());
  }

  private List<DiffCommitFile> getCommitDiffFiles(RevCommit revCommit, String pattern)
      throws IOException {
    List<DiffEntry> diffs;
    TreeFilter filter = null;
    if (!isNullOrEmpty(pattern)) {
      filter =
          AndTreeFilter.create(
              PathFilterGroup.createFromStrings(Collections.singleton(pattern)),
              TreeFilter.ANY_DIFF);
    }
    List<DiffCommitFile> commitFilesList = new ArrayList<>();
    try (TreeWalk tw = new TreeWalk(repository)) {
      tw.setRecursive(true);
      // get the current commit parent in order to compare it with the current commit
      // and to get the list of DiffEntry.
      if (revCommit.getParentCount() > 0) {
        RevCommit parent = parseCommit(revCommit.getParent(0));
        tw.reset(parent.getTree(), revCommit.getTree());
        if (filter != null) {
          tw.setFilter(filter);
        } else {
          tw.setFilter(TreeFilter.ANY_DIFF);
        }
        diffs = DiffEntry.scan(tw);
      } else {
        // If the current commit has no parents (which means it is the initial commit),
        // then create an empty tree and compare it to the current commit to get the
        // list of DiffEntry.
        try (RevWalk rw = new RevWalk(repository);
            DiffFormatter diffFormat = new DiffFormatter(NullOutputStream.INSTANCE)) {
          diffFormat.setRepository(repository);
          if (filter != null) {
            diffFormat.setPathFilter(filter);
          }
          diffs =
              diffFormat.scan(
                  new EmptyTreeIterator(),
                  new CanonicalTreeParser(null, rw.getObjectReader(), revCommit.getTree()));
        }
      }
    }
    if (diffs != null) {
      commitFilesList.addAll(
          diffs
              .stream()
              .map(
                  diff ->
                      newDto(DiffCommitFile.class)
                          .withOldPath(diff.getOldPath())
                          .withNewPath(diff.getNewPath())
                          .withChangeType(diff.getChangeType().name()))
              .collect(toList()));
    }
    return commitFilesList;
  }

  private RevCommit parseCommit(RevCommit revCommit) {
    try (RevWalk rw = new RevWalk(repository)) {
      return rw.parseCommit(revCommit);
    } catch (IOException exception) {
      LOG.error("Failed to parse commit. ", exception);
      return revCommit;
    }
  }

  private void setRevisionRange(LogCommand logCommand, LogParams params) throws IOException {
    if (params != null && logCommand != null) {
      String revisionRangeSince = params.getRevisionRangeSince();
      String revisionRangeUntil = params.getRevisionRangeUntil();
      if (revisionRangeSince != null && revisionRangeUntil != null) {
        ObjectId since = repository.resolve(revisionRangeSince);
        ObjectId until = repository.resolve(revisionRangeUntil);
        logCommand.addRange(since, until);
      }
    }
  }

  @Override
  public List<GitUser> getCommiters() throws GitException {
    List<GitUser> gitUsers = new ArrayList<>();
    try {
      LogCommand logCommand = getGit().log();
      for (RevCommit commit : logCommand.call()) {
        PersonIdent committerIdentity = commit.getCommitterIdent();
        GitUser gitUser =
            newDto(GitUser.class)
                .withName(committerIdentity.getName())
                .withEmail(committerIdentity.getEmailAddress());
        if (!gitUsers.contains(gitUser)) {
          gitUsers.add(gitUser);
        }
      }
    } catch (GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
    return gitUsers;
  }

  @Override
  public MergeResult merge(String commit) throws GitException {
    org.eclipse.jgit.api.MergeResult jGitMergeResult;
    MergeResult.MergeStatus status;
    try {
      Ref ref = repository.findRef(commit);
      if (ref == null) {
        throw new GitException("Invalid reference to commit for merge " + commit);
      }
      // Shorten local branch names by removing '/refs/heads/' from the beginning
      String name = ref.getName();
      if (name.startsWith(R_HEADS)) {
        name = name.substring(R_HEADS.length());
      }
      jGitMergeResult = getGit().merge().include(name, ref.getObjectId()).call();
    } catch (CheckoutConflictException exception) {
      jGitMergeResult = new org.eclipse.jgit.api.MergeResult(exception.getConflictingPaths());
    } catch (IOException | GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }

    switch (jGitMergeResult.getMergeStatus()) {
      case ALREADY_UP_TO_DATE:
        status = MergeResult.MergeStatus.ALREADY_UP_TO_DATE;
        break;
      case CONFLICTING:
        status = MergeResult.MergeStatus.CONFLICTING;
        break;
      case FAILED:
        status = MergeResult.MergeStatus.FAILED;
        break;
      case FAST_FORWARD:
        status = MergeResult.MergeStatus.FAST_FORWARD;
        break;
      case MERGED:
        status = MergeResult.MergeStatus.MERGED;
        break;
      case NOT_SUPPORTED:
        status = MergeResult.MergeStatus.NOT_SUPPORTED;
        break;
      case CHECKOUT_CONFLICT:
        status = MergeResult.MergeStatus.CONFLICTING;
        break;
      default:
        throw new IllegalStateException("Unknown merge status " + jGitMergeResult.getMergeStatus());
    }

    ObjectId[] jGitMergedCommits = jGitMergeResult.getMergedCommits();
    List<String> mergedCommits = new ArrayList<>();
    if (jGitMergedCommits != null) {
      for (ObjectId jGitMergedCommit : jGitMergedCommits) {
        mergedCommits.add(jGitMergedCommit.getName());
      }
    }

    List<String> conflicts;
    if (org.eclipse.jgit.api.MergeResult.MergeStatus.CHECKOUT_CONFLICT.equals(
        jGitMergeResult.getMergeStatus())) {
      conflicts = jGitMergeResult.getCheckoutConflicts();
    } else {
      Map<String, int[][]> jGitConflicts = jGitMergeResult.getConflicts();
      conflicts = jGitConflicts != null ? new ArrayList<>(jGitConflicts.keySet()) : emptyList();
    }

    Map<String, ResolveMerger.MergeFailureReason> jGitFailing = jGitMergeResult.getFailingPaths();
    ObjectId newHead = jGitMergeResult.getNewHead();

    return newDto(MergeResult.class)
        .withFailed(jGitFailing != null ? new ArrayList<>(jGitFailing.keySet()) : emptyList())
        .withNewHead(newHead != null ? newHead.getName() : null)
        .withMergeStatus(status)
        .withConflicts(conflicts)
        .withMergedCommits(mergedCommits);
  }

  @Override
  public RebaseResponse rebase(String operation, String branch) throws GitException {
    RebaseResult result;
    RebaseStatus status;
    List<String> failed;
    List<String> conflicts;
    try {
      RebaseCommand rebaseCommand = getGit().rebase();
      setRebaseOperation(rebaseCommand, operation);
      if (branch != null && !branch.isEmpty()) {
        rebaseCommand.setUpstream(branch);
      }
      result = rebaseCommand.call();
    } catch (GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
    switch (result.getStatus()) {
      case ABORTED:
        status = RebaseStatus.ABORTED;
        break;
      case CONFLICTS:
        status = RebaseStatus.CONFLICTING;
        break;
      case UP_TO_DATE:
        status = RebaseStatus.ALREADY_UP_TO_DATE;
        break;
      case FAST_FORWARD:
        status = RebaseStatus.FAST_FORWARD;
        break;
      case NOTHING_TO_COMMIT:
        status = RebaseStatus.NOTHING_TO_COMMIT;
        break;
      case OK:
        status = RebaseStatus.OK;
        break;
      case STOPPED:
        status = RebaseStatus.STOPPED;
        break;
      case UNCOMMITTED_CHANGES:
        status = RebaseStatus.UNCOMMITTED_CHANGES;
        break;
      case EDIT:
        status = RebaseStatus.EDITED;
        break;
      case INTERACTIVE_PREPARED:
        status = RebaseStatus.INTERACTIVE_PREPARED;
        break;
      case STASH_APPLY_CONFLICTS:
        status = RebaseStatus.STASH_APPLY_CONFLICTS;
        break;
      default:
        status = RebaseStatus.FAILED;
    }
    conflicts = result.getConflicts() != null ? result.getConflicts() : emptyList();
    failed =
        result.getFailingPaths() != null
            ? new ArrayList<>(result.getFailingPaths().keySet())
            : emptyList();
    return newDto(RebaseResponse.class)
        .withStatus(status)
        .withConflicts(conflicts)
        .withFailed(failed);
  }

  private void setRebaseOperation(RebaseCommand rebaseCommand, String operation) {
    RebaseCommand.Operation op = RebaseCommand.Operation.BEGIN;

    // If other operation other than 'BEGIN' was specified, set it
    if (operation != null) {
      switch (operation) {
        case REBASE_OPERATION_ABORT:
          op = RebaseCommand.Operation.ABORT;
          break;
        case REBASE_OPERATION_CONTINUE:
          op = RebaseCommand.Operation.CONTINUE;
          break;
        case REBASE_OPERATION_SKIP:
          op = RebaseCommand.Operation.SKIP;
          break;
        default:
          op = RebaseCommand.Operation.BEGIN;
          break;
      }
    }
    rebaseCommand.setOperation(op);
  }

  @Override
  public void mv(String source, String target) throws GitException {
    try {
      getGit().add().addFilepattern(target).call();
      getGit().rm().addFilepattern(source).call();
    } catch (GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  @Override
  public PullResponse pull(PullParams params) throws GitException, UnauthorizedException {
    String remoteName = params.getRemote();
    String remoteUri;
    try {
      if (repository.getRepositoryState().equals(RepositoryState.MERGING)) {
        throw new GitException(ERROR_PULL_MERGING);
      }
      String fullBranch = repository.getFullBranch();
      if (!fullBranch.startsWith(R_HEADS)) {
        throw new DetachedHeadException(ERROR_PULL_HEAD_DETACHED);
      }

      String branch = fullBranch.substring(R_HEADS.length());

      StoredConfig config = repository.getConfig();
      if (remoteName == null) {
        remoteName =
            config.getString(
                ConfigConstants.CONFIG_BRANCH_SECTION, branch, ConfigConstants.CONFIG_KEY_REMOTE);
        if (remoteName == null) {
          remoteName = DEFAULT_REMOTE_NAME;
        }
      }
      remoteUri =
          config.getString(
              ConfigConstants.CONFIG_REMOTE_SECTION, remoteName, ConfigConstants.CONFIG_KEY_URL);

      String remoteBranch;
      RefSpec fetchRefSpecs = null;
      String refSpec = params.getRefSpec();
      if (refSpec != null) {
        fetchRefSpecs =
            (refSpec.indexOf(':') < 0) //
                ? new RefSpec(R_HEADS + refSpec + ":" + fullBranch) //
                : new RefSpec(refSpec);
        remoteBranch = fetchRefSpecs.getSource();
      } else {
        remoteBranch =
            config.getString(
                ConfigConstants.CONFIG_BRANCH_SECTION, branch, ConfigConstants.CONFIG_KEY_MERGE);
      }

      if (remoteBranch == null) {
        remoteBranch = fullBranch;
      }

      FetchCommand fetchCommand = getGit().fetch();
      fetchCommand.setRemote(remoteName);
      if (fetchRefSpecs != null) {
        fetchCommand.setRefSpecs(fetchRefSpecs);
      }
      int timeout = params.getTimeout();
      if (timeout > 0) {
        fetchCommand.setTimeout(timeout);
      }

      FetchResult fetchResult =
          (FetchResult)
              executeRemoteCommand(
                  remoteUri, fetchCommand, params.getUsername(), params.getPassword());

      Ref remoteBranchRef = fetchResult.getAdvertisedRef(remoteBranch);
      if (remoteBranchRef == null) {
        remoteBranchRef = fetchResult.getAdvertisedRef(R_HEADS + remoteBranch);
      }
      if (remoteBranchRef == null) {
        throw new GitException(format(ERROR_PULL_REF_MISSING, remoteBranch));
      }
      if (params.getRebase()) {
        RebaseResult.Status status =
            getGit().rebase().setUpstream(remoteName + "/" + branch).call().getStatus();
        if (status == UP_TO_DATE) {
          return newDto(PullResponse.class).withCommandOutput("Already up-to-date");
        } else if (status == UNCOMMITTED_CHANGES) {
          throw new GitException(ERROR_PULL_UNCOMMITED_CHANGES);
        } else if (status == STOPPED) {
          rebase(REBASE_OPERATION_ABORT, null);
          throw new GitException(ERROR_PULL_AUTO_REBASE_FAILED);
        }
      } else {
        org.eclipse.jgit.api.MergeResult mergeResult =
            getGit().merge().include(remoteBranchRef).call();
        if (mergeResult
            .getMergeStatus()
            .equals(org.eclipse.jgit.api.MergeResult.MergeStatus.ALREADY_UP_TO_DATE)) {
          return newDto(PullResponse.class).withCommandOutput("Already up-to-date");
        }
        if (mergeResult.getConflicts() != null) {
          StringBuilder message = new StringBuilder(ERROR_PULL_MERGE_CONFLICT_IN_FILES);
          message.append(lineSeparator());
          Map<String, int[][]> allConflicts = mergeResult.getConflicts();
          for (String path : allConflicts.keySet()) {
            message.append(path).append(lineSeparator());
          }
          message.append(ERROR_PULL_AUTO_MERGE_FAILED);
          throw new GitException(message.toString());
        }
      }
    } catch (CheckoutConflictException exception) {
      StringBuilder message = new StringBuilder(ERROR_CHECKOUT_CONFLICT);
      message.append(lineSeparator());
      for (String path : exception.getConflictingPaths()) {
        message.append(path).append(lineSeparator());
      }
      message.append(ERROR_PULL_COMMIT_BEFORE_MERGE);
      throw new GitException(message.toString(), exception);
    } catch (IOException | GitAPIException exception) {
      String errorMessage;
      if (exception.getMessage().equals("Invalid remote: " + remoteName)) {
        errorMessage = ERROR_NO_REMOTE_REPOSITORY;
      } else {
        errorMessage = generateExceptionMessage(exception);
      }
      throw new GitException(errorMessage, exception);
    }
    return newDto(PullResponse.class).withCommandOutput("Successfully pulled from " + remoteUri);
  }

  @Override
  public PushResponse push(PushParams params) throws GitException, UnauthorizedException {
    List<Map<String, String>> updates = new ArrayList<>();
    String currentBranch = getCurrentBranch();
    String remoteName = params.getRemote();
    String remoteUri =
        getRepository()
            .getConfig()
            .getString(
                ConfigConstants.CONFIG_REMOTE_SECTION, remoteName, ConfigConstants.CONFIG_KEY_URL);
    PushCommand pushCommand = getGit().push();
    if (params.getRemote() != null) {
      pushCommand.setRemote(remoteName);
    }
    List<String> refSpec = params.getRefSpec();
    if (!refSpec.isEmpty()) {
      pushCommand.setRefSpecs(refSpec.stream().map(RefSpec::new).collect(toList()));
    }
    pushCommand.setForce(params.isForce());
    int timeout = params.getTimeout();
    if (timeout > 0) {
      pushCommand.setTimeout(timeout);
    }
    try {
      @SuppressWarnings("unchecked")
      Iterable<PushResult> pushResults =
          (Iterable<PushResult>)
              executeRemoteCommand(
                  remoteUri, pushCommand, params.getUsername(), params.getPassword());
      PushResult pushResult = pushResults.iterator().next();
      String commandOutput =
          pushResult.getMessages().isEmpty()
              ? "Successfully pushed to " + remoteUri
              : pushResult.getMessages();
      Collection<RemoteRefUpdate> refUpdates = pushResult.getRemoteUpdates();

      for (RemoteRefUpdate remoteRefUpdate : refUpdates) {
        final String remoteRefName = remoteRefUpdate.getRemoteName();
        // check status only for branch given in the URL or tags - (handle special "refs/for" case)
        String shortenRefFor =
            remoteRefName.startsWith("refs/for/")
                ? remoteRefName.substring("refs/for/".length())
                : remoteRefName;
        if (!currentBranch.equals(Repository.shortenRefName(remoteRefName))
            && !currentBranch.equals(shortenRefFor)
            && !remoteRefName.startsWith(R_TAGS)) {
          continue;
        }
        Map<String, String> update = new HashMap<>();
        RemoteRefUpdate.Status status = remoteRefUpdate.getStatus();
        if (status != RemoteRefUpdate.Status.OK) {
          List<String> refSpecs = params.getRefSpec();
          if (remoteRefUpdate.getStatus() == RemoteRefUpdate.Status.UP_TO_DATE) {
            commandOutput = INFO_PUSH_IGNORED_UP_TO_DATE;
          } else {
            String remoteBranch =
                !refSpecs.isEmpty() ? refSpecs.get(0).split(REFSPEC_COLON)[1] : "master";
            String errorMessage =
                format(
                    ERROR_PUSH_CONFLICTS_PRESENT,
                    currentBranch + BRANCH_REFSPEC_SEPERATOR + remoteBranch,
                    remoteUri);
            if (remoteRefUpdate.getMessage() != null) {
              errorMessage += "\nError errorMessage: " + remoteRefUpdate.getMessage() + ".";
            }
            throw new GitException(errorMessage);
          }
        }
        if (status != RemoteRefUpdate.Status.UP_TO_DATE || !remoteRefName.startsWith(R_TAGS)) {
          update.put(KEY_COMMIT_MESSAGE, remoteRefUpdate.getMessage());
          update.put(KEY_RESULT, status.name());
          TrackingRefUpdate refUpdate = remoteRefUpdate.getTrackingRefUpdate();
          if (refUpdate != null) {
            update.put(KEY_REMOTENAME, Repository.shortenRefName(refUpdate.getLocalName()));
            update.put(KEY_LOCALNAME, Repository.shortenRefName(refUpdate.getRemoteName()));
          } else {
            update.put(KEY_REMOTENAME, Repository.shortenRefName(remoteRefUpdate.getSrcRef()));
            update.put(KEY_LOCALNAME, Repository.shortenRefName(remoteRefUpdate.getRemoteName()));
          }
          updates.add(update);
        }
      }
      return newDto(PushResponse.class).withCommandOutput(commandOutput).withUpdates(updates);
    } catch (GitAPIException exception) {
      if ("origin: not found.".equals(exception.getMessage())) {
        throw new GitException(ERROR_NO_REMOTE_REPOSITORY, exception);
      } else {
        String message = generateExceptionMessage(exception);
        throw new GitException(message, exception);
      }
    }
  }

  @Override
  public void remoteAdd(RemoteAddParams params) throws GitException {
    String remoteName = params.getName();
    if (isNullOrEmpty(remoteName)) {
      throw new GitException(ERROR_ADD_REMOTE_NAME_MISSING);
    }

    StoredConfig config = repository.getConfig();
    Set<String> remoteNames = config.getSubsections("remote");
    if (remoteNames.contains(remoteName)) {
      throw new GitException(format(ERROR_ADD_REMOTE_NAME_ALREADY_EXISTS, remoteName));
    }

    String url = params.getUrl();
    if (isNullOrEmpty(url)) {
      throw new GitException(ERROR_ADD_REMOTE_URL_MISSING);
    }

    RemoteConfig remoteConfig;
    try {
      remoteConfig = new RemoteConfig(config, remoteName);
    } catch (URISyntaxException exception) {
      // Not happen since it is newly created remote.
      throw new GitException(exception.getMessage(), exception);
    }

    try {
      remoteConfig.addURI(new URIish(url));
    } catch (URISyntaxException exception) {
      throw new GitException("Remote url " + url + " is invalid. ");
    }

    List<String> branches = params.getBranches();
    if (branches.isEmpty()) {
      remoteConfig.addFetchRefSpec(
          new RefSpec(R_HEADS + "*" + ":" + R_REMOTES + remoteName + "/*").setForceUpdate(true));
    } else {
      for (String branch : branches) {
        remoteConfig.addFetchRefSpec(
            new RefSpec(R_HEADS + branch + ":" + R_REMOTES + remoteName + "/" + branch)
                .setForceUpdate(true));
      }
    }

    remoteConfig.update(config);

    try {
      config.save();
    } catch (IOException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  @Override
  public void remoteDelete(String name) throws GitException {
    StoredConfig config = repository.getConfig();
    Set<String> remoteNames = config.getSubsections(ConfigConstants.CONFIG_KEY_REMOTE);
    if (!remoteNames.contains(name)) {
      throw new GitException("error: Could not remove config section 'remote." + name + "'");
    }

    config.unsetSection(ConfigConstants.CONFIG_REMOTE_SECTION, name);
    Set<String> branches = config.getSubsections(ConfigConstants.CONFIG_BRANCH_SECTION);

    for (String branch : branches) {
      String r =
          config.getString(
              ConfigConstants.CONFIG_BRANCH_SECTION, branch, ConfigConstants.CONFIG_KEY_REMOTE);
      if (name.equals(r)) {
        config.unset(
            ConfigConstants.CONFIG_BRANCH_SECTION, branch, ConfigConstants.CONFIG_KEY_REMOTE);
        config.unset(
            ConfigConstants.CONFIG_BRANCH_SECTION, branch, ConfigConstants.CONFIG_KEY_MERGE);
        List<Branch> remoteBranches = branchList(LIST_REMOTE);
        for (Branch remoteBranch : remoteBranches) {
          if (remoteBranch.getDisplayName().startsWith(name)) {
            branchDelete(remoteBranch.getName(), true);
          }
        }
      }
    }

    try {
      config.save();
    } catch (IOException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  @Override
  public List<Remote> remoteList(String remoteName, boolean verbose) throws GitException {
    StoredConfig config = repository.getConfig();
    Set<String> remoteNames =
        new HashSet<>(config.getSubsections(ConfigConstants.CONFIG_KEY_REMOTE));

    if (remoteName != null && remoteNames.contains(remoteName)) {
      remoteNames.clear();
      remoteNames.add(remoteName);
    }

    List<Remote> result = new ArrayList<>(remoteNames.size());
    for (String remote : remoteNames) {
      try {
        List<URIish> uris = new RemoteConfig(config, remote).getURIs();
        result.add(
            newDto(Remote.class)
                .withName(remote)
                .withUrl(uris.isEmpty() ? null : uris.get(0).toString()));
      } catch (URISyntaxException exception) {
        throw new GitException(exception.getMessage(), exception);
      }
    }
    return result;
  }

  @Override
  public void remoteUpdate(RemoteUpdateParams params) throws GitException {
    String remoteName = params.getName();
    if (isNullOrEmpty(remoteName)) {
      throw new GitException(ERROR_UPDATE_REMOTE_NAME_MISSING);
    }

    StoredConfig config = repository.getConfig();
    Set<String> remoteNames = config.getSubsections(ConfigConstants.CONFIG_KEY_REMOTE);
    if (!remoteNames.contains(remoteName)) {
      throw new GitException("Remote " + remoteName + " not found. ");
    }

    RemoteConfig remoteConfig;
    try {
      remoteConfig = new RemoteConfig(config, remoteName);
    } catch (URISyntaxException e) {
      throw new GitException(e.getMessage(), e);
    }

    List<String> branches = params.getBranches();
    if (!branches.isEmpty()) {
      if (!params.isAddBranches()) {
        remoteConfig.setFetchRefSpecs(emptyList());
        remoteConfig.setPushRefSpecs(emptyList());
      } else {
        // Replace wildcard refSpec if any.
        remoteConfig.removeFetchRefSpec(
            new RefSpec(R_HEADS + "*" + ":" + R_REMOTES + remoteName + "/*").setForceUpdate(true));
        remoteConfig.removeFetchRefSpec(
            new RefSpec(R_HEADS + "*" + ":" + R_REMOTES + remoteName + "/*"));
      }

      // Add new refSpec.
      for (String branch : branches) {
        remoteConfig.addFetchRefSpec(
            new RefSpec(R_HEADS + branch + ":" + R_REMOTES + remoteName + "/" + branch)
                .setForceUpdate(true));
      }
    }

    // Remove URLs first.
    for (String url : params.getRemoveUrl()) {
      try {
        remoteConfig.removeURI(new URIish(url));
      } catch (URISyntaxException e) {
        LOG.debug(ERROR_UPDATE_REMOTE_REMOVE_INVALID_URL);
      }
    }

    // Add new URLs.
    for (String url : params.getAddUrl()) {
      try {
        remoteConfig.addURI(new URIish(url));
      } catch (URISyntaxException e) {
        throw new GitException("Remote url " + url + " is invalid. ");
      }
    }

    // Remove URLs for pushing.
    for (String url : params.getRemovePushUrl()) {
      try {
        remoteConfig.removePushURI(new URIish(url));
      } catch (URISyntaxException e) {
        LOG.debug(ERROR_UPDATE_REMOTE_REMOVE_INVALID_URL);
      }
    }

    // Add URLs for pushing.
    for (String url : params.getAddPushUrl()) {
      try {
        remoteConfig.addPushURI(new URIish(url));
      } catch (URISyntaxException e) {
        throw new GitException("Remote push url " + url + " is invalid. ");
      }
    }

    remoteConfig.update(config);

    try {
      config.save();
    } catch (IOException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  @Override
  public void reset(ResetParams params) throws GitException {
    try {
      ResetCommand resetCommand = getGit().reset();
      resetCommand.setRef(params.getCommit());
      List<String> patterns = params.getFilePattern();
      patterns.forEach(resetCommand::addPath);

      if (params.getType() != null && patterns.isEmpty()) {
        switch (params.getType()) {
          case HARD:
            resetCommand.setMode(ResetType.HARD);
            break;
          case KEEP:
            resetCommand.setMode(ResetType.KEEP);
            break;
          case MERGE:
            resetCommand.setMode(ResetType.MERGE);
            break;
          case MIXED:
            resetCommand.setMode(ResetType.MIXED);
            break;
          case SOFT:
            resetCommand.setMode(ResetType.SOFT);
            break;
        }
      }

      resetCommand.call();
    } catch (GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  @Override
  public RevertResult revert(String commit) throws GitException {
    RevCommit revCommit;
    RevertCommand revertCommand = getGit().revert();
    try {
      revertCommand.include(this.repository.resolve(commit));
      revCommit = revertCommand.call();
    } catch (IOException | GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }

    return newDto(RevertResult.class)
        .withRevertedCommits(getRevertedCommits(revertCommand))
        .withConflicts(getRevertConflicts(revertCommand))
        .withNewHead(revCommit != null ? revCommit.getId().getName() : null);
  }

  private List<String> getRevertedCommits(RevertCommand revertCommand) {
    List<Ref> jGitRevertedCommits = revertCommand.getRevertedRefs();
    List<String> revertedCommits = new ArrayList<String>();
    if (jGitRevertedCommits != null) {
      jGitRevertedCommits.forEach(ref -> revertedCommits.add(ref.getObjectId().name()));
    }
    return revertedCommits;
  }

  private Map<String, RevertResult.RevertStatus> getRevertConflicts(RevertCommand revertCommand) {
    Map<String, RevertResult.RevertStatus> conflicts = new HashMap<>();
    if (revertCommand.getFailingResult() != null) {
      Map<String, MergeFailureReason> failingPaths =
          revertCommand.getFailingResult().getFailingPaths();
      if (failingPaths != null && !failingPaths.isEmpty()) {
        failingPaths
            .entrySet()
            .forEach(
                failure ->
                    conflicts.put(
                        failure.getKey(),
                        getRevertStatusFromMergeFailureReason(failure.getValue())));
      }
    }
    List<String> unmergedPaths = revertCommand.getUnmergedPaths();
    if (unmergedPaths != null && !unmergedPaths.isEmpty()) {
      unmergedPaths
          .stream()
          .filter(unmergedPath -> !conflicts.containsKey(unmergedPath))
          .forEach(unmergedPath -> conflicts.put(unmergedPath, RevertResult.RevertStatus.FAILED));
    }
    return conflicts;
  }

  private RevertResult.RevertStatus getRevertStatusFromMergeFailureReason(
      @NotNull MergeFailureReason mergeFailureReason) {
    switch (mergeFailureReason) {
      case COULD_NOT_DELETE:
        return RevertResult.RevertStatus.COULD_NOT_DELETE;
      case DIRTY_INDEX:
        return RevertResult.RevertStatus.DIRTY_INDEX;
      case DIRTY_WORKTREE:
        return RevertResult.RevertStatus.DIRTY_WORKTREE;
      default:
        return RevertResult.RevertStatus.FAILED;
    }
  }

  @Override
  public void rm(RmParams params) throws GitException {
    List<String> files = params.getItems();
    RmCommand rmCommand = getGit().rm();

    rmCommand.setCached(params.isCached());

    if (files != null) {
      files.forEach(rmCommand::addFilepattern);
    }
    try {
      rmCommand.call();
    } catch (GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  @Override
  public Status status(List<String> filter) throws GitException {
    if (!RepositoryCache.FileKey.isGitRepository(getRepository().getDirectory(), FS.DETECTED)) {
      throw new GitException("Not a git repository");
    }
    String branchName = getCurrentBranch();
    StatusCommand statusCommand = getGit().status();
    if (filter != null) {
      filter.forEach(statusCommand::addPath);
    }
    return new JGitStatusImpl(branchName, statusCommand);
  }

  @Override
  public Tag tagCreate(TagCreateParams params) throws GitException {
    String commit = params.getCommit();
    if (commit == null) {
      commit = HEAD;
    }

    try {
      RevWalk revWalk = new RevWalk(repository);
      RevObject revObject;
      try {
        revObject = revWalk.parseAny(repository.resolve(commit));
      } finally {
        revWalk.close();
      }

      TagCommand tagCommand =
          getGit()
              .tag()
              .setName(params.getName())
              .setObjectId(revObject)
              .setMessage(params.getMessage())
              .setForceUpdate(params.isForce());

      GitUser tagger = getUser();
      if (tagger != null) {
        tagCommand.setTagger(new PersonIdent(tagger.getName(), tagger.getEmail()));
      }

      Ref revTagRef = tagCommand.call();
      RevTag revTag = revWalk.parseTag(revTagRef.getLeaf().getObjectId());
      return newDto(Tag.class).withName(revTag.getTagName());
    } catch (IOException | GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  @Override
  public void tagDelete(String name) throws GitException {
    try {
      Ref tagRef = repository.findRef(name);
      if (tagRef == null) {
        throw new GitException("Tag " + name + " not found. ");
      }

      RefUpdate updateRef = repository.updateRef(tagRef.getName());
      updateRef.setRefLogMessage("tag deleted", false);
      updateRef.setForceUpdate(true);
      Result deleteResult = updateRef.delete();
      if (deleteResult != Result.FORCED && deleteResult != Result.FAST_FORWARD) {
        throw new GitException(format(ERROR_TAG_DELETE, name, deleteResult));
      }
    } catch (IOException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  @Override
  public List<Tag> tagList(String patternStr) throws GitException {
    Pattern pattern = null;
    if (patternStr != null) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < patternStr.length(); i++) {
        char c = patternStr.charAt(i);
        if (c == '*' || c == '?') {
          sb.append('.');
        } else if (c == '.' || c == '(' || c == ')' || c == '[' || c == ']' || c == '^' || c == '$'
            || c == '|') {
          sb.append('\\');
        }
        sb.append(c);
      }
      pattern = Pattern.compile(sb.toString());
    }

    Set<String> tagNames = repository.getTags().keySet();
    List<Tag> tags = new ArrayList<>(tagNames.size());

    for (String tagName : tagNames) {
      if (pattern == null || pattern.matcher(tagName).matches()) {
        tags.add(newDto(Tag.class).withName(tagName));
      }
    }
    return tags;
  }

  @Override
  public void close() {
    repository.close();
  }

  @Override
  public File getWorkingDir() {
    return repository.getWorkTree();
  }

  @Override
  public List<RemoteReference> lsRemote(String remoteUrl)
      throws UnauthorizedException, GitException {
    LsRemoteCommand lsRemoteCommand = getGit().lsRemote().setRemote(remoteUrl);
    Collection<Ref> refs;
    try {
      refs = lsRemoteCommand.call();
    } catch (GitAPIException exception) {
      if (exception.getMessage().contains(ERROR_AUTHENTICATION_REQUIRED)) {
        throw new UnauthorizedException(format(ERROR_AUTHENTICATION_FAILED, remoteUrl));
      } else {
        throw new GitException(exception.getMessage(), exception);
      }
    }

    return refs.stream()
        .map(
            ref ->
                newDto(RemoteReference.class)
                    .withCommitId(ref.getObjectId().name())
                    .withReferenceName(ref.getName()))
        .collect(toList());
  }

  @Override
  public Config getConfig() throws GitException {
    if (config != null) {
      return config;
    }
    return config = new JGitConfigImpl(repository);
  }

  @Override
  public void setOutputLineConsumerFactory(LineConsumerFactory lineConsumerFactory) {
    this.lineConsumerFactory = lineConsumerFactory;
  }

  private Git getGit() {
    if (git != null) {
      return git;
    }
    return git = new Git(repository);
  }

  @Override
  public List<String> listFiles(LsFilesParams params) throws GitException {
    return Arrays.asList(
        getWorkingDir()
            .list(
                new FilenameFilter() {
                  @Override
                  public boolean accept(File dir, String name) {
                    return !name.startsWith(".");
                  }
                }));
  }

  @Override
  public void cloneWithSparseCheckout(String directory, String remoteUrl)
      throws GitException, UnauthorizedException {
    // TODO rework this code when jgit will support sparse-checkout. Tracked issue:
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=383772
    if (directory == null) {
      throw new GitException("Subdirectory for sparse-checkout is not specified");
    }
    clone(CloneParams.create(remoteUrl));
    final String sourcePath = getWorkingDir().getPath();
    final String keepDirectoryPath = sourcePath + "/" + directory;
    IOFileFilter folderFilter =
        new DirectoryFileFilter() {
          public boolean accept(File dir) {
            String directoryPath = dir.getPath();
            return !(directoryPath.startsWith(keepDirectoryPath)
                || directoryPath.startsWith(sourcePath + "/.git"));
          }
        };
    Collection<File> files =
        org.apache.commons.io.FileUtils.listFilesAndDirs(
            getWorkingDir(), TrueFileFilter.INSTANCE, folderFilter);
    try {
      DirCache index = getRepository().lockDirCache();
      int sourcePathLength = sourcePath.length() + 1;
      files
          .stream()
          .filter(File::isFile)
          .forEach(
              file ->
                  index.getEntry(file.getPath().substring(sourcePathLength)).setAssumeValid(true));
      index.write();
      index.commit();
      for (File file : files) {
        if (keepDirectoryPath.startsWith(file.getPath())) {
          continue;
        }
        if (file.exists()) {
          FileUtils.delete(file, FileUtils.RECURSIVE);
        }
      }
    } catch (IOException exception) {
      String message = generateExceptionMessage(exception);
      throw new GitException(message, exception);
    }
  }

  /**
   * Execute remote jgit command.
   *
   * @param remoteUrl remote url
   * @param command command to execute
   * @return executed command
   * @throws GitException
   * @throws GitAPIException
   * @throws UnauthorizedException
   */
  @VisibleForTesting
  Object executeRemoteCommand(
      String remoteUrl,
      TransportCommand command,
      @Nullable String username,
      @Nullable String password)
      throws GitException, GitAPIException, UnauthorizedException {
    File keyDirectory = null;
    UserCredential credentials = null;

    try {
      if (GitUrlUtils.isSSH(remoteUrl)) {
        keyDirectory = Files.createTempDir();
        final File sshKey = writePrivateKeyFile(remoteUrl, keyDirectory);

        SshSessionFactory sshSessionFactory =
            new JschConfigSessionFactory() {
              @Override
              protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
              }

              @Override
              protected JSch getJSch(final OpenSshConfig.Host hc, FS fs) throws JSchException {
                JSch jsch = super.getJSch(hc, fs);
                jsch.removeAllIdentity();
                jsch.addIdentity(sshKey.getAbsolutePath());
                return jsch;
              }
            };
        command.setTransportConfigCallback(
            transport -> {
              // If recursive clone is performed and git-module added by http(s) url is present in
              // the cloned project,
              // transport will be instance of TransportHttp in the step of cloning this module
              if (transport instanceof SshTransport) {
                ((SshTransport) transport).setSshSessionFactory(sshSessionFactory);
              }
            });
      } else {
        if (remoteUrl != null && GIT_URL_WITH_CREDENTIALS_PATTERN.matcher(remoteUrl).matches()) {
          username = remoteUrl.substring(remoteUrl.indexOf("://") + 3, remoteUrl.lastIndexOf(":"));
          password = remoteUrl.substring(remoteUrl.lastIndexOf(":") + 1, remoteUrl.indexOf("@"));
          command.setCredentialsProvider(
              new UsernamePasswordCredentialsProvider(username, password));
        } else {
          if (username != null && password != null) {
            command.setCredentialsProvider(
                new UsernamePasswordCredentialsProvider(username, password));
          } else {
            credentials = credentialsLoader.getUserCredential(remoteUrl);
            if (credentials != null) {
              command.setCredentialsProvider(
                  new UsernamePasswordCredentialsProvider(
                      credentials.getUserName(), credentials.getPassword()));
            }
          }
        }
      }
      ProxyAuthenticator.initAuthenticator(remoteUrl);
      return command.call();
    } catch (GitException | TransportException exception) {
      if ("Unable get private ssh key".equals(exception.getMessage())) {
        throw new UnauthorizedException(
            exception.getMessage(), ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY);
      } else if (exception.getMessage().contains(ERROR_AUTHENTICATION_REQUIRED)) {
        final ProviderInfo info = credentialsLoader.getProviderInfo(remoteUrl);
        if (info != null) {
          throw new UnauthorizedException(
              exception.getMessage(),
              ErrorCodes.UNAUTHORIZED_GIT_OPERATION,
              ImmutableMap.of(
                  PROVIDER_NAME,
                  info.getProviderName(),
                  AUTHENTICATE_URL,
                  info.getAuthenticateUrl(),
                  "authenticated",
                  Boolean.toString(credentials != null)));
        }
        throw new UnauthorizedException(
            exception.getMessage(), ErrorCodes.UNAUTHORIZED_GIT_OPERATION);
      } else {
        throw exception;
      }
    } finally {
      if (keyDirectory != null && keyDirectory.exists()) {
        try {
          FileUtils.delete(keyDirectory, FileUtils.RECURSIVE);
        } catch (IOException exception) {
          throw new GitException("Can't remove SSH key directory", exception);
        }
      }
      ProxyAuthenticator.resetAuthenticator();
    }
  }

  /**
   * Writes private SSH key into file.
   *
   * @return file that contains SSH key
   * @throws GitException if other error occurs
   */
  private File writePrivateKeyFile(String url, File keyDirectory) throws GitException {
    final File keyFile = new File(keyDirectory, "identity");
    try (FileOutputStream fos = new FileOutputStream(keyFile)) {
      byte[] sshKey = sshKeyProvider.getPrivateKey(url);
      fos.write(sshKey);
    } catch (IOException | ServerException exception) {
      String errorMessage = "Can't store ssh key. ".concat(exception.getMessage());
      LOG.error(errorMessage, exception);
      throw new GitException(errorMessage, ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY);
    }
    Set<PosixFilePermission> permissions = EnumSet.of(OWNER_READ, OWNER_WRITE);
    try {
      java.nio.file.Files.setPosixFilePermissions(keyFile.toPath(), permissions);
    } catch (IOException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
    return keyFile;
  }

  private GitUser getUser() throws GitException {
    return userResolver.getUser();
  }

  private void deleteRepositoryFolder() {
    try {
      if (repository.getDirectory().exists()) {
        FileUtils.delete(repository.getDirectory(), FileUtils.RECURSIVE | FileUtils.IGNORE_ERRORS);
      }
    } catch (Exception exception) {
      // Ignore the error since we want to throw the original error
      LOG.error(
          "Could not remove .git folder in path " + repository.getDirectory().getPath(), exception);
    }
  }

  private Repository getRepository() {
    return repository;
  }

  /**
   * Get the current branch on the current directory
   *
   * @return the name of the branch
   * @throws GitException if any exception occurs
   */
  public String getCurrentBranch() throws GitException {
    try {
      return Repository.shortenRefName(repository.exactRef(HEAD).getLeaf().getName());
    } catch (IOException exception) {
      throw new GitException(exception.getMessage(), exception);
    }
  }

  /**
   * Method for cleaning name of remote branch to be checked out. I.e. it takes something like
   * "origin/testBranch" and returns "testBranch". This is needed for view-compatibility with
   * console Git client.
   *
   * @param branchName is a name of branch to be cleaned
   * @return branchName without remote repository name
   * @throws GitException
   */
  private String cleanRemoteName(String branchName) throws GitException {
    String returnName = branchName;
    List<Remote> remotes = this.remoteList(null, false);
    for (Remote remote : remotes) {
      if (branchName.startsWith(remote.getName())) {
        returnName = branchName.replaceFirst(remote.getName() + "/", "");
      }
    }
    return returnName;
  }

  /**
   * Method for generate exception message. The default logic return message from the error. It also
   * check if the type of the message is for SSL or in case that the error start with "file name to
   * long" then it raise the relevant message
   *
   * @param error throwable error
   * @return exception message
   */
  private String generateExceptionMessage(Throwable error) {
    String message = error.getMessage();
    while (error.getCause() != null) {
      // if e caused by an SSLHandshakeException - replace thrown message with a hardcoded message
      if (error.getCause() instanceof SSLHandshakeException) {
        message =
            "The system is not configured to trust the security certificate provided by the Git server";
        break;
      } else if (error.getCause() instanceof IOException) {
        // Security fix - error message should not include complete local file path on the target
        // system
        // Error message for example - File name too long (path /xx/xx/xx/xx/xx/xx/xx/xx /, working
        // dir /xx/xx/xx)
        if (message != null && message.startsWith(FILE_NAME_TOO_LONG_ERROR_PREFIX)) {
          try {
            String repoPath = repository.getWorkTree().getCanonicalPath();
            int startIndex = message.indexOf(repoPath);
            int endIndex = message.indexOf(",");
            if (startIndex > -1 && endIndex > -1) {
              message =
                  FILE_NAME_TOO_LONG_ERROR_PREFIX
                      + " "
                      + message.substring(startIndex + repoPath.length(), endIndex);
            }
            break;
          } catch (IOException e) {
            // Hide exception as it is only needed for this message generation
          }
        }
      }
      error = error.getCause();
    }
    return message;
  }
}
