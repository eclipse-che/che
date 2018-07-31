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
package org.eclipse.che.ide.ext.git.client.reset.commit;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link ResetToCommitPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface ResetToCommitView extends View<ResetToCommitView.ActionDelegate> {
  /** Needs for delegate some function into ResetToCommit view. */
  public interface ActionDelegate {
    /** Performs any actions appropriate in response to the user having pressed the Reset button. */
    void onResetClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();

    /**
     * Performs any action in response to the user having select revision.
     *
     * @param revision selected revision
     */
    void onRevisionSelected(@NotNull Revision revision);

    /** Occurs when the last entry in the list has been displayed. */
    void onScrolledToBottom();
  }

  /**
   * Set available revisions.
   *
   * @param revisions git revisions
   */
  void setRevisions(@NotNull List<Revision> revisions);

  /** Deselect active revision in the table of available revisions. */
  void resetRevisionSelection();

  /** @return <code>true</code> if soft mode is chosen, and <code>false</code> otherwise */
  boolean isSoftMode();

  /**
   * Select soft mode.
   *
   * @param isSoft <code>true</code> to select soft mode, <code>false</code> not to select
   */
  void setSoftMode(boolean isSoft);

  /** @return <code>true</code> if mix mode is chosen, and <code>false</code> otherwise */
  boolean isMixMode();

  /**
   * Select mix mode.
   *
   * @param isMix <code>true</code> to select mix mode, <code>false</code> not to select
   */
  void setMixMode(boolean isMix);

  /** @return <code>true</code> if hard mode is chosen, and <code>false</code> otherwise */
  boolean isHardMode();

  /**
   * Select mix mode.
   *
   * @param isHard <code>true</code> to select hard mode, <code>false</code> not to select
   */
  void setHardMode(boolean isHard);

  /**
   * IDEUI-166 No cursor in terminal
   *
   * <p>Change the enable state of the reset button.
   *
   * @param enabled <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnableResetButton(boolean enabled);

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();
}
