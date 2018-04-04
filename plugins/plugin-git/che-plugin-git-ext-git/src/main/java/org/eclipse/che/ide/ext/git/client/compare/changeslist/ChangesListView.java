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
package org.eclipse.che.ide.ext.git.client.compare.changeslist;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;

/**
 * The view of {@link ChangesListPresenter}.
 *
 * @author Igor Vinokur
 */
public interface ChangesListView extends View<ChangesListView.ActionDelegate> {
  /** Needs for delegate some function into Changed list view. */
  interface ActionDelegate {
    /**
     * Performs any actions appropriate in response to the user having pressed the 'Close' button.
     */
    void onCloseClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the 'Compare' button.
     */
    void onCompareClicked();
  }

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();

  /**
   * Change the enable state of the compare button.
   *
   * @param enabled <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnableCompareButton(boolean enabled);

  /** Initialize changes panel. */
  void setChangesPanelView(ChangesPanelView changesPanelView);
}
