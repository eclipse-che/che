/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.panel;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;

/** @author Mykola Morhun */
public interface GitPanelView extends View<GitPanelView.ActionDelegate> {

  /**
   * Adds new repository into the repositories list.
   *
   * @param repository new repository
   */
  void addRepository(String repository);

  /**
   * Removes given repository from panel.
   *
   * @param repository name of repository to remove from panel
   */
  void removeRepository(String repository);

  /**
   * Changes repository title in the git panel.
   *
   * @param oldRepositoryName name of repository before rename
   * @param newRepositoryName name of repository after rename
   */
  void renameRepository(String oldRepositoryName, String newRepositoryName);

  /**
   * Updates label with changes number of the specified repository.
   *
   * @param repository name of repository
   * @param changes number of changed files in the git repository
   */
  void updateRepositoryChanges(String repository, int changes);

  /**
   * Returns repository which is selected in the repositories list. If nothing selected null will be
   * returned.
   */
  @Nullable
  String getSelectedRepository();

  /** Embed changed files panel ui */
  void setChangesPanelView(ChangesPanelView changesPanelView);

  interface ActionDelegate extends BaseActionDelegate {

    /**
     * Invoked each time when user changes selection in repositories list. Passes null as argument
     * if nothing selected.
     */
    void onRepositorySelectionChanged(String selectedRepository);
  }
}
