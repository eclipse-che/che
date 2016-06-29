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
package org.eclipse.che.git.impl.nativegit;


import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.git.Config;
import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.api.git.DiffPage;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitException;
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
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.git.impl.nativegit.commands.AddCommand;
import org.eclipse.che.git.impl.nativegit.commands.BranchCreateCommand;
import org.eclipse.che.git.impl.nativegit.commands.BranchDeleteCommand;
import org.eclipse.che.git.impl.nativegit.commands.BranchListCommand;
import org.eclipse.che.git.impl.nativegit.commands.BranchRenameCommand;
import org.eclipse.che.git.impl.nativegit.commands.CloneCommand;
import org.eclipse.che.git.impl.nativegit.commands.CommitCommand;
import org.eclipse.che.git.impl.nativegit.commands.EmptyGitCommand;
import org.eclipse.che.git.impl.nativegit.commands.FetchCommand;
import org.eclipse.che.git.impl.nativegit.commands.InitCommand;
import org.eclipse.che.git.impl.nativegit.commands.LogCommand;
import org.eclipse.che.git.impl.nativegit.commands.LsRemoteCommand;
import org.eclipse.che.git.impl.nativegit.commands.PullCommand;
import org.eclipse.che.git.impl.nativegit.commands.PushCommand;
import org.eclipse.che.git.impl.nativegit.commands.RemoteListCommand;
import org.eclipse.che.git.impl.nativegit.commands.RemoteOperationCommand;
import org.eclipse.che.git.impl.nativegit.commands.ShowFileContentCommand;
import org.eclipse.che.plugin.ssh.key.script.SshScriptProvider;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static org.eclipse.che.api.git.shared.ProviderInfo.AUTHENTICATE_URL;
import static org.eclipse.che.api.git.shared.ProviderInfo.PROVIDER_NAME;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Native implementation of GitConnection
 *
 * @author Eugene Voevodin
 */
public class NativeGitConnection implements GitConnection {

    private static final Pattern authErrorPattern                         =
            Pattern.compile(
                    ".*fatal: could not read (Username|Password) for '.*': No such device or address.*|" +
                    ".*fatal: could not read (Username|Password) for '.*': Input/output error.*|" +
                    ".*fatal: Authentication failed for '.*'.*|.*fatal: Could not read from remote repository\\.\\n\\nPlease make sure " +
                    "you have the correct access rights\\nand the repository exists\\.\\n.*",
                    Pattern.MULTILINE);
    private static final Pattern notInGitRepoErrorPattern                 =
            Pattern.compile("^fatal: Not a git repository.*(\\n.*)*$", Pattern.MULTILINE);
    private static final Pattern noInitCommitWhenBranchCreateErrorPattern = Pattern.compile("fatal: Not a valid object name: '.*'.\n");
    private static final Pattern noInitCommitWhenLogErrorPattern          =
            Pattern.compile("fatal: your current branch '.*' does not have any commits yet\n");
    private static final Pattern noInitCommitWhenPullErrorPattern         = Pattern.compile("fatal: empty ident name .* not allowed\n");
    private final NativeGit         nativeGit;
    private final CredentialsLoader credentialsLoader;
    private final GitUserResolver   userResolver;

    /**
     * @param repository
     *         directory where commands will be invoked
     * @param sshScriptProvider
     *         manager for ssh keys. If it is null default ssh will be used;
     * @param credentialsLoader
     *         loader for credentials
     * @throws GitException
     *         when some error occurs
     * @deprecated
     *         use JGit implementation instead
     */
    public NativeGitConnection(File repository, SshScriptProvider sshScriptProvider,
                               CredentialsLoader credentialsLoader, GitUserResolver userResolver) throws GitException {
        this(new NativeGit(repository, sshScriptProvider, credentialsLoader, new GitAskPassScript()), credentialsLoader, userResolver);
    }

    /**
     * @param nativeGit
     *         native git client
     * @param credentialsLoader
     *         loader for credentials
     * @throws GitException
     *         when some error occurs
     */
    public NativeGitConnection(NativeGit nativeGit, CredentialsLoader credentialsLoader, GitUserResolver userResolver)
            throws GitException {
        this.credentialsLoader = credentialsLoader;
        this.nativeGit = nativeGit;
        this.userResolver = userResolver;
    }

