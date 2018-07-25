/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.DiffType;
import org.eclipse.che.api.git.shared.EditedRegion;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.RevertResult;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.auth.Credentials;
import org.eclipse.che.ide.resource.Path;

/**
 * Service contains methods for working with Git repository from client side.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 * @author Igor Vinokur
 */
public interface GitServiceClient {

  /**
   * Add changes to Git index (temporary storage). Sends request over WebSocket.
   *
   * @param project project (root of GIT repository)
   * @param update if <code>true</code> then never stage new files, but stage modified new contents
   *     of tracked files and remove files from the index if the corresponding files in the working
   *     tree have been removed
   * @param paths pattern of the files to be added, default is "." (all files are added)
   */
  Promise<Void> add(Path project, boolean update, Path[] paths);

  /**
   * Fetch changes from remote repository to local one (sends request over WebSocket).
   *
   * @param project project root of GIT repository
   * @param remote remote repository's name
   * @param refspec list of refspec to fetch.
   *     <p>Expected form is:
   *     <ul>
   *       <li>refs/heads/featured:refs/remotes/origin/featured - branch 'featured' from remote
   *           repository will be fetched to 'refs/remotes/origin/featured'.
   *       <li>featured - remote branch name.
   *     </ul>
   *
   * @param removeDeletedRefs if <code>true</code> then delete removed refs from local repository
   * @param credentials credentials to perform vcs authorization
   */
  Promise<Void> fetch(
      Path project,
      String remote,
      List<String> refspec,
      boolean removeDeletedRefs,
      Credentials credentials);

  /**
   * Get the list of the branches. For now, all branches cannot be returned at once, so the
   * parameter <code>remote</code> tells to get remote branches if <code>true</code> or local ones
   * (if <code>false</code>).
   *
   * @param project project (root of GIT repository)
   * @param mode get remote branches
   */
  Promise<List<Branch>> branchList(Path project, BranchListMode mode);

  /**
   * Delete branch.
   *
   * @param project project (root of GIT repository)
   * @param name name of the branch to delete
   * @param force force if <code>true</code> delete branch {@code name} even if it is not fully
   *     merged
   */
  Promise<Void> branchDelete(Path project, String name, boolean force);

  /**
   * Checkout the branch with pointed name.
   *
   * @param project project (root of GIT repository)
   * @param oldName branch's current name
   * @param newName branch's new name
   */
  Promise<Void> branchRename(Path project, String oldName, String newName);

  /**
   * Create new branch with pointed name.
   *
   * @param project project (root of GIT repository)
   * @param name new branch's name
   * @param startPoint name of a commit at which to start the new branch
   */
  Promise<Branch> branchCreate(Path project, String name, String startPoint);

  /**
   * Checkout the branch with pointed name.
   *
   * @param project project (root of GIT repository)
   * @param request checkout request
   */
  Promise<String> checkout(Path project, CheckoutRequest request);

  /**
   * Get the list of remote repositories for pointed by {@code projectConfig} parameter one.
   *
   * @param project project (root of GIT repository)
   * @param remote remote repository's name. Can be null in case when it is need to fetch all {@link
   *     Remote}
   * @param verbose If <code>true</code> show remote url and name otherwise show remote name
   * @return a promise that provides list {@link Remote} repositories for the {@code workspaceId},
   *     {@code projectConfig}, {@code remoteName}, {@code verbose} or rejects with an error.
   */
  Promise<List<Remote>> remoteList(Path project, String remote, boolean verbose);

  /**
   * Adds remote repository to the list of remote repositories.
   *
   * @param project project (root of GIT repository)
   * @param name remote repository's name
   * @param url remote repository's URL
   */
  Promise<Void> remoteAdd(Path project, String name, String url);

  /**
   * Deletes the pointed(by name) remote repository from the list of repositories.
   *
   * @param project project (root of GIT repository)
   * @param name remote repository name to delete
   */
  Promise<Void> remoteDelete(Path project, String name);

  /**
   * Remove items from the working tree and the index.
   *
   * @param project project (root of GIT repository)
   * @param items items to remove
   * @param cached is for removal only from index
   */
  Promise<Void> remove(Path project, Path[] items, boolean cached);

  /**
   * Reset current HEAD to the specified state. There two types of the reset: <br>
   * 1. Reset files in index - content of files is untouched. Typically it is useful to remove from
   * index mistakenly added files.<br>
   * <code>git reset [paths]</code> is the opposite of <code>git add [paths]</code>. 2. Reset the
   * current branch head to [commit] and possibly updates the index (resetting it to the tree of
   * [commit]) and the working tree depending on [mode].
   *
   * @param project project (root of GIT repository)
   * @param commit commit to which current head should be reset
   * @param resetType type of the reset
   * @param files pattern of the files to reset the index. If <code>null</code> then reset the
   *     current branch head to [commit], else reset received files in index.
   */
  Promise<Void> reset(Path project, String commit, ResetRequest.ResetType resetType, Path[] files);

  /**
   * Initializes new Git repository (over WebSocket).
   *
   * @param project project (root of GIT repository)
   * @param bare to create bare repository or not
   */
  Promise<Void> init(Path project, boolean bare);

  /**
   * Pull (fetch and merge) changes from remote repository to local one (sends request over
   * WebSocket).
   *
   * @param project project (root of GIT repository)
   * @param refSpec list of refspec to fetch.
   *     <p>Expected form is:
   *     <ul>
   *       <li>refs/heads/featured:refs/remotes/origin/featured - branch 'featured' from remote
   *           repository will be fetched to 'refs/remotes/origin/featured'.
   *       <li>featured - remote branch name.
   *     </ul>
   *
   * @param remote remote remote repository's name
   * @param rebase use rebase instead of merge
   * @param credentials credentials to perform vcs authorization
   */
  Promise<PullResponse> pull(
      Path project, String refSpec, String remote, boolean rebase, Credentials credentials);

