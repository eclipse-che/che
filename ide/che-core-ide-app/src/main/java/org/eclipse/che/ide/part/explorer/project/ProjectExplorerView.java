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
package org.eclipse.che.ide.part.explorer.project;

import java.util.List;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * View interface for the {@link ProjectExplorerViewImpl}.
 *
 * @author Vlad Zhukovskiy
 */
public interface ProjectExplorerView extends View<ProjectExplorerView.ActionDelegate> {

  /**
   * Activate "Go Into" mode on specified node if. Node should support this mode. See {@link
   * Node#supportGoInto()}.
   *
   * @param node node which should be activated in "Go Into" mode
   * @return true - if "Go Into" mode has been activated
   */
  boolean setGoIntoModeOn(Node node);

  /** Deactivate "Go Into" mode. */
  void setGoIntoModeOff();

  /**
   * Get "Go Into" state on current tree.
   *
   * @return true - if "Go Into" mode has been activated.
   */
  boolean isGoIntoActivated();

  void reloadChildren(Node parent);

  void reloadChildren(Node parent, boolean deep);

  /**
   * Reload children by node type. Useful method if you want to reload specified nodes, e.g.
   * External Liraries.
   *
   * @param type node type to update
   */
  void reloadChildrenByType(Class<?> type);

  /** Collapse all non-leaf nodes. */
  void collapseAll();

  /**
   * Configure tree to show or hide files that starts with ".", e.g. hidden files. Affects all
   * expanded nodes.
   *
   * @param show true - if those files should be shown, otherwise - false
   */
  void showHiddenFilesForAllExpandedNodes(boolean show);

  /**
   * Set selection on node in project tree.
   *
   * @param item node which should be selected
   * @param keepExisting keep current selection or reset it
   */
  void select(Node item, boolean keepExisting);

  /**
   * Set selection on nodes in project tree.
   *
   * @param items nodes which should be selected
   * @param keepExisting keep current selection or reset it
   */
  void select(List<Node> items, boolean keepExisting);

  /**
   * Set current part visibility state.
   *
   * @param visible true - if visible, otherwise - false
   */
  void setVisible(boolean visible);

  Tree getTree();

  /**
   * Shows placeholder instead projects tree while the workspace is loading.
   *
   * @param placeholder <b>true</b> to show placeholder
   */
  void showPlaceholder(boolean placeholder);

  interface ActionDelegate extends BaseActionDelegate {}
}