    @Override
    public File getWorkingDir() {
        return nativeGit.getRepository();
    }

    @Override
    public void add(AddRequest request) throws GitException {
        AddCommand command = nativeGit.createAddCommand();
        command.setFilePattern(request.getFilepattern() == null ?
                               AddRequest.DEFAULT_PATTERN :
                               request.getFilepattern());
        command.setUpdate(request.isUpdate());
        command.execute();
    }

    @Override
    public void checkout(CheckoutRequest request) throws GitException {
        nativeGit.createCheckoutCommand()
                 .setBranchName(request.getName())
                 .setStartPoint(request.getStartPoint())
                 .setCreateNew(request.isCreateNew())
                 .setTrackBranch(request.getTrackBranch())
                 .setFilePaths(request.getFiles())
                 .setNoTrack(request.isNoTrack())
                 .execute();
    }

    @Override
    public void cloneWithSparseCheckout(String directory, String remoteUrl, String branch) throws GitException, UnauthorizedException {
        /*
        Does following sequence of Git commands:
        $ git init
        $ git remote add origin <URL>
        $ git config core.sparsecheckout true
        $ echo keepDirectory >> .git/info/sparse-checkout
        $ git pull origin master
        */
        init(newDto(InitRequest.class).withBare(false));
        remoteAdd(newDto(RemoteAddRequest.class).withName("origin").withUrl(remoteUrl));
        getConfig().add("core.sparsecheckout", "true");
        try {
            Files.write(Paths.get(getWorkingDir() + "/.git/info/sparse-checkout"),
                        (directory.startsWith("/") ? directory : "/" + directory).getBytes());
        } catch (IOException exception) {
            throw new GitException(exception.getMessage(), exception);
        }
        try {
            fetch(newDto(FetchRequest.class).withRemote("origin"));
        } catch (GitException exception) {
            throw new GitException(
                    String.format("Unable to fetch remote branch %s. Make sure it exists and can be accessed.", branch), exception);
        }
        try {
            checkout(newDto(CheckoutRequest.class).withName(branch));
        } catch (GitException exception) {
            throw new GitException(
                    String.format("Unable to checkout branch %s. Make sure it exists and can be accessed.", branch), exception);
        }
    }

    @Override
    public Branch branchCreate(BranchCreateRequest request) throws GitException {
        BranchCreateCommand branchCreateCommand = nativeGit.createBranchCreateCommand();
        branchCreateCommand.setBranchName(request.getName())
                           .setStartPoint(request.getStartPoint());
        try {
            branchCreateCommand.execute();
        } catch (ServerException exception) {
            if (noInitCommitWhenBranchCreateErrorPattern.matcher(exception.getMessage()).find()) {
                throw new GitException(exception.getMessage(), ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED);
            }
        }
        return DtoFactory.getInstance().createDto(Branch.class).withName(getBranchRef(request.getName())).withActive(false)
                         .withDisplayName(request.getName()).withRemote(false);
    }

    @Override
    public void branchDelete(BranchDeleteRequest request) throws GitException, UnauthorizedException {
        String branchName = getBranchRef(request.getName());
        String remoteName = null;
        String remoteUri = null;

        if (branchName.startsWith("refs/remotes/")) {
            remoteName = parseRemoteName(branchName);
            remoteUri = getRemoteUri(remoteName);
        }
        branchName = parseBranchName(branchName);

        BranchDeleteCommand branchDeleteCommand = nativeGit.createBranchDeleteCommand();

        branchDeleteCommand.setBranchName(branchName)
                           .setRemote(remoteName)
                           .setDeleteFullyMerged(request.isForce())
                           .setRemoteUri(remoteUri);

        executeRemoteCommand(branchDeleteCommand);
    }

