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
package org.eclipse.che.ide.api.git;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.Commiters;
import org.eclipse.che.api.git.shared.DiffType;
import org.eclipse.che.api.git.shared.GitUrlVendorInfo;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.RepoInfo;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import java.util.List;
import java.util.Map;

/**
 * Service contains methods for working with Git repository from client side.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
public interface GitServiceClient {

    /**
     * Add changes to Git index (temporary storage). Sends request over WebSocket.
     *
     * @param devMachine
     *         current machine
     * @param projectConfig
     *         project (root of GIT repository)
     * @param update
     *         if <code>true</code> then never stage new files, but stage modified new contents of tracked files and remove files from
     *         the index if the corresponding files in the working tree have been removed
     * @param filePattern
     *         pattern of the files to be added, default is "." (all files are added)
     * @param callback
     *         callback
     * @throws WebSocketException
     * @deprecated use {@link #add(DevMachine, Path, boolean, Path[])}
     */
    void add(DevMachine devMachine,
             ProjectConfig projectConfig,
             boolean update,
             List<String> filePattern,
             RequestCallback<Void> callback) throws WebSocketException;

    /**
     * Add changes to Git index (temporary storage). Sends request over WebSocket.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param update
     *         if <code>true</code> then never stage new files, but stage modified new contents of tracked files and remove files from
     *         the index if the corresponding files in the working tree have been removed
     * @param paths
     *         pattern of the files to be added, default is "." (all files are added)
     * @throws WebSocketException
     */
    Promise<Void> add(DevMachine devMachine, Path project, boolean update, Path[] paths);

    /**
     * Fetch changes from remote repository to local one (sends request over WebSocket).
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project root of GIT repository
     * @param remote
     *         remote repository's name
     * @param refspec
     *         list of refspec to fetch.
     *         <p/>
     *         Expected form is:
     *         <ul>
     *         <li>refs/heads/featured:refs/remotes/origin/featured - branch 'featured' from remote repository will be fetched to
     *         'refs/remotes/origin/featured'.</li>
     *         <li>featured - remote branch name.</li>
     *         </ul>
     * @param removeDeletedRefs
     *         if <code>true</code> then delete removed refs from local repository
     * @param callback
     *         callback
     * @throws WebSocketException
     * @deprecated use {@link #fetch(DevMachine, Path, String, List, boolean)}
     */
    @Deprecated
    void fetch(DevMachine devMachine,
               ProjectConfigDto project,
               String remote,
               List<String> refspec,
               boolean removeDeletedRefs,
               RequestCallback<String> callback) throws WebSocketException;

    /**
     * Fetch changes from remote repository to local one (sends request over WebSocket).
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project root of GIT repository
     * @param remote
     *         remote repository's name
     * @param refspec
     *         list of refspec to fetch.
     *         <p/>
     *         Expected form is:
     *         <ul>
     *         <li>refs/heads/featured:refs/remotes/origin/featured - branch 'featured' from remote repository will be fetched to
     *         'refs/remotes/origin/featured'.</li>
     *         <li>featured - remote branch name.</li>
     *         </ul>
     * @param removeDeletedRefs
     *         if <code>true</code> then delete removed refs from local repository
     * @throws WebSocketException
     */
    Promise<Void> fetch(DevMachine devMachine, Path project, String remote, List<String> refspec, boolean removeDeletedRefs);

    /**
     * Get the list of the branches. For now, all branches cannot be returned at once, so the parameter <code>remote</code> tells to get
     * remote branches if <code>true</code> or local ones (if <code>false</code>).
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param mode
     *         get remote branches
     * @param callback
     * @deprecated use {@link #branchList(DevMachine, Path, BranchListMode)}
     */
    @Deprecated
    void branchList(DevMachine devMachine,
                    ProjectConfig project,
                    @Nullable BranchListMode mode,
                    AsyncRequestCallback<List<Branch>> callback);

    /**
     * Get the list of the branches. For now, all branches cannot be returned at once, so the parameter <code>remote</code> tells to get
     * remote branches if <code>true</code> or local ones (if <code>false</code>).
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param mode
     *         get remote branches
     */
    Promise<List<Branch>> branchList(DevMachine devMachine, Path project, BranchListMode mode);

    /**
     * Delete branch.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param name
     *         name of the branch to delete
     * @param force
     *         force if <code>true</code> delete branch {@code name} even if it is not fully merged
     * @deprecated use {@link #branchDelete(DevMachine, Path, String, boolean)}
     */
    @Deprecated
    void branchDelete(DevMachine devMachine,
                      ProjectConfigDto project,
                      String name,
                      boolean force,
                      AsyncRequestCallback<String> callback);

    /**
     * Delete branch.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param name
     *         name of the branch to delete
     * @param force
     *         force if <code>true</code> delete branch {@code name} even if it is not fully merged
     */
    Promise<Void> branchDelete(DevMachine devMachine, Path project, String name, boolean force);

    /**
     * Checkout the branch with pointed name.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param oldName
     *         branch's current name
     * @param newName
     *         branch's new name
     * @deprecated use {@link #branchRename(DevMachine, Path, String, String)}
     */
    @Deprecated
    void branchRename(DevMachine devMachine,
                      ProjectConfigDto project,
                      String oldName,
                      String newName,
                      AsyncRequestCallback<String> callback);

    /**
     * Checkout the branch with pointed name.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param oldName
     *         branch's current name
     * @param newName
     *         branch's new name
     */
    Promise<Void> branchRename(DevMachine devMachine, Path project, String oldName, String newName);

    /**
     * Create new branch with pointed name.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param name
     *         new branch's name
     * @param startPoint
     *         name of a commit at which to start the new branch
     * @deprecated use {@link #branchCreate(DevMachine, Path, String, String)}
     */
    @Deprecated
    void branchCreate(DevMachine devMachine,
                      ProjectConfigDto project,
                      String name,
                      @Nullable String startPoint,
                      AsyncRequestCallback<Branch> callback);

    /**
     * Create new branch with pointed name.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param name
     *         new branch's name
     * @param startPoint
     *         name of a commit at which to start the new branch
     */
    Promise<Branch> branchCreate(DevMachine devMachine, Path project, String name, String startPoint);

    /**
     * Checkout the branch with pointed name.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param checkoutRequest
     *         checkout request
     * @deprecated {@link #checkout(DevMachine, Path, CheckoutRequest)}
     */
    @Deprecated
    void checkout(DevMachine devMachine,
                  ProjectConfig project,
                  CheckoutRequest checkoutRequest,
                  AsyncRequestCallback<String> callback);

    /**
     * Checkout the branch with pointed name.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param request
     *         checkout request
     */
    Promise<Void> checkout(DevMachine devMachine, Path project, CheckoutRequest request);

    /**
     * Get the list of remote repositories for pointed by {@code projectConfig} parameter one.
     *
     * @param devMachine
     *         current machine
     * @param projectConfig
     *         project (root of GIT repository)
     * @param remoteName
     *         remote repository's name
     * @param verbose
     *         If <code>true</code> show remote url and name otherwise show remote name
     * @deprecated instead of this method should use {@link GitServiceClient#remoteList(DevMachine, ProjectConfig, String, boolean)}
     */
    @Deprecated
    void remoteList(DevMachine devMachine,
                    ProjectConfigDto projectConfig,
                    @Nullable String remoteName,
                    boolean verbose,
                    AsyncRequestCallback<List<Remote>> callback);

    /**
     * Get the list of remote repositories for pointed by {@code projectConfig} parameter one.
     *
     * @param devMachine
     *         current machine
     * @param projectConfig
     *         project (root of GIT repository)
     * @param remoteName
     *         remote repository's name. Can be null in case when it is need to fetch all {@link Remote}
     * @param verbose
     *         If <code>true</code> show remote url and name otherwise show remote name
     * @return a promise that provides list {@link Remote} repositories for the {@code workspaceId}, {@code projectConfig},
     * {@code remoteName}, {@code verbose} or rejects with an error.
     * @deprecated use {@link #remoteList(DevMachine, Path, String, boolean)}
     */
    @Deprecated
    Promise<List<Remote>> remoteList(DevMachine devMachine, ProjectConfig projectConfig, @Nullable String remoteName, boolean verbose);

    /**
     * Get the list of remote repositories for pointed by {@code projectConfig} parameter one.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param remote
     *         remote repository's name. Can be null in case when it is need to fetch all {@link Remote}
     * @param verbose
     *         If <code>true</code> show remote url and name otherwise show remote name
     * @return a promise that provides list {@link Remote} repositories for the {@code workspaceId}, {@code projectConfig},
     * {@code remoteName}, {@code verbose} or rejects with an error.
     * @deprecated use {@link #remoteList(DevMachine, Path, String, boolean)}
     */
    Promise<List<Remote>> remoteList(DevMachine devMachine, Path project, String remote, boolean verbose);

    /**
     * Adds remote repository to the list of remote repositories.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param name
     *         remote repository's name
     * @param url
     *         remote repository's URL
     * @deprecated use {@link #remoteAdd(DevMachine, Path, String, String)}
     */
    @Deprecated
    void remoteAdd(DevMachine devMachine,
                   ProjectConfig project,
                   String name,
                   String url,
                   AsyncRequestCallback<String> callback);

    /**
     * Adds remote repository to the list of remote repositories.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param name
     *         remote repository's name
     * @param url
     *         remote repository's URL
     */
    Promise<Void> remoteAdd(DevMachine devMachine, Path project, String name, String url);

    /**
     * Deletes the pointed(by name) remote repository from the list of repositories.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param name
     *         remote repository name to delete
     * @deprecated use {@link #remoteDelete(DevMachine, Path, String)}
     */
    @Deprecated
    void remoteDelete(DevMachine devMachine,
                      ProjectConfig project,
                      String name,
                      AsyncRequestCallback<String> callback);

    /**
     * Deletes the pointed(by name) remote repository from the list of repositories.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param name
     *         remote repository name to delete
     */
    Promise<Void> remoteDelete(DevMachine devMachine, Path project, String name);

    /**
     * Remove items from the working tree and the index.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param items
     *         items to remove
     * @param cached
     *         is for removal only from index
     * @deprecated use {@link #remove(DevMachine, Path, Path[], boolean)}
     */
    @Deprecated
    void remove(DevMachine devMachine, ProjectConfigDto project, List<String> items, boolean cached, AsyncRequestCallback<String> callback);

    /**
     * Remove items from the working tree and the index.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param items
     *         items to remove
     * @param cached
     *         is for removal only from index
     */
    Promise<Void> remove(DevMachine devMachine, Path project, Path[] items, boolean cached);

    /**
     * Reset current HEAD to the specified state. There two types of the reset: <br>
     * 1. Reset files in index - content of files is untouched. Typically it is useful to remove from index mistakenly added files.<br>
     * <code>git reset [paths]</code> is the opposite of <code>git add [paths]</code>. 2. Reset the current branch head to [commit] and
     * possibly updates the index (resetting it to the tree of [commit]) and the working tree depending on [mode].
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param commit
     *         commit to which current head should be reset
     * @param resetType
     *         type of the reset
     * @param filePattern
     *         pattern of the files to reset the index. If <code>null</code> then reset the current branch head to [commit],
     *         else reset received files in index.
     * @deprecated use {@link #reset(DevMachine, Path, String, ResetRequest.ResetType, Path[])}
     */
    @Deprecated
    void reset(DevMachine devMachine,
               ProjectConfigDto project,
               String commit,
               @Nullable ResetRequest.ResetType resetType,
               @Nullable List<String> filePattern,
               AsyncRequestCallback<Void> callback);

    /**
     * Reset current HEAD to the specified state. There two types of the reset: <br>
     * 1. Reset files in index - content of files is untouched. Typically it is useful to remove from index mistakenly added files.<br>
     * <code>git reset [paths]</code> is the opposite of <code>git add [paths]</code>. 2. Reset the current branch head to [commit] and
     * possibly updates the index (resetting it to the tree of [commit]) and the working tree depending on [mode].
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param commit
     *         commit to which current head should be reset
     * @param resetType
     *         type of the reset
     * @param files
     *         pattern of the files to reset the index. If <code>null</code> then reset the current branch head to [commit],
     *         else reset received files in index.
     */
    Promise<Void> reset(DevMachine devMachine, Path project, String commit, ResetRequest.ResetType resetType, Path[] files);

    /**
     * Initializes new Git repository (over WebSocket).
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param bare
     *         to create bare repository or not
     * @param callback
     *         callback
     * @deprecated use {@link #init(DevMachine, Path, boolean)}
     */
    @Deprecated
    void init(DevMachine devMachine, ProjectConfigDto project, boolean bare, RequestCallback<Void> callback) throws WebSocketException;

    /**
     * Initializes new Git repository (over WebSocket).
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param bare
     *         to create bare repository or not
     */
    Promise<Void> init(DevMachine devMachine, Path project, boolean bare);

    /**
     * Pull (fetch and merge) changes from remote repository to local one (sends request over WebSocket).
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param refSpec
     *         list of refspec to fetch.
     *         <p/>
     *         Expected form is:
     *         <ul>
     *         <li>refs/heads/featured:refs/remotes/origin/featured - branch 'featured' from remote repository will be fetched to
     *         'refs/remotes/origin/featured'.</li>
     *         <li>featured - remote branch name.</li>
     *         </ul>
     * @param remote
     *         remote remote repository's name
     * @param callback
     *         callback
     * @throws WebSocketException
     * @deprecated use {@link #pull(DevMachine, Path, String, String)}
     */
    @Deprecated
    void pull(DevMachine devMachine,
              ProjectConfigDto project,
              String refSpec,
              String remote,
              AsyncRequestCallback<PullResponse> callback);

    /**
     * Pull (fetch and merge) changes from remote repository to local one (sends request over WebSocket).
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param refSpec
     *         list of refspec to fetch.
     *         <p/>
     *         Expected form is:
     *         <ul>
     *         <li>refs/heads/featured:refs/remotes/origin/featured - branch 'featured' from remote repository will be fetched to
     *         'refs/remotes/origin/featured'.</li>
     *         <li>featured - remote branch name.</li>
     *         </ul>
     * @param remote
     *         remote remote repository's name
     */
    Promise<PullResponse> pull(DevMachine devMachine, Path project, String refSpec, String remote);

    /**
     * Push changes from local repository to remote one (sends request over WebSocket).
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project
     * @param refSpec
     *         list of refspec to push
     * @param remote
     *         remote repository name or url
     * @param force
     *         push refuses to update a remote ref that is not an ancestor of the local ref used to overwrite it. If <code>true</code>
     *         disables the check. This can cause the remote repository to lose commits
     * @param callback
     *         callback
     * @throws WebSocketException
     * @deprecated use {@link #push(DevMachine, Path, List, String, boolean)}
     */
    @Deprecated
    void push(DevMachine devMachine,
              ProjectConfigDto project,
              List<String> refSpec,
              String remote, boolean force,
              AsyncRequestCallback<PushResponse> callback);


    /**
     * Push changes from local repository to remote one (sends request over WebSocket).
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project
     * @param refSpec
     *         list of refspec to push
     * @param remote
     *         remote repository name or url
     * @param force
     *         push refuses to update a remote ref that is not an ancestor of the local ref used to overwrite it. If <code>true</code>
     *         disables the check. This can cause the remote repository to lose commits
     * @deprecated use {@link #push(DevMachine, Path, List, String, boolean)}
     */
    @Deprecated
    Promise<PushResponse> push(DevMachine devMachine,
                               ProjectConfig project,
                               List<String> refSpec,
                               String remote,
                               boolean force);

    /**
     * Push changes from local repository to remote one (sends request over WebSocket).
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project
     * @param refSpec
     *         list of refspec to push
     * @param remote
     *         remote repository name or url
     * @param force
     *         push refuses to update a remote ref that is not an ancestor of the local ref used to overwrite it. If <code>true</code>
     *         disables the check. This can cause the remote repository to lose commits
     */
    Promise<PushResponse> push(DevMachine devMachine, Path project, List<String> refSpec, String remote, boolean force);

    /**
     * Clones one remote repository to local one (over WebSocket).
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param remoteUri
     *         the location of the remote repository
     * @param remoteName
     *         remote name instead of "origin"
     * @param callback
     *         callback
     * @throws WebSocketException
     * @deprecated not used anymore
     */
    void cloneRepository(DevMachine devMachine,
                         ProjectConfigDto project,
                         String remoteUri,
                         String remoteName,
                         RequestCallback<RepoInfo> callback) throws WebSocketException;

    /**
     * Performs commit changes from index to repository. The result of the commit is represented by {@link Revision}, which is returned by
     * callback in <code>onSuccess(Revision result)</code>. Sends request over WebSocket.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param message
     *         commit log message
     * @param all
     *         automatically stage files that have been modified and deleted
     * @param amend
     *         indicates that previous commit must be overwritten
     * @param callback
     *         callback
     * @throws WebSocketException
     * @deprecated use {@link #commit(DevMachine, Path, String, boolean, boolean)}
     */
    @Deprecated
    void commit(DevMachine devMachine,
                ProjectConfig project,
                String message,
                boolean all,
                boolean amend,
                AsyncRequestCallback<Revision> callback);

    /**
     * Performs commit changes from index to repository. The result of the commit is represented by {@link Revision}, which is returned by
     * callback in <code>onSuccess(Revision result)</code>. Sends request over WebSocket.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param message
     *         commit log message
     * @param all
     *         automatically stage files that have been modified and deleted
     * @param amend
     *         indicates that previous commit must be overwritten
     * @throws WebSocketException
     */
    Promise<Revision> commit(DevMachine devMachine, Path project, String message, boolean all, boolean amend);

    /**
     * Performs commit for the given files (ignoring git index).
     *
     * @param devMachine
     *         current machine
     * @param projectConfig
     *         project (root of GIT repository)
     * @param message
     *         commit log message
     * @param files
     *         the list of iles that are commited, ignoring the index
     * @param amend
     *         indicates that previous commit must be overwritten
     * @param callback
     *         callback
     * @throws WebSocketException
     * @deprecated use {@link #commit(DevMachine, Path, String, Path[], boolean)}
     */
    @Deprecated
    void commit(DevMachine devMachine,
                ProjectConfigDto projectConfig,
                String message,
                List<String> files,
                boolean amend,
                AsyncRequestCallback<Revision> callback);

    /**
     * Performs commit for the given files (ignoring git index).
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param message
     *         commit log message
     * @param files
     *         the list of iles that are commited, ignoring the index
     * @param amend
     *         indicates that previous commit must be overwritten
     * @throws WebSocketException
     */
    Promise<Revision> commit(DevMachine devMachine, Path project, String message, Path[] files, boolean amend);

    /**
     * Performs commit changes from index to repository.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param message
     *         commit log message
     * @param all
     *         automatically stage files that have been modified and deleted
     * @param files
     *         the list of files that are committed, ignoring the index
     * @param amend
     *         indicates that previous commit must be overwritten
     */
    Promise<Revision> commit(DevMachine devMachine, Path project, String message, boolean all, Path[] files, boolean amend);

    /**
     * Get repository options.
     *
     * @param devMachine
     *         current machine
     * @param projectConfig
     *         project (root of GIT repository)
     * @param requestedConfig
     *         list of config keys
     * @param callback
     *         callback for sending asynchronous response
     * @deprecated use {@link #config(DevMachine, Path, List)}
     */
    @Deprecated
    void config(DevMachine devMachine,
                ProjectConfigDto projectConfig,
                List<String> requestedConfig,
                AsyncRequestCallback<Map<String, String>> callback);

    /**
     * Get repository options.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param requestedConfig
     *         list of config keys
     */
    Promise<Map<String, String>> config(DevMachine devMachine, Path project, List<String> requestedConfig);

    /**
     * Compare two commits, get the diff for pointed file(s) or for the whole project in text format.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param fileFilter
     *         files for which to show changes
     * @param type
     *         type of diff format
     * @param noRenames
     *         don't show renamed files
     * @param renameLimit
     *         the limit of shown renamed files
     * @param commitA
     *         first commit to compare
     * @param commitB
     *         second commit to be compared
     * @deprecated use {@link #diff(DevMachine, Path, List, DiffType, boolean, int, String, String)}
     */
    @Deprecated
    void diff(DevMachine devMachine,
              ProjectConfigDto project,
              List<String> fileFilter,
              DiffType type,
              boolean noRenames,
              int renameLimit,
              String commitA,
              String commitB,
              AsyncRequestCallback<String> callback);

    /**
     * Compare two commits, get the diff for pointed file(s) or for the whole project in text format.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param fileFilter
     *         files for which to show changes
     * @param type
     *         type of diff format
     * @param noRenames
     *         don't show renamed files
     * @param renameLimit
     *         the limit of shown renamed files
     * @param commitA
     *         first commit to compare
     * @param commitB
     *         second commit to be compared
     */
    Promise<String> diff(DevMachine devMachine,
                         Path project,
                         List<String> fileFilter,
                         DiffType type,
                         boolean noRenames,
                         int renameLimit,
                         String commitA,
                         String commitB);

    /**
     * Compare commit with index or working tree (depends on {@code cached}), get the diff for pointed file(s) or for the whole project in
     * text format.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param fileFilter
     *         files for which to show changes
     * @param type
     *         type of diff format
     * @param noRenames
     *         don't show renamed files
     * @param renameLimit
     *         the limit of shown renamed files
     * @param commitA
     *         commit to compare
     * @param cached
     *         if <code>true</code> then compare commit with index, if <code>false</code>, then compare with working tree.
     * @deprecated use {@link #diff(DevMachine, Path, List, DiffType, boolean, int, String, boolean)}
     */
    @Deprecated
    void diff(DevMachine devMachine,
              ProjectConfigDto project,
              List<String> fileFilter,
              DiffType type,
              boolean noRenames,
              int renameLimit,
              String commitA,
              boolean cached,
              AsyncRequestCallback<String> callback);

    /**
     * Compare commit with index or working tree (depends on {@code cached}), get the diff for pointed file(s) or for the whole project in
     * text format.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param files
     *         files for which to show changes
     * @param type
     *         type of diff format
     * @param noRenames
     *         don't show renamed files
     * @param renameLimit
     *         the limit of shown renamed files
     * @param commitA
     *         commit to compare
     * @param cached
     *         if <code>true</code> then compare commit with index, if <code>false</code>, then compare with working tree.
     */
    Promise<String> diff(DevMachine devMachine,
                         Path project,
                         List<String> files,
                         DiffType type,
                         boolean noRenames,
                         int renameLimit,
                         String commitA,
                         boolean cached);

    /**
     * Get the file content from specified revision or branch.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project configuration of root GIT repository
     * @param file
     *         file name with its full path
     * @param version
     *         revision or branch where the showed file is present
     * @param callback
     *         callback for sending asynchronous response with file content
     * @deprecated use {@link #showFileContent(DevMachine, Path, Path, String)}
     */
    @Deprecated
    void showFileContent(DevMachine devMachine, ProjectConfigDto project, String file, String version,
                         AsyncRequestCallback<ShowFileContentResponse> callback);

    /**
     * Get the file content from specified revision or branch.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project configuration of root GIT repository
     * @param file
     *         file name with its full path
     * @param version
     *         revision or branch where the showed file is present
     */
    Promise<ShowFileContentResponse> showFileContent(DevMachine devMachine, Path project, Path file, String version);

    /**
     * Get log of commits. The result is the list of {@link Revision}, which is returned by callback in
     * <code>onSuccess(Revision result)</code>.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param fileFilter
     *         range of files to filter revisions list
     * @param isTextFormat
     *         if <code>true</code> the loq response will be in text format
     * @deprecated use {@link #log(DevMachine, Path, Path[], boolean)}
     */
    @Deprecated
    void log(DevMachine devMachine, ProjectConfigDto project, List<String> fileFilter, boolean isTextFormat,
             AsyncRequestCallback<LogResponse> callback);

    /**
     * Get log of commits.
     *
     * Method is deprecated. Use {@link #log(DevMachine, Path, Path[], int, int, boolean)} to pass
     * {@code skip} and {@code maxCount} parameters to limit the number of returning entries.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param fileFilter
     *         range of files to filter revisions list
     * @param plainText
     *         if <code>true</code> the loq response will be in text format
     */
    @Deprecated
    Promise<LogResponse> log(DevMachine devMachine, Path project, @Nullable Path[] fileFilter, boolean plainText);

    /**
     * Get log of commits.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param fileFilter
     *         range of files to filter revisions list
     * @param skip
     *          the number of commits that will be skipped
     * @param maxCount
     *          the number of commits that will be returned
     * @param plainText
     *         if <code>true</code> the loq response will be in text format
     */
    Promise<LogResponse> log(DevMachine devMachine, Path project, @Nullable Path[] fileFilter, int skip, int maxCount, boolean plainText);

    /**
     * Merge the pointed commit with current HEAD.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param commit
     *         commit's reference to merge with
     * @deprecated use {@link #merge(DevMachine, Path, String)}
     */
    @Deprecated
    void merge(DevMachine devMachine, ProjectConfigDto project, String commit, AsyncRequestCallback<MergeResult> callback);

    /**
     * Merge the pointed commit with current HEAD.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param commit
     *         commit's reference to merge with
     */
    Promise<MergeResult> merge(DevMachine devMachine, Path project, String commit);

    /**
     * Gets the working tree status. The status of added, modified or deleted files is shown is written in {@link String}. The format may
     * be
     * long, short or porcelain. Example of detailed format:<br>
     * <p/>
     * <p/>
     * <pre>
     * # Untracked files:
     * #
     * # file.html
     * # folder
     * </pre>
     * <p/>
     * Example of short format:
     * <p/>
     * <p/>
     * <pre>
     * M  pom.xml
     * A  folder/test.html
     * D  123.txt
     * ?? folder/test.css
     * </pre>
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param format
     *         to show in short format or not
     * @deprecated use {@link #statusText(DevMachine, Path, StatusFormat)}
     */
    @Deprecated
    void statusText(DevMachine devMachine, ProjectConfigDto project, StatusFormat format, AsyncRequestCallback<String> callback);

    /**
     * Gets the working tree status. The status of added, modified or deleted files is shown is written in {@link String}. The format may
     * be
     * long, short or porcelain. Example of detailed format:<br>
     * <p/>
     * <p/>
     * <pre>
     * # Untracked files:
     * #
     * # file.html
     * # folder
     * </pre>
     * <p/>
     * Example of short format:
     * <p/>
     * <p/>
     * <pre>
     * M  pom.xml
     * A  folder/test.html
     * D  123.txt
     * ?? folder/test.css
     * </pre>
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @param format
     *         to show in short format or not
     */
    Promise<String> statusText(DevMachine devMachine, Path project, StatusFormat format);

    /**
     * Gets the working tree status : list of untracked, changed not commited and changed not updated.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         project (root of GIT repository)
     * @deprecated use {@link #getStatus(DevMachine, Path)}
     */
    @Deprecated
    void status(DevMachine devMachine, ProjectConfigDto project, AsyncRequestCallback<Status> callback);

    /**
     * Returns the current working tree status.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         the project.
     * @return the promise which either resolves working tree status or rejects with an error
     */
    Promise<Status> getStatus(DevMachine devMachine, Path project);

    /**
     * Remove the git repository from given path.
     *
     * @param devMachine
     *         current machine
     * @param project
     *         the project path
     * @return the promise with success status
     */
    Promise<Void> deleteRepository(DevMachine devMachine, Path project);

    @Deprecated
    void getCommitters(DevMachine devMachine, ProjectConfigDto project, AsyncRequestCallback<Commiters> callback);

    @Deprecated
    void deleteRepository(DevMachine devMachine, ProjectConfigDto project, AsyncRequestCallback<Void> callback);


    @Deprecated
    void getUrlVendorInfo(DevMachine devMachine, String vcsUrl, AsyncRequestCallback<GitUrlVendorInfo> callback);
}
