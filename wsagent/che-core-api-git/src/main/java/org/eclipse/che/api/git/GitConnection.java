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

import java.io.Closeable;
import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.git.exception.GitException;
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
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.EditedRegion;
import org.eclipse.che.api.git.shared.GitUser;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.RebaseResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.RemoteReference;
import org.eclipse.che.api.git.shared.RevertResult;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.Tag;

/**
 * Connection to Git repository.
 *
 * @author andrew00x
 * @author Igor Vinokur
 */
public interface GitConnection extends Closeable {
  File getWorkingDir();

  /**
   * Add content of working tree to Git index. This action prepares content to next commit.
   *
   * @param params add params
   * @throws GitException if any error occurs when add files to the index
   * @see AddParams
   */
  void add(AddParams params) throws GitException;

  /**
   * Checkout a branch / file to the working tree.
   *
   * @param params checkout params
   * @throws GitException if any error occurs when checkout
   * @see CheckoutParams
   */
  void checkout(CheckoutParams params) throws GitException;

  /**
   * Create new branch.
   *
   * @param name name of the branch to create
   * @param startPoint hash commit from which to start new branch. If <code>null</code> HEAD will be
   *     used
   * @return newly created branch
   * @throws GitException if any error occurs when creating branch
   */
  Branch branchCreate(String name, String startPoint) throws GitException;

  /**
   * Delete branch.
   *
   * @param name name of the branch to delete
   * @param force <code>true</code> if need to delete branch with force
   * @throws UnauthorizedException if it is not possible to delete remote branch with existing
   *     credentials
   * @throws GitException if any other error occurs
   */
  void branchDelete(String name, boolean force) throws GitException, UnauthorizedException;

  /**
   * Rename branch.
   *
   * @param oldName current name of branch
   * @param newName new name of branch
   * @throws UnauthorizedException if it is not possible to rename remote branch with existing
   *     credentials
   * @throws GitException if any other error occurs
   */
  void branchRename(String oldName, String newName) throws GitException, UnauthorizedException;

  /**
   * List branches.
   *
   * @param listMode specifies what branches to list. {@link BranchListMode#LIST_ALL} will be set if
   *     parameter is not specified
   * @throws GitException if any error occurs
   */
  List<Branch> branchList(BranchListMode listMode) throws GitException;

  /**
   * Show information about files in the index and the working tree.
   *
   * @param params list files params
   * @return list of files.
   * @throws GitException if any error occurs
   */
  List<String> listFiles(LsFilesParams params) throws GitException;

  /**
   * Clone repository.
   *
   * @param params clone params
   * @throws UnauthorizedException if it is not possible to clone with existing credentials
   * @throws GitException if any other error occurs
   * @see CloneParams
   */
  void clone(CloneParams params) throws URISyntaxException, ServerException, UnauthorizedException;

  /**
   * Perform clone with sparse-checkout to specified directory.
   *
   * @param directory path to keep in working tree
   * @param remoteUrl url to clone
   * @throws UnauthorizedException if it is not possible to clone with existing credentials
   * @throws GitException if any other error occurs
   */
  void cloneWithSparseCheckout(String directory, String remoteUrl)
      throws GitException, UnauthorizedException;

  /**
   * Commit current state of index in new commit.
   *
   * @param params commit params
   * @return new commit
   * @throws GitException if any error occurs
   * @see CommitParams
   */
  Revision commit(CommitParams params) throws GitException;

  /**
   * Show diff between commits.
   *
   * @param params diff params
   * @return diff page. Diff info can be serialized to stream by using method {@link
   *     DiffPage#writeTo(java.io.OutputStream)}
   * @throws GitException if any error occurs
   * @see DiffPage
   * @see DiffParams
   */
  DiffPage diff(DiffParams params) throws GitException;

  /**
   * Get list of edited regions (insertions, modifications, removals) of the file.
   *
   * @param file path of the file
   * @return list of {@link EditedRegion} objects that contain type and range of the editing
   * @throws GitException if any error occurs
   */
  List<EditedRegion> getEditedRegions(String file) throws GitException;

  /**
   * Show content of the file from specified revision or branch.
   *
   * @param file path of the file to show
   * @param version hash of revision or branch
   * @return response that contains content of the file
   * @throws GitException if any error occurs
   */
  ShowFileContentResponse showFileContent(String file, String version) throws GitException;

  /**
   * Fetch data from remote repository.
   *
   * @param params fetch params
   * @throws UnauthorizedException if it is not possible to fetch with existing credentials
   * @throws GitException if any other error occurs
   * @see FetchParams
   */
  void fetch(FetchParams params) throws UnauthorizedException, GitException;

  /**
   * Initialize new Git repository.
   *
   * @param bare <code>true</code> to create bare repository
   * @throws GitException if any error occurs
   */
  void init(boolean bare) throws GitException;

  /**
   * Check if directory, which was used to create Git connection, is inside the working tree.
   *
   * @return <b>true</b> if only directory is inside working tree, and <b>false</b> if directory is
   *     outside the working tree including directory inside .git directory, or bare repository.
   * @throws GitException if any error occurs
   */
  boolean isInsideWorkTree() throws GitException;