    @Override
    public void branchRename(String oldName, String newName) throws GitException, UnauthorizedException {
        String branchName = getBranchRef(oldName);
        String remoteName = null;
        String remoteUri = null;

        if (branchName.startsWith("refs/remotes/")) {
            remoteName = parseRemoteName(branchName);
            remoteUri = getRemoteUri(remoteName);
        }

        branchName = remoteName != null ? parseBranchName(branchName) : oldName;

        BranchRenameCommand branchRenameCommand = nativeGit.createBranchRenameCommand();

        branchRenameCommand.setNames(branchName, newName)
                           .setRemote(remoteName)
                           .setRemoteUri(remoteUri);

        executeRemoteCommand(branchRenameCommand);
    }

    @Override
    public List<Branch> branchList(BranchListRequest request) throws GitException {
        String listMode = request.getListMode();
        if (listMode != null
            && !(listMode.equals(BranchListRequest.LIST_ALL) || listMode.equals(BranchListRequest.LIST_REMOTE))) {
            throw new IllegalArgumentException("Unsupported list mode '" + listMode + "'. Must be either 'a' or 'r'. ");
        }
        List<Branch> branches;
        BranchListCommand branchListCommand = nativeGit.createBranchListCommand();
        if (request.getListMode() == null) {
            branches = branchListCommand.execute();
        } else if (request.getListMode().equals(BranchListRequest.LIST_ALL)) {
            branches = branchListCommand.execute();
            branches.addAll(branchListCommand.setShowRemotes(true).execute());
        } else {
            branches = branchListCommand.setShowRemotes(true).execute();
        }
        return branches;
    }

    @Override
    public List<String> listFiles(LsFilesRequest request) throws GitException {
        return nativeGit.createListFilesCommand()
                        .setOthers(request.isOthers())
                        .setModified(request.isModified())
                        .setStaged(request.isStaged())
                        .setCached(request.isCached())
                        .setDeleted(request.isDeleted())
                        .setIgnored(request.isIgnored())
                        .setExcludeStandard(request.isExcludeStandard())
                        .execute();
    }

    @Override
    public void clone(CloneRequest request) throws URISyntaxException, UnauthorizedException, GitException {
        final String remoteUri = request.getRemoteUri();
        CloneCommand clone = nativeGit.createCloneCommand();
        clone.setRemoteUri(remoteUri);
        clone.setRemoteName(request.getRemoteName());
        clone.setRecursiveEnabled(request.isRecursive());
        if (clone.getTimeout() > 0) {
            clone.setTimeout(request.getTimeout());
        }

        executeRemoteCommand(clone);

        UserCredential credentials = credentialsLoader.getUserCredential(remoteUri);
        if (credentials != null) {
            getConfig().set("codenvy.credentialsProvider", credentials.getProviderId());
        }
        nativeGit.createRemoteUpdateCommand()
                 .setRemoteName(request.getRemoteName() == null ? "origin" : request.getRemoteName())
                 .setNewUrl(remoteUri)
                 .execute();
    }

    @Override
    public Revision commit(CommitRequest request) throws GitException {
        CommitCommand command = nativeGit.createCommitCommand();
        GitUser committer = getLocalCommitter();
        command.setCommitter(committer);

        try {
            // overrider author from .gitconfig. We may set it in previous versions.
            // We need to override it since committer can differ from the person who clone or init repository.
            getConfig().get("user.name");
            command.setAuthor(committer);
        } catch (GitException e) {
            //ignore property not found.
        }

        command.setAll(request.isAll());
        command.setAmend(request.isAmend());
        command.setMessage(request.getMessage());
        command.setFiles(request.getFiles());

        command.execute();
        LogCommand log = nativeGit.createLogCommand();
        Revision rev = log.execute().get(0);
        rev.setBranch(getCurrentBranch());
        return rev;
    }

    @Override
    public DiffPage diff(DiffRequest request) throws GitException {
        return new NativeGitDiffPage(request, nativeGit);
    }

    @Override
    public ShowFileContentResponse showFileContent(ShowFileContentRequest request) throws GitException {
        ShowFileContentCommand showCommand = nativeGit.createShowFileContentCommand().withFile(request.getFile())
                                                      .withVersion(request.getVersion());
        return showCommand.execute();
    }

