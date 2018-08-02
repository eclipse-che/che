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
package org.eclipse.che.plugin.pullrequest.client.vcs;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.promises.client.Promise;

/** Service for VCS operations. */
public interface VcsService {

  /**
   * Add a remote to the project VCS metadata.
   *
   * @param project the project descriptor.
   * @param remote the remote name.
   * @param remoteUrl the remote URL.
   * @param callback callback when the operation is done.
   */
  void addRemote(
      @NotNull ProjectConfig project,
      @NotNull String remote,
      @NotNull String remoteUrl,
      @NotNull AsyncCallback<Void> callback);

  /**
   * Checkout a branch of the given project.
   *
   * @param project the project descriptor.
   * @param branchName the name of the branch to checkout.
   * @param createNew create a new branch if {@code true}.
   * @param callback callback when the operation is done.
   */
  void checkoutBranch(
      @NotNull ProjectConfig project,
      @NotNull String branchName,
      boolean createNew,
      @NotNull AsyncCallback<String> callback);

  /**
   * Commits the current changes of the given project.
   *
   * @param project the project descriptor.
   * @param includeUntracked {@code true} to include untracked files, {@code false} otherwise.
   * @param commitMessage the commit message.
   * @param callback callback when the operation is done.
   */
  void commit(
      @NotNull ProjectConfig project,
      boolean includeUntracked,
      @NotNull String commitMessage,
      @NotNull AsyncCallback<Void> callback);

  /**
   * Removes a remote to the project VCS metadata.
   *
   * @param project the project descriptor.
   * @param remote the remote name.
   * @param callback callback when the operation is done.
   */
  void deleteRemote(
      @NotNull ProjectConfig project,
      @NotNull String remote,
      @NotNull AsyncCallback<Void> callback);

  /**
   * Returns the name of the current branch for the given {@code project}.
   *
   * @param project the project.
   * @return the promise that resolves branch name or rejects with an error
   */
  Promise<String> getBranchName(ProjectConfig project);

  /**
   * Returns if the given project has uncommitted changes.
   *
   * @param project the project descriptor.
   * @param callback what to do if the project has uncommitted changes.
   */
  void hasUncommittedChanges(
      @NotNull ProjectConfig project, @NotNull AsyncCallback<Boolean> callback);

  /**
   * Returns if a local branch with the given name exists in the given project.
   *
   * @param project the project descriptor.
   * @param branchName the branch name.
   * @param callback callback called when operation is done.
   */
  void isLocalBranchWithName(
      @NotNull ProjectConfig project,
      @NotNull String branchName,
      @NotNull AsyncCallback<Boolean> callback);

  /**
   * List the local branches.
   *
   * @param project the project descriptor.
   * @param callback what to do with the branches list.
   */
  void listLocalBranches(
      @NotNull ProjectConfig project, @NotNull AsyncCallback<List<Branch>> callback);

  /**
   * Returns the list of the remotes for given {@code project}.
   *
   * @param project the project
   * @return the promise which resolves {@literal List<Remote>} or rejects with an error
   */
  Promise<List<Remote>> listRemotes(ProjectConfig project);

  /**
   * Push a local branch to remote.
   *
   * @param project the project descriptor.
   * @param remote the remote name
   * @param localBranchName the local branch name
   */
  Promise<PushResponse> pushBranch(ProjectConfig project, String remote, String localBranchName);
}