  /**
   * Get commit logs.
   *
   * @param params log params
   * @return log page. Logs can be serialized to stream by using method {@link
   *     DiffPage#writeTo(java.io.OutputStream)}
   * @throws GitException if any error occurs
   * @see LogParams
   */
  LogPage log(LogParams params) throws GitException;

  /**
   * List references in a remote repository.
   *
   * @param remoteUrl url of the remote repository
   * @return list references in a remote repository.
   * @throws UnauthorizedException if it is not possible to list references with existing
   *     credentials
   * @throws GitException if any other error occurs
   */
  List<RemoteReference> lsRemote(String remoteUrl) throws UnauthorizedException, GitException;

  /**
   * Merge commits.
   *
   * @param commit hash of commit to merge
   * @return result of merge
   * @throws GitException if any error occurs
   */
  MergeResult merge(String commit) throws GitException;

  /**
   * Rebase on a branch.
   *
   * @param operation rebase operation to use
   * @param branch rebase branch to use
   * @throws GitException if any error occurs when checkout
   */
  RebaseResponse rebase(String operation, String branch) throws GitException;

  /**
   * Move or rename file or directory.
   *
   * @param source file that will be moved or renamed
   * @param target new name or destination directory
   * @throws GitException if any error occurs
   */
  void mv(String source, String target) throws GitException;

  /**
   * Pull (fetch and merge at once) changes from remote repository to local branch.
   *
   * @param params pull params
   * @throws UnauthorizedException if it is not possible to pull with existing credentials
   * @throws GitException if any other error occurs
   * @see PullParams
   */
  PullResponse pull(PullParams params) throws GitException, UnauthorizedException;

  /**
   * Send changes from local repository to remote one.
   *
   * @param params push params
   * @throws UnauthorizedException if it is not possible to push with existing credentials
   * @throws GitException if any other error occurs
   * @see PushParams
   */
  PushResponse push(PushParams params) throws GitException, UnauthorizedException;

  /**
   * Add new remote configuration.
   *
   * @param params add remote configuration params
   * @throws GitException if any error occurs
   * @see RemoteAddParams
   */
  void remoteAdd(RemoteAddParams params) throws GitException;

  /**
   * Remove the remote named <code>name</code>. All remote tracking branches and configuration
   * settings for the remote are removed.
   *
   * @param name remote configuration to remove
   * @throws GitException if any error occurs
   */
  void remoteDelete(String name) throws GitException;

  /**
   * Show remotes.
   *
   * @param remoteName name of the remote
   * @param verbose if <code>true</code> show remote url and name, otherwise show remote name only
   * @throws GitException if any error occurs
   */
  List<Remote> remoteList(String remoteName, boolean verbose) throws GitException;

  /**
   * Update remote configuration.
   *
   * @param params update remote configuration params
   * @throws GitException if any error occurs
   * @see RemoteUpdateParams
   */
  void remoteUpdate(RemoteUpdateParams params) throws GitException;

  /**
   * Reset current HEAD to the specified state.
   *
   * @param params reset params
   * @throws GitException if any error occurs
   * @see ResetParams
   */
  void reset(ResetParams params) throws GitException;

  /**
   * Remove files.
   *
   * @param params remove params
   * @throws GitException if any error occurs
   * @see RmParams
   */
  void rm(RmParams params) throws GitException;

  /**
   * Get status.
   *
   * @param filter list of paths to filter the status. Status result will include only files witch
   *     paths are contained in the filter list, or are children of the folder paths that are
   *     mentioned in the filter list. Unfiltered status of working tree will be returned, if the
   *     filter list is empty
   * @return status.
   * @throws GitException if any error occurs
   */
  Status status(List<String> filter) throws GitException;

  /**
   * Create new tag.
   *
   * @param params tag create params
   * @throws GitException if any error occurs
   * @see TagCreateParams
   */
  Tag tagCreate(TagCreateParams params) throws GitException;

  /**
   * @param name name of the tag to delete
   * @throws GitException if any error occurs
   */
  void tagDelete(String name) throws GitException;

  /**
   * Returns list of available tags.
   *
   * @param pattern tag's names pattern
   * @return list of tags matched to request
   * @throws GitException if any error occurs
   */
  List<Tag> tagList(String pattern) throws GitException;

  /**
   * Gel list of commiters in current repository.
   *
   * @return list of commiters
   * @throws GitException
   */
  List<GitUser> getCommiters() throws GitException;

  /** Get configuration. */
  Config getConfig() throws GitException;

  /** Close connection, release associated resources. */
  @Override
  void close();

  /** Set publisher for git output, e.g. for sending git command output to the client side. */
  void setOutputLineConsumerFactory(LineConsumerFactory outputPublisherFactory);

  /**
   * Get the current branch on the current directory
   *
   * @deprecated Use {@link #getCurrentReference()} instead.
   * @return the name of the branch or <i>HEAD</i> if the repo points to tag or commit
   * @throws GitException if any exception occurs
   */
  String getCurrentBranch() throws GitException;

  /**
   * Get the current reference on the current directory
   *
   * @return reference object with branch, tag or commit id
   * @throws GitException if any exception occurs
   */
  Reference getCurrentReference() throws GitException;

  /**
   * Revert the specified commit
   *
   * @param commit the commit to revert
   * @throws GitException if any error occurs
   */
  RevertResult revert(String commit) throws GitException;
}
