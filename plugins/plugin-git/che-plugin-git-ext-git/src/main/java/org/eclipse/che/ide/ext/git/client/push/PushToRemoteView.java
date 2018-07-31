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
package org.eclipse.che.ide.ext.git.client.push;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link PushToRemotePresenter}.
 *
 * @author Andrey Plotnikov
 * @author Sergii Leschenko
 */
public interface PushToRemoteView extends View<PushToRemoteView.ActionDelegate> {
  /** Needs for delegate some function into PushToRemote view. */
  public interface ActionDelegate {
    /** Performs any actions appropriate in response to the user having pressed the Push button. */
    void onPushClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();

    /** Performs any actions appropriate in response to the local branch value changed. */
    void onLocalBranchChanged();

    /** Performs any actions appropriate in response to the repository value changed. */
    void onRepositoryChanged();
  }

  /**
   * Returns selected repository.
   *
   * @return repository.
   */
  @NotNull
  String getRepository();

  /**
   * Sets available repositories.
   *
   * @param repositories available repositories
   */
  void setRepositories(@NotNull List<Remote> repositories);

  /** @return local branch */
  @NotNull
  String getLocalBranch();

  /**
   * Set local branches into view.
   *
   * @param branches local branches
   */
  void setLocalBranches(@NotNull List<String> branches);

  /** @return remote branches */
  @NotNull
  String getRemoteBranch();

  /**
   * Set remote branches into view.
   *
   * @param branches remote branches
   */
  void setRemoteBranches(@NotNull List<String> branches);

  /**
   * Add remote branch into view.
   *
   * @param branch remote branch
   * @return {@code true} if branch added and {@code false} if branch already exist
   */
  boolean addRemoteBranch(@NotNull String branch);

  /**
   * Selects pointed local branch
   *
   * @param branch local branch to select
   */
  void selectLocalBranch(@NotNull String branch);

  /**
   * Selects pointed remote branch
   *
   * @param branch remote branch to select
   */
  void selectRemoteBranch(@NotNull String branch);

  /**
   * Change the enable state of the push button.
   *
   * @param enabled {@code true} to enable the button, {@code false} to disable it
   */
  void setEnablePushButton(boolean enabled);

  /** Set selected force push check-box. */
  void setSelectedForcePushCheckBox(boolean isSelected);

  /** Returns {@code true} if force push check-box is selected, otherwise returns {@code false}. */
  boolean isForcePushSelected();

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();
}
