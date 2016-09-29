/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *   SAP           - implementation
 *******************************************************************************/
package org.eclipse.che.git.impl.jgit;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.git.Config;
import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.api.git.DiffPage;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.exception.GitConflictException;
import org.eclipse.che.api.git.exception.GitRefAlreadyExistsException;
import org.eclipse.che.api.git.exception.GitRefNotFoundException;
import org.eclipse.che.api.git.exception.GitInvalidRefNameException;
import org.eclipse.che.api.git.GitUrlUtils;
import org.eclipse.che.api.git.GitUserResolver;
import org.eclipse.che.api.git.LogPage;
import org.eclipse.che.api.git.UserCredential;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchCreateRequest;
import org.eclipse.che.api.git.shared.BranchDeleteRequest;
import org.eclipse.che.api.git.shared.BranchListRequest;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.CloneRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.DiffRequest;
import org.eclipse.che.api.git.shared.FetchRequest;
import org.eclipse.che.api.git.shared.GitUser;
import org.eclipse.che.api.git.shared.InitRequest;
import org.eclipse.che.api.git.shared.LogRequest;
import org.eclipse.che.api.git.shared.LsFilesRequest;
import org.eclipse.che.api.git.shared.LsRemoteRequest;
import org.eclipse.che.api.git.shared.MergeRequest;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.MoveRequest;
import org.eclipse.che.api.git.shared.ProviderInfo;
import org.eclipse.che.api.git.shared.PullRequest;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.PushRequest;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.RebaseRequest;
import org.eclipse.che.api.git.shared.RebaseResponse;
import org.eclipse.che.api.git.shared.RebaseResponse.RebaseStatus;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.RemoteAddRequest;
import org.eclipse.che.api.git.shared.RemoteListRequest;
import org.eclipse.che.api.git.shared.RemoteReference;
import org.eclipse.che.api.git.shared.RemoteUpdateRequest;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.RmRequest;
import org.eclipse.che.api.git.shared.ShowFileContentRequest;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.api.git.shared.Tag;
import org.eclipse.che.api.git.shared.TagCreateRequest;
import org.eclipse.che.api.git.shared.TagDeleteRequest;
import org.eclipse.che.api.git.shared.TagListRequest;
import org.eclipse.che.api.git.shared.GitRequest;
import org.eclipse.che.plugin.ssh.key.script.SshKeyProvider;
import org.eclipse.che.commons.proxy.ProxyAuthenticator;
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
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RefUpdate.Result;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.merge.ResolveMerger;
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
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.net.ssl.SSLHandshakeException;
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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.api.git.shared.ProviderInfo.AUTHENTICATE_URL;
import static org.eclipse.che.api.git.shared.ProviderInfo.PROVIDER_NAME;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Andrey Parfonov
 * @author Igor Vinokur
 */
class JGitConnection implements GitConnection {
    private static final String REBASE_OPERATION_SKIP     = "SKIP";
    private static final String REBASE_OPERATION_CONTINUE = "CONTINUE";
    private static final String REBASE_OPERATION_ABORT    = "ABORT";
    private static final String ADD_ALL_OPTION            = "all";

    // Push Response Constants
    private static final String BRANCH_REFSPEC_SEPERATOR = " -> ";
    private static final String REFSPEC_COLON            = ":";
    private static final String KEY_COMMIT_MESSAGE       = "Message";
    private static final String KEY_RESULT               = "Result";
    private static final String KEY_REMOTENAME           = "RemoteName";
    private static final String KEY_LOCALNAME            = "LocalName";

    private static final String ERROR_UPDATE_REMOTE_NAME_MISSING       = "Update operation failed, remote name is required.";
    private static final String ERROR_UPDATE_REMOTE_REMOVE_INVALID_URL = "remoteUpdate: Ignore this error. Cannot remove invalid URL.";

    private static final String ERROR_ADD_REMOTE_NAME_ALREADY_EXISTS = "Add Remote operation failed, remote name %s already exists.";
    private static final String ERROR_ADD_REMOTE_NAME_MISSING        = "Add operation failed, remote name is required.";
    private static final String ERROR_ADD_REMOTE_URL_MISSING         = "Add Remote operation failed, Remote url required.";

    private static final String ERROR_PULL_MERGING                 = "Could not pull because the repository state is 'MERGING'.";
    private static final String ERROR_PULL_HEAD_DETACHED           = "Could not pull because HEAD is detached.";
    private static final String ERROR_PULL_REF_MISSING             = "Could not pull because remote ref is missing for branch %s.";
    private static final String ERROR_PULL_AUTO_MERGE_FAILED       = "Automatic merge failed; fix conflicts and then commit the result.";
    private static final String ERROR_PULL_MERGE_CONFLICT_IN_FILES = "Could not pull because a merge conflict is detected in the files:";
    private static final String ERROR_PULL_COMMIT_BEFORE_MERGE     = "Could not pull. Commit your changes before merging.";

    private static final String ERROR_CHECKOUT_BRANCH_NAME_EXISTS = "A branch named '%s' already exists.";
    private static final String ERROR_CHECKOUT_CONFLICT           = "Checkout operation failed, the following files would be " +
                                                                    "overwritten by merge:";

    private static final String ERROR_PUSH_CONFLICTS_PRESENT = "failed to push '%s' to '%s'. Try to merge " +
                                                               "remote changes using pull, and then push again.";
    private static final String INFO_PUSH_IGNORED_UP_TO_DATE = "Everything up-to-date";

    private static final String ERROR_AUTHENTICATION_REQUIRED = "Authentication is required but no CredentialsProvider has been registered";
    private static final String ERROR_AUTHENTICATION_FAILED   = "fatal: Authentication failed for '%s/'" + lineSeparator();

    private static final String ERROR_BRANCH_LIST_UNSUPPORTED_LIST_MODE = "Unsupported list mode '%s'. Must be either 'a' or 'r'.";
    private static final String ERROR_TAG_DELETE                        = "Could not delete the tag %1$s. An error occurred: %2$s.";
    private static final String ERROR_LOG_NO_HEAD_EXISTS                = "No HEAD exists and no explicit starting revision was specified";
    private static final String ERROR_INIT_FOLDER_MISSING               = "The working folder %s does not exist.";
    private static final String ERROR_NO_REMOTE_REPOSITORY              = "No remote repository specified.  Please, specify either a " +
                                                                          "URL or a remote name from which new revisions should be " +
                                                                          "fetched in request.";