  /**
   * Push changes from local repository to remote one (sends request over WebSocket).
   *
   * @param project project
   * @param refSpec list of refspec to push
   * @param remote remote repository name or url
   * @param force push refuses to update a remote ref that is not an ancestor of the local ref used
   *     to overwrite it. If <code>true</code> disables the check. This can cause the remote
   *     repository to lose commits
   * @param credentials credentials to perform vcs authorization
   */
  Promise<PushResponse> push(
      Path project, List<String> refSpec, String remote, boolean force, Credentials credentials);

  /**
   * Clones one remote repository to local one (over WebSocket).
   *
   * @param project project (root of GIT repository)
   * @param remoteUri the location of the remote repository
   * @param remoteName remote name instead of "origin"
   */
  Promise<Void> clone(Path project, String remoteUri, String remoteName);

  /**
   * Performs commit changes from index to repository. The result of the commit is represented by
   * {@link Revision}, which is returned by callback in <code>onSuccess(Revision result)</code>.
   * Sends request over WebSocket.
   *
   * @param project project (root of GIT repository)
   * @param message commit log message
   * @param all automatically stage files that have been modified and deleted
   * @param amend indicates that previous commit must be overwritten
   */
  Promise<Revision> commit(Path project, String message, boolean all, boolean amend);

  /**
   * Performs commit changes from index to repository.
   *
   * @param project project (root of GIT repository)
   * @param message commit log message
   * @param amend indicates that previous commit must be overwritten
   * @param files the list of files that are committed, ignoring the index
   */
  Promise<Revision> commit(Path project, String message, boolean amend, Path[] files);

  /**
   * Get repository options.
   *
   * @param project project (root of GIT repository)
   * @param requestedConfig list of config keys
   */
  Promise<Map<String, String>> config(Path project, List<String> requestedConfig);

  /**
   * Compare two commits, get the diff for pointed file(s) or for the whole project in text format.
   *
   * @param project project (root of GIT repository)
   * @param fileFilter files for which to show changes
   * @param type type of diff format
   * @param noRenames don't show renamed files
   * @param renameLimit the limit of shown renamed files
   * @param commitA first commit to compare
   * @param commitB second commit to be compared
   */
  Promise<String> diff(
      Path project,
      List<String> fileFilter,
      DiffType type,
      boolean noRenames,
      int renameLimit,
      String commitA,
      String commitB);

  /**
   * Compare commit with index or working tree (depends on {@code cached}), get the diff for pointed
   * file(s) or for the whole project in text format.
   *
   * @param project project (root of GIT repository)
   * @param files files for which to show changes
   * @param type type of diff format
   * @param noRenames don't show renamed files
   * @param renameLimit the limit of shown renamed files
   * @param commitA commit to compare
   * @param cached if <code>true</code> then compare commit with index, if <code>false</code>, then
   *     compare with working tree.
   */
  Promise<String> diff(
      Path project,
      List<String> files,
      DiffType type,
      boolean noRenames,
      int renameLimit,
      String commitA,
      boolean cached);

  /**
   * Get list of edited regions (insertions, modifications, removals) of the file.
   *
   * @param project project (root of GIT repository)
   * @param filePath path to the file
   */
  Promise<List<EditedRegion>> getEditedRegions(Path project, Path filePath);

  /**
   * Get the file content from specified revision or branch.
   *
   * @param project project configuration of root GIT repository
   * @param file file name with its full path
   * @param version revision or branch where the showed file is present
   */
  Promise<ShowFileContentResponse> showFileContent(Path project, Path file, String version);

  /**
   * Get log of commits.
   *
   * @param project project (root of GIT repository)
   * @param fileFilter range of files to filter revisions list
   * @param skip the number of commits that will be skipped
   * @param maxCount the number of commits that will be returned
   * @param plainText if <code>true</code> the loq response will be in text format
   */
  Promise<LogResponse> log(
      Path project, @Nullable Path[] fileFilter, int skip, int maxCount, boolean plainText);

  /**
   * Merge the pointed commit with current HEAD.
   *
   * @param project project (root of GIT repository)
   * @param commit commit's reference to merge with
   */
  Promise<MergeResult> merge(Path project, String commit);

  /**
   * Gets the working tree status. The status of added, modified or deleted files is shown is
   * written in {@link String}. The format may be long, short or porcelain. Example of detailed
   * format:<br>
   *
   * <p>
   *
   * <p>
   *
   * <pre>
   * # Untracked files:
   * #
   * # file.html
   * # folder
   * </pre>
   *
   * <p>Example of short format:
   *
   * <p>
   *
   * <p>
   *
   * <pre>
   * M  pom.xml
   * A  folder/test.html
   * D  123.txt
   * ?? folder/test.css
   * </pre>
   *
   * @param project project (root of GIT repository)
   */
  Promise<String> statusText(Path project);

  /**
   * Returns the current status.
   *
   * @param project the project.
   * @param filter list of paths to filter the status. Status result will include only files witch
   *     paths are contained in the filter list, or are children of the folder paths that are
   *     mentioned in the filter list. Unfiltered status of working tree will be returned, if the
   *     filter list is empty
   * @return the promise which either resolves working tree status or rejects with an error
   */
  Promise<Status> getStatus(Path project, List<String> filter);

  /**
   * Remove the git repository from given path.
   *
   * @param project the project path
   * @return the promise with success status
   */
  Promise<Void> deleteRepository(Path project);

  /**
   * Revert the specified commit
   *
   * @param project project (root of GIT repository)
   * @param commit commit to revert
   */
  Promise<RevertResult> revert(Path project, String commit);
}
