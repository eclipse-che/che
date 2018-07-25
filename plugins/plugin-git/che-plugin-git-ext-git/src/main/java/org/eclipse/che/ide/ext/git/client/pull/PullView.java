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
package org.eclipse.che.ide.ext.git.client.pull;

import java.util.List;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link PullPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface PullView extends View<PullView.ActionDelegate> {
  /** Needs for delegate some function into Pull view. */
  interface ActionDelegate {
    /** Performs any actions appropriate in response to the user having pressed the Pull button. */
    void onPullClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();

    /** Performs any actions appropriate in response to the remote branch value changed. */
    void onRemoteBranchChanged();

    /** Performs any actions appropriate in response to the repository value changed. */
    void onRemoteRepositoryChanged();
  }

  /**
   * Returns selected repository name.
   *
   * @return repository name.
   */
  String getRepositoryName();

  /**
   * Returns selected repository url.
   *
   * @return repository url.
   */
  String getRepositoryUrl();

  /**
   * Sets available repositories.
   *
   * @param repositories available repositories
   */
  void setRepositories(List<Remote> repositories);

  /** @return local branch */
  String getLocalBranch();

  /**
   * Selects pointed local branch
   *
   * @param branch local branch to select
   */
  void selectLocalBranch(String branch);

  /**
   * Selects pointed remote branch
   *
   * @param branch remote branch to select
   */
  void selectRemoteBranch(String branch);

  /**
   * Set local branches into view.
   *
   * @param branches local branches
   */
  void setLocalBranches(List<String> branches);

  /** @return remote branches */
  String getRemoteBranch();

  /**
   * Set remote branches into view.
   *
   * @param branches remote branches
   */
  void setRemoteBranches(List<String> branches);

  /** Returns the value of 'pull with rebase' flag. */
  boolean getRebase();

  /**
   * Change the enable state of the push button.
   *
   * @param enabled <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnablePullButton(boolean enabled);

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();
}
