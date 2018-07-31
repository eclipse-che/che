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
package org.eclipse.che.ide.ext.git.client.compare.changespanel;

import java.util.Set;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.git.client.compare.AlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent.SelectionChangedHandler;
import org.eclipse.che.ide.ui.smartTree.presentation.PresentationRenderer;

/**
 * The view of {@link ChangesPanelPresenter}.
 *
 * @author Igor Vinokur
 */
public interface ChangesPanelView extends View<ChangesPanelView.ActionDelegate> {

  /** Needs for delegate some function into Changes list view. */
  interface ActionDelegate {

    /**
     * Performs any actions appropriate in response to the user having pressed the button that
     * changes view mode of changed files.
     */
    void onChangeViewModeButtonClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the 'Expand all
     * directories' button.
     */
    void onExpandButtonClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the 'Collapse all
     * directories' button.
     */
    void onCollapseButtonClicked();

    /** Performs any actions appropriate in response to the user double clicked on the file node. */
    void onFileNodeDoubleClicked(String file, Status status);
  }

  /** Add selection changed handler. */
  void addSelectionHandler(SelectionChangedHandler handler);

  void viewChangedFiles(AlteredFiles files, ViewMode viewMode);

  /** Clear panel from old nodes. */
  void resetPanelState();

  /** Expand all directories. */
  void expandAllDirectories();

  /** Collapse all directories. */
  void collapseAllDirectories();

  /**
   * Change the enable state of the 'Expand/Collapse all directories' buttons.
   *
   * @param enabled <code>true</code> to enable the buttons, <code>false</code> to disable them
   */
  void setEnableExpandCollapseButtons(boolean enabled);

  /**
   * Change the enable state of the button that changes view mode of changed files.
   *
   * @param enabled <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnabledChangeViewModeButton(boolean enabled);

  /**
   * Update button that changes view mode of changed files with proper icon and title.
   *
   * @param viewMode selected view mode (tree or list)
   */
  void updateChangeViewModeButton(ViewMode viewMode);

  /** Set custom presentation render for nodes in the panel. */
  void setTreeRender(PresentationRenderer render);

  /** Returns style of the {@link Tree} widget in the panel. */
  TreeStyles getTreeStyles();

  /** Refresh all nodes in the panel. */
  void refreshNodes();

  /** Returns paths of all shown nodes e.g. paths of the files and their parent folders. */
  Set<Path> getNodePaths();
}