    @Override
    public void fetch(FetchRequest request) throws GitException, UnauthorizedException {
        String remoteUri = getRemoteUri(request.getRemote());
        FetchCommand fetchCommand = nativeGit.createFetchCommand();
        fetchCommand.setRemote(request.getRemote())
                    .setPrune(request.isRemoveDeletedRefs())
                    .setRefSpec(request.getRefSpec())
                    .setRemoteUri(remoteUri)
                    .setTimeout(request.getTimeout());
        executeRemoteCommand(fetchCommand);
    }

    @Override
    public boolean isInsideWorkTree() throws GitException {
        final EmptyGitCommand emptyGitCommand = nativeGit.createEmptyGitCommand();
        
        // command "rev-parse --is-inside-work-tree" returns true/false
        try {
            emptyGitCommand.setNextParameter("rev-parse")
                           .setNextParameter("--is-inside-work-tree")
                           .execute();
                   
            final String output = emptyGitCommand.getText();
            return Boolean.valueOf(output);
        } catch(GitException ge) {
            String msg = ge.getMessage();
            if (msg != null && notInGitRepoErrorPattern.matcher(msg).matches()) {
                return false;
            }
            
            throw ge;
        }
    }

    @Override
    public void init(InitRequest request) throws GitException {
        InitCommand initCommand = nativeGit.createInitCommand();
        initCommand.setBare(request.isBare());
        initCommand.execute();
    }

    @Override
    public LogPage log(LogRequest request) throws GitException {
        try {
            return new LogPage(nativeGit.createLogCommand().setFileFilter(request.getFileFilter()).execute());
        } catch (ServerException exception) {
            if (noInitCommitWhenLogErrorPattern.matcher(exception.getMessage()).find()) {
                throw new GitException(exception.getMessage(), ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED);
            } else {
                throw exception;
            }
        }        
    }

    @Override
    public List<RemoteReference> lsRemote(LsRemoteRequest request) throws GitException, UnauthorizedException {
        LsRemoteCommand command = nativeGit.createLsRemoteCommand().setRemoteUrl(request.getRemoteUrl());
        executeRemoteCommand(command);
        return command.getRemoteReferences();
    }

    @Override
    public MergeResult merge(MergeRequest request) throws GitException {
        final String gitObjectType = getRevisionType(request.getCommit());
        if (!("commit".equalsIgnoreCase(gitObjectType) || "tag".equalsIgnoreCase(gitObjectType))) {
            throw new GitException("Invalid object for merge " + request.getCommit() + ".");
        }
        return nativeGit.createMergeCommand().setCommit(request.getCommit()).setCommitter(getLocalCommitter()).execute();
    }

    @Override
    public RebaseResponse rebase(RebaseRequest request) throws GitException {
    	throw new GitException("Unsupported method");
    }

    @Override
    public void mv(MoveRequest request) throws GitException {
        nativeGit.createMoveCommand()
                 .setSource(request.getSource())
                 .setTarget(request.getTarget())
                 .execute();
    }

    @Override
    public PullResponse pull(PullRequest request) throws GitException, UnauthorizedException {
        String remoteUri = getRemoteUri(request.getRemote());

        PullCommand pullCommand = nativeGit.createPullCommand();
        pullCommand.setRemote(request.getRemote())
                   .setRefSpec(request.getRefSpec())
                   .setAuthor(getLocalCommitter())
                   .setRemoteUri(remoteUri)
                   .setTimeout(request.getTimeout());

        try {
            executeRemoteCommand(pullCommand);
        } catch (GitException exception) {
            if (noInitCommitWhenPullErrorPattern.matcher(exception.getMessage()).find()) {
                throw new GitException(exception.getMessage(), ErrorCodes.NO_COMMITTER_NAME_OR_EMAIL_DEFINED);
            } else if ("Unable get private ssh key".equals(exception.getMessage())) {
                throw new GitException(exception.getMessage(), ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY);
            } else if (("Auto-merging file\nCONFLICT (content): Merge conflict in file\n" +
                        "Automatic merge failed; fix conflicts and then commit the result.\n").equals(exception.getMessage())) {
                throw new GitException(exception.getMessage(), ErrorCodes.MERGE_CONFLICT);
            } else {
                throw exception;
            }
        }

        return pullCommand.getPullResponse();
    }