    private static final String MESSAGE_COMMIT_NOT_POSSIBLE       = "Commit is not possible because repository state is '%s'";
    private static final String MESSAGE_COMMIT_AMEND_NOT_POSSIBLE = "Amend is not possible because repository state is '%s'";

    private static final String FILE_NAME_TOO_LONG_ERROR_PREFIX = "File name too long";

    private static final Pattern GIT_URL_WITH_CREDENTIALS_PATTERN = Pattern.compile("https?://[^:]+:[^@]+@.*");

    private static final Logger LOG = LoggerFactory.getLogger(JGitConnection.class);

    private Git                 git;
    private JGitConfigImpl      config;
    private LineConsumerFactory lineConsumerFactory;

    private final CredentialsLoader credentialsLoader;
    private final SshKeyProvider    sshKeyProvider;
    private final GitUserResolver   userResolver;
    private final Repository        repository;

    @Inject
    JGitConnection(Repository repository, CredentialsLoader credentialsLoader, SshKeyProvider sshKeyProvider,
                   GitUserResolver userResolver) {
        this.repository = repository;
        this.credentialsLoader = credentialsLoader;
        this.sshKeyProvider = sshKeyProvider;
        this.userResolver = userResolver;
    }

    @Override
    public void add(AddRequest request) throws GitException {
        add(request, request.isUpdate());

        // "all" option, when update is false, should run git add with both update true and update false
        if ((!request.isUpdate()) && request.getAttributes().containsKey(ADD_ALL_OPTION)) {
            add(request, true);
        }
    }

    /*
     * Perform the "add" according to the add request. isUpdate is always used
     * as the value for the "update" parameter instead of the value in the
     * AddRequest.
     */
    private void add(AddRequest request, boolean isUpdate) throws GitException {
        AddCommand addCommand = getGit().add().setUpdate(isUpdate);

        List<String> filePatterns = request.getFilepattern();
        if (filePatterns.isEmpty()) {
            filePatterns = AddRequest.DEFAULT_PATTERN;
        }
        filePatterns.forEach(addCommand::addFilepattern);

        try {
            addCommand.call();
        } catch (GitAPIException exception) {
            throw new GitException(exception.getMessage(), exception);
        }
    }

