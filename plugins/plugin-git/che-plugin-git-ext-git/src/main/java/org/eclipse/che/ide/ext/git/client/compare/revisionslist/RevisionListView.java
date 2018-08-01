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
package org.eclipse.che.ide.ext.git.client.compare.revisionslist;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link RevisionListPresenter}.
 *
 * @author Igor Vinokur
 */
public interface RevisionListView extends View<RevisionListView.ActionDelegate> {
  /** Needs for delegate some function into list of revisions view. */
  interface ActionDelegate {
    /** Performs any actions appropriate in response to the user having pressed the Close button. */
    void onCloseClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Compare button.
     */
    void onCompareClicked();

    /**
     * Performs any action in response to the user having select revision.
     *
     * @param revision selected revision
     */
    void onRevisionSelected(@NotNull Revision revision);

    /** Performs any action in response to the user do not have double-clicked any revision. */
    void onRevisionDoubleClicked();

    /** Performs any action in response to the user do not have any selected revision. */
    void onRevisionUnselected();
  }

  /**
   * Set available revisions.
   *
   * @param revisions git commits
   */
  void setRevisions(@NotNull List<Revision> revisions);

  /**
   * Change the enable state of the compare button.
   *
   * @param enabled <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnableCompareButton(boolean enabled);

  /**
   * Set message to description field.
   *
   * @param description message
   */
  void setDescription(String description);

  /** Close dialog. */
  void close();

  /**
   * Returns whether the view is shown.
   *
   * @return <code>true</code> if the view is shown, and <code>false</code> otherwise
   */

  /** Show dialog. */
  void showDialog();
}
