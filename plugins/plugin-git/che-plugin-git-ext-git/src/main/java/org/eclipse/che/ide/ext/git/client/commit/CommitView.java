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
package org.eclipse.che.ide.ext.git.client.commit;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;

/**
 * The view of {@link CommitPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface CommitView extends View<CommitView.ActionDelegate> {
  /** Needs for delegate some function into Commit view. */
  interface ActionDelegate {
    /**
     * Performs any actions appropriate in response to the user having pressed the Commit button.
     */
    void onCommitClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();

    /** Performs any actions appropriate in response to the user having changed something. */
    void onValueChanged();

    /** Set the commit message for an amend commit. */
    void setAmendCommitMessage();
  }

  /** Returns entered commit message */
  @NotNull
  String getCommitMessage();

  /** Returns selected remote branch from branches drop-down list. */
  String getRemoteBranch();

  /**
   * Set content into message field.
   *
   * @param message text what need to insert
   */
  void setMessage(@NotNull String message);

  /** Set list of remote branches to drop-down. */
  void setRemoteBranchesList(List<Branch> branches);

  /**
   * Returns <code>true</code> if need to amend the last commit, and <code>false</code> otherwise
   */
  boolean isAmend();

  /** Set checked or unchecked the 'Amend' checkbox. */
  void setValueToAmendCheckBox(boolean value);

  /** Set checked or unchecked the 'Push after commit' checkbox. */
  void setValueToPushAfterCommitCheckBox(boolean value);

  /**
   * Change the enable state of the 'Amend' check-box.
   *
   * @param enable <code>true</code> to enable the check-box, <code>false</code> to disable it
   */
  void setEnableAmendCheckBox(boolean enable);

  /**
   * Change the enable state of the 'Push after commit' check-box.
   *
   * @param enable <code>true</code> to enable the check-box, <code>false</code> to disable it
   */
  void setEnablePushAfterCommitCheckBox(boolean enable);

  /**
   * Change the enable state of the 'Remote branches' drop-down list.
   *
   * @param enable <code>true</code> to enable the drop-down list, <code>false</code> to disable it
   */
  void setEnableRemoteBranchesDropDownLis(boolean enable);

  /** Returns <code>true</code> if need to push after commit, and <code>false</code> otherwise */
  boolean isPushAfterCommit();

  /**
   * Change the enable state of the commit button.
   *
   * @param enable <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnableCommitButton(boolean enable);

  /** Give focus to message field. */
  void focusInMessageField();

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();

  /** Initialize changed panel. */
  void setChangesPanelView(ChangesPanelView changesPanelView);
}