    @Override
    public PushResponse push(PushRequest request) throws GitException, UnauthorizedException {
        String remoteUri = getRemoteUri(request.getRemote());

        PushCommand pushCommand = nativeGit.createPushCommand();

        pushCommand.setRemote(request.getRemote())
                   .setForce(request.isForce())
                   .setRefSpec(request.getRefSpec())
                   .setRemoteUri(remoteUri)
                   .setTimeout(request.getTimeout());

        executeRemoteCommand(pushCommand);

        return pushCommand.getPushResponse();
    }

    @Override
    public void remoteAdd(RemoteAddRequest request) throws GitException {
        nativeGit.createRemoteAddCommand()
                 .setName(request.getName())
                 .setUrl(request.getUrl())
                 .setBranches(request.getBranches())
                 .execute();
    }

    @Override
    public void remoteDelete(String name) throws GitException {
        nativeGit.createRemoteDeleteCommand().setName(name).execute();
    }

    @Override
    public List<Remote> remoteList(RemoteListRequest request) throws GitException {
        RemoteListCommand remoteListCommand = nativeGit.createRemoteListCommand();
        return remoteListCommand.setRemoteName(request.getRemote()).execute();
    }

    @Override
    public void remoteUpdate(RemoteUpdateRequest request) throws GitException {
        nativeGit.createRemoteUpdateCommand()
                 .setRemoteName(request.getName())
                 .setAddUrl(request.getAddUrl())
                 .setBranchesToAdd(request.getBranches())
                 .setAddBranches(request.isAddBranches())
                 .setAddPushUrl(request.getAddPushUrl())
                 .setRemovePushUrl(request.getRemovePushUrl())
                 .setRemoveUrl(request.getRemoveUrl())
                 .execute();
    }

    @Override
    public void reset(ResetRequest request) throws GitException {
        nativeGit.createResetCommand()
                 .setMode(request.getType().getValue())
                 .setCommit(request.getCommit())
                 .setFilePattern(request.getFilePattern())
                 .execute();
    }

    @Override
    public void rm(RmRequest request) throws GitException {
        nativeGit.createRemoveCommand()
                 .setCached(request.isCached())
                 .setListOfItems(request.getItems())
                 .setRecursively(request.isRecursively())
                 .execute();
    }

    @Override
    public Status status(final StatusFormat format) throws GitException {
        return new NativeGitStatusImpl(getCurrentBranch(), nativeGit, format);
    }

    @Override
    public Tag tagCreate(TagCreateRequest request) throws GitException {
        return nativeGit.createTagCreateCommand().setName(request.getName())
                        .setCommitter(getLocalCommitter())
                        .setCommit(request.getCommit())
                        .setMessage(request.getMessage())
                        .setForce(request.isForce())
                        .execute();
    }

    @Override
    public void tagDelete(TagDeleteRequest request) throws GitException {
        nativeGit.createTagDeleteCommand().setName(request.getName()).execute();
    }

    @Override
    public List<Tag> tagList(TagListRequest request) throws GitException {
        return nativeGit.createTagListCommand().setPattern(request.getPattern()).execute();
    }

    @Override
    public List<GitUser> getCommiters() throws GitException {
        List<GitUser> users = new LinkedList<>();
        List<Revision> revList = nativeGit.createLogCommand().execute();
        for (Revision rev : revList) {
            users.add(rev.getCommitter());
        }
        return users;
    }

    @Override
    public Config getConfig() throws GitException {
        return nativeGit.createConfig();
    }

    @Override
    public void close() {
        //do not need to do anything
    }

    @Override
    public void setOutputLineConsumerFactory(LineConsumerFactory gitOutputPublisherFactory) {
        nativeGit.setOutputLineConsumerFactory(gitOutputPublisherFactory);
    }

    /**
     * Gets current branch name.
     *
     * @return name of current branch or <code>null</code> if current branch not exists
     * @throws GitException
     *         if any error occurs
     */
    private String getCurrentBranch() throws GitException {
        BranchListCommand command = nativeGit.createBranchListCommand();
        command.execute();
        String branchName = null;
        for (String outLine : command.getLines()) {
            if (outLine.indexOf('*') != -1) {
                branchName = outLine.substring(2);
            }
        }
        return branchName;
    }

