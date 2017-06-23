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
package org.eclipse.che.ide.ext.git.client;

import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.DiffType;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.api.promises.client.Promise;
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
     * @deprecated use {@link #add(Path, boolean, Path[])}
     */
    void add(ProjectConfig projectConfig,
             boolean update,
             List<String> filePattern,
             RequestCallback<Void> callback) throws WebSocketException;

    /**
     * Add changes to Git index (temporary storage). Sends request over WebSocket.
     *
     * @param project
     *         project (root of GIT repository)
     * @param update
     *         if <code>true</code> then never stage new files, but stage modified new contents of tracked files and remove files from
     *         the index if the corresponding files in the working tree have been removed
     * @param paths
     *         pattern of the files to be added, default is "." (all files are added)
     * @throws WebSocketException
     */
    Promise<Void> add(Path project, boolean update, Path[] paths);

    /**
     * Fetch changes from remote repository to local one (sends request over WebSocket).
     *
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
    Promise<Void> fetch(Path project, String remote, List<String> refspec, boolean removeDeletedRefs);

    /**
     * Get the list of the branches. For now, all branches cannot be returned at once, so the parameter <code>remote</code> tells to get
     * remote branches if <code>true</code> or local ones (if <code>false</code>).
     *
     * @param project
     *         project (root of GIT repository)
     * @param mode
     *         get remote branches
     * @param callback
     * @deprecated use {@link #branchList(Path, BranchListMode)}
     */
    @Deprecated
    void branchList(ProjectConfig project,
                    @Nullable BranchListMode mode,
                    AsyncRequestCallback<List<Branch>> callback);

    /**
     * Get the list of the branches. For now, all branches cannot be returned at once, so the parameter <code>remote</code> tells to get
     * remote branches if <code>true</code> or local ones (if <code>false</code>).
     *  @param project
     *         project (root of GIT repository)
     * @param mode
     */
    Promise<List<Branch>> branchList(Path project, BranchListMode mode);

    /**
     * Delete branch.
     *  @param project
     *         project (root of GIT repository)
     * @param name
     *         name of the branch to delete
     * @param force
 *         force if <code>true</code> delete branch {@code name} even if it is not fully merged
     */
    Promise<Void> branchDelete(Path project, String name, boolean force);

    /**
     * Checkout the branch with pointed name.
     *  @param project
     *         project (root of GIT repository)
     * @param oldName
     *         branch's current name
     * @param newName
     */
    Promise<Void> branchRename(Path project, String oldName, String newName);

    /**
     * Create new branch with pointed name.
     *  @param project
     *         project (root of GIT repository)
     * @param name
     *         new branch's name
     * @param startPoint
     */
    Promise<Branch> branchCreate(Path project, String name, String startPoint);

    /**
     * Checkout the branch with pointed name.
     *
     * @param project
     *         project (root of GIT repository)
     * @param checkoutRequest
     *         checkout request
     * @deprecated {@link #checkout(Path, CheckoutRequest)}
     */
    @Deprecated
    void checkout(ProjectConfig project,
                  CheckoutRequest checkoutRequest,
                  AsyncRequestCallback<String> callback);

    /**
     * Checkout the branch with pointed name.
     *  @param project
     *         project (root of GIT repository)
     * @param request
     */
    Promise<Void> checkout(Path project, CheckoutRequest request);

    /**
     * Get the list of remote repositories for pointed by {@code projectConfig} parameter one.
     *
     * @param projectConfig
     *         project (root of GIT repository)
     * @param remoteName
     *         remote repository's name. Can be null in case when it is need to fetch all {@link Remote}
     * @param verbose
     *         If <code>true</code> show remote url and name otherwise show remote name
     * @return a promise that provides list {@link Remote} repositories for the {@code workspaceId}, {@code projectConfig},
     * {@code remoteName}, {@code verbose} or rejects with an error.
     * @deprecated use {@link #remoteList(Path, String, boolean)}
     */
    @Deprecated
    Promise<List<Remote>> remoteList(ProjectConfig projectConfig, @Nullable String remoteName, boolean verbose);

    /**
     * Get the list of remote repositories for pointed by {@code projectConfig} parameter one.
     *
     * @param project
     *         project (root of GIT repository)
     * @param remote
     *         remote repository's name. Can be null in case when it is need to fetch all {@link Remote}
     * @param verbose
     *         If <code>true</code> show remote url and name otherwise show remote name
     * @return a promise that provides list {@link Remote} repositories for the {@code workspaceId}, {@code projectConfig},
     * {@code remoteName}, {@code verbose} or rejects with an error.
     * @deprecated use {@link #remoteList(Path, String, boolean)}
     */
    Promise<List<Remote>> remoteList(Path project, String remote, boolean verbose);

    /**
     * Adds remote repository to the list of remote repositories.
     *
     * @param project
     *         project (root of GIT repository)
     * @param name
     *         remote repository's name
     * @param url
     *         remote repository's URL
     * @deprecated use {@link #remoteAdd(Path, String, String)}
     */
    @Deprecated
    void remoteAdd(ProjectConfig project,
                   String name,
                   String url,
                   AsyncRequestCallback<String> callback);

    /**
     * Adds remote repository to the list of remote repositories.
     *  @param project
     *         project (root of GIT repository)
     * @param name
     *         remote repository's name
     * @param url
     */
    Promise<Void> remoteAdd(Path project, String name, String url);

    /**
     * Deletes the pointed(by name) remote repository from the list of repositories.
     *
     * @param project
     *         project (root of GIT repository)
     * @param name
     *         remote repository name to delete
     * @deprecated use {@link #remoteDelete(Path, String)}
     */
    @Deprecated
    void remoteDelete(ProjectConfig project,
                      String name,
                      AsyncRequestCallback<String> callback);

    /**
     * Deletes the pointed(by name) remote repository from the list of repositories.
     *  @param project
     *         project (root of GIT repository)
     * @param name
     */
    Promise<Void> remoteDelete(Path project, String name);

    /**
     * Remove items from the working tree and the index.
     *  @param project
     *         project (root of GIT repository)
     * @param items
     *         items to remove
     * @param cached
     */
    Promise<Void> remove(Path project, Path[] items, boolean cached);

    /**
     * Reset current HEAD to the specified state. There two types of the reset: <br>
     * 1. Reset files in index - content of files is untouched. Typically it is useful to remove from index mistakenly added files.<br>
     * <code>git reset [paths]</code> is the opposite of <code>git add [paths]</code>. 2. Reset the current branch head to [commit] and
     * possibly updates the index (resetting it to the tree of [commit]) and the working tree depending on [mode].
     *  @param project
     *         project (root of GIT repository)
     * @param commit
     *         commit to which current head should be reset
     * @param resetType
 *         type of the reset
     * @param files
*         pattern of the files to reset the index. If <code>null</code> then reset the current branch head to [commit],
     */
    Promise<Void> reset(Path project, String commit, ResetRequest.ResetType resetType, Path[] files);

    /**
     * Initializes new Git repository (over WebSocket).
     *  @param project
     *         project (root of GIT repository)
     * @param bare
     */
    Promise<Void> init(Path project, boolean bare);

    /**
     * Pull (fetch and merge) changes from remote repository to local one (sends request over WebSocket).
     *  @param project
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
     */
    Promise<PullResponse> pull(Path project, String refSpec, String remote);

    /**
     * Push changes from local repository to remote one (sends request over WebSocket).
     *
     * @param project
     *         project
     * @param refSpec
     *         list of refspec to push
     * @param remote
     *         remote repository name or url
     * @param force
     *         push refuses to update a remote ref that is not an ancestor of the local ref used to overwrite it. If <code>true</code>
     *         disables the check. This can cause the remote repository to lose commits
     * @deprecated use {@link #push(Path, List, String, boolean)}
     */
    @Deprecated
    Promise<PushResponse> push(ProjectConfig project,
                               List<String> refSpec,
                               String remote,
                               boolean force);

    /**
     * Push changes from local repository to remote one (sends request over WebSocket).
     *  @param project
     *         project
     * @param refSpec
     *         list of refspec to push
     * @param remote
 *         remote repository name or url
     * @param force
*         push refuses to update a remote ref that is not an ancestor of the local ref used to overwrite it. If <code>true</code>
     */
    Promise<PushResponse> push(Path project, List<String> refSpec, String remote, boolean force);

    /**
     * Performs commit changes from index to repository. The result of the commit is represented by {@link Revision}, which is returned by
     * callback in <code>onSuccess(Revision result)</code>. Sends request over WebSocket.
     *
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
     * @deprecated use {@link #commit(Path, String, boolean, boolean)}
     */
    @Deprecated
    void commit(ProjectConfig project,
                String message,
                boolean all,
                boolean amend,
                AsyncRequestCallback<Revision> callback);

    /**
     * Performs commit changes from index to repository. The result of the commit is represented by {@link Revision}, which is returned by
     * callback in <code>onSuccess(Revision result)</code>. Sends request over WebSocket.
     *
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
    Promise<Revision> commit(Path project, String message, boolean all, boolean amend);

    /**
     * Performs commit for the given files (ignoring git index).
     *
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
    Promise<Revision> commit(Path project, String message, Path[] files, boolean amend);

    /**
     * Performs commit changes from index to repository.
     *  @param project
     *         project (root of GIT repository)
     * @param message
     *         commit log message
     * @param all
 *         automatically stage files that have been modified and deleted
     * @param files
*         the list of files that are committed, ignoring the index
     * @param amend
     */
    Promise<Revision> commit(Path project, String message, boolean all, Path[] files, boolean amend);

    /**
     * Get repository options.
     *  @param project
     *         project (root of GIT repository)
     * @param requestedConfig
     */
    Promise<Map<String, String>> config(Path project, List<String> requestedConfig);

    /**
     * Compare two commits, get the diff for pointed file(s) or for the whole project in text format.
     *  @param project
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
     */
    Promise<String> diff(Path project,
                         List<String> fileFilter,
                         DiffType type,
                         boolean noRenames,
                         int renameLimit,
                         String commitA,
                         String commitB);

    /**
     * Compare commit with index or working tree (depends on {@code cached}), get the diff for pointed file(s) or for the whole project in
     * text format.
     *  @param project
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
     */
    Promise<String> diff(Path project,
                         List<String> files,
                         DiffType type,
                         boolean noRenames,
                         int renameLimit,
                         String commitA,
                         boolean cached);

    /**
     * Get the file content from specified revision or branch.
     *  @param project
     *         project configuration of root GIT repository
     * @param file
     *         file name with its full path
     * @param version
     */
    Promise<ShowFileContentResponse> showFileContent(Path project, Path file, String version);

    /**
     * Get log of commits.
     *
     * Method is deprecated. Use {@link #log(Path, Path[], int, int, boolean)} to pass
     * {@code skip} and {@code maxCount} parameters to limit the number of returning entries.
     *  @param project
     *         project (root of GIT repository)
     * @param fileFilter
     *         range of files to filter revisions list
     * @param plainText
     */
    @Deprecated
    Promise<LogResponse> log(Path project, @Nullable Path[] fileFilter, boolean plainText);

    /**
     * Get log of commits.
     *  @param project
     *         project (root of GIT repository)
     * @param fileFilter
     *         range of files to filter revisions list
     * @param skip
 *          the number of commits that will be skipped
     * @param maxCount
*          the number of commits that will be returned
     * @param plainText
     */
    Promise<LogResponse> log(Path project, @Nullable Path[] fileFilter, int skip, int maxCount, boolean plainText);

    /**
     * Merge the pointed commit with current HEAD.
     *  @param project
     *         project (root of GIT repository)
     * @param commit
     */
    Promise<MergeResult> merge(Path project, String commit);

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
     *  @param project
     *         project (root of GIT repository)
     * @param format
     */
    Promise<String> statusText(Path project, StatusFormat format);

    /**
     * Returns the current working tree status.
     *
     * @param project
     *         the project.
     * @return the promise which either resolves working tree status or rejects with an error
     */
    Promise<Status> getStatus(Path project);

    /**
     * Remove the git repository from given path.
     *
     * @param project
     *         the project path
     * @return the promise with success status
     */
    Promise<Void> deleteRepository(Path project);
}
