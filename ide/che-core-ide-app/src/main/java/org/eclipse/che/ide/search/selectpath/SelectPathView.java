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
package org.eclipse.che.ide.search.selectpath;

import com.google.inject.ImplementedBy;
import java.util.List;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Represents the structure of the workspace. It needs for choosing a directory to search.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(SelectPathViewImpl.class)
public interface SelectPathView extends View<SelectPathView.ActionDelegate> {
  /** Needs for delegate some function into SelectPath view. */
  interface ActionDelegate {
    /** Sets path of the directory. */
    void setSelectedPath(String path);
  }

  /**
   * Show structure of the tree.
   *
   * @param nodes list of the project root nodes
   */
  void setStructure(List<Node> nodes);

  /** Show dialog. */
  void showDialog();
}