    /**
     * Executes remote command.
     * <p/>
     * Note: <i>'need for authorization'</i> check based on command execution fail message, so this
     * check can fail when i.e. git version updated, for more information see {@link #isOperationNeedAuth(String)}
     *
     * @param command
     *         remote command which should be executed
     * @throws GitException
     *         when error occurs while {@code command} execution is going except of unauthorized error
     * @throws UnauthorizedException
     *         when it is not possible to execute {@code command} with existing credentials
     */
    private void executeRemoteCommand(RemoteOperationCommand<?> command) throws GitException, UnauthorizedException {
        try {
            command.execute();
        } catch (GitException gitEx) {
            if (!isOperationNeedAuth(gitEx.getMessage())) {
                throw gitEx;
            }
            ProviderInfo info = credentialsLoader.getProviderInfo(command.getRemoteUri());
            if (info != null) {
                boolean isAuthenticated = credentialsLoader.getUserCredential(command.getRemoteUri()) != null;
                throw new UnauthorizedException(gitEx.getMessage(),
                                                ErrorCodes.UNAUTHORIZED_GIT_OPERATION,
                                                ImmutableMap.of(PROVIDER_NAME, info.getProviderName(),
                                                                AUTHENTICATE_URL, info.getAuthenticateUrl(),
                                                                "authenticated", Boolean.toString(isAuthenticated)));
            }
            throw new UnauthorizedException(gitEx.getMessage(), ErrorCodes.UNAUTHORIZED_GIT_OPERATION);
        }
    }

    /**
     * Check if error message from git output corresponding authenticate issue.
     */
    private boolean isOperationNeedAuth(String errorMessage) {
        return authErrorPattern.matcher(errorMessage).find();
    }

    /**
     * Gets branch ref by branch name.
     *
     * @param branchName
     *         existing git branch name
     * @return ref to the branch
     * @throws GitException
     *         when it is not possible to get branchName ref
     */
    private String getBranchRef(String branchName) throws GitException {
        EmptyGitCommand command = nativeGit.createEmptyGitCommand();
        command.setNextParameter("show-ref").setNextParameter(branchName).execute();
        final String output = command.getText();

        if (output.isEmpty()) {
            throw new GitException("Error getting reference of branch.");
        }

        return output.split(" ")[1];
    }


    /**
     * Gets type of git object.
     *
     * @param gitObject
     *         revision object e.g. commit, tree, blob, tag.
     * @return type of git object
     */
    private String getRevisionType(String gitObject) throws GitException {
        EmptyGitCommand command = nativeGit.createEmptyGitCommand()
                                           .setNextParameter("cat-file")
                                           .setNextParameter("-t")
                                           .setNextParameter(gitObject);
        command.execute();
        return command.getText();
    }

    private String parseBranchName(String name) {
        int branchNameIndex = 0;
        if (name.startsWith("refs/remotes/")) {
            branchNameIndex = name.indexOf("/", "refs/remotes/".length()) + 1;
        } else if (name.startsWith("refs/heads/")) {
            branchNameIndex = name.indexOf("/", "refs/heads".length()) + 1;
        }
        return name.substring(branchNameIndex);
    }

    private String parseRemoteName(String branchRef) {
        int remoteStartIndex = "refs/remotes/".length();
        int remoteEndIndex = branchRef.indexOf("/", remoteStartIndex);
        return branchRef.substring(remoteStartIndex, remoteEndIndex);
    }

    private GitUser getLocalCommitter() throws GitException {
        return userResolver.getUser();
    }


    private String getRemoteUri(String remoteName) throws GitException {
        List<Remote> remotes;
        try {
            remotes = nativeGit.createRemoteListCommand()
                    .setRemoteName(remoteName)
                    .execute();
        } catch (GitException ignored) {
            return remoteName;
        }

        if (remotes.isEmpty()) {
            throw new GitException("No remote repository specified.  " +
                    "Please, specify either a URL or a remote name from which new revisions should be fetched in request.");
        }

        return remotes.get(0).getUrl();
    }

}
