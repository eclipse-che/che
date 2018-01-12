/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.history;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link HistoryPresenter}.
 *
 * @author Igor Vinokur
 */
public interface HistoryView extends View<HistoryView.ActionDelegate> {
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

    /** Occurs when the last entry in the list has been displayed. */
    void onScrolledToButton();
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
   * @param description description message
   */
  void setDescription(String description);

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();
}