    @Override
    public void checkout(CheckoutRequest request) throws GitException {
        CheckoutCommand checkoutCommand = getGit().checkout();
        String startPoint = request.getStartPoint();
        String name = request.getName();
        String trackBranch = request.getTrackBranch();

        // checkout files?
        List<String> files = request.getFiles();
        boolean shouldCheckoutToFile = name != null && new File(getWorkingDir(), name).exists();
        if (shouldCheckoutToFile || !files.isEmpty()) {
            if (shouldCheckoutToFile) {
                checkoutCommand.addPath(request.getName());
            } else {
                files.forEach(checkoutCommand::addPath);
            }
        } else {
            // checkout branch
            if (startPoint != null && trackBranch != null) {
                throw new GitException("Start point and track branch can not be used together.");
            }
            if (request.isCreateNew() && name == null) {
                throw new GitException("Branch name must be set when createNew equals to true.");
            }
            if (startPoint != null) {
                checkoutCommand.setStartPoint(startPoint);
            }
            if (request.isCreateNew()) {
                checkoutCommand.setCreateBranch(true);
                checkoutCommand.setName(name);
            } else if (name != null) {
                checkoutCommand.setName(name);
                List<String> localBranches =
                        branchList(newDto(BranchListRequest.class).withListMode(BranchListRequest.LIST_LOCAL)).stream()
                                                                                                              .map(Branch::getDisplayName)
                                                                                                              .collect(Collectors.toList());
                if (!localBranches.contains(name)) {
                    Optional<Branch> remoteBranch = branchList(newDto(BranchListRequest.class).withListMode(BranchListRequest.LIST_REMOTE))
                            .stream()
                            .filter(branch -> branch.getName().contains(name))
                            .findFirst();
                    if (remoteBranch.isPresent()) {
                        checkoutCommand.setCreateBranch(true);
                        checkoutCommand.setStartPoint(remoteBranch.get().getName());
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
        } catch(CheckoutConflictException exception){
            throw new GitConflictException(exception.getMessage(), exception.getConflictingPaths());
        } catch(RefAlreadyExistsException exception){
            throw new GitRefAlreadyExistsException(exception.getMessage());
        } catch(RefNotFoundException exception){
            throw new GitRefNotFoundException(exception.getMessage());
        } catch(InvalidRefNameException exception){
            throw new GitInvalidRefNameException(exception.getMessage());
        } catch (GitAPIException exception) {
            if (exception.getMessage().endsWith("already exists")) {
                throw new GitException(format(ERROR_CHECKOUT_BRANCH_NAME_EXISTS, name != null ? name : cleanRemoteName(trackBranch)));
            }
            throw new GitException(exception.getMessage(), exception);
        }
    }

    @Override
    public Branch branchCreate(BranchCreateRequest request) throws GitException {
        CreateBranchCommand createBranchCommand = getGit().branchCreate().setName(request.getName());
        String start = request.getStartPoint();
        if (start != null) {
            createBranchCommand.setStartPoint(start);
        }
        try {
            Ref brRef = createBranchCommand.call();
            String refName = brRef.getName();
            String displayName = Repository.shortenRefName(refName);
            return newDto(Branch.class).withName(refName)
                                       .withDisplayName(displayName)
                                       .withActive(false)
                                       .withRemote(false);
        } catch (GitAPIException exception) {
            throw new GitException(exception.getMessage(), exception);
        }
    }

    @Override
    public void branchDelete(BranchDeleteRequest request) throws GitException {
        try {
            getGit().branchDelete()
                    .setBranchNames(request.getName())
                    .setForce(request.isForce())
                    .call();
        } catch (GitAPIException exception) {
            throw new GitException(exception.getMessage(), exception);
        }
    }

    @Override
    public void branchRename(String oldName, String newName) throws GitException {
        try {
            getGit().branchRename()
                    .setOldName(oldName)
                    .setNewName(newName)
                    .call();
        } catch (GitAPIException exception) {
            throw new GitException(exception.getMessage(), exception);
        }
    }

    @Override
    public List<Branch> branchList(BranchListRequest request) throws GitException {
        String listMode = request.getListMode();
        if (listMode != null && !BranchListRequest.LIST_ALL.equals(listMode) && !BranchListRequest.LIST_REMOTE.equals(listMode)) {
            throw new GitException(format(ERROR_BRANCH_LIST_UNSUPPORTED_LIST_MODE, listMode));
        }

        ListBranchCommand listBranchCommand = getGit().branchList();
        if (BranchListRequest.LIST_ALL.equals(listMode)) {
            listBranchCommand.setListMode(ListMode.ALL);
        } else if (BranchListRequest.LIST_REMOTE.equals(listMode)) {
            listBranchCommand.setListMode(ListMode.REMOTE);
        }
        List<Ref> refs;
        String currentRef;
        try {
            refs = listBranchCommand.call();
            String headBranch = getRepository().getBranch();
            Optional<Ref> currentTag = getGit().tagList().call().stream()
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
            boolean isCommitOrTag = Constants.HEAD.equals(refName);
            String branchName = isCommitOrTag ? currentRef : refName;
            String branchDisplayName;
            if (isCommitOrTag) {
                branchDisplayName = "(detached from " + Repository.shortenRefName(currentRef) + ")";
            } else {
                branchDisplayName = Repository.shortenRefName(refName);
            }
            Branch branch = newDto(Branch.class).withName(branchName)
                                                .withActive(isCommitOrTag || refName.equals(currentRef))
                                                .withDisplayName(branchDisplayName)
                                                .withRemote(refName.startsWith("refs/remotes"));
            branches.add(branch);
        }
        return branches;
    }

    public void clone(CloneRequest request) throws GitException, UnauthorizedException {
        String remoteUri = request.getRemoteUri();
        boolean removeIfFailed = false;
        try {
            if (request.getRemoteName() == null) {
                request.setRemoteName(Constants.DEFAULT_REMOTE_NAME);
            }
            if (request.getWorkingDir() == null) {
                request.setWorkingDir(repository.getWorkTree().getCanonicalPath());
            }

            // If clone fails and the .git folder didn't exist we want to remove it.
            // We have to do this here because the clone command doesn't revert its own changes in case of failure.
            removeIfFailed = !repository.getDirectory().exists();

            CloneCommand cloneCommand = Git.cloneRepository()
                                           .setDirectory(new File(request.getWorkingDir()))
                                           .setRemote(request.getRemoteName())
                                           .setCloneSubmodules(request.isRecursive())
                                           .setURI(remoteUri);
            if (request.getBranchesToFetch().isEmpty()) {
                cloneCommand.setCloneAllBranches(true);
            } else {
                cloneCommand.setBranchesToClone(request.getBranchesToFetch());
            }

            LineConsumer lineConsumer = lineConsumerFactory.newLineConsumer();
            cloneCommand.setProgressMonitor(new BatchingProgressMonitor() {
                @Override
                protected void onUpdate(String taskName, int workCurr) {
                    try {
                        lineConsumer.writeLine(taskName + ": " + workCurr + " completed");
                    } catch (IOException exception) {
                        LOG.error(exception.getMessage(), exception);
                    }
                }

                @Override
                protected void onEndTask(String taskName, int workCurr) {
                }

                @Override
                protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
                    try {
                        lineConsumer.writeLine(taskName + ": " + workCurr + " of " + workTotal + " completed, " + percentDone + "% done");
                    } catch (IOException exception) {
                        LOG.error(exception.getMessage(), exception);
                    }
                }

                @Override
                protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
                }
            });

            executeRemoteCommand(remoteUri, cloneCommand , request);

            StoredConfig repositoryConfig = getRepository().getConfig();
            GitUser gitUser = getUser();
            if (gitUser != null) {
                repositoryConfig.setString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_NAME, gitUser.getName());
                repositoryConfig.setString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_EMAIL, gitUser.getEmail());
            }
            repositoryConfig.save();
        } catch (IOException | GitAPIException exception) {
            // Delete .git directory in case it was created
            if (removeIfFailed) {
                deleteRepositoryFolder();
            }
            //TODO remove this when JGit will support HTTP 301 redirects, https://bugs.eclipse.org/bugs/show_bug.cgi?id=465167
            //try to clone repository by replacing http to https in the url if HTTP 301 redirect happened
            if (exception.getMessage().contains(": 301 Moved Permanently")) {
                remoteUri = "https" + remoteUri.substring(4);
                try {
                    clone(request.withRemoteUri(remoteUri));
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
    public Revision commit(CommitRequest request) throws GitException {
        try {
            String message = request.getMessage();
            GitUser committer = getUser();
            if (message == null) {
                throw new GitException("Message wasn't set");
            }
            if (committer == null) {
                throw new GitException("Committer can't be null");
            }

            //Check that there are staged changes present for commit, or any changes if is 'isAll' enabled, otherwise throw exception
            Status status = status(StatusFormat.SHORT);
            if (!request.isAmend() && !request.isAll()
                && status.getAdded().isEmpty() && status.getChanged().isEmpty() && status.getRemoved().isEmpty()) {
                throw new GitException("No changes added to commit");
            } else if (!request.isAmend() && request.isAll() && status.isClean()) {
                throw new GitException("Nothing to commit, working directory clean");
            }

            String committerName = committer.getName();
            String committerEmail = committer.getEmail();
            if (committerName == null || committerEmail == null) {
                throw new GitException("Git user name and (or) email wasn't set", ErrorCodes.NO_COMMITTER_NAME_OR_EMAIL_DEFINED);
            }
            if (!repository.getRepositoryState().canCommit()) {
                Revision rev = newDto(Revision.class);
                rev.setMessage(format(MESSAGE_COMMIT_NOT_POSSIBLE, repository.getRepositoryState().getDescription()));
                return rev;
            }

            if (request.isAmend() && !repository.getRepositoryState().canAmend()) {
                Revision rev = newDto(Revision.class);
                rev.setMessage(format(MESSAGE_COMMIT_AMEND_NOT_POSSIBLE, repository.getRepositoryState().getDescription()));
                return rev;
            }

            CommitCommand commitCommand = getGit().commit()
                                                  .setCommitter(committerName, committerEmail).setAuthor(committerName, committerEmail)
                                                  .setMessage(message)
                                                  .setAll(request.isAll())
                                                  .setAmend(request.isAmend());

            // Check if repository is configured with Gerrit Support
            String gerritSupportConfigValue = repository.getConfig().getString(
                    ConfigConstants.CONFIG_GERRIT_SECTION, null,
                    ConfigConstants.CONFIG_KEY_CREATECHANGEID);
            boolean isGerritSupportConfigured = gerritSupportConfigValue != null ? Boolean.valueOf(gerritSupportConfigValue) : false;
            commitCommand.setInsertChangeId(isGerritSupportConfigured);
            RevCommit result = commitCommand.call();
            GitUser gitUser = newDto(GitUser.class).withName(committerName).withEmail(committerEmail);

            return newDto(Revision.class).withBranch(getCurrentBranch())
                                         .withId(result.getId().getName()).withMessage(result.getFullMessage())
                                         .withCommitTime(MILLISECONDS.convert(result.getCommitTime(), SECONDS)).withCommitter(gitUser);
        } catch (GitAPIException exception) {
            throw new GitException(exception.getMessage(), exception);
        }
    }

    @Override
    public DiffPage diff(DiffRequest request) throws GitException {
        return new JGitDiffPage(request, repository);
    }

    @Override
    public boolean isInsideWorkTree() throws GitException {
        return RepositoryCache.FileKey.isGitRepository(getRepository().getDirectory(), FS.DETECTED);
    }

    @Override
    public ShowFileContentResponse showFileContent(ShowFileContentRequest request) throws GitException {
        String content;
        ObjectId revision;
        try {
            revision = getRepository().resolve(request.getVersion());
            try (RevWalk revWalk = new RevWalk(getRepository())) {
                RevCommit revCommit = revWalk.parseCommit(revision);
                RevTree tree = revCommit.getTree();

                try (TreeWalk treeWalk = new TreeWalk(getRepository())) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create(request.getFile()));
                    if (!treeWalk.next()) {
                        throw new GitException("fatal: Path '" + request.getFile() + "' does not exist in '"
                                               + request.getVersion() + "'" + lineSeparator());
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
    public void fetch(FetchRequest request) throws GitException, UnauthorizedException {
        String remoteName = request.getRemote();
        String remoteUri;
        try {
            List<RefSpec> fetchRefSpecs;
            List<String> refSpec = request.getRefSpec();
            if (!refSpec.isEmpty()) {
                fetchRefSpecs = new ArrayList<>(refSpec.size());
                for (String refSpecItem : refSpec) {
                    RefSpec fetchRefSpec = (refSpecItem.indexOf(':') < 0) //
                                           ? new RefSpec(Constants.R_HEADS + refSpecItem + ":") //
                                           : new RefSpec(refSpecItem);
                    fetchRefSpecs.add(fetchRefSpec);
                }
            } else {
                fetchRefSpecs = Collections.emptyList();
            }

            FetchCommand fetchCommand = getGit().fetch();

            // If this an unknown remote with no refspecs given, put HEAD
            // (otherwise JGit fails)
            if (remoteName != null && refSpec.isEmpty()) {
                boolean found = false;
                List<Remote> configRemotes = remoteList(newDto(RemoteListRequest.class));
                for (Remote configRemote : configRemotes) {
                    if (remoteName.equals(configRemote.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    fetchRefSpecs = Collections.singletonList(new RefSpec(Constants.HEAD + ":" + Constants.FETCH_HEAD));
                }
            }

            if (remoteName == null) {
                remoteName = Constants.DEFAULT_REMOTE_NAME;
            }
            fetchCommand.setRemote(remoteName);
            remoteUri = getRepository().getConfig().getString(ConfigConstants.CONFIG_REMOTE_SECTION, remoteName,
                                                              ConfigConstants.CONFIG_KEY_URL);
            fetchCommand.setRefSpecs(fetchRefSpecs);

            int timeout = request.getTimeout();
            if (timeout > 0) {
                fetchCommand.setTimeout(timeout);
            }
            fetchCommand.setRemoveDeletedRefs(request.isRemoveDeletedRefs());

            executeRemoteCommand(remoteUri, fetchCommand, request);
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
    public void init(InitRequest request) throws GitException {
        File workDir = repository.getWorkTree();
        if (!workDir.exists()) {
            throw new GitException(format(ERROR_INIT_FOLDER_MISSING, workDir));
        }
        // If create fails and the .git folder didn't exist we want to remove it.
        // We have to do this here because the create command doesn't revert its own changes in case of failure.
        boolean removeIfFailed = !repository.getDirectory().exists();

        try {
            repository.create(request.isBare());
        } catch (IOException exception) {
            if (removeIfFailed) {
                deleteRepositoryFolder();
            }
            throw new GitException(exception.getMessage(), exception);
        }
    }

    @Override
    public LogPage log(LogRequest request) throws GitException {
        LogCommand logCommand = getGit().log();
        try {
            setRevisionRange(logCommand, request);

            request.getFileFilter().forEach(logCommand::addPath);

            Iterator<RevCommit> revIterator = logCommand.call().iterator();
            List<Revision> commits = new ArrayList<>();

            while (revIterator.hasNext()) {
                RevCommit commit = revIterator.next();
                PersonIdent committerIdentity = commit.getCommitterIdent();

                GitUser gitUser = newDto(GitUser.class).withName(committerIdentity.getName())
                                                       .withEmail(committerIdentity.getEmailAddress());

                Revision revision = newDto(Revision.class).withId(commit.getId().getName())
                                                          .withMessage(commit.getFullMessage())
                                                          .withCommitTime(MILLISECONDS.convert(commit.getCommitTime(), SECONDS))
                                                          .withCommitter(gitUser);
                commits.add(revision);
            }
            return new LogPage(commits);
        } catch (GitAPIException | IOException exception) {
            String errorMessage = exception.getMessage();
            if (ERROR_LOG_NO_HEAD_EXISTS.equals(errorMessage)) {
                throw new GitException(errorMessage, ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED);
            }
            throw new GitException(errorMessage, exception);
        }
    }

    private void setRevisionRange(LogCommand logCommand, LogRequest request) throws IOException {
        if (request != null) {
            String revisionRangeSince = request.getRevisionRangeSince();
            String revisionRangeUntil = request.getRevisionRangeUntil();
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
                GitUser gitUser = newDto(GitUser.class).withName(committerIdentity.getName())
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
    public MergeResult merge(MergeRequest request) throws GitException {
        org.eclipse.jgit.api.MergeResult jGitMergeResult;
        MergeResult.MergeStatus status;
        try {
            Ref ref = repository.findRef(request.getCommit());
            if (ref == null) {
                throw new GitException("Invalid reference to commit for merge " + request.getCommit());
            }
            // Shorten local branch names by removing '/refs/heads/' from the beginning
            String name = ref.getName();
            if (name.startsWith(Constants.R_HEADS)) {
                name = name.substring(Constants.R_HEADS.length());
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
            for (ObjectId commit : jGitMergedCommits) {
                mergedCommits.add(commit.getName());
            }
        }

        List<String> conflicts;
        if (org.eclipse.jgit.api.MergeResult.MergeStatus.CHECKOUT_CONFLICT.equals(jGitMergeResult.getMergeStatus())) {
            conflicts = jGitMergeResult.getCheckoutConflicts();
        } else {
            Map<String, int[][]> jGitConflicts = jGitMergeResult.getConflicts();
            conflicts = jGitConflicts != null ? new ArrayList<>(jGitConflicts.keySet()) : Collections.emptyList();
        }

        Map<String, ResolveMerger.MergeFailureReason> jGitFailing = jGitMergeResult.getFailingPaths();
        ObjectId newHead = jGitMergeResult.getNewHead();

        return newDto(MergeResult.class).withFailed(jGitFailing != null ? new ArrayList<>(jGitFailing.keySet()) : Collections.emptyList())
                                        .withNewHead(newHead != null ? newHead.getName() : null)
                                        .withMergeStatus(status)
                                        .withConflicts(conflicts)
                                        .withMergedCommits(mergedCommits);
    }

    @Override
    public RebaseResponse rebase(RebaseRequest request) throws GitException {
        RebaseResult result;
        RebaseStatus status;
        List<String> failed;
        List<String> conflicts;
        try {
            RebaseCommand rebaseCommand = getGit().rebase();
            setRebaseOperation(rebaseCommand, request);
            String branch = request.getBranch();
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
        conflicts = result.getConflicts() != null ? result.getConflicts() : Collections.emptyList();
        failed = result.getFailingPaths() != null ? new ArrayList<>(result.getFailingPaths().keySet()) : Collections.emptyList();
        return newDto(RebaseResponse.class).withStatus(status).withConflicts(conflicts).withFailed(failed);
    }

    private void setRebaseOperation(RebaseCommand rebaseCommand, RebaseRequest request) {
        RebaseCommand.Operation op = RebaseCommand.Operation.BEGIN;

        // If other operation other than 'BEGIN' was specified, set it
        if (request.getOperation() != null) {
            switch (request.getOperation()) {
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
    public void mv(MoveRequest request) throws GitException {
        try {
            getGit().add().addFilepattern(request.getTarget()).call();
            getGit().rm().addFilepattern(request.getSource()).call();
        } catch (GitAPIException exception) {
            throw new GitException(exception.getMessage(), exception);
        }
    }

    @Override
    public PullResponse pull(PullRequest request) throws GitException, UnauthorizedException {
        String remoteName = request.getRemote();
        String remoteUri;
        try {
            if (repository.getRepositoryState().equals(RepositoryState.MERGING)) {
                throw new GitException(ERROR_PULL_MERGING);
            }
            String fullBranch = repository.getFullBranch();
            if (!fullBranch.startsWith(Constants.R_HEADS)) {
                throw new DetachedHeadException(ERROR_PULL_HEAD_DETACHED);
            }

            String branch = fullBranch.substring(Constants.R_HEADS.length());

            StoredConfig config = repository.getConfig();
            if (remoteName == null) {
                remoteName = config.getString(ConfigConstants.CONFIG_BRANCH_SECTION, branch,
                                              ConfigConstants.CONFIG_KEY_REMOTE);
                if (remoteName == null) {
                    remoteName = Constants.DEFAULT_REMOTE_NAME;
                }
            }
            remoteUri = config.getString(ConfigConstants.CONFIG_REMOTE_SECTION, remoteName, ConfigConstants.CONFIG_KEY_URL);

            String remoteBranch;
            RefSpec fetchRefSpecs = null;
            String refSpec = request.getRefSpec();
            if (refSpec != null) {
                fetchRefSpecs = (refSpec.indexOf(':') < 0) //
                                ? new RefSpec(Constants.R_HEADS + refSpec + ":" + fullBranch) //
                                : new RefSpec(refSpec);
                remoteBranch = fetchRefSpecs.getSource();
            } else {
                remoteBranch = config.getString(ConfigConstants.CONFIG_BRANCH_SECTION, branch,
                                                ConfigConstants.CONFIG_KEY_MERGE);
            }

            if (remoteBranch == null) {
                remoteBranch = fullBranch;
            }

            FetchCommand fetchCommand = getGit().fetch();
            fetchCommand.setRemote(remoteName);
            if (fetchRefSpecs != null) {
                fetchCommand.setRefSpecs(fetchRefSpecs);
            }
            int timeout = request.getTimeout();
            if (timeout > 0) {
                fetchCommand.setTimeout(timeout);
            }

            FetchResult fetchResult = (FetchResult)executeRemoteCommand(remoteUri, fetchCommand, request);

            Ref remoteBranchRef = fetchResult.getAdvertisedRef(remoteBranch);
            if (remoteBranchRef == null) {
                remoteBranchRef = fetchResult.getAdvertisedRef(Constants.R_HEADS + remoteBranch);
            }
            if (remoteBranchRef == null) {
                throw new GitException(format(ERROR_PULL_REF_MISSING, remoteBranch));
            }
            org.eclipse.jgit.api.MergeResult mergeResult = getGit().merge().include(remoteBranchRef).call();
            if (mergeResult.getMergeStatus().equals(org.eclipse.jgit.api.MergeResult.MergeStatus.ALREADY_UP_TO_DATE)) {
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
    public PushResponse push(PushRequest request) throws GitException, UnauthorizedException {
        List<Map<String, String>> updates = new ArrayList<>();
        String currentBranch = getCurrentBranch();
        String remoteName = request.getRemote();
        String remoteUri = getRepository().getConfig().getString(ConfigConstants.CONFIG_REMOTE_SECTION, remoteName,
                                                                 ConfigConstants.CONFIG_KEY_URL);
        PushCommand pushCommand = getGit().push();
        if (request.getRemote() != null) {
            pushCommand.setRemote(remoteName);
        }
        List<String> refSpec = request.getRefSpec();
        if (!refSpec.isEmpty()) {
            pushCommand.setRefSpecs(refSpec.stream()
                                           .map(RefSpec::new)
                                           .collect(Collectors.toList()));
        }
        pushCommand.setForce(request.isForce());
        int timeout = request.getTimeout();
        if (timeout > 0) {
            pushCommand.setTimeout(timeout);
        }
        try {
            @SuppressWarnings("unchecked")
            Iterable<PushResult> pushResults = (Iterable<PushResult>)executeRemoteCommand(remoteUri, pushCommand, request);
            PushResult pushResult = pushResults.iterator().next();
            String commandOutput = pushResult.getMessages().isEmpty() ? "Successfully pushed to " + remoteUri : pushResult.getMessages();
            Collection<RemoteRefUpdate> refUpdates = pushResult.getRemoteUpdates();

            for (RemoteRefUpdate remoteRefUpdate : refUpdates) {
                final String remoteRefName = remoteRefUpdate.getRemoteName();
                // check status only for branch given in the URL or tags - (handle special "refs/for" case)
                String shortenRefFor = remoteRefName.startsWith("refs/for/") ?
                                       remoteRefName.substring("refs/for/".length()) :
                                       remoteRefName;
                if (!currentBranch.equals(Repository.shortenRefName(remoteRefName)) && !currentBranch.equals(shortenRefFor)
                    && !remoteRefName.startsWith(Constants.R_TAGS)) {
                    continue;
                }
                Map<String, String> update = new HashMap<>();
                RemoteRefUpdate.Status status = remoteRefUpdate.getStatus();
                if (status != RemoteRefUpdate.Status.OK) {
                    List<String> refSpecs = request.getRefSpec();
                    if (remoteRefUpdate.getStatus() == RemoteRefUpdate.Status.UP_TO_DATE) {
                        commandOutput = INFO_PUSH_IGNORED_UP_TO_DATE;
                    } else {
                        String remoteBranch = !refSpecs.isEmpty() ? refSpecs.get(0).split(REFSPEC_COLON)[1] : "master";
                        String errorMessage =
                                format(ERROR_PUSH_CONFLICTS_PRESENT, currentBranch + BRANCH_REFSPEC_SEPERATOR + remoteBranch, remoteUri);
                        if (remoteRefUpdate.getMessage() != null) {
                            errorMessage += "\nError errorMessage: " + remoteRefUpdate.getMessage() + ".";
                        }
                        throw new GitException(errorMessage);
                    }
                }
                if (status != RemoteRefUpdate.Status.UP_TO_DATE || !remoteRefName.startsWith(Constants.R_TAGS)) {
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
    public void remoteAdd(RemoteAddRequest request) throws GitException {
        String remoteName = request.getName();
        if (isNullOrEmpty(remoteName)) {
            throw new GitException(ERROR_ADD_REMOTE_NAME_MISSING);
        }

        StoredConfig config = repository.getConfig();
        Set<String> remoteNames = config.getSubsections("remote");
        if (remoteNames.contains(remoteName)) {
            throw new GitException(format(ERROR_ADD_REMOTE_NAME_ALREADY_EXISTS, remoteName));
        }

        String url = request.getUrl();
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

        List<String> branches = request.getBranches();
        if (branches.isEmpty()) {
            remoteConfig.addFetchRefSpec(
                    new RefSpec(Constants.R_HEADS + "*" + ":" + Constants.R_REMOTES + remoteName + "/*").setForceUpdate(true));
        } else {
            for (String branch : branches) {
                remoteConfig.addFetchRefSpec(new RefSpec(Constants.R_HEADS + branch + ":" + Constants.R_REMOTES + remoteName + "/" + branch)
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
            String r = config.getString(ConfigConstants.CONFIG_BRANCH_SECTION, branch,
                                        ConfigConstants.CONFIG_KEY_REMOTE);
            if (name.equals(r)) {
                config.unset(ConfigConstants.CONFIG_BRANCH_SECTION, branch, ConfigConstants.CONFIG_KEY_REMOTE);
                config.unset(ConfigConstants.CONFIG_BRANCH_SECTION, branch, ConfigConstants.CONFIG_KEY_MERGE);
                List<Branch> remoteBranches = branchList(newDto(BranchListRequest.class).withListMode("r"));
                for (Branch remoteBranch : remoteBranches) {
                    if (remoteBranch.getDisplayName().startsWith(name)) {
                        branchDelete(newDto(BranchDeleteRequest.class).withName(remoteBranch.getName()).withForce(true));
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
    public List<Remote> remoteList(RemoteListRequest request) throws GitException {
        StoredConfig config = repository.getConfig();
        Set<String> remoteNames = new HashSet<>(config.getSubsections(ConfigConstants.CONFIG_KEY_REMOTE));
        String remote = request.getRemote();

        if (remote != null && remoteNames.contains(remote)) {
            remoteNames.clear();
            remoteNames.add(remote);
        }

        List<Remote> result = new ArrayList<>(remoteNames.size());
        for (String remoteName : remoteNames) {
            try {
                List<URIish> uris = new RemoteConfig(config, remoteName).getURIs();
                result.add(newDto(Remote.class).withName(remoteName).withUrl(uris.isEmpty() ? null : uris.get(0).toString()));
            } catch (URISyntaxException exception) {
                throw new GitException(exception.getMessage(), exception);
            }
        }
        return result;
    }

    @Override
    public void remoteUpdate(RemoteUpdateRequest request) throws GitException {
        String remoteName = request.getName();
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

        List<String> branches = request.getBranches();
        if (!branches.isEmpty()) {
            if (!request.isAddBranches()) {
                remoteConfig.setFetchRefSpecs(Collections.emptyList());
                remoteConfig.setPushRefSpecs(Collections.emptyList());
            } else {
                // Replace wildcard refSpec if any.
                remoteConfig.removeFetchRefSpec(
                        new RefSpec(Constants.R_HEADS + "*" + ":" + Constants.R_REMOTES + remoteName + "/*")
                                .setForceUpdate(true));
                remoteConfig.removeFetchRefSpec(
                        new RefSpec(Constants.R_HEADS + "*" + ":" + Constants.R_REMOTES + remoteName + "/*"));
            }

            // Add new refSpec.
            for (String branch : branches) {
                remoteConfig.addFetchRefSpec(
                        new RefSpec(Constants.R_HEADS + branch + ":" + Constants.R_REMOTES + remoteName + "/" + branch)
                                .setForceUpdate(true));
            }
        }

        // Remove URLs first.
        for (String url : request.getRemoveUrl()) {
            try {
                remoteConfig.removeURI(new URIish(url));
            } catch (URISyntaxException e) {
                LOG.debug(ERROR_UPDATE_REMOTE_REMOVE_INVALID_URL);
            }
        }

        // Add new URLs.
        for (String url : request.getAddUrl()) {
            try {
                remoteConfig.addURI(new URIish(url));
            } catch (URISyntaxException e) {
                throw new GitException("Remote url " + url + " is invalid. ");
            }
        }

        // Remove URLs for pushing.
        for (String url : request.getRemovePushUrl()) {
            try {
                remoteConfig.removePushURI(new URIish(url));
            } catch (URISyntaxException e) {
                LOG.debug(ERROR_UPDATE_REMOTE_REMOVE_INVALID_URL);
            }
        }

        // Add URLs for pushing.
        for (String url : request.getAddPushUrl()) {
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
    public void reset(ResetRequest request) throws GitException {
        try {
            ResetCommand resetCommand = getGit().reset();
            resetCommand.setRef(request.getCommit());
            List<String> patterns = request.getFilePattern();
            patterns.stream().forEach(resetCommand::addPath);

            if (request.getType() != null && patterns.isEmpty()) {
                switch (request.getType()) {
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
    public void rm(RmRequest request) throws GitException {
        List<String> files = request.getItems();
        RmCommand rmCommand = getGit().rm();

        rmCommand.setCached(request.isCached());

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
    public Status status(StatusFormat format) throws GitException {
        if (!RepositoryCache.FileKey.isGitRepository(getRepository().getDirectory(), FS.DETECTED)) {
            throw new GitException("Not a git repository");
        }
        String branchName = getCurrentBranch();
        return new JGitStatusImpl(branchName, getGit().status(), format);
    }

    @Override
    public Tag tagCreate(TagCreateRequest request) throws GitException {
        String commit = request.getCommit();
        if (commit == null) {
            commit = Constants.HEAD;
        }

        try {
            RevWalk revWalk = new RevWalk(repository);
            RevObject revObject;
            try {
                revObject = revWalk.parseAny(repository.resolve(commit));
            } finally {
                revWalk.close();
            }

            TagCommand tagCommand = getGit().tag()
                                            .setName(request.getName())
                                            .setObjectId(revObject)
                                            .setMessage(request.getMessage())
                                            .setForceUpdate(request.isForce());

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
    public void tagDelete(TagDeleteRequest request) throws GitException {
        try {
            String tagName = request.getName();
            Ref tagRef = repository.findRef(tagName);
            if (tagRef == null) {
                throw new GitException("Tag " + tagName + " not found. ");
            }

            RefUpdate updateRef = repository.updateRef(tagRef.getName());
            updateRef.setRefLogMessage("tag deleted", false);
            updateRef.setForceUpdate(true);
            Result deleteResult = updateRef.delete();
            if (deleteResult != Result.FORCED && deleteResult != Result.FAST_FORWARD) {
                throw new GitException(format(ERROR_TAG_DELETE, tagName, deleteResult));
            }
        } catch (IOException exception) {
            throw new GitException(exception.getMessage(), exception);
        }
    }

    @Override
    public List<Tag> tagList(TagListRequest request) throws GitException {
        String patternStr = request.getPattern();
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
    public List<RemoteReference> lsRemote(LsRemoteRequest request) throws UnauthorizedException, GitException {
        String remoteUrl = request.getRemoteUrl();
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
                   .map(ref -> newDto(RemoteReference.class).withCommitId(ref.getObjectId().name()).withReferenceName(ref.getName()))
                   .collect(Collectors.toList());
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
    public List<String> listFiles(LsFilesRequest request) throws GitException {
        return Arrays.asList(getWorkingDir().list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.startsWith(".");
            }
        }));
    }

    @Override
    public void cloneWithSparseCheckout(String directory, String remoteUrl, String branch) throws GitException, UnauthorizedException {
        //TODO rework this code when jgit will support sparse-checkout. Tracked issue: https://bugs.eclipse.org/bugs/show_bug.cgi?id=383772
        clone(newDto(CloneRequest.class).withRemoteUri(remoteUrl));
        if (!"master".equals(branch)) {
            checkout(newDto(CheckoutRequest.class).withName(branch));
        }
        final String sourcePath = getWorkingDir().getPath();
        final String keepDirectoryPath = sourcePath + "/" + directory;
        IOFileFilter folderFilter = new DirectoryFileFilter() {
            public boolean accept(File dir) {
                String directoryPath = dir.getPath();
                return !(directoryPath.startsWith(keepDirectoryPath) || directoryPath.startsWith(sourcePath + "/.git"));
            }
        };
        Collection<File> files = org.apache.commons.io.FileUtils.listFilesAndDirs(getWorkingDir(), TrueFileFilter.INSTANCE, folderFilter);
        try {
            DirCache index = getRepository().lockDirCache();
            int sourcePathLength = sourcePath.length() + 1;
            files.stream()
                 .filter(File::isFile)
                 .forEach(file -> index.getEntry(file.getPath().substring(sourcePathLength)).setAssumeValid(true));
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
     * @param remoteUrl
     *         remote url
     * @param command
     *         command to execute
     * @return executed command
     * @throws GitException
     * @throws GitAPIException
     * @throws UnauthorizedException
     */
    @VisibleForTesting
    Object executeRemoteCommand(String remoteUrl, TransportCommand command, GitRequest request)
            throws GitException, GitAPIException, UnauthorizedException {
        File keyDirectory = null;
        UserCredential credentials = null;

        try {
            if (GitUrlUtils.isSSH(remoteUrl)) {
                keyDirectory =  Files.createTempDir();
                final File sshKey = writePrivateKeyFile(remoteUrl, keyDirectory);

                SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
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
                command.setTransportConfigCallback(transport -> {
                    // If recursive clone is performed and git-module added by http(s) url is present in the cloned project,
                    // transport will be instance of TransportHttp in the step of cloning this module
                    if (transport instanceof SshTransport) {
                        ((SshTransport)transport).setSshSessionFactory(sshSessionFactory);
                    }
                });
            } else {
                if (remoteUrl != null && GIT_URL_WITH_CREDENTIALS_PATTERN.matcher(remoteUrl).matches()) {
                    String username = remoteUrl.substring(remoteUrl.indexOf("://") + 3, remoteUrl.lastIndexOf(":"));
                    String password = remoteUrl.substring(remoteUrl.lastIndexOf(":") + 1, remoteUrl.indexOf("@"));
                    command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
                } else {
                    String gitUser = request.getAttributes().get("username");
                    String gitPassword = request.getAttributes().get("password");
                    if (gitUser != null && gitPassword != null) {
                        command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUser, gitPassword));
                    } else {
                        credentials = credentialsLoader.getUserCredential(remoteUrl);
                        if (credentials != null) {
                            command.setCredentialsProvider(
                                    new UsernamePasswordCredentialsProvider(credentials.getUserName(), credentials.getPassword()));
                        }
                    }
                }
            }
            ProxyAuthenticator.initAuthenticator(remoteUrl);
            return command.call();
        } catch (GitException | TransportException exception) {
            if ("Unable get private ssh key".equals(exception.getMessage())) {
                throw new UnauthorizedException(exception.getMessage(), ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY);
            } else if (exception.getMessage().contains(ERROR_AUTHENTICATION_REQUIRED)) {
                final ProviderInfo info = credentialsLoader.getProviderInfo(remoteUrl);
                if (info != null) {
                    throw new UnauthorizedException(exception.getMessage(),
                                                    ErrorCodes.UNAUTHORIZED_GIT_OPERATION,
                                                    ImmutableMap.of(PROVIDER_NAME, info.getProviderName(),
                                                                    AUTHENTICATE_URL, info.getAuthenticateUrl(),
                                                                    "authenticated", Boolean.toString(credentials != null)));
                }
                throw new UnauthorizedException(exception.getMessage(), ErrorCodes.UNAUTHORIZED_GIT_OPERATION);
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
     * @throws GitException
     *         if other error occurs
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
            LOG.error("Could not remove .git folder in path " + repository.getDirectory().getPath(), exception);
        }
    }

    private Repository getRepository() {
        return repository;
    }

    /**
     * Get the current branch on the current directory
     *
     * @return the name of the branch
     * @throws GitException
     *         if any exception occurs
     */
    public String getCurrentBranch() throws GitException {
        try {
            return Repository.shortenRefName(repository.exactRef(Constants.HEAD).getLeaf().getName());
        } catch (IOException exception) {
            throw new GitException(exception.getMessage(), exception);
        }
    }

    /**
     * Method for cleaning name of remote branch to be checked out. I.e. it
     * takes something like "origin/testBranch" and returns "testBranch". This
     * is needed for view-compatibility with console Git client.
     *
     * @param branchName
     *         is a name of branch to be cleaned
     * @return branchName without remote repository name
     * @throws GitException
     */
    private String cleanRemoteName(String branchName) throws GitException {
        String returnName = branchName;
        List<Remote> remotes = this.remoteList(newDto(RemoteListRequest.class));
        for (Remote remote : remotes) {
            if (branchName.startsWith(remote.getName())) {
                returnName = branchName.replaceFirst(remote.getName() + "/", "");
            }
        }
        return returnName;
    }

    /**
     * Method for generate exception message. The default logic return message from the error.
     * It also check if the type of the message is for SSL or in case that the error
     * start with "file name to long" then it raise the relevant message
     *
     * @param error
     *        throwable error
     * @return exception message
     */
    private String generateExceptionMessage(Throwable error) {
        String message = error.getMessage();
        while (error.getCause() != null) {
            //if e caused by an SSLHandshakeException - replace thrown message with a hardcoded message
            if (error.getCause() instanceof SSLHandshakeException) {
                message = "The system is not configured to trust the security certificate provided by the Git server";
                break;
            } else if (error.getCause() instanceof IOException) {
                // Security fix - error message should not include complete local file path on the target system
                // Error message for example - File name too long (path /xx/xx/xx/xx/xx/xx/xx/xx /, working dir /xx/xx/xx)
                if (message != null && message.startsWith(FILE_NAME_TOO_LONG_ERROR_PREFIX)) {
                    try {
                        String repoPath = repository.getWorkTree().getCanonicalPath();
                        int startIndex = message.indexOf(repoPath);
                        int endIndex = message.indexOf(",");
                        if (startIndex > -1 && endIndex > -1) {
                            message = FILE_NAME_TOO_LONG_ERROR_PREFIX + " " + message.substring(startIndex + repoPath.length(), endIndex);
                        }
                        break;
                    } catch (IOException e) {
                        //Hide exception as it is only needed for this message generation
                    }
                }
            }
            error = error.getCause();
        }
        return message;
    }
}
